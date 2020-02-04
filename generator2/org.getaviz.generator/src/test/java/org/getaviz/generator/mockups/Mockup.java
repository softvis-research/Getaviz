package org.getaviz.generator.mockups;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.configuration.BoltConnector;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Mockup {
	
	protected DatabaseConnector connector;
	protected GraphDatabaseService graphDb;
	protected BoltConnector bolt = new BoltConnector("0");
	
	protected static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
	
	public void loadProperties(String resourcePath) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		String path = classLoader.getResource(resourcePath).getPath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		SettingsConfiguration.getInstance(path);
	}
	
	protected void resetDatabase () {
		connector.executeWrite("MATCH (n) DETACH DELETE n; ");
	}
	
	protected void runCypherScript (String resource) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		String filePath = classLoader.getResource(resource).getFile();
		File file = new File(filePath);
		String path = file.getAbsolutePath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			connector.executeWrite(new String(encoded));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		graphDb.shutdown();
	}
	
	public DatabaseConnector getConnector() {
		return connector;
	}
}
