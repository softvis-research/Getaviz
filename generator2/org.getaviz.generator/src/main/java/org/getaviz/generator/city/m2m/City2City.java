package org.getaviz.generator.city.m2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.city.CityUtils;
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes;
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.getaviz.generator.SettingsConfiguration.Panels.SeparatorModes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;

public class City2City {
	SettingsConfiguration config = SettingsConfiguration.getInstance();
	Log log = LogFactory.getLog(this.getClass());
	List<RGBColor> PCKG_colors;
	List<RGBColor> NOS_colors;
	HashMap<Long, double[]> properties = new HashMap<Long, double[]>();
	Node model;
	DatabaseConnector connector = DatabaseConnector.getInstance();

	public City2City() {
		log.info("City2City started");
		model = connector.executeRead("MATCH (n:Model {building_type: \'" + config.getBuildingTypeAsString() +
			"\'}) RETURN n").next().get("n").asNode();
		if (config.getBuildingType() == BuildingType.CITY_BRICKS || config.getBuildingType() == BuildingType.CITY_PANELS) {
			connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(m:BuildingSegment) RETURN m").forEachRemaining((result) -> {
				setBuildingSegmentAttributes(result.get("m").asNode().id());
			});
		}
		int packageMaxLevel = connector.executeRead("MATCH p=(n:District)-[:CONTAINS*]->(m:District) WHERE NOT (m)-[:CONTAINS]->(:District) RETURN length(p) AS length ORDER BY length(p) DESC LIMIT 1").
			single().get("length").asInt() + 1;
		PCKG_colors = createColorGradiant(new RGBColor(config.getPackageColorStart()), new RGBColor(config.getPackageColorEnd()),
			packageMaxLevel);

		if (config.getOriginalBuildingMetric() == BuildingMetric.NOS) {
			int NOS_max = connector.executeRead("MATCH (n:Building) RETURN max(n.numberOfStatements) AS nos").single().
				get("nos").asInt();
			NOS_colors = createColorGradiant(new RGBColor(config.getClassColorStart()), new RGBColor(config.getClassColorEnd()),
				NOS_max + 1);
		}

		connector.executeRead("MATCH p=(n:Model:City)-[:CONTAINS*]->(m:District) RETURN p").forEachRemaining((result) -> {
			setDistrictAttributes(result.get("p").asPath());
		});
		connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEachRemaining((result) -> {
			setBuildingAttributes(result.get("b").asNode());
		});
		connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(d:District)-[:VISUALIZES]->(element)  RETURN d, element.hash as hash ORDER BY element.hash").forEachRemaining((result) -> {
			Node node = result.get("d").asNode();
			double width = node.get("width").asDouble(0.0);
			double length = node.get("length").asDouble(0.0);
			double[] array = {width, length};
			properties.put(node.id(), array);
		});
		connector.executeRead(
			"MATCH (n:Model:City)-[:CONTAINS*]->(b:Building)-[:VISUALIZES]->(element) " +
				"RETURN b, element.hash as hash " + "ORDER BY element.hash"
		).forEachRemaining((result) -> {
			Node node = result.get("b").asNode();
			double width = node.get("width").asDouble(0.0);
			double length = node.get("length").asDouble(0.0);
			double[] array = {width, length};
			properties.put(node.id(), array);
		});

		CityLayout.cityLayout(model.id(), properties);

		switch (config.getBuildingType()) {
			case CITY_BRICKS:
				BrickLayout.brickLayout(model.id()); break; // Layout for buildingSegments
			case CITY_PANELS:
				connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEachRemaining((result) -> {
					setBuildingSegmentPositions(result.get("b").asNode());
				});
				break;
			case CITY_FLOOR:
				connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEachRemaining((result) -> {
					calculateSegments(result.get("b").asNode());
				});
				break;
			default: {
			} // CityDebugUtils.infoEntities(cityRoot.document.entities, 0, true, true)	
		}
		log.info("City2City finished");
	}

	private void setDistrictAttributes(Path districtPath) {
		String color = "";
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getPackageColorHex();
		} else {
			color = PCKG_colors.get(districtPath.length() - 1).asPercentage();
		}
		connector.executeWrite(
			String.format("MATCH (n) WHERE ID(n) = %d SET n.height = %f, n.color = \'%s\'", districtPath.end().id(),
				config.getHeightMin(), color));
	}

	private void setBuildingAttributes(Node building) {
		int methodCounter = connector.executeRead(
			"MATCH (n)-[:VISUALIZES]->(o)-[:DECLARES]->(m:Method) WHERE ID(n) = " + building.id() +
				" RETURN COUNT(m) AS mCount").single().get("mCount").asInt();
		int dataCounter = connector.executeRead(
			"MATCH (n)-[:VISUALIZES]->(o)-[:DECLARES]->(f:Field) WHERE ID(n) = " + building.id() +
				" AND NOT o:Enum RETURN COUNT(f) AS fCount").single().get("fCount").asInt();
		switch (config.getBuildingType()) {
			case CITY_ORIGINAL: setBuildingAttributesOriginal(building, methodCounter, dataCounter); break;
			case CITY_PANELS: setBuildingAttributesPanels(building, methodCounter, dataCounter); break;
			case CITY_BRICKS: setBuildingAttributesBricks(building, methodCounter, dataCounter); break;
			case CITY_FLOOR: setBuildingAttributesFloors(building, methodCounter, dataCounter); break;
		}
	}

	private void setBuildingAttributesOriginal(Node building, int methodCounter, int dataCounter) {
		double width = 0.0;
		double length = 0.0;
		double height = 0.0;
		String color = "";
		if (dataCounter == 0) {
			width = config.getWidthMin();
			length = config.getWidthMin();
		} else {
			width = dataCounter;
			length = dataCounter;
		}
		if (methodCounter == 0) {
			height = config.getHeightMin();
		} else {
			height = methodCounter;
		}
		if (config.getOriginalBuildingMetric() == BuildingMetric.NOS) {
			color = NOS_colors.get(building.get("numberOfStatements").asInt(0)).asPercentage();
		} else if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getClassColorHex();
		} else {
			color = new RGBColor(config.getClassColor()).asPercentage();
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id(), width, length, height, color));
	}

	private void setBuildingAttributesPanels(Node building, int methodCounter, int dataCounter) {
		double height = 0.0;
		double width = 0.0;
		double length = 0.0;
		String color = "";
		if (config.isShowBuildingBase()) {
			height = config.getHeightMin();
		} else {
			height = 0;
		}
		int areaUnit = 1;
		if (config.getClassElementsMode() == ClassElementsModes.ATTRIBUTES_ONLY) {
			areaUnit = methodCounter;
		} else {
			areaUnit = dataCounter;
		}
		if (areaUnit <= 1) {
			width = config.getWidthMin() + config.getPanelHorizontalMargin() * 2;
			length = config.getWidthMin() + config.getPanelHorizontalMargin() * 2;
		} else {
			width = config.getWidthMin() * areaUnit + config.getPanelHorizontalMargin() * 2;
			length = config.getWidthMin() * areaUnit + config.getPanelHorizontalMargin() * 2;
		} 
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getClassColorHex();
		} else {
			color = new RGBColor(config.getClassColor()).asPercentage();
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id(), width, length, height, color));
	}

	private void setBuildingAttributesBricks(Node building, int methodCounter, int dataCounter) {
		double height = 0.0;
		double width = 0.0;
		double length = 0.0;
		int sideCapacity = 0;
		String color = "";
		if (config.isShowBuildingBase()) {
			height = config.getHeightMin();
		} else {
			height = 0;
		}
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getClassColorHex();
		} else {
			color = new RGBColor(config.getClassColor()).asPercentage();
		}
		// Setting width, height & sideCapacity
		switch (config.getBrickLayout()) {
			case STRAIGHT: {
				sideCapacity = 1;
				break;
			}
			case BALANCED: {
				switch (config.getClassElementsMode()) {
					case ATTRIBUTES_ONLY: sideCapacity = calculateSideCapacity(methodCounter); break;
					case METHODS_AND_ATTRIBUTES: sideCapacity = calculateSideCapacity(dataCounter + methodCounter); break;
					default: sideCapacity = calculateSideCapacity(dataCounter);
				}
			}
			case PROGRESSIVE: {
				switch (config.getClassElementsMode()) {
					case METHODS_ONLY: sideCapacity = calculateSideCapacity(methodCounter); break;
					case METHODS_AND_ATTRIBUTES: sideCapacity = calculateSideCapacity(dataCounter + methodCounter); break;
					default: sideCapacity = calculateSideCapacity(dataCounter);
				}
			}
			default: {
				sideCapacity = 1;
			}
		}
		width = config.getBrickSize() * sideCapacity + config.getBrickHorizontalMargin() * 2 +
			config.getBrickHorizontalGap() * (sideCapacity - 1);
		length = config.getBrickSize() * sideCapacity + config.getBrickHorizontalMargin() * 2 +
			config.getBrickHorizontalGap() * (sideCapacity - 1);
		connector.executeWrite(
			cypherSetBuildingSegmentAttributes(building.id(), width, length, height, color) + ", n.sideCapacity = " +
				sideCapacity);
	}

	private void setBuildingAttributesFloors(Node building, int methodCounter, int dataCounter) {
		double width = 0.0;
		double length = 0.0;
		double height = 0.0;
		String color = "";
		if (dataCounter < 2) { // pko 2016
			width = 2; // TODO in settings datei aufnehmen
			length = 2;
		} else {
			width = Math.ceil(dataCounter / 4.0) + 1; // pko 2016
			length = Math.ceil(dataCounter / 4.0) + 1; // pko 2016
		}
		if (methodCounter == 0) {
			height = config.getHeightMin();
		} else {
			height = methodCounter;
		}
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getClassColorHex();
		} else {
			color = 53 / 255.0 + " " + 53 / 255.0 + " " + 89 / 255.0; // pko 2016
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id(), width, length, height, color));
	}

	private void setBuildingSegmentAttributes(Long segment) {
		switch (config.getBuildingType()) {
			case CITY_PANELS:
				setBuildingSegmentAttributesPanels(segment); break;
			case CITY_BRICKS:
				setBuildingSegmentAttributesBricks(segment); break;
			default: {
			}
		}
	}

	private void setBuildingSegmentAttributesPanels(Long segment) {
		Path path = connector.executeRead(
			"MATCH p = (parent)-[:CONTAINS]->(s)-[:VISUALIZES]->(e) WHERE ID(s) = " + segment + " RETURN p").next().get(
			"p").asPath();
		Node relatedEntity = path.end();
		long parent = path.start().id();
		List<Node> childsM = connector.executeRead(
			"MATCH (s)-[:CONTAINS]->(child)-[r:VISUALIZES]->(e:Method) WHERE ID(s) = " + parent +
				" AND (e:Field OR e:Method)" + " AND NOT (e)<-[:DECLARES]-(:Enum) RETURN e").stream().map(s -> s.get("e").asNode()).collect(Collectors.toList());
		List<Node> childsA = connector.executeRead(
				"MATCH (s)-[:CONTAINS]->(child)-[r:VISUALIZES]->(e:Field) WHERE ID(s) = " + parent +
					" AND NOT (e)<-[:DECLARES]-(:Enum) RETURN e").stream().map(s -> s.get("e").asNode()).collect(Collectors.toList());
		int areaUnit = 1;
		if (config.getClassElementsMode() == ClassElementsModes.ATTRIBUTES_ONLY) {
			areaUnit = childsM.size();
		} else {
			areaUnit = childsA.size();
		}
		double width = 0.0;
		double length = 0.0;
		if (areaUnit <= 1) {
			width = config.getWidthMin();
			length = config.getWidthMin();
		} else {
			width = config.getWidthMin() * areaUnit;
			length = config.getWidthMin() * areaUnit;
		}
		int index = 0;
		int effectiveLineCount = relatedEntity.get("effectiveLineCount").asInt(0);
		while (index < config.getPanelHeightTresholdNos().length &&
			effectiveLineCount >= config.getPanelHeightTresholdNos()[index]) {
			index = index + 1;
		}
		double height = config.getPanelHeightUnit() * (index + 1);
		connector.executeWrite(
			cypherSetBuildingSegmentAttributes(segment, width, length, height,
				CityUtils.setBuildingSegmentColor(relatedEntity)));
	}

	private void setBuildingSegmentAttributesBricks(Long segment) {
		Path path = connector.executeRead(
			"MATCH p = (parent)-[:CONTAINS]->(s)-[:VISUALIZES]->(e) WHERE ID(s) = " + segment + " RETURN p").next().get(
			"p").asPath();
		Node relatedEntity = path.end();
		connector.executeWrite(
			cypherSetBuildingSegmentAttributes(segment, config.getBrickSize(), config.getBrickSize(), config.getBrickSize(),
				CityUtils.setBuildingSegmentColor(relatedEntity)));
		CityUtils.setBuildingSegmentColor(relatedEntity);
	}

	private void setBuildingSegmentPositions(Node building) {
		// Sorting elements
		List<Node> classElements = new ArrayList<>();
		switch (config.getClassElementsMode()) {
			case ATTRIBUTES_ONLY:
				classElements.addAll(CityUtils.getData(building.id()));
			case METHODS_AND_ATTRIBUTES: {
				classElements.addAll(CityUtils.getData(building.id()));
				classElements.addAll(CityUtils.getMethods(building.id()));
			}
			default:
				classElements.addAll(CityUtils.getMethods(building.id()));
		}
		CityUtils.sortBuildingSegments(classElements);
		// upper bound of the panel below the actual panel inside the loop
		Node position = connector.getPosition(building.id());
		double lowerBsPosY = position.get("y").asDouble() + building.get("height").asDouble() / 2 + config.getPanelVerticalMargin();

		// Correcting the initial gap on top of building depending on SeparatorMode
		if (config.getPanelSeparatorMode() == SeparatorModes.GAP ||
			config.getPanelSeparatorMode() == SeparatorModes.SEPARATOR) {
			lowerBsPosY = lowerBsPosY - config.getPanelVerticalGap();
		}
		// System.out.println("")
		// Looping through methods of building
		for (int i = 0; i < classElements.size(); i++) {
			Node segment = classElements.get(i);
			double height = segment.get("height").asDouble();
			double width = segment.get("width").asDouble();
			// System.out.println(bs.getType() + " " + bs.getValue() + " " + bs.getModifiers() + " " + bs.getNumberOfStatements());
//			val bsPos = cityFactory.createPosition
			double x = position.get("x").asDouble();
			double y = 0;
			double z = position.get("z").asDouble();
			String panelSeparatorCypher = "";
			switch (config.getPanelSeparatorMode()) {
				case NONE: { // place segments on top of each other
					y = lowerBsPosY + height / 2;
					lowerBsPosY = y + height / 2;
				}
				case GAP: { // Leave a free space between segments
					y = lowerBsPosY + config.getPanelVerticalGap() + height / 2;
					lowerBsPosY = y + height / 2;
				}
				case SEPARATOR: { // Placing additional separators
					y = lowerBsPosY + height / 2;
					// Placing a separator on top of the current method if it is not last method
					if (i < classElements.size() - 1) {
						double sepY = y + height / 2 + config.getPanelSeparatorHeight() / 2;
						// Deciding which shape the separator has to have
						Node nextElementType = connector.getVisualizedEntity(classElements.get(i + 1).id());
						Node segmentType = connector.getVisualizedEntity(segment.id());
						panelSeparatorCypher = String.format(
							"(psp:City:Position {x: %f, y: %f, z: %f})<-[:HAS]-(ps:City:PanelSeparator", x, sepY, z);
						if ((segmentType.hasLabel(Labels.Method.name()) &&
							nextElementType.hasLabel(Labels.Method.name())) || !config.isShowAttributesAsCylinders()) {
							panelSeparatorCypher +=
								String.format(":Box {width: %f, length: %f})<-[:HAS]-", width,
									segment.get("length").asDouble());
						} else {
							panelSeparatorCypher += String.format(":Cylinder {radius: %f})<-[:HAS]-", width / 2);
						}
						lowerBsPosY = sepY + config.getPanelSeparatorHeight() / 2;
					}
				}
			}
			String s = String.format(
				"MATCH(n) WHERE ID(n) = %d CREATE %s(n)-[:HAS]->(p:Position:City {x: %f, y: %f, z: %f})", segment.id(),
				panelSeparatorCypher, x, y, z);
			connector.executeWrite(s);
		}
	}

	private void calculateSegments(Node building) {
		calculateFloors(building);
		calculateChimneys(building);
	}

	private void calculateFloors(Node building) {
		Node position = connector.getPosition(building.id());
		double bHeight = building.get("height").asDouble();
		double bWidth = building.get("width").asDouble();
		double bLength = building.get("length").asDouble();
		double bPosX = position.get("x").asDouble();
		double bPosY = position.get("y").asDouble();
		double bPosZ = position.get("z").asDouble();
		StatementResult floors = connector.executeRead("MATCH (n)-[:CONTAINS]->(f:Floor) WHERE ID(n) = " + building.id() +
			" RETURN f");
		int floorNumberValue = connector.executeRead("MATCH (n)-[:CONTAINS]->(f:Floor) WHERE ID(n) = " + building.id() +
			" RETURN COUNT(f) as floorNumber").single().get("floorNumber").asInt();

		int floorCounter = 0;
		while (floors.hasNext()) {
			Record record = floors.next();
			long floor = record.get("f").asNode().id();
			floorCounter++;
			String color = 20 / 255.0 + " " + 133 / 255.0 + " " + 204 / 255.0;
			if (config.getOutputFormat() == OutputFormat.AFrame) {
				color = "#1485CC";
			}
			String statement = cypherSetBuildingSegmentAttributes(floor, bWidth * 1.1, bLength * 1.1,
				bHeight / (floorNumberValue + 2 ) * 0.80, color);
			statement +=
				String.format("CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})", bPosX,
					(bPosY - ( bHeight / 2) ) + bHeight / ( floorNumberValue + 2 ) * floorCounter, bPosZ);
			connector.executeWrite(statement);
		}
	}

	private void calculateChimneys(Node building) {
		Node position = connector.getPosition(building.id());
		double bHeight = building.get("height").asDouble();
		double bWidth = building.get("width").asDouble();
		double bPosX = position.get("x").asDouble();
		double bPosY = position.get("y").asDouble();
		double bPosZ = position.get("z").asDouble();

		// val chimneyNumber = chimneys.length
		List<Long> courner1 = new ArrayList<>();
		List<Long> courner2 = new ArrayList<>();
		List<Long> courner3 = new ArrayList<>();
		List<Long> courner4 = new ArrayList<>();

		int chimneyCounter = 0;
		StatementResult chimneys = connector.executeRead("MATCH (n)-[:CONTAINS]->(c:Chimney) WHERE ID(n) = " + building.id() +
			" RETURN c");
		while (chimneys.hasNext()) {
			long chimney = chimneys.next().get("c").asNode().id();
			String color = 255 / 255.0 + " " + 252 / 255.0 + " " + 25 / 255.0;
			if (config.getOutputFormat() == OutputFormat.AFrame) {
				color = "#FFFC19";
			}
			connector.executeWrite(cypherSetBuildingSegmentAttributes(chimney, 0.5, 0.5, 1.0, color));
			if (chimneyCounter % 4 == 0) {
				courner1.add(chimney);
			}
			if (chimneyCounter % 4 == 1) {
				courner2.add(chimney);
			}
			if (chimneyCounter % 4 == 2) {
				courner3.add(chimney);
			}
			if (chimneyCounter % 4 == 3) {
				courner4.add(chimney);
			}
			chimneyCounter++;

		}
		chimneyCounter = 0;
		for (Long chimney : courner1) {
			double x = (bPosX - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter);
			double y = (bPosY + ( bHeight / 2) ) + 0.5;
			double z = (bPosZ - ( bWidth / 2) ) + 0.5;
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z));
			chimneyCounter++;
		}
		chimneyCounter = 0;
		for (Long chimney : courner2) {
			double x = (bPosX + ( bWidth / 2) ) - 0.5;
			double y = (bPosY + ( bHeight / 2) ) + 0.5;
			double z = (bPosZ - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter);
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z));
			chimneyCounter++;
		}
		chimneyCounter = 0;
		for (Long chimney : courner3) {
			double x = (bPosX + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter);
			double y = (bPosY + ( bHeight / 2) ) + 0.5;
			double z = (bPosZ + ( bWidth / 2) ) - 0.5;
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z));
			chimneyCounter++;
		}
		chimneyCounter = 0;
		for (Long chimney : courner4) {
			double x = (bPosX - ( bWidth / 2) ) + 0.5;
			double y = (bPosY + ( bHeight / 2) ) + 0.5;
			double z = (bPosZ + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter);
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z));
			chimneyCounter++;
		}
	}

	private List<RGBColor> createColorGradiant(RGBColor start, RGBColor end, int maxLevel) {
		int steps = maxLevel - 1;
		if (maxLevel == 1) {
			steps++;
		}
		double r_step = (end.r() - start.r()) / steps;
		double g_step = (end.g() - start.g()) / steps;
		double b_step = (end.b() - start.b()) / steps;

		List<RGBColor> colorRange = new ArrayList<>();
		for (int i = 0; i < maxLevel; i++) {
			double newR = start.r() + i * r_step;
			double newG = start.g() + i * g_step;
			double newB = start.b() + i * b_step;
			colorRange.add(new RGBColor(newR, newG, newB));
		}
		return colorRange;
	}

	// Calculates side capacity for progressive/balanced bricks layout
	private int calculateSideCapacity(double value) {
		int sc = 0; // side capacity
		int lc = 0; // layer capacity
		int nolMin = 0; // number of layers
		int bcMin = 0; // building capacity min
		int bcMax = 0; // building capacity max
		do {
			sc++;
			lc = sc * 4;
			nolMin = sc * 2;
			bcMin = lc * nolMin;
			bcMax = bcMin - 1;
		} while (bcMax < value);

		return sc;
	}

	private String cypherSetBuildingSegmentAttributes(Long segment, double width, double length, double height,
		String color) {
		return String.format(
			"MATCH (n) WHERE ID(n) = %d SET n.width = %f, n.length = %f, n.height = %f, n.color = \'%s\'", segment,
			width, length, height, color);
	}

}
