package org.getaviz.generator.abap.layouts.road.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RoadGraphDijkstraAlgorithm {

	private Map<RoadNode, ArrayList<RoadNode>> adjacencyList;

	public RoadGraphDijkstraAlgorithm(Map<RoadNode, ArrayList<RoadNode>> adjacencyList) {
		this.adjacencyList = adjacencyList;
	}

	public List<List<RoadNode>> calculateAllShortestPaths(RoadNode startNode, RoadNode destinationNode) {
		Map<RoadNode, Double> distanceMap = new HashMap<RoadNode, Double>();
		Map<RoadNode, List<List<RoadNode>>> shortestPaths = new HashMap<RoadNode, List<List<RoadNode>>>();
		Set<RoadNode> visitedNodes = new HashSet<RoadNode>();
		
		// initialize distanceMap
		for (RoadNode node : this.adjacencyList.keySet()) {
			if (node.equals(startNode)) {
				distanceMap.put(node, 0.0);
			} else {
				distanceMap.put(node, Double.MAX_VALUE);
			}
		}
		
		shortestPaths.put(startNode, new ArrayList<List<RoadNode>>() {{ add(new ArrayList<RoadNode>()); }});

		while (!distanceMap.isEmpty()) {
			RoadNode nearestNode = getNearestNode(distanceMap);
			
			// mark current node as visited
			visitedNodes.add(nearestNode);
			
			// add current node to all shortest paths to the current node
			// so that these paths are complete
			for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
				shortestPath.add(nearestNode);
			}
			
			if (nearestNode.equals(destinationNode)) {
				return shortestPaths.get(destinationNode);
			}
			
			// distance from current node to start node
			double currentDistance = distanceMap.remove(nearestNode);
			
			for (RoadNode neighbourNode : this.adjacencyList.get(nearestNode)) {
				
				// shortest path to visited nodes was already calculated
				if (visitedNodes.contains(neighbourNode)) {
					continue;
				}

				double newDistance = currentDistance + distance(nearestNode, neighbourNode);
				double previousDistance = distanceMap.get(neighbourNode);

				if (newDistance < previousDistance) {
					distanceMap.put(neighbourNode, newDistance);
					
					// shortest paths to current node plus path to nearby node are
					// shorter than the previous calculated shortest paths to nearby node
					// so we have to save only the shortest paths to current node
					shortestPaths.put(neighbourNode, new ArrayList<List<RoadNode>>());					
					for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
						shortestPaths.get(neighbourNode).add(new ArrayList<RoadNode>(shortestPath));
					}
					
				} else if (newDistance == previousDistance) {
					
					// shortest paths to current node plus path to nearby node are
					// equal short than the previous calculated shortest paths to nearby node
					// so we have to save all
					for (List<RoadNode> shortestPath : shortestPaths.get(nearestNode)) {
						shortestPaths.get(neighbourNode).add(new ArrayList<RoadNode>(shortestPath));
					}
				}
			}
		}
		return shortestPaths.get(destinationNode);
	}

	public List<RoadNode> calculateShortestPath(RoadNode startNode, RoadNode destinationNode) {
		Map<RoadNode, Double> distanceMap = new HashMap<RoadNode, Double>();
		Map<RoadNode, List<RoadNode>> shortestPaths = new HashMap<RoadNode, List<RoadNode>>();
		Set<RoadNode> visitedNodes = new HashSet<RoadNode>();

		// initialize distanceMap
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
			
			// mark current node as visited
			visitedNodes.add(nearestNode);
			
			// add current node to his shortest path
			// so that the path is complete
			shortestPaths.get(nearestNode).add(nearestNode);

			if (nearestNode.equals(destinationNode)) {
				return shortestPaths.get(destinationNode);
			}
			
			// distance from current node to start node
			double currentDistance = distanceMap.remove(nearestNode);

			for (RoadNode neighbourNode : this.adjacencyList.get(nearestNode)) {
				
				// shortest path to visited nodes was already calculated
				if (visitedNodes.contains(neighbourNode)) {
					continue;
				}

				double newDistance = currentDistance + distance(nearestNode, neighbourNode);
				double previousDistance = distanceMap.get(neighbourNode);
				
				if (newDistance < previousDistance) {
					distanceMap.put(neighbourNode, newDistance);
					
					// shortest path to current node plus path to nearby node is
					// shorter than the previous calculated shortest path to nearby node
					// so we have to save the shortest path to current node
					shortestPaths.put(neighbourNode, new ArrayList<RoadNode>(shortestPaths.get(nearestNode)));
				}
			}
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
