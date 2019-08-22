package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
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
        disk.writeToDatabase(connector, false);
        Record result = connector
                .executeRead("MATCH (d:Disk)-[:VISUALIZES]->(n:Package) WHERE ID(n) = " + nodeID +
                        " RETURN ID(d) AS id, d.height AS height, d.ringWidth AS ringWidth, " +
                        "d.transparency AS transparency, d.color AS color").single();
        long diskID = result.get("id").asLong();
        double height = result.get("height").asDouble();
        double ringWidth = result.get("ringWidth").asDouble();
        double transparency = result.get("transparency").asDouble();
        String color = result.get("color").asString();
        assertEquals(disk.getID(), diskID);
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
    void sumTest() {
        Disk disk = createObjectsForTest2();
        ArrayList<DiskSegment> list = disk.getOuterSegments();
        double sum = Disk.sum(list);
        assertEquals(2, sum);
    }

    @Test
    void calculateRingsTest() {
        Disk disk = createObjectsForTest2();
        SettingsConfiguration.OutputFormat outputFormat = SettingsConfiguration.OutputFormat.AFrame;
        disk.calculateRings(outputFormat);
        double radius = disk.getInnerSegments().get(0).getOuterRadius();
        String crossSection = disk.calculateCrossSection();
        String spine = disk.getSpine();
        assertEquals(0.5641895835477563, radius);
        assertEquals("'-0.25 0.5, 0.25 0.5, 0.25 0, -0.25 0, -0.25 0.5'", crossSection);
        assertEquals("'-0.25 -0.0 0.0, -0.24802867532861947 -0.031333308391076065 0.0, -0.24214579028215777 " +
                "-0.0621724717912137 0.0, -0.23244412147206284 -0.0920311381711695 0.0, -0.2190766700109659 " +
                "-0.12043841852542883 0.0, -0.20225424859373686 -0.14694631307311828 0.0, -0.1822421568553529 " +
                "-0.17113677648217218 0.0, -0.1593559974371724 -0.1926283106939473 0.0, -0.13395669874474914" +
                " -0.21108198137550377 0.0, -0.10644482289126816 -0.2262067631165049 0.0, -0.07725424859373686 " +
                "-0.23776412907378838 0.0, -0.04684532864643113 -0.24557181268217218 0.0, -0.015697629882328326 " +
                "-0.2495066821070679 0.0, 0.01569762988232835 -0.2495066821070679 0.0, 0.046845328646431206 " +
                "-0.24557181268217215 0.0, 0.07725424859373689 -0.23776412907378838 0.0, 0.10644482289126818 " +
                "-0.22620676311650487 0.0, 0.13395669874474922 -0.21108198137550374 0.0, 0.15935599743717244 " +
                "-0.1926283106939473 0.0, 0.18224215685535292 -0.17113677648217213 0.0, 0.20225424859373684" +
                " -0.1469463130731183 0.0, 0.2190766700109659 -0.1204384185254288 0.0, 0.23244412147206286 " +
                "-0.09203113817116944 0.0, 0.24214579028215777 -0.062172471791213706 0.0, 0.24802867532861947 " +
                "-0.03133330839107602 0.0, 0.25 8.040613248383183E-17 0.0, 0.24802867532861947 0.03133330839107607 0.0," +
                " 0.24214579028215777 0.062172471791213754 0.0, 0.2324441214720628 0.09203113817116958 0.0, " +
                "0.2190766700109659 0.12043841852542884 0.0, 0.2022542485937368 0.14694631307311834 0.0, " +
                "0.1822421568553529 0.17113677648217218 0.0, 0.15935599743717238 0.19262831069394734 0.0, " +
                "0.13395669874474908 0.21108198137550382 0.0, 0.10644482289126804 0.22620676311650495 0.0, " +
                "0.07725424859373689 0.23776412907378838 0.0, 0.04684532864643116 0.24557181268217218 0.0, " +
                "0.015697629882328302 0.2495066821070679 0.0, -0.01569762988232843 0.2495066821070679 0.0, " +
                "-0.04684532864643128 0.24557181268217215 0.0, -0.07725424859373681 0.2377641290737884 0.0, " +
                "-0.10644482289126815 0.2262067631165049 0.0, -0.1339566987447492 0.21108198137550374 0.0, " +
                "-0.1593559974371725 0.19262831069394726 0.0, -0.18224215685535297 0.17113677648217207 0.0, " +
                "-0.20225424859373695 0.14694631307311815 0.0, -0.2190766700109659 0.12043841852542883 0.0, " +
                "-0.23244412147206286 0.09203113817116947 0.0, -0.2421457902821578 0.06217247179121362 0.0, " +
                "-0.24802867532861947 0.03133330839107595 0.0, -0.25 -0.0 0.0'", spine);
    }

    @Test
    void WriteToDatabaseRD2RDTest() {
        Disk disk = createObjectsForTest2();
        disk.setPosition(1,2,3);
        disk.setRadius(2.5);
        disk.writeToDatabase(connector, true);
        Record result = connector.executeRead("MATCH (d) WHERE ID(d) = " + disk.getID() + " RETURN d.radius" +
                " AS radius").single();
        double radius = result.get("radius").asDouble();
        assertEquals(2.5, radius);
    }

    private static Disk createObjectsForTest1() {
        nodeID = connector.addNode(("CREATE (n:Package)"), "n").id();
        return new Disk(nodeID, -1, 1.5, 1.0, 0.0, "#000000");
    }

    private static Disk createObjectsForTest2() {
        ArrayList<DiskSegment> outerSegments = new ArrayList<>();
        ArrayList<DiskSegment> innerSegments = new ArrayList<>();
        Disk disk = new Disk(0, 1, 1002, 2, 1, 0.5, 0.5, false);
        Record createDisk = connector.executeRead("CREATE (d:Disk) RETURN ID(d) AS id").single();
        long newID = createDisk.get("id").asLong();
        disk.setID(newID);
        outerSegments.add(new DiskSegment(2, 1003, 2));
        innerSegments.add(new DiskSegment(2, 1004, 2));
        disk.setOuterSegmentsList(outerSegments);
        disk.setInnerSegmentsList(innerSegments);
        return disk;
    }
}
