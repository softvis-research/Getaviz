package org.getaviz.generator.database;

import org.getaviz.generator.SettingsConfiguration;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

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
	
	public StatementResult executeRead(String statement) {
		try (Session session = driver.session(AccessMode.READ)) {
			StatementResult result = session.run(statement);
			return result;
		}
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}
}
