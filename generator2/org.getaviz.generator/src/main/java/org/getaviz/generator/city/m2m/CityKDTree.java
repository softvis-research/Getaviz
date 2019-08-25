package org.getaviz.generator.city.m2m;

import java.util.ArrayList;
import java.util.List;
import org.getaviz.generator.city.m2m.Rectangle;

public class CityKDTree{
	public CityKDTree() {
		super();
		this.root = new CityKDTreeNode();
	}

	public CityKDTree(CityKDTreeNode root) {
		super();
		this.root = root;
	}

	public CityKDTree(Rectangle rectangle) {
		super();
		this.root = new CityKDTreeNode(rectangle);
	}

	private CityKDTreeNode root;

	public List<CityKDTreeNode> getFittingNodes(Rectangle r){
		List<CityKDTreeNode> fittingNodes = new ArrayList<CityKDTreeNode>();
		this.root.isEmptyLeaf(r, fittingNodes);
		return fittingNodes;
	}
	public CityKDTreeNode getRoot() {
		return root;
	}
	public void setRoot(CityKDTreeNode root) {
		this.root = root;
	}
}