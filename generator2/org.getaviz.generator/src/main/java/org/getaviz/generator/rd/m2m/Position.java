package org.getaviz.generator.rd.m2m;

import org.getaviz.generator.rd.Disk;

public class Position {
    public Double x;
    public Double y;
    public Double z;

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    static double correctAngle(double angle) {
        return angle % 360;
    }

    static double distance(Disk firstPoint, Disk secondPoint) {
        double a = Math.abs(firstPoint.getPosition().y - secondPoint.getPosition().y);
        double b = Math.abs(firstPoint.getPosition().x - secondPoint.getPosition().x);
        return Math.sqrt(a * a + b * b);
    }

    static double angleBetweenPoints_XasKatethe(Disk firstPoint, Disk secondPoint) {
        double a = Math.abs(firstPoint.getPosition().y - secondPoint.getPosition().y);
        double b = Math.abs(firstPoint.getPosition().x - secondPoint.getPosition().x);
        double c = Math.sqrt(a * a + b * b);
        return Math.abs(Math.toDegrees(Math.asin(b / c)));
    }

    static double angleBetweenPoints_YasKatethe(Disk firstPoint, Disk secondPoint) {
        double a = Math.abs(firstPoint.getPosition().y - secondPoint.getPosition().y);
        double b = Math.abs(firstPoint.getPosition().x - secondPoint.getPosition().x);
        double c = Math.sqrt(a * a + b * b);
        return Math.abs(Math.toDegrees(Math.asin(a / c)));
    }

    static boolean intersect(Disk firstPoint, Disk secondPoint) {
        double a = Math.abs(firstPoint.getPosition().y - secondPoint.getPosition().y);
        double b = Math.abs(firstPoint.getPosition().x - secondPoint.getPosition().x);
        // there must be a little tolerance!
        return (Math.sqrt(a * a + b * b) <= firstPoint.getRadius() + secondPoint.getRadius() - 0.001);
    }

    static boolean isLeftOf(Disk firstPoint, Disk secondPoint) {
        return firstPoint.getPosition().x < secondPoint.getPosition().x;
    }

    static boolean isRightOf(Disk firstPoint, Disk secondPoint) {
        return firstPoint.getPosition().x > secondPoint.getPosition().x;
    }

    static boolean isAboveOf(Disk firstPoint, Disk secondPoint) {
        return firstPoint.getPosition().y > secondPoint.getPosition().y;
    }

    static boolean isBelowOf(Disk firstPoint, Disk secondPoint) {
        return firstPoint.getPosition().y < secondPoint.getPosition().y ;
    }

    static Position calculateCentre(Disk m, Disk n, double angle) {
        angle = correctAngle(angle);
        if (angle == 0 || angle == 360) {
            return new Position(m.getPosition().x, m.getPosition().y + n.getRadius() + m.getRadius(), n.getPosition().z);
        } else if (0 < angle && angle < 90) {
            double c = n.getRadius() + m.getRadius();
            double b = c * Math.cos(Math.toRadians(angle));
            double a = c * Math.sin(Math.toRadians(angle));

            return new Position(m.getPosition().x + a, m.getPosition().y + b, n.getPosition().z);
        } else if (angle == 90) {
            return new Position(m.getPosition().x + n.getRadius() + m.getRadius(), m.getPosition().y, n.getPosition().z);
        } else if (90 < angle && angle < 180) {
            double c = n.getRadius() + m.getRadius();
            double b = c * Math.cos(Math.toRadians(angle - 90));
            double a = c * Math.sin(Math.toRadians(angle - 90));

            return new Position(m.getPosition().x + b, m.getPosition().y - a, n.getPosition().z);
        } else if (angle == 180) {
            return new Position(m.getPosition().x, m.getPosition().y - n.getRadius() - m.getRadius(), n.getPosition().z);
        } else if (180 < angle && angle < 270) {
            double c = n.getRadius() + m.getRadius();
            double b = c * Math.cos(Math.toRadians(angle - 180));
            double a = c * Math.sin(Math.toRadians(angle - 180));

            return new Position(m.getPosition().x - a, m.getPosition().y - b, n.getPosition().z);
        } else if (angle == 270) {
            return new Position(m.getPosition().x - n.getRadius() - m.getRadius(), m.getPosition().y, n.getPosition().z);
        } else if (270 < angle && angle < 360) {
            double c = n.getRadius() + m.getRadius();
            double b = c * Math.cos(Math.toRadians(angle - 270));
            double a = c * Math.sin(Math.toRadians(angle - 270));
            return new Position(m.getPosition().x - b, m.getPosition().y + a, n.getPosition().z);
        }
        return null;
    }

    public String toString() {
        return "x: " + x + " y: " + y;
    }
}
