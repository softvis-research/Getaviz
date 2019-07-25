package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.types.Node;

public class DiskSegment implements RDElement {
    private double size;
    private String properties;
    private long visualizedNodeID;
    private long parentID;
    private long newParentID;
    private DatabaseConnector connector;

    public DiskSegment (long visualizedNodeID, long parentID, double transparency, double height, String color,
                        DatabaseConnector connector) {
        this.visualizedNodeID = visualizedNodeID;
        this.parentID = parentID;
        this.connector = connector;
        this.size = 1.0;

        properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", size, height,
                transparency, color);
    }

    public DiskSegment (Node structure, long parentID, double transparency, double minArea, double height, String color,
                        DatabaseConnector connector) {
        this.visualizedNodeID = structure.id();
        this.parentID = parentID;
        this.connector = connector;

        int numberOfStatements = structure.get("effectiveLineCount").asInt(0);
        size = numberOfStatements;
        if (numberOfStatements <= minArea) {
            size = minArea;
        }
        properties = String.format(
                "size: %f, height: %f, transparency: %f, color: \'%s\'", size, height, transparency, color);
    }

    public void write() {
        connector.executeWrite(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                newParentID, visualizedNodeID, Labels.DiskSegment.name(), properties));
    }

    public long getParentID(){
        return parentID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }

    public void setNewParentID(long newParentID) {
        this.newParentID = newParentID;
    }
}
