package org.getaviz.generator.city.m2m;

import java.util.List;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.city.CityUtils;
import org.getaviz.generator.database.DatabaseConnector;

public class BrickLayout {
	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
	private static DatabaseConnector connector = DatabaseConnector.getInstance();

	public static void brickLayout(Long model) {
		StatementResult buildings = connector
				.executeRead("MATCH (n:City:Model)-[:CONTAINS*]->(b:Building) WHERE ID(n) = " + model + " RETURN b");
		while (buildings.hasNext()) {
			Node building = buildings.next().get("b").asNode();
			separateBuilding(building);
		}
	}

	// Builds up the bricks for a specific given building/class
	private static void separateBuilding(Node building) {
		// Don't build up bricks, if this building isn't visualized or isn't positioned
		// (e.g. is an inner classes)
		if (!connector.executeRead("MATCH (b)-[:HAS]->(n) WHERE ID(b) = " + building.id() + " RETURN n").hasNext()) {
			return;
		}

		// variables for brick algorithm
		int sideCapacity, layerCapacity, brickIndexWithinSide, brickIndexWithinLayer, sideIndex, // side index -
																									// north,east,...
				bsPosIndex_X, bsPosIndex_Y, bsPosIndex_Z;
		double b_lowerLeftX, b_upperY, b_lowerLeftZ;
		sideCapacity = building.get("sideCapacity").asInt();
		List<Node> classElements = null;
		switch (config.getClassElementsMode()) {
		case ATTRIBUTES_ONLY:
			classElements = CityUtils.getData(building.id());
			CityUtils.sortBuildingSegments(CityUtils.getData(building.id()));
			break;
		case METHODS_ONLY:
			classElements = CityUtils.getMethods(building.id());
			CityUtils.sortBuildingSegments(CityUtils.getMethods(building.id()));
			break;
		default:
			classElements = CityUtils.getChildren(building.id());
			break;
		}
		CityUtils.sortBuildingSegments(classElements);
		// coordinates of edges of building
		Node position = connector.getPosition(building.id());
		b_lowerLeftX = position.get("x").asDouble() - building.get("width").asDouble() / 2;
		b_lowerLeftZ = position.get("z").asDouble() - building.get("length").asDouble() / 2;
		b_upperY = position.get("y").asDouble() + building.get("height").asDouble() / 2;
		// System.out.println("");
		// set positions for all methods in current class
		for (int i = 0; i < classElements.size(); ++i) {
			if (sideCapacity <= 1) {
				layerCapacity = 1;
				brickIndexWithinSide = 0;
				sideIndex = 0;
			} else {
				layerCapacity = (sideCapacity - 1) * 4;
				brickIndexWithinLayer = i % layerCapacity;
				brickIndexWithinSide = brickIndexWithinLayer % (sideCapacity - 1);
				sideIndex = brickIndexWithinLayer / (sideCapacity - 1);
			}
			// System.out.println(bs.getType() + " " + bs.getValue() + " " +
			// bs.getModifiers() + " " + bs.getNumberOfStatements());
			// calculating position for brick
			switch (sideIndex) {
			case 0:
				bsPosIndex_X = brickIndexWithinSide;
				bsPosIndex_Z = 0;
				break;
			case 1:
				bsPosIndex_X = sideCapacity - 1;
				bsPosIndex_Z = brickIndexWithinSide;
				break;
			case 2:
				bsPosIndex_X = sideCapacity - brickIndexWithinSide - 1;
				bsPosIndex_Z = sideCapacity - 1;
				break;
			default:
				bsPosIndex_X = 0;
				bsPosIndex_Z = sideCapacity - brickIndexWithinSide - 1;
				break;
			}
			bsPosIndex_Y = i / layerCapacity;

			// setting position for brick
			double x = b_lowerLeftX + config.getBrickHorizontalMargin()
					+ (config.getBrickHorizontalGap() + config.getBrickSize()) * bsPosIndex_X
					+ config.getBrickSize() * 0.5;
			double y = b_upperY + config.getBrickVerticalMargin()
					+ (config.getBrickVerticalGap() + config.getBrickSize()) * bsPosIndex_Y
					+ config.getBrickSize() * 0.5;
			double z = b_lowerLeftZ + config.getBrickHorizontalMargin()
					+ (config.getBrickHorizontalGap() + config.getBrickSize()) * bsPosIndex_Z
					+ config.getBrickSize() * 0.5;
			connector.executeWrite(String.format(
					"MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:City:Position {x: %f, y: %f, z: %f})",
					classElements.get(i).id(), x, y, z));
		}
	}
}
