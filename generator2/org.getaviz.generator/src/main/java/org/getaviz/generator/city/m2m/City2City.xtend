package org.getaviz.generator.city.m2m

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.database.Labels
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.getaviz.generator.city.CityUtils
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric
import org.getaviz.generator.SettingsConfiguration.OutputFormat
import java.util.HashMap
import java.util.List
import java.util.ArrayList
import org.getaviz.generator.SettingsConfiguration.Panels.SeparatorModes
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node
import org.neo4j.driver.v1.types.Path

class City2City {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var RGBColor[] PCKG_colors
	var RGBColor[] NOS_colors
	var properties = new HashMap<Long, double[]>
	var Node model
	val connector = DatabaseConnector::instance

	new() {
		log.info("City2City started")
		model = connector.executeRead("MATCH (n:Model {building_type: \'" + config.buildingTypeAsString +
			"\'}) RETURN n").next.get("n").asNode
		if (config.buildingType == BuildingType::CITY_BRICKS || config.buildingType == BuildingType::CITY_PANELS) {
			connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(m:BuildingSegment) RETURN m").forEach [
				setBuildingSegmentAttributes(get("m").asNode.id)
			]
		}
		val packageMaxLevel = connector.executeRead("MATCH p=(n:District)-[:CONTAINS*]->(m:District) 
							   WHERE NOT (m)-[:CONTAINS]->(:District) RETURN length(p) AS length ORDER BY length(p) DESC LIMIT 1").
			single.get("length").asLong.intValue + 1
		PCKG_colors = createColorGradiant(new RGBColor(config.packageColorStart), new RGBColor(config.packageColorEnd),
			packageMaxLevel)

		if (config.originalBuildingMetric == BuildingMetric::NOS) {
			val NOS_max = connector.executeRead("MATCH (n:Building) RETURN max(n.numberOfStatements) AS nos").single.
				get("nos").asInt
			NOS_colors = createColorGradiant(new RGBColor(config.classColorStart), new RGBColor(config.classColorEnd),
				NOS_max + 1)
		}

		connector.executeRead("MATCH p=(n:Model:City)-[:CONTAINS*]->(m:District) RETURN p").forEach [
			setDistrictAttributes(get("p").asPath)
		]
		connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEach [
			setBuildingAttributes(get("b").asNode)
		]
		connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(d:District)-[:VISUALIZES]->(element)  RETURN d, element.hash as hash ORDER BY element.hash").forEach [
			val node = get("d").asNode
			var width = node.get("width").asDouble(0.0)
			var length = node.get("length").asDouble(0.0)
			val double[] array = #[width, length]
			properties.put(node.id, array)
			log.debug("New Package: " + get("hash").asString);
			log.debug("width: " + width)
			log.debug("length: " + length)
		]
		connector.executeRead(
			"MATCH (n:Model:City)-[:CONTAINS*]->(b:Building)-[:VISUALIZES]->(element) " +
				"RETURN b, element.hash as hash " + "ORDER BY element.hash"
		).forEach [
			val node = get("b").asNode
			var width = node.get("width").asDouble(0.0)
			var length = node.get("length").asDouble(0.0)
			val double[] array = #[width, length]
			properties.put(node.id, array)
			log.debug("New Type: " + get("hash").asString);
			log.debug("width: " + width)
			log.debug("length: " + length)
		]

		CityLayout::cityLayout(model.id, properties)

