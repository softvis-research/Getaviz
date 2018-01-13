package org.svis.generator.city.m2m;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.svis.generator.city.CitySettings;
import org.svis.xtext.city.CityFactory;
import org.svis.xtext.city.Document;
import org.svis.xtext.city.Entity;
import org.svis.xtext.city.Position;
import org.svis.xtext.city.Root;
import org.svis.xtext.city.impl.CityFactoryImpl;

public class CityLayout {
	private static boolean DEBUG = false;
	private static boolean DEBUG_Part1 = false;
	private static boolean DEBUG_Part2 = false;
	private static String info = "[INFOstream] ";
	private static CityFactory cityFactory = new CityFactoryImpl();
	public static Rectangle rootRectangle;
	
	/**
	 * @param root
	 * <p>this method starts the layout-process</p>
	 */
	public static void cityLayout(Root root){
		if(DEBUG){System.out.println(info+ "cityLayout(root)-arrival.");}

		//receives List of ALL CITYelements in the form of the root element
		arrangeChildren(root.getDocument());
		adjustPositions(root.getDocument().getEntities(), 0, 0, 0);
		
		if(DEBUG){System.out.println(info+ "cityLayout(root)-exit.");}
	}

/*functions for Document*/
	private static void arrangeChildren(Document document){
		//get maxArea (worst case) for root of KDTree
		Rectangle docRectangle = calculateMaxArea(document);
		CityKDTree ptree = new CityKDTree(docRectangle);
		Rectangle covrec = new Rectangle();
		List<Rectangle> elements = sortChildrenAsRectangles(document.getEntities());
		
		//algorithm
		for(Rectangle el: elements){
			List<CityKDTreeNode> pnodes = ptree.getFittingNodes(el);
			Map<CityKDTreeNode, Double> preservers = new LinkedHashMap<CityKDTreeNode, Double>();		//LinkedHashMap necessary, so elements are ordered by inserting-order
			Map<CityKDTreeNode, Double> expanders = new LinkedHashMap<CityKDTreeNode, Double>();
			CityKDTreeNode targetNode = new CityKDTreeNode();
			CityKDTreeNode fitNode = new CityKDTreeNode();
			
			//check all empty leaves: either they extend COVREC (->expanders) or it doesn't change (->preservers)
			for(CityKDTreeNode pnode: pnodes){
				sortEmptyLeaf(pnode, el, covrec, preservers, expanders);
			}
			
			//choose best-fitting pnode
			if(preservers.isEmpty() != true){
				targetNode = bestFitIsPreserver(preservers.entrySet());
			}else{
				targetNode = bestFitIsExpander(expanders.entrySet());
			}
			
			//modify targetNode if necessary
			if(targetNode.getRectangle().getWidth() == el.getWidth() && targetNode.getRectangle().getLength() == el.getLength()){		//this if could probably be skipped, trimmingNode() always returns fittingNode
				fitNode = targetNode;
			}else{
				fitNode = trimmingNode(targetNode, el);
			}
			
			//set fitNode as occupied
			fitNode.setOccupied(true);
			
			//give Entity it's Position
			setNewPosition(el, fitNode);
			
			//if fitNode expands covrec, update covrec
			if(fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX() || fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()){
				updateCovrec(fitNode, covrec);
			}
		}

		rootRectangle = covrec; // used to adjust viewpoint in x3d
	}

