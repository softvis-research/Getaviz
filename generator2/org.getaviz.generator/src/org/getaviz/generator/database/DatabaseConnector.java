package org.getaviz.generator.database;

import org.getaviz.generator.SettingsConfiguration;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.types.Node;

public class DatabaseConnector implements AutoCloseable {
	private final Driver driver;
	private static DatabaseConnector instance = null;
	private SettingsConfiguration config = SettingsConfiguration.getInstance();

	private DatabaseConnector() {
		driver = GraphDatabase.driver(config.getDatabase());
	}

	public static DatabaseConnector getInstance() {
		if (instance == null) {
			instance = new DatabaseConnector();
		}
		return instance;
	}

	public void executeWrite(String... statements) {
		try (Session session = driver.session(AccessMode.WRITE)) {
			session.writeTransaction(new TransactionWork<Integer>() {
				@Override 
				public Integer execute(Transaction tx) {
					for(String statement : statements) {
						tx.run(statement);
					}
					return 1;
				}
			});		
		}
	}
	
	public Node addNode(String statement, String parameterName) {
		Node result = null;
		try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                result = tx.run(statement + " RETURN " + parameterName).next().get(parameterName).asNode();
                tx.success();  // Mark this write as successful.
            }
        }
		return result;
	}
	
	public void addRelationship(Long from, Long to, String relationship) {
		try (Session session = driver.session(AccessMode.WRITE)) {
			session.writeTransaction(new TransactionWork<Integer>() {
				@Override 
				public Integer execute(Transaction tx) {
						tx.run(String.format("MATCH (from),(to) WHERE ID(from) = %d AND ID(to) = %d CREATE (from)-[r:%s]->(to)", from, to, relationship));
					return 1;
				}
			});		
		}
	}
	
	public StatementResult executeRead(String statement) {
		try (Session session = driver.session(AccessMode.READ)) {
			StatementResult result = session.run(statement);
			return result;
		}
	}
	
	public Node getVisualizedEntity(Long id) {
		return executeRead("MATCH (n)-[:VISUALIZES]->(e) WHERE ID(n) = " + id + " RETURN e").single().get("e").asNode();
	}
	
	public Node getPosition(Long id) {
		return executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + id + " RETURN p").single().get("p").asNode();
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}
}
