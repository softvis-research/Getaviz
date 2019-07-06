package org.getaviz.generator.city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.getaviz.generator.city.m2m.BuildingSegmentComparator;
import org.getaviz.generator.city.m2m.RGBColor;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

public class CityUtils {

	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
	private static DatabaseConnector connector = DatabaseConnector.getInstance();
	public static String getFamixClassString(final String className) {
		String s = className.substring(0, 5) + "." + className.substring(5, className.length());
		if (className.endsWith("Impl"))
			s = s.substring(0, s.length() - 4);
		return s;
	}

	/**
	 * Creates the color gradient for the packages depending on your hierarchy
	 * level.
	 *
	 * @param start
	 *            RGBColor
	 * @param end
	 *            RGBColor
	 * @param maxLevel
	 *            int
	 * @return color range
	 */
	public static RGBColor[] createPackageColorGradient(final RGBColor start, final RGBColor end, final int maxLevel) {
		int steps = maxLevel - 1;
		if (maxLevel == 1) {
			steps++;
		}
		double r_step = (end.r() - start.r()) / steps;
		double g_step = (end.g() - start.g()) / steps;
		double b_step = (end.b() - start.b()) / steps;

		RGBColor[] colorRange = new RGBColor[maxLevel];
		double newR, newG, newB;
		for (int i = 0; i < maxLevel; ++i) {
			newR = start.r() + i * r_step;
			newG = start.g() + i * g_step;
			newB = start.b() + i * b_step;

			colorRange[i] = new RGBColor(newR, newG, newB);
		}

		return colorRange;
	}

	public static String setBuildingSegmentColor(Node relatedEntity) {
		String color = "";
		String visibility = relatedEntity.get("visibility").asString("");
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			switch (config.getScheme()) {
			case VISIBILITY:
				if (visibility.equals("public")) {
					color = config.getCityColorHex("dark_green");
				} else if (visibility.equals("protected")) {
					color = config.getCityColorHex("yellow");
				} else if (visibility.equals("private")) {
					color = config.getCityColorHex("red");
				} else {
					// Package visibility or default
					color = config.getCityColorHex("blue");
				}
				break;
			case TYPES:
				if(relatedEntity.hasLabel(Labels.Field.name())) {
					color = setAttributeColor(relatedEntity.id());
				} else if(relatedEntity.hasLabel(Labels.Method.name())) {
					color = setMethodColor(relatedEntity);
				} else {
					color =  config.getCityColorHex("blue");
				}
			default:
				color = config.getCityColorHex("blue");
			}
		} else {
			switch (config.getScheme()) {
			case VISIBILITY:
				if (visibility.equals("public")) {
					color = config.getCityColorAsPercentage("dark_green");
				} else if (visibility.equals("protected")) {
					color = config.getCityColorAsPercentage("yellow");
				} else if (visibility.equals("private")) {
					color = config.getCityColorAsPercentage("red");
				} else {
					// Package visibility or default
					color = config.getCityColorAsPercentage("blue");
				}
				break;
			case TYPES:
				if(relatedEntity.hasLabel(Labels.Field.name())) {
					color = setAttributeColor(relatedEntity.id());
				} else if(relatedEntity.hasLabel(Labels.Method.name())) {
					color = setMethodColor(relatedEntity);
				} else {
					color = config.getCityColorAsPercentage("blue");
				}
				break;
			default:
				color = config.getCityColorAsPercentage("blue");
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
				color = config.getCityColorHex("pink");
			} else { // complex type
				color = config.getCityColorHex("aqua");
			}
		} else {
			if (isPrimitive) {
				color = config.getCityColorAsPercentage("pink");
			} else { // complex type
				color = config.getCityColorAsPercentage("aqua");
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
				color = config.getCityColorHex("red");
			} else if (relatedEntity.hasLabel(Labels.Getter.name())) {
				color = config.getCityColorHex("light_green");
			} else if (relatedEntity.hasLabel(Labels.Setter.name())) {
				color = config.getCityColorHex("dark_green");
			} else if (isStatic) {
				color = config.getCityColorHex("yellow");
			} else if (isAbstract) {
				color = config.getCityColorHex("orange");
			} else {
				// Default
				color = config.getCityColorHex("violet");
			}
		} else {
			// if (bs.getMethodKind().equals("constructor")) {
			if (relatedEntity.hasLabel(Labels.Constructor.name())) {
				color = config.getCityColorAsPercentage("red");
			} else if (relatedEntity.hasLabel(Labels.Getter.name())) {
				color = config.getCityColorAsPercentage("light_green");
			} else if (relatedEntity.hasLabel(Labels.Setter.name())) {
				color = config.getCityColorAsPercentage("dark_green");
			} else if (isStatic) {
				color = config.getCityColorAsPercentage("yellow");
			} else if (isAbstract) {
				color = config.getCityColorAsPercentage("orange");
			} else {
				// Default
				color = config.getCityColorAsPercentage("violet");
			}
		}
		return color;
	}

	/**
	 * Sorting the {@link BuildingSegment}s with help of
	 * {@link BuildingSegmentComparator} based on sorting settings in
	 * {@link CitySettings}.
	 * 
	 * @param bsList
	 *            BuildingSegments which are to be sorted.
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