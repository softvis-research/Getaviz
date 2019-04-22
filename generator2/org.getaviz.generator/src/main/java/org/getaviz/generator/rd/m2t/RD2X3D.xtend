package org.getaviz.generator.rd.m2t

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.OutputFormatHelper
import java.io.FileWriter
import java.io.IOException
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node

class RD2X3D {
	val config = SettingsConfiguration.instance
	val connector = DatabaseConnector::instance
	val log = LogFactory::getLog(class)
	extension OutputFormatHelper helper = new OutputFormatHelper
	
	new() {
		log.info("RD2X3D has started")
		var FileWriter fw = null
		var fileName = "model.x3d"
		try {
			fw = new FileWriter(config.outputPath + fileName)
			fw.write(X3DHead() + toRD + X3DTail())
		} catch (IOException e) {
			log.error("Could not create file " + fileName);
		} finally {
			if (fw !== null)
				try {
					fw.close;
				} catch (IOException e) {
					e.printStackTrace;
				}
		}
		log.info("RD2X3D has finished")
	}
	
	def private String toRD() {
		val disks = new StringBuilder
		val segments = new StringBuilder
		connector.executeRead("MATCH (n:Model:RD)-[:CONTAINS*]->(d:Disk)-[:HAS]->(p:Position) RETURN d,p").forEach[
			disks.append(toDisk(get("d").asNode,get("p").asNode))
		]
		
		connector.executeRead("MATCH (n:Model:RD)-[:CONTAINS*]->(ds:DiskSegment) RETURN ds").forEach[
			segments.append(toSegment(get("ds").asNode))
		]			
		return disks.toString + segments.toString
	}
	
	def private String toDisk(Node disk, Node position) {
		val entity = connector.getVisualizedEntity(disk.id)
		val result = '''
		<Transform translation='«position.get("x") + " " + position.get("y") + " " + position.get("z")»' 
			rotation='0 0 1 1.57'
			scale='1 1 «disk.get("height")»'>
			<Transform DEF='«entity.get("hash").asString»'>
				<Shape>
					<Extrusion
						convex='true'
						solid='true'
						crossSection='«disk.get("crossSection").asString»'
						spine='«disk.get("spine").asString»'
						creaseAngle='1'
						beginCap='true'
						endCap='true'></Extrusion>
					<Appearance>
							<Material
								diffuseColor='«disk.get("color").asString»'
								transparency='«disk.get("transparency")»'
							></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Transform>
	'''
		return result
	}
	
	def private String toSegment(Node segment) {
		val position = connector.executeRead("MATCH (n)<-[:CONTAINS]-(parent)-[:HAS]->(p:Position) WHERE ID(n) = " + segment.id + " RETURN p").single.get("p").asNode
		val entity = connector.getVisualizedEntity(segment.id)
		val result = '''
			<Transform  translation='«position.get("x") + " " + position.get("y") + " " +
			position.get("z")»' rotation='0 0 1 1.57'>
				<Transform DEF='«entity.get("hash").asString»'>
					<Shape>
						<Extrusion
							convex='true'
							solid='true'
							crossSection='«segment.get("crossSection").asString»'
							spine='«segment.get("spine").asString»'
							creaseAngle='1'
							beginCap='true'
							endCap='true'></Extrusion>
						<Appearance>
								<Material
									diffuseColor='«segment.get("color").asString»'
									transparency='«segment.get("transparency")»'
								></Material>
						</Appearance>
					</Shape>
				</Transform>
			</Transform>
	'''
		return result
	}	
}