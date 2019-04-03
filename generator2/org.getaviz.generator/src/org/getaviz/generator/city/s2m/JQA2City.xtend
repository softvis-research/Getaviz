package org.getaviz.generator.city.s2m

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.database.Labels
import java.util.GregorianCalendar
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.database.DatabaseConnector

class JQA2City {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	val connector = DatabaseConnector::instance

	new() {
		log.info("JQA2City started")
		connector.executeWrite("MATCH (n:City) DETACH DELETE n")
		val model = connector.addNode(
			String.format("CREATE (n:Model:City {date: \'%s\', building_type: \'%s\'})",
				new GregorianCalendar().time.toString, config.buildingTypeAsString),"n").id
		connector.executeRead("MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN n").forEach [
			val namespace = get("n").asNode.id
			namespaceToDistrict(namespace, model)
		]
		log.info("JQA2City finished")
	}

	def private Long namespaceToDistrict(Long namespace, Long parent) {
		val district = connector.addNode(cypherCreateNode(parent,namespace,Labels.District.name),"n").id
		val subPackages = connector.executeRead("MATCH (n)-[:CONTAINS]->(p:Package) WHERE ID(n) = " + namespace +
			" RETURN p")
		val subTypes = connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + namespace +
			" AND EXISTS(t.hash) AND (t:Class OR t:Interface) AND NOT t:Inner RETURN t")
		subPackages.forEach[namespaceToDistrict(get("p").asNode.id, district)]
		subTypes.forEach[structureToBuilding(get("t").asNode.id, district)]
		return district;
	}

	def private Long structureToBuilding(Long structure, Long parent) {
		val building = connector.addNode(cypherCreateNode(parent, structure, Labels.Building.name),"n").id
		val methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = " + structure +
			" AND EXISTS(m.hash) RETURN m")
		val attributes = connector.executeRead("MATCH (n)-[:DECLARES]->(a:Field) WHERE ID(n) = " + structure +
			" AND EXISTS(a.hash) RETURN a")
		if (config.buildingType == BuildingType::CITY_FLOOR && methods !== null) {
			methods.forEach[methodToFloor(get("m").asNode.id, building)]
			attributes.forEach[attributeToChimney(get("a").asNode.id, building)]
		} else {
			if (config.originalBuildingMetric == BuildingMetric::NOS) {
				val numberOfStatements = methods.fold(0) [ sum, record |
					val method = record.get("m").asNode
					var effectiveLineCount = 0
					if (method.containsKey("effectiveLineCount")) {
						effectiveLineCount = method.get("effectiveLineCount").asInt
					}
					sum + effectiveLineCount
				]
				connector.executeWrite("MATCH(n) WHERE ID(n) = " + building + " SET n.numberOfStatements = " +
					numberOfStatements)
			}

			if ((config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
				config.classElementsMode === ClassElementsModes::METHODS_ONLY)) {
				methods.forEach [
					methodToBuildingSegment(get("m").asNode.id, building)
				]
			}

			if (config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
				config.classElementsMode === ClassElementsModes::ATTRIBUTES_ONLY) {
				attributes.forEach[attributeToBuildingSegment(get("a").asNode.id, building)]
			}
		}
		val subStructures = connector.executeRead("MATCH (n)-[:DECLARES]->(t:Type:Inner) WHERE ID(n) = " + structure +
			" AND EXISTS(t.hash) RETURN t")
		subStructures.forEach[structureToBuilding(get("t").asNode.id, parent)]
		return building
	}

	def private methodToBuildingSegment(Long method, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, method, Labels.BuildingSegment.name))
	}

	def private methodToFloor(Long method, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, method, Labels.BuildingSegment.name + ":" + Labels.Floor.name))
	}

	def private attributeToBuildingSegment(Long attribute, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.BuildingSegment.name))
	}

	def private attributeToChimney(Long attribute, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.BuildingSegment.name + ":" + Labels.Chimney.name))
	}

	def private cypherCreateNode(Long parent, Long visualizedNode, String label) {
		return String.format(
			"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->(n:City:%s)-[:VISUALIZES]->(s)",
			parent, visualizedNode, label)
	}
}
