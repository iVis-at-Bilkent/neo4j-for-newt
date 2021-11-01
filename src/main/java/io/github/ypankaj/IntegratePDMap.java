package io.github.ypankaj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ypankaj.enums.PDNode;


public class IntegratePDMap {

	static Logger logger = LoggerFactory.getLogger(IntegratePDMap.class);

	// This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;
    
    @Context
	public GraphDatabaseService db;
    
    @Context
    public Transaction tx;
    
    public class Output {

		Output(String out) {
			this.out = out;
		}

		public String out;
	}

    /**
     * Write node to the database if it does not already exists in it.
     *
     * @param node  entity to be pushed to the database
     * @return  ideally should be nothing but still not sure about it
     */
    @Procedure(value = "integratePDMap")
    @Description("Merge PD maps on push to database command")
    public Stream<Output> pushToDatabasePD(@Name("node") Node node) { 

        boolean nodeExists = checkIfNodeExistsInDatabase(node);

        String returnStatement = "Operation pushToDatabase Done";
    	return Stream.of(new Output(returnStatement));
    }

    /**
     * Check if the unprocessed node under consideration already exists in
     * database or not. This is done by comparing the neighbors and parent of
     * the node under consideration to the nodes present in the database with
     * isProcessed property equals to 1.
     * @param node node under consideration
     * @return true if the node already exists in the database else false
     */
    public boolean checkIfNodeExistsInDatabase(Node node) {

        List<Node> matchingNodes = getMatchingNodesByComparingNodeAndItsParentProperties(node);
        
        logger.info("Comparing neighboring nodes");
        checkIfNodeExistsInDatabaseByComparingNeighboringNodes(node, matchingNodes);
        logger.info("Neighboring node comparision complete");
        return true;
    }

    /**
     * Compare node's properties and its parent's property to get matching nodes
     * already present in the database. Parent node can be null too and its
     * handled here.
     * @param node node under consideration
     * @return list of matching nodes
     */
    List<Node> getMatchingNodesByComparingNodeAndItsParentProperties(Node node) {
        List<Node> matchingNodes;

        // get PDLangNode from Node
        PDLangNode unprocessedNode = new PDLangNode(node);
        Node parentNode = getParentNodeFromNodePD(node);
        PDLangNode parentPDNode = new PDLangNode(parentNode);

        logger.info("parentPDNode toString = {}", parentPDNode.toString());
        if(parentNode != null) {
            logger.info("Node's parent label {}", Helper.getNodeLabel(parentNode));
            logger.info("Node's parent entity name {}", Helper.getNodeEntityName(parentNode));
            
            Map<String, Object> nodeProps = getPropertiesAsMap(node);
            Map<String, Object> parentProps = getPropertiesAsMap(parentNode);
            
            Map<String, Object> queryProps = new HashMap<>();
            queryProps.put("nodeProps", nodeProps);
            queryProps.put("parentProps", parentProps);
            
            // TODO: Match isProcessed property = 1 in the query to match nodes
            // which are already present in DB
            String getMatchingNodeQuery = "MATCH (parentNode:" + parentPDNode.getLabel() +  ")<-[:belongs_to_compartment|belongs_to_submap|belongs_to_complex]-(n:" + unprocessedNode.getLabel() + ") \n" +
                                    " WHERE n.entityName = $nodeProps.entityName AND  parentNode.entityName = $parentProps.entityName \n" + // labels
                                    " AND n.unitsOfInformation = $nodeProps.unitsOfInformation AND  parentNode.unitsOfInformation = $parentProps.unitsOfInformation \n" + // units of information 
                                    " AND n.stateVariables = $nodeProps.stateVariables AND  parentNode.stateVariables = $parentProps.stateVariables \n" + // state variables
                                    " AND n.multimer = $nodeProps.multimer AND  parentNode.multimer = $parentProps.multimer \n" + // multimer
                                    " AND n.cloneMarker = $nodeProps.cloneMarker AND  parentNode.cloneMarker = $parentProps.cloneMarker \n" + // clone marker
                                    " AND n.cloneLabel = $nodeProps.cloneLabel AND  parentNode.cloneLabel = $parentProps.cloneLabel \n" + // clone label
                                    " RETURN n \n";

            logger.info("getMatchingNodeQuery = {}", getMatchingNodeQuery);

            Result result = tx.execute(getMatchingNodeQuery, queryProps);

            matchingNodes = result.stream()
                    .map(row -> (Node)row.get("n"))
                    .collect(Collectors.toList());

        }
        else {
            logger.info("parentNode is null");
            // TODO: Match isProcessed property = 1 in the query to match nodes
            // which are already present in DB
            String getMatchingNodeQuery = "MATCH (n:" + unprocessedNode.getLabel() + ") \n" +
                                    " WHERE n.entityName = $nodeProps.entityName \n" + // labels
                                    " AND n.unitsOfInformation = $nodeProps.unitsOfInformation \n" + // units of information 
                                    " AND n.stateVariables = $nodeProps.stateVariables \n" + // state variables
                                    " AND n.multimer = $nodeProps.multimer \n" + // multimer
                                    " AND n.cloneMarker = $nodeProps.cloneMarker \n" + // clone marker
                                    " AND n.cloneLabel = $nodeProps.cloneLabel \n" + // clone label
                                    " RETURN n \n";

            logger.info("getMatchingNodeQuery = {}", getMatchingNodeQuery);
            Result result = tx.execute(getMatchingNodeQuery);

            matchingNodes = result.stream()
                    .map(row -> (Node)row.get("n"))
                    .collect(Collectors.toList());
                    
        }

        return matchingNodes;
    }

