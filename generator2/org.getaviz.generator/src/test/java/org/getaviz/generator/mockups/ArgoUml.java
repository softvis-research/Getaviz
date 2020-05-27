package org.getaviz.generator.mockups;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class ArgoUml extends Mockup {
	
	private static Log log = LogFactory.getLog(ArgoUml.class);
	
	public void setupDatabase(String directory) {
	graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(directory))
			.setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true")
			.setConfig(bolt.listen_address, "localhost:7689").newGraphDatabase();
	registerShutdownHook(graphDb);
	connector = DatabaseConnector.getInstance("bolt://localhost:7689");
	resetDatabase();
	log.info("Cypher script import started");
	runCypherScript("ArgoUml.cypher");
	log.info("Cypher script import finished");
	}
}
