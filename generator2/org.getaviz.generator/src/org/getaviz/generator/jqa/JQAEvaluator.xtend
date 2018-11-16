package org.getaviz.generator.jqa

import org.neo4j.graphdb.traversal.Evaluator
import org.neo4j.graphdb.Path
import org.getaviz.lib.database.Labels
import org.neo4j.graphdb.traversal.Evaluation
//import org.getaviz.lib.database.Database
//import org.getaviz.generator.SettingsConfiguration
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction

class JQAEvaluator implements Evaluator {
	// val config = SettingsConfiguration.instance
	// val graph = Database::getInstance(config.databaseName)
	override evaluate(Path path) {
		val node = path.endNode

		if (node.hasLabel(Labels.Package)) {
			return Evaluation.INCLUDE_AND_CONTINUE
		}

		if (node.hasLabel(Labels.Anonymous)) {
			return Evaluation.EXCLUDE_AND_PRUNE
		}

		if (node.hasLabel(Labels.Type) &&
			((node.hasLabel(Labels.Class) || node.hasLabel(Labels.Interface) || node.hasLabel(Labels.Enum) ||
				node.hasLabel(Labels.Annotation)))) {
			val pre = path.nodes.get(path.length - 1)
			if ((pre.hasLabel(Labels.Package) && node.hasLabel(Labels.Inner)) || pre.hasLabel(Labels.Method)) {
				return Evaluation.EXCLUDE_AND_PRUNE
			} else {
				return Evaluation.INCLUDE_AND_CONTINUE
			}
		}

		var name = node.getProperty("name", "") as String
		if (node.hasLabel(Labels.Field) && !name.contains("$")) {
			return Evaluation.INCLUDE_AND_CONTINUE
		}

		if (node.hasLabel(Labels.Method) && !name.contains("$")) {
			if (node.hasLabel(Labels.Constructor)) {
				return Evaluation.INCLUDE_AND_CONTINUE
			} else {
				var found = false
				var signature = node.getProperty("signature") as String
				val c = node.getSingleRelationship(Rels.DECLARES, Direction.INCOMING).startNode
				val ps = c.getRelationships(Rels.EXTENDS, Direction.OUTGOING)
				for (p : ps) {
					if (p.endNode.hasLabel(Labels.Type)) {
						val m2s = p.endNode.getRelationships(Rels.DECLARES, Direction.OUTGOING)
						for (m2 : m2s) {
							if (m2.endNode.hasLabel(Labels.Method)) {
								if (signature == m2.endNode.getProperty("signature") as String) {
									found = true
								}

							}
						}
					}
				}
				if (found) {
					return Evaluation.EXCLUDE_AND_CONTINUE
				} else {
					return Evaluation.INCLUDE_AND_CONTINUE
				}

//				val result = graph.execute(
//					"MATCH (m1:Method)<-[:DECLARES]-(c:Type)-[:EXTENDS]->(p:Type)-[:DECLARES]->(m2) WHERE ID(m1) = " +
//						node.id + " AND m1.signature = m2.signature RETURN m1")
//				if (result === null) {
//					return Evaluation.INCLUDE_AND_CONTINUE
//				} else {
//					return Evaluation.EXCLUDE_AND_CONTINUE
//				}
			}
		}
		return Evaluation.EXCLUDE_AND_PRUNE
	}
}
