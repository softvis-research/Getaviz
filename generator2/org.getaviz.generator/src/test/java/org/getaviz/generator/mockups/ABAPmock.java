package org.getaviz.generator.mockups;

import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

public class ABAPmock extends Mockup {

	public void setupDatabase(String directory, String cypherScript) {

		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(directory))
				.setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true")
				.setConfig(bolt.listen_address, "localhost:11003").newGraphDatabase();
		registerShutdownHook(graphDb);
		connector = DatabaseConnector.getInstance("bolt://localhost:11003");
		resetDatabase();
		runCypherScript(cypherScript);


		/*
		try {
			//connector = DatabaseConnector.getInstance("bolt://localhost:7687");
			connector = DatabaseConnector.getInstance("bolt://localhost:11003"); //Toni
		} catch(Exception exception){
			exception.printStackTrace();
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(directory))
					.setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true")
					.setConfig(bolt.listen_address, "localhost:1103").newGraphDatabase();
			registerShutdownHook(graphDb);
			connector = DatabaseConnector.getInstance("bolt://localhost:1103");
		}

		resetDatabase();
		runCypherScript(cypherScript);
	}

	@Override
	public void close() {
		if(graphDb != null){
			super.close();
		}
	}

		 */
	}
}
