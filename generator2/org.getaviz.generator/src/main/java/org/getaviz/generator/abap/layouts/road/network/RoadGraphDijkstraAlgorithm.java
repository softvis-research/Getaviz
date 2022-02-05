package org.getaviz.generator.abap.layouts.road.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RoadGraphDijkstraAlgorithm {

	private Map<RoadNode, ArrayList<RoadNode>> adjacencyList;

	public RoadGraphDijkstraAlgorithm(Map<RoadNode, ArrayList<RoadNode>> adjacencyList) {
		this.adjacencyList = adjacencyList;
	}

	public List<List<RoadNode>> calculateAllShortestPaths(RoadNode startNode, RoadNode destinationNode) {
		Map<RoadNode, Double> distanceMap = new HashMap<RoadNode, Double>();
		Map<RoadNode, List<List<RoadNode>>> shortestPaths = new HashMap<RoadNode, List<List<RoadNode>>>();
		List<RoadNode> path = new ArrayList<RoadNode>();

		for (RoadNode node : this.adjacencyList.keySet()) {
			if (node.equals(startNode)) {
				distanceMap.put(node, 0.0);
			} else {
				distanceMap.put(node, Double.MAX_VALUE);
			}
		}

		List<List<RoadNode>> dummy2 = new ArrayList<List<RoadNode>>();
		dummy2.add(new ArrayList<RoadNode>());

		shortestPaths.put(startNode, dummy2);

		while (!distanceMap.isEmpty()) {
			RoadNode nearestNode = getNearestNode(distanceMap);
			path.add(nearestNode);

			if (nearestNode == null || shortestPaths.get(nearestNode) == null) {
				System.out.println("Hilfe");
			}

			for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
				shortestPath.add(nearestNode);
			}

			if (nearestNode.equals(destinationNode)) {
				return shortestPaths.get(destinationNode);
			}

			double currentDistance = distanceMap.remove(nearestNode);

			for (RoadNode neighbourNode : this.adjacencyList.get(nearestNode)) {

				if (path.contains(neighbourNode)) {
					continue;
				}

				double newDistance = currentDistance + distance(nearestNode, neighbourNode);

				double oldDistance = distanceMap.get(neighbourNode);

				if (newDistance < oldDistance) {
					distanceMap.put(neighbourNode, newDistance);
					shortestPaths.put(neighbourNode, new ArrayList<List<RoadNode>>());
					for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
						shortestPaths.get(neighbourNode).add(new ArrayList<RoadNode>(shortestPath));
					}
				} else if (newDistance == oldDistance) {
					for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
						shortestPaths.get(neighbourNode).add(new ArrayList<RoadNode>(shortestPath));
					}
				}

//				if (distanceMap.get(nearestNode) + distance(nearestNode, neighbourNode) < distanceMap.get(neighbourNode)) {
//					distanceMap.put(neighbourNode, distanceMap.get(nearestNode) + distance(nearestNode, neighbourNode));
//					shortestPaths.put(neighbourNode, new ArrayList<List<RoadNode>>());
//					for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
//						shortestPaths.get(neighbourNode).add(new ArrayList<RoadNode>(shortestPath));
//					}
//				} else if (distanceMap.get(nearestNode) + distance(nearestNode, neighbourNode) == distanceMap.get(neighbourNode)) {
//					for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
//						shortestPaths.get(neighbourNode).add(new ArrayList<RoadNode>(shortestPath));
//					}
//				}
			}
//			distanceMap.remove(nearestNode);
			if (getNearestNode(distanceMap) == null) {
				System.out.println("debug");
			}
		}
		return shortestPaths.get(destinationNode);
	}

	public List<RoadNode> calculateShortestPath(RoadNode startNode, RoadNode destinationNode) {
		Map<RoadNode, Double> distanceMap = new HashMap<RoadNode, Double>();
		Map<RoadNode, List<RoadNode>> shortestPaths = new HashMap<RoadNode, List<RoadNode>>();
		List<RoadNode> path = new ArrayList<RoadNode>();

		for (RoadNode node : this.adjacencyList.keySet()) {
			if (node.equals(startNode)) {
				distanceMap.put(node, 0.0);
			} else {
				distanceMap.put(node, Double.MAX_VALUE);
			}
		}

		shortestPaths.put(startNode, new ArrayList<RoadNode>());

		while (!distanceMap.isEmpty()) {
			RoadNode nearestNode = getNearestNode(distanceMap);
			path.add(nearestNode);
			shortestPaths.get(nearestNode).add(nearestNode);

			if (nearestNode.equals(destinationNode)) {
				return shortestPaths.get(destinationNode);
			}

			for (RoadNode neighbourNode : this.adjacencyList.get(nearestNode)) {
				if (path.contains(neighbourNode)) {
					continue;
				}
				if (distanceMap.get(nearestNode) + distance(nearestNode, neighbourNode) < distanceMap
						.get(neighbourNode)) {
					distanceMap.put(neighbourNode, distanceMap.get(nearestNode) + distance(nearestNode, neighbourNode));
					shortestPaths.put(neighbourNode, new ArrayList<RoadNode>(shortestPaths.get(nearestNode)));
				}
			}
			distanceMap.remove(nearestNode);
		}
		return shortestPaths.get(destinationNode);
	}

	private RoadNode getNearestNode(Map<RoadNode, Double> distanceMap) {
		Double minDistance = Double.MAX_VALUE;
		RoadNode nearestNode = null;

		for (Entry<RoadNode, Double> nodeDistance : distanceMap.entrySet()) {
			if (nodeDistance.getValue() < minDistance) {
				nearestNode = nodeDistance.getKey();
				minDistance = nodeDistance.getValue();
			}
		}

		return nearestNode;
	}

	private double distance(RoadNode start, RoadNode destination) {

		// use Manhattan metric instead of Euclid metric due to performance
		return Math.abs(destination.getX() - start.getX() + destination.getY() - start.getY());
	}

}
