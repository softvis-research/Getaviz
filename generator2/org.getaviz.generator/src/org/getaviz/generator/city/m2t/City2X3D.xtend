package org.getaviz.generator.city.m2t

import org.getaviz.generator.SettingsConfiguration
import org.neo4j.graphdb.Transaction
import org.getaviz.generator.database.Database
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Direction
import org.getaviz.generator.database.Rels
import org.getaviz.generator.database.Labels
import org.apache.commons.logging.LogFactory
import java.io.FileWriter
import org.getaviz.generator.OutputFormatHelper
import java.io.IOException

class City2X3D {
	val config = SettingsConfiguration.instance
	val graph = Database::instance
	var Transaction tx
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
			log.error("Could not create file");
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
		tx = graph.beginTx
		try {
			var result = graph.execute("MATCH (n:Model)-[:CONTAINS*]->(m:District) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN m").map[return get("m") as Node]
			result.forEach[districts.append(toDistrict)]
			tx.success
		} finally {
			tx.close
		}	
		
		tx = graph.beginTx
		try {
			var result = graph.execute("MATCH (n:Model)-[:CONTAINS*]->(m:Building) WHERE n.building_type = \'" + config.buildingTypeAsString + "\' RETURN m").map[return get("m") as Node]
			result.forEach[
				val entity = getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
				if(entity.hasLabel(Labels.Class) || entity.hasLabel(Labels.Interface)) {
					buildings.append(toBuilding(entity))
				}
			]
			tx.success
		} finally {
			tx.close
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
	
	def private toDistrict(Node district) {
		val entity = district.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val position = district.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val result = '''
		<Group DEF='«entity.getProperty("hash")»'>
			<Transform translation='«position.getProperty("x") +" "+ position.getProperty("y") +" "+ position.getProperty("z")»'>
				<Shape>
					<Box size='«district.getProperty("width") +" "+ district.getProperty("height") +" "+ district.getProperty("length")»'></Box>
					<Appearance>
						<Material diffuseColor='«district.getProperty("color")»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}
	
	def private toBuilding(Node building, Node entity) {
		val position = building.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val result = '''
		<Group DEF='«entity.getProperty("hash")»'>
			<Transform translation='«position.getProperty("x") +" "+ position.getProperty("y") +" "+ position.getProperty("z")»'>
				<Shape>
					<Box size='«building.getProperty("width") +" "+ building.getProperty("height") +" "+ building.getProperty("length")»'></Box>
					<Appearance>
						<Material diffuseColor='«building.getProperty("color")»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}

	def private String toBuildingSegment(Node segment) {
		val entity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val has = segment.getRelationships(Rels.HAS, Direction.OUTGOING).map[return endNode]
		val position = has.filter[hasLabel(Labels.Position)].head
		val separators = has.filter[hasLabel(Labels.PanelSeparator)]
		val x = position.getProperty("x")
		val y = position.getProperty("y")
		val z = position.getProperty("z")
		val width = segment.getProperty("width") as Double
		val height = segment.getProperty("height")
		val length = segment.getProperty("length")
		val result = '''
		<Group DEF='«entity.getProperty("hash")»'>
			<Transform translation='«x +" "+ y +" "+ z»'>
				<Shape>
				«IF config.buildingType == BuildingType.CITY_PANELS
						&& segment.hasLabel(Labels.Field)
						&& config.showAttributesAsCylinders»
					<Cylinder radius='«width/2»' height='«height»'></Cylinder>
				«ELSE»
					<Box size='«width +" "+ height +" "+ length»'></Box>
				«ENDIF»
					<Appearance>
						<Material diffuseColor='«segment.getProperty("color")»'></Material>
					</Appearance>
				</Shape>
			</Transform>
			«FOR separator : separators»
			«val pos = separator.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode»
			<Transform translation='«pos.getProperty("x") +" "+ pos.getProperty("y") +" "+ pos.getProperty("z")»'>
				<Shape>
				«IF separator.hasLabel(Labels.Cylinder)»
					<Cylinder radius='«separator.getProperty("radius")»' height='«config.panelSeparatorHeight»'></Cylinder>
				«ELSE»
					<Box size='«separator.getProperty("width") +" "+ config.panelSeparatorHeight + " "+ separator.getProperty("length")»'></Box>
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
	
	def private toFloor(Node floor) {
		val entity = floor.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val position = floor.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val result = '''
		<Group DEF='«entity.getProperty("hash")»'>
			<Transform translation='«position.getProperty("x") +" "+ position.getProperty("y") +" "+ position.getProperty("z")»'>
				<Shape>
					<Box size='«floor.getProperty("width") +" "+ floor.getProperty("height") +" "+ floor.getProperty("length")»'></Box>
					<Appearance>
						<Material diffuseColor='«floor.getProperty("color")»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}
	
	def private toChimney(Node chimney) {
		val entity = chimney.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val position = chimney.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val result = '''
		<Group DEF='«entity.getProperty("hash")»'>
			<Transform translation='«position.getProperty("x") +" "+ position.getProperty("y") +" "+ position.getProperty("z")»'>
				<Shape>
					<Cylinder height='«chimney.getProperty("height")»' radius='«chimney.getProperty("width")»'></Cylinder>
					<Appearance>
						<Material diffuseColor='«chimney.getProperty("color")»'></Material>
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
		return result
	}				
}