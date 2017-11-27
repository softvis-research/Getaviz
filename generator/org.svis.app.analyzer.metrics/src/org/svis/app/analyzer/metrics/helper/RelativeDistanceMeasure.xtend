package org.svis.app.analyzer.metrics.helper

import org.apache.commons.math3.ml.distance.DistanceMeasure
import org.apache.commons.math3.exception.DimensionMismatchException

class RelativeDistanceMeasure implements DistanceMeasure {
	
	override compute(double[] a, double[] b) throws DimensionMismatchException {
	
		val x = 2 * Math::sqrt(a.get(0) / Math::PI)
		val y = 2 * Math::sqrt(b.get(0) / Math::PI)
		

		var double distance 
		if (x > y) {
			distance = x/y
		} else {
			distance = y/x
		}
		return distance
	}
	
}