package io.github.ypankaj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


public class IntegrateAFMap {
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
     * @param sourceNodeG1  Source node from which the relationship originates
     * @return  A RelationshipTypes instance with the relations (incoming and outgoing) for a given node.
     */
    @Procedure(value = "integrateAFMap")
    @Description("Merge AF maps on push to database command")
    public Stream<SourceAndTargetNodes> pushToDatabase(@Name("sourceNodeG1") Node sourceNodeG1, @Name("targetNodeG1") Node targetNodeG1) {
        /*
         * Make graph from given data
         * For each node get its parent node. 
         * Match the node in the graph, check if the outgoing edges are same
         * and adjacent nodes are same or not.
         * If node not found then add node.
         * 
         * For Process nodes just check if the adjacent and edges are subset or not.
         * 
         * Finally check for edges. Get adjacent nodes and check if 
         */
    	
    	Node matchingSourceNode = getMatchingNode(sourceNodeG1);
    	Node matchingTargetNode = getMatchingNode(targetNodeG1);    	
    	
    	return Stream.of(new SourceAndTargetNodes(matchingSourceNode, matchingTargetNode));
    }    
    
    /**
     * Return the matching node already present in the database
     *
     * @param node  used to compare any other node for matching purpose
     * @return node which matches with the input node the most
     */
    public Node getMatchingNode(Node node) {
    	// Extract information from the node
    	Node matchingNodeToReturn = null;
    	long nodeId = Helper.getNodeId(node);
    	String nodeLabel = Helper.getNodeLabel(node);
    	String nodeEntityName = Helper.getNodeEntityName(node);
    	String[] nodeUOI = Helper.getNodeUOI(node);
    	
    	HashMap<String, Object> nodeInfo = new HashMap<>();
    	nodeInfo.put("nodeLabel", nodeLabel);
    	nodeInfo.put("nodeEntityName", nodeEntityName);
    	nodeInfo.put("nodeUOI", nodeUOI);
    	
    	
    	
    	// contains id of the node
    	Map<String, Object> queryParam = new HashMap<>();
    	queryParam.put("nodeId", nodeId);
    	
    	// Store neighboring nodes info
    	List<AFLangNode> neighborhoodNodesInfo;
    	
    	// get and store neighboring nodes info
		Result neighborhoodNodes = Helper.getNeighboringNodesUsingId(queryParam, tx);
		neighborhoodNodesInfo = Helper.getNeighborhoodNodesInfoAFMap(neighborhoodNodes);
	
		String getMatchingNodeQuery = "MATCH (n) WHERE labels(n) = [$nodeLabel] AND n.entityName = $nodeEntityName AND n.unitsOfInformation = $nodeUOI AND n.processed = 1 " +
									"RETURN n";
			
	
		Result matchingNodesResult = tx.execute(getMatchingNodeQuery, nodeInfo);
		System.out.println("getMatchingNodeQuery");
		
		int maximumMatchingNeighbors = 0;
		
		// loop over all the records
		while(matchingNodesResult.hasNext()) {
			Map<String, Object> row = matchingNodesResult.next();
			
			// loop over the return key, value. Here only key is n
			for(Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey();
                Node matchingNode = (Node) entry.getValue();
				System.out.println(key);
				System.out.println(matchingNode.getId());
				
				// get neighboring nodes and its properties for this matching node
				Map<String, Object> matchingNodeQueryParam = new HashMap<>();
				matchingNodeQueryParam.put("nodeId", matchingNode.getId());
				Result matchingNodeNeighborhoodNodes = Helper.getNeighboringNodesUsingId(matchingNodeQueryParam, tx);
				List<AFLangNode> matchingNodeNeighborhoodNodesInfo = Helper.getNeighborhoodNodesInfoAFMap(matchingNodeNeighborhoodNodes);
				
				int matchCount = Helper.getIntersectionOfAFNodes(matchingNodeNeighborhoodNodesInfo, neighborhoodNodesInfo);
				System.out.println("matchCount " + matchCount);
				
				if(matchCount > maximumMatchingNeighbors) {
					matchingNodeToReturn = matchingNode;
					maximumMatchingNeighbors = matchCount;
				}
			}
		}
//		}
    	// Get matching node with same parameters but which already exists in the database i.e. node.processed = 1
//    	String processedMatchingNodeQuery = "MATCH (n) WHERE labels(n) = [$label] AND 
		
		return matchingNodeToReturn;
    }
    
    /**
     * Complete this later
     *
     */
    public static class SourceAndTargetNodes {
        // These records contain two lists of distinct relationship types going in and out of a Node.
        public Node matchingSourceNode;
        public Node matchingTargetNode;

        public SourceAndTargetNodes(Node matchingSourceNode, Node matchingTargetNode) {
            this.matchingSourceNode = matchingSourceNode;
            this.matchingTargetNode = matchingTargetNode;
        }
    }
}
