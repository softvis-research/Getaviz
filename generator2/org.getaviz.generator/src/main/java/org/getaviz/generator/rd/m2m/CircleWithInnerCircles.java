package org.getaviz.generator.rd.m2m;

import java.util.ArrayList;
import org.getaviz.generator.rd.s2m.Disk;
import org.getaviz.generator.rd.s2m.DiskSegment;

class CircleWithInnerCircles extends Circle {
	private ArrayList<CircleWithInnerCircles> innerCircles = new ArrayList<>();
	private Disk disk;

	CircleWithInnerCircles(Disk disk, Boolean nesting) {
		this.disk = disk;
		ArrayList<DiskSegment> innerSegments = disk.getInnerSegments();
		ArrayList<DiskSegment> outerSegments = disk.getOuterSegments();
		if (nesting) {
			minArea = Disk.sum(outerSegments) + Disk.sum(innerSegments);
		} else {
			minArea = disk.getAreaWithoutBorder();
		}
		ringWidth = disk.getRingWidth();
		serial =  disk.getVisualizedNodeID() + "";
		areaWithoutBorder = disk.getAreaWithoutBorder();
		radius = disk.getRadius();
		areaWithBorder = disk.getAreaWithBorder();
		ArrayList<Disk> subDisks = RD2RD.getSubDisk(disk.getId());
		subDisks.forEach(d -> {
			CircleWithInnerCircles circle = new CircleWithInnerCircles(d,true);
			innerCircles.add(circle);
		});
	}
	/**
	 * write calculated positions into extended disk
	 * 
	 */
	void updateDiskNode() {
		double oldZPosition = 0.0;
		if (disk.getPosZ() != null) {
			oldZPosition = disk.getPosZ();
		}
		disk.setRadius(radius);
		disk.setAreaWithoutBorder(areaWithoutBorder);
		disk.setAreaWithBorder(areaWithBorder);
		disk.setPosition(centre.x, centre.y, oldZPosition);
		for (CircleWithInnerCircles circle : innerCircles) {
			circle.updateDiskNode();
		}
	}

	ArrayList<CircleWithInnerCircles> getInnerCircles() {
		return innerCircles;
	}
}
