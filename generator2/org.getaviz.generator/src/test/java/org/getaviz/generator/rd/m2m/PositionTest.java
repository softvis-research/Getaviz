package org.getaviz.generator.rd.m2m;

import org.getaviz.generator.rd.Disk;
import org.getaviz.generator.rd.MainDisk;
import org.getaviz.generator.rd.SubDisk;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private static Disk disk1;
    private static Disk disk2;

    @BeforeAll
    static void setup(){
        createObjectsForTests();
    }

    @Test
    void distanceTest() {
        double distance = Position.distance(disk1, disk2);
        assertEquals(1.4142135623730951, distance);
    }

    @Test
    void angleBetweenPoints_XasKatetheTest() {
        double angle = Position.angleBetweenPoints_XasKatethe(disk1, disk2);
        assertEquals(44.99999999999999, angle);
    }

    @Test
    void angleBetweenPoints_YasKatetheTest() {
        double angle = Position.angleBetweenPoints_YasKatethe(disk1, disk2);
        assertEquals(44.99999999999999, angle);
    }

    @Test
    void intersectTest() {
        boolean result = Position.intersect(disk1, disk2);
        assertTrue(result);
    }

    @Test
    void relativePositionTest() {
        boolean resultIsLeftOf = Position.isLeftOf(disk1, disk2);
        boolean resultIsRightOf = Position.isRightOf(disk1, disk2);
        boolean resultIsAboveOf = Position.isAboveOf(disk1, disk2);
        boolean resultIsBelowOf = Position.isBelowOf(disk1, disk2);
        assertFalse(resultIsLeftOf);
        assertTrue(resultIsRightOf);
        assertTrue(resultIsAboveOf);
        assertFalse(resultIsBelowOf);
    }

    @Test
    void calculateCentreTest() {
        Position position = Position.calculateCentre(disk1, disk2, 90);
        assert position != null;
        double x = position.x;
        double y = position.y;
        double z = position.z;
        assertEquals(10.5, x);
        assertEquals(5, y);
        assertEquals(0, z);
    }

    private static void createObjectsForTests() {
        disk1 = new MainDisk(1,2,3,1.5,
                3.0);
        disk2 = new SubDisk(4, 5, 6, 2.5,
                1.0);
        Position position1 = new Position(2, 5, 0);
        Position position2 = new Position(1, 4, 0);
        disk1.setPosition(position1);
        disk2.setPosition(position2);
        disk1.setRadius(5.0);
        disk2.setRadius(3.5);
    }
}
