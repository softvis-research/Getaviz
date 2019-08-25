package org.getaviz.generator.rd.m2m;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.getaviz.generator.rd.RDUtils;
import java.util.ArrayList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.types.Node;
import java.util.Iterator;
import java.util.List;
import org.neo4j.driver.v1.StatementResult;
import com.google.common.collect.Lists;
import java.util.stream.Collectors;

public class RD2RD {
	private SettingsConfiguration config = SettingsConfiguration.getInstance();
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(RD2RD.class);

// TODO set colors via RGBColor class for all entities
// color scheme
	private RGBColor NS_colorStart = new RGBColor(150, 150, 150);
	private RGBColor NS_colorEnd = new RGBColor(240, 240, 240); // from CodeCity
	private List<RGBColor> NS_colors;

	public RD2RD() {
		log.info("RD2RD started");
		StatementResult length = connector.executeRead(
				"MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) WHERE NOT (m)-[:CONTAINS]->(:RD:Disk) RETURN max(length(p)) AS length");
		int diskMaxLevel = length.single().get("length").asInt(0) + 1;



		NS_colors = createColorGradiant(NS_colorStart, NS_colorEnd, diskMaxLevel);


		connector.executeRead(
				"MATCH p = (n:Model:RD)-[:CONTAINS*]->(d:Disk)-[:VISUALIZES]->(e) RETURN d,e,length(p)-1 AS length")
				.forEachRemaining((result) -> {

					String setString = " SET n.maxLevel = " + diskMaxLevel;
					Node disk = result.get("d").asNode();
					if ((result.get("e").asNode().hasLabel(Labels.Package.name())) ||
							(result.get("e").asNode().hasLabel(Labels.TranslationUnit.name()))) {
						setString += ", n.color = \'" + setNamespaceColor(result.get("length").asInt()) + "\'";
					}
					connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk.id() + setString);
				});

		calculateNetArea(getRootDisks());
		getDisks().forEachRemaining((result) -> {
			calculateRadius(result.get("d").asNode());
		});
		calculateLayout(getRootDisks());

		getDisks().forEachRemaining((result) -> {
			postLayout(result.get("d").asNode());
		});

		getDisks().forEachRemaining((result) -> {
			postLayout2(result.get("d").asNode());
		});

