package org.svis.generator.rd.m2m;

import java.awt.geom.Point2D;

public class Util {
	public static double correctAngle(double angle) {
		return angle % 360;
	}

	public static double correctAngleWithin90(double angle) {

		double result = Double.valueOf(angle);
		while (result > 90) {
			result -= 90;
		}
		return result;
	}

	public static double distance(Circle firstPoint, Circle secondPoint) {
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);

		return Math.sqrt(a * a + b * b);
	}

	public static double angleBetweenPoints_XasKatethe(Circle firstPoint, Circle secondPoint) {
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);
		double c = Math.sqrt(a * a + b * b);
		return Math.abs(Math.toDegrees(Math.asin(b / c)));

	}

	public static double angleBetweenPoints_YasKatethe(Circle firstPoint, Circle secondPoint) {
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);
		double c = Math.sqrt(a * a + b * b);
		return Math.abs(Math.toDegrees(Math.asin(a / c)));

	}

	public static boolean intersect(Circle firstPoint, Circle secondPoint) {
		//TODO remove
		//System.out.println(((SnailCircle)secondPoint).getSerial());
		double a = Math.abs(firstPoint.getCentre().y - secondPoint.getCentre().y);
		double b = Math.abs(firstPoint.getCentre().x - secondPoint.getCentre().x);
		// there must be a little tolerance!
		return (Math.sqrt(a * a + b * b) <= firstPoint.getRadius() + secondPoint.getRadius() - 0.001) ? true : false;
	}

	public static boolean isLeftOf(Circle firstPoint, Circle secondPoint) {
		return firstPoint.getCentre().x < secondPoint.getCentre().x ? true : false;
	}

	public static boolean isRightOf(Circle firstPoint, Circle secondPoint) {
		return firstPoint.getCentre().x > secondPoint.getCentre().x ? true : false;
	}

	public static boolean isAboveOf(Circle firstPoint, Circle secondPoint) {
		return firstPoint.getCentre().y > secondPoint.getCentre().y ? true : false;
	}

	public static boolean isBelowOf(Circle firstPoint, Circle secondPoint) {
		return firstPoint.getCentre().y < secondPoint.getCentre().y ? true : false;
	}

	public static Point2D.Double calculateCentre(Circle m, Circle n, double angle) {
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

	public static double calculateAngle(Circle m, Circle n, Circle n_minus_1, double alpha_m_n_minus_1) {

		double rm = m.getRadius();
		double rn = n.getRadius();
		double rn_minus_1 = n_minus_1.getRadius();

		return alpha_m_n_minus_1
				+ Math.toDegrees(Math.acos((rm * rm + rm * rn_minus_1 + rm * rn - rn_minus_1 * rn)
						/ (rm * rm + rm * rn_minus_1 + rm * rn + rn_minus_1 * rn)));
	}
	
}
