package org.svis.generator.city.m2m;

import java.util.List;
/**
 * This class is specifically designed for a KD-Tree used in SVIS-Generato,
 * following the example of Richard Wettel's CodeCity-Visualization Tool
 * 
 * @see <a href="http://www.blackpawn.com/texts/lightmaps/"></a>
 */
public class CityKDTreeNode {
	public CityKDTreeNode() {
		super();
		this.leftChild = null;
		this.rightChild = null;
		this.rectangle = new Rectangle();
		this.occupied = false;
	}

	public CityKDTreeNode(Rectangle rectangle) {
		super();
		this.leftChild = null;
		this.rightChild = null;
		this.rectangle = rectangle;
		this.occupied = false;
	}

	public CityKDTreeNode(CityKDTreeNode leftChild, CityKDTreeNode rightChild, Rectangle rectangle) {
		super();
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.rectangle = rectangle;
		this.occupied = false;
	}
	
	private CityKDTreeNode leftChild;
	private CityKDTreeNode rightChild;
	private Rectangle rectangle;
	private boolean occupied;
	
	public void isEmptyLeaf(Rectangle r, List<CityKDTreeNode> list){
		if(this.rectangle.getWidth() >= r.getWidth() && this.rectangle.getLength() >= r.getLength() && this.occupied == false){
			list.add(this);
		}
		if(this.leftChild != null){
			this.leftChild.isEmptyLeaf(r, list);
		}
		if(this.rightChild != null){
			this.rightChild.isEmptyLeaf(r, list);
		}
	}
	public CityKDTreeNode getLeftChild() {
		return leftChild;
	}
	public void setLeftChild(CityKDTreeNode leftChild) {
		this.leftChild = leftChild;
	}
	public CityKDTreeNode getRightChild() {
		return rightChild;
	}
	public void setRightChild(CityKDTreeNode rightChild) {
		this.rightChild = rightChild;
	}
	public Rectangle getRectangle() {
		return rectangle;
	}
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
	public boolean isOccupied() {
		return occupied;
	}
	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}
}