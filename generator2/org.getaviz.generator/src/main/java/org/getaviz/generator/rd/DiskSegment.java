package org.getaviz.generator.rd;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

public class DiskSegment implements RDElement {

    private double size;
    private double height;
    private double transparency;
    private double innerRadius;
    private double outerRadius;
    private double angle;
    private double anglePosition;
    private String color;
    private String crossSection;
    private String spine;
    private long visualizedNodeID;
    private long parentVisualizedNodeID;
    private long parentID;
    private long id;

    public DiskSegment(long visualizedNodeID, long parentVisualizedNodeID, double height, double transparency, String color) {
        this.visualizedNodeID = visualizedNodeID;
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.size = 1.0;
        this.transparency = transparency;
        this.height = height;
        this.color = color;
    }

    public DiskSegment(long visualizedNodeID, long parentVisualizedNodeID, double height, double transparency, double minArea, String color,
                int numberOfStatements) {
        this(visualizedNodeID, parentVisualizedNodeID, transparency, height, color);

        size = numberOfStatements;
        if (numberOfStatements <= minArea) {
            size = minArea;
        }
    }

    public DiskSegment(long parentID, long id, double size, double dataFactor) {
        this.parentID = parentID;
        this.id = id;
        this.size = size * dataFactor;
    }

    void calculateSize(double sum) {
        size = size / sum;
    }

    public void createNode(DatabaseConnector connector) {
        String label = Labels.DiskSegment.name();
        long id = connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, label, propertiesToString()), "n").id();
        setID(id);
    }

    public void updateNode(DatabaseConnector connector) {
        connector.executeWrite("MATCH (s) WHERE ID(s) = " + id + " SET s.outerRadius = " +
                outerRadius + ", s.innerRadius = " + innerRadius + ", s.angle = " + angle + ", s.anglePosition = " +
                anglePosition + ", s.spine = " + spine + ", s.crossSection =  " + crossSection + " ");
    }

    private String propertiesToString() {
        return String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", size, height,
                transparency, color);
    }

    public void setCrossSection(String crossSection) {
        this.crossSection = crossSection;
    }

    public void setParentID(long newParentID) {
        this.parentID = newParentID;
    }

    public long getParentID() {
        return parentID;
    }

    public void setID(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public double getSize() { return size; }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() { return angle; }

    public void setOuterRadius(double outerRadius) {
        this.outerRadius = outerRadius;
    }

    public void setInnerRadius(double innerRadius) {
        this.innerRadius = innerRadius;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public void setSpine(String spine) {
        this.spine = spine;
    }

    public void setAnglePosition(double anglePosition) {
        this.anglePosition = anglePosition;
    }

    public long getParentVisualizedNodeID() {
        return parentVisualizedNodeID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }
}
