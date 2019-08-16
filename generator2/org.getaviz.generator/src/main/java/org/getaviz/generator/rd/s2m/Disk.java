package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

import java.util.ArrayList;

public class Disk implements RDElement{

    private double height;
    private double transparency;
    private double ringWidth;
    private double areaWithoutBorder;
    private double outerSegmentsArea;
    private double innerSegmentsArea;
    private double radius;
    private double areaWithBorder;
    private double posX;
    private double posY;
    private double posZ;
    private int maxLevel;
    private String color;
    private String crossSection;
    private String spine;
    private long parentVisualizedNodeID;
    private long visualizedNodeID;
    private long parentID;
    private long id;
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

    public void writeToDatabase(DatabaseConnector connector, String source) {
        switch (source) {
            case "JQA2RD" :
                JQA2RDWriteToDatabase(connector);
                break;
            case "RD2RD" :
                RD2RDWriteToDatabase(connector);
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
        String createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", posX, posY, posZ);
        connector.executeWrite(updateNode + createPosition);
    }

    public void calculateAreaWithoutBorder(double dataFactor) {
        double innerSegmentsSum = calculateDiskSegmentSizeSum(innerSegments, dataFactor);
        double outerSegmentsSum = calculateDiskSegmentSizeSum(outerSegments, dataFactor);
        areaWithoutBorder = innerSegmentsSum + outerSegmentsSum;
    }

    public void calculateRadius() {
        radius = Math.sqrt(areaWithoutBorder / Math.PI) + ringWidth;
    }

    public void calculateSum()  {
        outerSegmentsArea = sum(outerSegments) / areaWithoutBorder;
        innerSegmentsArea = sum(innerSegments) / areaWithoutBorder;
        calculateDiskSegmentSum(innerSegments);
        calculateDiskSegmentSum(outerSegments);
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

    public void setRadius (double radius) { this.radius = radius; }

    public void setAreaWithBorder(double areaWithBorder) { this.areaWithBorder = areaWithBorder; }

    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }

    public void setPosition(double posX, double posY, double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public void setCrossSection (String crossSection) { this.crossSection = "\'" +  crossSection + "\'"; }

    public void setSpine (String spine) { this.spine = spine;}

    public void setColor (String color) {this.color = color;}

    public void setInnerSegmentsList(ArrayList<DiskSegment> list) {
        innerSegments.addAll(list);
    }

    public void setOuterSegmentsList(ArrayList<DiskSegment> list) {
        outerSegments.addAll(list);
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

    public double getAreaWithoutBorder() { return areaWithoutBorder; }

    public double getRingWidth() {
        return ringWidth;
    }

    public double getRadius() { return radius; }

    public double getAreaWithBorder() { return areaWithBorder; }

    public double getHeight() { return height; }

    public double getOuterSegmentsArea() { return outerSegmentsArea; }

    public double getInnerSegmentsArea() {
        return innerSegmentsArea;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public Double getPosZ() {return posZ;}

    public ArrayList<DiskSegment> getInnerSegments() {
       return innerSegments;
    }

    public ArrayList<DiskSegment> getOuterSegments(){
       return outerSegments;
    }

    private String propertiesToString() {
        return String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
    }
}
