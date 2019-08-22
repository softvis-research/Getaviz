package org.getaviz.generator.rd.s2m;

import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;;

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

    public void calculateSpines(double factor) {
        int spinePointCount = 50;
        List<String> completeSpine = new ArrayList<>();
        double stepX = 2 * Math.PI / spinePointCount;
        for (int i = 0; i < spinePointCount; ++i) {
            completeSpine.add(factor * Math.cos(i * stepX) + " " + factor * Math.sin(i * stepX) + " " + 0.0);
        }
        completeSpine.add(completeSpine.get(0));
        spine = "\'" + removeBrackets(completeSpine) + "\'";
    }

     private String calculateCrossSection() {
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

    public void setSegmentsArea() {
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
