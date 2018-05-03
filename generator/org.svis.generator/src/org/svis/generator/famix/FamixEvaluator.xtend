package org.svis.generator.famix

import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator

class FamixEvaluator implements Evaluator {

	override evaluate(Path path) {
		var result = Evaluation.EXCLUDE_AND_PRUNE
		for(label : path.endNode.labels) {
			if(label.name == "Package" ||
				label.name == "Class" ||
				label.name == "Method") {
				result = Evaluation.INCLUDE_AND_CONTINUE
			} 
		}
		return result
	}	
}