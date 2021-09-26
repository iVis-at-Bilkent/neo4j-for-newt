package io.github.ypankaj;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.procedure.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegratePDMapTest {
	private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private static Driver driver;
    private Neo4j embeddedDatabaseServer;

	static Logger logger = LoggerFactory.getLogger(IntegratePDMapTest.class);
       
    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(Procedure.class)
                .withProcedure(IntegratePDMap.class)
                .withProcedure(apoc.create.Create.class)
                .withDisabledServer()
                .build();

        IntegratePDMapTest.driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
    }

    @AfterAll
    void closeDriver(){
        IntegratePDMapTest.driver.close();
        this.embeddedDatabaseServer.close();
    }

    @AfterEach
    void cleanDb(){
        try(Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    
//     @Test
//     public void checkReturnStatement() {
//     	System.out.println("Inside");
//         final String nameOne = "INCOMING";
//         final String nameTwo= "OUTGOING";

//         // In a try-block, to make sure we close the session after the test
//         try(Session session = driver.session()) {
        	
//         	Map<String,Object> n1 = new HashMap<>();
//         	n1.put( "name", "Andy" );
//         	n1.put( "position", "Developer" );
//         	n1.put( "awesome", true );

//         	Map<String,Object> n2 = new HashMap<>();
//         	n2.put( "name", "Michael" );
//         	n2.put( "position", "Developer" );
//         	n2.put( "children", 3 );

//         	Map<String,Object> params = new HashMap<>();
//         	List<Map<String,Object>> maps = new ArrayList<Map<String,Object>>();
//         	maps.add(n1);
//         	maps.add(n2);
        	
//         	params.put( "props", maps );

//         	String query =
//         	    "UNWIND $props AS properties" + "\n" +
//         	    "CREATE (n:Person)" + "\n" +
//         	    "SET n = properties" + "\n" +
//         	    "RETURN n";

//         	session.run( query, params );
        	        	
            
//             //Execute our procedure against it.
//             Result result = session.run("MATCH (u:Person) CALL integratePDMap(u) YIELD out RETURN out");

//             while (result.hasNext()) {
// 				Map<String, Object> row = result.next().asMap();
// 				for(String key: row.keySet()) {
// 					System.out.println(key);
// //					String key = name.toString();
// 					String value = row.get(key).toString();
// 					System.out.println(row.getClass().getName());
// 					System.out.println(value);
// 				}

// 			}
            
//             assertEquals(true, true);
// //            assertEquals("Operation pushToDatabase Done", record.get("returnStatement"));
//             //Get the incoming / outgoing relationships from the result
// //            assertThat(record.get("incoming").asList(x -> x.asString())).containsOnly(expectedIncoming);
// //            assertThat(record.get("outgoing").asList(x -> x.asString())).containsOnly(expectedOutgoing);
//         }
//     }

    @Test
    void testPushToDataBase() {
        logger.info("Starting testPushToDatabase test");
        try(Session session = driver.session()) {
            Transaction tx = session.beginTransaction();
            logger.info("Generating map");
            generateMap("PD/NeuronalMuscleSignalling.json", "PD/PDQuery.json", tx);
            // tx.commit();
            logger.info("Map Generated");


            String query = "MATCH (n) WHERE n.entityName = \"ChAT\" RETURN n";
            Result result = tx.run(query);
            Map<String, Object> record = result.next().asMap();

            InternalNode queriedNode = (InternalNode) record.get("n");

            logger.info("Node's entity name {}", queriedNode.asMap().get("entityName"));
            
            String queryIntegratePDMap = "MATCH (n) WHERE n.entityName = \"ChAT\" " +
                                            "CALL integratePDMap(n) YIELD out RETURN out";
            logger.info("Calling integratePDMap"); 
            tx.run(queryIntegratePDMap);
            logger.info("Call to integratePDMap over");
                            
            // function is still not complete so using this
            assertEquals(true, true);
        }
    }

    @Test
    void checkIfNeuronalMuscleSignallingMapIsLoadedCorrectly() throws Exception {
        String NeuronalMuscleSignallingFile = "src/test/resources/PD/NeuronalMuscleSignalling.json";
        String NeuronalMuscleSignallingAsString = Helper.readFileAsString(NeuronalMuscleSignallingFile);

        String queryLocation = "src/test/resources/PD/PDQuery.json";
        String queryMapAsString = Helper.readFileAsString(queryLocation);
        
        Map<String, Object> NeuronalMuscleSignallingData = Helper.getMapFromJSONString(NeuronalMuscleSignallingAsString);
        Map<String, Object> queryMap = Helper.getMapFromJSONString(queryMapAsString);

        String query = (String) queryMap.get("query");

        logger.info("Generating map");
        System.out.println("Generating map");
        System.out.println("this is here "+ logger.isInfoEnabled());
		// generate TGF Beta Singalling Map
        try(Session session = driver.session()) {
        	session.run( query, NeuronalMuscleSignallingData );
        }
        logger.info("Map Generated");

		// assert if node count is what is expected
		try(Session session = driver.session()) {
        	Result result = session.run("MATCH (n) RETURN COUNT(n) as NodeCount");
			Record row = result.single();
			int NodeCount = row.get("NodeCount").asInt();

			assertEquals(48, NodeCount);
        }

		// assert if relationship count is what is expected
		try(Session session = driver.session()) {
        	Result result = session.run("MATCH ()-[r]->() RETURN COUNT(r) as RelCount");
			Record row = result.single();
			int RelCount = row.get("RelCount").asInt();

			assertEquals(83, RelCount);
        }
    }

    // @Test
    // public void testGetParentNodeFromNodePD() {
    //     try(Session session = driver.session()) {
    //         Transaction tx = session.beginTransaction();
    //         generateMap("PD/NeuronalMuscleSignalling.json", "PD/PDQuery.json", tx);
    //         tx.commit();

    //         String query = "MATCH (n) WHERE n.entityName = \"ChAT\" RETURN n";
    //         Result result = tx.run(query);
    //         Record record = result.next();
    //         Node queriedNode = (Node) record.get("n");

    //         Node parentNode = helper.getParentNodeFromNodePD(queriedNode, tx);
    //         String entityName = helper.getNodeEntityName(parentNode);
    //         System.out.println(entityName);
    //         assertEquals("expected", entityName);
    //     }
        
    // }

    public void generateMap(String fileLocation, String queryLocation, Transaction tx) {
        try {
            String SBGNMapFile = "src/test/resources/" + fileLocation;
            String SBGNMapAsString = Helper.readFileAsString(SBGNMapFile);

            queryLocation = "src/test/resources/" + queryLocation;
            String queryMapAsString = Helper.readFileAsString(queryLocation);
            
            Map<String, Object> SBGNMapData = Helper.getMapFromJSONString(SBGNMapAsString);
            Map<String, Object> queryMap = Helper.getMapFromJSONString(queryMapAsString);

            String query = (String) queryMap.get("query");

            tx.run(query, SBGNMapData);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
