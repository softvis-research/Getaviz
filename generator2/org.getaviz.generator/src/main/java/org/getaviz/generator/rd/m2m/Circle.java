package org.getaviz.generator.rd.m2m;

import java.awt.geom.Point2D.Double;

abstract class Circle implements Comparable<Circle> {

	protected double radius = 0;

//	@Accessors double radius = 0
	protected Double centre = new Double(0, 0);
	protected double minArea = 0;
	protected double netArea;
	protected double grossArea;
	protected String serial = "";
	protected double ringWidth;

	public int compareTo(Circle circle) {
		if (netArea < circle.getNetArea()) {
			return 1;
		} else if (netArea > circle.getNetArea()) {
			return -1;
		} else {
			return 0;
		}
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public Double getCentre() {
		return centre;
	}

	public void setCentre(Double centre) {
		this.centre = centre;
	}

	public double getMinArea() {
		return minArea;
	}

	public double getNetArea() {
		return netArea;
	}

	public void setNetArea(double netArea) {
		this.netArea = netArea;
	}

	public double getGrossArea() {
		return grossArea;
	}

	public void setGrossArea(double grossArea) {
		this.grossArea = grossArea;
	}

	public double getRingWidth() {
		return ringWidth;
	}
}