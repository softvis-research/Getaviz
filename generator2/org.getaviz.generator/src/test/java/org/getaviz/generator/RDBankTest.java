package org.getaviz.generator.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.getaviz.generator.StepFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

class RDBankTest {

	private static DatabaseConnector connector;
	private static Bank mockup = new Bank();

	@BeforeAll
	static void setup() {
		mockup.setupDatabase("./test/databases/RDBankTest.db");
		mockup.loadProperties("RDBankTest.properties");
		connector = mockup.getConnector();
		SettingsConfiguration config = SettingsConfiguration.getInstance();
		StepFactory factory = new StepFactory(config);
		Step s2m = factory.createSteps2m();
		Step m2m = factory.createStepm2m();
		s2m.run();
		m2m.run();
	}

	@AfterAll
	static void close() {
		mockup.close();
	}

	@Test
	void numberOfVisualizedPackages() {
		Record result = connector
				.executeRead("MATCH (disk:Disk)-[:VISUALIZES]->(:Package) RETURN count(disk) AS result").single();
		int numberOfVisualizedPackages = result.get("result").asInt();
		assertEquals(3, numberOfVisualizedPackages);
	}

	@Test
	void numberOfVisualizedTypes() {
		Record result = connector.executeRead("MATCH (disk:Disk)-[:VISUALIZES]->(:Type) RETURN count(disk) AS result")
				.single();
		int numberOfVisualizedTypes = result.get("result").asInt();
		assertEquals(7, numberOfVisualizedTypes);
	}

	@Test
	void numberOfVisualizedMethods() {
		Record result = connector
				.executeRead("MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Method) RETURN count(segment) AS result")
				.single();
		int numberOfVisualizedMethods = result.get("result").asInt();
		assertEquals(20, numberOfVisualizedMethods);
	}

	@Test
	void layoutAlgorithmPackage() {
		String hash = "ID_4481fcdc97864a546f67c76536e0308a3058f75d";
		Record result = connector.executeRead(
				"MATCH (:Package {hash: '" + hash + "'})<-[:VISUALIZES]-(:Disk)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z")
				.single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(-9.214771, x);
		assertEquals(-11.790536, y);
		assertEquals(0, z);
	}

	@Test
	void layoutAlgorithmClass() {
		String hash = "ID_26f25e4da4c82dc2370f3bde0201e612dd88c04c";
		Record result = connector
				.executeRead("MATCH (:Type {hash: '" + hash + "'})<-[:VISUALIZES]-(:Disk)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z")
				.single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(-9.214771, x);
		assertEquals(-1.498676, y);
		assertEquals(0, z);
	}

	@Test
	void methodSorting() {
		String hash = "ID_d2955e64b6776b754f9d69f8f480a62f849584ca";
		Record result = connector.executeRead("MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Method {hash: '" + hash
				+ "'}) RETURN segment.anglePosition as anglePosition").single();
		double anglePosition = result.get("anglePosition").asDouble();
		assertEquals(270.03296703296706, anglePosition);
	}
}
