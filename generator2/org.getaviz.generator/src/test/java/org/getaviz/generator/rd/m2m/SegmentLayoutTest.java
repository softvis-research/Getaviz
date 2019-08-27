package org.getaviz.generator.rd.m2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.getaviz.generator.rd.s2m.Disk;
import org.getaviz.generator.rd.s2m.DiskSegment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SegmentLayoutTest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static ArrayList<Disk> diskList = new ArrayList<>();
    private static ArrayList<DiskSegment> outerSegments = new ArrayList<>();
    private static ArrayList<DiskSegment> innerSegments = new ArrayList<>();

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/SegmentLayout.db");
        connector = mockup.getConnector();
        createObjectsForTest();
    }

    @Test
    void calculateRingsTest() {
        SettingsConfiguration.OutputFormat outputFormat = SettingsConfiguration.OutputFormat.AFrame;
        SegmentLayout.calculateRings(diskList, outputFormat);
        double angle = outerSegments.get(0).getAngle();
        double radius = innerSegments.get(0).getInnerRadius();
        System.out.println(radius);
        assertEquals(359.0027700831025, angle);
        assertEquals(0, radius);
    }


    private static void createObjectsForTest() {
        Disk disk = new Disk(0, 1, 1002, 2, 1, 0.5, 0.5, false);
        Record createDisk = connector.executeRead("CREATE (d:Disk) RETURN ID(d) AS id").single();
        long newID = createDisk.get("id").asLong();
        disk.setID(newID);
        diskList.add(disk);
        outerSegments.add(new DiskSegment(2, 1003, 2));
        innerSegments.add(new DiskSegment(2, 1004, 2));
        disk.setOuterSegmentsList(outerSegments);
        disk.setInnerSegmentsList(innerSegments);
    }
}
