package org.getaviz.generator.city.m2t

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.getaviz.generator.database.Labels
import org.apache.commons.logging.LogFactory
import java.io.FileWriter
import org.getaviz.generator.OutputFormatHelper
import java.io.IOException
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node
import java.util.ArrayList

class City2X3D {
	val config = SettingsConfiguration.instance
	val connector = DatabaseConnector.instance
	val log = LogFactory::getLog(class)
	extension OutputFormatHelper helper = new OutputFormatHelper
		
	new () {
		log.info("CityOutput started")
		var FileWriter fw = null
		var head = X3DHead()
		var body = viewports() + toX3DModel()
		var tail = X3DTail()
		var fileName = "model.x3d"
		try {
			fw = new FileWriter(config.outputPath + fileName)
			switch (config.buildingType) {
				case BuildingType::CITY_ORIGINAL: fw.write(head + body  + tail)
				case BuildingType::CITY_PANELS,
				case BuildingType::CITY_FLOOR,
				case BuildingType::CITY_BRICKS:
					fw.write(head + settingsInfo + body + tail)
			}
		} catch (IOException e) {
			log.error(e)
			log.error("Could not create file")
		} finally {
			if (fw !== null)
				try {
					fw.close;
				} catch (IOException e) {
					e.printStackTrace;
				}
		}
		log.info("CityOutput finished")
	}
	
	def private String toX3DModel() {
		val districts = new StringBuilder
		val buildings = new StringBuilder
		val segments = new StringBuilder
		connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(d:District)-[:VISUALIZES]->(e) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN d,e").
		forEach[districts.append(toDistrict(get("d").asNode,get("e").asNode))]
		connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(b:Building)-[:VISUALIZES]->(e) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN b,e").
		forEach[buildings.append(toBuilding(get("b").asNode,get("e").asNode))]	
		if(!(config.buildingType == BuildingType.CITY_ORIGINAL)) {
			connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(bs:BuildingSegment)-[:VISUALIZES]->(e) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN bs, e").forEach[
				val segment = get("bs").asNode
				if(segment.hasLabel(Labels.Floor.name)) {
					segments.append(toFloor(segment, get("e").asNode))
				} 
				else if(segment.hasLabel(Labels.Chimney.name)) {
					segments.append(toChimney(segment,get("e").asNode))
				}
				else {
					segments.append(toBuildingSegment(segment, get("e").asNode))
				}
			]
		}			
		return districts.toString + buildings + segments
	}
	
	def private toDistrict(Node district, Node entity) {
		val position = connector.getPosition(district.id)
		val result = '''
		<Group DEF='«entity.get("hash").asString»'>
			<Transform translation='«position.get("x").asDouble +" "+ position.get("y").asDouble +" "+ position.get("z").asDouble»'>
				<Shape>
					<Box size='«district.get("width").asDouble +" "+ district.get("height").asDouble +" "+ district.get("length").asDouble»'></Box>
					<Appearance>
						<Material diffuseColor='«district.get("color").asString»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}
	
	def private toBuilding(Node building, Node entity) {
		val position = connector.getPosition(building.id)
		val result = '''
		<Group DEF='«entity.get("hash").asString»'>
			<Transform translation='«position.get("x").asDouble +" "+ position.get("y").asDouble +" "+ position.get("z").asDouble»'>
				<Shape>
					<Box size='«building.get("width").asDouble +" "+ building.get("height").asDouble +" "+ building.get("length").asDouble»'></Box>
					<Appearance>
						<Material diffuseColor='«building.get("color").asString»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}

	def private String toBuildingSegment(Node segment, Node entity) {
		val position = connector.getPosition(segment.id)
		val separators = new ArrayList
		connector.executeRead("MATCH (n)-[:HAS]->(ps:PanelSeparator)-[:HAS]->(p:Position) WHERE ID(n) = " + segment.id + " RETURN ps,p").forEach[separators.add(it)]
		val x = position.get("x").asDouble
		val y = position.get("y").asDouble
		val z = position.get("z").asDouble
		val width = segment.get("width").asDouble
		val height = segment.get("height").asDouble
		val length = segment.get("length").asDouble
		val result = '''
		<Group DEF='«entity.get("hash").asString»'>
			<Transform translation='«x +" "+ y +" "+ z»'>
				<Shape>
				«IF config.buildingType == BuildingType.CITY_PANELS
						&& entity.hasLabel(Labels.Field.name)
						&& config.showAttributesAsCylinders»
					<Cylinder radius='«width/2»' height='«height»'></Cylinder>
				«ELSE»
					<Box size='«width +" "+ height +" "+ length»'></Box>
				«ENDIF»
					<Appearance>
						<Material diffuseColor='«segment.get("color").asString»'></Material>
					</Appearance>
				</Shape>
			</Transform>
			«FOR record : separators»
			«val separator = record.get("ps").asNode»
			«val pos = record.get("p").asNode»
			<Transform translation='«pos.get("x").asDouble +" "+ pos.get("y").asDouble +" "+ pos.get("z").asDouble»'>
				<Shape>
				«IF separator.hasLabel(Labels.Cylinder.name)»
					<Cylinder radius='«separator.get("radius").asDouble»' height='«config.panelSeparatorHeight»'></Cylinder>
				«ELSE»
					<Box size='«separator.get("width").asDouble +" "+ config.panelSeparatorHeight + " "+ separator.get("length").asDouble»'></Box>
				«ENDIF»
					<Appearance>
						<Material diffuseColor='«config.getCityColorAsPercentage("black")»'></Material>
					</Appearance>
				</Shape>
			</Transform>
			«ENDFOR»
		</Group>
	'''	
		return result
	}
	
	def private toFloor(Node floor, Node entity) {
		val position = connector.getPosition(floor.id)
		val result = '''
		<Group DEF='«entity.get("hash")»'>
			<Transform translation='«position.get("x").asDouble +" "+ position.get("y").asDouble +" "+ position.get("z").asDouble»'>
				<Shape>
					<Box size='«floor.get("width").asDouble +" "+ floor.get("height").asDouble +" "+ floor.get("length").asDouble»'></Box>
					<Appearance>
						<Material diffuseColor='«floor.get("color").asString»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}
	
	def private toChimney(Node chimney, Node entity) {
		val position = connector.getPosition(chimney.id)
		val result = '''
		<Group DEF='«entity.get("hash")»'>
			<Transform translation='«position.get("x").asDouble +" "+ position.get("y").asDouble +" "+ position.get("z").asDouble»'>
				<Shape>
					<Cylinder height='«chimney.get("height").asDouble»' radius='«chimney.get("width").asDouble»'></Cylinder>
					<Appearance>
						<Material diffuseColor='«chimney.get("color").asString»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}				
}