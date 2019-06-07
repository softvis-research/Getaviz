package org.getaviz.generator.rd.m2t

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.lib.database.Database
import org.neo4j.graphdb.Node
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.getaviz.generator.OutputFormatHelper
import java.io.FileWriter
import java.io.IOException
import org.apache.commons.logging.LogFactory

class RD2X3D {
	val config = SettingsConfiguration.instance
	val graph = Database::getInstance(config.databaseName)
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
		var tx = graph.beginTx
		try {
			var result = graph.execute("MATCH (n:Disk) RETURN n").map[return get("n") as Node]
			result.forEach[disks.append(toDisk)]
			tx.success
		} finally {
			tx.close
		}	
		tx = graph.beginTx
		try {
			var result = graph.execute("MATCH (n:DiskSegment) RETURN n").map[return get("n") as Node]
			result.forEach[segments.append(toSegment)]
			tx.success
		} finally {
			tx.close
		}		
		return disks.toString + segments.toString
	}
	
	def private String toDisk(Node disk) {
		val position = disk.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode as Node
		val entity = disk.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val result = '''
		<Transform translation='«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»' 
			rotation='0 0 1 1.57'
			scale='1 1 «disk.getProperty("height") as Double»'>
			<Transform DEF='«entity.getProperty("hash") as String»'>
				<Shape>
					<Extrusion
						convex='true'
						solid='true'
						crossSection='«disk.getProperty("crossSection") as String»'
						spine='«disk.getProperty("spine") as String»'
						creaseAngle='1'
						beginCap='true'
						endCap='true'></Extrusion>
					<Appearance>
							<Material
								diffuseColor='«disk.getProperty("color") as String»'
								transparency='«disk.getProperty("transparency") as Double»'
							></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Transform>
	'''
		return result
	}
	
	def private String toSegment(Node segment) {
		val parent = segment.getSingleRelationship(Rels.CONTAINS, Direction.INCOMING).startNode
		val position = parent.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val entity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		var spines = segment.getProperty("spine") as String
		if(spines.isNullOrEmpty) return ""
		val result = '''
			<Transform  translation='«position.getProperty("x") + " " + position.getProperty("y") + " " +
			position.getProperty("z")»' rotation='0 0 1 1.57'>
				<Transform DEF='«entity.getProperty("hash", "NOHASH") as String»'>
					<Shape>
						<Extrusion
							convex='true'
							solid='true'
							crossSection='«segment.getProperty("crossSection") as String»'
							spine='«segment.getProperty("spine") as String»'
							creaseAngle='1'
							beginCap='true'
							endCap='true'></Extrusion>
						<Appearance>
								<Material
									diffuseColor='«segment.getProperty("color") as String»'
									transparency='«segment.getProperty("transparency") as Double»'
								></Material>
						</Appearance>
					</Shape>
				</Transform>
			</Transform>
	'''
		return result
	}	
}