package org.getaviz.generator.rd.m2m;

import java.awt.geom.Point2D;
import java.util.List;
import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.getaviz.generator.rd.s2m.Disk;

import java.util.ArrayList;

class RDLayout {
	
	static void nestedLayout(ArrayList<Disk> disksList) {
		layout(disksList);
		transformPositions(disksList);
	}

	private static void layout(List<Disk> diskList) {
		diskList.forEach(disk->  {
			if (disk.getSubDisksList().size() > 0) {
				List<Disk> innerDisks = disk.getSubDisksList();
				layout(innerDisks);
				calculateRadiusForOuterCircles(disk, innerDisks);
			}
		});
		updateArea(diskList);
		Calculator.calculate(diskList);
	}

	private static void transformPositions(List<Disk> disksList) {
		for (Disk disk : disksList) {
			if (disk.getSubDisksList() != null) {
				transformPositionOfInnerCircles(disk,(disk.getSubDisksList()));
				transformPositions(disk.getSubDisksList());
			}
		}
	}
	
	private static void updateArea(List<Disk> disksList) {
		for (Disk disk : disksList) {
			disk.setAreaWithBorder(disk.getRadius() * disk.getRadius() * Math.PI);
			if (disk.getSubDisksList().size() < 1) {
				disk.setAreaWithoutBorder(disk.getAreaWithBorder());
			} else {
				List<Disk> innerDisks = disk.getSubDisksList();
				disk.setAreaWithoutBorder(disk.getAreaWithoutBorder() + (2 * disk.getRadius() - disk.getRingWidth())
						* disk.getRingWidth() * Math.PI);
				
				for (Disk d : innerDisks) {
					disk.setAreaWithoutBorder(disk.getAreaWithoutBorder() + d.getAreaWithoutBorder());
				}
			
			}
		}
	}
	private static void transformPositionOfInnerCircles(Disk outerDisk,
			List<Disk> innerDisks) {
		final double x_outer = outerDisk.getCentre().x;
		final double y_outer = outerDisk.getCentre().y;

		for (Disk disk : innerDisks) {
			disk.getCentre().x += x_outer;
			disk.getCentre().y += y_outer;
		}
	}
	
	private static void calculateRadiusForOuterCircles(Disk outerDisk, List<Disk> innerDisks) {

		CoordinateList coordinates = new CoordinateList();
		for (Disk disk : innerDisks) {
			coordinates.add(createCircle(disk.getCentre().x, disk.getCentre().y, disk.getRadius()).getCoordinates(), false);
		}
		
		GeometryFactory geoFactory = new GeometryFactory();
		MultiPoint innerCirclemultipoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray());
		MinimumBoundingCircle mbc = new MinimumBoundingCircle(innerCirclemultipoint);

//		outerDisk.setCentre(centre);
//		outerDisk.setRadius(RING_WIDTH + radius + calculateB(calculateD(outerDisk.getMinArea(), radius), radius));
//		normalizePositionOfInnerCircles(outerDisk, innerDisks);
		final double radius = mbc.getRadius();
		final Point2D.Double centre = new Point2D.Double(mbc.getCentre().x, mbc.getCentre().y);
		
		outerDisk.setCentre(centre);
		outerDisk.setRadius(outerDisk.getRingWidth() + radius + calculateB(calculateD(outerDisk.getMinArea(), radius), radius));
		normalizePositionOfInnerCircles(outerDisk, innerDisks);
	}

    public static Geometry createCircle(double x, double y, final double RADIUS) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(64);
        shapeFactory.setCentre(new Coordinate(x, y));
        shapeFactory.setSize(RADIUS * 2);
        return shapeFactory.createCircle();
    }
	
	private static double calculateD(double area, double radius) {
		return Math.sqrt((2 * radius) * (2 * radius) + (area / Math.PI) * 4);
	}

	private static double calculateB(double D, double radius) {
		return (D - (2 * radius)) / 2;
	}

	private static void normalizePositionOfInnerCircles(Disk outerDisk,
			List<Disk> innerDisks) {
		final double x_outer = outerDisk.getCentre().x;
		final double y_outer = outerDisk.getCentre().y;

		for (Disk disk : innerDisks) {
			disk.getCentre().x -= x_outer;
			disk.getCentre().y -= y_outer;
		}
		outerDisk.setCentre(new Point2D.Double(0, 0));
	}
}
