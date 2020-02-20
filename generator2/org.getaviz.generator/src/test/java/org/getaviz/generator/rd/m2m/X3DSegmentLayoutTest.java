package org.getaviz.generator.rd.m2m;

import org.getaviz.generator.rd.Disk;
import org.getaviz.generator.rd.DiskSegment;
import org.getaviz.generator.rd.SubDisk;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class X3DSegmentLayoutTest {
    private static ArrayList<SubDisk> diskList = new ArrayList<>();
    private static DiskSegment innerDiskSegment;

    @BeforeAll
    static void setup() {
        X3DSegmentLayout layout = createX3DSegmentLayout();
        layout.calculateInnerSegments();
        layout.calculateOuterSegments();
    }

    @Test
    void innerRadius() {
        double radius = innerDiskSegment.getInnerRadius();
        assertEquals(0, radius);
    }

    @Test
    void innerSegmentsRadiusWithSubDisk() {
        double radius = diskList.get(1).getInnerSegmentsRadius();
        assertEquals(1.5957691216057308, radius);
    }

    @Test
    void innerSegmentsRadiusWithoutSubDisk() {
        double radius = diskList.get(2).getInnerSegmentsRadius();
        assertEquals(0, radius);
    }

    private static X3DSegmentLayout createX3DSegmentLayout() {
        SubDisk disk = new SubDisk(0, 1, 1002, 0.5, 0.5);
        SubDisk disk2 = new SubDisk(0, 1, 1002, 0.5, 0.5);
        SubDisk innerDisk = new SubDisk(0, 1, 1002, 0.5, 0.5);
        List<Disk> innerDisks = new ArrayList<>();
        innerDisks.add(innerDisk);
        disk.setInnerDisks(innerDisks);
        diskList.add(innerDisk);
        diskList.add(disk);
        diskList.add(disk2);
        ArrayList<DiskSegment> outerSegments = new ArrayList<>();
        ArrayList<DiskSegment> innerSegments = new ArrayList<>();
        ArrayList<DiskSegment> innerInnerSegments = new ArrayList<>();
        innerDiskSegment = new DiskSegment(2, 1004, 2, 4);
        outerSegments.add(new DiskSegment(2, 1003, 2, 4));
        innerSegments.add(innerDiskSegment);
        disk.setOuterSegmentsList(outerSegments);
        disk.setInnerSegmentsList(innerSegments);
        innerInnerSegments.add(new DiskSegment(2, 1004, 2, 4));
        innerDisk.setInnerSegmentsList(innerInnerSegments);

        return new X3DSegmentLayout(diskList);
    }
}
