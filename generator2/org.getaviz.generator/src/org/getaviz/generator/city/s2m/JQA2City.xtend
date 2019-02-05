package org.getaviz.generator.city.s2m

import org.neo4j.graphdb.GraphDatabaseService
import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.database.Database
import org.getaviz.generator.database.Labels
import java.util.GregorianCalendar
import org.neo4j.graphdb.Node
import org.getaviz.generator.database.Rels
import org.neo4j.graphdb.Direction
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes
//import org.neo4j.graphdb.traversal.Evaluators
//import org.getaviz.generator.jqa.EndNodeEvaluator
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric
import org.apache.commons.logging.LogFactory

class JQA2City {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var GraphDatabaseService graph

	new () {
		log.info("JQA2City started")
		graph = Database::getInstance(config.databaseName)
		var tx = graph.beginTx
		try {
			val result = graph.execute("MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package)RETURN n")
			val root = graph.createNode(Labels.Model, Labels.City)
			root.setProperty("date", new GregorianCalendar().time.toString)
			root.setProperty("building_type", config.buildingTypeAsString)
			val rootNamespaces = result.map[return get("n") as Node]
			rootNamespaces.forEach [
				root.createRelationshipTo(namespaceToDistrict, Rels.CONTAINS)
			]
			tx.success
		} finally {
			tx.close
		}
		log.info("JQA2City finished")
	}

	def private Node namespaceToDistrict(Node namespace) {
		val district = graph.createNode(Labels.City, Labels.District)
		district.createRelationshipTo(namespace, Rels.VISUALIZES)
		val subElements = namespace.getRelationships(Rels.CONTAINS, Direction.OUTGOING).map[return endNode]
		val structures = subElements.filter[(hasLabel(Labels.Class) || hasLabel(Labels.Interface)) && !hasLabel(Labels.Inner) && hasProperty("hash")]
		val subPackages = subElements.filter[hasLabel(Labels.Package)]
		structures.forEach[district.createRelationshipTo(structureToBuilding(district), Rels.CONTAINS)]
		subPackages.forEach[district.createRelationshipTo(namespaceToDistrict, Rels.CONTAINS)]
		return district
	}

//	def private Node structureToDistrict(Node structure) {
//		val district = graph.createNode(Labels.City, Labels.District)
//		district.createRelationshipTo(structure, Rels.VISUALIZES)
//		val subElements = structure.getRelationships(Rels.DECLARES, Direction.OUTGOING).map[return endNode as Node]
//		val methods = subElements.filter [hasLabel(Labels.Method) && hasProperty("hash")]
//		if (config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
//			config.classElementsMode === ClassElementsModes::METHODS_ONLY) {
//			methods.forEach[district.createRelationshipTo(methodToBuilding, Rels.CONTAINS)]
//		}
//		return district
//	}
	
	def private Node structureToBuilding(Node structure, Node district) {
		val building = graph.createNode(Labels.City, Labels.Building)
		building.createRelationshipTo(structure, Rels.VISUALIZES)
		
		val subElements = structure.getRelationships(Rels.DECLARES, Direction.OUTGOING).map[return endNode as Node]
//		val methods = getEndNodes(Rels.DECLARES, Labels.Method, structure)
		val methods = subElements.filter[hasProperty("hash") && hasLabel(Labels.Method)]
		val attributes = subElements.filter[hasProperty("hash") && hasLabel(Labels.Field)]
		if (config.buildingType == BuildingType::CITY_FLOOR && methods !== null) {
			methods.forEach[building.createRelationshipTo(methodToFloor, Rels.CONTAINS)]
			attributes.forEach[building.createRelationshipTo(attributeToChimney, Rels.CONTAINS)]
		} else {
			if (config.originalBuildingMetric == BuildingMetric::NOS) {
				val numberOfStatements = methods.fold(0)[sum, method | 
					var effectiveLineCount = 0
					if(method.hasProperty("effectiveLineCount")) {
						effectiveLineCount = method.getProperty("effectiveLineCount") as Integer
					}
					sum + effectiveLineCount
				]
				building.setProperty("numberOfStatements", numberOfStatements)
			}

			if ((config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
				config.classElementsMode === ClassElementsModes::METHODS_ONLY)) {
				methods.forEach[building.createRelationshipTo(methodToBuildingSegment, Rels.CONTAINS)]
			}

			if (config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
				config.classElementsMode === ClassElementsModes::ATTRIBUTES_ONLY) {
				attributes.forEach[building.createRelationshipTo(attributeToBuildingSegment, Rels.CONTAINS)]
			}
		}
		val subStructures = subElements.filter[hasLabel(Labels.Inner) && hasProperty("hash")]
		subStructures.forEach[district.createRelationshipTo(structureToBuilding(district), Rels.CONTAINS)]
		return building
	}	
	
//	def private Node methodToBuilding(Node method) {
//		val building = graph.createNode(Labels.City, Labels.Building)
//		building.createRelationshipTo(method, Rels.VISUALIZES)
//		return building
//	}
	
	def private Node methodToBuildingSegment(Node method) {
		val segment = graph.createNode(Labels.City, Labels.BuildingSegment)
		segment.createRelationshipTo(method, Rels.VISUALIZES)
		return segment		
	}
	
	def private Node methodToFloor(Node method) {
		val floor = graph.createNode(Labels.City, Labels.BuildingSegment, Labels.Floor)
		floor.createRelationshipTo(method, Rels.VISUALIZES)
		return floor
	}

	def private Node attributeToBuildingSegment(Node attribute) {
		val segment = graph.createNode(Labels.City, Labels.BuildingSegment)
		segment.createRelationshipTo(attribute, Rels.VISUALIZES)
		return segment				
	}

	def private Node attributeToChimney(Node attribute) {
		val chimney = graph.createNode(Labels.City, Labels.BuildingSegment, Labels.Chimney)
		chimney.createRelationshipTo(attribute, Rels.VISUALIZES)
		return chimney
	}
			
	/*def private getEndNodes(Rels relationship, Labels label, Node startNode) {
		val endNodes = graph.traversalDescription().relationships(relationship, Direction.OUTGOING)
		.evaluator(Evaluators.toDepth(1)).evaluator(Evaluators.fromDepth(1))
		.evaluator(new EndNodeEvaluator(label))
		.traverse(startNode).nodes
		return endNodes
	}	*/
}
