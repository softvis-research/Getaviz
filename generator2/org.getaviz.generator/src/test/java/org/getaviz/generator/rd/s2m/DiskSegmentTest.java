package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskSegmentTest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static long nodeID;

    @BeforeAll
    static void setup () {
        mockup.setupDatabase("./test/databases/DiskSegmentTest.db");
        connector = mockup.getConnector();
    }

    @Test
    void writeToDatabaseJQA2RDTest() {
        DiskSegment diskSegment = createObjectsFotTest1();
        diskSegment.writeToDatabase(connector,false);
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) WHERE ID(n) = " + nodeID +
                        " RETURN ID(d) AS id, d.height AS height, d.transparency AS transparency, d.color AS color")
                .single();
        long diskID = result.get("id").asLong();
        double height = result.get("height").asDouble();
        double transparency= result.get("transparency").asDouble();
        String  color = result.get("color").asString();
        assertEquals(diskSegment.getID(), diskID);
        assertEquals(1.5, height);
        assertEquals(0.5, transparency);
        assertEquals("#66000000", color);
    }

    @Test
    void writeToDatabaseRD2RDTest() {
        DiskSegment diskSegment = createObjectsForTest2();
        diskSegment.setAngle(0.5);
        diskSegment.writeToDatabase(connector, true);
        Record result = connector.executeRead("MATCH (d) WHERE ID(d) = " + diskSegment.getID() + " RETURN d.angle" +
                " AS angle").single();
        double size = result.get("angle").asDouble();
        assertEquals(0.5, size);
    }

    private static DiskSegment createObjectsFotTest1() {
        nodeID = connector.addNode(("CREATE (n:Field)"), "n").id();
        return new DiskSegment(nodeID, -1, 1.5, 0.5, "#66000000");
    }

    private static DiskSegment createObjectsForTest2() {
        DiskSegment diskSegment = new DiskSegment(2, 5, 1);
        Record createDiskSegment = connector.executeRead("CREATE (d:DiskSegment) RETURN ID(d) AS id").single();
        long newID = createDiskSegment.get("id").asLong();
        diskSegment.setID(newID);
        return diskSegment;
    }
}
