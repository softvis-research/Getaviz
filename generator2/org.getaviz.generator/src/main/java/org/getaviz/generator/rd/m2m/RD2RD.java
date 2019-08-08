package org.getaviz.generator.rd.m2m;

import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.ColorGradient;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;

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
import org.neo4j.driver.v1.types.Node;
import java.util.List;
import org.neo4j.driver.v1.StatementResult;

public class RD2RD implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(RD2RD.class);
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

		String NS_colorStart = "#969696";
		String NS_colorEnd = "#F0F0F0";
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

		createLists();
		calculateNetArea(rootDisksList);
		calculateRadius(disksList);
		calculateLayout(rootDisksList);
		postLayout(disksList);
		postLayout2(disksList);

		writeToDatabase();

		log.info("RD2RD finished");
	}

	private String setNamespaceColor(int level) {
		return NS_colors.get(level - 1);
	}

	private void calculateNetArea(ArrayList<Disk> list) {
		list.forEach((disk) -> {
			long id = disk.getId();
			ArrayList<Disk> subDisks = getSubDisk(id);
			calculateNetArea(subDisks);
			calculateNetArea(disk);
		});
	}

	private void calculateNetArea(Disk disk) {
		double netArea;
		long id = disk.getId();
		ArrayList<DiskSegment> sizeFields = getDiskSegment(id, fields);
		ArrayList<DiskSegment> sizeMethods = getDiskSegment(id, methods);
		double dataSum = calculateDiskSegmentSizeSum(sizeFields);
		double methodSum = calculateDiskSegmentSizeSum(sizeMethods);
		netArea = dataSum + methodSum;
		disk.setNetArea(netArea);;
	}

	private void calculateRadius(ArrayList<Disk> list) {
		list.forEach(disk -> {
					double netArea = disk.getNetArea();
					double ringWidth = disk.getRingWidth();
					double radius = Math.sqrt(netArea / Math.PI) + ringWidth;
					disk.setRadius(radius);
		});
	}

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

	private void postLayout(ArrayList<Disk> list) {
		list.forEach(disk -> {
			ArrayList<DiskSegment> data = getDiskSegment(disk.getId(), fields);
			ArrayList<DiskSegment> method = getDiskSegment(disk.getId(), methods);
			fractions(disk, data, method);
			fractions(data);
			fractions(method);
		});
	}

	private void postLayout2(ArrayList<Disk> list) {
		list.forEach(disk->{
			ArrayList<Disk> subDisks = getSubDisk(disk.getId());
				subDisks.forEach(this::calculateRings);
			calculateRings(disk);
		});
	}

	private void fractions(Disk disk, ArrayList<DiskSegment> data, ArrayList<DiskSegment> method) {
		double netArea = disk.getNetArea();
		double currentMethodArea = sum(method) / netArea;
		double currentDataArea = sum(data) / netArea;
		disk.setMethodArea(currentMethodArea);
		disk.setDataArea(currentDataArea);
	}

	private void fractions(ArrayList<DiskSegment> list) {
		double sum = sum(list);
		for (DiskSegment segment : list) {
			double newSize = segment.getSize()/ sum;
			segment.setSize(newSize);
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
		ArrayList<Disk> subDisks = getSubDisk(id);
		if (subDisks.size() == 0) {
			double r_data = Math.sqrt(dataArea * netArea / Math.PI);
			double r_methods = radius - ringWidth;
			double b_methods = r_methods - r_data;
			ArrayList<DiskSegment> diskMethods = getDiskSegment(id, methods);
			ArrayList<DiskSegment> diskData = getDiskSegment(id, fields);
			if (!diskMethods.isEmpty()) {
				diskMethods.forEach(method -> calculateCrossSection(method.getId(), b_methods, height));
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods);
				if (outputFormat == OutputFormat.AFrame) {
					for (DiskSegment method : diskMethods) {
						method.setOuterAndInnerRadius(r_methods, r_data);
					}
				}
			}
			if (!diskData.isEmpty()) {
				diskData.forEach(field -> calculateCrossSection(field.getId(), r_data, height));
				//calculateCrossSection(diskData, r_data, height);
				calculateSpines(diskData, 0.5 * r_data);
				if (outputFormat == OutputFormat.AFrame) {
					for (DiskSegment data : diskData) {
						data.setOuterAndInnerRadius(r_data, 0.0);
					}
				}
			}
		} else {
			double outerRadius = calculateOuterRadius(disk);
			double r_data = Math.sqrt((dataArea * netArea / Math.PI) + (outerRadius * outerRadius));
			double b_data = r_data - outerRadius;
			double r_methods = Math.sqrt((methodArea * netArea / Math.PI) + (r_data * r_data));
			double b_methods = r_methods - r_data;
			ArrayList<DiskSegment> diskMethods = getDiskSegment(id, methods);
			if (!diskMethods.isEmpty()) {
				diskMethods.forEach(method -> calculateCrossSection(method.getId(), b_methods, height) );
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods);
				if (outputFormat == OutputFormat.AFrame) {
					for (DiskSegment method : diskMethods) {
						method.setOuterAndInnerRadius(r_methods, r_data);
					}
				}
			}
			ArrayList<DiskSegment> diskData = getDiskSegment(id, fields);
			if (!diskData.isEmpty()) {
				diskData.forEach(field -> calculateCrossSection(field.getId(), b_data, height));
				calculateSpines(diskData, r_data - 0.5 * b_data);
				if (outputFormat == OutputFormat.AFrame) {
					for (DiskSegment data : diskData) {
						data.setOuterAndInnerRadius(r_data, (r_data - b_data));
					}
				}
			}
		}
	}

	private double calculateOuterRadius(Disk disk) {
		CoordinateList coordinates = new CoordinateList();
		ArrayList<Disk> subDisks = getSubDisk(disk.getId());
		subDisks.forEach(d -> {
			double x = disk.getPosX();
			double y = disk.getPosY();
			coordinates.add(createCircle(x, y, disk.getRadius()).getCoordinates(), false);
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

	private void calculateCrossSection(Long disk, double width, double height) {
		String crossSection = (-(width / 2) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", "
				+ ((width / 2) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height));
		connector.executeWrite("MATCH (n)  WHERE ID(n) = " + disk + " SET n.crossSection = \'" + crossSection + "\'");
	}

	private void calculateSpines(ArrayList<DiskSegment> segments, double factor) {
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
			for (DiskSegment segment : segments) {
				double size = segment.getSize();
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
				statementList.add("MATCH (n) WHERE ID(n) = " + segment.getId() + " SET n.spine = \'"
						+ removeBrackets(partSpine) + "\'");
			}
			connector.executeWrite(statementList.toArray(new String[0]));

		}
		if (outputFormat == OutputFormat.AFrame) {
			if (!segments.isEmpty()) {
				int length = segments.size();
				double sizeSum = 0.0;
				double position = 0.0;
				for (DiskSegment segment : segments) {
					double size = segment.getSize();
					sizeSum += size;
				}
				sizeSum += sizeSum / 360 * length;
				for (DiskSegment segment : segments) {
					double angle = (segment.getSize() / sizeSum) * 360;
					connector.executeWrite("MATCH (n) WHERE ID(n) = " + segment.getId() + " SET n.angle = " + angle
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
				" d.size AS size, ID(d) AS id, ID(p) AS parentID ORDER BY element.hash");
		result.forEachRemaining(d -> {
			double grossArea = d.get("grossArea").asDouble(0.0);
			double netArea = d.get("netArea").asDouble(0.0);
			Disk disk = new Disk (d.get("parentID").asLong(), d.get("id").asLong(), grossArea, netArea,
					d.get("ringWidth").asDouble(), d.get("height").asDouble());
			disksList.add(disk);
		});
	}

	private void createRootDisksList() {
		StatementResult result = connector
				.executeRead("MATCH (n:RD:Model)-[:CONTAINS]->(d:Disk)-[:VISUALIZES]->(element) " + "RETURN d "
						+ " AS d, d.grossArea AS grossArea,d.netArea AS netArea, d.ringWidth AS ringWidth, d.height AS height," +
						" d.size AS size, ID(d) as id, ID(n) AS parentID ORDER BY element.hash");
		result.forEachRemaining(d -> {
			double grossArea = d.get("grossArea").asDouble(0.0);
			double netArea = d.get("netArea").asDouble(0.0);
			Disk disk = new Disk(d.get("parentID").asLong(), d.get("id").asLong(), grossArea, netArea,
					d.get("ringWidth").asDouble(), d.get("height").asDouble());
			rootDisksList.add(disk);
		});
	}

	private void createFieldsList() {
		StatementResult result = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(field:Field) " +
						"RETURN d AS d, d.size AS size, ID(d) as id, ID(n) as parentID ORDER BY field.hash");
		result.forEachRemaining(f -> {
			DiskSegment field = new DiskSegment(f.get("parentID").asLong(), f.get("id").asLong(),f.get("size").asDouble());
			fields.add(field);
		});
	}

	private void createMethodsList() {
		StatementResult result = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(method:Method) " +
						"RETURN d AS d, d.size AS size, ID(d) as id, ID(n) as parentID ORDER BY method.hash");
		result.forEachRemaining(f -> {
			DiskSegment method = new DiskSegment(f.get("parentID").asLong(), f.get("id").asLong(),f.get("size").asDouble());
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

	private void createLists() {
		createRootDisksList();
		createDisksList();
		createFieldsList();
		createMethodsList();
	}

	private void writeToDatabase() {
		String write;
		fields.forEach(field-> field.RD2RDWriteToDatabase(connector));
		methods.forEach(methods -> methods.RD2RDWriteToDatabase(connector));
		disksList.forEach(disk -> disk.RD2RDWriteToDatabase(connector));
	}

	static ArrayList<DiskSegment> getFields() {
	    return fields;
    }

    static ArrayList<DiskSegment> getMethods() {
        return methods;
    }

    static double sum(ArrayList<DiskSegment> list) {
		double sum = 0.0;
		for(DiskSegment segment: list) {
			sum += segment.getSize();
		}
		return sum;
	}

	private double calculateDiskSegmentSizeSum (ArrayList<DiskSegment> list) {
		double sizeSum = 0.0;
		for (DiskSegment segment : list) {
			double newSize = segment.getSize() * dataFactor;
			sizeSum += newSize;
			segment.setSize(newSize);
		}
		return sizeSum;
	}
}
