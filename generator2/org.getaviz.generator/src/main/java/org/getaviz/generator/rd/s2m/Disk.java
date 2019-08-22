package org.getaviz.generator.rd.s2m;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.getaviz.generator.rd.m2m.RDLayout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Disk implements RDElement, Comparable<Disk> {

    private double height;
    private double transparency;
    private double ringWidth;
    private double areaWithBorder;
    private double areaWithoutBorder;
    private double radius = 0;
    private double minArea = 0;
    private Point2D.Double centre = new Point2D.Double(0, 0);
    private Position position;
    private String color;
    private String spine;
    private long parentVisualizedNodeID;
    private long visualizedNodeID;
    private long parentID;
    private long id;
    private boolean nesting;
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
                double height, boolean nesting) {
        this(visualizedNodeID, ringWidth, height);
        this.parentID = parentID;
        this.id = id;
        this.areaWithBorder = areaWithBorder;
        this.areaWithoutBorder = areaWithoutBorder;
        this.nesting = nesting;
        //String serial = visualizedNodeID + "";
    }

    public int compareTo(Disk disk)  {
        return java.lang.Double.compare(disk.getAreaWithoutBorder(), areaWithoutBorder);
    }

    private String propertiesToString() {
        return String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
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
        setID(id);
    }

    private void RD2RDWriteToDatabase(DatabaseConnector connector) {
        String crossSection = calculateCrossSection();
        String updateNode = String.format(
                "MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.crossSection = %s, n.spine = %s, n.color = %s ", id,
                radius, crossSection, spine, color);
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
        double innerSegmentsArea = calculateSum(innerSegments);
        double outerSegmentsArea = calculateSum(outerSegments);
        setSegmentsArea();
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

            double position = 0.0;
            double sizeSum = sum(segments);
            sizeSum += sizeSum / 360 * length;
            for (DiskSegment segment : segments) {
                double angle = (segment.getSize() / sizeSum) * 360;
                segment.setAngle(angle);
                segment.setAnglePosition(position);
                position += angle + 1;
            }
        }
    }

     String calculateCrossSection() {
        double crossHeight;
        if (ringWidth == 0) {
            crossHeight = 0.0;
        } else {
            crossHeight = this.height;
        }
        return "\'" + (-(ringWidth / 2) + " " + (crossHeight)) + ", " + ((ringWidth / 2) + " " + (crossHeight)) + ", "
                + ((ringWidth / 2) + " " + 0) + ", " + (-(ringWidth / 2) + " " + 0) + ", " + (-(ringWidth / 2) + " " +
                (crossHeight)) + "\'";
    }

    private double calculateOuterRadius() {
        CoordinateList coordinates = new CoordinateList();
        for (Disk d : subDisksList) {
            coordinates.add(RDLayout.createCircle(d.position.x, d.position.y, d.radius)
                    .getCoordinates(), false);
        }
        GeometryFactory geoFactory = new GeometryFactory();
        MultiPoint innerCircleMultiPoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray());
        MinimumBoundingCircle mbc = new MinimumBoundingCircle(innerCircleMultiPoint);
        return mbc.getRadius();
    }

    private static String removeBrackets(List<String> list) {
        return removeBrackets(list.toString());
    }

    private static String removeBrackets(String string) {
        return StringUtils.remove(StringUtils.remove(string, "["), "]");
    }

    public void calculateAreaWithoutBorder(double dataFactor) {
        innerSegments.forEach(segment -> segment.calculateNewSize(dataFactor));
        outerSegments.forEach(segment -> segment.calculateNewSize(dataFactor));
        areaWithoutBorder = sum(innerSegments) + sum(outerSegments);
    }

    public void updateDiskNode() {
        double oldZPosition = 0.0;
        if (position != null) {
            oldZPosition = position.z;
        }
        setPosition(centre.x, centre.y, oldZPosition);
        for (Disk disk : subDisksList) {
            disk.updateDiskNode();
        }
    }

    public void calculateRadius() {
        radius = Math.sqrt(areaWithoutBorder / Math.PI) + ringWidth;
    }

    private double calculateSum(ArrayList<DiskSegment> list) {
        return sum(list) / areaWithBorder;
    }

    private void updateDiskSegmentSize(ArrayList<DiskSegment> list) {
        double sum = sum(list);
        for (DiskSegment segment : list) {
            segment.calculateSize(sum);
        }
    }

    public static double sum(ArrayList<DiskSegment> list) {
        double sum = 0.0;
        for (DiskSegment segment : list) {
            sum += segment.getSize();
        }
        return sum;
    }

    private void setSegmentsArea() {
        updateDiskSegmentSize(innerSegments);
        updateDiskSegmentSize(outerSegments);
    }

    public void setMinArea() {
        if (nesting) {
            minArea = Disk.sum(outerSegments) + Disk.sum(innerSegments);
        } else {
            minArea = areaWithoutBorder;
        }
    }

    public void setParentID(long id) {
        this.parentID = id;
    }

    public void setID(long id) {
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

    public void setPosition(double x, double y, double z) {
        this.position = new Position(x, y, z);
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

    public void setCentre(Point2D.Double centre) {
        this.centre = centre;
    }

    String getSpine() {
        return spine;
    }

    public double getMinArea() {
        return minArea;
    }

    public Point2D.Double getCentre() {
        return  centre;
    }

    public long getParentVisualizedNodeID() {
        return parentVisualizedNodeID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }

    public long getID() {
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
}