	private static Rectangle calculateMaxArea(Document document) {
		if(DEBUG){System.out.println("\t\t" +info+ "calculateMaxArea(document)-arrival.");}
		EList<Entity> children = document.getEntities();
		double sum_width = 0;
		double sum_length = 0;
		
		for(Entity child: children){
			if(CitySettings.BUILDING_TYPE == CitySettings.BuildingType.CITY_DYNAMIC){
				if(child.getType().equals("FAMIX.Namespace") || child.getType().equals("FAMIX.Class")) {
					if(DEBUG){System.out.println("\t\t\t" +info+ "layOut(" +child.getFqn()+ ")-call, recursive.");}
					arrangeChildren(child);
				}
			} else {
				if(child.getType().equals("FAMIX.Namespace")) {
					if(DEBUG){System.out.println("\t\t\t" +info+ "layOut(" +child.getFqn()+ ")-call, recursive.");}
					arrangeChildren(child);
				}
			}
			sum_width += child.getWidth()+ CitySettings.BLDG_horizontalGap;
			sum_length += child.getLength() + CitySettings.BLDG_horizontalGap;
			if(DEBUG_Part1){System.out.println("\t\t\t" +info+ "Child " +child.getFqn()+ " [modVALUES]: width=" +child.getWidth()+ " length=" +child.getLength()+ "|=> sum_width=" +sum_width+ " sum_length=" +sum_length);}
		}
		
		if(DEBUG){System.out.println("\t\t" +info+ "calculateMaxArea(document)-exit.");}
		return new Rectangle(0,0, sum_width, sum_length,1);
	}

/*functions for Entity*/
	private static void arrangeChildren(Entity entity){
		if(DEBUG){System.out.println("\t" +info+ "layOut("+ entity.getFqn()+ ")-arrival.");}
		//get maxArea (worst case) for root of KDTree
		if(DEBUG){System.out.println("\t\t" +info+ "calculateMaxArea(" + entity.getFqn()+ ")-call.");}
		Rectangle entityRec = calculateMaxArea(entity);
		CityKDTree ptree = new CityKDTree(entityRec);
		if(DEBUG_Part1){System.out.println("\t\t" +info+ "KDTree [checkVALUES]: root[(" +ptree.getRoot().getRectangle().getUpperLeftX()+ "|" +ptree.getRoot().getRectangle().getUpperLeftY()+
																			"), (" +ptree.getRoot().getRectangle().getBottomRightX()+ "|" +ptree.getRoot().getRectangle().getBottomRightY()+ ")]");}
		Rectangle covrec = new Rectangle();
		if(DEBUG_Part1){System.out.println("\t\t" +info+ "CovRec [checkVALUES]: [(" +covrec.getUpperLeftX()+ "|" +covrec.getUpperLeftY()+ "), (" +covrec.getBottomRightX()+ "|" +covrec.getBottomRightY()+ ")]");}
		List<Rectangle> elements = sortChildrenAsRectangles(entity.getEntities());
		
		//start algorithm
		for(Rectangle el: elements){
			if(DEBUG_Part2){System.out.println("\n\t\t" +info+ "Entity " +el.getEntityLink().getFqn()+ " starts algorithm.");}
			List<CityKDTreeNode> pnodes = ptree.getFittingNodes(el);
			if(DEBUG_Part2){
				System.out.println("\n\t\t" +info+ "show all fittingNodes!");
				int node_number =0;
				for(CityKDTreeNode n: pnodes){
					node_number++;
					System.out.println("\t\t" +info+ "Node #" +node_number);
					System.out.println("\t\t" +info+ "Node Rec[(" +n.getRectangle().getUpperLeftX()+ "|" +n.getRectangle().getUpperLeftY()+
															"), (" +n.getRectangle().getBottomRightX()+ "|" +n.getRectangle().getBottomRightY()+ ")]");
				}
			}
			Map<CityKDTreeNode, Double> preservers = new LinkedHashMap<CityKDTreeNode, Double>();		//LinkedHashMap necessary, so elements are ordered by inserting-order
			Map<CityKDTreeNode, Double> expanders = new LinkedHashMap<CityKDTreeNode, Double>();
			CityKDTreeNode targetNode = new CityKDTreeNode();
			CityKDTreeNode fitNode = new CityKDTreeNode();
			
			//check all empty leaves: either they extend COVREC (->expanders) or it doesn't change (->preservers)
			if(DEBUG_Part2){System.out.println("\n\t\t" +info+ "check all empty leaves!");}
			for(CityKDTreeNode pnode: pnodes){
				sortEmptyLeaf(pnode, el, covrec, preservers, expanders);
			}
			
			//choose best-fitting pnode
			if(preservers.isEmpty() != true){
				targetNode = bestFitIsPreserver(preservers.entrySet());
			}else{
				targetNode = bestFitIsExpander(expanders.entrySet());
			}
			
			//modify targetNode if necessary
			if(targetNode.getRectangle().getWidth() == el.getWidth() && targetNode.getRectangle().getLength() == el.getLength()){		//this if could be skipped, trimmingNode() always returns fittingNode
				fitNode = targetNode;
				if(DEBUG_Part2){System.out.println("\n\t\t" +info+ "targetNode fits.");}
			}else{
				if(DEBUG_Part2){System.out.println("\n\t\t" +info+ "targetNode needs trimming.");}
				fitNode = trimmingNode(targetNode, el);
			}
			
			//set fitNode as occupied
			fitNode.setOccupied(true);
			
			//give Entity it's Position	
			setNewPosition(el, fitNode);
			
			//if fitNode expands covrec, update covrec
			if(fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX() || fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()){
				updateCovrec(fitNode, covrec);
			}
		}
		entity.setWidth(covrec.getBottomRightX()+ (CitySettings.BLDG_horizontalMargin-CitySettings.BLDG_horizontalGap/2)*2);
		entity.setLength(covrec.getBottomRightY()+ (CitySettings.BLDG_horizontalMargin-CitySettings.BLDG_horizontalGap/2)*2);
		if(DEBUG){System.out.println("\t\t" +info+ "Entity " +entity.getFqn()+ " [checkVALUES]: width=" +entity.getWidth()+ " length=" +entity.getLength());}
		if(DEBUG){System.out.println("\t" +info+ "layOut("+ entity.getFqn()+ ")-exit.");}
	}

