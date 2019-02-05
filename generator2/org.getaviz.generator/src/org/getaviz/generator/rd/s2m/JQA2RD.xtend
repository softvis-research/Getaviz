package org.getaviz.generator.rd.s2m

import org.getaviz.generator.database.Database
import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.database.Labels
import java.util.GregorianCalendar
import org.neo4j.graphdb.Node
import org.getaviz.generator.database.Rels
import org.neo4j.graphdb.Direction
import org.getaviz.generator.SettingsConfiguration.OutputFormat
import org.apache.commons.logging.LogFactory

class JQA2RD {
	val config = SettingsConfiguration.getInstance
	var graph = Database::getInstance(config.databaseName)
	val log = LogFactory::getLog(class)
	
	new () {
		log.info("JQA2RD started")
		var tx = graph.beginTx
		try {
			val result = graph.execute("MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN n")
			val root = graph.createNode(Labels.Model, Labels.RD)
			root.setProperty("date", new GregorianCalendar().time.toString)
			val configuration = graph.createNode(Labels.RD, Labels.Configuration)
			configuration.setProperty("method_type_mode", config.methodTypeMode)
			configuration.setProperty("method_disks", config.methodDisks)
			configuration.setProperty("data_disks", config.dataDisks)
			root.createRelationshipTo(configuration, Rels.USED)
			val rootNamespaces = result.map[return get("n") as Node]
			rootNamespaces.forEach [
				root.createRelationshipTo(namespaceToDisk, Rels.CONTAINS)
			]
			tx.success
		} finally {
			tx.close
		}
		log.info("JQA2RD finished")
	}

