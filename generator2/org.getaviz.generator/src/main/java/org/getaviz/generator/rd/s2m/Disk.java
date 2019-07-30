package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

class Disk implements RDElement{

    private double height;
    private double transparency;
    private double ringWidth;
    private String color;
    private String label;
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
        this.label = Labels.Disk.name();
    }

    Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency, String color) {
        this(visualizedNodeId, parentVisualizedNodeID, ringWidth, height, transparency);
        this.color = color;
    }

    public void createNodeForVisualization(DatabaseConnector connector) {
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
}
