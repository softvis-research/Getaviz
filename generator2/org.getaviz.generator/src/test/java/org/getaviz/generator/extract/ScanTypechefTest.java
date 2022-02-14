//package org.getaviz.generator.extract;
//
//import org.getaviz.generator.garbage.ProgrammingLanguage;
//import org.getaviz.generator.loader.database.DatabaseConnector;
//import org.getaviz.generator.loader.extract.ScanStep;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.neo4j.driver.v1.Record;
//import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import org.neo4j.kernel.configuration.BoltConnector;
//
//import java.io.File;
//import java.util.List;
//
//import static org.junit.Assert.assertTrue;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assumptions.assumeFalse;
//
//class ScanTypechefTest {
//
//    private static DatabaseConnector connector;
//    private static GraphDatabaseService graphDb;
//    private static String pathJQAssistant = ""; // replace with local path to jQAssistant 1.7
//
//    @BeforeAll
//    static void setup() {
//        checkRequirements();
//        initializeDatabase();
//        scanArtifact();
//    }
//
//    private static void checkRequirements() {
//        assumeFalse(pathJQAssistant.equals(""), "Correct path to jQAssistant needed");
//    }
//
//    private static void initializeDatabase() {
//        String directory = "./test/databases/ScanTypechefTest.db";
//        BoltConnector bolt = new BoltConnector("0");
//        graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(directory))
//                .setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true")
//                .setConfig(bolt.listen_address, "localhost:7689").newGraphDatabase();
//        registerShutdownHook(graphDb);
//        connector = DatabaseConnector.getInstance("bolt://localhost:7689");
//    }
//
//    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
//        // Registers a shutdown hook for the Neo4j instance so that it
//        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
//        // running application).
//        Runtime.getRuntime().addShutdownHook(new Thread(graphDb::shutdown));
//    }
//
//    private static void scanArtifact() {
//        String inputFile = "https://home.uni-leipzig.de/svis/share/beispiel_header.ast";
//        ScanStep scanStep = new ScanStep(inputFile, false);
//        scanStep.setPathJQAssistant(pathJQAssistant);
//        scanStep.run();
//    }
//
//    @Test
//    void numberOfTypes() {
//        Record result = connector.executeRead("MATCH (type:Type) RETURN count(type) AS result").single();
//        int numberOfTypes = result.get("result").asInt();
//        assertEquals(7, numberOfTypes);
//    }
//
//    @Test
//    void numberOfTranslationUnits() {
//        Record result = connector.executeRead("MATCH (units:TranslationUnit) RETURN count(units) AS result")
//                .single();
//        int numberOfTranslationUnits = result.get("result").asInt();
//        assertEquals(1, numberOfTranslationUnits);
//    }
//
//    @Test
//    void numberOfFunctions() {
//        Record result = connector.executeRead("MATCH (function:Function) RETURN count(function) AS result").single();
//        int numberOfFunctions = result.get("result").asInt();
//        assertEquals(3, numberOfFunctions);
//    }
//
//    @Test
//    void detectC() {
//        Importer importer = new Importer("");
//        List<ProgrammingLanguage> languages = importer.getImportedProgrammingLanguages();
//        assertTrue(languages.contains(ProgrammingLanguage.C));
//    }
//
//    @AfterAll
//    static void teardown() {
//        if(graphDb != null) {
//            graphDb.shutdown();
//        }
//    }
//}
