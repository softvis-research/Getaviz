package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelTest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static Model model;
    private static Disk disk;
    private static DiskSegment diskSegment;
    private static long modelID;

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/ModelTest.db");
        connector = mockup.getConnector();
        createTestObjects();
        model.addRDElement(disk);
        model.addRDElement(diskSegment);
        model.writeToDatabase(connector);
        modelID = model.getId();
    }

    @Test
    void modelIDTest() {
        Record result = connector.executeRead("MATCH (m:Model:RD) RETURN ID(m) AS result").single();
        long id = result.get("result").asLong();
        assertEquals(modelID, id);
    }

    @Test
    void writtenVisualisationsTest() {
        Record result = connector.executeRead("MATCH (n)-[:VISUALIZES]->(s) RETURN count(n) as result").single();
        long count  = result.get("result").asLong();
        assertEquals(2, count);
    }

    @Test
    void parentChildRelationTest () {
        long diskID = disk.getId();
        long diskSegmentID = diskSegment.getId();
        Record result = connector.executeRead("MATCH (n)-[r]->(m) WHERE ID(n) = " + diskID + " AND ID(m) = " +
                diskSegmentID + " RETURN type(r) AS result").single();
        String relation = result.get("result").asString();
        assertEquals("CONTAINS", relation);
    }

    private static void createTestObjects() {
        model = new Model(false,false,false);
        long nodeID4Disk = connector.addNode(("CREATE (n:Package)"), "n").id();
        long nodeID4DiskSegment = connector.addNode(("CREATE (n:Field)"), "n").id();
        disk = new Disk(nodeID4Disk, -1, 1.5,1.0, 0.0, "#000000");
        diskSegment = new DiskSegment(nodeID4DiskSegment, nodeID4Disk, 1.5, 0.5, "#66000000");
    }
}
