package org.getaviz.generator.abap.layouts.road.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;

public class RoadGraph {
	
	private Map<RoadNode, LinkedList<RoadNode>> adjacencyList;
    private SettingsConfiguration config;
	
	public RoadGraph(SettingsConfiguration config) {
		this.config = config;
		this.adjacencyList = new HashMap<RoadNode, LinkedList<RoadNode>>();
	}
	
	public void initializeGraph(Collection<ACityElement> districtElements) {
		
		Map<Double, ArrayList<RoadNode>> nodesPerRow = new HashMap<Double, ArrayList<RoadNode>>();
		Map<Double, ArrayList<RoadNode>> nodesPerColumn = new HashMap<Double, ArrayList<RoadNode>>();
		
		Map<Double, ArrayList<ACityElement>> elementsPerRow = new HashMap<Double, ArrayList<ACityElement>>();
		Map<Double, ArrayList<ACityElement>> elementsPerColumn = new HashMap<Double, ArrayList<ACityElement>>();
		
		for (ACityElement districtElement : districtElements) {
			
			for (RoadNode surroundingNode : this.getSurroundingNodes(districtElement)) {
				
				if (!this.adjacencyList.containsKey(surroundingNode)) {
					this.adjacencyList.put(surroundingNode, new LinkedList<RoadNode>());						
				}			
				
				if (nodesPerRow.containsKey(surroundingNode.getY())) {
					nodesPerRow.get(surroundingNode.getY()).add(surroundingNode);
				} else {
					nodesPerRow.put(surroundingNode.getY(), new ArrayList<RoadNode>(){{add(surroundingNode);}});
				}
				
				if (nodesPerColumn.containsKey(surroundingNode.getX())) {
					nodesPerColumn.get(surroundingNode.getX()).add(surroundingNode);
				} else {
					nodesPerColumn.put(surroundingNode.getX(), new ArrayList<RoadNode>(){{add(surroundingNode);}});
				}
			}					
		}
				
		for (ACityElement districtElement : districtElements) {
			double rightBound = districtElement.getXPosition() + districtElement.getWidth() / 2.0 + config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
			double leftBound = districtElement.getXPosition() - districtElement.getWidth() / 2.0 - config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;
			double upperBound = districtElement.getZPosition() + districtElement.getWidth() / 2.0 + config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
			double lowerBound = districtElement.getZPosition() - districtElement.getWidth() / 2.0 - config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;
			
			for (Double column : nodesPerColumn.keySet()) {
				if (leftBound < column && column < rightBound) {
					if (elementsPerColumn.containsKey(column)) {
						elementsPerColumn.get(column).add(districtElement);
					} else {
						elementsPerColumn.put(column, new ArrayList<ACityElement>(){{add(districtElement);}});
					}
				}				
			}
			
			for (Double row : nodesPerRow.keySet()) {
				if (lowerBound < row && row < upperBound) {
					if (elementsPerRow.containsKey(row)) {
						elementsPerRow.get(row).add(districtElement);
					} else {
						elementsPerRow.put(row, new ArrayList<ACityElement>(){{add(districtElement);}});
					}
				}				
			}
		}
		
		for (RoadNode node : this.adjacencyList.keySet()) {
			ArrayList<RoadNode> nodesInSameRow = nodesPerRow.get(node.getY());
			ArrayList<ACityElement> elementsInSameRow = elementsPerRow.get(node.getY());
 			
			RoadNode nextLeftNode = new RoadNode(Double.NEGATIVE_INFINITY, node.getY());
			RoadNode nextRightNode = new RoadNode(Double.POSITIVE_INFINITY, node.getY());
						
			for (RoadNode nodeInSameRow : nodesInSameRow) {
				if (nodeInSameRow.getX() < node.getX() && nextLeftNode.getX() < nodeInSameRow.getX()) {				
					if (elementsInSameRow == null) {
						nextLeftNode = nodeInSameRow;
					}
					else if(elementsInSameRow.stream().filter(element -> (nodeInSameRow.getX() < element.getXPosition() && element.getXPosition() < node.getX())).count() == 0) {
						nextLeftNode = nodeInSameRow;
					};
				}
				
				if (nodeInSameRow.getX() > node.getX() && nextRightNode.getX() > nodeInSameRow.getX()) {
					if (elementsInSameRow == null) {
						nextRightNode = nodeInSameRow;
					}
					else if(elementsInSameRow.stream().filter(element -> (nodeInSameRow.getX() > element.getXPosition() && element.getXPosition() > node.getX())).count() == 0) {
						nextRightNode = nodeInSameRow;
					};
				}
			}
			
			ArrayList<RoadNode> nodesInSameColumn = nodesPerColumn.get(node.getX());
			ArrayList<ACityElement> elementsInSameColumn = elementsPerColumn.get(node.getX());
 			
			RoadNode nextLowerNode = new RoadNode(node.getX(), Double.NEGATIVE_INFINITY);
			RoadNode nextUpperNode = new RoadNode(node.getX(), Double.POSITIVE_INFINITY);
			
			for (RoadNode nodeInSameColumn : nodesInSameColumn) {
				if (nodeInSameColumn.getY() < node.getY() && nextLowerNode.getY() < nodeInSameColumn.getY()) {
					if (elementsInSameColumn == null) {
						nextLowerNode = nodeInSameColumn;
					}
					else if(elementsInSameColumn.stream().filter(element -> (nodeInSameColumn.getY() < element.getZPosition() && element.getZPosition() < node.getY())).count() == 0) {
						nextLowerNode = nodeInSameColumn;
					};
				}
				
				if (nodeInSameColumn.getY() > node.getY() && nextUpperNode.getY() > nodeInSameColumn.getY()) {
					if (elementsInSameColumn == null) {
						nextUpperNode = nodeInSameColumn;
					}
					else if(elementsInSameColumn.stream().filter(element -> (nodeInSameColumn.getY() > element.getZPosition() && element.getZPosition() > node.getY())).count() == 0) {
						nextUpperNode = nodeInSameColumn;
					};
				}
			}
			
			if (nextLeftNode.getX() != Double.NEGATIVE_INFINITY && !this.adjacencyList.get(node).contains(nextLeftNode)) {
				insertEdge(node, nextLeftNode);
			}
			
			if (nextRightNode.getX() != Double.POSITIVE_INFINITY && !this.adjacencyList.get(node).contains(nextRightNode)) {
				insertEdge(node, nextRightNode);
			}
			
			if (nextLowerNode.getY() != Double.NEGATIVE_INFINITY && !this.adjacencyList.get(node).contains(nextLowerNode)) {
				insertEdge(node, nextLowerNode);
			}
			
			if (nextUpperNode.getY() != Double.POSITIVE_INFINITY && !this.adjacencyList.get(node).contains(nextUpperNode)) {
				insertEdge(node, nextUpperNode);
			}
		}
		
	}
	
