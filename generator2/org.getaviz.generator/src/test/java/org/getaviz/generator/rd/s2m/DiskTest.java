package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.rd.Disk;
import org.getaviz.generator.rd.DiskSegment;
import org.getaviz.generator.rd.SubDisk;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskTest {

    @Test
    void calculateAreaWithoutBorderAreaTest() {
        SubDisk disk = createSubDisk();
        disk.calculateAreaWithoutBorder();
        double result = disk.getAreaWithoutBorder();
        assertEquals(16, result);
    }

    @Test
    void calculateSpinesTest() {
        Disk disk = createSubDisk();
        disk.setRadius(3.5);
        disk.calculateSpines();
        String spine = disk.getSpine();
        assertEquals("'3.5 0.0 0.0, 3.4724014546006727 0.4386663174750649 0.0, 3.3900410639502088 " +
                "0.8704146050769918 0.0, 3.25421770060888 1.288435934396373 0.0, 3.0670733801535226 1.6861378593560037 " +
                "0.0, 2.831559480312316 2.057248383023656 0.0, 2.5513901959749403 2.3959148707504108 0.0, " +
                "2.230983964120414 2.6967963497152625 0.0, 1.875393782426488 2.9551477392570527 0.0, 1.4902275204777542 " +
                "3.1668946836310683 0.0, 1.081559480312316 3.3286978070330373 0.0, 0.6558346010500358 3.4380053775504105" +
                " 0.0, 0.21976681835259657 3.4930935494989503 0.0, -0.2197668183525969 3.4930935494989503 0.0," +
                " -0.6558346010500369 3.43800537755041 0.0, -1.0815594803123165 3.3286978070330373 0.0, " +
                "-1.4902275204777544 3.1668946836310683 0.0, -1.875393782426489 2.955147739257052 0.0, " +
                "-2.230983964120414 2.6967963497152625 0.0, -2.5513901959749408 2.39591487075041 0.0, " +
                "-2.8315594803123156 2.0572483830236563 0.0, -3.0670733801535226 1.6861378593560032 0.0, " +
                "-3.25421770060888 1.288435934396372 0.0, -3.3900410639502088 0.8704146050769919 0.0, " +
                "-3.4724014546006727 0.43866631747506435 0.0, -3.5 -1.1256858547736455E-15 0.0, -3.4724014546006727 " +
                "-0.438666317475065 0.0, -3.3900410639502088 -0.8704146050769925 0.0, -3.2542177006088795 " +
                "-1.288435934396374 0.0, -3.0670733801535226 -1.686137859356004 0.0, -2.831559480312315 " +
                "-2.0572483830236568 0.0, -2.5513901959749403 -2.3959148707504108 0.0, -2.2309839641204134 " +
                "-2.696796349715263 0.0, -1.875393782426487 -2.9551477392570535 0.0, -1.4902275204777526 " +
                "-3.166894683631069 0.0, -1.0815594803123165 -3.3286978070330373 0.0, -0.6558346010500362 " +
                "-3.4380053775504105 0.0, -0.21976681835259623 -3.4930935494989503 0.0, 0.21976681835259804 " +
                "-3.4930935494989503 0.0, 0.655834601050038 -3.43800537755041 0.0, 1.0815594803123152 " +
                "-3.3286978070330377 0.0, 1.4902275204777542 -3.1668946836310683 0.0, 1.8753937824264888 " +
                "-2.955147739257052 0.0, 2.2309839641204148 -2.6967963497152616 0.0, 2.5513901959749417 " +
                "-2.395914870750409 0.0, 2.8315594803123174 -2.057248383023654 0.0, 3.0670733801535226 " +
                "-1.6861378593560037 0.0, 3.25421770060888 -1.2884359343963725 0.0, 3.390041063950209 " +
                "-0.8704146050769908 0.0, 3.4724014546006727 -0.43866631747506324 0.0, 3.5 0.0 0.0'", spine);
    }

    @Test
    void calculateRadiusAndAreaWithoutBorderTest() {
        SubDisk disk = createSubDisk();
        disk.calculateAreaWithoutBorder();
       // disk.calculateRadius();
        double radius = disk.getRadius();
        double area = disk.getAreaWithoutBorder();
        assertEquals(16, area);
        assertEquals(2.756758334191025, radius);
    }

    private static SubDisk createSubDisk() {
        ArrayList<DiskSegment> outerSegments = new ArrayList<>();
        ArrayList<DiskSegment> innerSegments = new ArrayList<>();
        SubDisk disk = new SubDisk(0, 1, 1002, 0.5, 0.5);
        outerSegments.add(new DiskSegment(2, 1003, 2, 4));
        innerSegments.add(new DiskSegment(2, 1004, 2, 4));
        disk.setOuterSegmentsList(outerSegments);
        disk.setInnerSegmentsList(innerSegments);
        return disk;
    }
}
