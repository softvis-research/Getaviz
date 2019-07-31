package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskSegmentTest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static Model model;
    private static DiskSegment diskSegment;
    private static long nodeID;

    @BeforeAll
    static void setup () {
        mockup.setupDatabase("./test/databases/DiskSegmentTest.db");
        connector = mockup.getConnector();
        createTestObjects();
        model.setList(diskSegment);
        model.writeToDatabase(connector);
    }

    @Test
    void addNodeTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) WHERE ID(n) = " + nodeID +
                        " RETURN ID(d) AS result").single();
        long diskID = result.get("result").asLong();
        assertEquals(diskSegment.getId(), diskID);
    }

    @Test
    void heightTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.height AS result").single();
        double height = result.get("result").asDouble();
        assertEquals(1.5, height);
    }

    @Test
    void transparencyTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.transparency AS result").single();
        double transparency= result.get("result").asDouble();
        assertEquals(0.5, transparency);
    }

    @Test
    void colorTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.color AS result").single();
        String  color = result.get("result").asString();
        assertEquals("#66000000", color);
    }

    private static void createTestObjects() {
        model = new Model(false,false,false);
        Record result = connector
                .executeRead("CREATE (n:Field) RETURN ID(n) AS result").single();
        nodeID = result.get("result").asLong();
        diskSegment = new DiskSegment(nodeID, -1, 1.5, 0.5, "#66000000");
    }
}
