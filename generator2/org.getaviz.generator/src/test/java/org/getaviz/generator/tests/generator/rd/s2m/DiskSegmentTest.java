package org.getaviz.generator.tests.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.getaviz.generator.rd.s2m.DiskSegment;
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
        mockup.loadProperties("RDBankTest.properties");
        connector = mockup.getConnector();
        SettingsConfiguration config = SettingsConfiguration.getInstance();
        long model = connector.addNode(
                String.format(
                        "CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', " +
                                "method_disks: \'%s\', data_disks:\'%s\'})",
                        new GregorianCalendar().getTime().toString(), config.isMethodTypeMode(), config.isMethodDisks(),
                        config.isDataDisks()),
                "m").id();
        Record result = connector
                .executeRead("CREATE (n:Field) RETURN ID(n) AS result").single();
        node = result.get("result").asLong();
        diskSegment = new DiskSegment(node, model, 1.5, 0.5, "#66000000", connector);
        diskSegment.setNewParentID(model);
        diskSegment.write();
    }

    @Test
    void writeHeightTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.height AS result").single();
        double height = result.get("result").asDouble();
        assertEquals(0.5, height);
    }

    @Test
    void writeTransparencyTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.transparency AS result").single();
        double transparency= result.get("result").asDouble();
        assertEquals(1.5, transparency);
    }

    @Test
    void writeColorTest() {
        Record result = connector
                .executeRead("MATCH (d:DiskSegment)-[:VISUALIZES]->(n:Field) RETURN d.color AS result").single();
        String  color = result.get("result").asString();
        assertEquals("#66000000", color);
    }
}
