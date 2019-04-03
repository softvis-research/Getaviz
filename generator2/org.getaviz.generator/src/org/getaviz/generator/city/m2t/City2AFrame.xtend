package org.getaviz.generator.city.m2t

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.database.Labels
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.apache.commons.logging.LogFactory
import java.io.IOException
import java.io.FileWriter
import org.getaviz.generator.OutputFormatHelper
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node
import java.util.ArrayList

class City2AFrame {
	val config = SettingsConfiguration.instance
	val connector = DatabaseConnector.instance
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
		connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(d:District)-[:HAS]->(p:Position) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN d,p").forEach[
			districts.append(toDistrict(get("d").asNode,get("p").asNode))
		]		
		if(config.buildingType == BuildingType.CITY_ORIGINAL || config.showBuildingBase) {
			connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(b:Building)-[:HAS]->(p:Position) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN b,p").forEach[
				buildings.append(toBuilding(get("b").asNode,get("p").asNode))
			]	
		}
			
		if(!(config.buildingType == BuildingType.CITY_ORIGINAL)) {
			connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(bs:BuildingSegment)-[:HAS]->(p:Position) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN bs,p").forEach[
				val segment = get("bs").asNode
				if(segment.hasLabel(Labels.Floor.name)) {
					segments.append(toFloor(segment, get("p").asNode))
				} 
				else if(segment.hasLabel(Labels.Chimney.name)) {
					segments.append(toChimney(segment,get("p").asNode))
				}
				else {
					segments.append(toBuildingSegment(segment, get("p").asNode))
				}				
			]
		}			
		return districts.toString + buildings + segments
	}
	
	def private String toDistrict(Node district, Node position) {
		val entity = connector.getVisualizedEntity(district.id)
		val result = '''
		<a-box id="«entity.get("hash").asString»"
			position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
			width="«district.get("width")»"
			height="«district.get("height")»"
			depth="«district.get("length")»"
			color="«district.get("color").asString»"
			shader="flat"
			flat-shading="true">
		</a-box>
	'''
		return result	
	}

	def private String toBuilding(Node building, Node position) {
		val entity = connector.getVisualizedEntity(building.id)
		val result = '''
		<a-box id="«entity.get("hash").asString»"
				position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
				width="«building.get("width")»"
				height="«building.get("height")»"
				depth="«building.get("length")»"
				color="«building.get("color").asString»"
				shader="flat"
				flat-shading="true">
		</a-box>
	'''
		return result
	}

	def private String toBuildingSegment(Node segment, Node position) {
		val entity = connector.getVisualizedEntity(segment.id)
		val separators = new ArrayList
		connector.executeRead("MATCH (n)-[:HAS]->(ps:PanelSeparator) RETURN ps").forEach[separators.add(get("ps").asNode)]
		val width = segment.get("width").asDouble
		val height = segment.get("height").asDouble
		val length = segment.get("length").asDouble
		val result = '''
			«IF config.buildingType == BuildingType.CITY_PANELS
					&& entity.hasLabel(Labels.Field.name)
					&& config.showAttributesAsCylinders»
				<a-cylinder id="«entity.get("hash").asString»"
					position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
					radius="«width/2»"
					height="«height»" 
					color="«segment.get("color").asString»"
					shader="flat"
					flat-shading="true"
					segments-height="2"
					segments-radial="20">
				</a-cylinder>
			«ELSE»
				<a-box id="«entity.get("hash").asString»"
					position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
					width="«width»"
					height="«height»"
					depth="«length»"
					color="«segment.get("color").asString»"
					shader="flat"
					flat-shading="true">
				</a-box>
			«ENDIF»		
			«FOR separator : separators»
			«val pos = connector.getPosition(separator.id)»
			«IF separator.hasLabel(Labels.Cylinder.name)»
				<a-cylinder  id="«entity.get("hash").asString»"
					position="«pos.get("x") + " " + pos.get("y") + " " + pos.get("z")»"
					radius="«separator.get("radius")»" 
					height="«config.panelSeparatorHeight»" 
					color="«config.getCityColorHex("black")»"
					shader="flat"
					flat-shading="true"
					segments-height="2"
					segments-radial="20">
				</a-cylinder>
			«ELSE»
				<a-box id="«entity.get("hash").asString»"
					position="«pos.get("x") + " " + pos.get("y") + " " + pos.get("z")»"
					width="«separator.get("width")»"
					height="«config.panelSeparatorHeight»"
					depth="«separator.get("length")»"
					color="«config.getCityColorHex("black")»"
					shader="flat"
					flat-shading="true">
				</a-box>
			«ENDIF»
		«ENDFOR»
	'''	
		return result
	}	

	def private toFloor(Node floor, Node position) {
		val entity = connector.getVisualizedEntity(floor.id)
		val result = '''
			<a-box id="«entity.get("hash").asString»"
				position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
				width="«floor.get("width")»"
				height="«floor.get("height")»"
				depth="«floor.get("length")»"
				color="«floor.get("color").asString»"
				shader="flat"
				flat-shading="true">
			</a-box>
	'''
		return result
	}

	def private toChimney(Node chimney, Node position) {
		val entity = connector.getVisualizedEntity(chimney.id)		
		val result = '''
			<a-box id="«entity.get("hash").asString»"
				position="«position.get("x") + " " + position.get("y") + " " + position.get("z")»"
				width="«chimney.get("width")»"
				height="«chimney.get("height")»"
				depth="«chimney.get("length")»"
				color="«chimney.get("color").asString»"
				shader="flat"
				flat-shading="true">
			</a-box>
	'''	
		return result
	}
}