	private static Rectangle calculateMaxArea(Entity entity) {
		if(DEBUG){System.out.println("\t\t" +info+ "calculateMaxArea(" +entity.getFqn()+ ")-arrival.");}
		EList<Entity> children = entity.getEntities();
		double sum_width = 0;
		double sum_length = 0;
		
		for(Entity child: children){
			if(CitySettings.BUILDING_TYPE == CitySettings.BuildingType.CITY_DYNAMIC){
				if(child.getType().equals("FAMIX.Namespace") || child.getType().equals("FAMIX.Class")) {
					if(DEBUG){System.out.println("\t\t\t" +info+ "layOut(" +child.getFqn()+ ")-call, recursive.");}
					arrangeChildren(child);
				}
			} else {
				if(child.getType().equals("FAMIX.Namespace")) {
					if(DEBUG){System.out.println("\t\t\t" +info+ "layOut(" +child.getFqn()+ ")-call, recursive.");}
					arrangeChildren(child);
				}
			}
			sum_width += child.getWidth()+CitySettings.BLDG_horizontalGap;
			sum_length += child.getLength() +CitySettings.BLDG_horizontalGap;
			if(DEBUG_Part1){System.out.println("\t\t\t" +info+ "Child " +child.getFqn()+ " [modVALUES]: width=" +child.getWidth()+ " length=" +child.getLength()+ "|=> sum_width=" +sum_width+ " sum_length=" +sum_length);}
		}
		
		if(DEBUG_Part1){System.out.println("\t\t\t" +info+ "Entity " +entity.getFqn()+ " [newVALUES]: width=" +entity.getWidth()+ " length=" +entity.getLength());}
		if(DEBUG){System.out.println("\t\t" +info+ "calculateMaxArea(" +entity.getFqn()+ ")-exit.");}
		return new Rectangle(0,0, sum_width, sum_length,1);
	}

/*functions for algorithm*/
	private static List<Rectangle> sortChildrenAsRectangles(EList<Entity> entities) {
		List<Rectangle> elements = new ArrayList<Rectangle>();
		//copy all child-elements into a List<Rectangle> (for easier sort) with links to former entities
		for(Entity e: entities){
			Rectangle rectangle = new Rectangle(0, 0, e.getWidth()+CitySettings.BLDG_horizontalGap, e.getLength()+CitySettings.BLDG_horizontalGap, 1);
			rectangle.setEntityLink(e);
			elements.add(rectangle);
			if(DEBUG_Part1){System.out.println("\t\t" +info+ " " +e.getFqn()+ " [checkVALUES]: [(" +rectangle.getUpperLeftX()+ "|" +rectangle.getUpperLeftY()+"), (" +rectangle.getBottomRightX()+ "|" +rectangle.getBottomRightY()+ ")]");}
			if(DEBUG_Part1){System.out.println("\t\t" +info+ "[checkEntity]: rectangle.getEntityLink() == e ->" +(rectangle.getEntityLink() == e));}
		}
				
		//sort elements by size in descending order
		Collections.sort(elements);
		if(DEBUG_Part1){
			System.out.println("\n\t\t" +info+ "[checkSort1]: order elements");
			for(Rectangle r: elements){
				System.out.println("\t\t" +info+ " " +r.getEntityLink().getFqn()+ "[checkValues]: area=" +r.getArea()+ " width=" +r.getWidth()+ " length=" +r.getLength());
			}
		}
		Collections.reverse(elements);
		if(DEBUG_Part2){
			System.out.println("\n\t\t" +info+ "[checkSort2]: descending order");
			for(Rectangle r: elements){
				System.out.println("\t\t" +info+ " " +r.getEntityLink().getFqn()+ "[checkValues]: area=" +r.getArea()+ " width=" +r.getWidth()+ " length=" +r.getLength());
			}
		}
		return elements;
	}

