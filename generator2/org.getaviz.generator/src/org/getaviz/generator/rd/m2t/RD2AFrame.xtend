package org.getaviz.generator.rd.m2t

import org.getaviz.generator.SettingsConfiguration
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.OutputFormatHelper
import java.io.FileWriter
import java.io.IOException
import org.neo4j.driver.v1.types.Node
import org.getaviz.generator.database.DatabaseConnector
import java.util.List

class RD2AFrame {
	val config = SettingsConfiguration.instance
	val connector = DatabaseConnector::instance
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
		connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(d:Disk)-[:HAS]->(p:Position) RETURN d,p").forEach[
			elements.append(toDisk(get("d").asNode,get("p").asNode))
		]
		return elements.toString
	}

	def private toDisk(Node disk, Node position) {
		val radius = disk.get("radius").asDouble
		val entity = connector.getVisualizedEntity(disk.id)
		val segments = newArrayList
		connector.executeRead("MATCH (n)-[:CONTAINS]->(ds:DiskSegment) WHERE ID(n) = " + disk.id + " RETURN ds").forEach[
			segments.add(get("ds").asNode)
		]
		val result = '''
			«IF radius - config.RDRingWidth == 0»
				<a-circle id="«entity.get("hash").asString»" 
					position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
					radius="«radius»" 
					color="«disk.get("color").asString »"
					shader="flat"
					buffer="true"
					flat-shading="true"
					depth-test="false"
					depth-write="false">
«««					segments="20">
					«segments.toSegment»
				</a-circle>
			«ELSE»
				<a-ring id="«entity.get("hash").asString»"
					position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
					radius-inner="«radius - config.RDRingWidth»"
					radius-outer="«radius»" 
					color="«disk.get("color").asString »"
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

	def private toSegment(List<Node> segments) {
		val result = '''
		«FOR segment : segments»
			«val entity = connector.getVisualizedEntity(segment.id)»
			«IF segment.get("innerRadius").asDouble == 0»
				<a-circle id="«entity.get("hash").asString»"
					radius="«segment.get("outerRadius")»" 
					color="«segment.get("color").asString »"
					theta-start="«segment.get("anglePosition")»"
					theta-length="«segment.get("angle")»"
					shader="flat"
					buffer="true"
					flat-shading="true"
					depth-test="false"
					depth-write="false">
«««					segments="«(segment.getProperty("angle") as Double/20).intValue+1»">
				</a-circle>
			«ELSE»
				<a-ring id="«entity.get("hash").asString»"
					radius-inner="«segment.get("innerRadius")»"
					radius-outer="«segment.get("outerRadius")»" 
					color="«segment.get("color").asString »"
					shader="flat"
					buffer="true"
					flat-shading="true"
					depth-test="false"
					depth-write="false"
					theta-start="«segment.get("anglePosition")»"
					theta-length="«segment.get("angle")»"
«««					segments-theta="«(segment.getProperty("angle") as Double/20).intValue+1»"
					segments-phi="1">
				</a-ring>
			«ENDIF»
		«ENDFOR»
		'''
		return result
	}
}
