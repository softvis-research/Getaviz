package org.getaviz.generator.city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.getaviz.generator.city.m2m.BuildingSegmentComparator;
import org.getaviz.generator.city.m2m.RGBColor;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.database.Rels;

public class CityUtils {

	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
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

	public static void setBuildingSegmentColor(final Node segment) {
		String color = "";
		Node entity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).getEndNode();
		String visibility = "";
		if(entity.hasProperty("visibility")) {
			visibility = (String)(entity.getProperty("visibility"));
		}
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
				segment.setProperty("color", color);
				break;
			case TYPES:
				if(entity.hasLabel(Labels.Field)) {
					setAttributeColor(segment);
				} else if(entity.hasLabel(Labels.Method)) {
					setMethodColor(segment);
				} else {
					segment.setProperty("color", config.getCityColorHex("blue"));
				}
			default:
				segment.setProperty("color", config.getCityColorHex("blue"));
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
				segment.setProperty("color", color);
				break;
			case TYPES:
				if(entity.hasLabel(Labels.Field)) {
					setAttributeColor(segment);
				} else if(entity.hasLabel(Labels.Method)) {
					setMethodColor(segment);
				} else {
					segment.setProperty("color", config.getCityColorAsPercentage("blue"));
				}
				break;
			default:
				segment.setProperty("color", config.getCityColorAsPercentage("blue"));
			}
		}
	}	

	private static void setAttributeColor(final Node segment) {
		String color = "";
		Node entity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).getEndNode();
		boolean isPrimitive = false;
		if(entity.hasRelationship(Rels.OF_TYPE)) {
			Node declaredType = entity.getSingleRelationship(Rels.OF_TYPE, Direction.OUTGOING).getEndNode();
			if (declaredType.hasLabel(Labels.Primitive)) {
				isPrimitive = true;
			}
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
		segment.setProperty("color", color);
	}
	
	private static void setMethodColor(final Node segment) {
		Node entity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).getEndNode();
		String color = "";
		boolean isStatic = false;
		if(entity.hasProperty("static")) {
			isStatic = (Boolean)entity.getProperty("static");
		}
		boolean isAbstract = false;
		if(entity.hasProperty("abstract")) {
			isAbstract = (Boolean)entity.getProperty("abstract");
		}		
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			// if (bs.getMethodKind().equals("constructor")) {
			if (entity.hasLabel(Labels.Constructor)) {
				color = config.getCityColorHex("red");
			} else if (entity.hasLabel(Labels.Getter)) {
				color = config.getCityColorHex("light_green");
			} else if (entity.hasLabel(Labels.Setter)) {
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
			if (entity.hasLabel(Labels.Constructor)) {
				color = config.getCityColorAsPercentage("red");
			} else if (entity.hasLabel(Labels.Getter)) {
				color = config.getCityColorAsPercentage("light_green");
			} else if (entity.hasLabel(Labels.Setter)) {
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
		segment.setProperty("color", color);
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
	
	public static void sortBuildingSegments(final List<Node> buildingSegments) {
		final List<BuildingSegmentComparator> sortedList = new ArrayList<BuildingSegmentComparator>(buildingSegments.size());
		for (Node segment : buildingSegments)
			sortedList.add(new BuildingSegmentComparator(segment));
		Collections.sort(sortedList);
		buildingSegments.clear();
		for (BuildingSegmentComparator bsc : sortedList)
			buildingSegments.add(bsc.getSegment());
	}
		
	public static List<Node> getChildren(Node parent) {
		ArrayList<Node> children = new ArrayList<Node>();
		Iterable<Relationship> childrenRels = parent.getRelationships(Rels.CONTAINS, Direction.OUTGOING);
		for (Relationship relationship : childrenRels) {
			children.add(relationship.getEndNode());
		}
		return children;
	}

	public static List<Node> getMethods(Node building) {
		ArrayList<Node> methods = new ArrayList<Node>();
		for (Node child : getChildren(building)) {
			Node entity = child.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).getEndNode();
			if (entity.hasLabel(Labels.Method)) {
				methods.add(child);
			}
		}
		return methods;
	}

	public static List<Node> getData(Node building) {
		ArrayList<Node> data = new ArrayList<Node>();
		for (Node child : getChildren(building)) {
			Node entity = child.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).getEndNode();
			if (entity.hasLabel(Labels.Field)) {
				data.add(child);
			}
		}
		return data;
	}
}