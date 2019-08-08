package org.getaviz.generator.jqa;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.jqa.ScanStep;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class ScanStepTest {

    private static DatabaseConnector connector;
    private static String pathJqassistant = "";

    @BeforeAll
    static void setup() {
        assumeFalse(pathJqassistant.equals(""), "Correct path to jQAssistant needed");
        String directory = "./test/databases/ScanStepTest.db";
        BoltConnector bolt = new BoltConnector("0");
        loadProperties("ScanStepTest.properties");
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(directory))
                .setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true")
                .setConfig(bolt.listen_address, "localhost:7687").newGraphDatabase();
        registerShutdownHook(graphDb);
        connector = DatabaseConnector.getInstance("bolt://localhost:7687");
        SettingsConfiguration config = SettingsConfiguration.getInstance();
        ScanStep scanStep = new ScanStep(config);
        scanStep.setPathJqassisent(pathJqassistant);
        scanStep.run();
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            graphDb.shutdown();
        }));
    }

    private static void loadProperties(String resourcePath) {
        ClassLoader classLoader = ScanStepTest.class.getClassLoader();
        String path = classLoader.getResource(resourcePath).getPath();
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SettingsConfiguration.getInstance(path);
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
}
