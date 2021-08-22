package io.github.ypankaj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class Helper {
	
	public static Set<String> logicalOperators = new HashSet<>(Arrays.asList("and", "or", "not", "delay"));
	
	/** 
	 * Returns the id of the node
	 */
	public static long getNodeId(Node node) {
		return node.getId();
	}
	/**
     *  Returns the label of a node
     */
    public static String getNodeLabel(Node node) {
    	Iterable<Label> labels = node.getLabels();
    	String label = labels.iterator().next().name();
    	
    	return label;
    }
    
    /**
     * Returns node's entityName
     */
    public static String getNodeEntityName(Node node) {
    	String entityName = (String) node.getProperty("entityName");
    	return entityName;
    }
    
    /**
     * Get node's uoi
     */
    public static String[] getNodeUOI(Node node) {
    	String[] uoi =  (String[]) node.getProperty("unitsOfInformation");
    	return uoi;
    }
    
    /**
     * 	Run Cypher query to get neighboring nodes for a node
     */
    public static Result getNeighboringNodesUsingId(Map<String, Object> queryParams, Transaction tx) {
    	String queryString = "MATCH (n)-[]-(m) WHERE id(n) = $nodeId " +
								"RETURN m;" ;
    	Result result = tx.execute(queryString, queryParams);
    	
    	return result;
    }
    
    /**
     * Return the information of neighboring nodes
     * 
     * @param Result the neighboring from which information/properties is being collected
     * @return array of AFLangNodes containing useful info/properties
     */
    public static ArrayList<AFLangNode> getNeighborhoodNodesInfoAFMap(Result neighboorhoodNodes) {
    	ArrayList<AFLangNode> neighborhoodNodesInfo = new ArrayList<AFLangNode>();
    	
    	while( neighboorhoodNodes.hasNext()) {
			Map<String, Object> row = neighboorhoodNodes.next();
			for(Object value: row.values()) {
				Node tempNode = (Node) value;
				System.out.print(tempNode.getId() + " ");
				Iterable<Label> labels = tempNode.getLabels();
				String currentLabel = labels.iterator().next().name();
				
				String currentEntityName = (String) tempNode.getProperty("entityName");
				String[] uoi =  (String[]) tempNode.getProperty("unitsOfInformation");
				
				System.out.println(currentLabel + " " + currentEntityName + " " + Arrays.toString(uoi));
				
				AFLangNode currentNode = new AFLangNode(currentLabel, currentEntityName, uoi);
				neighborhoodNodesInfo.add(currentNode);
			}
    	}
    	return neighborhoodNodesInfo;
    }
    
    /**
     * Get the number of matching AFNodes in two sets of nodes
     * 
     * @param set1 fist set
     * @param set2 second set
     * @return number of matching nodes
     */
    public static int getIntersectionOfAFNodes(ArrayList<AFLangNode> list1, ArrayList<AFLangNode> list2) {
    	int matchCount = 0;
    	
    	for(AFLangNode afLangNodeSet1: list1) {
    		for(AFLangNode afLangNodeSet2: list2) {
    			if(afLangNodeSet1.equals(afLangNodeSet2)) {
    				matchCount++;
    				break;
    			}
    		}
    	}
    	return matchCount;
    }
}
