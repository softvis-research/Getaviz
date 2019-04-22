package org.getaviz.generator.city.m2m;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.city.m2m.Rectangle;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.database.DatabaseConnector;

public class CityLayout {
	private static boolean DEBUG = false;
	private static boolean DEBUG_Part2 = false;
	private static String info = "[INFOstream] ";
	public static Rectangle rootRectangle;
	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
	private static DatabaseConnector connector = DatabaseConnector.getInstance();
	// This is to hold actual values of width (index 0) and length (index 1) of
	// database object before transaction can save them
	private static Map<Long, double[]> properties;

	public static void cityLayout(Long model, Map<Long, double[]> testMap) {
		properties = testMap;
			arrangeChildrenRoot(model);
			adjustPositions(getChildren(model), 0, 0, 0);

	}

	/* functions for Document */

	private static void arrangeChildrenRoot(Long model) {
		// get maxArea (worst case) for root of KDTree
		Rectangle docRectangle = calculateMaxAreaRoot(model);
		CityKDTree ptree = new CityKDTree(docRectangle);
		Rectangle covrec = new Rectangle();
		List<Rectangle> elements = sortChildrenAsRectangles(getChildren(model));

		// algorithm
		for (Rectangle el : elements) {
			List<CityKDTreeNode> pnodes = ptree.getFittingNodes(el);
			Map<CityKDTreeNode, Double> preservers = new LinkedHashMap<CityKDTreeNode, Double>(); // LinkedHashMap
																									// necessary, so
																									// elements are
																									// ordered by
																									// inserting-order
			Map<CityKDTreeNode, Double> expanders = new LinkedHashMap<CityKDTreeNode, Double>();
			CityKDTreeNode targetNode = new CityKDTreeNode();
			CityKDTreeNode fitNode = new CityKDTreeNode();

			// check all empty leaves: either they extend COVREC (->expanders) or it doesn't
			// change (->preservers)
			for (CityKDTreeNode pnode : pnodes) {
				sortEmptyLeaf(pnode, el, covrec, preservers, expanders);
			}

			// choose best-fitting pnode
			if (preservers.isEmpty() != true) {
				targetNode = bestFitIsPreserver(preservers.entrySet());
			} else {
				targetNode = bestFitIsExpander(expanders.entrySet());
			}

			// modify targetNode if necessary
			if (targetNode.getRectangle().getWidth() == el.getWidth()
					&& targetNode.getRectangle().getLength() == el.getLength()) { // this if could probably be skipped,
																					// trimmingNode() always returns
																					// fittingNode
				fitNode = targetNode;
			} else {
				fitNode = trimmingNode(targetNode, el);
			}

			// set fitNode as occupied
			fitNode.setOccupied(true);

			// give Entity it's Position
			setNewPositionFromNode(el, fitNode);

			// if fitNode expands covrec, update covrec
			if (fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX()
					|| fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()) {
				updateCovrec(fitNode, covrec);
			}
		}

		rootRectangle = covrec; // used to adjust viewpoint in x3d
	}

	private static Rectangle calculateMaxAreaRoot(Long model) {
		double sum_width = 0;
		double sum_length = 0;
		for (Node child : getChildren(model)) {
			Node entity = connector.getVisualizedEntity(child.id());
//			Map<String, Double> properties = null;
			if (entity.hasLabel(Labels.Package.name())) {
				arrangeChildren(child.id());
			}
			sum_width += properties.get(child.id())[0] + config.getBuildingHorizontalGap();
			sum_length += properties.get(child.id())[1] + config.getBuildingHorizontalGap();
		}
		return new Rectangle(0, 0, sum_width, sum_length, 1);
	}

	/* functions for Entity */

