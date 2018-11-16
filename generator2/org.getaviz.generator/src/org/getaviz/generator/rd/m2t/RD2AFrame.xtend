package org.getaviz.generator.rd.m2t

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.lib.database.Database
import org.neo4j.graphdb.Node
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.getaviz.lib.database.Labels
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.OutputFormatHelper
import java.io.FileWriter
import java.io.IOException

class RD2AFrame {
	val config = SettingsConfiguration.instance
	var graph = Database::getInstance(config.databaseName)
	val log = LogFactory::getLog(class)
	extension OutputFormatHelper helper = new OutputFormatHelper

	new() {
		log.info("RD2AFrame has started")
		var FileWriter fw = null
		var fileName = "model.html"
		try {
			fw = new FileWriter(config.outputPath + fileName)
			fw.write(AFrameHead() + toX3DOMRD() + AFrameTail())
		} catch (IOException e) {
			log.error("Could not create file");
		} finally {
			if (fw !== null)
				try {
					fw.close;
				} catch (IOException e) {
					e.printStackTrace;
				}
		}
		log.info("RD2AFrame has finished")
	}

	def private toX3DOMRD() {
		val elements = new StringBuilder
		val tx = graph.beginTx
		try {
			var result = graph.execute("MATCH (n:Model)-[:CONTAINS*]->(m:Disk) RETURN m").map[return get("m") as Node]
			result.forEach[elements.append(toDisk)]
			tx.success
		} finally {
			tx.close
		}
		return elements.toString
	}

	def private toDisk(Node disk) {
		val radius = disk.getProperty("radius") as Double
		val hash = disk.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode.getProperty("hash")
		val position = disk.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val segments = disk.getRelationships(Rels.CONTAINS, Direction.OUTGOING).map[return endNode].filter [
			hasLabel(Labels.DiskSegment)
		]
		val result = '''
			«IF radius - config.RDRingWidth == 0»
				<a-circle id="«hash»" 
					position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
					radius="«radius»" 
					color="«disk.getProperty("color") »"
					shader="flat"
					buffer="true"
					flat-shading="true"
					depth-test="false"
					depth-write="false">
«««					segments="20">
					«segments.toSegment»
				</a-circle>
			«ELSE»
				<a-ring id="«hash»"
					position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
					radius-inner="«radius - config.RDRingWidth»"
					radius-outer="«radius»" 
					color="«disk.getProperty("color") »"
					shader="flat"
					buffer="true"
					flat-shading="true"
					depth-test="false"
					depth-write="false"
					segments-phi="1">
			«««				segments-theta="20"
«««					segments-phi="1">
				«segments.toSegment»
				</a-ring>
			«ENDIF»
		'''
		return result
	}

	def private toSegment(Iterable<Node> segments) {
		val result = '''
		«FOR segment : segments»
			«val hash = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode.getProperty("hash")»
			«IF segment.getProperty("innerRadius") as Double == 0»
				<a-circle id="«hash»"
					radius="«segment.getProperty("outerRadius")»" 
					color="«segment.getProperty("color") »"
					theta-start="«segment.getProperty("anglePosition")»"
					theta-length="«segment.getProperty("angle")»"
					shader="flat"
					buffer="true"
					flat-shading="true"
					depth-test="false"
					depth-write="false">
«««					segments="«(segment.getProperty("angle") as Double/20).intValue+1»">
				</a-circle>
			«ELSE»
				<a-ring id="«hash»"
					radius-inner="«segment.getProperty("innerRadius")»"
					radius-outer="«segment.getProperty("outerRadius")»" 
					color="«segment.getProperty("color") »"
					shader="flat"
					buffer="true"
					flat-shading="true"
					depth-test="false"
					depth-write="false"
					theta-start="«segment.getProperty("anglePosition")»"
					theta-length="«segment.getProperty("angle")»"
«««					segments-theta="«(segment.getProperty("angle") as Double/20).intValue+1»"
					segments-phi="1">
				</a-ring>
			«ENDIF»
		«ENDFOR»
		'''
		return result
	}
}
