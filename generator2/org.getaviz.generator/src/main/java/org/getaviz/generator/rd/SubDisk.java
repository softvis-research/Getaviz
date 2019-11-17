package org.getaviz.generator.rd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.rd.m2m.Position;
import java.util.ArrayList;

public class SubDisk extends Disk {
    private Log log = LogFactory.getLog(this.getClass());
    private ArrayList<DiskSegment> innerSegments = new ArrayList<>();
    private ArrayList<DiskSegment> outerSegments = new ArrayList<>();
    private double minArea;

    public SubDisk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency,
         String color) {
        super(visualizedNodeId, parentVisualizedNodeID, ringWidth, height, transparency);
        this.color = color;
    }

    public SubDisk(long visualizedNodeID, long parentID, long id, double ringWidth, double height) {
        super(visualizedNodeID, ringWidth, height);
        this.parentID = parentID;
        this.id = id;
        this.position = new Position(0, 0, 0);
        this.wroteToDatabase = true;
    }

    public void calculateAreaWithoutBorder() {
        double innerSegmentsArea = getInnerSegmentsArea();
        double outerSegmentsArea = getOuterSegmentsArea();

        areaWithoutBorder = innerSegmentsArea + outerSegmentsArea;
        radius = Math.sqrt(areaWithoutBorder / Math.PI) + borderWidth;
        areaWithBorder = radius * radius * Math.PI;
        areaWithoutBorder += getInnerDisksArea();
        minArea = innerSegmentsArea + outerSegmentsArea;

        outerSegments.forEach(segment -> {
            segment.calculateSize(outerSegmentsArea);
        });
    }

    public void setInnerSegmentsList(ArrayList<DiskSegment> list) {
        innerSegments.addAll(list);
    }

    public ArrayList<DiskSegment> getInnerSegments() {
        return innerSegments;
    }

    public void setOuterSegmentsList(ArrayList<DiskSegment> list) {
        outerSegments.addAll(list);
    }

    public ArrayList<DiskSegment> getOuterSegments() {
        return outerSegments;
    }

    public double getMinArea() {
        return minArea;
    }

    public void createNode(DatabaseConnector connector) {
        String label = Labels.SubDisk.name();
        try {
            long id = connector.addNode(String.format(
                    "MATCH(s) WHERE ID(s) = %d CREATE (n:RD:%s {%s})-[:VISUALIZES]->(s)",
                    visualizedNodeID, label, propertiesToString()), "n").id();
            setID(id);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void updateNode(DatabaseConnector connector) {
        String crossSection = calculateCrossSection();
        String updateNode = String.format(
                "MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.crossSection = %s, n.spine = %s ", id,
                radius, crossSection, spine);
        String createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", position.x,
                position.y, position.z);
        connector.executeWrite(updateNode + createPosition);
    }

    public boolean hasInnerSegments() {
        return !innerSegments.isEmpty();
    }

    public boolean hasOuterSegments() {
        return !outerSegments.isEmpty();
    }

    public double getInnerSegmentsArea() {
        double sum = 0.0;
        for (DiskSegment segment : innerSegments) {
            sum += segment.getSize();
        }
        return sum;
    }

    public double getOuterSegmentsArea() {
        double sum = 0.0;
        for (DiskSegment segment : outerSegments) {
            sum += segment.getSize();
        }
        return sum;
    }

    private double getInnerDisksArea() {
        double sum = 0.0;
        for (Disk disk : getInnerDisks()) {
            sum += disk.getAreaWithoutBorder();
        }
        return sum;
    }
}
