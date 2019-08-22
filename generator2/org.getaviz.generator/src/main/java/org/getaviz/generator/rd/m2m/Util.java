package org.getaviz.generator.rd.m2m;

import org.getaviz.generator.rd.s2m.Disk;

import java.awt.geom.Point2D;

class Util {
	static double correctAngle(double angle) {
		return angle % 360;
	}

	static double distance(Disk firstPoint, Disk secondPoint) {
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);

		return Math.sqrt(a * a + b * b);
	}

	static double angleBetweenPoints_XasKatethe(Disk firstPoint, Disk secondPoint) {
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);
		double c = Math.sqrt(a * a + b * b);
		return Math.abs(Math.toDegrees(Math.asin(b / c)));

	}

	static double angleBetweenPoints_YasKatethe(Disk firstPoint, Disk secondPoint) {
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);
		double c = Math.sqrt(a * a + b * b);
		return Math.abs(Math.toDegrees(Math.asin(a / c)));

	}

	static boolean intersect(Disk firstPoint, Disk secondPoint) {
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);
		// there must be a little tolerance!
		return (Math.sqrt(a * a + b * b) <= firstPoint.getRadius() + secondPoint.getRadius() - 0.001);
	}

	static boolean isLeftOf(Disk firstPoint, Disk secondPoint) {
		return firstPoint.getCentre().x < secondPoint.getCentre().x;
	}

	static boolean isRightOf(Disk firstPoint, Disk secondPoint) {
		return firstPoint.getCentre().x > secondPoint.getCentre().x;
	}

	static boolean isAboveOf(Disk firstPoint, Disk secondPoint) {
		return firstPoint.getCentre().y > secondPoint.getCentre().y;
	}

	static boolean isBelowOf(Disk firstPoint, Disk secondPoint) {
		return firstPoint.getCentre().y < secondPoint.getCentre().y ;
	}

	static Point2D.Double calculateCentre(Disk m, Disk n, double angle) {
		angle = Util.correctAngle(angle);
		if (angle == 0 || angle == 360) {
			return new Point2D.Double(m.getCentre().x, m.getCentre().y + n.getRadius() + m.getRadius());
		} else if (0 < angle && angle < 90) {
			double c = n.getRadius() + m.getRadius();
			double b = c * Math.cos(Math.toRadians(angle));
			double a = c * Math.sin(Math.toRadians(angle));

			return new Point2D.Double(m.getCentre().x + a, m.getCentre().y + b);
		} else if (angle == 90) {
			return new Point2D.Double(m.getCentre().x + n.getRadius() + m.getRadius(), m.getCentre().y);
		} else if (90 < angle && angle < 180) {
			double c = n.getRadius() + m.getRadius();
			double b = c * Math.cos(Math.toRadians(angle - 90));
			double a = c * Math.sin(Math.toRadians(angle - 90));

			return new Point2D.Double(m.getCentre().x + b, m.getCentre().y - a);
		} else if (angle == 180) {
			return new Point2D.Double(m.getCentre().x, m.getCentre().y - n.getRadius() - m.getRadius());
		} else if (180 < angle && angle < 270) {
			double c = n.getRadius() + m.getRadius();
			double b = c * Math.cos(Math.toRadians(angle - 180));
			double a = c * Math.sin(Math.toRadians(angle - 180));

			return new Point2D.Double(m.getCentre().x - a, m.getCentre().y - b);
		} else if (angle == 270) {
			return new Point2D.Double(m.getCentre().x - n.getRadius() - m.getRadius(), m.getCentre().y);
		} else if (270 < angle && angle < 360) {
			double c = n.getRadius() + m.getRadius();
			double b = c * Math.cos(Math.toRadians(angle - 270));
			double a = c * Math.sin(Math.toRadians(angle - 270));
			return new Point2D.Double(m.getCentre().x - b, m.getCentre().y + a);
		}
		return null;
	}
}
