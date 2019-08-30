package org.getaviz.generator.rd.m2m;

import org.getaviz.generator.rd.s2m.Disk;

import java.util.Collections;
import java.util.List;

class Calculator {

	static void calculate(final List<Disk> disksList) {

		if (disksList == null || disksList.size() == 0) {
			return;
		}

		Collections.sort(disksList);
		int m = 0;

		// angle is the angle between m and n
		double angle = 0;
		// List<ICircle> disksList = new ArrayList<ICircle>();

		// Instantiate the first circle
		Disk first = disksList.get(0);
		first.setPosition(new Position(0, 0, first.getPosition().z));

		if (disksList.size() < 2) {
			return;
		}

		// Instantiate the second circle
		Disk second = disksList.get(1);

		second.setPosition(Position.calculateCentre(first, second, angle));

		// start from the third circle, because the first and the second are fix

		for (int n = 2; n < disksList.size(); n++) {

			// new circle
			Disk n_disk = disksList.get(n);

			// previous circle
			Disk n_minus1_disk = disksList.get(n - 1);

			// m circle
			Disk m_disk = disksList.get(m);

			// 1. calculate triangleangle. this is the angle in the triangle
			// (m,n,n-1)
			// so angle + triangle_angle will be the angle of the new circle
			// at this point angle ist the angle between m and n-1
			double triangle_angle;
			double a = n_disk.getRadius() + n_minus1_disk.getRadius();
			double b = m_disk.getRadius() + n_disk.getRadius();
			double c = m_disk.getRadius() + n_minus1_disk.getRadius();

			triangle_angle = Math.toDegrees(Math.acos((a * a - b * b - c * c) / (-2 * b * c)));
			// 2. calc centre of new circle (n)
			n_disk.setPosition(Position.calculateCentre(disksList.get(m), n_disk, angle + triangle_angle));
			// 3. check intersect of new circle (n) with circle m+1
			if (!Position.intersect(disksList.get(m + 1), n_disk)) {

				// 3.1 if no intersect
				angle += triangle_angle;
				angle = Position.correctAngle(angle);
				// angle is now the angle between m and n
			} else {
				// 3.2 if n intersects with m+1
				// angle2 is the same angle as 'angle', but between m+1 and n-1

				// if the circles are not ordered, intersect must be checked for
				// all circles that are are at the same side in the range of m
				// to n-1
				Disk m_plus1_disk = disksList.get(m + 1);
				double angle2;
				a = n_disk.getRadius() + n_minus1_disk.getRadius();
				b = Position.distance(disksList.get(m + 1), disksList.get(n - 1));
				c = m_plus1_disk.getRadius() + n_disk.getRadius();

				// alpha is the angle in the trangle (m,n-1,n)
				// alpha + angle2 is the total angle between m+1 and n
				double alpha = Math.toDegrees(Math.acos((a * a - b * b - c * c) / (-2 * b * c)));

				if (Position.isLeftOf(disksList.get(n - 1), disksList.get(m + 1))) {
					if (Position.isAboveOf(disksList.get(n - 1), disksList.get(m + 1))) {
						// upper left
						angle2 = Position.correctAngle(360 - Position.angleBetweenPoints_XasKatethe(disksList.get(m + 1),
								disksList.get(n - 1)));
					} else if (Position.isBelowOf(disksList.get(n - 1), disksList.get(m + 1))) {
						// bottom left
						angle2 = Position.correctAngle(180 + Position.angleBetweenPoints_XasKatethe(disksList.get(m + 1),
								disksList.get(n - 1)));
					} else {
						// total left
						angle2 = 270;

					}
				} else if (Position.isRightOf(disksList.get(n - 1), disksList.get(m + 1))) {
					if (Position.isAboveOf(disksList.get(n - 1), disksList.get(m + 1))) {
						// upper right
						angle2 = Position.correctAngle(Position.angleBetweenPoints_XasKatethe(disksList.get(m + 1),
								disksList.get(n - 1)));

					} else if (Position.isBelowOf(disksList.get(n - 1), disksList.get(m + 1))) {
						// bottom right
						angle2 = Position.correctAngle(90 + Position.angleBetweenPoints_YasKatethe(disksList.get(m + 1),
								disksList.get(n - 1)));
					} else {
						// total right
						angle2 = 90;
					}
				} else {
					if (Position.isAboveOf(disksList.get(n - 1), disksList.get(m + 1))) {
						// total above
						angle2 = 0;
					} else {
						// total below
						angle2 = 180;
					}
				}

				// angle will be the angle between m+1 and the new circle n
				angle = Position.correctAngle(angle2 + alpha);

				// calculate the centre of the new circle
				n_disk.setPosition(Position.calculateCentre(disksList.get(m + 1), n_disk, angle));

				// increase m
				m += 1;

				// angle is now the angle between m and n
			}
		}
		// return the result
	}
}