	private static void arrangeChildren(Long entity) {
		// get maxArea (worst case) for root of KDTree
		Rectangle entityRec = calculateMaxArea(entity);
		CityKDTree ptree = new CityKDTree(entityRec);
		Rectangle covrec = new Rectangle();
		List<Rectangle> elements = sortChildrenAsRectangles(getChildren(entity));

		// start algorithm
		for (Rectangle el : elements) {
			List<CityKDTreeNode> pnodes = ptree.getFittingNodes(el);
			Map<CityKDTreeNode, Double> preservers = new LinkedHashMap<CityKDTreeNode, Double>(); // LinkedHashMap
																									// necessary, so
																									// elements are
																									// ordered by
																									// inserting-order
			Map<CityKDTreeNode, Double> expanders = new LinkedHashMap<CityKDTreeNode, Double>();
			CityKDTreeNode targetNode = new CityKDTreeNode();
			CityKDTreeNode fitNode = new CityKDTreeNode();

			// check all empty leaves: either they extend COVREC (->expanders) or it doesn't
			// change (->preservers)
			for (CityKDTreeNode pnode : pnodes) {
				sortEmptyLeaf(pnode, el, covrec, preservers, expanders);
			}

			// choose best-fitting pnode
			if (preservers.isEmpty() != true) {
				targetNode = bestFitIsPreserver(preservers.entrySet());
			} else {
				targetNode = bestFitIsExpander(expanders.entrySet());
			}

			// modify targetNode if necessary
			if (targetNode.getRectangle().getWidth() == el.getWidth()
					&& targetNode.getRectangle().getLength() == el.getLength()) { // this if could be skipped,
																					// trimmingNode() always returns
																					// fittingNode
				fitNode = targetNode;
			} else {
				fitNode = trimmingNode(targetNode, el);
			}

			// set fitNode as occupied
			fitNode.setOccupied(true);

			// give Entity it's Position
			setNewPositionFromNode(el, fitNode);

			// if fitNode expands covrec, update covrec
			if (fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX()
					|| fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()) {
				updateCovrec(fitNode, covrec);
			}
		}
		double width = covrec.getBottomRightX()
				+ (config.getBuildingHorizontalMargin() - config.getBuildingHorizontalGap() / 2) * 2;
		double length = covrec.getBottomRightY()
				+ (config.getBuildingHorizontalMargin() - config.getBuildingHorizontalGap() / 2) * 2;
		double[] array = { width, length };
		properties.put(entity, array);
	}

	private static Rectangle calculateMaxArea(Long entity) {
		double sum_width = 0;
		double sum_length = 0;
		for (Node child : getChildren(entity)) {
			Node element = connector.getVisualizedEntity(child.id());
			if (element.hasLabel(Labels.Package.name())) {
				arrangeChildren(child.id());
			}
			sum_width += properties.get(child.id())[0] + config.getBuildingHorizontalGap();
			sum_length += properties.get(child.id())[1] + config.getBuildingHorizontalGap();
		}
		return new Rectangle(0, 0, sum_width, sum_length, 1);
	}

	/* functions for algorithm */
	private static List<Rectangle> sortChildrenAsRectangles(List<Node> children) {
		List<Rectangle> elements = new ArrayList<Rectangle>();
		// copy all child-elements into a List<Rectangle> (for easier sort) with links
		// to former entities
		for (Node child : children) {
			double width = properties.get(child.id())[0];
			double length = properties.get(child.id())[1];

			Rectangle rectangle = new Rectangle(0, 0, width + config.getBuildingHorizontalGap(),
					length + config.getBuildingHorizontalGap(), 1);
			rectangle.setNodeLink(child);
			elements.add(rectangle);
		}
		// sort elements by size in descending order
		Collections.sort(elements);
		Collections.reverse(elements);
		return elements;
	}

