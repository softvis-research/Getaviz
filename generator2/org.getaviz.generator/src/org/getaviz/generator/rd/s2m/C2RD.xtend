package org.getaviz.generator.rd.s2m

import java.util.GregorianCalendar
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.SettingsConfiguration.OutputFormat
import org.getaviz.lib.database.Database
import org.getaviz.lib.database.Labels
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node

class C2RD {
	val config = SettingsConfiguration.getInstance
	var graph = Database::getInstance(config.databaseName)
	val log = LogFactory::getLog(class)
	
	new () {
		log.info("C2RD started")
		var tx = graph.beginTx
		try {
			var result = graph.execute("MATCH (n:TranslationUnit) RETURN n")
			val root = graph.createNode(Labels.Model, Labels.RD)
			root.setProperty("date", new GregorianCalendar().time.toString)
			val configuration = graph.createNode(Labels.RD, Labels.Configuration)
			configuration.setProperty("method_type_mode", config.methodTypeMode)
			configuration.setProperty("method_disks", config.methodDisks)
			configuration.setProperty("data_disks", config.dataDisks)
			root.createRelationshipTo(configuration, Rels.USED)
			val translationUnits = result.map[return get("n") as Node]
			translationUnits.forEach [
				root.createRelationshipTo(translationUnitToDisk, Rels.CONTAINS)
			]
			tx.success
		} finally {
			tx.close
		}
		log.info("C2RD finished")
	}
	
	def private Node translationUnitToDisk(Node translationUnit) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(translationUnit, Rels.VISUALIZES)
		disk.setProperty("ringWidth", config.RDRingWidth)
		disk.setProperty("height", config.RDHeight)
		disk.setProperty("transparency", config.RDNamespaceTransparency)
		
		if (config.outputFormat == OutputFormat::AFrame) {
			disk.setProperty("color", config.RDClassColorHex)
		} else {
			disk.setProperty("color", config.RDClassColorAsPercentage)
		}
		val subElements = translationUnit.getRelationships(Rels.DECLARES, Direction.OUTGOING).map[return endNode]
		val functions = subElements.filter[hasLabel(Labels.Function)]
		val variables = subElements.filter[hasLabel(Labels.Variable)]

		if (config.dataDisks) {
			variables.forEach[disk.createRelationshipTo(variableToDisk, Rels.CONTAINS)]
		} else {
			variables.forEach[disk.createRelationshipTo(variableToDiskSegment, Rels.CONTAINS)]
		}
		if (config.methodDisks) {
			functions.forEach[disk.createRelationshipTo(functionToDisk, Rels.CONTAINS)]
		} else {
			functions.forEach[disk.createRelationshipTo(functionToDiskSegment, Rels.CONTAINS)]
		}

		return disk
	}

	def private functionToDisk(Node function) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(function, Rels.VISUALIZES)
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

	def private functionToDiskSegment(Node function) {
		val diskSegment = graph.createNode(Labels.RD, Labels.DiskSegment)
		diskSegment.createRelationshipTo(function, Rels.VISUALIZES)
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
		if (function.hasProperty("effectiveLineCount")) {
			numberOfStatements = function.getProperty("effectiveLineCount") as Integer
		}
		if (numberOfStatements <= config.RDMinArea) {
			diskSegment.setProperty("size", config.RDMinArea)
		} else {
			diskSegment.setProperty("size", numberOfStatements.doubleValue)
		}
		return diskSegment
	}

	def private variableToDisk(Node variable) {
		val disk = graph.createNode(Labels.RD, Labels.Disk)
		disk.createRelationshipTo(variable, Rels.VISUALIZES)
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

	def private variableToDiskSegment(Node variable) {
		val diskSegment = graph.createNode(Labels.RD, Labels.DiskSegment)
		diskSegment.createRelationshipTo(variable, Rels.VISUALIZES)
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

//	def private enumValueToDisk(Node enumValue) {
//		val disk = graph.createNode(Labels.RD, Labels.Disk)
//		disk.createRelationshipTo(enumValue, Rels.VISUALIZES)
//		disk.setProperty("ringWidth", config.RDRingWidthAD)
//		disk.setProperty("height", config.RDHeight)
//		disk.setProperty("transparency", config.RDDataTransparency)
//		var color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0
//		if (config.outputFormat == OutputFormat::AFrame) {
//			color = config.RDDataColorHex
//		}
//		disk.setProperty("color", color)
//		return disk
//	}
	
}
