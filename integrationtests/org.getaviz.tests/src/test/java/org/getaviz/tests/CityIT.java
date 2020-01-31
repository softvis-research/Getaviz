package org.getaviz.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.getaviz.tests.helper.DatabaseTestConnector;
import org.getaviz.tests.helper.ParameterStringBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

 
public class CityIT {
	
	private static DatabaseTestConnector connector;
	
	@BeforeAll
	static void run() throws Exception {
		connector = new DatabaseTestConnector();
		URL url = new URL("http://backend:8080");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		Map<String, String> parameters = new HashMap<>();
		parameters.put("input.files", "https://repo1.maven.org/maven2/com/android/tools/build/gradle/0.1/gradle-0.1.jar");
		parameters.put("metaphor", "city");
		 
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
		out.flush();
		out.close();
		con.getResponseCode();
	}
	
	@AfterAll
	static void close() throws Exception {
		connector.close();
	}

	@Test
    void numberOfVisualizedPackages() {
		Record result = connector.executeRead(
			"MATCH (district:District)-[:VISUALIZES]->(:Package) " + 
			"RETURN count(district) AS result"
		).single();
		int numberOfVisualizedPackages = result.get("result").asInt();
	    assertEquals(5 , numberOfVisualizedPackages);
    }

	@Test
    void numberOfVisualizedTypes() {
		Record result = connector.executeRead(
			"MATCH (building:Building)-[:VISUALIZES]->(:Type) " + 
			"RETURN count(building) AS result"
		).single();
		int numberOfVisualizedTypes = result.get("result").asInt();
	    assertEquals(87, numberOfVisualizedTypes);
    }

	@Test
	void layoutAlgorithmPackage() {
		Record result = connector.executeRead(
			"MATCH (:Package {hash: 'ID_736ff4e8c509af97b93144e21132e8b6f9810a26'})<-[:VISUALIZES]-(:District)-[:HAS]->(position:Position) " + 
			"RETURN position.x as x, position.y as y, position.z as z"
		).single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
	    assertEquals(88.0, x);
	    assertEquals(4.5, y);
	    assertEquals(88.0, z);
	}

	@Test
	void layoutAlgorithmClass() {
		Record result = connector.executeRead(
	 		"MATCH (:Type {hash: 'ID_a81ef0e8a047d15d20f4cc7bc0a8fef658100aa2'})<-[:VISUALIZES]-(:Building)-[:HAS]->(position:Position) " + 
	 		"RETURN position.x as x, position.y as y, position.z as z"
		).single();
	    double x = result.get("x").asDouble();
	    double y = result.get("y").asDouble();
	    double z = result.get("z").asDouble();
	    assertEquals(90.5, x);
	 	assertEquals(12.0, y);
	 	assertEquals(146.5, z);
	}

	@Test
	void classMembers() {
		Record result = connector.executeRead(
		 	"MATCH (building:Building)-[:VISUALIZES]->(:Type {hash: 'ID_14c970c755ed96a4d3ab92e63887ac7ed2e90bc2'}) " + 
		 	"RETURN building.height as height, building.length as length, building.width as width"
		 ).single();
		double height = result.get("height").asDouble();
		double length = result.get("length").asDouble();
		double width = result.get("width").asDouble();
		assertEquals(39.0, height);
		assertEquals(10.0, length);
		assertEquals(10.0, width);
	}
}
