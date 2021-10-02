package io.github.ypankaj;

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
     * This procedure takes a Node and gets the relationships going in and out of it
     *
     * @param node  The node to get the relationships for
     * @return  A RelationshipTypes instance with the relations (incoming and outgoing) for a given node.
     */
    @Procedure(value = "integratePDMap")
    @Description("Merge PD maps on push to database command")
    public Stream<Output> pushToDatabasePD(@Name("node") Node node) { 

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
            String getMatchingNodeQuery = "MATCH (parentNode:" + parentPDNode.getLabel() +  ")<-[:belongs_to_compartment|belongs_to_submap|belongs_to_complex]-(n:" + unprocessedNode.getLabel() + ") \n" +
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

        // TODO: Compare the neighbors of the unprocessed node and matching
        // nodes to determine if the unprocessed node exists in database or not
        boolean exists = doesNodeAlreadyExistInDatabase(node, matchingNodes);
        


    	
    	String returnStatement = "Operation pushToDatabase Done";
    	return Stream.of(new Output(returnStatement));
    }

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

    boolean doesNodeAlreadyExistInDatabase(Node unprocessedNode, List<Node> nodes) {

        return true;
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
}
