package org.getaviz.generator.rd;


import java.util.Iterator;
import java.util.stream.Collectors;

import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
//import org.apache.commons.logging.LogFactory

public class RDUtils {
	static DatabaseConnector connector = DatabaseConnector.getInstance();
	//static val log = LogFactory::getLog(RDUtils)
	
	
	public static Iterator<Node> getMethods(Long disk) {
		return connector.executeRead(
			"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(method:Method) " + 
			"WHERE ID(n) = " + disk + " " + 
			"RETURN d " +
			"ORDER BY method.hash"
		).stream().map(s -> s.get("d").asNode()).collect(Collectors.toList()).listIterator();
	}

	public static StatementResult getSubDisks(Long disk) {
		return connector.executeRead(
			"MATCH (n)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) " + 
			"WHERE ID(n) = " + disk + " " +
			"RETURN d " + 
			"ORDER BY element.hash"
		);
	}

	public static Iterator<Node> getData(Long disk) {
		return connector.executeRead(
			"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(field:Field) " +
			"WHERE ID(n) = " + disk + " " + 
			"RETURN d " +
			"ORDER BY field.hash"
		).stream().map(s -> s.get("d").asNode()).collect(Collectors.toList()).listIterator();

	}

	public static double sum(Iterable<Node> segments) {
		double sum = 0.0; 
		for(Node segment: segments) {
			sum += segment.get("size").asDouble();			
		}
		return sum;
	}
}

