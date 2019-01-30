package org.getaviz.generator.rd

import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Direction
import org.getaviz.generator.database.Rels
import org.getaviz.generator.database.Labels
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Path

class RDUtils {
	
	def static getMethods(Node disk) {
		val methods = disk.getRelationships(Direction.OUTGOING, Rels.CONTAINS).filter [
			endNode.hasLabel(Labels.DiskSegment)
		].map[return endNode].filter [
			getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode.hasLabel(Labels.Method)
		]
		return methods
	}

	def static getSubDisks(Node disk) {
		val subDisks = disk.getRelationships(Direction.OUTGOING, Rels.CONTAINS).filter [
			endNode.hasLabel(Labels.Disk)
		].map[return endNode]
		return subDisks
	}

	def static getData(Node disk) {
		val data = disk.getRelationships(Direction.OUTGOING, Rels.CONTAINS).filter [
			endNode.hasLabel(Labels.DiskSegment)
		].map[return endNode].filter [
			getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode.hasLabel(Labels.Field)
		]
		return data
	}

	def static sum(Iterable<Node> segments) {
		var sum = 0.0
		for (segment : segments) {
			sum += segment.getProperty("size") as Double
		}
		return sum
	}

	def static getLevel(GraphDatabaseService graph, Node disk) {
		val result = graph.execute(
			"MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) WHERE ID(m) = " + disk.id + " RETURN p LIMIT 1")
		val path = result.head.get("p") as Path
		return path.length
	}
}
