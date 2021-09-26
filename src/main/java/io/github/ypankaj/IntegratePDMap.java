package io.github.ypankaj;

import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    	long id = Helper.getNodeId(node);
    	String label = Helper.getNodeLabel(node);
    	String entityName = Helper.getNodeEntityName(node);
    	String[] UOI = Helper.getNodeUOI(node);
        String[] stateVariables = Helper.getNodeStateVariables(node);
        Boolean multimer = Helper.getNodeMultimer(node);
        Boolean cloneMarker = Helper.getNodeCloneMarker(node);
        String cloneLabel = Helper.getNodeCloneLabel(node);


        // get parent node
        Node parentNode = getParentNodeFromNodePD(node);

        if(parentNode != null) {
            logger.info("Node's parent label {}", Helper.getNodeLabel(parentNode));
            logger.info("Node's parent entity name {}", Helper.getNodeEntityName(parentNode));
        }
        else {
            logger.info("parentNode is null");
        }
        


    	
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
}