	def private Node namespaceToDisk(Node namespace) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(namespace, Rels.VISUALIZES)
		disk.setProperty("ringWidth", config.RDRingWidth)
		disk.setProperty("height", config.RDHeight)
		disk.setProperty("transparency", config.RDNamespaceTransparency)
		val subElements = namespace.getRelationships(Rels.CONTAINS, Direction.OUTGOING).map[return endNode]
		val structures = subElements.filter[hasProperty("hash") && !hasLabel(Labels.Inner) && (hasLabel(Labels.Annotation) || hasLabel(Labels.Class) || hasLabel(Labels.Interface) || hasLabel(Labels.Enum))]
		val subPackages = subElements.filter[hasProperty("hash") && hasLabel(Labels.Package)]
		structures.forEach[disk.createRelationshipTo(structureToDisk, Rels.CONTAINS)]
		subPackages.forEach[disk.createRelationshipTo(namespaceToDisk, Rels.CONTAINS)]
		return disk
	}

	def private Node structureToDisk(Node structure) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(structure, Rels.VISUALIZES)
		disk.setProperty("ringWidth", config.RDRingWidth)
		disk.setProperty("height", config.RDHeight)
		disk.setProperty("transparency", config.RDClassTransparency)
		if (config.outputFormat == OutputFormat::AFrame) {
			disk.setProperty("color", config.RDClassColorHex)
		} else {
			disk.setProperty("color", config.RDClassColorAsPercentage)
		}
		val subElements = structure.getRelationships(Rels.DECLARES, Direction.OUTGOING).map[return endNode as Node]
		val methods = subElements.filter [hasProperty("hash") && hasLabel(Labels.Method)]
		val attributes = subElements.filter[hasProperty("hash") && !structure.hasLabel(Labels.Enum) && hasLabel(Labels.Field)]
		val enumValues = subElements.filter[hasProperty("hash") && structure.hasLabel(Labels.Enum) && hasLabel(Labels.Field)]

		if (config.methodTypeMode) {
			methods.forEach [
				if (hasLabel(Labels.Constructor)) {
					disk.createRelationshipTo(methodToDiskSegment, Rels.CONTAINS)
				} else {
					disk.createRelationshipTo(methodToDisk, Rels.CONTAINS)
				}
			]
			attributes.forEach[disk.createRelationshipTo(attributeToDisk, Rels.CONTAINS)]
			enumValues.forEach[disk.createRelationshipTo(enumValueToDisk, Rels.CONTAINS)]
		} else {
			if (config.dataDisks) {
				attributes.forEach[disk.createRelationshipTo(attributeToDisk, Rels.CONTAINS)]
				enumValues.forEach[disk.createRelationshipTo(enumValueToDisk, Rels.CONTAINS)]
			} else {
				attributes.forEach[disk.createRelationshipTo(attributeToDiskSegment, Rels.CONTAINS)]
				enumValues.forEach[disk.createRelationshipTo(enumValueToDiskSegment, Rels.CONTAINS)]
			}
			if (config.methodDisks) {
				methods.forEach[disk.createRelationshipTo(methodToDisk, Rels.CONTAINS)]
			} else {
				methods.forEach[disk.createRelationshipTo(methodToDiskSegment, Rels.CONTAINS)]
			}
		}
		val subStructures = subElements.filter[hasProperty("hash") && (hasLabel(Labels.Class) || hasLabel(Labels.Interface) || hasLabel(Labels.Enum) || hasLabel(Labels.Annotation))]
		subStructures.forEach [disk.createRelationshipTo(structureToDisk, Rels.CONTAINS)]
		return disk
	}

	def private methodToDisk(Node method) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(method, Rels.VISUALIZES)
		disk.setProperty("ringWidth", config.RDRingWidth)
		disk.setProperty("height", config.RDHeight)
		disk.setProperty("transparency", config.RDMethodTransparency)
		var color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDMethodColorHex
		}
		disk.setProperty("color", color)
		return disk
	}

	def private methodToDiskSegment(Node method) {
		val diskSegment = graph.createNode(Labels.RD, Labels.DiskSegment)
		diskSegment.createRelationshipTo(method, Rels.VISUALIZES)
		var frequency = 0.0
		var luminance = 0.0
		var height = config.RDHeight
		if (config.outputFormat == OutputFormat::AFrame) {
			diskSegment.setProperty("color", config.RDMethodColorHex)
		} else {
			diskSegment.setProperty("color", config.RDMethodColorAsPercentage)
		}
		diskSegment.setProperty("transparency", config.RDMethodTransparency)
		diskSegment.setProperty("frequency", frequency)
		diskSegment.setProperty("luminance", luminance)
		diskSegment.setProperty("height", height)
		var numberOfStatements = 0
		if (method.hasProperty("effectiveLineCount")) {
			numberOfStatements = method.getProperty("effectiveLineCount") as Integer
		}
		if (numberOfStatements <= config.RDMinArea) {
			diskSegment.setProperty("size", config.RDMinArea)
		} else {
			diskSegment.setProperty("size", numberOfStatements.doubleValue)
		}
		return diskSegment
	}

	def private attributeToDisk(Node attribute) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(attribute, Rels.VISUALIZES)
		disk.setProperty("ringWidth", config.RDRingWidthAD)
		disk.setProperty("height", config.RDHeight)
		disk.setProperty("transparency", config.RDDataTransparency)
		var color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		disk.setProperty("color", color)
		// TODO: getter und setter als disk segment
		return disk
	}

	def private attributeToDiskSegment(Node attribute) {
		val diskSegment = graph.createNode(Labels.RD, Labels.DiskSegment)
		diskSegment.createRelationshipTo(attribute, Rels.VISUALIZES)
		diskSegment.setProperty("size", 1.0)
		diskSegment.setProperty("height", config.RDHeight)
		var color = config.RDDataColorAsPercentage
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		diskSegment.setProperty("color", color)
		diskSegment.setProperty("transparency", config.RDDataTransparency)
		return diskSegment
	}

	def private enumValueToDisk(Node enumValue) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(enumValue, Rels.VISUALIZES)
		disk.setProperty("ringWidth", config.RDRingWidthAD)
		disk.setProperty("height", config.RDHeight)
		disk.setProperty("transparency", config.RDDataTransparency)
		var color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		disk.setProperty("color", color)
		return disk
	}

	def private enumValueToDiskSegment(Node enumValue) {
		val diskSegment = graph.createNode(Labels.RD, Labels.DiskSegment)
		diskSegment.createRelationshipTo(enumValue, Rels.VISUALIZES)
		diskSegment.setProperty("size", 1.0)
		diskSegment.setProperty("height", config.RDHeight)
		var color = config.RDDataColorAsPercentage
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		diskSegment.setProperty("color", color)
		diskSegment.setProperty("transparency", config.RDDataTransparency)
		return diskSegment
	}
	
}