		switch (config.buildingType) {
			case CITY_BRICKS:
				BrickLayout.brickLayout(model.id) // Layout for buildingSegments
			case CITY_PANELS:
				connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEach [
					setBuildingSegmentPositions(get("b").asNode);
				]
			case CITY_FLOOR:
				connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEach [
					calculateSegments(get("b").asNode);
				]
			default: {
			} // CityDebugUtils.infoEntities(cityRoot.document.entities, 0, true, true)	
		}
		log.info("City2City finished")
	}

	def private void setDistrictAttributes(Path districtPath) {
		var color = ""
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.packageColorHex
		} else {
			color = PCKG_colors.get(districtPath.length - 1).asPercentage
		}
		connector.executeWrite(
			String.format("MATCH (n) WHERE ID(n) = %d SET n.height = %f, n.color = \'%s\'", districtPath.end.id,
				config.heightMin, color))
	}

	def private void setBuildingAttributes(Node building) {
		val methodCounter = connector.executeRead(
			"MATCH (n)-[:VISUALIZES]->(o)-[:DECLARES]->(m:Method) WHERE ID(n) = " + building.id +
				" RETURN COUNT(m) AS mCount").single.get("mCount").asInt
		val dataCounter = connector.executeRead(
			"MATCH (n)-[:VISUALIZES]->(o)-[:DECLARES]->(f:Field) WHERE ID(n) = " + building.id +
				" AND NOT o:Enum RETURN COUNT(f) AS fCount").single.get("fCount").asInt
		switch (config.buildingType) {
			case CITY_ORIGINAL: setBuildingAttributesOriginal(building, methodCounter, dataCounter)
			case CITY_PANELS: setBuildingAttributesPanels(building, methodCounter, dataCounter)
			case CITY_BRICKS: setBuildingAttributesBricks(building, methodCounter, dataCounter)
			case CITY_FLOOR: setBuildingAttributesFloors(building, methodCounter, dataCounter)
		}
	}

	def private setBuildingAttributesOriginal(Node building, int methodCounter, int dataCounter) {
		var width = 0.0
		var length = 0.0
		var height = 0.0
		var color = ""
		if (dataCounter == 0) {
			width = config.widthMin
			length = config.widthMin
		} else {
			width = dataCounter
			length = dataCounter
		}
		if (methodCounter == 0) {
			height = config.heightMin
		} else {
			height = methodCounter
		}
		if (config.originalBuildingMetric == BuildingMetric::NOS) {
			color = NOS_colors.get(building.get("numberOfStatements").asInt(0)).asPercentage
		} else if (config.outputFormat == OutputFormat::AFrame) {
			color = config.classColorHex
		} else {
			color = new RGBColor(config.classColor).asPercentage
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id, width, length, height, color))
	}

	def private setBuildingAttributesPanels(Node building, int methodCounter, int dataCounter) {
		var height = 0.0
		var width = 0.0
		var length = 0.0
		var color = ""
		if (config.showBuildingBase) {
			height = config.heightMin
		} else {
			height = 0
		}
		var int areaUnit = 1
		if (config.classElementsMode == ClassElementsModes::ATTRIBUTES_ONLY) {
			areaUnit = methodCounter
		} else {
			areaUnit = dataCounter
		}
		if (areaUnit <= 1) {
			width = config.widthMin + config.panelHorizontalMargin * 2
			length = config.widthMin + config.panelHorizontalMargin * 2
		} else {
			width = config.widthMin * areaUnit + config.panelHorizontalMargin * 2
			length = config.widthMin * areaUnit + config.panelHorizontalMargin * 2
		}
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.classColorHex
		} else {
			color = new RGBColor(config.classColor).asPercentage
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id, width, length, height, color))
	}

	def setBuildingAttributesBricks(Node building, int methodCounter, int dataCounter) {
		var height = 0.0
		var width = 0.0
		var length = 0.0
		var sideCapacity = 0
		var color = ""
		if (config.showBuildingBase) {
			height = config.heightMin
		} else {
			height = 0
		}
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.classColorHex
		} else {
			color = new RGBColor(config.classColor).asPercentage;
		}
		// Setting width, height & sideCapacity
		switch (config.brickLayout) {
			case STRAIGHT: {
				sideCapacity = 1;
			}
			case BALANCED: {
				switch (config.classElementsMode) {
					case ATTRIBUTES_ONLY: sideCapacity = calculateSideCapacity(methodCounter)
					case METHODS_AND_ATTRIBUTES: sideCapacity = calculateSideCapacity(dataCounter + methodCounter)
					default: sideCapacity = calculateSideCapacity(dataCounter)
				}
			}
			case PROGRESSIVE: {
				switch (config.classElementsMode) {
					case METHODS_ONLY: sideCapacity = calculateSideCapacity(methodCounter)
					case METHODS_AND_ATTRIBUTES: sideCapacity = calculateSideCapacity(dataCounter + methodCounter)
					default: sideCapacity = calculateSideCapacity(dataCounter)
				}
			}
			default: {
				sideCapacity = 1;
			}
		}
		width = config.brickSize * sideCapacity + config.brickHorizontalMargin * 2 +
			config.brickHorizontalGap * (sideCapacity - 1)
		length = config.brickSize * sideCapacity + config.brickHorizontalMargin * 2 +
			config.brickHorizontalGap * (sideCapacity - 1)
		connector.executeWrite(
			cypherSetBuildingSegmentAttributes(building.id, width, length, height, color) + ", n.sideCapacity = " +
				sideCapacity)
	}

	def void setBuildingAttributesFloors(Node building, int methodCounter, int dataCounter) {
		var width = 0.0
		var length = 0.0
		var height = 0.0
		var color = ""
		if (dataCounter < 2) { // pko 2016
			width = 2 // TODO in settings datei aufnehmen
			length = 2
		} else {
			width = Math.ceil(dataCounter / 4.0) + 1 // pko 2016
			length = Math.ceil(dataCounter / 4.0) + 1 // pko 2016
		}
		if (methodCounter == 0) {
			height = config.heightMin
		} else {
			height = methodCounter
		}
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.classColorHex
		} else {
			color = 53 / 255.0 + " " + 53 / 255.0 + " " + 89 / 255.0 // pko 2016
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id, width, length, height, color))
	}

	def private void setBuildingSegmentAttributes(Long segment) {
		switch (config.buildingType) {
			case CITY_PANELS:
				setBuildingSegmentAttributesPanels(segment)
			case CITY_BRICKS:
				setBuildingSegmentAttributesBricks(segment)
			default: {
			}
		}
	}

	def private setBuildingSegmentAttributesPanels(Long segment) {
		val path = connector.executeRead(
			"MATCH p = (parent)-[:CONTAINS]->(s)-[:VISUALIZES]->(e) WHERE ID(s) = " + segment + " RETURN p").next.get(
			"p").asPath
		val relatedEntity = path.end
		val parent = path.start.id
		val childs = connector.executeRead(
			"MATCH (s)-[:CONTAINS]->(child)-[r:VISUALIZES]->(e) WHERE ID(s) = " + parent +
				" AND (e:Field OR e:Method)" + " AND NOT (e)<-[:DECLARES]-(:Enum) RETURN e").map[get("e").asNode]
		var int areaUnit = 1
		if (config.classElementsMode == ClassElementsModes::ATTRIBUTES_ONLY) {
			areaUnit = childs.filter[hasLabel(Labels.Method.name)].size
		} else {
			areaUnit = childs.filter[hasLabel(Labels.Field.name)].size
		}
		var width = 0.0
		var length = 0.0
		if (areaUnit <= 1) {
			width = config.widthMin
			length = config.widthMin
		} else {
			width = config.widthMin * areaUnit
			length = config.widthMin * areaUnit
		}
		var index = 0
		val effectiveLineCount = relatedEntity.get("effectiveLineCount").asLong(0).intValue
		while (index < config.panelHeightTresholdNos.size &&
			effectiveLineCount >= config.panelHeightTresholdNos.get(index)) {
			index = index + 1
		}
		val height = config.panelHeightUnit * (index + 1)
		connector.executeWrite(
			cypherSetBuildingSegmentAttributes(segment, width, length, height,
				CityUtils.setBuildingSegmentColor(relatedEntity)))
	}

	def private setBuildingSegmentAttributesBricks(Long segment) {
		val path = connector.executeRead(
			"MATCH p = (parent)-[:CONTAINS]->(s)-[:VISUALIZES]->(e) WHERE ID(s) = " + segment + " RETURN p").next.get(
			"p").asPath
		val relatedEntity = path.end
		connector.executeWrite(
			cypherSetBuildingSegmentAttributes(segment, config.brickSize, config.brickSize, config.brickSize,
				CityUtils.setBuildingSegmentColor(relatedEntity)))
		CityUtils.setBuildingSegmentColor(relatedEntity);
	}

	def private void setBuildingSegmentPositions(Node building) {
		// Sorting elements
		var List<Node> classElements = new ArrayList
		switch (config.classElementsMode) {
			case ATTRIBUTES_ONLY:
				classElements += CityUtils.getData(building.id)
			case METHODS_AND_ATTRIBUTES: {
				classElements += CityUtils.getData(building.id)
				classElements += CityUtils.getMethods(building.id)
			}
			default:
				classElements += CityUtils.getMethods(building.id)
		}
		CityUtils.sortBuildingSegments(classElements)
		// upper bound of the panel below the actual panel inside the loop
		val position = connector.getPosition(building.id);
		var lowerBsPosY = position.get("y").asDouble + building.get("height").asDouble / 2 + config.panelVerticalMargin

		// Correcting the initial gap on top of building depending on SeparatorMode
		if (config.panelSeparatorMode == SeparatorModes::GAP ||
			config.panelSeparatorMode == SeparatorModes::SEPARATOR) {
			lowerBsPosY = lowerBsPosY - config.panelVerticalGap
		}
		// System.out.println("")
		// Looping through methods of building
		for (var i = 0; i < classElements.size(); i++) {
			val segment = classElements.get(i);
			val height = segment.get("height").asDouble
			val width = segment.get("width").asDouble
			// System.out.println(bs.getType() + " " + bs.getValue() + " " + bs.getModifiers() + " " + bs.getNumberOfStatements());
//			val bsPos = cityFactory.createPosition
			val x = position.get("x").asDouble
			var double y
			val z = position.get("z").asDouble
			var panelSeparatorCypher = ""
			switch (config.panelSeparatorMode) {
				case NONE: { // place segments on top of each other
					y = lowerBsPosY + height / 2
					lowerBsPosY = y + height / 2
				}
				case GAP: { // Leave a free space between segments
					y = lowerBsPosY + config.panelVerticalGap + height / 2
					lowerBsPosY = y + height / 2
				}
				case SEPARATOR: { // Placing additional separators
					y = lowerBsPosY + height / 2
					// Placing a separator on top of the current method if it is not last method
					if (i < classElements.size() - 1) {
						val sepY = y + height / 2 + config.panelSeparatorHeight / 2
						// Deciding which shape the separator has to have
						val nextElementType = connector.getVisualizedEntity(classElements.get(i + 1).id)
						val segmentType = connector.getVisualizedEntity(segment.id)
						panelSeparatorCypher = String.format(
							"(psp:City:Position {x: %f, y: %f, z: %f})<-[:HAS]-(ps:City:PanelSeparator", x, sepY, z)
						if ((segmentType.hasLabel(Labels.Method.name) &&
							nextElementType.hasLabel(Labels.Method.name)) || !config.showAttributesAsCylinders) {
							panelSeparatorCypher +=
								String.format(":Box {width: %f, length: %f})<-[:HAS]-", width,
									segment.get("length").asDouble)
						} else {
							panelSeparatorCypher += String.format(":Cylinder {radius: %f})<-[:HAS]-", width / 2)
						}
						lowerBsPosY = sepY + config.panelSeparatorHeight / 2
					}
				}
			}
			val s = String.format(
				"MATCH(n) WHERE ID(n) = %d CREATE %s(n)-[:HAS]->(p:Position:City {x: %f, y: %f, z: %f})", segment.id,
				panelSeparatorCypher, x, y, z)
			connector.executeWrite(s)
		}
	}

	def calculateSegments(Node building) {
		building.calculateFloors
		building.calculateChimneys
	}

	def void calculateFloors(Node building) {
		val position = connector.getPosition(building.id)
		val bHeight = building.get("height").asDouble
		val bWidth = building.get("width").asDouble
		val bLength = building.get("length").asDouble
		val bPosX = position.get("x").asDouble
		val bPosY = position.get("y").asDouble
		val bPosZ = position.get("z").asDouble
		val floors = connector.executeRead("MATCH (n)-[:CONTAINS]->(f:Floor) WHERE ID(n) = " + building.id +
			" RETURN f")
		var floorNumberValue = connector.executeRead("MATCH (n)-[:CONTAINS]->(f:Floor) WHERE ID(n) = " + building.id +
			" RETURN COUNT(f) as floorNumber").single.get("floorNumber")

		var floorCounter = 0
		while (floors.hasNext) {
			val record = floors.next
			val floor = record.get("f").asNode.id
			floorCounter++
			var color = 20 / 255.0 + " " + 133 / 255.0 + " " + 204 / 255.0
			if (config.outputFormat == OutputFormat::AFrame) {
				color = "#1485CC"
			}
			var statement = cypherSetBuildingSegmentAttributes(floor, bWidth * 1.1, bLength * 1.1,
				bHeight / (floorNumberValue.asInt + 2 ) * 0.80, color)
			statement +=
				String.format("CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})", bPosX,
					(bPosY - ( bHeight / 2) ) + bHeight / ( floorNumberValue.asInt + 2 ) * floorCounter, bPosZ)
			connector.executeWrite(statement)
		}
	}

	def void calculateChimneys(Node building) {
		val position = connector.getPosition(building.id)
		val bHeight = building.get("height").asDouble
		val bWidth = building.get("width").asDouble
		val bPosX = position.get("x").asDouble
		val bPosY = position.get("y").asDouble
		val bPosZ = position.get("z").asDouble

		// val chimneyNumber = chimneys.length
		var courner1 = newArrayList()
		var courner2 = newArrayList()
		var courner3 = newArrayList()
		var courner4 = newArrayList()

		var chimneyCounter = 0
		val chimneys = connector.executeRead("MATCH (n)-[:CONTAINS]->(c:Chimney) WHERE ID(n) = " + building.id +
			" RETURN c")
		while (chimneys.hasNext) {
			val chimney = chimneys.next.get("c").asNode.id
			var color = 255 / 255.0 + " " + 252 / 255.0 + " " + 25 / 255.0
			if (config.outputFormat == OutputFormat::AFrame) {
				color = "#FFFC19"
			}
			connector.executeWrite(cypherSetBuildingSegmentAttributes(chimney, 0.5, 0.5, 1.0, color))
			if (chimneyCounter % 4 == 0) {
				courner1.add(chimney)
			}
			if (chimneyCounter % 4 == 1) {
				courner2.add(chimney)
			}
			if (chimneyCounter % 4 == 2) {
				courner3.add(chimney)
			}
			if (chimneyCounter % 4 == 3) {
				courner4.add(chimney)
			}
			chimneyCounter++

		}
		chimneyCounter = 0
		for (chimney : courner1) {
			val x = (bPosX - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter)
			val y = (bPosY + ( bHeight / 2) ) + 0.5
			val z = (bPosZ - ( bWidth / 2) ) + 0.5
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z))
			chimneyCounter++
		}
		chimneyCounter = 0
		for (chimney : courner2) {
			val x = (bPosX + ( bWidth / 2) ) - 0.5
			val y = (bPosY + ( bHeight / 2) ) + 0.5
			val z = (bPosZ - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter)
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z))
			chimneyCounter++
		}
		chimneyCounter = 0
		for (chimney : courner3) {
			val x = (bPosX + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter)
			val y = (bPosY + ( bHeight / 2) ) + 0.5
			val z = (bPosZ + ( bWidth / 2) ) - 0.5
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z))
			chimneyCounter++
		}
		chimneyCounter = 0
		for (chimney : courner4) {
			val x = (bPosX - ( bWidth / 2) ) + 0.5
			val y = (bPosY + ( bHeight / 2) ) + 0.5
			val z = (bPosZ + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter)
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z))
			chimneyCounter++
		}
	}

	def private RGBColor[] createColorGradiant(RGBColor start, RGBColor end, int maxLevel) {
		var steps = maxLevel - 1
		if (maxLevel == 1) {
			steps++
		}
		val r_step = (end.r - start.r) / steps
		val g_step = (end.g - start.g) / steps
		val b_step = (end.b - start.b) / steps

		val colorRange = newArrayOfSize(maxLevel)
		for (i : 0 ..< maxLevel) {
			val newR = start.r + i * r_step
			val newG = start.g + i * g_step
			val newB = start.b + i * b_step
			colorRange.set(i, new RGBColor(newR, newG, newB))
		}
		return colorRange
	}

	// Calculates side capacity for progressive/balanced bricks layout
	def private int calculateSideCapacity(double value) {
		var sc = 0 // side capacity
		var lc = 0 // layer capacity
		var nolMin = 0 // number of layers
		var bcMin = 0 // building capacity min
		var bcMax = 0 // building capacity max
		do {
			sc++
			lc = sc * 4
			nolMin = sc * 2
			bcMin = lc * nolMin
			bcMax = bcMin - 1
		} while (bcMax < value)

		return sc;
	}

	def private String cypherSetBuildingSegmentAttributes(Long segment, double width, double length, double height,
		String color) {
		return String.format(
			"MATCH (n) WHERE ID(n) = %d SET n.width = %f, n.length = %f, n.height = %f, n.color = \'%s\'", segment,
			width, length, height, color)
	}

}
