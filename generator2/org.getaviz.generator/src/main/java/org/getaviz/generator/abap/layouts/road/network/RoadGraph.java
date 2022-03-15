package org.getaviz.generator.abap.layouts.road.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoadGraph {
	
	private Map<RoadNode, ArrayList<RoadNode>> adjacencyList;
	
	public RoadGraph() {
		this.adjacencyList = new HashMap<RoadNode, ArrayList<RoadNode>>();
	}
	
	public boolean hasNode(RoadNode node) {
		return this.adjacencyList.containsKey(node);
	}
	
	public void insertNode(RoadNode node) {
		this.adjacencyList.putIfAbsent(node, new ArrayList<RoadNode>());
	}
	
	public void insertEdge(RoadNode sourceNode, RoadNode targetNode) {
		this.adjacencyList.get(sourceNode).add(targetNode);
		this.adjacencyList.get(targetNode).add(sourceNode);
	}
	
	public void deleteEdge(RoadNode sourceNode, RoadNode targetNode) {
		this.adjacencyList.get(sourceNode).remove(targetNode);
		this.adjacencyList.get(targetNode).remove(sourceNode);
	}
	
	public double calculatePathLength(List<RoadNode> path) {
		double pathLength = 0.0;
		
		for (int i = 0; i < path.size() - 1; i++) {
			pathLength += this.distance(path.get(i), path.get(i + 1));
		}
		
		return pathLength;
	}
	
	public Map<RoadNode, ArrayList<RoadNode>> getGraph() {
		return this.adjacencyList;
	}
	
	public Collection<RoadNode> getNodes() {
		return this.adjacencyList.keySet();
	}

	private double distance(RoadNode start, RoadNode destination) {

		// use Manhattan metric instead of Euclid metric due to performance
		return Math.abs(destination.getX() - start.getX() + destination.getY() - start.getY());
	}

}
