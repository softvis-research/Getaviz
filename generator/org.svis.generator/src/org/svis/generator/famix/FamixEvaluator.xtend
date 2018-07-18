package org.svis.generator.famix

import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator

class FamixEvaluator implements Evaluator {

	override evaluate(Path path) {
		val node = path.endNode
		if (node.hasLabel(DBLabel.PACKAGE) || node.hasLabel(DBLabel.MEMBER)) {
			return Evaluation.INCLUDE_AND_CONTINUE
		}
		if (node.hasLabel(DBLabel.TYPE)) {
			if (node.hasLabel(DBLabel.CLASS) || node.hasLabel(DBLabel.INTERFACE) || node.hasLabel(DBLabel.ENUM) ||
				node.hasLabel(DBLabel.ANNOTATION)) {
				val pre = path.nodes.get(path.length - 1)
				val isInnerClass = path.endNode.getProperty("name").toString.contains("$")
				if ((pre.hasLabel(DBLabel.PACKAGE) && isInnerClass) || pre.hasLabel(DBLabel.METHOD)) {
					return Evaluation.EXCLUDE_AND_PRUNE
				} else {
					return Evaluation.INCLUDE_AND_CONTINUE
				}
			}
		}
		return Evaluation.EXCLUDE_AND_PRUNE
	}
}