	private static void sortEmptyLeaf(CityKDTreeNode pnode, Rectangle el, Rectangle covrec,
			Map<CityKDTreeNode, Double> preservers, Map<CityKDTreeNode, Double> expanders) {
		// either element fits in current bounds (->preservers) or it doesn't
		// (->expanders)
		double nodeUpperLeftX = pnode.getRectangle().getUpperLeftX();
		double nodeUpperLeftY = pnode.getRectangle().getUpperLeftY();
		double nodeNewBottomRightX = nodeUpperLeftX + el.getWidth(); // expected BottomRightCorner, if el was insert
																		// into pnode
		double nodeNewBottomRightY = nodeUpperLeftY + el.getLength(); // this new corner-point is compared with covrec

		if (nodeNewBottomRightX <= covrec.getBottomRightX() && nodeNewBottomRightY <= covrec.getBottomRightY()) {
			double waste = pnode.getRectangle().getArea() - el.getArea();
			preservers.put(pnode, waste);
			if (DEBUG_Part2) {
				System.out.println("\t\t" + info + "Node is preserver. waste=" + waste);
			}
		} else {
			double ratio = ((nodeNewBottomRightX > covrec.getBottomRightX() ? nodeNewBottomRightX
					: covrec.getBottomRightX())
					/ (nodeNewBottomRightY > covrec.getBottomRightY() ? nodeNewBottomRightY
							: covrec.getBottomRightY()));
			expanders.put(pnode, ratio);
			if (DEBUG_Part2) {
				System.out.println(
						"\t\t" + info + "Node is expander. ratio=" + ratio + " distance=" + Math.abs(ratio - 1));
			}
		}
	}

	private static CityKDTreeNode bestFitIsPreserver(Set<Entry<CityKDTreeNode, Double>> entrySet) {
		// determines which entry in Set has the lowest value of all
		double lowestValue = -1;
		CityKDTreeNode targetNode = new CityKDTreeNode();
		for (Map.Entry<CityKDTreeNode, Double> entry : entrySet) {
			if (entry.getValue() < lowestValue || lowestValue == -1) {
				lowestValue = entry.getValue();
				targetNode = entry.getKey();
			}
		}
		if (DEBUG_Part2) {
			System.out.println("\t\t" + info + "chosen Node is preserver: " + lowestValue);
			System.out.println("\t\t" + info + "Node Rec[(" + targetNode.getRectangle().getUpperLeftX() + "|"
					+ targetNode.getRectangle().getUpperLeftY() + "), (" + targetNode.getRectangle().getBottomRightX()
					+ "|" + targetNode.getRectangle().getBottomRightY() + ")]");
		}
		return targetNode;
	}

	private static CityKDTreeNode bestFitIsExpander(Set<Entry<CityKDTreeNode, Double>> entrySet) {
		double closestTo = 1;
		double lowestDistance = -1;
		CityKDTreeNode targetNode = new CityKDTreeNode();
		for (Map.Entry<CityKDTreeNode, Double> entry : entrySet) {
			double distance = Math.abs(entry.getValue() - closestTo);
			if (distance < lowestDistance || lowestDistance == -1) {
				lowestDistance = distance;
				targetNode = entry.getKey();
			}
		}
		if (DEBUG_Part2) {
			System.out.println("\t\t" + info + "chosen Node is expander: " + lowestDistance);
			System.out.println("\t\t" + info + "Node Rec[(" + targetNode.getRectangle().getUpperLeftX() + "|"
					+ targetNode.getRectangle().getUpperLeftY() + "), (" + targetNode.getRectangle().getBottomRightX()
					+ "|" + targetNode.getRectangle().getBottomRightY() + ")]");
		}
		return targetNode;
	}

