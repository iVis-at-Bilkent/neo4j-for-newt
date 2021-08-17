package io.github.ypankaj;

import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class Helper {
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
    	String[] uoi =  (String[]) node.getProperty("uoi");
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
}