	private static void sortEmptyLeaf(CityKDTreeNode pnode, Rectangle el, Rectangle covrec, Map<CityKDTreeNode, Double> preservers,	Map<CityKDTreeNode, Double> expanders) {
		//either element fits in current bounds (->preservers) or it doesn't (->expanders)
		double nodeUpperLeftX = pnode.getRectangle().getUpperLeftX();
		double nodeUpperLeftY = pnode.getRectangle().getUpperLeftY();
		double nodeNewBottomRightX = nodeUpperLeftX + el.getWidth();				//expected BottomRightCorner, if el was insert into pnode
		double nodeNewBottomRightY = nodeUpperLeftY + el.getLength();				//this new corner-point is compared with covrec
		
		if(nodeNewBottomRightX <= covrec.getBottomRightX() && nodeNewBottomRightY <= covrec.getBottomRightY()){
			double waste = pnode.getRectangle().getArea() - el.getArea();
			preservers.put(pnode, waste);
			if(DEBUG_Part2){
				System.out.println("\t\t" +info+ "Node is preserver. waste=" +waste);
			}
		}else{
			double ratio = ( 	(nodeNewBottomRightX > covrec.getBottomRightX() ? nodeNewBottomRightX : covrec.getBottomRightX()) /
								(nodeNewBottomRightY > covrec.getBottomRightY() ? nodeNewBottomRightY : covrec.getBottomRightY()) );
			expanders.put(pnode, ratio);
			if(DEBUG_Part2){
				System.out.println("\t\t" +info+ "Node is expander. ratio=" +ratio+ " distance=" + Math.abs(ratio - 1));
			}
		}
	}

	private static CityKDTreeNode bestFitIsPreserver(Set<Entry<CityKDTreeNode, Double>> entrySet) {
		//determines which entry in Set has the lowest value of all
		double lowestValue =-1;
		CityKDTreeNode targetNode = new CityKDTreeNode();
		for(Map.Entry<CityKDTreeNode, Double> entry: entrySet){
			if(entry.getValue() < lowestValue || lowestValue == -1){
				lowestValue = entry.getValue();
				targetNode = entry.getKey();
			}
		}
		if(DEBUG_Part2){
			System.out.println("\t\t" +info+ "chosen Node is preserver: " +lowestValue);
			System.out.println("\t\t" +info+ "Node Rec[(" +targetNode.getRectangle().getUpperLeftX()+ "|" +targetNode.getRectangle().getUpperLeftY()+
					"), (" +targetNode.getRectangle().getBottomRightX()+ "|" +targetNode.getRectangle().getBottomRightY()+ ")]");
		}
		return targetNode;
	}

	private static CityKDTreeNode bestFitIsExpander(Set<Entry<CityKDTreeNode, Double>> entrySet) {
		double closestTo = 1;
		double lowestDistance = -1;
		CityKDTreeNode targetNode = new CityKDTreeNode();
		for(Map.Entry<CityKDTreeNode, Double> entry: entrySet){
			double distance = Math.abs(entry.getValue() - closestTo);
			if(distance < lowestDistance || lowestDistance == -1){
				lowestDistance = distance;
				targetNode = entry.getKey();
			}
		}
		if(DEBUG_Part2){
			System.out.println("\t\t" +info+ "chosen Node is expander: " +lowestDistance);
			System.out.println("\t\t" +info+ "Node Rec[(" +targetNode.getRectangle().getUpperLeftX()+ "|" +targetNode.getRectangle().getUpperLeftY()+
					"), (" +targetNode.getRectangle().getBottomRightX()+ "|" +targetNode.getRectangle().getBottomRightY()+ ")]");
		}
		return targetNode;
	}
	
