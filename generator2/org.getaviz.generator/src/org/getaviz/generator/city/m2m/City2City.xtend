package org.getaviz.generator.city.m2m

import org.neo4j.graphdb.GraphDatabaseService
import org.getaviz.generator.SettingsConfiguration
import org.getaviz.lib.database.Database
import org.getaviz.lib.database.Labels
import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.neo4j.graphdb.Node
import org.getaviz.generator.city.CityUtils
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes
import org.neo4j.graphdb.Path
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric
import org.getaviz.generator.SettingsConfiguration.OutputFormat
import java.util.HashMap
import java.util.List
import java.util.ArrayList
import org.getaviz.generator.SettingsConfiguration.Panels.SeparatorModes
import org.apache.commons.logging.LogFactory

class City2City {
	var GraphDatabaseService graph
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var RGBColor[] PCKG_colors
	var RGBColor[] NOS_colors
	var properties = new HashMap<Long, double[]>
	var Node model
		
	new () {
		log.info("CityModification started")
		graph = Database::getInstance(config.databaseName)
			var tx = graph.beginTx
			try {
				model = graph.findNode(Labels.Model, "building_type", config.buildingTypeAsString)
				if (config.buildingType == BuildingType::CITY_BRICKS ||
					config.buildingType == BuildingType::CITY_PANELS) {
					val buildingSegments = graph.execute(
						"MATCH (n:Model:City)-[:CONTAINS*]->(m:BuildingSegment) RETURN m").map[return get("m") as Node]
					buildingSegments.forEach[setBuildingSegmentAttributes]
				}

				val result = graph.execute(
						"MATCH p=(n:District)-[:CONTAINS*]->(m:District) WHERE NOT (m)-[:CONTAINS]->(:District) RETURN length(p) AS length ORDER BY length(p) DESC LIMIT 1")
				val packageMaxLevel = (result.head.get("length") as Long).intValue + 1
					PCKG_colors = createColorGradiant(new RGBColor(config.packageColorStart),
				new RGBColor(config.packageColorEnd), packageMaxLevel)
				
				if (config.originalBuildingMetric == BuildingMetric::NOS) {
					val result2 = graph.execute("MATCH (n:Building) RETURN max(n.numberOfStatements) AS nos")
					val NOS_max = result2.head.get("nos") as Integer
					NOS_colors = createColorGradiant(new RGBColor(config.classColorStart),
					new RGBColor(config.classColorEnd), NOS_max + 1)
				}			
				
				val districtPaths = graph.execute("MATCH p=(n:Model:City)-[:CONTAINS*]->(m:District) RETURN p").map[return get("p") as Path]
				val buildingNodes = graph.execute("MATCH (n:Model:City)-[:CONTAINS*]->(m:Building) RETURN m").map[return get("m") as Node]
				districtPaths.forEach[setDistrictAttributes]
				buildingNodes.forEach[setBuildingAttributes]	
				tx.success
			} finally {
				tx.close
			}
			
			tx = graph.beginTx
			try {
				val districtPaths = graph.execute("MATCH (n:Model:City)-[:CONTAINS*]->(m:District) RETURN m").map[return get("m") as Node]
				val buildingNodes = graph.execute("MATCH (n:Model:City)-[:CONTAINS*]->(m:Building) RETURN m").map[return get("m") as Node]
				districtPaths.forEach[
					var width = 0.0
					var length = 0.0
					if(hasProperty("width")) {
						width = getProperty("width") as Double
					}
					if(hasProperty("length")) {
						length = getProperty("length") as Double
					}					
					val double[] array = #[width, length]
					properties.put(id, array)
				]
				buildingNodes.forEach[
					var width = 0.0
					var length = 0.0
					if(hasProperty("width")) {
						width = getProperty("width") as Double
					}
					if(hasProperty("length")) {
						length = getProperty("length") as Double
					}					
					val double[] array = #[width, length]
					properties.put(id, array)]	
					tx.success
			} finally {
				tx.close
			}
			CityLayout::cityLayout(model, properties)
			tx = graph.beginTx
			try {
				val buildingNodes = graph.execute("MATCH (n:Model:City)-[:CONTAINS*]->(m:Building) RETURN m").map[return get("m") as Node]
				switch (config.buildingType) {
					case CITY_BRICKS:
						BrickLayout.brickLayout(model) // Layout for buildingSegments
					case CITY_PANELS:
						buildingNodes.forEach[setBuildingSegmentPositions]
					case CITY_FLOOR: {
						buildingNodes.forEach[calculateSegments]
					}
					default: {
					} // CityDebugUtils.infoEntities(cityRoot.document.entities, 0, true, true)	
				}
				tx.success
			} finally {
				tx.close
			}
			log.info("CityModification finished")
	}

