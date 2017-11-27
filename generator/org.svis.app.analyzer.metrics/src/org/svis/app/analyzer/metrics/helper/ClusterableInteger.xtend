package org.svis.app.analyzer.metrics.helper

import org.apache.commons.math3.ml.clustering.Clusterable
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.List
import java.util.ArrayList

class ClusterableInteger implements Clusterable {
    @Accessors double[] point
	
    new (Double value) {
    	this.point = #[value]
    }
    
    def static toClusterable(List<Double> list) {
		val result = new ArrayList<ClusterableInteger>
		list.forEach[l|
			result.add(new ClusterableInteger(l))
		]
		return result
	}
}
