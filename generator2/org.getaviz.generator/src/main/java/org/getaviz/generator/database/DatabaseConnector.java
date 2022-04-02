package org.getaviz.generator.database;

import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;

public class DatabaseConnector implements AutoCloseable {
	private static String URL = "bolt://neo4j:7687";
	private final Driver driver;
	private static DatabaseConnector instance = null;

	private DatabaseConnector() {
		driver = GraphDatabase.driver(URL);
	}
	
	private DatabaseConnector(String URL) {
		DatabaseConnector.URL = URL;
		driver = GraphDatabase.driver(URL);
	}
	
	public static String getDatabaseURL() {
		return URL;
	}
	
	public static DatabaseConnector getInstance() {
		if (instance == null) {
			instance = new DatabaseConnector();
		}
		return instance;
	}
	
	public static DatabaseConnector getInstance(String URL) {
		if (instance == null) {
			instance = new DatabaseConnector(URL);
		}
		return instance;
	}

	public void executeWrite(String... statements) {
		try (Session session = driver.session()) {
			session.writeTransaction((Transaction tx) -> {
				for (String statement : statements) {
					tx.run(statement);
				}
				return 1;
			});
		}
	}
	
	public Node addNode(String statement, String parameterName) {
		Node result;
		try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
				result = tx.run(statement + " RETURN " + parameterName).next().get(parameterName).asNode();
            }
        }
		return result;
	}
	
	public Result executeRead(String statement) {
		try (Session session = driver.session()) {
			return session.run(statement);
		}
	}
	
	public Node getVisualizedEntity(Long id) {
		return executeRead("MATCH (n)-[:VISUALIZES]->(e) WHERE ID(n) = " + id + " RETURN e").single().get("e").asNode();
	}
	
	public Node getPosition(Long id) {
		return executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + id + " RETURN p").single().get("p").asNode();
	}

	@Override
	public void close() {
		driver.close();
	}
}
