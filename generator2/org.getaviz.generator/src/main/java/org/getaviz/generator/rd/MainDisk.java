package org.getaviz.generator.rd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.rd.m2m.Position;

public class MainDisk extends Disk {

    private Log log = LogFactory.getLog(this.getClass());

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
        if(visualizedNodeID != -1) {
            try {
                long id = connector.addNode(String.format(
                        "MATCH(s) WHERE ID(s) = %d CREATE (n:RD:MainDisk {%s})-[:VISUALIZES]->(s)",
                        visualizedNodeID, propertiesToString()), "n").id();
                setID(id);
            } catch (Exception e) {
                log.error(e);
            }
        } else {
            try {
                long id = connector.addNode(String.format(
                        "CREATE (n:RD:MainDisk {%s})", propertiesToString()), "n").id();
                setID(id);
            } catch (Exception e) {
                log.error(e);
            }
        }
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
