package org.svis.generator.famix

import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator

class FamixEvaluator implements Evaluator {

	override evaluate(Path path) {
		val node = path.endNode
		if (node.hasLabel(Labels.Package) || node.hasLabel(Labels.Member)) {
			return Evaluation.INCLUDE_AND_CONTINUE
		}
		if (node.hasLabel(Labels.Type)) {
			if (node.hasLabel(Labels.Class) || node.hasLabel(Labels.Interface) || node.hasLabel(Labels.Enum) ||
				node.hasLabel(Labels.Annotation)) {
				val pre = path.nodes.get(path.length - 1)
				val isInnerClass = path.endNode.getProperty("name").toString.contains("$")
				if ((pre.hasLabel(Labels.Package) && isInnerClass) || pre.hasLabel(Labels.Method)) {
					return Evaluation.EXCLUDE_AND_PRUNE
				} else {
					return Evaluation.INCLUDE_AND_CONTINUE
				}
			}
		}
		return Evaluation.EXCLUDE_AND_PRUNE
	}
}
