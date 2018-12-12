package org.svis.generator.city.m2m;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.svis.generator.SettingsConfiguration;
import org.svis.generator.SettingsConfiguration.BuildingType;
import org.svis.generator.SettingsConfiguration.FamixParser;
import org.svis.xtext.city.CityFactory;
import org.svis.xtext.city.Document;
import org.svis.xtext.city.Entity;
import org.svis.xtext.city.Position;
import org.svis.xtext.city.Root;
import org.svis.xtext.city.impl.CityFactoryImpl;

public class ABAPCityLayout {
	private static boolean DEBUG = false;
	private static boolean DEBUG_Part1 = false;
	private static boolean DEBUG_Part2 = false;
	private static String info = "[INFOstream] ";
	private static CityFactory cityFactory = new CityFactoryImpl();
	public static Rectangle rootRectangle;
	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
	
	
///** TODO
// * importing the general size of class members in settings.properties 
// */
//	private static Double memberSize = 32.0;

	/**
	 * @param root
	 *            <p>
	 *            this method starts the layout-process
	 *            </p>
	 */
	public static void cityLayout(Root root) {
		if (DEBUG) {
			System.out.println(info + "cityLayout(root)-arrival.");
		}

		// receives List of ALL CITYelements in the form of the root element
		arrangeChildren(root.getDocument());
		adjustPositions(root.getDocument().getEntities(), 0, 0);

		if (DEBUG) {
			System.out.println(info + "cityLayout(root)-exit.");
		}
	}

	/* functions for Document */
	private static void arrangeChildren(Document document) {
		// get maxArea (worst case) for root of KDTree
		Rectangle docRectangle = calculateMaxArea(document);
		CityKDTree ptree = new CityKDTree(docRectangle);
		Rectangle covrec = new Rectangle();
		List<Rectangle> elements = sortChildrenAsRectangles(document.getEntities());

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
			setNewPosition(el, fitNode);

			// if fitNode expands covrec, update covrec
			if (fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX()
					|| fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()) {
				updateCovrec(fitNode, covrec);
			}
		}

