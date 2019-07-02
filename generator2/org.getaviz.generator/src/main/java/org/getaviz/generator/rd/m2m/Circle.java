package org.getaviz.generator.rd.m2m;

import java.awt.geom.Point2D.Double;

abstract class Circle implements Comparable<Circle> {

	protected double radius = 0;
	Double centre = new Double(0, 0);
	double minArea = 0;
	double netArea;
	double grossArea;
	String serial = "";
	double ringWidth;

	public int compareTo(Circle circle) {
		return java.lang.Double.compare(circle.getNetArea(), netArea);
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

	double getNetArea() {
		return netArea;
	}

	void setNetArea(double netArea) {
		this.netArea = netArea;
	}

	double getGrossArea() {
		return grossArea;
	}

	void setGrossArea(double grossArea) {
		this.grossArea = grossArea;
	}

	double getRingWidth() {
		return ringWidth;
	}
}