	private static CityKDTreeNode trimmingNode(CityKDTreeNode node, Rectangle r) {
		if (DEBUG) {
			System.out.println("\t\t" + info + "trimmingNode()-arrival.");
		}
		double nodeUpperLeftX = node.getRectangle().getUpperLeftX();
		double nodeUpperLeftY = node.getRectangle().getUpperLeftY();
		double nodeBottomRightX = node.getRectangle().getBottomRightX();
		double nodeBottomRightY = node.getRectangle().getBottomRightY();

		// first split: horizontal cut, if necessary
		// Round to 3 digits to prevent infinity loop, because e.g. 12.34000000007 is
		// declared equal to 12.34
		if (Math.round(node.getRectangle().getLength() * 1000d) != Math.round(r.getLength() * 1000d)) {
			// new child-nodes
			node.setLeftChild(new CityKDTreeNode(
					new Rectangle(nodeUpperLeftX, nodeUpperLeftY, nodeBottomRightX, (nodeUpperLeftY + r.getLength()))));
			node.setRightChild(new CityKDTreeNode(new Rectangle(nodeUpperLeftX, (nodeUpperLeftY + r.getLength()),
					nodeBottomRightX, nodeBottomRightY)));
			// set node as occupied (only leaves can contain elements)
			node.setOccupied(true);

			if (DEBUG_Part2) {
				System.out.println("\t\t\t" + info + "horizontal");
				System.out.println("\t\t\t" + info + "targetNode Rec[(" + nodeUpperLeftX + "|" + nodeUpperLeftY + "), ("
						+ nodeBottomRightX + "|" + nodeBottomRightY + ")]");
				System.out.println(
						"\t\t\t" + info + "LeftChild Rec[(" + node.getLeftChild().getRectangle().getUpperLeftX() + "|"
								+ node.getLeftChild().getRectangle().getUpperLeftY() + "), ("
								+ node.getLeftChild().getRectangle().getBottomRightX() + "|"
								+ node.getLeftChild().getRectangle().getBottomRightY() + ")]");
				System.out.println(
						"\t\t\t" + info + "RightChild Rec[(" + node.getRightChild().getRectangle().getUpperLeftX() + "|"
								+ node.getRightChild().getRectangle().getUpperLeftY() + "), ("
								+ node.getRightChild().getRectangle().getBottomRightX() + "|"
								+ node.getRightChild().getRectangle().getBottomRightY() + ")]");
			}

			return trimmingNode(node.getLeftChild(), r);
			// second split: vertical cut, if necessary
			// Round to 3 digits, because e.g. 12.34000000007 is declared equal to 12.34
		} else if (Math.round(node.getRectangle().getWidth() * 1000d) != Math.round(r.getWidth() * 1000d)) {
			// new child-nodes
			node.setLeftChild(new CityKDTreeNode(
					new Rectangle(nodeUpperLeftX, nodeUpperLeftY, (nodeUpperLeftX + r.getWidth()), nodeBottomRightY)));
			node.setRightChild(new CityKDTreeNode(new Rectangle((nodeUpperLeftX + r.getWidth()), nodeUpperLeftY,
					nodeBottomRightX, nodeBottomRightY)));
			// set node as occupied (only leaves can contain elements)
			node.setOccupied(true);

			if (DEBUG_Part2) {
				System.out.println("\t\t\t" + info + "vertical");
				System.out.println("\t\t\t" + info + "targetNode Rec[(" + nodeUpperLeftX + "|" + nodeUpperLeftY + "), ("
						+ nodeBottomRightX + "|" + nodeBottomRightY + ")]");
				System.out.println(
						"\t\t\t" + info + "LeftChild Rec[(" + node.getLeftChild().getRectangle().getUpperLeftX() + "|"
								+ node.getLeftChild().getRectangle().getUpperLeftY() + "), ("
								+ node.getLeftChild().getRectangle().getBottomRightX() + "|"
								+ node.getLeftChild().getRectangle().getBottomRightY() + ")]");
				System.out
						.println("\t\t\t" + info + "LeftChild center(" + node.getLeftChild().getRectangle().getCenterX()
								+ "|" + node.getLeftChild().getRectangle().getCenterY() + ")");
				System.out.println(
						"\t\t\t" + info + "RightChild Rec[(" + node.getRightChild().getRectangle().getUpperLeftX() + "|"
								+ node.getRightChild().getRectangle().getUpperLeftY() + "), ("
								+ node.getRightChild().getRectangle().getBottomRightX() + "|"
								+ node.getRightChild().getRectangle().getBottomRightY() + ")]");
			}
			if (DEBUG) {
				System.out.println("\t\t" + info + "trimmingNode()-exit.");
			}
			return node.getLeftChild();
		} else {
			if (DEBUG) {
				System.out.println("\t\t" + info + "trimmingNode()-exit.");
			}
			return node;
		}
	}

