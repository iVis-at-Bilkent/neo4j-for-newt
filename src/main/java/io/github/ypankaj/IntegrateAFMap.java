package io.github.ypankaj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
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
    
    private static Logger logg = Logger.getLogger(IntegrateAFMap.class.getName());
    
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
    @Description("Merge AF maps on push to database command	")
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
    	
    	String returnStatement = "Operation pushToDatabase Done";
//    	return Stream.of(new Output(returnStatement));
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
    	
    	HashMap<String, Object> nodeInfo = new HashMap<String, Object>();
    	nodeInfo.put("nodeLabel", nodeLabel);
    	nodeInfo.put("nodeEntityName", nodeEntityName);
    	nodeInfo.put("nodeUOI", nodeUOI);
    	
    	
    	
    	// contains id of the node
    	Map<String, Object> queryParam = new HashMap<String, Object>();
    	queryParam.put("nodeId", nodeId);
    	
    	// Store neighboring nodes info
    	ArrayList<AFLangNode> neighborhoodNodesInfo;
    	
    	// get and store neighboring nodes info
    	try ( Transaction tx = db.beginTx() ) {
    		Result neighborhoodNodes = Helper.getNeighboringNodesUsingId(queryParam, tx);
    		neighborhoodNodesInfo = Helper.getNeighborhoodNodesInfoAFMap(neighborhoodNodes);
    	
    		String getMatchingNodeQuery = "MATCH (n) WHERE labels(n) = [$nodeLabel] AND n.entityName = $nodeEntityName AND n.unitsOfInformation = $nodeUOI " +
    									"RETURN n";
    			
		
			Result matchingNodesResult = tx.execute(getMatchingNodeQuery, nodeInfo);
			System.out.println("getMatchingNodeQuery");
			
			int maximumMatchingNeighbors = 0;
			
			// loop over all the records
			while(matchingNodesResult.hasNext()) {
				Map<String, Object> row = matchingNodesResult.next();
				
				// loop over the return key, value. Here only key is n
				for(String key: row.keySet()) {
					System.out.println(key);
					Node matchingNode = (Node) row.get(key);
					System.out.println(matchingNode.getId());
					
					// get neighboring nodes and its properties for this matching node
					Map<String, Object> matchingNodeQueryParam = new HashMap<String, Object>();
					matchingNodeQueryParam.put("nodeId", matchingNode.getId());
					Result matchingNodeNeighborhoodNodes = Helper.getNeighboringNodesUsingId(matchingNodeQueryParam, tx);
					ArrayList<AFLangNode> matchingNodeNeighborhoodNodesInfo = Helper.getNeighborhoodNodesInfoAFMap(matchingNodeNeighborhoodNodes);
					
					int matchCount = Helper.getIntersectionOfAFNodes(matchingNodeNeighborhoodNodesInfo, neighborhoodNodesInfo);
					System.out.println("matchCount " + matchCount);
					
					if(matchCount > maximumMatchingNeighbors) {
						matchingNodeToReturn = matchingNode;
						maximumMatchingNeighbors = matchCount;
					}
				}
			}
		}
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
    
    
    
    
    /**
     * Adds the distinct type of a relationship to the given List<String>
     *
     * @param list  the list to add the distinct relationship type to
     * @param relationship  the relationship to get the name() from
     */
    private void AddDistinct(List<String> list, Relationship relationship){
        AddDistinct(list, relationship.getType().name());
    }

    /**
     * Adds an item to a List only if the item is not already in the List
     *
     * @param list  the list to add the distinct item to
     * @param item  the item to add to the list
     */
    private <T> void AddDistinct(List<T> list, T item){
        if(!list.contains(item))
            list.add(item);
    }

    /**
     * This is the output record for our search procedure. All procedures
     * that return results return them as a Stream of Records, where the
     * records are defined like this one - customized to fit what the procedure
     * is returning.
     * <p>
     * These classes can only have public non-final fields, and the fields must
     * be one of the following types:
     *
     * <ul>
     *     <li>{@link String}</li>
     *     <li>{@link Long} or {@code long}</li>
     *     <li>{@link Double} or {@code double}</li>
     *     <li>{@link Number}</li>
     *     <li>{@link Boolean} or {@code boolean}</li>
     *     <li>{@link Node}</li>
     *     <li>{@link org.neo4j.graphdb.Relationship}</li>
     *     <li>{@link org.neo4j.graphdb.Path}</li>
     *     <li>{@link Map} with key {@link String} and value {@link Object}</li>
     *     <li>{@link List} of elements of any valid field type, including {@link List}</li>
     *     <li>{@link Object}, meaning any of the valid field types</li>
     * </ul>
     */
    public static class RelationshipTypes {
        // These records contain two lists of distinct relationship types going in and out of a Node.
        public List<String> outgoing;
        public List<String> incoming;

        public RelationshipTypes(List<String> incoming, List<String> outgoing) {
            this.outgoing = outgoing;
            this.incoming = incoming;
        }
    }
}
