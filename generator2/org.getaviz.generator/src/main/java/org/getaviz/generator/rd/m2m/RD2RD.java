package org.getaviz.generator.rd.m2m;

import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.ColorGradient;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.getaviz.generator.rd.RDUtils;

import java.lang.reflect.Array;
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
import org.getaviz.generator.rd.s2m.Disk;
import org.getaviz.generator.rd.s2m.DiskSegment;
import org.getaviz.generator.rd.s2m.RDElement;
import org.neo4j.driver.v1.types.Node;
import java.util.Iterator;
import java.util.List;
import org.neo4j.driver.v1.StatementResult;
import com.google.common.collect.Lists;

import java.util.stream.Collectors;

public class RD2RD implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(RD2RD.class);
	private String NS_colorStart = "#969696";
	private String NS_colorEnd = "#F0F0F0";
	private List<String> NS_colors;
	private OutputFormat outputFormat;
	private double dataFactor;
	private static ArrayList<Disk> disksList = new ArrayList<>();
	private static ArrayList<Disk> rootDisksList = new ArrayList<>();
	private static ArrayList<DiskSegment> fields = new ArrayList<>();
	private static ArrayList<DiskSegment> methods = new ArrayList<>();

	public RD2RD(SettingsConfiguration config) {
		this.outputFormat = config.getOutputFormat();
		this.dataFactor = config.getRDDataFactor();
	}

	public void run() {
		log.info("RD2RD started");
		StatementResult length = connector.executeRead(
				"MATCH p=(n:Package)-[:CONTAINS*]->(m:Package) WHERE NOT (m)-[:CONTAINS]->(:Package) RETURN max(length(p)) AS length");
		int namespaceMaxLevel = length.single().get("length").asInt() + 1;
		length = connector.executeRead(
				"MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) WHERE NOT (m)-[:CONTAINS]->(:RD:Disk) RETURN max(length(p)) AS length");
		int diskMaxLevel = length.single().get("length").asInt() + 1;

		NS_colors = ColorGradient.createColorGradient(NS_colorStart, NS_colorEnd, namespaceMaxLevel);

		connector.executeRead(
				"MATCH p = (n:Model:RD)-[:CONTAINS*]->(d:Disk)-[:VISUALIZES]->(e) RETURN d,e,length(p)-1 AS length")
				.forEachRemaining((result) -> {

					String setString = " SET n.maxLevel = " + diskMaxLevel;
					Node disk = result.get("d").asNode();
					if (result.get("e").asNode().hasLabel(Labels.Package.name())) {
						setString += ", n.color = \'" + setNamespaceColor(result.get("length").asInt()) + "\'";
					}
					connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk.id() + setString);
				});
		//calculateNetArea(getRootDisks());
		createRootDisksList();
		createDisksList();
		createFieldsList();
		createMethodsList();
		calculateNetArea(rootDisksList);
		calculateRadius(disksList);

		//getDisks().forEachRemaining((result) -> calculateRadius(result.get("d").asNode()));
		//calculateLayout(getRootDisks());
		calculateLayout(rootDisksList);

		//getDisks().forEachRemaining((result) -> postLayout(result.get("d").asNode()));
		postLayout(disksList);

		getDisks().forEachRemaining((result) -> postLayout2(result.get("d").asNode()));

		log.info("RD2RD finished");
	}

	private String setNamespaceColor(int level) {
		return NS_colors.get(level - 1);
	}

	/*private void calculateNetArea(Iterator<Node> disks) {
		disks.forEachRemaining((disk) -> {
			 calculateNetArea(getSubDisks(disk.id()));
			calculateNetArea(disk.id());
		});
	}*/

	private void calculateNetArea(ArrayList<Disk> list) {
		list.forEach((disk) -> {
			long id = disk.getId();
			ArrayList<Disk> subDisks = getSubDisk(id);
			calculateNetArea(subDisks);
			//calculateNetArea(getSubDisks(id));
			calculateNetArea(disk);
		});
	}

	/*private void calculateNetArea(Long disk) {
		double netArea;
		double dataSum = connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Field) WHERE ID(n) = " + disk
						+ " SET d.size = d.size * " + dataFactor + " RETURN SUM(d.size) AS sum")
				.single().get("sum").asDouble();
		double methodSum = connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Method) WHERE ID(n) = " + disk
						+ " SET d.size = d.size * " + dataFactor + " RETURN SUM(d.size) AS sum")
				.single().get("sum").asDouble();
		netArea = dataSum + methodSum;
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk + " SET n.netArea = " + netArea);
	}*/

	private void calculateNetArea(RDElement disk) {
		double netArea;
		double dataSum = connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Field) WHERE ID(n) = " + disk.getId()
						+ " SET d.size = d.size * " + dataFactor + " RETURN SUM(d.size) AS sum")
				.single().get("sum").asDouble();
		double methodSum = connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Method) WHERE ID(n) = " + disk.getId()
						+ " SET d.size = d.size * " + dataFactor + " RETURN SUM(d.size) AS sum")
				.single().get("sum").asDouble();
		netArea = dataSum + methodSum;
		disk.setNetArea(netArea);
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk.getId() + " SET n.netArea = " + netArea);
	}

	/*private void calculateRadius(Node disk) {
		double netArea = disk.get("netArea").asDouble();
		double ringWidth = disk.get("ringWidth").asDouble();
		double radius = Math.sqrt(netArea / Math.PI) + ringWidth;
		connector.executeWrite("MATCH(n) WHERE ID(n) = " + disk.id() + " SET n.radius = " + radius);
	}*/

	private void calculateRadius(ArrayList<Disk> list) {
		list.forEach(disk -> {
					double netArea = disk.getNetArea();
					double ringWidth = disk.getRingWidth();
					double radius = Math.sqrt(netArea / Math.PI) + ringWidth;
					disk.setRadius(radius);
					connector.executeWrite("MATCH(n) WHERE ID(n) = " + disk.getId() + " SET n.radius = " + radius);
		});
	}

	/*private void calculateLayout(Iterator<Node> disks) {
		ArrayList<CircleWithInnerCircles> nestedCircles = new ArrayList<>();
		disks.forEachRemaining((disk) -> nestedCircles.add(new CircleWithInnerCircles(disk, false)));
		RDLayout.nestedLayout(nestedCircles);
		for (CircleWithInnerCircles circle : nestedCircles) {
			circle.updateDiskNode();
		}
	}*/

	private void calculateLayout(ArrayList<Disk> list) {
		ArrayList<CircleWithInnerCircles> nestedCircles = new ArrayList<>();
		list.forEach((disk) -> {
			nestedCircles.add(new CircleWithInnerCircles(disk, false));
		});
		RDLayout.nestedLayout(nestedCircles);
		for (CircleWithInnerCircles circle : nestedCircles) {
			circle.updateDiskNode();
		}
	}

	/*private void postLayout(Node disk) {
		List<Node> data = Lists.newArrayList(RDUtils.getData(disk.id()));
		List<Node> methods = Lists.newArrayList(RDUtils.getMethods(disk.id()));
		fractions(disk, data, methods);
		fractions(data);
		fractions(methods);
	}*/

	private void postLayout(ArrayList<Disk> list) {
		list.forEach(disk -> {
			//List<Node> data = Lists.newArrayList(RDUtils.getData(disk.getId()));
			//List<Node> methods = Lists.newArrayList(RDUtils.getMethods(disk.getId()));
			ArrayList<DiskSegment> data = getDiskSegment(disk.getId(), fields);
			ArrayList<DiskSegment> method = getDiskSegment(disk.getId(), methods);
			fractions(disk, data, method);
			fractions(data);
			fractions(method);
		});
	}

	private void postLayout2(Node disk) {
		RDUtils.getSubDisks(disk.id()).forEachRemaining((result) -> calculateRings(result.get("d").asNode()));
		calculateRings(disk);
	}

	/*private void fractions(Node disk, Iterable<Node> data, Iterable<Node> methods) {
		double netArea = disk.get("netArea").asDouble();
		double currentMethodArea = RDUtils.sum(methods) / netArea;
		double currentDataArea = RDUtils.sum(data) / netArea;
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk.id() + " SET n.methodArea = " + currentMethodArea
				+ ", n.dataArea = " + currentDataArea);
	}*/

	private void fractions(Disk disk, ArrayList<DiskSegment> data, ArrayList<DiskSegment> method) {
		double netArea = disk.getNetArea();
		double currentMethodArea = sum(method) / netArea;
		double currentDataArea = sum(data) / netArea;
		disk.setMethodArea(currentMethodArea);
		disk.setDataArea(currentDataArea);
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk.getId() + " SET n.methodArea = " + currentMethodArea
				+ ", n.dataArea = " + currentDataArea);
	}

	private void fractions(ArrayList<DiskSegment> list) {
		double sum = sum(list);
		for (DiskSegment segment : list) {
			double newSize = segment.getSize()/ sum;
			segment.setSize(newSize);
			connector.executeWrite("MATCH (n) WHERE ID(n) = " + segment.getId() + " SET n.size = n.size/" + sum);
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
				if (outputFormat == OutputFormat.AFrame) {
					for (Node method : diskMethods) {
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + method.id() + " SET n.outerRadius = "
								+ r_methods + ", n.innerRadius = " + r_data);
					}
				}
			}
			if (!diskData.isEmpty()) {
				calculateCrossSection(diskData, r_data, height);
				calculateSpines(diskData, 0.5 * r_data);
				if (outputFormat == OutputFormat.AFrame) {
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
				if (outputFormat == OutputFormat.AFrame) {
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
				if (outputFormat == OutputFormat.AFrame) {
					for (Node data : diskData) {
						connector.executeWrite("MATCH (n) WHERE ID (n)= " + data.id() + " SET n.outerRadius = " + r_data
								+ ", n.innerRadius = " + (r_data - b_data));
					}
				}
			}
		}
	}

	private void calculateRings(Disk disk) {
		long id = disk.getId();
		double ringWidth = disk.getRingWidth();
		double height = disk.getHeight();
		double radius = disk.getRadius();
		double methodArea = disk.getMethodArea();
		double dataArea = disk.getDataArea();
		double netArea = disk.getNetArea();
		if (ringWidth == 0) {
			calculateCrossSection(id, ringWidth, 0);
		} else {
			calculateCrossSection(id, ringWidth, height);
		}
		calculateSpines(id, radius - (0.5 * ringWidth));
		if (RDUtils.getSubDisks(id).list().size() == 0) {
			double r_data = Math.sqrt(dataArea * netArea / Math.PI);
			double r_methods = radius - ringWidth;
			double b_methods = r_methods - r_data;
			List<Node> diskMethods = Lists.newArrayList(RDUtils.getMethods(id));
			List<Node> diskData = Lists.newArrayList(RDUtils.getData(id));
			if (!diskMethods.isEmpty()) {
				calculateCrossSection(diskMethods, b_methods, height);
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods);
				if (outputFormat == OutputFormat.AFrame) {
					for (Node method : diskMethods) {
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + method.id() + " SET n.outerRadius = "
								+ r_methods + ", n.innerRadius = " + r_data);
					}
				}
			}
			if (!diskData.isEmpty()) {
				calculateCrossSection(diskData, r_data, height);
				calculateSpines(diskData, 0.5 * r_data);
				if (outputFormat == OutputFormat.AFrame) {
					for (Node data : diskData) {
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + data.id() + " SET n.outerRadius = " + r_data
								+ ", n.innerRadius = " + 0.0);
					}
				}
			}
		} else {
			double outerRadius = calculateOuterRadius(id);
			double r_data = Math.sqrt((dataArea * netArea / Math.PI) + (outerRadius * outerRadius));
			double b_data = r_data - outerRadius;
			double r_methods = Math.sqrt((methodArea * netArea / Math.PI) + (r_data * r_data));
			double b_methods = r_methods - r_data;
			List<Node> diskMethods = Lists.newArrayList(RDUtils.getMethods(id));
			if (!diskMethods.isEmpty()) {
				calculateCrossSection(diskMethods, b_methods, height);
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods);
				if (outputFormat == OutputFormat.AFrame) {
					for (Node method : diskMethods) {
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + method.id() + " SET n.outerRadius = "
								+ r_methods + ",n.innerRadius = " + r_data);
					}
				}
			}
			List<Node> diskData = Lists.newArrayList(RDUtils.getData(id));
			if (!diskData.isEmpty()) {
				calculateCrossSection(diskData, b_data, height);
				calculateSpines(diskData, r_data - 0.5 * b_data);
				if (outputFormat == OutputFormat.AFrame) {
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
			double x;
			double y;
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
		connector.executeWrite(statementList.toArray(new String[0]));
	}

	private void calculateCrossSection(Long disk, double width, double height) {
		String crossSection = (-(width / 2) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", "
				+ ((width / 2) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height));
		connector.executeWrite("MATCH (n)  WHERE ID(n) = " + disk + " SET n.crossSection = \'" + crossSection + "\'");
	}

	private void calculateSpines(List<Node> segments, double factor) {
		if (outputFormat == OutputFormat.X3D) {
			int spinePointCount;
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
			int start;
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
			connector.executeWrite(statementList.toArray(new String[0]));

		}
		if (outputFormat == OutputFormat.AFrame) {
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

	private StatementResult getDisks() {
		return connector.executeRead("MATCH (n:Model:RD)-[:CONTAINS*]->(d:Disk)-[:VISUALIZES]->(element) " + "RETURN d "
				+ "ORDER BY element.hash");
	}

	/*private Iterator<Node> getSubDisks(Long entity) {
		return connector
				.executeRead("MATCH (n)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) " + "WHERE ID(n) = " + entity
						+ " " + "RETURN d " + "ORDER BY element.hash")
				.stream().map(s -> s.get("d").asNode()).collect(Collectors.toList()).listIterator();
	}*/

	/*private Iterator<Node> getRootDisks() {
		return connector
				.executeRead("MATCH (n:RD:Model)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) " + "RETURN d "
						+ "ORDER BY element.hash")
				.stream().map(s -> s.get("d").asNode()).collect(Collectors.toList()).listIterator();
	}*/

	private static String removeBrackets(List<String> list) {
		return removeBrackets(list.toString());
	}


	private static String removeBrackets(String string) {
		return StringUtils.remove(StringUtils.remove(string, "["), "]");
	}

	private void createDisksList() {
		disksList.addAll(rootDisksList);
		StatementResult result = connector.executeRead("MATCH (p:Disk)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) "
				+ "RETURN d AS d, d.grossArea AS grossArea, d.netArea AS netArea, d.ringWidth AS ringWidth, d.height as height," +
				" ID(d) AS id, ID(p) AS parentID ORDER BY element.hash");
		result.forEachRemaining(d -> {
			double grossArea = d.get("grossArea").asDouble(0.0);
			double netArea = d.get("netArea").asDouble(0.0);
			Disk disk = new Disk(d.get("d").asNode(), d.get("parentID").asLong(), d.get("id").asLong(), grossArea, netArea,
					d.get("ringWidth").asDouble(), d.get("height").asDouble());
			disksList.add(disk);
		});
	}

	private void createRootDisksList() {
		StatementResult result = connector
				.executeRead("MATCH (n:RD:Model)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) " + "RETURN d "
						+ " AS d, d.grossArea AS grossArea,d.netArea AS netArea, d.ringWidth AS ringWidth, d.height AS height," +
						" ID(d) as id, ID(n) AS parentID ORDER BY element.hash");
		result.forEachRemaining(d -> {
			double grossArea = d.get("grossArea").asDouble(0.0);
			double netArea = d.get("netArea").asDouble(0.0);
			Disk disk = new Disk(d.get("d").asNode(), d.get("parentID").asLong(), d.get("id").asLong(), grossArea, netArea,
					d.get("ringWidth").asDouble(), d.get("height").asDouble());
			rootDisksList.add(disk);
		});
	}

	private void createFieldsList() {
		StatementResult result = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(field:Field) " +
						"RETURN d AS d, d.size AS size, ID(d) as id, ID(n) as parentID ORDER BY field.hash");
		result.forEachRemaining(f -> {
			DiskSegment field = new DiskSegment(f.get("d").asNode(), f.get("parentID").asLong(), f.get("id").asLong(),
			f.get("size").asDouble());
			fields.add(field);
		});
	}

	private void createMethodsList() {
		StatementResult result = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(method:Method) " +
						"RETURN d AS d, d.size AS size, ID(d) as id, ID(n) as parentID ORDER BY method.hash");
		result.forEachRemaining(f -> {
			DiskSegment method = new DiskSegment(f.get("d").asNode(), f.get("parentID").asLong(), f.get("id").asLong(),
					f.get("size").asDouble());
			methods.add(method);
		});
	}

	static ArrayList<Disk> getSubDisk(long id) {
		ArrayList<Disk> list = new ArrayList<>();
		disksList.forEach(d -> {
			long parentID = d.getParentID();
			if (id == parentID) {
				list.add(d);
			}
		});
		return list;
	}

	static ArrayList<DiskSegment> getDiskSegment(long id, ArrayList<DiskSegment> diskSegments) {
		ArrayList<DiskSegment> list = new ArrayList<>();
		diskSegments.forEach(d -> {
			long parentID = d.getParentID();
			if (id == parentID) {
				list.add(d);
			}
		});
		return list;
	}

	public static ArrayList<DiskSegment> getFields() {
	    return fields;
    }

    public static ArrayList<DiskSegment> getMethods() {
        return methods;
    }

    public static double sum(ArrayList<DiskSegment> list) {
		double sum = 0.0;
		for(DiskSegment segment: list) {
			sum += segment.getSize();
		}
		return sum;
	}
}
