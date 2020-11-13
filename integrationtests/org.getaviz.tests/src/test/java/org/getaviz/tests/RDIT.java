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
 
public class RDIT {

	private static DatabaseTestConnector connector;
	
	@BeforeAll
	static void run() throws Exception {
		connector = new DatabaseTestConnector();
		URL url = new URL("http://backend:8080");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		Map<String, String> parameters = new HashMap<>();
		parameters.put("input.files", "https://repo1.maven.org/maven2/com/android/tools/build/gradle/0.1/gradle-0.1.jar");
		parameters.put("metaphor", "rd");
		 
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
			"MATCH (disk:MainDisk)-[:VISUALIZES]->(:Package) " + 
			"RETURN count(disk) AS result"
		).single();
		int numberOfVisualizedPackages = result.get("result").asInt();
	    assertEquals(5, numberOfVisualizedPackages);
    }
	
	@Test
    void numberOfVisualizedTypes() {
		Record result = connector.executeRead(
			"MATCH (disk:SubDisk)-[:VISUALIZES]->(:Type) " + 
			"RETURN count(disk) AS result"
		).single();
		int numberOfVisualizedTypes = result.get("result").asInt();
	    assertEquals(87, numberOfVisualizedTypes);
    }
	
	@Test
    void numberOfVisualizedMethods() {
		Record result = connector.executeRead(
			"MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Method) " + 
			"RETURN count(segment) AS result"
		).single();
		int numberOfVisualizedMethods = result.get("result").asInt();
	    assertEquals(655, numberOfVisualizedMethods);
    }
	
	@Test
    void numberOfVisualizedAttributes() {
		Record result = connector.executeRead(
			"MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Field) " + 
			"RETURN count(segment) AS result"
		).single();
		int numberOfVisualizedAttributes = result.get("result").asInt();
	    assertEquals(236, numberOfVisualizedAttributes);
    }
	 
	@Test
	void layoutAlgorithmPackage() {
		Record result = connector.executeRead(
			"MATCH (:Package {hash: 'ID_52a6b74381719655ffacf4d2c8c38a22d5581078'})<-[:VISUALIZES]-(:MainDisk)-[:HAS]->(position:Position) " + 
			"RETURN position.x as x, position.y as y, position.z as z").single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
	    assertEquals(-2.576052, x);
	    assertEquals(-12.33451, y);
	    assertEquals(0, z);
	}
	
	@Test
	void layoutAlgorithmClass() {
		Record result = connector.executeRead(
	 		"MATCH (:Type {hash: 'ID_0d6283470c75fbfeae1b0199ae41f732db8dc820'})<-[:VISUALIZES]-(:SubDisk)-[:HAS]->(position:Position) " + 
	 		"RETURN position.x as x, position.y as y, position.z as z"
	 	).single();
	    double x = result.get("x").asDouble();
	    double y = result.get("y").asDouble();
	    double z = result.get("z").asDouble();
	    assertEquals(93.130997, x);
	 	assertEquals(-56.163684, y);
	 	assertEquals(0, z);
	}
	
	@Test
	void methodSorting() {
		Record result = connector.executeRead(
			"MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Method {hash: 'ID_1d573ecfa6e966a99a7bc5358cc5fbde40e1d472'}) " +
			"RETURN segment.anglePosition as anglePosition"
		).single();
		double anglePosition = result.get("anglePosition").asDouble();
		assertEquals(108.94736842105263, anglePosition);
	}
	
	@Test
	void dataSorting() {
		Record result = connector.executeRead(
			"MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Field {hash: 'ID_cc37950e08d5de980eb55cf907c9c0644a3dccb9'}) " +
			"RETURN segment.angle as angle, segment.anglePosition as anglePosition"
		).single();
		double anglePosition = result.get("anglePosition").asDouble();
		assertEquals(141.42857142857142, anglePosition);
	}
}
