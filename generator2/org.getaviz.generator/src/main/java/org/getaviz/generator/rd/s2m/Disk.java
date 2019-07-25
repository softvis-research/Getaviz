package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

public class Disk implements RDElement{

    private long parentVisualizedNodeID;
    private long visualizedNodeID;
    private long parentID;
    private long id;
    private double ringWidth;
    private double height;
    private double transparency;
    private String color;

    public Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency) {
        this.visualizedNodeID = visualizedNodeId;
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.ringWidth = ringWidth;
        this.height = height;
        this.transparency = transparency;
    }

    public Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency, String color) {
        this.visualizedNodeID = visualizedNodeId;
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.ringWidth = ringWidth;
        this.height = height;
        this.transparency = transparency;
        this.color = color;
    }

    public long addNode(DatabaseConnector connector) {
        return connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, Labels.Disk.name(), getProperties()), "n").id();
    }

    public void write(DatabaseConnector connector) {
        connector.executeWrite(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID,visualizedNodeID, Labels.Disk.name(), getProperties()));
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

    public String getProperties() {
        return String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
    }

    public void setParentVisualizedNodeID(long id) {
        this.parentID = id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