    /**
     * Returns parent node of input node.Returns null if node has no parent.
     * @param node input node for which parent node is to be returned.
     * @return
     */
    public Node getParentNodeFromNodePD(Node node) {
        long id = Helper.getNodeId(node);
        return getParentNodeFromNodeIdPD(id);
    }

    public Node getParentNodeFromNodeIdPD(long id) {
        String query = "MATCH (parentNode)<-[:belongs_to_compartment|belongs_to_submap|belongs_to_complex]-(m) WHERE id(m) = $id " +
                        "RETURN parentNode";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", id);

        Result result = tx.execute(query, queryParams);

        Node parentNode = null;
        if (result.hasNext()) {
            Map<String, Object> row = result.next();
            parentNode = (Node) row.get("parentNode");
        }

        return parentNode;
    }

    /**
     * Check if the unprocessed node under consideration already exists in
     * database or not. This is done by comparing the neighbors of the nodes.
     * @param unprocessedNode node with isProcessed property = 1 i.e. deposited
     *                        in the database just now for the algo to work
     * @param nodes matching nodes with isProcessed property = 0 i.e. already
     *              in the database
     * @return true if unprocessedNode is already there in the database.
     */
    boolean checkIfNodeExistsInDatabaseByComparingNeighboringNodes(Node unprocessedNode, List<Node> matchingNodes) {
        int maxOverlap = 0;
        List<Node> unprocessedNodesNeighbors = getNeighboringNodes(unprocessedNode);
        List<PDLangNode> unprocessedNodesNeighborsPDLang = convertNodeToPDLangNode(unprocessedNodesNeighbors);
        for(Node matchingNode: matchingNodes) {
            int num = getMatchingNeighborhoodNodesNumber(unprocessedNodesNeighborsPDLang, matchingNode);
            maxOverlap = Math.max(maxOverlap, num);
        }
        // matching criteria for now, if atleast one neighbor matches then
        // consider the nodes same and return true
        return maxOverlap > 0;
    }

