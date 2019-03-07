package org.getaviz.generator.jqa

import org.getaviz.generator.SettingsConfiguration
import org.neo4j.graphdb.Node
import org.getaviz.generator.database.Labels
import org.apache.commons.codec.digest.DigestUtils
import org.getaviz.generator.database.Rels
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.traversal.Uniqueness
import org.getaviz.generator.database.Database
import org.apache.commons.logging.LogFactory
import java.io.IOException
import org.neo4j.graphdb.GraphDatabaseService
import org.getaviz.generator.database.DatabaseConnector

class DatabaseBuilder {
	val log = LogFactory::getLog(class)
	val config = SettingsConfiguration.instance
	val connector = DatabaseConnector::instance
	val evaluator = new JQAEvaluator
	val runtime = Runtime.getRuntime();

	new() {
		scan();
		enhance();
	}

	def scan() {
		log.info("jQA scan started.")
		log.info("Scanning from URI(s) " + config.inputFiles);
		log.info("Scanning to database " + config.databaseName);
		try {
			val pScan = runtime.exec(
				"/opt/jqassistant/bin/jqassistant.sh scan -reset -u " + config.inputFiles + " -storeUri " +
					config.database)
			pScan.waitFor()
			val pRightsDatabase = runtime.exec("chmod -v 777 -R " + config.databaseName)
			pRightsDatabase.waitFor()
			val pRightsStoreLock = runtime.exec("chmod -v 777 -R " + config.databaseName + "/../store_lock")
			pRightsStoreLock.waitFor()
		} catch (InterruptedException e) {
			log.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		}
		log.info("jQA scan ended.")
	}

	def enhance() {
		log.info("jQA enhancement started.")
		connector.executeWrite(labelGetter, labelSetter, labelPrimitives, labelInnerTypes)
		connector.executeWrite(labelAnonymousInnerTypes)
		addHashes()
		log.info("jQA enhancement finished")
	}

	private def addHashes() {
		connector.executeWrite(labelInheritedMethods)
		connector.executeRead(collectRelevantNodes).forEach [
			val node = get("m").asNode
			var fqn = node.get("fqn").asString
			if (fqn.nullOrEmpty) {
				val container = connector.executeRead("MATCH (n)<-[:DECLARES]-(m) WHERE ID(n) = " + node.id +
					" RETURN m").next.get("m").asNode
				val containerFqn = container.get("fqn").asString
				var name = node.get("name").asString
				var signature = node.get("signature").asString
				val index = signature.indexOf(" ") + 1
				if (node.hasLabel("Method")) {
					val indexOfBracket = signature.indexOf("(")
					if (name.empty) {
						name = signature.substring(index, indexOfBracket)
					}
					fqn = containerFqn + "." + signature.substring(index)
				} else {
					if (name.empty) {
						name = signature.substring(index)
					}
					fqn = containerFqn + "." + name
				}
				connector.executeWrite("MATCH (n) WHERE ID(n) = " + node.id + " SET n.name = \'" + name + "\', n.fqn = \'" + fqn + "\'")
			} 
			if (node.get("hash").isNull) {
				connector.executeWrite("MATCH (n) WHERE ID(n) = " + node.id + " SET n.hash = \'" + createHash(fqn) + "\'")
			}
		]
//		connector.executeWrite(unlabelInheritedMethods)
	}

	private def createHash(String fqn) {
		return "ID_" + DigestUtils.sha1Hex(fqn + config.repositoryName + config.repositoryOwner)
	}

	private def labelPrimitives() {
		return "MATCH (n:Type) WHERE n.name =~ \"[a-z]+\" SET n:Primitive"
	}

	private def labelGetter() {
		return "MATCH (o:Type)-[:DECLARES]->(method:Method)-[getter:READS]->(attribute:Field)<-[:DECLARES]-(q:Type) 
				WHERE method.name =~ \"get[A-Z]+[A-Za-z]*\" 
				AND toLower(method.name) contains(attribute.name) AND ID(o) = ID(q) 
				SET method:Getter"
	}

	private def labelSetter() {
		return "MATCH (o:Type)-[:DECLARES]->(method:Method)-[setter:WRITES]->(attribute:Field)<-[:DECLARES]-(q:Type) 
				WHERE method.name =~ \"set[A-Z]+[A-Za-z]*\" 
				AND toLower(method.name) contains(attribute.name) AND ID(o) = ID(q) 
				SET method:Setter"
	}

	private def labelInnerTypes() {
		return "MATCH (:Type)-[:DECLARES]->(innerType:Type) SET innerType:Inner"
	}

	private def labelAnonymousInnerTypes() {
		return "MATCH (innerType:Inner:Type) WHERE innerType.name =~ \".*\\\\$[0-9]*\" SET innerType:Anonymous"
	}

	private def labelInheritedMethods() {
		return "MATCH p=(m:Method)<-[:DECLARES]-()-[:EXTENDS]->(:Type)-[:DECLARES]->(o:Method)
				WHERE m.signature = o.signature AND NOT m:Constructor
				SET m:Inherited"
	}

	private def unlabelInheritedMethods() {
		return "MATCH (m:Method:Inherited) REMOVE m:Inherited"
	}

	private def collectRelevantNodes() {
		return "MATCH (n:Package)-[:DECLARES|CONTAINS*]->(m) WHERE (
					m:Package OR 
    				m:Type 
        				AND (m:Interface OR m:Class OR m:Enum OR m:Annotation) 
        				AND NOT m:Anonymous AND NOT (m)<--(:Method) AND NOT (m:Inner)<--(:Package) OR
    				m:Field OR
    				m:Constructor OR
    				m:Method AND NOT m:Method:Inherited
				) AND NOT ((n)<-[:CONTAINS]-(:Package) OR m.name CONTAINS '$')
				RETURN DISTINCT m"
	}
}