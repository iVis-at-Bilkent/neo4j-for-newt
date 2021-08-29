package io.github.ypankaj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class Helper {
	
	protected static final Set<String> logicalOperators = new HashSet<>(Arrays.asList("and", "or", "not", "delay"));

	static Logger logger = Logger.getLogger(Helper.class.getName());
	
	private Helper() {}
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
    	return labels.iterator().next().name();
    }
    
    /**
     * Returns node's entityName
     */
    public static String getNodeEntityName(Node node) {
    	return (String) node.getProperty("entityName");
    }
    
    /**
     * Get node's uoi
     */
    public static String[] getNodeUOI(Node node) {
    	return (String[]) node.getProperty("unitsOfInformation");
    }

	/**
     * Get node's State Variable
     */
    public static String[] getNodeStateVariables(Node node) {
    	return (String[]) node.getProperty("stateVariables");
    }
    
    /**
     * 	Run Cypher query to get neighboring nodes for a node
     */
    public static Result getNeighboringNodesUsingId(Map<String, Object> queryParams, Transaction tx) {
    	String queryString = "MATCH (n)-[]-(m) WHERE id(n) = $nodeId " +
								"RETURN m;" ;
    	return tx.execute(queryString, queryParams);
    }
    
    /**
     * Return the information of neighboring nodes
     * 
     * @param Result the neighboring from which information/properties is being collected
     * @return array of AFLangNodes containing useful info/properties
     */
    public static List<AFLangNode> getNeighborhoodNodesInfoAFMap(Result neighboorhoodNodes) {
    	List<AFLangNode> neighborhoodNodesInfo = new ArrayList<>();
    	logger.info("getNeighborhoodNodesInfoAFMap ");
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
    public static int getIntersectionOfAFNodes(List<AFLangNode> list1, List<AFLangNode> list2) {
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

	/**
     * Convert JSON string to a Map to be used as params in queries
     * 
     * @param jsonStr JSON data as a string
     * @return Map representing parameters to be used in query
     */
	public static Map<String, Object> getMapFromJSONString(String jsonStr) {
		Map<String, Object> mapping = new HashMap<>();
        try {
			TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
			mapping = new ObjectMapper().readValue(jsonStr, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }

		return mapping;
	}

	/**
     * Read files as string
     * 
     * @param file path to a file
     * @return converted string
     */
	public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}