	def private void setDistrictAttributes(Path districtPath) {
		var color = ""
		val district = districtPath.endNode
		district.setProperty("height", config.heightMin)
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.packageColorHex
		} else {
			color = PCKG_colors.get(districtPath.length - 1).asPercentage
		}
		district.setProperty("color", color)
	}
	
	def private setBuildingAttributes(Node building) {
		val entity = building.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val subElements = entity.getRelationships(Rels.DECLARES, Direction.OUTGOING).map[return endNode as Node]
		val methodCounter = subElements.filter [hasLabel(Labels.Method)].size
		val dataCounter = subElements.filter[!entity.hasLabel(Labels.Enum) && hasLabel(Labels.Field)].size
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
			color = NOS_colors.get(building.getProperty("numberOfStatements") as Integer).asPercentage
		} else if (config.outputFormat == OutputFormat::AFrame) {
			color = config.classColorHex
		} else {
			color = new RGBColor(config.classColor).asPercentage
		}
		building.setProperty("width", width)
		building.setProperty("length", length)
		building.setProperty("height", height)		
		building.setProperty("color", color)
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
		building.setProperty("height", height)
		building.setProperty("width", width)
		building.setProperty("length", length)
		building.setProperty("color", color)
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
		building.setProperty("height", height)
		building.setProperty("width", width)
		building.setProperty("length", length)
		building.setProperty("sideCapacity", sideCapacity)
		building.setProperty("color", color)
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
		building.setProperty("width", width)
		building.setProperty("length", length)
		building.setProperty("height", height)
		building.setProperty("color", color)
	}	
			
	def private void setBuildingSegmentAttributes(Node segment) {
		switch (config.buildingType) {
			case CITY_PANELS:
				setBuildingSegmentAttributesPanels(segment)
			case CITY_BRICKS:
				setBuildingSegmentAttributesBricks(segment)
			default: {
			}
		}
	}

	def private setBuildingSegmentAttributesPanels(Node segment) {
		val relatedEntity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
		val parent = segment.getSingleRelationship(Rels.CONTAINS, Direction.INCOMING).startNode
		val childs = parent.getRelationships(Direction.OUTGOING, Rels.CONTAINS).map[return endNode]
		childs.filter [
			val entity = getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
			entity.hasLabel(Labels.Field) &&
				!entity.getSingleRelationship(Rels.DECLARES, Direction.INCOMING).startNode.hasLabel(Labels.Enum)
		].size

		var int areaUnit = 1
		if (config.classElementsMode == ClassElementsModes::ATTRIBUTES_ONLY) {
			areaUnit = childs.filter [
				val entity = getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
				entity.hasLabel(Labels.Method)
			].size
		} else {
			areaUnit = childs.filter [
				val entity = getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
				entity.hasLabel(Labels.Field) &&
					!entity.getSingleRelationship(Rels.DECLARES, Direction.INCOMING).startNode.hasLabel(Labels.Enum)
			].size
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
		var effectiveLineCount = 0
		if(relatedEntity.hasProperty("effectiveLineCount")) {
			effectiveLineCount = (relatedEntity.getProperty("effectiveLineCount") as Long).intValue
		}
		while (index < config.panelHeightTresholdNos.size &&
			effectiveLineCount >= config.panelHeightTresholdNos.get(index)) {
			index = index + 1
		}
		segment.setProperty("width", width)
		segment.setProperty("length", length)		
		segment.setProperty("height", config.panelHeightUnit * (index + 1))
		CityUtils.setBuildingSegmentColor(segment);
	}	

	def private setBuildingSegmentAttributesBricks(Node segment) {
		segment.setProperty("width", config.brickSize)
		segment.setProperty("height", config.brickSize)
		segment.setProperty("length", config.brickSize)
		CityUtils.setBuildingSegmentColor(segment);
	}

	def private void setBuildingSegmentPositions(Node building) {
		// Sorting elements
		var List<Node> classElements = new ArrayList
		switch (config.classElementsMode) {
			case ATTRIBUTES_ONLY:
				classElements += CityUtils.getData(building)
			case METHODS_AND_ATTRIBUTES: {
				classElements += CityUtils.getData(building)
				classElements += CityUtils.getMethods(building)
			}
			default:
				classElements += CityUtils.getMethods(building)
		}
		CityUtils.sortBuildingSegments(classElements)

		// upper bound of the panel below the actual panel inside the loop
		val position = building.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		var lowerBsPosY = position.getProperty("y") as Double + building.getProperty("height") as Double / 2 + config.panelVerticalMargin

		// Correcting the initial gap on top of building depending on SeparatorMode
		if (config.panelSeparatorMode == SeparatorModes::GAP || config.panelSeparatorMode == SeparatorModes::SEPARATOR)
			lowerBsPosY = lowerBsPosY - config.panelVerticalGap
		// System.out.println("")
		// Looping through methods of building
		for (var i = 0; i < classElements.size(); i++) {
			val segment = classElements.get(i)
			val height = segment.getProperty("height") as Double
			val width = segment.getProperty("width") as Double
			// System.out.println(bs.getType() + " " + bs.getValue() + " " + bs.getModifiers() + " " + bs.getNumberOfStatements());
//			val bsPos = cityFactory.createPosition
			val pos = graph.createNode(Labels.City, Labels.Position)
			val x = position.getProperty("x") as Double
			var double y
			val z = position.getProperty("z") as Double
			pos.setProperty("x", x)
			pos.setProperty("z", z)
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
						val sepPos = graph.createNode(Labels.City, Labels.Position)
						val sepY = y + height / 2 + config.panelSeparatorHeight / 2
						sepPos.setProperty("x", x)
						sepPos.setProperty("y", sepY)
						sepPos.setProperty("z", z)

						// Deciding which shape the separator has to have
						val nextElementType = classElements.get(i + 1).getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
						val segmentType = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
						val panelSeparator = graph.createNode(Labels.City, Labels.PanelSeparator)
						panelSeparator.createRelationshipTo(sepPos, Rels.HAS)
						segment.createRelationshipTo(panelSeparator, Rels.HAS)
						if ((segmentType.hasLabel(Labels.Method) && nextElementType.hasLabel(Labels.Method)) ||
							!config.showAttributesAsCylinders) {
							panelSeparator.addLabel(Labels.Box)
							panelSeparator.setProperty("width", width)
							panelSeparator.setProperty("length", segment.getProperty("length"))
						} else {
							panelSeparator.addLabel(Labels.Cylinder)
							panelSeparator.setProperty("radius", width / 2)
						}

						lowerBsPosY = x + config.panelSeparatorHeight / 2
					}
				}
			}
			pos.setProperty("y", y);
			segment.createRelationshipTo(pos, Rels.HAS)
		}
	}
	
	def calculateSegments(Node building) {
		building.calculateFloors
		building.calculateChimneys
	}

	def void calculateFloors(Node building) {
		val position = building.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val bHeight = building.getProperty("height") as Double
		val bWidth = building.getProperty("width") as Double
		val bLength = building.getProperty("length") as Double
		val bPosX = position.getProperty("x") as Double
		val bPosY = position.getProperty("y") as Double
		val bPosZ = position.getProperty("z") as Double
		val floors = building.getRelationships(Rels.CONTAINS, Direction.OUTGOING).map[return endNode].filter[(hasLabel(Labels.Floor))]
		val floorNumber = floors.length
		var floorCounter = 0
		for (floor : floors) {
			floorCounter++
			floor.setProperty("height", bHeight / ( floorNumber + 2 ) * 0.80)
			floor.setProperty("width", bWidth * 1.1)
			floor.setProperty("length", bLength * 1.1)
			var color = 20 / 255.0 + " " + 133 / 255.0 + " " + 204 / 255.0
			if (config.outputFormat == OutputFormat::AFrame) {
				color = "#1485CC"
			}
			floor.setProperty("color", color)
			val floorPosition = graph.createNode(Labels.City, Labels.Position)
			floorPosition.setProperty("x", bPosX)
			floorPosition.setProperty("y", (bPosY - ( bHeight / 2) ) + bHeight / ( floorNumber + 2 ) * floorCounter)
			floorPosition.setProperty("z", bPosZ)
			floor.createRelationshipTo(floorPosition, Rels.HAS)
		}
	}	
		
	def void calculateChimneys(Node building) {
		val position = building.getSingleRelationship(Rels.HAS, Direction.OUTGOING).endNode
		val bHeight = building.getProperty("height") as Double
		val bWidth = building.getProperty("width") as Double
		val bPosX = position.getProperty("x") as Double
		val bPosY = position.getProperty("y") as Double
		val bPosZ = position.getProperty("z") as Double
		val chimneys = building.getRelationships(Rels.CONTAINS, Direction.OUTGOING).map[return endNode].filter[(hasLabel(Labels.Chimney))]
				
		// val chimneyNumber = chimneys.length
		var courner1 = newArrayList()
		var courner2 = newArrayList()
		var courner3 = newArrayList()
		var courner4 = newArrayList()

		var chimneyCounter = 0
		for (chimney : chimneys) {
			chimney.setProperty("height", 1.0)
			chimney.setProperty("width", 0.5)
			chimney.setProperty("length", 0.5)
			var color = 255 / 255.0 + " " + 252 / 255.0 + " " + 25 / 255.0
			if (config.outputFormat == OutputFormat::AFrame) {
				color = "#FFFC19"
			}
			chimney.setProperty("color", color)
			val chimneyPosition = graph.createNode(Labels.City, Labels.Position)
			chimney.createRelationshipTo(chimneyPosition, Rels.HAS)

			if (chimneyCounter % 4 == 0) {
				courner1.add(chimneyPosition)
			}
			if (chimneyCounter % 4 == 1) {
				courner2.add(chimneyPosition)
			}
			if (chimneyCounter % 4 == 2) {
				courner3.add(chimneyPosition)
			}
			if (chimneyCounter % 4 == 3) {
				courner4.add(chimneyPosition)
			}
			chimneyCounter++
		}
		chimneyCounter = 0
		for (chimneyPosition : courner1) {
			chimneyPosition.setProperty("x", (bPosX - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter))
			chimneyPosition.setProperty("y", (bPosY + ( bHeight / 2) ) + 0.5)
			chimneyPosition.setProperty("z", (bPosZ - ( bWidth / 2) ) + 0.5)
			chimneyCounter++
		}
		chimneyCounter = 0
		for (chimneyPosition : courner2) {
			chimneyPosition.setProperty("x", (bPosX + ( bWidth / 2) ) - 0.5)
			chimneyPosition.setProperty("y", (bPosY + ( bHeight / 2) ) + 0.5)
			chimneyPosition.setProperty("z", (bPosZ - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter))
			chimneyCounter++
		}
		chimneyCounter = 0
		for (chimneyPosition : courner3) {
			chimneyPosition.setProperty("x", (bPosX + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter))
			chimneyPosition.setProperty("y", (bPosY + ( bHeight / 2) ) + 0.5)
			chimneyPosition.setProperty("z", (bPosZ + ( bWidth / 2) ) - 0.5)			
			chimneyCounter++
		}
		chimneyCounter = 0
		for (chimneyPosition : courner4) {
			chimneyPosition.setProperty("x", (bPosX - ( bWidth / 2) ) + 0.5)
			chimneyPosition.setProperty("y", (bPosY + ( bHeight / 2) ) + 0.5)
			chimneyPosition.setProperty("z", (bPosZ + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter))				
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
}