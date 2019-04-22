package org.getaviz.generator.rd.m2m;

import java.awt.geom.Point2D.Double;
import org.eclipse.xtend.lib.annotations.Accessors

 abstract class Circle implements Comparable<Circle> {

	@Accessors double radius = 0
	@Accessors Double centre = new Double(0,0)
	// TODO extended circle!
	@Accessors double minArea = 0
	@Accessors double netArea
	@Accessors double grossArea
	@Accessors String serial = ""
	@Accessors double ringWidth
	
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