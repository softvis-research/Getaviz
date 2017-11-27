package org.svis.generator.rd.m2m;

import java.awt.geom.Point2D.Double;

public abstract class Circle implements Comparable<Circle> {

	@Property double radius = 0
	@Property Double centre = new Double(0,0)
	// TODO extended circle!
	@Property double minArea = 0
	@Property double netArea
	@Property double grossArea
	@Property String serial = ""
	@Property double ringWidth
	
	
	override int compareTo(Circle circle) {
		if (netArea < circle.netArea) {
			return 1
		} else if (netArea > circle.netArea) {
			return -1
		} else {
			return 0
		}
	}	
}