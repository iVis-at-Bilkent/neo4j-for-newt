package io.github.ypankaj;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;


public class IntegratePDMap {
	// This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;
    
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
    @Description("Merge PD maps on push to database command	")
    public Stream<Output> pushToDatabase(@Name("sourceNode") Node node) {
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
    	
    	Map<String, Object> propertyMap = node.getAllProperties();
    	
    	System.out.println("Inside function, ready to print properties");
    	
    	for(String key: propertyMap.keySet()) {
    		System.out.print(key + " ");
    		String value = propertyMap.get(key).toString();
    		System.out.println(value);
    	}
    	
    	String returnStatement = "Operation pushToDatabase Done";
    	return Stream.of(new Output(returnStatement));
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
