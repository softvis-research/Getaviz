package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.types.Node;

public class DiskSegment implements RDElement {
    private double height;
    private String properties;
    private String color;
    private long visualizedNodeID;
    private long parentID;
    private long newParentID;
    private long internID;
    private DatabaseConnector connector;


    public DiskSegment (long visualizedNodeID, long parentID, double transparency, double height, String color,
                        DatabaseConnector connector) {
        this.visualizedNodeID = visualizedNodeID;
        this.parentID = parentID;
        this.height = height;
        this.color = color;
        this.connector = connector;
        properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", 1.0, height,
                transparency, color);
    }

    public DiskSegment (Node structure, long parentID, double transparency, double minArea, double height, String color,
                        DatabaseConnector connector) {
        this.visualizedNodeID = structure.id();
        this.parentID = parentID;
        this.height = height;
        this.color = color;
        this.connector = connector;

        int numberOfStatements = structure.get("effectiveLineCount").asInt(0);
        double size = numberOfStatements;
        if (numberOfStatements <= minArea) {
            size = minArea;
        }
        properties = String.format(
                "height: %f, transparency: %f, size: %f, color: \'%s\'", height, transparency, size, color);
    }

    public String getProperties() {
        return properties;
    }

    public long getParentID(){
        return parentID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }

    public long getNewParentID() {
        return newParentID;
    }

    public void setNewParentID(long newParentID) {
        this.newParentID = newParentID;
    }
}
