package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiskSegmentTest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static DiskSegment diskSegment;
    private static long node;

    @BeforeAll
    static void setup () {
        mockup.setupDatabase("./test/databases/DiskSegmentTest.db");
        connector = mockup.getConnector();
        DBModel model = new DBModel(false,false,false, connector);
        long modelID = model.getId();
        Record result = connector
                .executeRead("CREATE (n:Field) RETURN ID(n) AS result").single();
        node = result.get("result").asLong();
        diskSegment = new DiskSegment(node, modelID, 1.5, 0.5, "#66000000");
        diskSegment.setParentID(modelID);
        diskSegment.createNodeForVisualization(connector);
    }

    @Test
    void addNodeTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) WHERE ID(n) = " + node +
                        " RETURN ID(d) AS result").single();
        long diskID = result.get("result").asLong();
        assertEquals(diskSegment.getId(), diskID);
    }

    @Test
    void writeHeightTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.height AS result").single();
        double height = result.get("result").asDouble();
        assertEquals(1.5, height);
    }

    @Test
    void writeTransparencyTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.transparency AS result").single();
        double transparency= result.get("result").asDouble();
        assertEquals(0.5, transparency);
    }

    @Test
    void writeColorTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.color AS result").single();
        String  color = result.get("result").asString();
        assertEquals("#66000000", color);
    }
}