	public boolean hasRoadNode(RoadNode node) {
		return this.adjacencyList.containsKey(node);
	}
	
	public void insertEdge(RoadNode sourceNode, RoadNode targetNode) {
		this.adjacencyList.get(sourceNode).add(targetNode);
		this.adjacencyList.get(targetNode).add(sourceNode);
	}
	
	public void deleteEdge(RoadNode sourceNode, RoadNode targetNode) {
		this.adjacencyList.get(sourceNode).remove(targetNode);
		this.adjacencyList.get(targetNode).remove(sourceNode);
	}
	
	public List<RoadNode> getShortestPath(RoadNode startNode, RoadNode destinationNode) {
		RoadGraphDijkstraAlgorithm dijkstra = new RoadGraphDijkstraAlgorithm(this.adjacencyList);
		return dijkstra.calculateShortestPath(startNode, destinationNode);
	}
	
	public List<List<RoadNode>> getAllShortestPaths(RoadNode startNode, RoadNode destinationNode) {
		RoadGraphDijkstraAlgorithm dijkstra = new RoadGraphDijkstraAlgorithm(this.adjacencyList);
		return dijkstra.calculateAllShortestPaths(startNode, destinationNode);
	}
	
	public double calculatePathLength(List<RoadNode> path) {
		double pathLength = 0.0;
		
		for (int i = 0; i < path.size() - 1; i++) {
			pathLength += this.distance(path.get(i), path.get(i + 1));
		}
		
		return pathLength;
	}
	
	public Map<RoadNode, LinkedList<RoadNode>> getGraph() {
		return this.adjacencyList;
	}

	private List<RoadNode> getSurroundingNodes(ACityElement element) {
		List<RoadNode> surroundingNodes = new ArrayList<RoadNode>();
		
		// TODO
		// In ADistrictLightMapLayout/ADistrictCircularLayout Breite der Straßen einbeziehen, dann kann die Breite auch hier berücksichtigt werden
		// Ist das perspektivisch sinnvoll?
		
		double rightX = element.getXPosition() + element.getWidth() / 2.0 + config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		double leftX = element.getXPosition() - element.getWidth() / 2.0 - config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;
		
		double upperY = element.getZPosition() + element.getLength() / 2.0 + config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		double lowerY = element.getZPosition() - element.getLength() / 2.0 - config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;
		
		RoadNode upperLeftNode = new RoadNode(leftX, upperY);
		RoadNode upperCentralNode = new RoadNode(element.getXPosition(), upperY);
		RoadNode upperRightNode = new RoadNode(rightX, upperY);
		
		RoadNode leftNode = new RoadNode(leftX, element.getZPosition());
		RoadNode rightNode = new RoadNode(rightX, element.getZPosition());
		
		RoadNode lowerLeftNode = new RoadNode(leftX, lowerY);
		RoadNode lowerCentralNode = new RoadNode(element.getXPosition(), lowerY);
		RoadNode lowerRightNode = new RoadNode(rightX, lowerY);
		
		surroundingNodes.add(upperLeftNode);
		surroundingNodes.add(upperCentralNode);
		surroundingNodes.add(upperRightNode);
		surroundingNodes.add(leftNode);
		surroundingNodes.add(rightNode);
		surroundingNodes.add(lowerLeftNode);
		surroundingNodes.add(lowerCentralNode);
		surroundingNodes.add(lowerRightNode);	
		
		return surroundingNodes;
	}

	private double distance(RoadNode start, RoadNode destination) {

		// use Manhattan metric instead of Euclid metric due to performance
		return Math.abs(destination.getX() - start.getX() + destination.getY() - start.getY());
	}

}
