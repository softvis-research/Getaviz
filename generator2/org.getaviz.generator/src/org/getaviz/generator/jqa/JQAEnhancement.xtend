package org.getaviz.generator.jqa

import org.getaviz.generator.SettingsConfiguration
import org.neo4j.graphdb.Node
import org.getaviz.lib.database.Labels
import org.apache.commons.codec.digest.DigestUtils
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.traversal.Uniqueness
import org.getaviz.lib.database.Database
import org.apache.commons.logging.LogFactory

class JQAEnhancement {
	val log = LogFactory::getLog(class)
	val config = SettingsConfiguration.instance
	val graph = Database::getInstance(config.databaseName)
	val evaluator = new JQAEvaluator

	new() {
		log.info("JQAEnhancement has started.")
		var tx = graph.beginTx
		try {
			labelGetter()
			labelSetter()
			labelPrimitives()
			labelInnerTypes()
			tx.success
		} finally {
			tx.close
		}

		tx = graph.beginTx
		try {
			labelAnonymousInnerTypes()
			tx.success
		} finally {
			tx.close
		}

		tx = graph.beginTx
		try {
			addHashes()
			tx.success
		} finally {
			tx.close
		}
		log.info("JQAEnhancement finished")
	}

	private def addHashes() {
		val roots = graph.execute("MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN n").map [
			return get("n") as Node
		]
		
		roots.forEach [ package |
			graph.traversalDescription.depthFirst.relationships(Rels.CONTAINS, Direction.OUTGOING).relationships(
				Rels.DECLARES, Direction.OUTGOING).uniqueness(Uniqueness.NONE).evaluator(evaluator).traverse(package).
				nodes.forEach [
				//	log.debug(id)
					var fqn = getProperty("fqn", "") as String
					if (fqn.empty) {
						val container = getSingleRelationship(Rels.DECLARES, Direction.INCOMING).startNode
						val containerFqn = container.getProperty("fqn") as String
						var name = getProperty("name", "") as String
						var signature = getProperty("signature") as String
						val index = signature.indexOf(" ") + 1
						if (hasLabel(Labels.Method)) {
							val indexOfBracket = signature.indexOf("(")
							if (name.empty) {
								name = signature.substring(index, indexOfBracket)
								setProperty("name", name)
							}
							fqn = containerFqn + "." + signature.substring(index)
						} else {
							if (name.empty) {
								name = signature.substring(index)
								setProperty("name", name)
							}
							fqn = containerFqn + "." + name
						}
						setProperty("fqn", fqn)
					}
					// fqn = fqn.replace("$", ".")
					var hash = getProperty("hash", "") as String
					if (hash.empty) {
						setProperty("hash", createHash(fqn))
					}
				]
		]
	}

	private def createHash(String fqn) {
		return "ID_" + DigestUtils.sha1Hex(fqn + config.repositoryName + config.repositoryOwner)
	}

	private def labelPrimitives() {
		graph.execute("MATCH (p:Type) WHERE p.name =~ \"[a-z]+\" RETURN p").forEach [
			val primitive = get("p") as Node
			primitive.addLabel(Labels.Primitive)
		]
	}

	private def labelGetter() {
		graph.execute("MATCH (o:Type)-[:DECLARES]->(method:Method)-[getter:READS]->(attribute:Field)<-[:DECLARES]-(q:Type) 
					   WHERE method.name =~ \"get[A-Z]+[A-Za-z]*\" 
					   AND toLower(method.name) contains(attribute.name) AND ID(o) = ID(q) 
					   RETURN method").forEach [
			val getter = get("method") as Node
			getter.addLabel(Labels.Getter)
		]
	}

	private def labelSetter() {
		graph.execute("MATCH (o:Type)-[:DECLARES]->(method:Method)-[setter:WRITES]->(attribute:Field)<-[:DECLARES]-(q:Type) 
					   WHERE method.name =~ \"set[A-Z]+[A-Za-z]*\" 
					   AND toLower(method.name) contains(attribute.name) AND ID(o) = ID(q) 
					   RETURN method").forEach [
			val setter = get("method") as Node
			setter.addLabel(Labels.Getter)
		]
	}

	private def labelInnerTypes() {
		graph.execute("MATCH (:Type)-[:DECLARES]->(innerType:Type) SET innerType:Inner")
	}

	private def labelAnonymousInnerTypes() {
		graph.execute("MATCH (innerType:Inner:Type) WHERE innerType.name =~ \".*\\\\$[0-9]*\" SET innerType:Anonymous")
	}
}
