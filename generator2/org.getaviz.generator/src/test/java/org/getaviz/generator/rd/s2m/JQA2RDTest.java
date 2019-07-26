package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JQA2RDTest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/JQA2RDTest.db");
        connector = mockup.getConnector();
        SettingsConfiguration config = SettingsConfiguration.getInstance();
        JQA2RD testInstance = new JQA2RD(config);
        testInstance.run();
    }

    @Test
    void numberOfDisks() {
        Record result = connector
                .executeRead("MATCH (disk:Disk)-[:VISUALIZES]->(n) RETURN count(disk) AS result").single();
        int numberOfDisks = result.get("result").asInt();
        assertEquals(10, numberOfDisks);
    }

    @Test
    void numberOfDiskSegments() {
        Record result = connector
                .executeRead("MATCH (disk:DiskSegment)-[:VISUALIZES]->(n) RETURN count(disk) AS result").single();
        int numberOfDisksSegments = result.get("result").asInt();
        assertEquals(35, numberOfDisksSegments);
    }
}
