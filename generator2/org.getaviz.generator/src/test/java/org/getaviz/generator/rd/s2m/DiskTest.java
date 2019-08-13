package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskTest {

    private static DatabaseConnector connector;
    private static Model model;
    private static Bank mockup = new Bank();
    private static Disk disk;
    private static long nodeID;

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/DiskTest.db");
        connector = mockup.getConnector();
        createObjectsForTests();
        model.addRDElement(disk);
        model.writeToDatabase(connector);
    }

    @Test
    void addNodeTest() {
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN ID(d) AS result").single();
        long diskID = result.get("result").asLong();
        assertEquals(disk.getId(), diskID);
    }

    @Test
    void heightTest() {
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN d.height AS result").single();
        double height = result.get("result").asDouble();
        assertEquals(1.0, height);
    }

    @Test
    void ringWithTest() {
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN d.ringWidth AS result").single();
        double ringWidth = result.get("result").asDouble();
        assertEquals(1.5, ringWidth);
    }

    @Test
    void transparencyTest() {
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN d.transparency AS result").single();
        double transparency = result.get("result").asDouble();
        assertEquals(0.0, transparency);
    }

    @Test
    void addNodeColorTest() {
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN d.color AS result").single();
        String color = result.get("result").asString();
        assertEquals("#000000", color);
    }

    private static void createObjectsForTests() {
        model = new Model(false,false,false);
        nodeID = connector.addNode(("CREATE (n:Package)"), "n").id();
        disk = new Disk(nodeID, -1, 1.5,1.0, 0.0, "#000000");
    }
}
