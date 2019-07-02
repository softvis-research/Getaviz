package org.getaviz.generator.rd.m2m;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

class Calculator {

	static void calculate(final List<CircleWithInnerCircles> circleList) {

		if (circleList == null || circleList.size() == 0) {
			return;
		}

		Collections.sort(circleList);
		int m = 0;

		// angle is the angle between m and n
		double angle = 0;
		// List<ICircle> circleList = new ArrayList<ICircle>();

		// Instantiate the first circle
		Circle first = circleList.get(0);
		first.setCentre(new Point2D.Double(0, 0));

		if (circleList.size() < 2) {
			return;
		}

		// Instantiate the second circle
		Circle second = circleList.get(1);

		second.setCentre(Util.calculateCentre(first, second, angle));

		// start from the third circle, because the first and the second are fix

		for (int n = 2; n < circleList.size(); n++) {

			// new circle
			Circle n_circle = circleList.get(n);

			// previous circle
			Circle n_minus1_circle = circleList.get(n - 1);

			// m circle
			Circle m_circle = circleList.get(m);

			// 1. calculate triangleangle. this is the angle in the triangle
			// (m,n,n-1)
			// so angle + triangle_angle will be the angle of the new circle
			// at this point angle ist the angle between m and n-1
			double triangle_angle;
			double a = n_circle.getRadius() + n_minus1_circle.getRadius();
			double b = m_circle.getRadius() + n_circle.getRadius();
			double c = m_circle.getRadius() + n_minus1_circle.getRadius();

			triangle_angle = Math.toDegrees(Math.acos((a * a - b * b - c * c) / (-2 * b * c)));
			// 2. calc centre of new circle (n)
			n_circle.setCentre(Util.calculateCentre(circleList.get(m), n_circle, angle + triangle_angle));
			// 3. check intersect of new circle (n) with circle m+1
			if (!Util.intersect(circleList.get(m + 1), n_circle)) {

				// 3.1 if no intersect
				angle += triangle_angle;
				angle = Util.correctAngle(angle);
				// angle is now the angle between m and n
			} else {
				// 3.2 if n intersects with m+1
				// angle2 is the same angle as 'angle', but between m+1 and n-1

				// if the circles are not ordered, intersect must be checked for
				// all circles that are are at the same side in the range of m
				// to n-1
				Circle m_plus1_cirle = circleList.get(m + 1);
				double angle2;
				a = n_circle.getRadius() + n_minus1_circle.getRadius();
				b = Util.distance(circleList.get(m + 1), circleList.get(n - 1));
				c = m_plus1_cirle.getRadius() + n_circle.getRadius();

				// alpha is the angle in the trangle (m,n-1,n)
				// alpha + angle2 is the total angle between m+1 and n
				double alpha = Math.toDegrees(Math.acos((a * a - b * b - c * c) / (-2 * b * c)));

				if (Util.isLeftOf(circleList.get(n - 1), circleList.get(m + 1))) {
					if (Util.isAboveOf(circleList.get(n - 1), circleList.get(m + 1))) {
						// upper left
						angle2 = Util.correctAngle(360 - Util.angleBetweenPoints_XasKatethe(circleList.get(m + 1),
								circleList.get(n - 1)));
					} else if (Util.isBelowOf(circleList.get(n - 1), circleList.get(m + 1))) {
						// bottom left
						angle2 = Util.correctAngle(180 + Util.angleBetweenPoints_XasKatethe(circleList.get(m + 1),
								circleList.get(n - 1)));
					} else {
						// total left
						angle2 = 270;

					}
				} else if (Util.isRightOf(circleList.get(n - 1), circleList.get(m + 1))) {
					if (Util.isAboveOf(circleList.get(n - 1), circleList.get(m + 1))) {
						// upper right
						angle2 = Util.correctAngle(Util.angleBetweenPoints_XasKatethe(circleList.get(m + 1),
								circleList.get(n - 1)));

					} else if (Util.isBelowOf(circleList.get(n - 1), circleList.get(m + 1))) {
						// bottom right
						angle2 = Util.correctAngle(90 + Util.angleBetweenPoints_YasKatethe(circleList.get(m + 1),
								circleList.get(n - 1)));
					} else {
						// total right
						angle2 = 90;
					}
				} else {
					if (Util.isAboveOf(circleList.get(n - 1), circleList.get(m + 1))) {
						// total above
						angle2 = 0;
					} else {
						// total below
						angle2 = 180;
					}
				}

				// angle will be the angle between m+1 and the new circle n
				angle = Util.correctAngle(angle2 + alpha);

				// calculate the centre of the new circle
				n_circle.setCentre(Util.calculateCentre(circleList.get(m + 1), n_circle, angle));

				// increase m
				m += 1;

				// angle is now the angle between m and n
			}
		}
		// return the result
	}
}
