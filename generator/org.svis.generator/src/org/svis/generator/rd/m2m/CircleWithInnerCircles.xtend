package org.svis.generator.rd.m2m;

import java.util.ArrayList
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.impl.RdFactoryImpl
import org.svis.generator.FamixUtils
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * @author Rimue
 *
 */
//TODO rename
public class CircleWithInnerCircles extends Circle {
	@Accessors int level
	@Accessors val innerCircles = new ArrayList<CircleWithInnerCircles>
	 val diskFactory = new RdFactoryImpl
	 var Disk disk
	
	extension FamixUtils util = new FamixUtils
	
	new (Disk disk) {
		this(disk, false)
	}
	
	new (Disk disk_, Boolean nesting) {
		disk = disk_
		if(nesting == true) {
			minArea = disk.methods.sum + disk.data.sum
		} else {
			minArea = disk.netArea
			level = disk.level
		}
		ringWidth = disk.ringWidth
		serial = disk.name
		netArea = disk.netArea
		radius = disk.radius
		grossArea = disk.grossArea
		disk.disks.forEach[f|innerCircles.add(new CircleWithInnerCircles(f, true))]
	}
	
	/**
	 * write calculated positions into extended disk
	 * 
	 */
	def void updateDisk() {
		disk.radius = radius
		disk.netArea = netArea
		disk.grossArea = grossArea
		var oldZPosition = 0.0
		if (disk.position !== null) {
			oldZPosition = disk.position.z
		}
		
		disk.position = diskFactory.createPosition
		disk.position.x = centre.x
		disk.position.y = centre.y
		
		disk.position.z = oldZPosition
		innerCircles.forEach[updateDisk]
	}
}