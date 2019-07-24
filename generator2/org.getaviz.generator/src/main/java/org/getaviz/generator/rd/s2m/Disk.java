package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

public class Disk implements RDElement{

    private DatabaseConnector connector;

    private double transparency;
    private boolean methodTypeMode;
    private boolean methodDisks;

    private boolean dataDisks;

    private double height;
    private double ringWidth;
    private double minArea;
    private long parentID;
    private long visualizedNodeID;
    private long newParentID;
    private long internID;
    private String color;
    private String properties;


    public Disk(long visualizedNodeId, long parentID, double ringWidth, double height, double transparency,
                DatabaseConnector connector) {
        this.visualizedNodeID = visualizedNodeId;
        this.parentID = parentID;
        this.transparency = transparency;
        this.height = height;
        this.ringWidth = ringWidth;
        this.connector = connector;

        properties = String.format("ringWidth: %f, height: %f, transparency: %f", ringWidth,
                height, transparency);
    }

    public Disk(long visualizedNodeId, long parentID, double ringWidth, double height, double transparency, String color,
                DatabaseConnector connector) {
        this.visualizedNodeID = visualizedNodeId;
        this.parentID = parentID;
        this.transparency = transparency;
        this.height = height;
        this.ringWidth = ringWidth;
        this.connector = connector;
        this.color = color;

        properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, color);
    }

    public long getParentID() {
        return this.parentID;
    }

    public long getVisualizedNodeID() {
        return this.visualizedNodeID;
    }

    public String getColor() {
        return this.color;
    }

    public String getProperties() {
        return properties;
    }

    public long getInternID() {
        return internID;
    }

    public long getNewParentID() {
        return newParentID;
    }

    public void setNewParentID(long id) {
        this.newParentID = id;
    }

    public void setInternID(long id) {
        this.internID = id;
    }
}
