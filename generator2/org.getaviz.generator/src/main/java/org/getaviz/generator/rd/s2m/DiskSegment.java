package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.types.Node;

public class DiskSegment implements RDElement {

    private double size;
    private double height;
    private double transparency;
    private String color;
    private long visualizedNodeID;
    private long parentVisualizedNodeID;
    private long parentID;

    public DiskSegment (long visualizedNodeID, long parentVisualizedNodeID, double transparency, double height, String color) {
        this.visualizedNodeID = visualizedNodeID;
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.size = 1.0;
        this.transparency = transparency;
        this.height = height;
        this.color = color;
    }

    public DiskSegment (Node structure, long parentVisualizedNodeID, double transparency, double minArea, double height, String color) {
        this.visualizedNodeID = structure.id();
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.transparency = transparency;
        this.height = height;
        this.color = color;

        int numberOfStatements = structure.get("effectiveLineCount").asInt(0);
        size = numberOfStatements;
        if (numberOfStatements <= minArea) {
            size = minArea;
        }
    }

    public void write(DatabaseConnector connector) {
        connector.executeWrite(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, Labels.DiskSegment.name(), getProperties()));
    }

    public long getParentVisualizedNodeID(){
        return parentVisualizedNodeID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }

    public String getProperties() {
        return String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", size, height,
                transparency, color);
    }

    public void setParentVisualizedNodeID(long newParentID) {
        this.parentID = newParentID;
    }
}
