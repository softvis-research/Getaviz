package org.getaviz.generator.city.m2t

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.lib.database.Database
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.getaviz.lib.database.Labels
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.neo4j.graphdb.Transaction
import org.apache.commons.logging.LogFactory
import java.io.IOException
import java.io.FileWriter
import org.getaviz.generator.OutputFormatHelper

class City2AFrame {
	val config = SettingsConfiguration.instance
	val graph = Database::instance	
	var Transaction tx
	val log = LogFactory::getLog(class)
	extension OutputFormatHelper helper = new OutputFormatHelper
	
	new() {
		log.info("City2AFrame has started")
		var FileWriter fw = null
		var fileName = "model.html"
		
		try {
			fw = new FileWriter(config.outputPath + fileName)
			fw.write(AFrameHead() + toAFrameModel() + AFrameTail())
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
		log.info("City2AFrame has finished")
	}
	
	def private String toAFrameModel() {
		val districts = new StringBuilder
		val buildings = new StringBuilder
		val segments = new StringBuilder
		tx = graph.beginTx
		try {
			var result = graph.execute("MATCH (n:Model)-[:CONTAINS*]->(m:District) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN m").map[return get("m") as Node]
			result.forEach[districts.append(toDistrict)]
			tx.success
		} finally {
			tx.close
		}	
		
		if(config.buildingType == BuildingType.CITY_ORIGINAL || config.showBuildingBase) {
			tx = graph.beginTx
			try {
				var result = graph.execute("MATCH (n:Model)-[:CONTAINS*]->(m:Building) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN m").map[return get("m") as Node]
				result.forEach[buildings.append(toBuilding)]
				tx.success
			} finally {
				tx.close
			}	
		}
			
		if(!(config.buildingType == BuildingType.CITY_ORIGINAL)) {
			tx = graph.beginTx
			try {
				var result = graph.execute("MATCH (n:Model)-[:CONTAINS*]->(m:BuildingSegment) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN m").map[return get("m") as Node]
				result.forEach[
					if(hasLabel(Labels.Floor)) {
						segments.append(toFloor)
					} else if(hasLabel(Labels.Chimney)) {
						segments.append(toChimney)
					}
					else {
						segments.append(toBuildingSegment)
					}
				]
				tx.success
			} finally {
				tx.close
			}
		}			
		return districts.toString + buildings + segments
	}
	
	def private String toDistrict(Node district) {
		val position = district.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val result = '''
		<a-box position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
			width="«district.getProperty("width")»"
			height="«district.getProperty("height")»"
			depth="«district.getProperty("length")»"
			color="«district.getProperty("color")»"
			shader="flat"
			fog="false"
			flat-shading="true">
		</a-box>
	'''
		return result	
	}

	def private String toBuilding(Node building) {
		val position = building.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val result = '''
		<a-box position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
				width="«building.getProperty("width")»"
				height="«building.getProperty("height")»"
				depth="«building.getProperty("length")»"
				color="«building.getProperty("color")»"
				shader="flat"
				fog="false"
				flat-shading="true">
		</a-box>
	'''
		return result
	}

	def private String toBuildingSegment(Node segment) {
		val entity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val has = segment.getRelationships(Rels.HAS, Direction.OUTGOING).map[return endNode]
		val position = has.filter[hasLabel(Labels.Position)].head
		val separators = has.filter[hasLabel(Labels.PanelSeparator)]
		val width = segment.getProperty("width") as Double
		val height = segment.getProperty("height")
		val length = segment.getProperty("length")
		val result = '''
			«IF config.buildingType == BuildingType.CITY_PANELS
					&& entity.hasLabel(Labels.Field)
					&& config.showAttributesAsCylinders»
				<a-cylinder position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
					 radius="«width/2»"
					 height="«height»" 
					 color="«segment.getProperty("color")»"
					 shader="flat"
					 fog="false"
					 flat-shading="true"
					 segments-height="2"
					 segments-radial="20">
				</a-cylinder>
			«ELSE»
				<a-box position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
						width="«width»"
						height="«height»"
						depth="«length»"
						color="«segment.getProperty("color")»"
						shader="flat"
						fog="false"
						flat-shading="true">
				</a-box>
			«ENDIF»		
			«FOR separator : separators»
			«val pos = separator.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode»
			«IF separator.hasLabel(Labels.Cylinder)»
				<a-cylinder position="«pos.getProperty("x") + " " + pos.getProperty("y") + " " + pos.getProperty("z")»"
					 radius="«separator.getProperty("radius")»" 
					 height="«config.panelSeparatorHeight»" 
					 color="«config.getCityColorHex("black")»"
					 shader="flat"
					 fog="false"
					 flat-shading="true"
					 segments-height="2"
					 segments-radial="20">
				</a-cylinder>
			«ELSE»
				<a-box position="«pos.getProperty("x") + " " + pos.getProperty("y") + " " + pos.getProperty("z")»"
						width="«separator.getProperty("width")»"
						height="«config.panelSeparatorHeight»"
						depth="«separator.getProperty("length")»"
						color="«config.getCityColorHex("black")»"
						shader="flat"
						fog="false"
						flat-shading="true">
				</a-box>
			«ENDIF»
		«ENDFOR»
	'''	
		return result
	}	

	def private toFloor(Node floor) {
		val has = floor.getRelationships(Rels.HAS, Direction.OUTGOING).map[return endNode]
		val position = has.filter[hasLabel(Labels.Position)].head		
		val result = '''
			<a-box position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
				width="«floor.getProperty("width")»"
				height="«floor.getProperty("height")»"
				depth="«floor.getProperty("length")»"
				color="«floor.getProperty("color")»"
				shader="flat"
				fog="false"
				flat-shading="true">
			</a-box>
	'''
		return result
	}

	def private toChimney(Node chimney) {
		val has = chimney.getRelationships(Rels.HAS, Direction.OUTGOING).map[return endNode]
		val position = has.filter[hasLabel(Labels.Position)].head			
		val result = '''
			<a-box position="«position.getProperty("x") + " " + position.getProperty("y") + " " + position.getProperty("z")»"
				width="«chimney.getProperty("width")»"
				height="«chimney.getProperty("height")»"
				depth="«chimney.getProperty("length")»"
				color="«chimney.getProperty("color")»"
				shader="flat"
				fog="false"
				flat-shading="true">
			</a-box>
	'''	
		return result
	}
}