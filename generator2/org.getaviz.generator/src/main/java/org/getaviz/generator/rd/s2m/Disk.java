package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

public class Disk implements RDElement{

    private DatabaseConnector connector;

    private long parentID;
    private long visualizedNodeID;
    private long newParentID;
    private long internID;
    private String properties;

    public Disk(long visualizedNodeId, long parentID, double ringWidth, double height, double transparency,
                DatabaseConnector connector) {
        this.visualizedNodeID = visualizedNodeId;
        this.parentID = parentID;
        this.connector = connector;

        properties = String.format("ringWidth: %f, height: %f, transparency: %f", ringWidth,
                height, transparency);
    }

    public Disk(long visualizedNodeId, long parentID, double ringWidth, double height, double transparency, String color,
                DatabaseConnector connector) {
        this.visualizedNodeID = visualizedNodeId;
        this.parentID = parentID;
        this.connector = connector;

        properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
    }

    public long addNode() {
        return connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                newParentID, visualizedNodeID, Labels.Disk.name(), properties), "n").id();
    }

    public void write() {
        connector.executeWrite(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                newParentID,visualizedNodeID, Labels.Disk.name(), properties));
    }

    public long getParentID() {
        return this.parentID;
    }

    public long getVisualizedNodeID() {
        return this.visualizedNodeID;
    }

    public long getInternID() {
        return internID;
    }

    public void setNewParentID(long id) {
        this.newParentID = id;
    }

    public void setInternID(long id) {
        this.internID = id;
    }
}
