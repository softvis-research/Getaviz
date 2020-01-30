package org.getaviz.generator.extract;

import org.getaviz.generator.database.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class ScanJarTest {

    private static DatabaseConnector connector;
    private static GraphDatabaseService graphDb;
    private static String pathJQAssistant = ""; // replace with local path to jQAssistant

    @BeforeAll
    static void setup() {
        checkRequirements();
        initializeDatabase();
        scanArtifact();
    }

    private static void checkRequirements() {
        assumeFalse(pathJQAssistant.equals(""), "Correct path to jQAssistant needed");
    }

    private static void initializeDatabase() {
        String directory = "./test/databases/ScanJarTest.db";
        BoltConnector bolt = new BoltConnector("0");
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(directory))
                .setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true")
                .setConfig(bolt.listen_address, "localhost:7689").newGraphDatabase();
        registerShutdownHook(graphDb);
        connector = DatabaseConnector.getInstance("bolt://localhost:7689");
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread(graphDb::shutdown));
    }

    private static void scanArtifact() {
        String inputFiles = "https://github.com/softvis-research/Bank/releases/download/test/bank-1.0.0-SNAPSHOT.jar";
        ScanStep scanStep = new ScanStep(inputFiles, false);
        scanStep.setPathJQAssistant(pathJQAssistant);
        scanStep.run();
    }

    @Test
    void numberOfTypes() {
        Record result = connector.executeRead("MATCH (type:Type) RETURN count(type) AS result").single();
        int numberOfTypes = result.get("result").asInt();
        assertEquals(17, numberOfTypes);
    }

    @Test
    void numberOfPackages() {
        Record result = connector.executeRead("MATCH (package:Package) RETURN count(package) AS result")
                .single();
        int numberOfPackages = result.get("result").asInt();
        assertEquals(3, numberOfPackages);
    }

    @Test
    void numberOfMethods() {
        Record result = connector.executeRead("MATCH (method:Method) RETURN count(method) AS result").single();
        int numberOfMethods = result.get("result").asInt();
        assertEquals(42, numberOfMethods);
    }

    @AfterAll
    static void teardown() {
        if(graphDb != null) {
            graphDb.shutdown();
        }
    }
}