	private static CityKDTreeNode trimmingNode(CityKDTreeNode node, Rectangle r){
		if(DEBUG){System.out.println("\t\t" +info+ "trimmingNode()-arrival.");}
		double nodeUpperLeftX = node.getRectangle().getUpperLeftX();
		double nodeUpperLeftY = node.getRectangle().getUpperLeftY();
		double nodeBottomRightX = node.getRectangle().getBottomRightX();
		double nodeBottomRightY = node.getRectangle().getBottomRightY();
		
		//first split: horizontal cut, if necessary
		// Round to 3 digits to prevent infinity loop, because e.g. 12.34000000007 is declared equal to 12.34
		if(Math.round(node.getRectangle().getLength()*1000d) != Math.round(r.getLength()*1000d)){
			//new child-nodes
			node.setLeftChild(new CityKDTreeNode(new Rectangle(nodeUpperLeftX, nodeUpperLeftY, nodeBottomRightX, (nodeUpperLeftY + r.getLength()))));
			node.setRightChild(new CityKDTreeNode(new Rectangle(nodeUpperLeftX, (nodeUpperLeftY + r.getLength()), nodeBottomRightX, nodeBottomRightY)));
			//set node as occupied (only leaves can contain elements)
			node.setOccupied(true);

			if(DEBUG_Part2){
				System.out.println("\t\t\t" +info+ "horizontal");
				System.out.println("\t\t\t" +info+ "targetNode Rec[(" +nodeUpperLeftX+ "|" +nodeUpperLeftY+
										"), (" +nodeBottomRightX+ "|" +nodeBottomRightY+ ")]");
				System.out.println("\t\t\t" +info+ "LeftChild Rec[(" +node.getLeftChild().getRectangle().getUpperLeftX()+ "|" +node.getLeftChild().getRectangle().getUpperLeftY()+
										"), (" +node.getLeftChild().getRectangle().getBottomRightX()+ "|" +node.getLeftChild().getRectangle().getBottomRightY()+ ")]");
				System.out.println("\t\t\t" +info+ "RightChild Rec[(" +node.getRightChild().getRectangle().getUpperLeftX()+ "|" +node.getRightChild().getRectangle().getUpperLeftY()+
						"), (" +node.getRightChild().getRectangle().getBottomRightX()+ "|" +node.getRightChild().getRectangle().getBottomRightY()+ ")]");
			}
			
			return trimmingNode(node.getLeftChild(), r);
		//second split: vertical cut, if necessary
		// Round to 3 digits, because e.g. 12.34000000007 is declared equal to 12.34
		}else if(Math.round(node.getRectangle().getWidth()*1000d) != Math.round(r.getWidth()*1000d)){
			//new child-nodes
			node.setLeftChild(new CityKDTreeNode(new Rectangle(nodeUpperLeftX, nodeUpperLeftY, (nodeUpperLeftX + r.getWidth()), nodeBottomRightY)));
			node.setRightChild(new CityKDTreeNode(new Rectangle((nodeUpperLeftX + r.getWidth()), nodeUpperLeftY, nodeBottomRightX, nodeBottomRightY)));
			//set node as occupied (only leaves can contain elements)
			node.setOccupied(true);
			
			if(DEBUG_Part2){
				System.out.println("\t\t\t" +info+ "vertical");
				System.out.println("\t\t\t" +info+ "targetNode Rec[(" +nodeUpperLeftX+ "|" +nodeUpperLeftY+
										"), (" +nodeBottomRightX+ "|" +nodeBottomRightY+ ")]");
				System.out.println("\t\t\t" +info+ "LeftChild Rec[(" +node.getLeftChild().getRectangle().getUpperLeftX()+ "|" +node.getLeftChild().getRectangle().getUpperLeftY()+
										"), (" +node.getLeftChild().getRectangle().getBottomRightX()+ "|" +node.getLeftChild().getRectangle().getBottomRightY()+ ")]");
				System.out.println("\t\t\t" +info+ "LeftChild center(" +node.getLeftChild().getRectangle().getCenterX()+ "|" +node.getLeftChild().getRectangle().getCenterY()+ ")");
				System.out.println("\t\t\t" +info+ "RightChild Rec[(" +node.getRightChild().getRectangle().getUpperLeftX()+ "|" +node.getRightChild().getRectangle().getUpperLeftY()+
						"), (" +node.getRightChild().getRectangle().getBottomRightX()+ "|" +node.getRightChild().getRectangle().getBottomRightY()+ ")]");
			}
			if(DEBUG){System.out.println("\t\t" +info+ "trimmingNode()-exit.");}
			return node.getLeftChild();
		}else{
			if(DEBUG){System.out.println("\t\t" +info+ "trimmingNode()-exit.");}
			return node;
		}
	}

