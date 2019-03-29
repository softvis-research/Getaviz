package org.getaviz.generator.rd.m2m;

import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors
import org.getaviz.generator.rd.RDUtils
import org.getaviz.generator.database.Database
import org.apache.commons.logging.LogFactory
import org.neo4j.driver.v1.types.Node
import org.getaviz.generator.database.DatabaseConnector

class CircleWithInnerCircles extends Circle {
	@Accessors int level
	@Accessors val innerCircles = new ArrayList<CircleWithInnerCircles>
	val log = LogFactory::getLog(class)
	var Node diskNode
	val graph = Database::instance
	val connector = DatabaseConnector::instance

	new(Node disk, Boolean nesting) {
		diskNode = disk
		val data = RDUtils.getData(disk.id)
		val methods = RDUtils.getMethods(disk.id)
		if (nesting == true) {
			minArea = RDUtils.sum(methods) + RDUtils.sum(data)
		} else {
			minArea = disk.get("netArea").asDouble
			level = RDUtils.getLevel(graph, disk.id)
		}
		ringWidth = disk.get("ringWidth").asDouble
		serial = connector.getVisualizedEntity(disk.id).id.toString
		netArea = disk.get("netArea").asDouble
		log.debug("set netArea to " + netArea + "for disk " + diskNode.id)
		radius = disk.get("radius").asDouble(0.0)
		grossArea = disk.get("grossArea").asDouble(0.0)
		RDUtils.getSubDisks(disk.id).forEach [
			innerCircles.add(new CircleWithInnerCircles(get("d").asNode, true))
		]
	}

	/**
	 * write calculated positions into extended disk
	 * 
	 */
	def void updateDiskNode() {
		val updateNode = String.format(
			"MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.netArea = %f, n.grossArea = %f ", diskNode.id, radius,
			netArea, grossArea)

		log.debug("set netArea to " + netArea + "for disk " + diskNode.id)
		val position = connector.executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + diskNode.id +
			" RETURN p")
		var oldZPosition = 0.0
		if (!position.nullOrEmpty) {
			val node = position.single().get("p").asNode();
			oldZPosition = node.get("z").asDouble
		}
		val createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", centre.x,
			centre.y, oldZPosition)
		connector.executeWrite(updateNode + createPosition)
		innerCircles.forEach[updateDiskNode]
	}
}
