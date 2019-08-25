package org.getaviz.generator.rd.m2m;

import java.util.ArrayList;
import org.getaviz.generator.rd.RDUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.database.DatabaseConnector;

class CircleWithInnerCircles extends Circle {
	private ArrayList<CircleWithInnerCircles> innerCircles = new ArrayList<CircleWithInnerCircles>();
	private Log log = LogFactory.getLog(CircleWithInnerCircles.class);
	private Node diskNode;
	private DatabaseConnector connector = DatabaseConnector.getInstance();

	CircleWithInnerCircles(Node disk, Boolean nesting) {
		diskNode = disk;
		Iterable<Node> data = () -> RDUtils.getData(disk.id());
		Iterable<Node> methods = () -> RDUtils.getMethods(disk.id());
		if (nesting == true) {
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
		RDUtils.getSubDisks(disk.id()).forEachRemaining((subDisk) -> {
			innerCircles.add(new CircleWithInnerCircles(subDisk.get("d").asNode(), true));
		});
	}

	/**
	 * write calculated positions into extended disk
	 * 
	 */
	public void updateDiskNode() {
		String updateNode = String.format(
				"MATCH (n) WHERE ID(n) = %d SET n.radius = %f, n.netArea = %f, n.grossArea = %f ", diskNode.id(),
				radius, netArea, grossArea);

		log.debug("set netArea to " + netArea + "for disk " + diskNode.id());
		StatementResult position = connector
				.executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + diskNode.id() + " RETURN p");
		double oldZPosition = 0.0;
		if (position.hasNext()) {
			Node node = position.single().get("p").asNode();
			oldZPosition = node.get("z").asDouble();
		}
		String createPosition = String.format("CREATE (n)-[:HAS]->(:RD:Position {x: %f, y: %f, z: %f})", centre.x,
				centre.y, oldZPosition);
		connector.executeWrite(updateNode + createPosition);
		for (CircleWithInnerCircles circle : innerCircles) {
			circle.updateDiskNode();
		}
	}

	public ArrayList<CircleWithInnerCircles> getInnerCircles() {
		return innerCircles;
	}
}
