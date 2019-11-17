package org.getaviz.generator.database;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;

public class DatabaseConnector implements AutoCloseable {
	private static String URL;
	private final Driver driver;
	private static DatabaseConnector instance = null;

	private DatabaseConnector() {
		URL = "bolt://neo4j:7687";
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
		try (Session session = driver.session(AccessMode.WRITE)) {
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
                tx.success();  // Mark this write as successful.
            }
        }
		return result;
	}
	
	public StatementResult executeRead(String statement) {
		try (Session session = driver.session(AccessMode.READ)) {
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
