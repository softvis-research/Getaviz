package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.types.Node;

public class Disk implements RDElement{

    private double height;
    private double transparency;
    private double ringWidth;
    private double netArea;
    private double methodArea;
    private double dataArea;
    private double radius;
    private double grossArea;
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

   Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency) {
        this.visualizedNodeID = visualizedNodeId;
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.ringWidth = ringWidth;
        this.height = height;
        this.transparency = transparency;
    }

    Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency, String color) {
        this(visualizedNodeId, parentVisualizedNodeID, ringWidth, height, transparency);
        this.color = color;
    }

    public Disk(long visualizedNodeID, long parentID, long id, double grossArea, double netArea, double ringWidth, double height) {
       this.visualizedNodeID = visualizedNodeID;
       this.parentID = parentID;
       this.id = id;
       this.grossArea = grossArea;
       this.netArea = netArea;
       this.ringWidth = ringWidth;
       this.height = height;
    }

    public void JQA2RDWriteToDatabase(DatabaseConnector connector) {
       String label = Labels.Disk.name();
       long id = connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, label, propertiesToString()), "n").id();
       setId(id);
    }

    public void RD2RDWriteToDatabase(DatabaseConnector connector) {
        String updateNode = String.format(
                "MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.netArea = %f, n.grossArea = %f, n.methodArea = %f, " +
                        "n.dataArea = %f, n.maxLevel = %d, n.crossSection = " + crossSection +  ", n.spine = "
                        + spine + ", n.color = " + color + " ", id, radius, netArea, grossArea, methodArea, dataArea, maxLevel);
        String createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", posX, posY, posZ);
        connector.executeWrite(updateNode + createPosition);
    }

    public void setParentID(long id) {
        this.parentID = id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public void setNetArea(double netArea) {
        this.netArea = netArea;
    }

    public void setRadius (double radius) { this.radius = radius; }

    public void setGrossArea(double grossArea) { this.grossArea = grossArea; }

    public void setDataArea(double dataArea) {
        this.dataArea = dataArea;
    }

    public void setMethodArea(double methodArea) {
        this.methodArea = methodArea;
    }

    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }

    public void setPosition(double posX, double posY, double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public void setCrossSection (String crossSection) { this.crossSection = "\'" +  crossSection + "\'"; }

    public void setSpine (String spine) { this.spine = spine;}

    public void setColor (String color) {this.color = color;}

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

    public double getNetArea() { return netArea; }

    public double getRingWidth() {
        return ringWidth;
    }

    public double getRadius() { return radius; }

    public double getGrossArea() { return grossArea; }

    public double getHeight() { return height; }

    public double getMethodArea() { return  methodArea; }

    public double getDataArea() {
        return dataArea;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public Double getPosZ() {return posZ;}

    private String propertiesToString() {
        return String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
    }
}
