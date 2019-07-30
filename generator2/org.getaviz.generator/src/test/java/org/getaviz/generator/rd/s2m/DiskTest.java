package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiskTest {

    private static DatabaseConnector connector;
    private static Model model;
    private static Bank mockup = new Bank();
    private static Disk disk;
    private static long nodeID;
    private static ArrayList<RDElement> RDElementsList = new ArrayList<>();

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/DiskTest.db");
        connector = mockup.getConnector();
        createObjectsForTests();
        RDElementsList.add(disk);
        model.setRDElementsList(RDElementsList);
        model.createVisualization(connector);
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
    void addNodeHeightTest() {
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN d.height AS result").single();
        double height = result.get("result").asDouble();
        assertEquals(1.0, height);
    }

    @Test
    void addNodeRingWithTest() {
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN d.ringWidth AS result").single();
        double ringWidth = result.get("result").asDouble();
        assertEquals(1.5, ringWidth);
    }

    @Test
    void addNodeTransparencyTest() {
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
        model = new Model(false,false,false, connector);
        Record result = connector
                .executeRead("CREATE (n:Package) RETURN ID(n) AS result").single();
        nodeID = result.get("result").asLong();
        disk = new Disk(nodeID, -1, 1.5,1.0, 0.0, "#000000");
    }
}
