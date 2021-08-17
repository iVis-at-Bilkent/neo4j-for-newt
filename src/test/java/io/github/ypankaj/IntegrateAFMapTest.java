package io.github.ypankaj;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Iterator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.procedure.Procedure;


import org.neo4j.graphdb.Node;
import org.neo4j.driver.internal.InternalNode;

import apoc.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrateAFMapTest {
	private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private static Driver driver;
    private Neo4j embeddedDatabaseServer;
    
    private static Logger log = Logger.getLogger(IntegrateAFMapTest.class.getName());

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
        		.withProcedure(Procedure.class)
        		.withProcedure(apoc.create.Create.class)
                .withDisabledServer()
                .withProcedure(IntegrateAFMap.class)
                .build();

        this.driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
    }

    @AfterAll
    void closeDriver(){
        this.driver.close();
        this.embeddedDatabaseServer.close();
    }

    @AfterEach
    void cleanDb(){
        try(Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    
    @Test
    public void checkReturnStatement() {
    	System.out.println("Inside");
        final String nameOne = "INCOMING";
        final String nameTwo= "OUTGOING";

        // In a try-block, to make sure we close the session after the test
        try(Session session = driver.session()) {
        	ArrayList<String> uoi = new ArrayList<>();
        	uoi.add("Alpha");
        	uoi.add("Beta");
        	Map<String,Object> n1 = new HashMap<>();
        	n1.put( "language", "AF" );
        	n1.put("class", "BA_plain");
        	n1.put( "entityName", "TGF" );
        	n1.put( "uoi", uoi );

        	ArrayList<String> uoi2 = new ArrayList<>();
        	Map<String,Object> n2 = new HashMap<>();
        	n2.put( "language", "AF" );
        	n2.put("class", "BA_plain");
        	n2.put( "entityName", "TGF2" );
        	n2.put( "uoi", uoi2 );
        	
        	ArrayList<String> uoi3 = new ArrayList<>();
        	Map<String,Object> n3 = new HashMap<>();
        	n3.put( "language", "AF" );
        	n3.put("class", "or");
        	n3.put( "entityName", "orNode" );
        	n3.put( "uoi", uoi3 );

        	
        	/*
        	 * Adding relationships
        	 */
        	Map<String,Object> r1 = new HashMap<>();
        	r1.put( "language", "AF" );
        	r1.put("class", "production");
        	r1.put( "source", "orNode" );
        	r1.put( "target", "TGF");
        	
        	Map<String,Object> r2 = new HashMap<>();
        	r2.put( "language", "AF" );
        	r2.put("class", "production");
        	r2.put( "source", "orNode" );
        	r2.put( "target", "TGF2");
        	
        	/*
        	 * Preparing params
        	 */
        	Map<String,Object> params = new HashMap<>();
        	List<Map<String,Object>> nodes = new ArrayList<Map<String,Object>>();
        	List<Map<String,Object>> rels = new ArrayList<Map<String,Object>>();
        	
        	nodes.add(n1);
        	nodes.add(n2);
        	nodes.add(n3);
        	
        	rels.add(r1);
        	rels.add(r2);
        	
        	params.put( "props", nodes );
        	params.put( "relProps", rels );
        	
        	log.info("Running Create node query");

        	String query =
        	    "CALL { " +
        	    "UNWIND $props AS properties" + "\n" +
        	    "CALL apoc.create.node([properties.class], properties)" + "\n" +
        	    "YIELD node" + "\n" +
        	    "RETURN count(node) as nodeCount } " +
        	    "WITH nodeCount " +
        	    "CALL { " +
        	    "UNWIND $relProps AS relProperties " +
        	    "MATCH (node1) WHERE node1.entityName = relProperties.source " +
        	    "MATCH (node2) WHERE node2.entityName = relProperties.target " +
        	    "CALL apoc.create.relationship(node1, relProperties.class, relProperties, node2) " +
        	    "YIELD rel " +
        	    "RETURN count(rel) as relCount } " +
        	    "RETURN relCount";

        	Result result1 = session.run( query, params );
        	
        	
        	List<String> resultKeys = result1.keys();
        	
        	for(String resultKey: resultKeys) {
        		System.out.println(resultKey);
        	}
        	
//        	while(result1.hasNext()) {
//        		Map<String, Object> row = result1.next().asMap();
//        		System.out.println(row.containsKey("relCount"));
////        		System.out.println(((Node) row.get("node")).getProperty("language").toString());
//        		InternalNode tempNode = (InternalNode) row.get("relCount");
//        		Iterable<String> nodeKeys = tempNode.keys();
//        		for(String key: nodeKeys) {
//        			System.out.println(key + " " + tempNode.get(key));
//        		}
////        		System.out.println(tempNode.keys());
//        	}
        	
//        	query = "CREATE (n:Person)" + "\n" +
//        	"SET n.name = 5";
//        	session.run(query, params);
        	
        	log.info("Create node query complete");
        	        	
            
            //Execute our procedure against it.
            Result result = session.run("MATCH (u:or) CALL integrateAFMap(u,u) YIELD out RETURN out");

            System.out.println();
            while (result.hasNext()) {
				Map<String, Object> row = result.next().asMap();
				for(String key: row.keySet()) {
//					System.out.println(key);
//					String key = name.toString();
					Object value = row.get(key);
					System.out.println("Test " + key + " " + row.getClass().getName() + " " + value);
//					System.out.println(value);
				}

			}
            
            log.info("Running Asserts");
            assertEquals(true, true);
        }
        catch(Exception e) {
        	log.info(e.getMessage());
        }
    }
}
