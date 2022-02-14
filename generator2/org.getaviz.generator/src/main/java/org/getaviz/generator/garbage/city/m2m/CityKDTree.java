package org.getaviz.generator.garbage.city.m2m;

import java.util.ArrayList;
import java.util.List;

class CityKDTree{

	CityKDTree(Rectangle rectangle) {
		super();
		this.root = new CityKDTreeNode(rectangle);
	}

	private CityKDTreeNode root;

	List<CityKDTreeNode> getFittingNodes(Rectangle r){
		List<CityKDTreeNode> fittingNodes = new ArrayList<>();
		this.root.isEmptyLeaf(r, fittingNodes);
		return fittingNodes;
	}

}