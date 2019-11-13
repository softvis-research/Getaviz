package org.getaviz.generator.rd;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.rd.m2m.Position;

public class MainDisk extends Disk {

    public MainDisk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency) {
        super(visualizedNodeId, parentVisualizedNodeID, ringWidth, height, transparency);
    }

    public double getMinArea() {
        return 0;
    }

    public MainDisk(long visualizedNodeID, long parentID, long id, double ringWidth, double height) {
        super(visualizedNodeID, ringWidth, height);
        this.parentID = parentID;
        this.id = id;
        this.areaWithBorder = 0;
        this.areaWithoutBorder = 0;
        this.position = new Position(0, 0, 0);
        this.wroteToDatabase = true;
    }

    public void createNode(DatabaseConnector connector) {
        String label = Labels.MainDisk.name();
        long id = connector.addNode(String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
                        "(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parentID, visualizedNodeID, label, propertiesToString()), "n").id();
        setID(id);
    }

    public void updateNode(DatabaseConnector connector) {
        String crossSection = calculateCrossSection();
        String updateNode = String.format(
                "MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.crossSection = %s, n.spine = %s, n.color = %s ", id,
                radius, crossSection, spine, color);
        String createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", position.x,
                position.y, position.z);
        connector.executeWrite(updateNode + createPosition);
    }

    public void calculateAreaWithoutBorder() {
        for (Disk d : getInnerDisks()) {
            setAreaWithoutBorder(getAreaWithoutBorder() + d.getAreaWithoutBorder());
        }
    }
}