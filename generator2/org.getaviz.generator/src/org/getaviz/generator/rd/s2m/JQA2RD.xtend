package org.getaviz.generator.rd.s2m

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.database.Labels
import java.util.GregorianCalendar
import org.getaviz.generator.SettingsConfiguration.OutputFormat
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node

class JQA2RD {
	val config = SettingsConfiguration.getInstance
	val connector = DatabaseConnector.instance
	val log = LogFactory::getLog(class)

	new() {
		log.info("JQA2RD started")
		connector.executeWrite("MATCH (n:RD) DETACH DELETE n")
		val model = connector.addNode(
			String.format(
				"CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', method_disks: \'%s\', data_disks:\'%s\'})",
				new GregorianCalendar().time.toString, config.methodTypeMode, config.methodDisks, config.dataDisks),
			"m").id
		connector.executeRead("MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN n").
		forEach [namespaceToDisk(get("n").asNode.id, model)]
		log.info("JQA2RD finished")
	}

	def private void namespaceToDisk(Long namespace, Long parent) {
		val properties = String.format("ringWidth: %f, height: %f, transparency: %f", config.RDRingWidth,
			config.RDHeight, config.RDNamespaceTransparency)
		val disk = connector.addNode(cypherCreateNode(parent, namespace, Labels.Disk.name, properties), "n").id
		connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + namespace +
			" AND EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) AND NOT t:Inner RETURN t").
			forEach[structureToDisk(get("t").asNode, disk)]
		connector.executeRead("MATCH (n)-[:CONTAINS]->(p:Package) WHERE ID(n) = " + namespace +
			" AND EXISTS(p.hash) RETURN p").
			forEach[namespaceToDisk(get("p").asNode.id, disk)]
	}

	def private void structureToDisk(Node structure, Long parent) {
		var color = config.RDClassColorAsPercentage
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDClassColorHex
		}
		val properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", config.RDRingWidth,
			config.RDHeight, config.RDClassTransparency, color)
		val disk = connector.addNode(cypherCreateNode(parent, structure.id, Labels.Disk.name, properties), "n").id
		val methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = " + structure.id +
			" AND EXISTS(m.hash) RETURN m")
		val fields = connector.executeRead("MATCH (n)-[:DECLARES]->(f:Field) WHERE ID(n) = " + structure.id +
			" AND EXISTS(f.hash) RETURN f")

		if (config.methodTypeMode) {
			methods.forEach [
				val method = get("m").asNode
				if (method.hasLabel(Labels.Constructor.name)) {
					methodToDiskSegment(method, disk)
				} else {
					methodToDisk(method.id, disk)
				}
			]
			fields.forEach [
				if (structure.hasLabel(Labels.Enum.name)) {
					enumValueToDisk(get("f").asNode.id, disk)
				} else {
					attributeToDisk(get("f").asNode.id, disk)
				}
			]
		} else {
			if (config.dataDisks) {
				fields.forEach [
					if (structure.hasLabel(Labels.Enum.name)) {
						enumValueToDisk(get("f").asNode.id, disk)
					} else {
						attributeToDisk(get("f").asNode.id, disk)
					}
				]
			} else {
				fields.forEach [
					if (structure.hasLabel(Labels.Enum.name)) {
						enumValueToDiskSegment(get("f").asNode.id, disk)
					} else {
						attributeToDiskSegment(get("f").asNode.id, disk)
					}
				]
			}
			if (config.methodDisks) {
				methods.forEach[methodToDisk(get("m").asNode.id, disk)]
			} else {
				methods.forEach[methodToDiskSegment(get("m").asNode, disk)]
			}
		}
		connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + structure.id +
			" AND EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) RETURN t").
			forEach[structureToDisk(get("t").asNode, disk)]
	}

	def private void methodToDisk(Long method, Long parent) {
		var color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDMethodColorHex
		}
		val properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", config.RDRingWidth,
			config.RDHeight, config.RDMethodTransparency, color)
		connector.executeWrite(cypherCreateNode(parent, method, Labels.Disk.name, properties))
	}

	def private void methodToDiskSegment(Node method, Long parent) {
		var frequency = 0.0
		var luminance = 0.0
		var height = config.RDHeight
		var color = config.RDMethodColorAsPercentage
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDMethodColorHex
		}
		var numberOfStatements = method.get("effectiveLineCount").asInt(0)
		var size = numberOfStatements.doubleValue
		if (numberOfStatements <= config.RDMinArea) {
			size = config.RDMinArea
		}
		val properties = String.format(
			"frequency: %f, luminance: %f, height: %f, transparency: %f, size: %f, color: \'%s\'", frequency, luminance,
			height, config.RDMethodTransparency, size, color)
		connector.executeWrite(cypherCreateNode(parent, method.id, Labels.DiskSegment.name, properties))
	}

	def private void attributeToDisk(Long attribute, Long parent) {
		var color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		val properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'",
			config.RDRingWidthAD, config.RDHeight, config.RDDataTransparency, color)
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.Disk.name, properties))
	}

	def private void attributeToDiskSegment(Long attribute, Long parent) {
		var color = config.RDDataColorAsPercentage
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		val properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", 1.0, config.RDHeight,
			config.RDDataTransparency, color)
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.DiskSegment.name, properties))
	}

	def private void enumValueToDisk(Long enumValue, Long parent) {
		var color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		val properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'",
			config.RDRingWidthAD, config.RDHeight, config.RDDataTransparency, color)
		connector.executeWrite(cypherCreateNode(parent, enumValue, Labels.Disk.name, properties))
	}

	def private void enumValueToDiskSegment(Long enumValue, Long parent) {
		var color = config.RDDataColorAsPercentage
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDDataColorHex
		}
		val properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", 1.0, config.RDHeight,
			config.RDDataTransparency, color)
		connector.executeWrite(cypherCreateNode(parent, enumValue, Labels.DiskSegment.name, properties))

	}

	def private cypherCreateNode(Long parent, Long visualizedNode, String label, String properties) {
		return String.format(
			"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->(n:RD:%s {%s})-[:VISUALIZES]->(s)",
			parent, visualizedNode, label, properties)
	}
}
