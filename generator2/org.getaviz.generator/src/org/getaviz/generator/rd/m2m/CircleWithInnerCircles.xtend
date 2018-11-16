package org.getaviz.generator.rd.m2m;

import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors
import org.neo4j.graphdb.Node
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.getaviz.lib.database.Labels
import org.getaviz.generator.rd.RDUtils
import org.getaviz.lib.database.Database
import org.apache.commons.logging.LogFactory

class CircleWithInnerCircles extends Circle {
	@Accessors int level
	@Accessors val innerCircles = new ArrayList<CircleWithInnerCircles>
	val log = LogFactory::getLog(class)
	var Node diskNode
	val graph = Database::instance

	new(Node disk, Boolean nesting) {
		diskNode = disk
		val data = RDUtils.getData(disk)
		val methods = RDUtils.getMethods(disk)
		if (nesting == true) {
			minArea = RDUtils.sum(methods) + RDUtils.sum(data)
		} else {
			minArea = disk.getProperty("netArea") as Double
			level = RDUtils.getLevel(graph, disk)
		}
		ringWidth = disk.getProperty("ringWidth") as Double
		serial = disk.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode.id.toString
		netArea = disk.getProperty("netArea") as Double
		log.debug("set netArea to " + netArea + "for disk " + diskNode.id)
		radius = disk.getProperty("radius", 0.0) as Double

		if (disk.hasProperty("grossArea")) {
			grossArea = disk.getProperty("grossArea") as Double
		} else {
			grossArea = 0.0
		}
		val subDisks = RDUtils.getSubDisks(disk)
		subDisks.forEach [
			innerCircles.add(new CircleWithInnerCircles(it, true))
		]
	}

	/**
	 * write calculated positions into extended disk
	 * 
	 */
	def void updateDiskNode() {
		diskNode.setProperty("radius", radius)
		diskNode.setProperty("netArea", netArea)
		diskNode.setProperty("grossArea", grossArea)
		log.debug("set netArea to " + netArea + "for disk " + diskNode.id)
		val position = diskNode.getSingleRelationship(Rels.HAS, Direction.OUTGOING)
		val newPosition = graph.createNode(Labels.Position, Labels.RD)
		var oldZPosition = 0.0
		if (position !== null) {
			oldZPosition = position.endNode.getProperty("z") as Double
		} else {
			diskNode.createRelationshipTo(newPosition, Rels.HAS)
		}
		newPosition.setProperty("x", centre.x)
		newPosition.setProperty("y", centre.y)
		newPosition.setProperty("z", oldZPosition)

		innerCircles.forEach[updateDiskNode]
	}
}