	private static void setNewPosition(Rectangle el, CityKDTreeNode fitNode) {
		Position newPos = cityFactory.createPosition();
		//mapping 2D rectangle on 3D building
		newPos.setX(fitNode.getRectangle().getCenterX()-CitySettings.BLDG_horizontalGap/2);	//width
		newPos.setZ(fitNode.getRectangle().getCenterY()-CitySettings.BLDG_horizontalGap/2);	//length
		newPos.setY((el.getEntityLink().getHeight()/2));						//height
		
		el.getEntityLink().setPosition(newPos);
		if(DEBUG){System.out.println("\n\t\t" +info+ "Entity " +el.getEntityLink().getFqn()+ " [checkVALUES]: (" +el.getEntityLink().getPosition().getX()+ "|" +el.getEntityLink().getPosition().getY()+ "|" +el.getEntityLink().getPosition().getZ()+ ")\n");}
	}

	private static void updateCovrec(CityKDTreeNode fitNode, Rectangle covrec) {
		double newX = (fitNode.getRectangle().getBottomRightX() > covrec.getBottomRightX()? fitNode.getRectangle().getBottomRightX() : covrec.getBottomRightX());
		double newY = (fitNode.getRectangle().getBottomRightY() > covrec.getBottomRightY()? fitNode.getRectangle().getBottomRightY() : covrec.getBottomRightY());
		covrec.changeRectangle(0, 0, newX, newY);
		if(DEBUG){System.out.println("\t\t" +info+ "CovRec [checkVALUES]: [(" +covrec.getUpperLeftX()+ "|" +covrec.getUpperLeftY()+ "), (" +covrec.getBottomRightX()+ "|" +covrec.getBottomRightY()+ ")]");}
	}
	
	private static void adjustPositions(EList<Entity> children, double parentX, double parentY, double parentZ){
		for(Entity e: children){
			double centerX = e.getPosition().getX();
			double centerZ = e.getPosition().getZ();
			double centerY = e.getPosition().getY();
			e.getPosition().setX(centerX+parentX+CitySettings.BLDG_horizontalMargin/*-BLDG_horizontalGap/2*/);
			e.getPosition().setZ(centerZ+parentZ+CitySettings.BLDG_horizontalMargin/*-BLDG_horizontalGap/2*/);
			e.getPosition().setY(centerY+parentY+CitySettings.BLDG_verticalMargin);
			if(CitySettings.BUILDING_TYPE == CitySettings.BuildingType.CITY_DYNAMIC){
				if(e.getType().equals("FAMIX.Namespace") || e.getType().equals("FAMIX.Class")) {
					double newUpperLeftX = e.getPosition().getX()-e.getWidth()/2;
					double newUpperLeftZ = e.getPosition().getZ()-e.getLength()/2;
					double newUpperLeftY = e.getPosition().getY()-e.getHeight()/2;
					adjustPositions(e.getEntities(), newUpperLeftX, newUpperLeftY, newUpperLeftZ);
				}
			} else {
				if(e.getType().equals("FAMIX.Namespace")) {
					double newUpperLeftX = e.getPosition().getX()-e.getWidth()/2;
					double newUpperLeftZ = e.getPosition().getZ()-e.getLength()/2;
					double newUpperLeftY = e.getPosition().getY()-e.getHeight()/2;
					adjustPositions(e.getEntities(), newUpperLeftX, newUpperLeftY, newUpperLeftZ);
				}	
			}
		}
	}
}