		rootRectangle = covrec; // used to adjust viewpoint in x3d
	}

	private static Rectangle calculateMaxArea(Document document) {
		if (DEBUG) {
			System.out.println("\t\t" + info + "calculateMaxArea(document)-arrival.");
		}
		EList<Entity> children = document.getEntities();
		double sum_width = 0;
		double sum_length = 0;

		for (Entity child : children) {
			if (config.getBuildingType() == BuildingType.CITY_DYNAMIC) {
				if (child.getType().equals("FAMIX.Namespace") || child.getType().equals("FAMIX.Class")) {
					if (DEBUG) {
						System.out.println("\t\t\t" + info + "layOut(" + child.getFqn() + ")-call, recursive.");
					}
					arrangeChildren(child);
				}
			} else {
				if (child.getType().equals("FAMIX.Namespace")  || child.getType().equals("reportDistrict") || child.getType().equals("classDistrict")
						|| child.getType().equals("functionGroupDistrict") || child.getType().equals("tableDistrict") 
						|| child.getType().equals("dcDataDistrict")) {
					if (DEBUG) {
						System.out.println("\t\t\t" + info + "layOut(" + child.getFqn() + ")-call, recursive.");
					}
					arrangeChildren(child);
				}
			}
			sum_width += child.getWidth() + config.getBuildingHorizontalGap();
			sum_length += child.getLength() + config.getBuildingHorizontalGap();
			if (DEBUG_Part1) {
				System.out.println("\t\t\t" + info + "Child " + child.getFqn() + " [modVALUES]: width="
						+ child.getWidth() + " length=" + child.getLength() + "|=> sum_width=" + sum_width
						+ " sum_length=" + sum_length);
			}
		}

		if (DEBUG) {
			System.out.println("\t\t" + info + "calculateMaxArea(document)-exit.");
		}
		return new Rectangle(0, 0, sum_width, sum_length, 1);
	}

	/* functions for Entity */
	private static void arrangeChildren(Entity entity) {
		if (DEBUG) {
			System.out.println("\t" + info + "layOut(" + entity.getFqn() + ")-arrival.");
		}
		// get maxArea (worst case) for root of KDTree
		if (DEBUG) {
			System.out.println("\t\t" + info + "calculateMaxArea(" + entity.getFqn() + ")-call.");
		}
		Rectangle entityRec = calculateMaxArea(entity);
		CityKDTree ptree = new CityKDTree(entityRec);
		if (DEBUG_Part1) {
			System.out.println(
					"\t\t" + info + "KDTree [checkVALUES]: root[(" + ptree.getRoot().getRectangle().getUpperLeftX()
							+ "|" + ptree.getRoot().getRectangle().getUpperLeftY() + "), ("
							+ ptree.getRoot().getRectangle().getBottomRightX() + "|"
							+ ptree.getRoot().getRectangle().getBottomRightY() + ")]");
		}
		Rectangle covrec = new Rectangle();
		if (DEBUG_Part1) {
			System.out.println(
					"\t\t" + info + "CovRec [checkVALUES]: [(" + covrec.getUpperLeftX() + "|" + covrec.getUpperLeftY()
							+ "), (" + covrec.getBottomRightX() + "|" + covrec.getBottomRightY() + ")]");
		}
		List<Rectangle> elements = sortChildrenAsRectangles(entity.getEntities());

		// start algorithm
		for (Rectangle el : elements) {
			if (DEBUG_Part2) {
				System.out.println("\n\t\t" + info + "Entity " + el.getEntityLink().getFqn() + " starts algorithm.");
			}
			List<CityKDTreeNode> pnodes = ptree.getFittingNodes(el);
			if (DEBUG_Part2) {
				System.out.println("\n\t\t" + info + "show all fittingNodes!");
				int node_number = 0;
				for (CityKDTreeNode n : pnodes) {
					node_number++;
					System.out.println("\t\t" + info + "Node #" + node_number);
					System.out.println("\t\t" + info + "Node Rec[(" + n.getRectangle().getUpperLeftX() + "|"
							+ n.getRectangle().getUpperLeftY() + "), (" + n.getRectangle().getBottomRightX() + "|"
							+ n.getRectangle().getBottomRightY() + ")]");
				}
			}
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
			if (DEBUG_Part2) {
				System.out.println("\n\t\t" + info + "check all empty leaves!");
			}
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
				if (DEBUG_Part2) {
					System.out.println("\n\t\t" + info + "targetNode fits.");
				}
			} else {
				if (DEBUG_Part2) {
					System.out.println("\n\t\t" + info + "targetNode needs trimming.");
				}
				fitNode = trimmingNode(targetNode, el);
			}

			// set fitNode as occupied
			fitNode.setOccupied(true);

			// give Entity it's Position
			setNewPosition(el, fitNode);

			// if fitNode expands covrec, update covrec
			if (fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX()
					|| fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()) {
				updateCovrec(fitNode, covrec);
			}
		}
		entity.setWidth(covrec.getBottomRightX()
				+ (config.getBuildingHorizontalMargin() - config.getBuildingHorizontalGap() / 2) * 2);
		entity.setLength(covrec.getBottomRightY()
				+ (config.getBuildingHorizontalMargin() - config.getBuildingHorizontalGap() / 2) * 2);
		if (DEBUG) {
			System.out.println("\t\t" + info + "Entity " + entity.getFqn() + " [checkVALUES]: width="
					+ entity.getWidth() + " length=" + entity.getLength());
		}
		if (DEBUG) {
			System.out.println("\t" + info + "layOut(" + entity.getFqn() + ")-exit.");
		}
	}

	private static Rectangle calculateMaxArea(Entity entity) {
		if (DEBUG) {
			System.out.println("\t\t" + info + "calculateMaxArea(" + entity.getFqn() + ")-arrival.");
		}
		EList<Entity> children = entity.getEntities();
		double sum_width = 0;
		double sum_length = 0;

		for (Entity child : children) {
			if (config.getBuildingType() == BuildingType.CITY_DYNAMIC) {
				if (child.getType().equals("FAMIX.Namespace") || child.getType().equals("FAMIX.Class")) {
					if (DEBUG) {
						System.out.println("\t\t\t" + info + "layOut(" + child.getFqn() + ")-call, recursive.");
					}
					arrangeChildren(child);
				}
			} else {
				if (child.getType().equals("FAMIX.Namespace") || child.getType().equals("tableDistrict") || child.getType().equals("dcDataDistrict")) {
					if (DEBUG) {
						System.out.println("\t\t\t" + info + "layOut(" + child.getFqn() + ")-call, recursive.");
					}
					arrangeChildren(child);
				} else if (child.getType().equals("classDistrict")) {
					arrangeClassDistrict(child);
				} else if (child.getType().equals("functionGroupDistrict")) {
					arrangeFunctionGroupDistrict(child);
				} else if (child.getType().equals("reportDistrict")) {
					arrangeReportDistrict(child);
				}  
			}
			sum_width += child.getWidth() + config.getBuildingHorizontalGap();
			sum_length += child.getLength() + config.getBuildingHorizontalGap();
			if (DEBUG_Part1) {
				System.out.println("\t\t\t" + info + "Child " + child.getFqn() + " [modVALUES]: width="
						+ child.getWidth() + " length=" + child.getLength() + "|=> sum_width=" + sum_width
						+ " sum_length=" + sum_length);
			}
		}

		if (DEBUG_Part1) {
			System.out.println("\t\t\t" + info + "Entity " + entity.getFqn() + " [newVALUES]: width="
					+ entity.getWidth() + " length=" + entity.getLength());
		}
		if (DEBUG) {
			System.out.println("\t\t" + info + "calculateMaxArea(" + entity.getFqn() + ")-exit.");
		}
		return new Rectangle(0, 0, sum_width, sum_length, 1);
	}

	/* functions for algorithm */
	private static List<Rectangle> sortChildrenAsRectangles(EList<Entity> entities) {
		List<Rectangle> elements = new ArrayList<Rectangle>();
		// copy all child-elements into a List<Rectangle> (for easier sort) with links
		// to former entities
		for (Entity e : entities) {
			Rectangle rectangle = new Rectangle(0, 0, e.getWidth() + config.getBuildingHorizontalGap(),
					e.getLength() + config.getBuildingHorizontalGap(), 1);
			rectangle.setEntityLink(e);
			elements.add(rectangle);
			if (DEBUG_Part1) {
				System.out.println("\t\t" + info + " " + e.getFqn() + " [checkVALUES]: [(" + rectangle.getUpperLeftX()
						+ "|" + rectangle.getUpperLeftY() + "), (" + rectangle.getBottomRightX() + "|"
						+ rectangle.getBottomRightY() + ")]");
			}
			if (DEBUG_Part1) {
				System.out.println("\t\t" + info + "[checkEntity]: rectangle.getEntityLink() == e ->"
						+ (rectangle.getEntityLink() == e));
			}
		}

		// sort elements by size in descending order
		Collections.sort(elements);
		if (DEBUG_Part1) {
			System.out.println("\n\t\t" + info + "[checkSort1]: order elements");
			for (Rectangle r : elements) {
				System.out.println("\t\t" + info + " " + r.getEntityLink().getFqn() + "[checkValues]: area="
						+ r.getArea() + " width=" + r.getWidth() + " length=" + r.getLength());
			}
		}
		Collections.reverse(elements);
		if (DEBUG_Part2) {
			System.out.println("\n\t\t" + info + "[checkSort2]: descending order");
			for (Rectangle r : elements) {
				System.out.println("\t\t" + info + " " + r.getEntityLink().getFqn() + "[checkValues]: area="
						+ r.getArea() + " width=" + r.getWidth() + " length=" + r.getLength());
			}
		}
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

	private static void setNewPosition(Rectangle el, CityKDTreeNode fitNode) {
		
		Position newPos = cityFactory.createPosition();
		// mapping 2D rectangle on 3D building
		newPos.setX(fitNode.getRectangle().getCenterX() - config.getBuildingHorizontalGap() / 2); // width
		newPos.setZ(fitNode.getRectangle().getCenterY() - config.getBuildingHorizontalGap() / 2); // length

		el.getEntityLink().setPosition(newPos);
		if (DEBUG) {
			System.out.println("\n\t\t" + info + "Entity " + el.getEntityLink().getFqn() + " [checkVALUES]: ("
					+ el.getEntityLink().getPosition().getX() + "|" + el.getEntityLink().getPosition().getY() + "|"
					+ el.getEntityLink().getPosition().getZ() + ")\n");
		}
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

	private static void adjustPositions(EList<Entity> children, double parentX, double parentZ) {
		for (Entity e : children) {
			double centerX = e.getPosition().getX();
			double centerZ = e.getPosition().getZ();
			e.getPosition().setX(centerX + parentX + config.getBuildingHorizontalMargin()/*-BLDG_horizontalGap/2*/);
			e.getPosition().setZ(centerZ + parentZ + config.getBuildingHorizontalMargin()/*-BLDG_horizontalGap/2*/);
			
			if (e.getType().equals("FAMIX.Namespace") || e.getType().equals("reportDistrict")
					|| e.getType().equals("classDistrict") || e.getType().equals("functionGroupDistrict")
					|| e.getType().equals("tableDistrict") || e.getType().equals("dcDataDistrict")) {
				double newUpperLeftX = e.getPosition().getX() - e.getWidth() / 2;
				double newUpperLeftZ = e.getPosition().getZ() - e.getLength() / 2;
				adjustPositions(e.getEntities(), newUpperLeftX, newUpperLeftZ);
			}
		}
	} // End of adjustPositions
	
	/** NEW LAYOUT PROCESSING */
	private static void arrangeClassDistrict(Entity classDistrict) {
		// setting district size
		// maybe adding the margin?
		Double squareSize = Math.ceil(Math.sqrt(classDistrict.getEntities().size()));
		double size = squareSize * (config.getAbapClassMemberSideLength() + config.getBuildingHorizontalGap());		
		classDistrict.setWidth(size + 2 * config.getBuildingHorizontalMargin()); // or size + config.getBuildingHorizontalMargin() + config.getBuildingHorizontalGap() ??
		classDistrict.setLength(size + 2 * config.getBuildingHorizontalMargin());
		Rectangle classDistrictSquare = new Rectangle(0, 0, size, size);
		
		EList<Entity> members = classDistrict.getEntities();
		
		List<Rectangle> privateMembers = new ArrayList<Rectangle>();
		List<Rectangle> protectedMembers = new ArrayList<Rectangle>();
		List<Rectangle> publicMembers = new ArrayList<Rectangle>();
		
		double unitSize = config.getAbapClassMemberSideLength() + config.getBuildingHorizontalGap();
		
		// ordering the members as rectangles by visibility
		for (Entity member : members) {
			Rectangle square = new Rectangle(0, 0, unitSize, unitSize);
			square.setEntityLink(member);
			
			switch (member.getVisibility()) {
			case "PRIVATE":
				privateMembers.add(square);
				break;
			case "PROTECTED":
				protectedMembers.add(square);
				break;
			case "PUBLIC":
				publicMembers.add(square);
				break;
			default:
				publicMembers.add(square);
				break;
			}
		}
		
		// merge it to one list
		List<Rectangle> sortedMembers = new ArrayList<Rectangle>();
		sortedMembers.addAll(privateMembers);
		sortedMembers.addAll(protectedMembers);
//		sortedMembers.addAll(publicMembers);
		
		// start algorithm
		List<String> position = getPositionList(squareSize);
		
		// moving the entities to the right place
		moveElementsToPosition(sortedMembers, position, classDistrictSquare, unitSize, squareSize, false);
		moveElementsToPosition(publicMembers, position, classDistrictSquare, unitSize, squareSize, true);
		
	}
	
	private static void arrangeFunctionGroupDistrict(Entity functionGroupDistrict) {
		Double squareSize = Math.ceil(Math.sqrt(functionGroupDistrict.getEntities().size()));
		double size = squareSize * (config.getAbapFunctionGroupMemberSideLength() + config.getBuildingHorizontalGap());		
		functionGroupDistrict.setWidth(size + 2 * config.getBuildingHorizontalMargin()); // or size + config.getBuildingHorizontalMargin() + config.getBuildingHorizontalGap() ??
		functionGroupDistrict.setLength(size + 2 * config.getBuildingHorizontalMargin());
		Rectangle functionGroupDistrictSquare = new Rectangle(0, 0, size, size);
		
		EList<Entity> members = functionGroupDistrict.getEntities();
		
		List<Rectangle> privateMembers = new ArrayList<Rectangle>();
		List<Rectangle> publicMembers = new ArrayList<Rectangle>();
		
		double unitSize = config.getAbapFunctionGroupMemberSideLength() + config.getBuildingHorizontalGap();
		
		// ordering the members as rectangles by visibility
		for (Entity member : members) {
			Rectangle square = new Rectangle(0, 0, unitSize, unitSize);
			square.setEntityLink(member);
			
			switch (member.getType()) {
			case "FAMIX.Attribute":
				privateMembers.add(square);
				break;
			case "FAMIX.FunctionModule":
				publicMembers.add(square);
				break;
			default:
				publicMembers.add(square);
				break;
			}
		}
		
		// start algorithm
		List<String> position = getPositionList(squareSize);
		
		// moving the entities to the right place
		moveElementsToPosition(privateMembers, position, functionGroupDistrictSquare, unitSize, squareSize, false);
		moveElementsToPosition(publicMembers, position, functionGroupDistrictSquare, unitSize, squareSize, true);
	}
	
	private static void arrangeReportDistrict(Entity reportDistrict) {
		Double squareSize = Math.ceil(Math.sqrt(reportDistrict.getEntities().size()));
		double size = squareSize * (config.getAbapReportMemberSideLength() + config.getBuildingHorizontalGap());		
		reportDistrict.setWidth(size + 2 * config.getBuildingHorizontalMargin()); // or size + config.getBuildingHorizontalMargin() + config.getBuildingHorizontalGap() ??
		reportDistrict.setLength(size + 2 * config.getBuildingHorizontalMargin());
		Rectangle reportDistrictSquare = new Rectangle(0, 0, size, size);
		
		EList<Entity> members = reportDistrict.getEntities();
		
		List<Rectangle> privateMembers = new ArrayList<Rectangle>();
		List<Rectangle> attributes = new ArrayList<Rectangle>();
		List<Rectangle> forms = new ArrayList<Rectangle>();
		
		double unitSize = config.getAbapReportMemberSideLength() + config.getBuildingHorizontalGap();
		
		// ordering the members as rectangles by visibility
		for (Entity member : members) {
			Rectangle square = new Rectangle(0, 0, unitSize, unitSize);
			square.setEntityLink(member);
			
			switch (member.getType()) {
			case "FAMIX.Report":
				privateMembers.add(square);
				break;
			case "FAMIX.Attribute":
				attributes.add(square);
				break;
			case "FAMIX.Formroutine":
				forms.add(square);
				break;
			default:
				forms.add(square);
				break;
			}
		}
		
		privateMembers.addAll(attributes);
		
		// start algorithm
		List<String> position = getPositionList(squareSize);
		
		// moving the entities to the right place
		moveElementsToPosition(privateMembers, position, reportDistrictSquare, unitSize, squareSize, false);
		moveElementsToPosition(forms, position, reportDistrictSquare, unitSize, squareSize, true);
	}
	
	private static List<String> getPositionList(Double squareSize) {
		int counter = 0;
		List<String> position = new ArrayList<String>();
		position.add(0, "");
		position.add(1, "U");
		position.add(2, "R");
		position.add(3, "D");
		position.add(4, "L");
		
		if (squareSize.intValue() % 2 == 1) {
			for (int k = 3; k <= squareSize; k += 2) {
				counter++;
				for (int i = (k - 2) * (k - 2); i < k * k; ++i) {
					// the first four fields are already filled
					if (i < 5)
						continue;
					
					if ((i <= (k - 2) * (k - 2) + 3)) {
						position.add(i, appendNextCharacter(position.get(i - 8 * (counter - 1)), false));
					} else {
						if (position.get(i - 4).length() % counter == 0) {
							position.add(i, appendNextCharacter(position.get(i - 4), true));
						} else {
							position.add(i, appendNextCharacter(position.get(i - 4), false));
						}
					}
				}
			}
		} else {
			for (int l = 4; l <= squareSize; l += 2) {
				counter++;
				for (int j = (l - 2) * (l - 2) + 1; j <= l * l; ++j) {
					// the first four need some special treatment
					if (j < 9) {
						switch(j) {
						    case 5: position.add(5, "U"); break;
						    case 6: position.add(6, "R"); break;
						    case 7: position.add(7, "D"); break;
						    case 8: position.add(8, "L"); break;
						}
						continue;
					}
					
					if (j <= (l - 2) * (l - 2) + 4) {
						position.add(j, appendNextCharacter(position.get(j - 8 * (counter - 1) - 4), false)); // j - 8 * counter + 4
					} else {
						if (position.get(j - 4).length() % counter == 0) {
							position.add(j, appendNextCharacter(position.get(j - 4), true));
						} else {
							position.add(j, appendNextCharacter(position.get(j - 4), false));
						}
					}
					
				} 
			}			
		}
		return position;
	}
	
	private static String appendNextCharacter(String string, boolean changeDirection) {
		String lastCharacter = string.substring(string.length() - 1);
		if (changeDirection) {
			switch (lastCharacter) {
			case "U":
				return string.concat("R");
			case "R":
				return string.concat("D");
			case "D":
				return string.concat("L");
			case "L":
				return string.concat("U");
			default:
				return "";
			}
		} else {
			return string.concat(lastCharacter);
		}
	}
	
	private static void moveElementsToPosition(List<Rectangle> childrenRectangles, List<String> position, Rectangle districtSquare, double unitSize, Double squareSize, boolean reverse) {
		int counter,
			counterIncrement;
		
		if (reverse) {
			counter = squareSize.intValue() * squareSize.intValue() - 1;
			counterIncrement = -1;
			
		} else {
			counter = 0;
			counterIncrement = 1;
		}
		
		if (squareSize.intValue() % 2 == 1) {			
			for (Rectangle r : childrenRectangles) {
				if (counter == 0) {
					Position newPos = cityFactory.createPosition();
					newPos.setX(districtSquare.getCenterX());
					newPos.setZ(districtSquare.getCenterY());
					
					r.getEntityLink().setPosition(newPos);
					counter += counterIncrement;
				} else {
					Position newPos = cityFactory.createPosition();
					newPos.setX(districtSquare.getCenterX());
					newPos.setZ(districtSquare.getCenterY());
					for (int i = 0; i < position.get(counter).length(); ++i) {
						char direction = position.get(counter).charAt(i);
						switch (direction) {
						case 'U':
							newPos.setZ(newPos.getZ() + unitSize); // you don't need this unitSize necessarily...
							break;
						case 'R':
							newPos.setX(newPos.getX() + unitSize); // you could also write:  newPos.setX(newPos.getX() + r.getWidth())  
							break;
						case 'D':
							newPos.setZ(newPos.getZ() - unitSize); // if you use unitSize, you need not to create one rectangle per entity in arrangeMembers()
							break;
						case 'L':
							newPos.setX(newPos.getX() - unitSize);
							break;
						}						
					}
					r.getEntityLink().setPosition(newPos);
					counter += counterIncrement;
				}
			}
		} else {			
			counter += 1;
			
			for (Rectangle r : childrenRectangles) {
				if (counter <= 4) {
					Position newPos = cityFactory.createPosition();
					newPos.setX(districtSquare.getCenterX());
					newPos.setZ(districtSquare.getCenterY());
					
					switch (position.get(counter)) {
					case "U":
						newPos.setX(newPos.getX() + unitSize / 2);
						newPos.setZ(newPos.getZ() + unitSize / 2);
						break;
					case "R":
						newPos.setX(newPos.getX() + unitSize / 2);
						newPos.setZ(newPos.getZ() - unitSize / 2);
						break;
					case "D":
						newPos.setX(newPos.getX() - unitSize / 2);
						newPos.setZ(newPos.getZ() - unitSize / 2);
						break;
					case "L":
						newPos.setX(newPos.getX() - unitSize / 2);
						newPos.setZ(newPos.getZ() + unitSize / 2);
						break;
					}
					
					r.getEntityLink().setPosition(newPos);
					counter += counterIncrement;
				} else {
					Position newPos = cityFactory.createPosition();
					newPos.setX(districtSquare.getCenterX());
					newPos.setZ(districtSquare.getCenterY());
					
					switch (counter % 4) {
					case 1:
						newPos.setX(newPos.getX() + unitSize / 2);
						newPos.setZ(newPos.getZ() + unitSize / 2);
						break;
					case 2:
						newPos.setX(newPos.getX() + unitSize / 2);
						newPos.setZ(newPos.getZ() - unitSize / 2);
						break;
					case 3:
						newPos.setX(newPos.getX() - unitSize / 2);
						newPos.setZ(newPos.getZ() - unitSize / 2);
						break;
					case 0:
						newPos.setX(newPos.getX() - unitSize / 2);
						newPos.setZ(newPos.getZ() + unitSize / 2);
						break;
					}
					
					for (int i = 0; i < position.get(counter).length(); ++i) {
						char direction = position.get(counter).charAt(i);
						
						switch (direction) {
						case 'U':
							newPos.setZ(newPos.getZ() + unitSize);
							break;
						case 'R':
							newPos.setX(newPos.getX() + unitSize);
							break;
						case 'D':
							newPos.setZ(newPos.getZ() - unitSize);
							break;
						case 'L':
							newPos.setX(newPos.getX() - unitSize);
							break;
						}						
					}
					
					r.getEntityLink().setPosition(newPos);
					counter += counterIncrement;
				}
			}
		}
	}
}
