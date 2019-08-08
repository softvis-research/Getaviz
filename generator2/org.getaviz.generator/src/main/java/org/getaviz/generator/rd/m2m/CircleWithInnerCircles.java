package org.getaviz.generator.rd.m2m;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.rd.s2m.Disk;
import org.getaviz.generator.rd.s2m.DiskSegment;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.database.DatabaseConnector;

class CircleWithInnerCircles extends Circle {
	private ArrayList<CircleWithInnerCircles> innerCircles = new ArrayList<>();
	private Log log = LogFactory.getLog(CircleWithInnerCircles.class);
	private Disk disk;
	private DatabaseConnector connector = DatabaseConnector.getInstance();

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
		serial = connector.getVisualizedEntity(disk.getId()).id() + "";
		netArea = disk.getNetArea();
		log.debug("set netArea to " + netArea + "for disk " + disk.getId());
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
		log.debug("set netArea to " + netArea + "for disk " + disk.getId());
		StatementResult position = connector
				.executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + disk.getId() + " RETURN p");
		double oldZPosition = 0.0;
		if (!position.list().isEmpty()) {
			Node node = position.single().get("p").asNode();
			oldZPosition = node.get("z").asDouble();
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
