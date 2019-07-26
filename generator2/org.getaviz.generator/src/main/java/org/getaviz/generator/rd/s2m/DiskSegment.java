package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.types.Node;

class DiskSegment implements RDElement {

    private double size;
    private double height;
    private double transparency;
    private String color;
    private long visualizedNodeID;
    private long parentVisualizedNodeID;
    private long parentID;
    private long id;

    DiskSegment (long visualizedNodeID, long parentVisualizedNodeID, double transparency, double height, String color) {
        this.visualizedNodeID = visualizedNodeID;
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.size = 1.0;
        this.transparency = transparency;
        this.height = height;
        this.color = color;
    }

    DiskSegment (long visualizedNodeID, long parentVisualizedNodeID, double transparency, double minArea, double height, String color,
        int numberOfStatements) {
        this(visualizedNodeID, parentVisualizedNodeID, transparency, height, color);

        size = numberOfStatements;
        if (numberOfStatements <= minArea) {
            size = minArea;
        }
    }

    public long addNode(DatabaseConnector connector) {
        return connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, Labels.DiskSegment.name(), getProperties()), "n").id();
    }

    public long getParentVisualizedNodeID(){
        return parentVisualizedNodeID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }

   String getProperties() {
        return String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", size, height,
                transparency, color);
    }

    public long getId() {
        return id;
    }

    public void setParentVisualizedNodeID(long newParentID) {
        this.parentID = newParentID;
    }

    public void setId(long id) {
        this.id = id;
    }
}
