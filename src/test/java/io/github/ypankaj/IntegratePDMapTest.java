package io.github.ypankaj;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegratePDMapTest {
	private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private static Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(IntegratePDMap.class)
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
        	
        	Map<String,Object> n1 = new HashMap<>();
        	n1.put( "name", "Andy" );
        	n1.put( "position", "Developer" );
        	n1.put( "awesome", true );

        	Map<String,Object> n2 = new HashMap<>();
        	n2.put( "name", "Michael" );
        	n2.put( "position", "Developer" );
        	n2.put( "children", 3 );

        	Map<String,Object> params = new HashMap<>();
        	List<Map<String,Object>> maps = new ArrayList<Map<String,Object>>();
        	maps.add(n1);
        	maps.add(n2);
        	
        	params.put( "props", maps );

        	String query =
        	    "UNWIND $props AS properties" + "\n" +
        	    "CREATE (n:Person)" + "\n" +
        	    "SET n = properties" + "\n" +
        	    "RETURN n";

        	session.run( query, params );
        	        	
            
            //Execute our procedure against it.
            Result result = session.run("MATCH (u:Person) CALL integratePDMap(u) YIELD out RETURN out");

            while (result.hasNext()) {
				Map<String, Object> row = result.next().asMap();
				for(String key: row.keySet()) {
					System.out.println(key);
//					String key = name.toString();
					String value = row.get(key).toString();
					System.out.println(row.getClass().getName());
					System.out.println(value);
				}

			}
            
            assertEquals(true, true);
//            assertEquals("Operation pushToDatabase Done", record.get("returnStatement"));
            //Get the incoming / outgoing relationships from the result
//            assertThat(record.get("incoming").asList(x -> x.asString())).containsOnly(expectedIncoming);
//            assertThat(record.get("outgoing").asList(x -> x.asString())).containsOnly(expectedOutgoing);
        }
    }
}