		log.info("RD2RD finished");
	}

	private String setNamespaceColor(int level) {
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			return config.getRDNamespaceColorHex();
		} else {
			return NS_colors.get(level - 1).asPercentage();
		}
	}

	private void calculateNetArea(Iterator<Node> disks) {
		disks.forEachRemaining((disk) -> {
			calculateNetArea(getSubDisks(disk.id()));
			calculateNetArea(disk.id());
		});
	}

	private void calculateNetArea(Long disk) {
		double netArea = 0.0;
		double dataSum = connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Field) WHERE ID(n) = " + disk
						+ " SET d.size = d.size * " + config.getRDDataFactor() + " RETURN SUM(d.size) AS sum")
				.single().get("sum").asDouble();
		double methodSum = connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Method) WHERE ID(n) = " + disk
						+ " SET d.size = d.size * " + config.getRDDataFactor() + " RETURN SUM(d.size) AS sum")
				.single().get("sum").asDouble();
		netArea = dataSum + methodSum;
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk + " SET n.netArea = " + netArea);
	}

	private void calculateRadius(Node disk) {
		double netArea = disk.get("netArea").asDouble();
		double ringWidth = disk.get("ringWidth").asDouble();
		double radius = Math.sqrt(netArea / Math.PI) + ringWidth;
		connector.executeWrite("MATCH(n) WHERE ID(n) = " + disk.id() + " SET n.radius = " + radius);
	}

	private void calculateLayout(Iterator<Node> disks) {
		ArrayList<CircleWithInnerCircles> nestedCircles = new ArrayList<CircleWithInnerCircles>();
		disks.forEachRemaining((disk) -> {
			nestedCircles.add(new CircleWithInnerCircles(disk, false));
		});
		RDLayout.nestedLayout(nestedCircles);
		for (CircleWithInnerCircles circle : nestedCircles) {
			circle.updateDiskNode();
		}
	}

	private void postLayout(Node disk) {
		List<Node> data = Lists.newArrayList(RDUtils.getData(disk.id()));
		List<Node> methods = Lists.newArrayList(RDUtils.getMethods(disk.id()));
		fractions(disk, data, methods);
		fractions(data);
		fractions(methods);
	}

	private void postLayout2(Node disk) {
		RDUtils.getSubDisks(disk.id()).forEachRemaining((result) -> {
			calculateRings(result.get("d").asNode());
		});
		calculateRings(disk);
	}

	private void fractions(Node disk, Iterable<Node> data, Iterable<Node> methods) {
		double netArea = disk.get("netArea").asDouble();
		double currentMethodArea = RDUtils.sum(methods) / netArea;
		double currentDataArea = RDUtils.sum(data) / netArea;
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk.id() + " SET n.methodArea = " + currentMethodArea
				+ ", n.dataArea = " + currentDataArea);
	}

	private void fractions(Iterable<Node> segments) {
		double sum = RDUtils.sum(segments);
		for (Node segment : segments) {
			connector.executeWrite("MATCH (n) WHERE ID(n) = " + segment.id() + " SET n.size = n.size/" + sum);
		}
	}

	private void calculateRings(Node disk) {
		double ringWidth = disk.get("ringWidth").asDouble();
		double height = disk.get("height").asDouble();
		double radius = disk.get("radius").asDouble();
		double methodArea = disk.get("methodArea").asDouble();
		double dataArea = disk.get("dataArea").asDouble();
		double netArea = disk.get("netArea").asDouble();
		if (ringWidth == 0) {
			calculateCrossSection(disk.id(), ringWidth, 0);
		} else {
			calculateCrossSection(disk.id(), ringWidth, height);
		}
		calculateSpines(disk.id(), radius - (0.5 * ringWidth));
		if (RDUtils.getSubDisks(disk.id()).list().size() == 0) {
			double r_data = Math.sqrt(dataArea * netArea / Math.PI);
			double r_methods = radius - ringWidth;
			double b_methods = r_methods - r_data;
			List<Node> diskMethods = Lists.newArrayList(RDUtils.getMethods(disk.id()));
			List<Node> diskData = Lists.newArrayList(RDUtils.getData(disk.id()));
			if (!diskMethods.isEmpty()) {
				calculateCrossSection(diskMethods, b_methods, height);
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods);
				if (config.getOutputFormat() == OutputFormat.AFrame) {
					for (Node method : diskMethods) {
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + method.id() + " SET n.outerRadius = "
								+ r_methods + ", n.innerRadius = " + r_data);
					}
				}
			}
			if (!diskData.isEmpty()) {
				calculateCrossSection(diskData, r_data, height);
				calculateSpines(diskData, 0.5 * r_data);
				if (config.getOutputFormat() == OutputFormat.AFrame) {
					for (Node data : diskData) {
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + data.id() + " SET n.outerRadius = " + r_data
								+ ", n.innerRadius = " + 0.0);
					}
				}
			}
		} else {
			double outerRadius = calculateOuterRadius(disk.id());
			double r_data = Math.sqrt((dataArea * netArea / Math.PI) + (outerRadius * outerRadius));
			double b_data = r_data - outerRadius;
			double r_methods = Math.sqrt((methodArea * netArea / Math.PI) + (r_data * r_data));
			double b_methods = r_methods - r_data;
			List<Node> diskMethods = Lists.newArrayList(RDUtils.getMethods(disk.id()));
			if (!diskMethods.isEmpty()) {
				calculateCrossSection(diskMethods, b_methods, height);
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods);
				if (config.getOutputFormat() == OutputFormat.AFrame) {
					for (Node method : diskMethods) {
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + method.id() + " SET n.outerRadius = "
								+ r_methods + ",n.innerRadius = " + r_data);
					}
				}
			}
			List<Node> diskData = Lists.newArrayList(RDUtils.getData(disk.id()));
			if (!diskData.isEmpty()) {
				calculateCrossSection(diskData, b_data, height);
				calculateSpines(diskData, r_data - 0.5 * b_data);
				if (config.getOutputFormat() == OutputFormat.AFrame) {
					for (Node data : diskData) {
						connector.executeWrite("MATCH (n) WHERE ID (n)= " + data.id() + " SET n.outerRadius = " + r_data
								+ ", n.innerRadius = " + (r_data - b_data));
					}
				}
			}
		}
	}

	private double calculateOuterRadius(Long disk) {
		CoordinateList coordinates = new CoordinateList();
		RDUtils.getSubDisks(disk).forEachRemaining((subDisk) -> {
			Node node = subDisk.get("d").asNode();
			StatementResult position = connector
					.executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + node.id() + " RETURN p");
			double x = 0.0;
			double y = 0.0;
			Node posNode = position.single().get("p").asNode();
			x = posNode.get("x").asDouble();
			y = posNode.get("y").asDouble();
			coordinates.add(createCircle(x, y, node.get("radius").asDouble()).getCoordinates(), false);
		});
		GeometryFactory geoFactory = new GeometryFactory();
		MultiPoint innerCircleMultiPoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray());
		MinimumBoundingCircle mbc = new MinimumBoundingCircle(innerCircleMultiPoint);

		return mbc.getRadius();
	}

	private Geometry createCircle(double x, double y, double radius) {
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(64);
		shapeFactory.setCentre(new Coordinate(x, y));
		shapeFactory.setSize(radius * 2);
		return shapeFactory.createCircle();
	}

	private void calculateCrossSection(List<Node> segments, double width, double height) {
		String crossSection = (-(width / 2) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", "
				+ ((width / 2) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height));
		List<String> statementList = Lists.newArrayList();
		for (Node segment : segments) {
			statementList
					.add("MATCH (n) WHERE ID(n) = " + segment.id() + " SET n.crossSection = \'" + crossSection + "\'");
		}
		;
		connector.executeWrite(statementList.stream().toArray(String[]::new));
	}

	private void calculateCrossSection(Long disk, double width, double height) {
		String crossSection = (-(width / 2) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", "
				+ ((width / 2) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height));
		connector.executeWrite("MATCH (n)  WHERE ID(n) = " + disk + " SET n.crossSection = \'" + crossSection + "\'");
	}

	private void calculateSpines(List<Node> segments, double factor) {
		if (config.getOutputFormat() == OutputFormat.X3D) {
			int spinePointCount = 0;
			if (segments.size() < 50) {
				spinePointCount = 400;
			} else {
				spinePointCount = 1000;
			}
			List<String> completeSpine = new ArrayList<>();
			double stepX = 2 * Math.PI / spinePointCount;

			for (int i = 0; i < spinePointCount; ++i) {
				completeSpine.add(factor * Math.cos(i * stepX) + " " + factor * Math.sin(i * stepX) + " " + 0.0);
			}
			completeSpine.add(completeSpine.get(0));
			// calculate spines according to fractions
			int start = 0;
			int end = 0;
			List<String> statementList = new ArrayList<>();
			for (Node segment : segments) {
				double size = segment.get("size").asDouble();
				start = end;
				end = start + (int) Math.floor(spinePointCount * size);
				if (end > (completeSpine.size() - 1)) {
					end = completeSpine.size() - 1;
				}
				if (segment == segments.get(segments.size() - 1)) {
					end = completeSpine.size() - 1;
				}
				List<String> partSpine = new ArrayList<>();
				for (int j = 0; j < end - start; j++) {
					partSpine.add(completeSpine.get(start + j));
				}
				statementList.add("MATCH (n) WHERE ID(n) = " + segment.id() + " SET n.spine = \'"
						+ removeBrackets(partSpine) + "\'");
			}
			connector.executeWrite(statementList.stream().toArray(String[]::new));

		}
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			if (!segments.isEmpty()) {
				int length = segments.size();
				double sizeSum = 0.0;
				double position = 0.0;
				for (Node segment : segments) {
					double size = segment.get("size").asDouble();
					sizeSum += size;
				}
				sizeSum += sizeSum / 360 * length;
				for (Node segment : segments) {
					double angle = (segment.get("size").asDouble() / sizeSum) * 360;
					connector.executeWrite("MATCH (n) WHERE ID(n) = " + segment.id() + " SET n.angle = " + angle
							+ ", n.anglePosition = " + position);
					position += angle + 1;
				}
			}
		}
	}

	private void calculateSpines(Long disk, double factor) {
		int spinePointCount = 50;
		List<String> completeSpine = new ArrayList<>();
		double stepX = 2 * Math.PI / spinePointCount;
		for (int i = 0; i < spinePointCount; ++i) {
			completeSpine.add(factor * Math.cos(i * stepX) + " " + factor * Math.sin(i * stepX) + " " + 0.0);
		}
		completeSpine.add(completeSpine.get(0));
		connector.executeWrite(
				"MATCH (n) WHERE ID(n) = " + disk + " SET n.spine = \'" + removeBrackets(completeSpine) + "\'");
	}

	private List<RGBColor> createColorGradiant(RGBColor start, RGBColor end, int maxLevel) {
		int steps = maxLevel;
		double r_step = (end.r() - start.r()) / steps;
		double g_step = (end.g() - start.g()) / steps;
		double b_step = (end.b() - start.b()) / steps;

		List<RGBColor> colorRange = new ArrayList<>();
		for (int i = 0; i < maxLevel; ++i) {
			double newR = start.r() + i * r_step;
			double newG = start.g() + i * g_step;
			double newB = start.b() + i * b_step;

			colorRange.add(i, new RGBColor(newR, newG, newB));
		}
		return colorRange;
	}

	private StatementResult getDisks() {
		return connector.executeRead("MATCH (n:Model:RD)-[:CONTAINS*]->(d:Disk)-[:VISUALIZES]->(element) " + "RETURN d "
				+ "ORDER BY element.hash");
	}

	private Iterator<Node> getSubDisks(Long entity) {
		return connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) " + "WHERE ID(n) = " + entity
						+ " " + "RETURN d " + "ORDER BY element.hash")
				.stream().map(s -> s.get("d").asNode()).collect(Collectors.toList()).listIterator();
	}

	private Iterator<Node> getRootDisks() {
		return connector
				.executeRead("MATCH (n:RD:Model)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) " + "RETURN d "
						+ "ORDER BY element.hash")
				.stream().map(s -> s.get("d").asNode()).collect(Collectors.toList()).listIterator();
	}

	private String removeBrackets(List<String> list) {
		return StringUtils.remove(StringUtils.remove(list.toString(), "["), "]");
	}
}
