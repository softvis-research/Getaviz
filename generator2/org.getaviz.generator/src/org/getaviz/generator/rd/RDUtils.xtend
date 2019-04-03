package org.getaviz.generator.rd

//import org.neo4j.graphdb.GraphDatabaseService
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node
//import org.apache.commons.logging.LogFactory

class RDUtils {
	static val connector = DatabaseConnector::instance
	//static val log = LogFactory::getLog(RDUtils)
	
	
	def static getMethods(Long disk) {
		return connector.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Method) WHERE ID(n) = " + disk + " RETURN d").map[get("d").asNode]
	}

	def static getSubDisks(Long disk) {
		return connector.executeRead("MATCH (n)-[:CONTAINS]->(d:Disk) WHERE ID(n) = " + disk + " RETURN d")
	}

	def static getData(Long disk) {
		return connector.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Field) WHERE ID(n) = " + disk + " RETURN d").map[get("d").asNode]
	}

	def static sum(Iterable<Node> segments) {
		var sum = 0.0 
		for(segment: segments) {
			sum += segment.get("size").asDouble			
		}
		return sum
	}

	def static getLevel(Long disk) {
		return connector.executeRead("MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) WHERE ID(m) = " + disk + " RETURN p LIMIT 1").single.get("p").asPath.length
	}
}