	private static void setNewPositionFromNode(Rectangle el, CityKDTreeNode fitNode) {
		Node node = el.getNodeLink();
		// mapping 2D rectangle on 3D building
		double x = fitNode.getRectangle().getCenterX() - config.getBuildingHorizontalGap() / 2;
		double y = node.get("height").asDouble() / 2;
		double z = fitNode.getRectangle().getCenterY() - config.getBuildingHorizontalGap() / 2;
		connector.executeWrite(String.format(
				"MATCH (n) WHERE ID(n) = %d CREATE (n)-[:HAS]->(p:Position:City {name: \'position\', x: %f, y: %f, z: %f})",
				node.id(), x, y, z));
	}

	private static void updateCovrec(CityKDTreeNode fitNode, Rectangle covrec) {
		double newX = (fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX()
				? fitNode.getRectangle().getBottomRightX()
				: covrec.getBottomRightX());
		double newY = (fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()
				? fitNode.getRectangle().getBottomRightY()
				: covrec.getBottomRightY());
		covrec.changeRectangle(0, 0, newX, newY);
		if (DEBUG) {
			System.out.println(
					"\t\t" + info + "CovRec [checkVALUES]: [(" + covrec.getUpperLeftX() + "|" + covrec.getUpperLeftY()
							+ "), (" + covrec.getBottomRightX() + "|" + covrec.getBottomRightY() + ")]");
		}
	}

	private static void adjustPositions(List<Node> children, double parentX, double parentY, double parentZ) {
		for (Node child : children) {
			Record record = connector.executeRead(
					"MATCH (p:Position)<-[:HAS]-(n)-[:VISUALIZES]->(e) WHERE ID(n) = " + child.id() + " RETURN e, p")
					.single();
			Node entity = record.get("e").asNode();
			Node position = record.get("p").asNode();
			double centerX = position.get("x").asDouble();
			double centerZ = position.get("z").asDouble();
			double centerY = position.get("y").asDouble();
			double setX = centerX + parentX + config.getBuildingHorizontalMargin();
			double setZ = centerZ + parentZ + config.getBuildingHorizontalMargin();
			double setY = centerY + parentY + config.getBuildingVerticalMargin();
			double width = properties.get(child.id())[0];
			double length = properties.get(child.id())[1];
			connector.executeWrite(String.format(
					"MATCH (c),(p) WHERE ID(c) = %d AND ID(p) = %d SET c.width = %f, c.length = %f, p.x = %f, p.y = %f, p.z = %f",
					child.id(), position.id(), width, length, setX, setY, setZ));
			double height = child.get("height").asDouble();
			if (entity.hasLabel(Labels.Package.name())) {
				double newUpperLeftX = setX - width / 2;
				double newUpperLeftZ = setZ - length / 2;
				double newUpperLeftY = setY - height / 2;
				adjustPositions(getChildren(child.id()), newUpperLeftX, newUpperLeftY, newUpperLeftZ);
			}
		}
	}

	private static List<Node> getChildren(Long entity) {
		List<Node> children = new ArrayList<Node>();
		StatementResult childs = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(c)-[:VISUALIZES]->(element) WHERE (c:District OR c:Building) AND ID(n) = " + entity + " RETURN c, element.hash as hash ORDER BY element.hash");
		while (childs.hasNext()) {
			children.add(childs.next().get("c").asNode());
		}
		return children;
	}
}
