package org.getaviz.generator;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CityBankTest {

	private static DatabaseConnector connector;
	private static Bank mockup = new Bank();

	@BeforeAll
	static void setup() {
		Locale.setDefault(Locale.US);
		mockup.setupDatabase("./test/databases/CityBankTest.db");
		mockup.loadProperties("CityBankTest.properties");
		connector = mockup.getConnector();
		SettingsConfiguration config = SettingsConfiguration.getInstance();
		StepFactory factory = new StepFactory(config, Collections.singletonList(ProgrammingLanguage.JAVA));
		Step s2m = factory.createSteps2m();
		Step m2m = factory.createStepm2m();
		Step m2t = factory.createStepm2t();
		s2m.run();
		m2m.run();
		m2t.run();
	}
	
	@AfterAll
	static void close() {
		mockup.close();
	}

	@Test
	void numberOfVisualizedPackages() {
		Record result = connector
				.executeRead("MATCH (district:District)-[:VISUALIZES]->(:Package) RETURN count(district) AS result")
				.single();
		int numberOfVisualizedPackages = result.get("result").asInt();
		assertEquals(3, numberOfVisualizedPackages);
	}

	@Test
	void numberOfVisualizedTypes() {
		Record result = connector
				.executeRead("MATCH (building:Building)-[:VISUALIZES]->(:Type) RETURN count(building) AS result")
				.single();
		int numberOfVisualizedTypes = result.get("result").asInt();
		assertEquals(7, numberOfVisualizedTypes);
	}

	@Test
	void layoutAlgorithmPackage() {
		String hash = "ID_4481fcdc97864a546f67c76536e0308a3058f75d";
		Record result = connector.executeRead(
				"MATCH (:Package {hash: '" + hash + "'})<-[:VISUALIZES]-(:District)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z")
				.single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(11.0, x);
		assertEquals(2.5, y);
		assertEquals(14.0, z);
	}

	@Test
	void layoutAlgorithmClass() {
		String hash = "ID_26f25e4da4c82dc2370f3bde0201e612dd88c04c";
		Record result = connector.executeRead(
				"MATCH (:Type {hash: '" + hash + "'})<-[:VISUALIZES]-(:Building)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z")
				.single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(10.5, x);
		assertEquals(5, y);
		assertEquals(17.5, z);
	}

	@Test
	void classMembers() {
		String hash = "ID_26f25e4da4c82dc2370f3bde0201e612dd88c04c";
		Record result = connector.executeRead(
				"MATCH (building:Building)-[:VISUALIZES]->(:Type {hash: '" + hash + "'}) "
						+ "RETURN building.height as height, building.length as length, building.width as width")
				.single();
		double height = result.get("height").asDouble();
		double length = result.get("length").asDouble();
		double width = result.get("width").asDouble();
		assertEquals(4.0, height);
		assertEquals(3.0, length);
		assertEquals(3.0, width);
	}

	@Test
	void districtColor() {
		String hash = "ID_4481fcdc97864a546f67c76536e0308a3058f75d";
		Record result = connector.executeRead(
				"MATCH (:Package {hash: '" + hash + "'})<-[:VISUALIZES]-(d:District)"
						+ "RETURN d.color as color")
				.single();
		String color = result.get("color").asString();
		assertEquals("#b1b1b1", color);
	}
}