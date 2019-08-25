package org.getaviz.tests.helper;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

public class DatabaseTestConnector implements AutoCloseable {
	private final Driver driver;

	public DatabaseTestConnector() {
		driver = GraphDatabase.driver("bolt://neo4j:7687");
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
