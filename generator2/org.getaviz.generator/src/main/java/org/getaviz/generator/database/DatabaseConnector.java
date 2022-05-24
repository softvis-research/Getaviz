package org.getaviz.generator.database;

import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.neo4j.driver.AccessMode.*;

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

	public List<Record> executeRead(String statement) {
		try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build())) {
			return session.run(statement).list();
		}
	}
	
	public Node getVisualizedEntity(Long id) {
		return executeRead("MATCH (n)-[:VISUALIZES]->(e) WHERE ID(n) = " + id + " RETURN e").get(0).get("e").asNode();
	}
	
	public Node getPosition(Long id) {
		return executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + id + " RETURN p").get(0).get("p").asNode();
	}

	@Override
	public void close() {
		driver.close();
	}
}
