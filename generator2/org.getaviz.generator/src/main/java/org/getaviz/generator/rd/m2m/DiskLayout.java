package org.getaviz.generator.rd.m2m;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import org.getaviz.generator.rd.Disk;
import org.getaviz.generator.rd.MainDisk;
import org.getaviz.generator.rd.SubDisk;

import java.util.Collections;
import java.util.List;

class DiskLayout {
	//private final Log log = LogFactory.getLog(this.getClass());
	private final List<MainDisk> rootDisks;

	DiskLayout(List<MainDisk> rootDisks) {
		this.rootDisks = rootDisks;
	}
	
	void run() {
		layout(rootDisks);
		transformPositions(rootDisks);
	}

	private <T extends Disk> void layout(List<T> diskList) {
		diskList.forEach(disk ->  {
			if (disk.hasInnerDisks()) {
				layout(disk.getInnerDisks());
				calculateRadiusForOuterDisks(disk);
			} else {
				if(disk instanceof  SubDisk) {
					calculateRadiusForInnerDisks((SubDisk) disk);
				}
			}
		});
		calculate(diskList);
	}

	private  <T extends Disk> void transformPositions(List<T> disks) {
		disks.forEach( (disk) -> {
			transformPositionOfInnerDisks(disk);
			transformPositions(disk.getInnerDisks());
		});
	}

	private void calculateRadiusForOuterDisks(Disk disk) {
		CoordinateList coordinates = new CoordinateList();
		disk.getInnerDisks().forEach((innerDisk) -> coordinates.add(innerDisk.getCoordinates(), false));

		GeometryFactory geoFactory = new GeometryFactory();
		MultiPoint innerCirclemultipoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray());
		MinimumBoundingCircle mbc = new MinimumBoundingCircle(innerCirclemultipoint);

		final double radius = mbc.getRadius();

		disk.updatePosition(mbc.getCentre().x,mbc.getCentre().y);
		if(disk instanceof MainDisk) {
			disk.setRadius(disk.getBorderWidth() + radius);
		} else {
			SubDisk subDisk = (SubDisk) disk;
			disk.setRadius(disk.getBorderWidth() + Math.sqrt(radius * radius + (subDisk.getInnerSegmentsArea() + subDisk.getOuterSegmentsArea())/Math.PI));
		}
		disk.setInnerRadius(radius);
		normalizePositionOfInnerDisks(disk);
	}

	private void calculateRadiusForInnerDisks(SubDisk disk) {
		disk.setRadius(Math.sqrt((disk.getInnerSegmentsArea() + disk.getOuterSegmentsArea()) / Math.PI) + disk.getBorderWidth());
	}

	private void normalizePositionOfInnerDisks(Disk disk) {
		disk.getInnerDisks().forEach( (innerDisk) -> {
			innerDisk.getPosition().x -= disk.getPosition().x;
			innerDisk.getPosition().y -= disk.getPosition().y;
		});
		disk.setPosition(new Position(0, 0, disk.getPosition().z));
	}

	private void transformPositionOfInnerDisks(Disk disk) {
		disk.getInnerDisks().forEach( (innerDisk) -> {
			Position pos = innerDisk.getPosition();
			pos.x += disk.getPosition().x;
			pos.y += disk.getPosition().y;
		});
	}

	private <T extends Disk> void calculate(final List<T> disksList) {

		if (disksList == null || disksList.isEmpty()) {
			return;
		}

		Collections.sort(disksList);

		Disk first = disksList.get(0);
		first.updatePosition(0, 0);
		if (disksList.size() < 2) {
			return;
		}

		Disk second = disksList.get(1);

		int m = 0;
		double angleBetweenNAndM = 0;
		second.setPosition(Position.calculateCentre(first, second, angleBetweenNAndM));

		// start from the third circle, because the first and the second are fix

		for (int n = 2; n < disksList.size(); n++) {
			Disk n_disk = disksList.get(n);
			Disk n_minus1_disk = disksList.get(n - 1);
			Disk m_disk = disksList.get(m);

			// 1. calculate triangleangle. this is the angle in the triangle (m,n,n-1)
			// so angle + triangle_angle will be the angle of the new circle
			// at this point angle ist the angle between m and n-1
			double triangle_angle;
			double a = n_disk.getRadius() + n_minus1_disk.getRadius();
			double b = m_disk.getRadius() + n_disk.getRadius();
			double c = m_disk.getRadius() + n_minus1_disk.getRadius();

			triangle_angle = Math.toDegrees(Math.acos((a * a - b * b - c * c) / (-2 * b * c)));
			if(Double.isNaN(triangle_angle)) {
				triangle_angle = 0;
			}

			n_disk.setPosition(Position.calculateCentre(m_disk, n_disk, angleBetweenNAndM + triangle_angle));

			// 3. check intersect of new circle (n) with circle m+1
			if (!Position.intersect(disksList.get(m + 1), n_disk)) {

				// 3.1 if no intersect
				angleBetweenNAndM += triangle_angle;
				angleBetweenNAndM = Position.correctAngle(angleBetweenNAndM);
				// angle is now the angle between m and n
			} else {
				angleBetweenNAndM = fixPosition(disksList, n_disk, n_minus1_disk, m, n);
				m++;
			}
		}
	}

	public <T extends Disk> double fixPosition(List<T> disksList, Disk n_disk, Disk n_minus1_disk, int m, int n) {
		// 3.2 if n intersects with m+1
		// angle2 is the same angle as 'angle', but between m+1 and n-1

		// if the circles are not ordered, intersect must be checked for
		// all circles that are are at the same side in the range of m
		// to n-1
		Disk m_plus1_disk = disksList.get(m + 1);
		double angle2;
		double a = n_disk.getRadius() + n_minus1_disk.getRadius();
		double b = Position.distance(disksList.get(m + 1), disksList.get(n - 1));
		double c = m_plus1_disk.getRadius() + n_disk.getRadius();

		// alpha is the angle in the triangle (m,n-1,n)
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
		double angleBetweenNAndM = Position.correctAngle(angle2 + alpha);

		// calculate the centre of the new circle
		n_disk.setPosition(Position.calculateCentre(disksList.get(m + 1), n_disk, angleBetweenNAndM));
		m++;

		// angle is now the angle between m and n
		if (Position.intersect(disksList.get(m + 1), n_disk)) {
			angleBetweenNAndM = fixPosition(disksList, n_disk, n_minus1_disk, m, n);
		}

		return angleBetweenNAndM;
	}
}
