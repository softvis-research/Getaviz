package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskTest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static long nodeID;

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/DiskTest.db");
        connector = mockup.getConnector();
    }

    @Test
    void writeToDiskForJQA2RDTest() {
        Disk disk = createObjectsForTest1();
        disk.writeToDatabase(connector, "JQA2RD");
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN ID(d) AS id, d.height AS height, d.ringWidth AS ringWidth, " +
                        "d.transparency AS transparency, d.color AS color").single();
        long diskID = result.get("id").asLong();
        double height = result.get("height").asDouble();
        double ringWidth = result.get("ringWidth").asDouble();
        double transparency = result.get("transparency").asDouble();
        String color = result.get("color").asString();
        assertEquals(disk.getId(), diskID);
        assertEquals(1.0, height);
        assertEquals(1.5, ringWidth);
        assertEquals(0.0, transparency);
        assertEquals("#000000", color);
    }

    @Test
    void calculateAreaWithoutBorderAreaTest() {
        Disk disk = createObjectsForTest2();
        disk.calculateAreaWithoutBorder(4);
        double result = disk.getAreaWithoutBorder();
        assertEquals(16, result);
    }

    @Test
    void calculateSumTest() {
        Disk disk = createObjectsForTest2();
        disk.calculateSum();
        double sumOuterSegments = disk.getOuterSegmentsArea();
        double sumInnerSegments = disk.getInnerSegmentsArea();
        assertEquals(2, sumOuterSegments);
        assertEquals(2, sumInnerSegments);
    }

    @Test
    void sumTest() {
        Disk disk = createObjectsForTest2();
        ArrayList<DiskSegment> list = disk.getOuterSegments();
        double sum = Disk.sum(list);
        assertEquals(2, sum);
    }

    @Test
    void WriteToDatabaseRD2RDTest() {
        Disk disk = createObjectsForTest2();
        disk.writeToDatabase(connector, "RD2RD");
        Record result = connector.executeRead("MATCH (d) WHERE ID(d) = " + disk.getId() + " RETURN d.netArea " +
                "AS areaWithoutBorder").single();
        double DBAreaWithoutBorder = result.get("areaWithoutBorder").asDouble();
        double DiskAreaWithoutBorder = disk.getAreaWithoutBorder();
        assertEquals(DBAreaWithoutBorder, DiskAreaWithoutBorder);
    }

    private static Disk createObjectsForTest1() {
        nodeID = connector.addNode(("CREATE (n:Package)"), "n").id();
        return new Disk(nodeID, -1, 1.5, 1.0, 0.0, "#000000");
    }

    private static Disk createObjectsForTest2() {
        ArrayList<DiskSegment> outerSegments = new ArrayList<>();
        ArrayList<DiskSegment> innerSegments = new ArrayList<>();
        Disk disk = new Disk(0,1, 1002, 2, 1, 0.5, 0.5);
        Record createDisk = connector.executeRead("CREATE (d:Disk) RETURN ID(d) AS id").single();
        long newID = createDisk.get("id").asLong();
        disk.setId(newID);
        outerSegments.add(new DiskSegment(2, 1003, 2));
        innerSegments.add(new DiskSegment(2, 1004, 2));
        disk.setOuterSegmentsList(outerSegments);
        disk.setInnerSegmentsList(innerSegments);
        return disk;
    }
}
