package org.getaviz.generator.city.m2m;

import java.util.List;

/**
 * This class is specifically designed for a KD-Tree used in SVIS-Generato,
 * following the example of Richard Wettel's CodeCity-Visualization Tool
 * 
 * @see <a href="http://www.blackpawn.com/texts/lightmaps/"></a>
 */
class CityKDTreeNode {
	CityKDTreeNode() {
		super();
		this.leftChild = null;
		this.rightChild = null;
		this.rectangle = new Rectangle();
		this.occupied = false;
	}

	CityKDTreeNode(Rectangle rectangle) {
		super();
		this.leftChild = null;
		this.rightChild = null;
		this.rectangle = rectangle;
		this.occupied = false;
	}

	private CityKDTreeNode leftChild;
	private CityKDTreeNode rightChild;
	private Rectangle rectangle;
	private boolean occupied;
	
	void isEmptyLeaf(Rectangle r, List<CityKDTreeNode> list){
		if(this.rectangle.getWidth() >= r.getWidth() && this.rectangle.getLength() >= r.getLength() && !this.occupied){
			list.add(this);
		}
		if(this.leftChild != null){
			this.leftChild.isEmptyLeaf(r, list);
		}
		if(this.rightChild != null){
			this.rightChild.isEmptyLeaf(r, list);
		}
	}
	CityKDTreeNode getLeftChild() {
		return leftChild;
	}
	void setLeftChild(CityKDTreeNode leftChild) {
		this.leftChild = leftChild;
	}
	CityKDTreeNode getRightChild() {
		return rightChild;
	}
	void setRightChild(CityKDTreeNode rightChild) {
		this.rightChild = rightChild;
	}
	Rectangle getRectangle() {
		return rectangle;
	}

	void setOccupied() {
		this.occupied = true;
	}
}