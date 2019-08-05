package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.types.Node;

public class Disk implements RDElement{

    private double height;
    private double transparency;
    private double ringWidth;
    private double netArea;
    private String color;
    private long parentVisualizedNodeID;
    private long visualizedNodeID;
    private long parentID;
    private long id;
    private Node node;

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

    public Disk(Node node, long id) {
       this.node = node;
       this.id = id;
    }

    public Disk(Node node, long id, double netArea, double ringWidth) {
       this.node = node;
       this.id= id;
       this.netArea = netArea;
       this.ringWidth = ringWidth;
    }

    public void writeToDatabase(DatabaseConnector connector) {
       String label = Labels.Disk.name();
       long id = connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, label, propertiesToString()), "n").id();
       setId(id);
    }

    public long getParentVisualizedNodeID() {
        return this.parentVisualizedNodeID;
    }

    public long getVisualizedNodeID() {
        return this.visualizedNodeID;
    }

    public long getId() {
        return id;
    }

    public double getNetArea() {
       return this.netArea;
    }

    public double getRingWidth() {
        return ringWidth;
    }

    public Node getNode() {
       return this.node;
    }

    private String propertiesToString() {
        return String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
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
}
