package org.getaviz.generator.rd.m2m;

import java.awt.geom.Point2D.Double;

abstract class Circle implements Comparable<Circle> {

	protected double radius = 0;
	Double centre = new Double(0, 0);
	double minArea = 0;
	double areaWithoutBorder;
	double areaWithBorder;
	String serial = "";
	double ringWidth;

	public int compareTo(Circle circle) {
		return java.lang.Double.compare(circle.getAreaWithoutBorder(), areaWithoutBorder);
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	Double getCentre() {
		return centre;
	}

	void setCentre(Double centre) {
		this.centre = centre;
	}

	double getMinArea() {
		return minArea;
	}

	double getAreaWithoutBorder() {
		return areaWithoutBorder;
	}

	void setAreaWithoutBorder(double areaWithoutBorder) {
		this.areaWithoutBorder = areaWithoutBorder;
	}

	double getAreaWithBorder() {
		return areaWithBorder;
	}

	void setAreaWithBorder(double areaWithBorder) {
		this.areaWithBorder = areaWithBorder;
	}

	double getRingWidth() {
		return ringWidth;
	}
}