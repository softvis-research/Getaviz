package org.getaviz.generator.garbage.city.m2m;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.garbage.ColorGradient;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes;
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric;
import org.getaviz.generator.SettingsConfiguration.Panels.SeparatorModes;
import org.getaviz.generator.garbage.Step;
import org.getaviz.generator.garbage.city.CityUtils;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.garbage.Labels;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class City2City implements Step {
	private Log log = LogFactory.getLog(this.getClass());
	private List<String> PCKG_colors;
	private List<String> NOS_colors;
	private HashMap<Long, double[]> properties = new HashMap<>();
	private DatabaseConnector connector = DatabaseConnector.getInstance();

	private String packageColorStart;
	private String packageColorEnd;

	private BuildingType buildingType;
	private String buildingTypeAsString;
	private BuildingMetric originalBuildingMetric;
	private String classColorStart;
	private String classColorEnd;
	private String classColor;
	private String floorColor;
	private String chimneyColor;
	private boolean showBuildingBase;

	private double heightMin;
	private double widthMin;

	private ClassElementsModes classElementsMode;
	private double panelHorizontalMargin;
	private int[] panelHeightTresholdNos;
	private double panelHeightUnit;
	private double panelVerticalMargin;
	private SeparatorModes panelSeparatorMode;
	private double panelVerticalGap;
	private double panelSeparatorHeight;

	private SettingsConfiguration.Bricks.Layout brickLayout;
	private double brickSize;
	private double brickHorizontalMargin;
	private double brickHorizontalGap;

	private boolean showAttributesAsCylinders;

	public City2City(SettingsConfiguration config) {
		this.buildingType = config.getBuildingType();
		this.buildingTypeAsString = config.getBuildingTypeAsString();
		this.packageColorStart = config.getPackageColorStart();
		this.packageColorEnd = config.getPackageColorEnd();
		this.originalBuildingMetric = config.getOriginalBuildingMetric();
		this.classColorStart = config.getClassColorStart();
		this.classColorEnd = config.getClassColorEnd();
		this.floorColor = config.getCityFloorColor();
		this.chimneyColor = config.getCityChimneyColor();
		this.heightMin = config.getHeightMin();
		this.widthMin = config.getWidthMin();
		this.classColor = config.getClassColor();
		this.showBuildingBase = config.isShowBuildingBase();
		this.classElementsMode = config.getClassElementsMode();
		this.panelHorizontalMargin = config.getPanelHorizontalMargin();
		this.brickLayout = config.getBrickLayout();
		this.brickSize = config.getBrickSize();
		this.brickHorizontalMargin = config.getBrickHorizontalMargin();
		this.brickHorizontalGap = config.getBrickHorizontalGap();
		this.panelHeightTresholdNos = config.getPanelHeightTresholdNos();
		this.panelHeightUnit = config.getPanelHeightUnit();
		this.panelVerticalMargin = config.getPanelVerticalMargin();
		this.panelSeparatorMode = config.getPanelSeparatorMode();
		this.panelVerticalGap = config.getPanelVerticalGap();
		this.panelSeparatorHeight = config.getPanelSeparatorHeight();
		this.showAttributesAsCylinders = config.isShowAttributesAsCylinders();
	}

	@Override
	public boolean checkRequirements() {
		return true;
	}

	public void run() {
		log.info("City2City started");
		Node model = connector.executeRead("MATCH (n:Model {building_type: '" + buildingTypeAsString +
				"\'}) RETURN n").next().get("n").asNode();
		if (buildingType == BuildingType.CITY_BRICKS || buildingType == BuildingType.CITY_PANELS) {
			connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(m:BuildingSegment) RETURN m").forEachRemaining((result) -> setBuildingSegmentAttributes(result.get("m").asNode().id()));
		}
		int packageMaxLevel = connector.executeRead("MATCH p=(n:District)-[:CONTAINS*]->(m:District) WHERE NOT (m)-[:CONTAINS]->(:District) RETURN length(p) AS length ORDER BY length(p) DESC LIMIT 1").
				single().get("length").asInt() + 1;
		PCKG_colors = ColorGradient.createColorGradient(packageColorStart, packageColorEnd, packageMaxLevel);

		if (originalBuildingMetric == BuildingMetric.NOS) {
			int NOS_max = connector.executeRead("MATCH (n:Building) RETURN max(n.numberOfStatements) AS nos").single().
					get("nos").asInt();
			NOS_colors = ColorGradient.createColorGradient(classColorStart, classColorEnd, NOS_max + 1);
		}

		connector.executeRead("MATCH p=(n:Model:City)-[:CONTAINS*]->(m:District) RETURN p").forEachRemaining((result) -> setDistrictAttributes(result.get("p").asPath()));
		connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEachRemaining((result) -> setBuildingAttributes(result.get("b").asNode()));
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

		switch (buildingType) {
			case CITY_BRICKS:
				BrickLayout.brickLayout(model.id()); break; // Layout for buildingSegments
			case CITY_PANELS:
				connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEachRemaining((result) -> setBuildingSegmentPositions(result.get("b").asNode()));
				break;
			case CITY_FLOOR:
				connector.executeRead("MATCH (n:Model:City)-[:CONTAINS*]->(b:Building) RETURN b").forEachRemaining((result) -> calculateSegments(result.get("b").asNode()));
				break;
			default: {
			} // CityDebugUtils.infoEntities(cityRoot.document.entities, 0, true, true)
		}
		log.info("City2City finished");
	}

	private void setDistrictAttributes(Path districtPath) {
		String color = PCKG_colors.get(districtPath.length() - 1);
		connector.executeWrite(
			String.format("MATCH (n) WHERE ID(n) = %d SET n.height = %f, n.color = '%s'", districtPath.end().id(),
				heightMin, color));
	}

	private void setBuildingAttributes(Node building) {
		int methodCounter = connector.executeRead(
			"MATCH (n)-[:VISUALIZES]->(o)-[:DECLARES]->(m:Method) WHERE ID(n) = " + building.id() +
				" RETURN COUNT(m) AS mCount").single().get("mCount").asInt();
		int dataCounter = connector.executeRead(
			"MATCH (n)-[:VISUALIZES]->(o)-[:DECLARES]->(f:Field) WHERE ID(n) = " + building.id() +
				" AND NOT o:Enum RETURN COUNT(f) AS fCount").single().get("fCount").asInt();
		switch (buildingType) {
			case CITY_ORIGINAL: setBuildingAttributesOriginal(building, methodCounter, dataCounter); break;
			case CITY_PANELS: setBuildingAttributesPanels(building, methodCounter, dataCounter); break;
			case CITY_BRICKS: setBuildingAttributesBricks(building, methodCounter, dataCounter); break;
			case CITY_FLOOR: setBuildingAttributesFloors(building, methodCounter, dataCounter); break;
		}
	}

	private void setBuildingAttributesOriginal(Node building, int methodCounter, int dataCounter) {
		double width;
		double length;
		double height;
		String color;
		if (dataCounter == 0) {
			width = widthMin;
			length = widthMin;
		} else {
			width = dataCounter;
			length = dataCounter;
		}
		if (methodCounter == 0) {
			height = heightMin;
		} else {
			height = methodCounter;
		}
		if (originalBuildingMetric == BuildingMetric.NOS) {
			color = NOS_colors.get(building.get("numberOfStatements").asInt(0));
		} else  {
			color = classColor;
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id(), width, length, height, color));
	}

	private void setBuildingAttributesPanels(Node building, int methodCounter, int dataCounter) {
		double height;
		double width;
		double length;
		if (showBuildingBase) {
			height = heightMin;
		} else {
			height = 0;
		}
		int areaUnit;
		if (classElementsMode == ClassElementsModes.ATTRIBUTES_ONLY) {
			areaUnit = methodCounter;
		} else {
			areaUnit = dataCounter;
		}
		if (areaUnit <= 1) {
			width = widthMin + panelHorizontalMargin * 2;
			length = widthMin + panelHorizontalMargin * 2;
		} else {
			width = widthMin * areaUnit + panelHorizontalMargin * 2;
			length = widthMin * areaUnit + panelHorizontalMargin * 2;
		} 

		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id(), width, length, height, classColor));
	}

	private void setBuildingAttributesBricks(Node building, int methodCounter, int dataCounter) {
		double height;
		double width;
		int sideCapacity;
		String color;
		if (showBuildingBase) {
			height = heightMin;
		} else {
			height = 0;
		}
		color = classColor;
		// Setting width, height & sideCapacity
		switch (brickLayout) {
			case STRAIGHT: {
				sideCapacity = 1;
				break;
			}
			case BALANCED: {
				switch (classElementsMode) {
					case ATTRIBUTES_ONLY: sideCapacity = calculateSideCapacity(methodCounter); break;
					case METHODS_AND_ATTRIBUTES: sideCapacity = calculateSideCapacity(dataCounter + methodCounter); break;
					default: sideCapacity = calculateSideCapacity(dataCounter);
				}
				break;
			}
			case PROGRESSIVE: {
				switch (classElementsMode) {
					case METHODS_ONLY: sideCapacity = calculateSideCapacity(methodCounter); break;
					case METHODS_AND_ATTRIBUTES: sideCapacity = calculateSideCapacity(dataCounter + methodCounter); break;
					default: sideCapacity = calculateSideCapacity(dataCounter);
				}
				break;
			}
			default: {
				sideCapacity = 1;
			}
		}
		width = brickSize * sideCapacity + brickHorizontalMargin * 2 +
			brickHorizontalGap * (sideCapacity - 1);
		double length = brickSize * sideCapacity + brickHorizontalMargin * 2 +
				brickHorizontalGap * (sideCapacity - 1);
		connector.executeWrite(
			cypherSetBuildingSegmentAttributes(building.id(), width, length, height, color) + ", n.sideCapacity = " +
				sideCapacity);
	}

	private void setBuildingAttributesFloors(Node building, int methodCounter, int dataCounter) {
		double width;
		double length;
		double height;
		if (dataCounter < 2) { // pko 2016
			width = 2;
			length = 2;
		} else {
			width = Math.ceil(dataCounter / 4.0) + 1; // pko 2016
			length = Math.ceil(dataCounter / 4.0) + 1; // pko 2016
		}
		if (methodCounter == 0) {
			height = heightMin;
		} else {
			height = methodCounter;
		}
		connector.executeWrite(cypherSetBuildingSegmentAttributes(building.id(), width, length, height, classColor));
	}

	private void setBuildingSegmentAttributes(Long segment) {
		switch (buildingType) {
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
		int areaUnit;
		if (classElementsMode == ClassElementsModes.ATTRIBUTES_ONLY) {
			areaUnit = childsM.size();
		} else {
			areaUnit = childsA.size();
		}
		double width;
		double length;
		if (areaUnit <= 1) {
			width = widthMin;
			length = widthMin;
		} else {
			width = widthMin * areaUnit;
			length = widthMin * areaUnit;
		}
		int index = 0;
		int effectiveLineCount = relatedEntity.get("effectiveLineCount").asInt(0);
		while (index < panelHeightTresholdNos.length &&
			effectiveLineCount >= panelHeightTresholdNos[index]) {
			index = index + 1;
		}
		double height = panelHeightUnit * (index + 1);
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
			cypherSetBuildingSegmentAttributes(segment, brickSize, brickSize, brickSize,
				CityUtils.setBuildingSegmentColor(relatedEntity)));
		CityUtils.setBuildingSegmentColor(relatedEntity);
	}

	private void setBuildingSegmentPositions(Node building) {
		// Sorting elements
		List<Node> classElements = new ArrayList<>();
		switch (classElementsMode) {
			case ATTRIBUTES_ONLY:
				classElements.addAll(CityUtils.getData(building.id()));
				break;
			case METHODS_AND_ATTRIBUTES: {
				classElements.addAll(CityUtils.getData(building.id()));
				classElements.addAll(CityUtils.getMethods(building.id()));
				break;
			}
			default:
				classElements.addAll(CityUtils.getMethods(building.id()));
				break;
		}
		CityUtils.sortBuildingSegments(classElements);
		// upper bound of the panel below the actual panel inside the loop
		Node position = connector.getPosition(building.id());
		double lowerBsPosY = position.get("y").asDouble() + building.get("height").asDouble() / 2 + panelVerticalMargin;

		// Correcting the initial gap on top of building depending on SeparatorMode
		if (panelSeparatorMode == SeparatorModes.GAP ||
			panelSeparatorMode == SeparatorModes.SEPARATOR) {
			lowerBsPosY = lowerBsPosY - panelVerticalGap;
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
			switch (panelSeparatorMode) {
				case NONE: { // place segments on top of each other
					y = lowerBsPosY + height / 2;
					lowerBsPosY = y + height / 2;
					break;
				}
				case GAP: { // Leave a free space between segments
					y = lowerBsPosY + panelVerticalGap + height / 2;
					lowerBsPosY = y + height / 2;
					break;
				}
				case SEPARATOR: { // Placing additional separators
					y = lowerBsPosY + height / 2;
					// Placing a separator on top of the current method if it is not last method
					if (i < classElements.size() - 1) {
						double sepY = y + height / 2 + panelSeparatorHeight / 2;
						// Deciding which shape the separator has to have
						Node nextElementType = connector.getVisualizedEntity(classElements.get(i + 1).id());
						Node segmentType = connector.getVisualizedEntity(segment.id());
						panelSeparatorCypher = String.format(
							"(psp:City:Position {x: %f, y: %f, z: %f})<-[:HAS]-(ps:City:PanelSeparator", x, sepY, z);
						if ((segmentType.hasLabel(Labels.Method.name()) &&
							nextElementType.hasLabel(Labels.Method.name())) || !showAttributesAsCylinders) {
							panelSeparatorCypher +=
								String.format(":Box {width: %f, length: %f})<-[:HAS]-", width,
									segment.get("length").asDouble());
						} else {
							panelSeparatorCypher += String.format(":Cylinder {radius: %f})<-[:HAS]-", width / 2);
						}
						lowerBsPosY = sepY + panelSeparatorHeight / 2;
						break;
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
			String statement = cypherSetBuildingSegmentAttributes(floor, bWidth * 1.1, bLength * 1.1,
				bHeight / (floorNumberValue + 2 ) * 0.80, floorColor);
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
			connector.executeWrite(cypherSetBuildingSegmentAttributes(chimney, 0.5, 0.5, 1.0, chimneyColor));
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
			double x = (bPosX - ( bWidth / 2) ) + 0.5 + chimneyCounter;
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
			double z = (bPosZ - ( bWidth / 2) ) + 0.5 + chimneyCounter;
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z));
			chimneyCounter++;
		}
		chimneyCounter = 0;
		for (Long chimney : courner3) {
			double x = (bPosX + ( bWidth / 2) ) - 0.5 - chimneyCounter;
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
			double z = (bPosZ + ( bWidth / 2) ) - 0.5 - chimneyCounter;
			connector.executeWrite(
				String.format("MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					chimney, x, y, z));
			chimneyCounter++;
		}
	}

	// Calculates side capacity for progressive/balanced bricks layout
	private int calculateSideCapacity(double value) {
		int sc = 0; // side capacity
		int lc; // layer capacity
		int nolMin; // number of layers
		int bcMin; // building capacity min
		int bcMax; // building capacity max
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
				"MATCH (n) WHERE ID(n) = %d SET n.width = %f, n.length = %f, n.height = %f, n.color = '%s'", segment,
			width, length, height, color);
	}

}
