package io.github.ypankaj;

import java.util.Map;
import java.util.stream.Stream;

import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class PushToDatabase {
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
    @Procedure(value = "pushToDatabasePD")
    @Description("Merge PD maps on push to database command	")
    public Stream<Output> pushToDatabasePD(@Name("sourceNode") Node node) {
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
    	
    	for(String key: propertyMap.keySet()) {
    		System.out.print(key + " ");
    		String value = propertyMap.get(key).toString();
    		System.out.println(value);
    	}
    	
    	String returnStatement = "Operation pushToDatabase Done";
    	return Stream.of(new Output(returnStatement));
    }

}
