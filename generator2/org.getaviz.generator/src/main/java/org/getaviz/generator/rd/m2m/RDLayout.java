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
import java.util.ArrayList;

class RDLayout {
	
	static void nestedLayout(ArrayList<CircleWithInnerCircles> circleList) {
		layout(circleList);
		transformPositions(circleList);
	}

	private static void layout(List<CircleWithInnerCircles> circleList) {
		for (CircleWithInnerCircles circle : circleList) {
			if (circle.getInnerCircles().size() > 0) {
				List<CircleWithInnerCircles> innerCircles = circle.getInnerCircles();
				layout(innerCircles);
				calculateRadiusForOuterCircles(circle,innerCircles);
			}
		}
		updateArea(circleList);
		Calculator.calculate(circleList);
	}

	private static void transformPositions(List<CircleWithInnerCircles> circleList) {
		for (Circle circle : circleList) {
			if (((CircleWithInnerCircles) circle).getInnerCircles() != null) {
				transformPositionOfInnerCircles(circle,((CircleWithInnerCircles) circle).getInnerCircles());
				transformPositions(((CircleWithInnerCircles) circle).getInnerCircles());
			}
		}
	}
	
	private static void updateArea(List<CircleWithInnerCircles> circleList) {
		for (CircleWithInnerCircles circle : circleList) {
			circle.setGrossArea(circle.getRadius() * circle.getRadius() * Math.PI);
			if (circle.getInnerCircles().size() < 1) {
				circle.setNetArea(circle.getGrossArea());
			} else {
				List<CircleWithInnerCircles> innerCircles = circle.getInnerCircles();
				circle.setNetArea(circle.getNetArea() + (2 * circle.getRadius() - circle.getRingWidth()) * circle.getRingWidth() * Math.PI);
				
				for (CircleWithInnerCircles c : innerCircles) {
					circle.setNetArea(circle.getNetArea() + c.getNetArea());
				}
			
			}
		}
	}
	private static void transformPositionOfInnerCircles(Circle outerCircle,
			List<CircleWithInnerCircles> innerCircles) {
		final double x_outer = outerCircle.getCentre().x;
		final double y_outer = outerCircle.getCentre().y;

		for (Circle circle : innerCircles) {
			circle.getCentre().x += x_outer;
			circle.getCentre().y += y_outer;
		}
	}
	
	private static void calculateRadiusForOuterCircles(CircleWithInnerCircles outerCircle, List<CircleWithInnerCircles> innerCircles) {

		CoordinateList coordinates = new CoordinateList();
		for (CircleWithInnerCircles circle : innerCircles) {
			coordinates.add(createCircle(circle.getCentre().x, circle.getCentre().y, circle.getRadius()).getCoordinates(), false);
		}
		
		GeometryFactory geoFactory = new GeometryFactory();
		MultiPoint innerCirclemultipoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray());
		MinimumBoundingCircle mbc = new MinimumBoundingCircle(innerCirclemultipoint);

//		outerCircle.setCentre(centre);
//		outerCircle.setRadius(RING_WIDTH + radius + calculateB(calculateD(outerCircle.getMinArea(), radius), radius));
//		normalizePositionOfInnerCircles(outerCircle, innerCircles);
		final double radius = mbc.getRadius();
		final Point2D.Double centre = new Point2D.Double(mbc.getCentre().x, mbc.getCentre().y);
		
		outerCircle.setCentre(centre);
		outerCircle.setRadius(outerCircle.getRingWidth() + radius + calculateB(calculateD(outerCircle.getMinArea(), radius), radius));
		normalizePositionOfInnerCircles(outerCircle, innerCircles);
	}
	
	private static double calculateD(double area, double radius) {
		return Math.sqrt((2 * radius) * (2 * radius) + (area / Math.PI) * 4);
	}

	private static double calculateB(double D, double radius) {
		return (D - (2 * radius)) / 2;
	}

	private static Geometry createCircle(double x, double y, final double RADIUS) {
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(64);
		shapeFactory.setCentre(new Coordinate(x, y));
		shapeFactory.setSize(RADIUS * 2);
		return shapeFactory.createCircle();
	}
	
	private static void normalizePositionOfInnerCircles(Circle outerCircle,
			List<CircleWithInnerCircles> innerCircles) {
		final double x_outer = outerCircle.getCentre().x;
		final double y_outer = outerCircle.getCentre().y;

		for (Circle circle : innerCircles) {
			circle.getCentre().x -= x_outer;
			circle.getCentre().y -= y_outer;
		}
		outerCircle.setCentre(new Point2D.Double(0, 0));
	}
}
