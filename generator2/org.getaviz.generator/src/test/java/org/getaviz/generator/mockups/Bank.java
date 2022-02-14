//package org.getaviz.generator.mockups;
//
//import java.io.File;
//import org.getaviz.generator.loader.database.DatabaseConnector;
//import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//
//public class Bank extends Mockup {
//
//	public void setupDatabase(String directory) {
//		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(directory))
//				.setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true")
//				.setConfig(bolt.listen_address, "localhost:7689").newGraphDatabase();
//		registerShutdownHook(graphDb);
//		connector = DatabaseConnector.getInstance("bolt://localhost:7689");
//		resetDatabase();
//		runCypherScript("Bank.cypher");
//	}
//}
