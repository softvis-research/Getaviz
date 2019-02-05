package org.getaviz.generator.city.m2m;

import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.city.CityUtils;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.database.Rels;
import org.getaviz.generator.database.Database;

public class BrickLayout {
	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
	private static GraphDatabaseService graph = Database.getInstance();

	public static void brickLayout(Node model) {
		Result buildings = graph.execute("MATCH (n:City:Model)-[:CONTAINS*]->(m:Building) WHERE ID(n) = " + model.getId() + " RETURN m");
		while(buildings.hasNext()) {
			Node building = (Node)buildings.next().get("m");
			separateBuilding(building);
		}
	}

	// Builds up the bricks for a specific given building/class
	private static void separateBuilding(Node building) {
		// Don't build up bricks, if this building isn't visualized or isn't positioned
		// (e.g. is an inner classes)
		if (building.getSingleRelationship(Rels.HAS, Direction.OUTGOING).getEndNode() == null) {
			return;
		}

		// variables for brick algorithm
		int sideCapacity, layerCapacity, brickIndexWithinSide, brickIndexWithinLayer, sideIndex, // side index -
																									// north,east,...
				bsPosIndex_X, bsPosIndex_Y, bsPosIndex_Z;
		double b_lowerLeftX, b_upperY, b_lowerLeftZ;
		sideCapacity = (Integer) building.getProperty("sideCapacity");
		List<Node> classElements = null;
		switch (config.getClassElementsMode()) {
		case ATTRIBUTES_ONLY:
			classElements = CityUtils.getData(building);
			CityUtils.sortBuildingSegments(CityUtils.getData(building));
			break;
		case METHODS_ONLY:
			classElements = CityUtils.getMethods(building);
			CityUtils.sortBuildingSegments(CityUtils.getMethods(building));
			break;
		default:
			classElements = CityUtils.getChildren(building);
			break;
		}
		CityUtils.sortBuildingSegments(classElements);
		// coordinates of edges of building
		Node position = building.getSingleRelationship(Rels.HAS, Direction.OUTGOING).getEndNode();
		b_lowerLeftX = (Double)position.getProperty("x") - (Double)building.getProperty("width") / 2;
		b_lowerLeftZ = (Double)position.getProperty("z") - (Double)building.getProperty("length") / 2;
		b_upperY = (Double)position.getProperty("y") + (Double)building.getProperty("height") / 2;
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
			Node pos = graph.createNode(Labels.Position, Labels.City, Labels.Dummy);
			classElements.get(i).createRelationshipTo(pos, Rels.HAS);
			pos.setProperty("x", b_lowerLeftX + config.getBrickHorizontalMargin()
					+ (config.getBrickHorizontalGap() + config.getBrickSize()) * bsPosIndex_X
					+ config.getBrickSize() * 0.5);
			pos.setProperty("y", b_upperY + config.getBrickVerticalMargin()
					+ (config.getBrickVerticalGap() + config.getBrickSize()) * bsPosIndex_Y
					+ config.getBrickSize() * 0.5);
			pos.setProperty("z", b_lowerLeftZ + config.getBrickHorizontalMargin()
			+ (config.getBrickHorizontalGap() + config.getBrickSize()) * bsPosIndex_Z
			+ config.getBrickSize() * 0.5);
		}
	}
}
