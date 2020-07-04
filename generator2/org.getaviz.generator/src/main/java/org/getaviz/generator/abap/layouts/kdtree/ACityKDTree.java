package org.getaviz.generator.abap.layouts.kdtree;

import java.util.ArrayList;
import java.util.List;

public class ACityKDTree {
	public ACityKDTree() {
		super();
		this.root = new ACityKDTreeNode();
	}

	public ACityKDTree(ACityKDTreeNode root) {
		super();
		this.root = root;
	}

	public ACityKDTree(ACityRectangle rectangle) {
		super();
		this.root = new ACityKDTreeNode(rectangle);
	}

	private ACityKDTreeNode root;

	public List<ACityKDTreeNode> getFittingNodes(ACityRectangle r){
		List<ACityKDTreeNode> fittingNodes = new ArrayList<ACityKDTreeNode>();
		this.root.isEmptyLeaf(r, fittingNodes);
		return fittingNodes;
	}
	public ACityKDTreeNode getRoot() {
		return root;
	}
	public void setRoot(ACityKDTreeNode root) {
		this.root = root;
	}
}