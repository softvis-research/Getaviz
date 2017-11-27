package org.svis.lib.database.utils

import org.svis.xtext.rd.Disk
import org.eclipse.emf.common.util.ECollections
import org.apache.commons.beanutils.BeanComparator
import org.apache.commons.collections.comparators.ReverseComparator
import org.apache.commons.collections.comparators.ComparableComparator

class RDUtils {
	
	/**
	 * Calculates the number of class disk layers for a given package
	 * A everytime the class disks complete a full circle, a new layer is counted
	 * 
	 * @param disk the package the value is calculated for
	 * @return the number of layers for the package disk
	 */
	
	def calculateNumberOfLayers(Disk disk) {
		val comparator = new BeanComparator("grossArea", new ReverseComparator(new ComparableComparator()))
		ECollections::sort(disk.disks, comparator)
		val x = disk.disks.get(0).position.x
		val y =	disk.disks.get(0).position.y
		var layers = 0
		var correctQuadrant = true
		
		for (subDisk : disk.disks) {
			if (subDisk.position.x.compareTo(x) >= 0 && subDisk.position.y.compareTo(y) >= 0 && correctQuadrant == true) {
				layers++
				correctQuadrant = false
			} else if (subDisk.position.x.compareTo(x) < 0 && subDisk.position.y.compareTo(y) < 0 && correctQuadrant == false) {
				correctQuadrant = true
			}
		}
		return layers 
	}
	
		/**
	 * If multiple package disks "stick together" since their are no other children, it appear to be one very thick disk
	 * This function calculates this thickness
	 * 
	 * @param disk root package disk
	 * @return summarized thickness of package disks
	 */
	
	def double calculateThickness(Disk disk) {
		if (disk.disks.size == 1 && disk.disks.last.type == "FAMIXNamespace") {
			return disk.ringWidth + calculateThickness(disk.disks.last)
		}
		return disk.ringWidth
	}
}