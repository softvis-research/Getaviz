package org.getaviz.generator.layouts.kdtree;

import java.util.List;

/**
 * This class is specifically designed for a KD-Tree used in SVIS-Generato,
 * following the example of Richard Wettel's CodeCity-Visualization Tool
 * 
 * @see <a href="http://www.blackpawn.com/texts/lightmaps/"></a>
 */
public class ACityKDTreeNode {
	public ACityKDTreeNode() {
		super();
		this.leftChild = null;
		this.rightChild = null;
		this.rectangle = new ACityRectangle();
		this.occupied = false;
	}

	public ACityKDTreeNode(ACityRectangle rectangle) {
		super();
		this.leftChild = null;
		this.rightChild = null;
		this.rectangle = rectangle;
		this.occupied = false;
	}

	public ACityKDTreeNode(ACityKDTreeNode leftChild, ACityKDTreeNode rightChild, ACityRectangle rectangle) {
		super();
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.rectangle = rectangle;
		this.occupied = false;
	}
	
	private ACityKDTreeNode leftChild;
	private ACityKDTreeNode rightChild;
	private ACityRectangle rectangle;
	private boolean occupied;
	
	public void isEmptyLeaf(ACityRectangle r, List<ACityKDTreeNode> list){
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
	public ACityKDTreeNode getLeftChild() {
		return leftChild;
	}
	public void setLeftChild(ACityKDTreeNode leftChild) {
		this.leftChild = leftChild;
	}
	public ACityKDTreeNode getRightChild() {
		return rightChild;
	}
	public void setRightChild(ACityKDTreeNode rightChild) {
		this.rightChild = rightChild;
	}
	public ACityRectangle getACityRectangle() {
		return rectangle;
	}
	public void setACityRectangle(ACityRectangle rectangle) {
		this.rectangle = rectangle;
	}
	public boolean isOccupied() {
		return occupied;
	}
	public void setOccupied() {
		this.occupied = true;
	}
}