package org.getaviz.generator.rd.s2m;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;

import java.util.ArrayList;
import java.util.List;

public class Disk implements RDElement {

    private double height;
    private double transparency;
    private double ringWidth;
    private double areaWithoutBorder;
    private double outerSegmentsArea;
    private double innerSegmentsArea;
    private double radius;
    private double areaWithBorder;
    private Position position;
    private int maxLevel;
    private String color;
    private String crossSection;
    private String spine;
    private long parentVisualizedNodeID;
    private long visualizedNodeID;
    private long parentID;
    private long id;
    private ArrayList<Disk> subDisksList = new ArrayList<>();
    private ArrayList<DiskSegment> innerSegments = new ArrayList<>();
    private ArrayList<DiskSegment> outerSegments = new ArrayList<>();

    private Disk(long visualizedNodeId, double ringWidth, double height) {
        this.visualizedNodeID = visualizedNodeId;
        this.ringWidth = ringWidth;
        this.height = height;
    }

    Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency) {
        this(visualizedNodeId, ringWidth, height);
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.transparency = transparency;
    }

    Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency,
         String color) {
        this(visualizedNodeId, parentVisualizedNodeID, ringWidth, height, transparency);
        this.color = color;
    }

    public Disk(long visualizedNodeID, long parentID, long id, double areaWithBorder, double areaWithoutBorder, double ringWidth,
                double height) {
        this(visualizedNodeID, ringWidth, height);
        this.parentID = parentID;
        this.id = id;
        this.areaWithBorder = areaWithBorder;
        this.areaWithoutBorder = areaWithoutBorder;
    }

    public void writeToDatabase(DatabaseConnector connector, boolean wroteToDatabase) {
        if (wroteToDatabase) {
            RD2RDWriteToDatabase(connector);
        } else {
            JQA2RDWriteToDatabase(connector);
        }
    }

    private void JQA2RDWriteToDatabase(DatabaseConnector connector) {
        String label = Labels.Disk.name();
        long id = connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, label, propertiesToString()), "n").id();
        setId(id);
    }

    private void RD2RDWriteToDatabase(DatabaseConnector connector) {
        String updateNode = String.format(
                "MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.netArea = %f, n.grossArea = %f, n.methodArea = %f, " +
                        "n.dataArea = %f, n.maxLevel = %d, n.crossSection = %s, n.spine = %s, n.color = %s ", id, radius,
                areaWithoutBorder, areaWithBorder, outerSegmentsArea, innerSegmentsArea, maxLevel, crossSection, spine, color);
        String createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", position.x,
                position.y, position.z);
        connector.executeWrite(updateNode + createPosition);
    }

    public void calculateRings(OutputFormat outputFormat) {
        double r_data;
        double r_methods;
        double b_methods;
        double width;
        double factor;
        double innerRadius;
        if (ringWidth == 0) {
            calculateCrossSection(ringWidth, 0);
        } else {
            calculateCrossSection(ringWidth, height);
        }
        calculateSpines(radius - (0.5 * ringWidth));
        if (subDisksList.size() == 0) {
            r_data = Math.sqrt(innerSegmentsArea * areaWithoutBorder / Math.PI);
            r_methods = radius - ringWidth;
            b_methods = r_methods - r_data;
            width = r_data;
            factor = 0.5 * r_data;
            innerRadius = 0.0;
        } else {
            double outerRadius = calculateOuterRadius();
            r_data = Math.sqrt((innerSegmentsArea * areaWithoutBorder / Math.PI) + (outerRadius * outerRadius));
            double b_data = r_data - outerRadius;
            r_methods = Math.sqrt((outerSegmentsArea * areaWithoutBorder / Math.PI) + (r_data * r_data));
            b_methods = r_methods - r_data;
            width = b_data;
            factor = r_data - 0.5 * b_data;
            innerRadius = r_data - b_data;
        }
        if (!innerSegments.isEmpty()) {
            updateDiskSegment(innerSegments, width, factor, outputFormat, r_data, innerRadius);
        }
        if (!outerSegments.isEmpty()) {
            updateDiskSegment(outerSegments, width, r_methods - 0.5 * b_methods, outputFormat, r_methods, r_data);
        }
    }

    private void updateDiskSegment(ArrayList<DiskSegment> list, double width, double factor, OutputFormat outputFormat,
                                   double outer, double inner) {
        list.forEach(segment -> segment.calculateCrossSection(width, height));
        if (outputFormat == SettingsConfiguration.OutputFormat.X3D) {
            calculateSpines(list, factor);
        }
        if (outputFormat == SettingsConfiguration.OutputFormat.AFrame) {
            calculateAngle(list);
            for (DiskSegment data : list) {
                data.setOuterAndInnerRadius(outer, inner);
            }
        }
    }

    private void calculateSpines(ArrayList<DiskSegment> segments, double factor) {
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
            String spine = " \'" + removeBrackets(partSpine) + "\'";
            segment.setSpine(spine);
        }
    }

    private void calculateSpines(double factor) {
        int spinePointCount = 50;
        List<String> completeSpine = new ArrayList<>();
        double stepX = 2 * Math.PI / spinePointCount;
        for (int i = 0; i < spinePointCount; ++i) {
            completeSpine.add(factor * Math.cos(i * stepX) + " " + factor * Math.sin(i * stepX) + " " + 0.0);
        }
        completeSpine.add(completeSpine.get(0));
        spine = "\'" + removeBrackets(completeSpine) + "\'";
    }

    private void calculateAngle(ArrayList<DiskSegment> segments) {
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
                segment.setAngle(angle);
                segment.setAnglePosition(position);
                position += angle + 1;
            }
        }
    }

    private void calculateCrossSection(double width, double height) {
        crossSection = "\'" + (-(width / 2) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", "
                + ((width / 2) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height)) +
                "\'";
    }

    private double calculateOuterRadius() {
        CoordinateList coordinates = new CoordinateList();
        subDisksList.forEach(d -> coordinates.add(createCircle(position.x, position.y, radius)
                .getCoordinates(), false));
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

    private static String removeBrackets(List<String> list) {
        return removeBrackets(list.toString());
    }

    private static String removeBrackets(String string) {
        return StringUtils.remove(StringUtils.remove(string, "["), "]");
    }

    public void calculateAreaWithoutBorder(double dataFactor) {
        double innerSegmentsSum = calculateDiskSegmentSizeSum(innerSegments, dataFactor);
        double outerSegmentsSum = calculateDiskSegmentSizeSum(outerSegments, dataFactor);
        areaWithoutBorder = innerSegmentsSum + outerSegmentsSum;
    }

    public void calculateRadius() {
        radius = Math.sqrt(areaWithoutBorder / Math.PI) + ringWidth;
    }

    private double calculateSum(ArrayList<DiskSegment> list) {
        return sum(list) / areaWithBorder;
    }

    private void calculateDiskSegmentSum(ArrayList<DiskSegment> list) {
        double sum = sum(list);
        for (DiskSegment segment : list) {
            segment.calculateSize(sum);
        }
    }

    private double calculateDiskSegmentSizeSum(ArrayList<DiskSegment> list, double dataFactor) {
        double sizeSum = 0.0;
        for (DiskSegment segment : list) {
            double newSize = segment.calculateNewSize(dataFactor);
            sizeSum += newSize;
        }
        return sizeSum;
    }

    public static double sum(ArrayList<DiskSegment> list) {
        double sum = 0.0;
        for (DiskSegment segment : list) {
            sum += segment.getSize();
        }
        return sum;
    }

    public void setParentID(long id) {
        this.parentID = id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAreaWithoutBorder(double areaWithoutBorder) {
        this.areaWithoutBorder = areaWithoutBorder;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setAreaWithBorder(double areaWithBorder) {
        this.areaWithBorder = areaWithBorder;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setPosition(double x, double y, double z) {
        position = new Position(x, y, z);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSubDisksList(ArrayList<Disk> list) {
        subDisksList.addAll(list);
    }

    public void setInnerSegmentsList(ArrayList<DiskSegment> list) {
        innerSegments.addAll(list);
    }

    public void setOuterSegmentsList(ArrayList<DiskSegment> list) {
        outerSegments.addAll(list);
    }

    public void setSegmentsArea() {
        innerSegmentsArea = calculateSum(innerSegments);
        outerSegmentsArea = calculateSum(outerSegments);
        calculateDiskSegmentSum(innerSegments);
        calculateDiskSegmentSum(outerSegments);

    }

    String getCrossSection() {
        return crossSection;
    }

    String getSpine() {
        return spine;
    }

    public long getParentVisualizedNodeID() {
        return parentVisualizedNodeID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }

    public long getId() {
        return id;
    }

    public long getParentID() {
        return parentID;
    }

    public double getAreaWithoutBorder() {
        return areaWithoutBorder;
    }

    public double getRingWidth() {
        return ringWidth;
    }

    public double getRadius() {
        return radius;
    }

    public double getAreaWithBorder() {
        return areaWithBorder;
    }

    public double getHeight() {
        return height;
    }

    double getOuterSegmentsArea() {
        return outerSegmentsArea;
    }

    double getInnerSegmentsArea() {
        return innerSegmentsArea;
    }

    public ArrayList<DiskSegment> getInnerSegments() {
        return innerSegments;
    }

    public ArrayList<DiskSegment> getOuterSegments() {
        return outerSegments;
    }

    public ArrayList<Disk> getSubDisksList() {
        return subDisksList;
    }

    public Position getPosition() {
        return position;
    }

    private String propertiesToString() {
        return String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
    }

}
