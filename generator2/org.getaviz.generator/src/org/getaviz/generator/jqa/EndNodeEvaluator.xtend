package org.getaviz.generator.jqa

import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator
import org.getaviz.lib.database.Labels

class EndNodeEvaluator implements Evaluator {
	var Labels label

	new (Labels label) {
		this.label = label
	}
	
	override evaluate(Path path) {
		if (path.endNode.hasLabel(label) && path.endNode.hasProperty("hash")) {
			return Evaluation.INCLUDE_AND_CONTINUE
		}
		else {
			return Evaluation.EXCLUDE_AND_CONTINUE
		}
	}
}
