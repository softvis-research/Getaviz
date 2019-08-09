package org.getaviz.generator.rd.m2m;

import java.util.ArrayList;
import org.getaviz.generator.rd.s2m.Disk;
import org.getaviz.generator.rd.s2m.DiskSegment;

class CircleWithInnerCircles extends Circle {
	private ArrayList<CircleWithInnerCircles> innerCircles = new ArrayList<>();
	private Disk disk;

	CircleWithInnerCircles(Disk disk, Boolean nesting) {
		this.disk = disk;
		ArrayList<DiskSegment> data = RD2RD.getDiskSegment(disk.getId(), RD2RD.getFields());
		ArrayList<DiskSegment> method = RD2RD.getDiskSegment(disk.getId(), RD2RD.getMethods());
		if (nesting) {
			minArea = RD2RD.sum(method) + RD2RD.sum(data);
		} else {
			minArea = disk.getNetArea();
		}
		ringWidth = disk.getRingWidth();
		serial =  disk.getVisualizedNodeID() + "";
		netArea = disk.getNetArea();
		radius = disk.getRadius();
		grossArea = disk.getGrossArea();
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
		disk.setNetArea(netArea);
		disk.setGrossArea(grossArea);
		disk.setPosition(centre.x, centre.y, oldZPosition);
		for (CircleWithInnerCircles circle : innerCircles) {
			circle.updateDiskNode();
		}
	}

	ArrayList<CircleWithInnerCircles> getInnerCircles() {
		return innerCircles;
	}
}