    /**
     * Get the common neighors of the unprocessedNode and the matchingNode
     * @param unprocessedNode node whose neigbors are to be compared
     * @param matchingNode another node whose neigbors is to be compared
     * @return the number of common neighbors
     */
    int getMatchingNeighborhoodNodesNumber(Node unprocessedNode, Node matchingNode) {
        List<Node> unprocessedNodesNeighbors = getNeighboringNodes(unprocessedNode);
        List<PDLangNode> unprocessedNodesNeighborsPDLang = convertNodeToPDLangNode(unprocessedNodesNeighbors);
        return getMatchingNeighborhoodNodesNumber(unprocessedNodesNeighborsPDLang, matchingNode);
    }

    /**
     * Compare the nodes in unprocessedNodesNeighbors with the neighbors of
     * matchedNode and return the common nodes.
     * @param unprocessedNodesNeighbors neighbors of the unprocessed nodes
     * @param matchedNode node whose neighbor will be compare to the above list
     * @return the number of common neighbors
     */
    int getMatchingNeighborhoodNodesNumber(List<PDLangNode> unprocessedNodesNeighbors, Node matchedNode) {
        List<Node> matchedNodesNeighbors = getNeighboringNodes(matchedNode);
        List<PDLangNode> matchedNodesNeighborsPDLang = convertNodeToPDLangNode(matchedNodesNeighbors);
        return getOverlapNumber(unprocessedNodesNeighbors, matchedNodesNeighborsPDLang);
    }

    List<Node> getNeighboringNodes(Node node) {
        List<Node> neighboringNodes = new ArrayList<>();

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("nodeId", Helper.getNodeId(node));
        String query = "MATCH (n)-[]-(m) WHERE id(n) = $nodeId " +
                        "RETURN collect(distinct m) as mNodeList";
        Result result = tx.execute(query, queryParams);
        
        while(result.hasNext()) {
            Map<String, Object> record = result.next();
            @SuppressWarnings("unchecked") List<Node> nodeList = (List<Node>) record.get("mNodeList");
            for(Node curNode: nodeList) {
                neighboringNodes.add(curNode);
            }
        }

        return neighboringNodes;
    }

    public Map<String, Object> getPropertiesAsMap(Node node) {
    	String label = Helper.getNodeLabel(node);
    	String entityName = Helper.getNodeEntityName(node);
    	String[] UOI = Helper.getNodeUOI(node);
        String[] stateVariables = Helper.getNodeStateVariables(node);
        Boolean multimer = Helper.getNodeMultimer(node);
        Boolean cloneMarker = Helper.getNodeCloneMarker(node);
        String cloneLabel = Helper.getNodeCloneLabel(node);

        Map<String, Object> props = new HashMap<>();

        props.put(PDNode.LABEL.toString(), label);
        props.put(PDNode.ENTITY_NAME.toString(), entityName);
        props.put(PDNode.UOI.toString(), UOI);
        props.put(PDNode.STATE_VARIABLES.toString(), stateVariables);
        props.put(PDNode.MULTIMER.toString(), multimer);
        props.put(PDNode.CLONE_MARKER.toString(), cloneMarker);
        props.put(PDNode.CLONE_LABEL.toString(), cloneLabel);

        return props;
    }

    List<PDLangNode> convertNodeToPDLangNode(List<Node> nodes) {
        List<PDLangNode> pdLangNodes = new ArrayList<>();
        for(Node node: nodes) {
            pdLangNodes.add(new PDLangNode(node));
        }

        return pdLangNodes;
    }

    PDLangNode convertNodeToPDLangNode(Node node) {
        return new PDLangNode(node);
    }

    /**
     * Return the numer of nodes that overlap between the two lists
     * @param l1 list 1
     * @param l2 list 2
     * @return number of matching nodes
     */
    int getOverlapNumber(List<PDLangNode> l1, List<PDLangNode> l2) {
        int matchCount = 0;
    	
    	for(PDLangNode pdLangNode1: l1) {
    		for(PDLangNode pdLangNode2: l2) {
    			if(pdLangNode1.equals(pdLangNode2)) {
    				matchCount++;
    				break;
    			}
    		}
    	}
    	return matchCount;
    }
}
