package org.getaviz.generator.jqa

import org.getaviz.generator.SettingsConfiguration
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.logging.LogFactory
import java.io.IOException
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.Record

class DatabaseBuilder {
	val log = LogFactory::getLog(class)
	val config = SettingsConfiguration.instance
	val connector = DatabaseConnector::instance
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
		connector.executeRead(collectAllPackages).forEach[enhanceNode]
		connector.executeRead(collectAllTypes).forEach[enhanceNode]
		connector.executeRead(collectAllFields).forEach[enhanceNode]
		connector.executeRead(collectAllMethods).forEach[enhanceNode]
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

	private def collectAllPackages() {
		return "MATCH (n:Package) RETURN n"
	}

	private def collectAllTypes() {
		return "MATCH (n:Type)
				WHERE (n:Interface OR n:Class OR n:Enum OR n:Annotation) 
				AND NOT n:Anonymous AND NOT (n)<-[:CONTAINS]-(:Method)
				RETURN n"
	}

	private def collectAllFields() {
		return "MATCH (n:Field)<-[:DECLARES]-(f:Type)
				WHERE (NOT n.name CONTAINS '$') AND (NOT f:Anonymous) RETURN DISTINCT n"
	}

	private def collectAllMethods() {
		return "MATCH (n:Method)<-[:DECLARES]-(f:Type)
				WHERE (NOT n.name CONTAINS '$') AND (NOT f:Anonymous) RETURN DISTINCT n"
	}

	private def enhanceNode(Record record) {
		val node = record.get("n").asNode
		var fqn = node.get("fqn").asString
		if (fqn.nullOrEmpty) {
			val container = connector.executeRead("MATCH (n)<-[:DECLARES]-(container) WHERE ID(n) = " + node.id +
				" RETURN container").single.get("container").asNode
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
			connector.executeWrite(
				"MATCH (n) WHERE ID(n) = " + node.id + " SET n.name = \'" + name + "\', n.fqn = \'" + fqn + "\'")
		}
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + node.id + " SET n.hash = \'" + createHash(fqn) + "\'")
	}

}
