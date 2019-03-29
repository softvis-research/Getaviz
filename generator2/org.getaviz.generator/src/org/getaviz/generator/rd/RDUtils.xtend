package org.getaviz.generator.rd

import org.neo4j.graphdb.GraphDatabaseService
import org.getaviz.generator.database.DatabaseConnector
import java.util.Iterator
import org.neo4j.driver.v1.types.Node

class RDUtils {
	static val connector = DatabaseConnector::instance
	
	def static getMethods(Long disk) {
		return connector.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Method) WHERE ID(n) = " + disk + " RETURN d").map[get("d").asNode]
	}

	def static getSubDisks(Long disk) {
		return connector.executeRead("MATCH (n)-[:CONTAINS]->(d:Disk) WHERE ID(n) = " + disk + " RETURN d")
	}

	def static getData(Long disk) {
		return connector.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Field) WHERE ID(n) = " + disk + " RETURN d").map[get("d").asNode]
	}

	def static sum(Iterator<Node> segments) {
		var sum = 0.0
		while(segments.hasNext) {
			sum += segments.next.get("size").asDouble
		}
		return sum
	}

	def static getLevel(GraphDatabaseService graph, Long disk) {
		return connector.executeRead("MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) WHERE ID(m) = " + disk + " RETURN p LIMIT 1").single.get("p").asPath.length
	}
}

