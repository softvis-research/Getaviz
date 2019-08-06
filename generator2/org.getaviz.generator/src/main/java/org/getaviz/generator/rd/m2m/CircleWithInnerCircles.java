package org.getaviz.generator.rd.m2m;

import java.util.ArrayList;
import org.getaviz.generator.rd.RDUtils;
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
	private Node diskNode;
	private Disk disk;
	private DatabaseConnector connector = DatabaseConnector.getInstance();

	/*CircleWithInnerCircles(Node disk, Boolean nesting) {
		diskNode = disk;
		Iterable<Node> data = () -> RDUtils.getData(disk.id());
		Iterable<Node> methods = () -> RDUtils.getMethods(disk.id());
		if (nesting) {
			minArea = RDUtils.sum(methods) + RDUtils.sum(data);
		} else {
			minArea = disk.get("netArea").asDouble();
		}
		ringWidth = disk.get("ringWidth").asDouble();
		serial = connector.getVisualizedEntity(disk.id()).id() + "";
		netArea = disk.get("netArea").asDouble();
		log.debug("set netArea to " + netArea + "for disk " + diskNode.id());
		radius = disk.get("radius").asDouble(0.0);
		grossArea = disk.get("grossArea").asDouble(0.0);
		RDUtils.getSubDisks(disk.id()).forEachRemaining((subDisk) -> innerCircles.add(new CircleWithInnerCircles(subDisk.get("d").asNode(), true)));
	}*/

	CircleWithInnerCircles(Disk disk, Boolean nesting) {
		this.disk = disk;
		diskNode = disk.getNode();
		//Iterable<Node> data = () -> RDUtils.getData(disk.getId());
		//Iterable<Node> methods = () -> RDUtils.getMethods(disk.getId());
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
		//RDUtils.getSubDisks(disk.getId()).forEachRemaining((sd) -> innerCircles.add(new CircleWithInnerCircles(sd.get("d").asNode(), true)));
	}

	/**
	 * write calculated positions into extended disk
	 * 
	 */
	void updateDiskNode() {
		String updateNode = String.format(
				"MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.netArea = %f, n.grossArea = %f ", disk.getId(),
				radius, netArea, grossArea);

		log.debug("set netArea to " + netArea + "for disk " + disk.getId());
		StatementResult position = connector
				.executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + disk.getId() + " RETURN p");
		double oldZPosition = 0.0;
		if (!position.list().isEmpty()) {
			Node node = position.single().get("p").asNode();
			oldZPosition = node.get("z").asDouble();
		}
		String createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", centre.x,
				centre.y, oldZPosition);
		disk.setRadius(radius);
		disk.setNetArea(netArea);
		disk.setGrossArea(grossArea);
		connector.executeWrite(updateNode + createPosition);
		for (CircleWithInnerCircles circle : innerCircles) {
			circle.updateDiskNode();
		}
	}

	ArrayList<CircleWithInnerCircles> getInnerCircles() {
		return innerCircles;
	}
}
