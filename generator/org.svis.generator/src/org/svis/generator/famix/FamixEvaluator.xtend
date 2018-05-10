package org.svis.generator.famix

import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator
import org.neo4j.graphdb.Label

class FamixEvaluator implements Evaluator {

	override evaluate(Path path) {
		var result = Evaluation.EXCLUDE_AND_PRUNE
		for (label : path.endNode.labels) {
			if (label.name == "Package" || label.name == "Method") {
				result = Evaluation.INCLUDE_AND_CONTINUE
			} 
			if (label.name == "Class") {
				val pre = path.nodes.get(path.length - 1)
				val isInnerClass = path.endNode.getProperty("name").toString.contains("$")
				if ((pre.hasLabel(Label.label("Package")) && isInnerClass) || pre.hasLabel(Label.label("Method")) ) {
						result = Evaluation.EXCLUDE_AND_PRUNE
					} 
					else {
						result = Evaluation.INCLUDE_AND_CONTINUE
				}
			}
			if(label.name == "Field") {
				result = Evaluation.INCLUDE_AND_CONTINUE
				//To-Do: Attribute entfernen, die nur lokal in Funktionen deklariert werden
			}
		
		}
		return result
	}
}
