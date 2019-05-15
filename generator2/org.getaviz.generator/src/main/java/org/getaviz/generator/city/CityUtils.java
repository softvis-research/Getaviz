package org.getaviz.generator.city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.getaviz.generator.city.m2m.BuildingSegmentComparator;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

public class CityUtils {

	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
	private static DatabaseConnector connector = DatabaseConnector.getInstance();

	public static String setBuildingSegmentColor(Node relatedEntity) {
		String color = "";
		String visibility = relatedEntity.get("visibility").asString("");
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			switch (config.getScheme()) {
			case VISIBILITY:
				if (visibility.equals("public")) {
					color = config.getCityColor("dark_green").toString();
				} else if (visibility.equals("protected")) {
					color = config.getCityColor("yellow").toString();
				} else if (visibility.equals("private")) {
					color = config.getCityColor("red").toString();
				} else {
					// Package visibility or default
					color = config.getCityColor("blue").toString();
				}
				break;
			case TYPES:
				if(relatedEntity.hasLabel(Labels.Field.name())) {
					color = setAttributeColor(relatedEntity.id());
				} else if(relatedEntity.hasLabel(Labels.Method.name())) {
					color = setMethodColor(relatedEntity);
				} else {
					color =  config.getCityColor("blue").toString();
				}
			default:
				color = config.getCityColor("blue").toString();
			}
		} else {
			switch (config.getScheme()) {
			case VISIBILITY:
				if (visibility.equals("public")) {
					color = config.getCityColor("dark_green").toString();
				} else if (visibility.equals("protected")) {
					color = config.getCityColor("yellow").toString();
				} else if (visibility.equals("private")) {
					color = config.getCityColor("red").toString();
				} else {
					// Package visibility or default
					color = config.getCityColor("blue").toString();
				}
				break;
			case TYPES:
				if(relatedEntity.hasLabel(Labels.Field.name())) {
					color = setAttributeColor(relatedEntity.id());
				} else if(relatedEntity.hasLabel(Labels.Method.name())) {
					color = setMethodColor(relatedEntity);
				} else {
					color = config.getCityColor("blue").toString();
				}
				break;
			default:
				color = config.getCityColor("blue").toString();
			}
		}
		return color;
	}	

	private static String setAttributeColor(Long relatedEntity) {
		String color = "";
		boolean isPrimitive = false;
		StatementResult result = connector.executeRead("MATCH (n)-[OF_TYPE]->(t:Primitive) WHERE ID(n) = " + relatedEntity + " RETURN t");
		if(result.hasNext()) {
			isPrimitive = true;
		}
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			if (isPrimitive) {
				color = config.getCityColor("pink").toString();
			} else { // complex type
				color = config.getCityColor("aqua").toString();
			}
		} else {
			if (isPrimitive) {
				color = config.getCityColor("pink").toString();
			} else { // complex type
				color = config.getCityColor("aqua").toString();
			}
		}
		return color;
	}
	
	private static String setMethodColor(Node relatedEntity) {
		String color = "";
		boolean isStatic = relatedEntity.get("static").asBoolean(false);
		boolean isAbstract = relatedEntity.get("abstract").asBoolean(false);
	
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			// if (bs.getMethodKind().equals("constructor")) {
			if (relatedEntity.hasLabel(Labels.Constructor.name())) {
				color = config.getCityColor("red").toString();
			} else if (relatedEntity.hasLabel(Labels.Getter.name())) {
				color = config.getCityColor("light_green").toString();
			} else if (relatedEntity.hasLabel(Labels.Setter.name())) {
				color = config.getCityColor("dark_green").toString();
			} else if (isStatic) {
				color = config.getCityColor("yellow").toString();
			} else if (isAbstract) {
				color = config.getCityColor("orange").toString();
			} else {
				// Default
				color = config.getCityColor("violet").toString();
			}
		} else {
			// if (bs.getMethodKind().equals("constructor")) {
			if (relatedEntity.hasLabel(Labels.Constructor.name())) {
				color = config.getCityColor("red").toString();
			} else if (relatedEntity.hasLabel(Labels.Getter.name())) {
				color = config.getCityColor("light_green").toString();
			} else if (relatedEntity.hasLabel(Labels.Setter.name())) {
				color = config.getCityColor("dark_green").toString();
			} else if (isStatic) {
				color = config.getCityColor("yellow").toString();
			} else if (isAbstract) {
				color = config.getCityColor("orange").toString();
			} else {
				// Default
				color = config.getCityColor("violet").toString();
			}
		}
		return color;
	}

	/**
	 * Sorting the BuildingSegments with help of
	 * {@link BuildingSegmentComparator} based on sorting settings
	 * 
	 * @param segments BuildingSegments which are to be sorted.
	 *
	 */
	
	public static void sortBuildingSegments(final List<Node> segments) {
		final List<BuildingSegmentComparator> sortedList = new ArrayList<BuildingSegmentComparator>(segments.size());
		for (Node segment : segments)
			sortedList.add(new BuildingSegmentComparator(segment));
		Collections.sort(sortedList);
		segments.clear();
		for (BuildingSegmentComparator bsc : sortedList)
			segments.add(bsc.getSegment());
	}
		
	public static List<Node> getChildren(Long parent) {
		ArrayList<Node> children = new ArrayList<Node>();
		StatementResult childs = connector.executeRead("MATCH (n)-[:CONTAINS]->(child) WHERE ID(n) = " + parent + " RETURN child");
		while(childs.hasNext()) {
			children.add(childs.next().get("child").asNode());
		}
		return children;
	}

	public static List<Node> getMethods(Long building) {
		StatementResult result = connector.executeRead("MATCH (n)-[:CONTAINS]->(bs:BuildingSegment)-[:VISUALIZES]->(m:Method) WHERE ID(n) = " + building + " RETURN bs");
		ArrayList<Node> methods = new ArrayList<Node>();
		while(result.hasNext()) {
			methods.add(result.next().get("bs").asNode());
		}
		return methods;
	}

	public static List<Node> getData(Long building) {
		StatementResult result = connector.executeRead("MATCH (n)-[:CONTAINS]->(bs:BuildingSegment)-[:VISUALIZES]->(f:Field) WHERE ID(n) = " + building + " RETURN bs");
		ArrayList<Node> data = new ArrayList<Node>();
		while(result.hasNext()) {
			data.add(result.next().get("bs").asNode());
		}
		return data;
	}
}