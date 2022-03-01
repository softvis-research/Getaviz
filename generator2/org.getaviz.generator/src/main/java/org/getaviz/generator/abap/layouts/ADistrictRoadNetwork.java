package org.getaviz.generator.abap.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.layouts.road.network.RoadGraph;
import org.getaviz.generator.abap.layouts.road.network.RoadGraphDijkstraAlgorithm;
import org.getaviz.generator.abap.layouts.road.network.RoadNode;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.abap.repository.ACityElement.ACityType;
import org.neo4j.driver.v1.types.Node;

public class ADistrictRoadNetwork {

	private RoadGraph roadGraph;

	private SourceNodeRepository nodeRepository;
	private ACityRepository repository;
	
	private ACityElement district;
	private Map<ACityElement, RoadNode> mainElementConnectors;
	
	private Set<ACityElement> subElements;
	private Map<ACityElement, HashMap<ACityElement, RoadNode>> subElementConnectors;
	
	private SettingsConfiguration config;

	public ADistrictRoadNetwork(SourceNodeRepository nodeRepository, ACityRepository repository, ACityElement mainElement, HashMap<ACityElement,
			 RoadNode> mainElementConnectors, SettingsConfiguration config) {
		
		this.roadGraph = new RoadGraph();
		
		this.nodeRepository = nodeRepository;
		this.repository = repository;
		
		this.district = mainElement;
		this.mainElementConnectors = mainElementConnectors;
		
		this.subElements = new HashSet<ACityElement>(district.getSubElements());
		this.subElementConnectors = new HashMap<ACityElement, HashMap<ACityElement,RoadNode>>();
		
		for (ACityElement subElement : this.subElements) {
			this.subElementConnectors.put(subElement, new HashMap<ACityElement, RoadNode>());			
		}
		
		this.config = config;
	}
	
	public HashMap<ACityElement, RoadNode> getSubElementConnectors(ACityElement subElement) {
		return this.subElementConnectors.get(subElement);
	}

	public List<ACityElement> calculate() {
		
		this.initializeRoadGraph();
		
		if (config.completeRoadNetwork()) {
			return this.extractRoads(this.roadGraph.getGraph());
		}

		Map<ACityElement, Set<ACityElement>> callsMap = this.getCallsRelations(this.district.getSubElements());		
		Map<ACityElement, Set<ACityElement>> calledMap = this.getCalledRelations(this.district.getSubElements());

		// TODO
		// erstmal nur Workaround
		List<List<RoadNode>> paths = new ArrayList<List<RoadNode>>();

		for (Entry<ACityElement, Set<ACityElement>> callsRelations : callsMap.entrySet()) {
			
			ACityElement containingSourceDistrict = this.getContainingDistrict(callsRelations.getKey());
			
			if (containingSourceDistrict == null) {
				continue;
			}
			
			List<RoadNode> slipRoadNodesSource = this.calculateSlipRoadNodes(containingSourceDistrict);		

			for (ACityElement target : callsRelations.getValue()) {
				// Top-Level-Quellcode-Distrikte für ACityElemente bestimmen
				ACityElement containingTargetDistrict = this.getContainingDistrict(target);

				// Aufrufbeziehungen auf dem gleichen Distrikt sind (erstmal) irrelevant
				if (containingTargetDistrict == null || containingSourceDistrict == containingTargetDistrict) {
					continue;
				}
				
				List<RoadNode> slipRoadNodesTarget;
				
				if (this.subElements.contains(containingTargetDistrict)) {
					// von den Distrikten die Auffahrtspunkte bestimmen
					slipRoadNodesTarget = this.calculateSlipRoadNodes(containingTargetDistrict);
				} else {
					slipRoadNodesTarget = new ArrayList<RoadNode>();
					// TODO Passenden Knoten bilden
					slipRoadNodesTarget.add(this.mainElementConnectors.get(containingTargetDistrict));
				}

				// TODO
				// erstmal nur Workaround
				double shortestPathLength = Double.MAX_VALUE;
				List<RoadNode> shortestPathAbsolut = null;

				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
				// Aufruf von dijkstra
				for (RoadNode slipRoadNodeSource : slipRoadNodesSource) {
					for (RoadNode slipRoadNodeTarget : slipRoadNodesTarget) {
						List<List<RoadNode>> shortestPath = this.getAllShortestPaths(slipRoadNodeSource, slipRoadNodeTarget);
						
						double pathLength = this.roadGraph.calculatePathLength(shortestPath.get(0));
						if (pathLength < shortestPathLength) {
							shortestPathAbsolut = shortestPath.get(0);
							shortestPathLength = pathLength;
						}
					}
				}

				// passenden Pfad bestimmen und merken
				// erstmal: kürzesten Pfad wählen
				if (shortestPathAbsolut != null) {
					
					// Auffahrt auf containingSourceDistrict
					shortestPathAbsolut.add(0, this.calculateDistrictRoadNode(containingSourceDistrict, shortestPathAbsolut.get(0)));
					
					if (this.subElements.contains(containingTargetDistrict)) {
						// Auffahrt auf containingTargetDistrict
						shortestPathAbsolut.add(this.calculateDistrictRoadNode(containingTargetDistrict, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
						this.subElementConnectors.get(containingSourceDistrict).put(containingTargetDistrict, this.calculateDistrictMarginNode(containingSourceDistrict, shortestPathAbsolut.get(0)));
						this.subElementConnectors.get(containingTargetDistrict).put(containingSourceDistrict, this.calculateDistrictMarginNode(containingTargetDistrict, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					} else {
						shortestPathAbsolut.add(this.calculateDistrictRoadNode(this.district, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					}
					paths.add(shortestPathAbsolut);
				}

			}

		}
		
		for (Entry<ACityElement, Set<ACityElement>> calledRelation : calledMap.entrySet()) {
			
			ACityElement containingTargetDistrict = this.getContainingDistrict(calledRelation.getKey());
			
			if (containingTargetDistrict == null) {
				continue;
			}
			
			List<RoadNode> slipRoadNodesTarget = this.calculateSlipRoadNodes(containingTargetDistrict);			

//			for (ACityElement source : calledRelation.getValue()) {
//				// Top-Level-Quellcode-Distrikte für ACityElemente bestimmen
//				ACityElement containingSourceDistrict = this.getContainingDistrict(source);
//
//				// Aufrufbeziehungen auf dem gleichen Distrikt sind (erstmal) irrelevant
//				if (containingSourceDistrict == null || containingTargetDistrict == containingSourceDistrict) {
//					continue;
//				}
//				
//				List<RoadNode> slipRoadNodesSource;
//				
//				if (this.subElements.contains(containingSourceDistrict)) {
//					// von den Distrikten die Auffahrtspunkte bestimmen
//					slipRoadNodesSource = this.calculateSlipRoadNodes(containingSourceDistrict);
//				} else {
//					slipRoadNodesSource = new ArrayList<RoadNode>();
//					// TODO Passenden Knoten bilden
//					slipRoadNodesSource.add(this.mainElementConnectors.get(containingSourceDistrict));
//				}
//
//				// TODO
//				// erstmal nur Workaround
//				double shortestPathLength = Double.MAX_VALUE;
//				List<RoadNode> shortestPathAbsolut = null;
//
//				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
//				// Aufruf von dijkstra
//				for (RoadNode slipRoadNodeTarget : slipRoadNodesTarget) {
//					for (RoadNode slipRoadNodeSource : slipRoadNodesSource) {
//						List<List<RoadNode>> shortestPath = this.getAllShortestPaths(slipRoadNodeTarget, slipRoadNodeSource);
//						double pathLength = this.roadGraph.calculatePathLength(shortestPath.get(0));
//						if (pathLength < shortestPathLength) {
//							shortestPathAbsolut = shortestPath.get(0);
//							shortestPathLength = pathLength;
//						}
//					}
//				}
//
//				// passenden Pfad bestimmen und merken
//				// erstmal: kürzesten Pfad wählen
//				if (shortestPathAbsolut != null) {
//			
//					// Auffahrt auf containingTargetDistrict
//					shortestPathAbsolut.add(0, this.calculateDistrictRoadNode(containingTargetDistrict, shortestPathAbsolut.get(0)));
			
//					if (this.subElements.contains(containingSourceDistrict)) {			
//						// Auffahrt auf containingSourceDistrict
//						shortestPathAbsolut.add(this.calculateDistrictRoadNode(containingSourceDistrict, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
//						this.subElementConnectors.get(containingTargetDistrict).put(containingSourceDistrict, shortestPathAbsolut.get(0));
//						this.subElementConnectors.get(containingSourceDistrict).put(containingTargetDistrict, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1));
//					}			
//					
//					paths.add(shortestPathAbsolut);
//				}
//
//			}
			
			// Wir müssen jetzt nur Beziehungen berücksichtigen, die von "draußen" kommen
			// Die anderen haben wir ja schon behandelt
			for (ACityElement source : calledRelation.getValue()) {
				// Top-Level-Quellcode-Distrikte für ACityElemente bestimmen
				ACityElement containingSourceDistrict = this.getContainingDistrict(source);

				// Aufrufbeziehungen auf dem gleichen Distrikt sind (erstmal) irrelevant
				if (containingSourceDistrict == null || containingTargetDistrict == containingSourceDistrict || this.subElements.contains(containingSourceDistrict)) {
					continue;
				}

				// TODO
				// erstmal nur Workaround
				double shortestPathLength = Double.MAX_VALUE;
				List<RoadNode> shortestPathAbsolut = null;

				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
				// Aufruf von dijkstra
				for (RoadNode slipRoadNodeTarget : slipRoadNodesTarget) {
					List<List<RoadNode>> shortestPath = this.getAllShortestPaths(slipRoadNodeTarget, this.mainElementConnectors.get(containingSourceDistrict));
					double pathLength = this.roadGraph.calculatePathLength(shortestPath.get(0));
					if (pathLength < shortestPathLength) {
						shortestPathAbsolut = shortestPath.get(0);
						shortestPathLength = pathLength;
					}
				}

				// passenden Pfad bestimmen und merken
				// erstmal: kürzesten Pfad wählen
				if (shortestPathAbsolut != null) {					
					// Auffahrt auf containingTargetDistrict
					shortestPathAbsolut.add(0, this.calculateDistrictRoadNode(containingTargetDistrict, shortestPathAbsolut.get(0)));
					shortestPathAbsolut.add(this.calculateDistrictRoadNode(this.district, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					paths.add(shortestPathAbsolut);
				}

			}

		}
		
		return this.extractRoads(paths);
	}

	private void initializeRoadGraph() {

		Map<Double, ArrayList<RoadNode>> nodesPerRows = new HashMap<Double, ArrayList<RoadNode>>();
		Map<Double, ArrayList<RoadNode>> nodesPerColumns = new HashMap<Double, ArrayList<RoadNode>>();

		Map<Double, ArrayList<ACityElement>> elementsPerRows = new HashMap<Double, ArrayList<ACityElement>>();
		Map<Double, ArrayList<ACityElement>> elementsPerColumns = new HashMap<Double, ArrayList<ACityElement>>();
		
		// create surrounding nodes of main district
		for (RoadNode node : this.calculateMarginRoadNodes(this.district)) {
			if (!this.roadGraph.hasNode(node)) {
				this.roadGraph.insertNode(node);
				
				nodesPerColumns.putIfAbsent(node.getX(), new ArrayList<RoadNode>());
				nodesPerColumns.get(node.getX()).add(node);
				
				nodesPerRows.putIfAbsent(node.getY(), new ArrayList<RoadNode>());
				nodesPerRows.get(node.getY()).add(node);					
			}
		}
		
		// create nodes per district subelement and group them by column and row
		for (ACityElement districtSubElement : this.district.getSubElements()) {
			for (RoadNode node : this.calculateSurroundingRoadNodes(districtSubElement)) {				
				if (!this.roadGraph.hasNode(node)) {
					this.roadGraph.insertNode(node);
					
					nodesPerColumns.putIfAbsent(node.getX(), new ArrayList<RoadNode>());
					nodesPerColumns.get(node.getX()).add(node);
					
					nodesPerRows.putIfAbsent(node.getY(), new ArrayList<RoadNode>());
					nodesPerRows.get(node.getY()).add(node);					
				}
			}
		}
		
		// group elements by column and row 
		// to check if an element is between two nodes
		for (ACityElement districtElement : this.district.getSubElements()) {
			double rightBound = districtElement.getXPosition() + districtElement.getWidth() / 2.0
					+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
			
			double leftBound = districtElement.getXPosition() - districtElement.getWidth() / 2.0
					- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;
			
			double upperBound = districtElement.getZPosition() + districtElement.getWidth() / 2.0
					+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
			
			double lowerBound = districtElement.getZPosition() - districtElement.getWidth() / 2.0
					- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

			for (Double column : nodesPerColumns.keySet()) {
				if (leftBound < column && column < rightBound) {
					elementsPerColumns.putIfAbsent(column, new ArrayList<ACityElement>());
					elementsPerColumns.get(column).add(districtElement);
				}
			}

			for (Double row : nodesPerRows.keySet()) {
				if (lowerBound < row && row < upperBound) {
					elementsPerRows.putIfAbsent(row, new ArrayList<ACityElement>());
					elementsPerRows.get(row).add(districtElement);
				}
			}
		}
		
		// create edges in every column
		for (Entry<Double, ArrayList<RoadNode>> nodesPerColumn : nodesPerColumns.entrySet()) {
			
			ArrayList<RoadNode> nodesInColumn = nodesPerColumn.getValue();
			
			// sort nodes ascending to get nearby nodes alongside in array
			Collections.sort(nodesInColumn, (node1, node2) -> {
				return Double.compare(node1.getY(), node2.getY());
			});
			
			ArrayList<ACityElement> elementsInSameColumn = elementsPerColumns.get(nodesPerColumn.getKey());
			
			for (int i = 0; i < nodesInColumn.size() - 1; i++) {
				
				RoadNode lowerNode = nodesInColumn.get(i);
				RoadNode upperNode = nodesInColumn.get(i + 1);
				
				if (elementsInSameColumn == null) {					
					// no elements in this columns -> create edge
					this.roadGraph.insertEdge(lowerNode, upperNode);
					
				} else if (elementsInSameColumn.stream().filter( 
								element -> ( lowerNode.getY() < element.getZPosition() && element.getZPosition() < upperNode.getY())
							).count() == 0) {
					
					// no elements between nearby nodes -> create edge 
					this.roadGraph.insertEdge(lowerNode, upperNode);
				}
			}			
		}
		
		// create edges in every row
		for (Entry<Double, ArrayList<RoadNode>> nodesPerRow : nodesPerRows.entrySet()) {
			
			ArrayList<RoadNode> nodesInRow = nodesPerRow.getValue();

			// sort nodes ascending to get nearby nodes alongside in array
			Collections.sort(nodesInRow, (node1, node2) -> {
				return Double.compare(node1.getX(), node2.getX());
			});
			
			ArrayList<ACityElement> elementsInSameRow = elementsPerRows.get(nodesPerRow.getKey());
			
			for (int i = 0; i < nodesInRow.size() - 1; i++) {
				
				RoadNode leftNode = nodesInRow.get(i);
				RoadNode rightNode = nodesInRow.get(i + 1);
				
				if (elementsInSameRow == null) {					
					// no elements in this row -> create edge
					this.roadGraph.insertEdge(leftNode, rightNode);
					
				} else if (elementsInSameRow.stream().filter( 
								element -> ( leftNode.getX() < element.getXPosition() && element.getXPosition() < rightNode.getX())
							).count() == 0) {
					
					// no elements between nearby nodes -> create edge 
					this.roadGraph.insertEdge(leftNode, rightNode);
				}
			}			
		}
	}
	
	private List<RoadNode> calculateMarginRoadNodes(ACityElement district) {
		List<RoadNode> marginNodes = new ArrayList<RoadNode>();
		
		double rightX = district.getXPosition() + district.getWidth() / 2.0
				- config.getACityDistrictHorizontalMargin();
		
		double leftX = district.getXPosition() - district.getWidth() / 2.0 
				+ config.getACityDistrictHorizontalMargin();

		double upperY = district.getZPosition() + district.getLength() / 2.0
				- config.getACityDistrictHorizontalMargin();
		
		double lowerY = district.getZPosition() - district.getLength() / 2.0
				+ config.getACityDistrictHorizontalMargin();
		
		RoadNode upperNode = new RoadNode(district.getXPosition(), upperY);
		RoadNode rightNode = new RoadNode(rightX, district.getZPosition());
		RoadNode lowerNode = new RoadNode(district.getXPosition(), lowerY);
		RoadNode leftNode = new RoadNode(leftX, district.getZPosition());
		
		RoadNode upperLeftNode = new RoadNode(leftX, upperY);
		RoadNode upperRightNode = new RoadNode(rightX, upperY);

		RoadNode lowerLeftNode = new RoadNode(leftX, lowerY);
		RoadNode lowerRightNode = new RoadNode(rightX, lowerY);
		
		marginNodes.add(upperNode);
		marginNodes.add(rightNode);
		marginNodes.add(lowerNode);
		marginNodes.add(leftNode);

		marginNodes.add(upperLeftNode);
		marginNodes.add(upperRightNode);
		marginNodes.add(lowerLeftNode);
		marginNodes.add(lowerRightNode);

		return marginNodes;
	}

	private List<RoadNode> calculateSurroundingRoadNodes(ACityElement element) {
		List<RoadNode> surroundingNodes = new ArrayList<RoadNode>();

		surroundingNodes.addAll(this.calculateSlipRoadNodes(element));
		surroundingNodes.addAll(this.calculateCornerRoadNodes(element));

		return surroundingNodes;
	}

	private List<RoadNode> calculateSlipRoadNodes(ACityElement element) {

		// 4 nodes, each per direction
		List<RoadNode> slipNodes = new ArrayList<RoadNode>(4);

		// TODO
		// In ADistrictLightMapLayout/ADistrictCircularLayout Breite der Straßen
		// einbeziehen, dann kann die Breite auch hier berücksichtigt werden
		// Ist das perspektivisch sinnvoll?

		double rightX = element.getXPosition() + element.getWidth() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double leftX = element.getXPosition() - element.getWidth() / 2.0 
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		double upperY = element.getZPosition() + element.getLength() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double lowerY = element.getZPosition() - element.getLength() / 2.0
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		RoadNode upperNode = new RoadNode(element.getXPosition(), upperY);
		RoadNode rightNode = new RoadNode(rightX, element.getZPosition());
		RoadNode lowerNode = new RoadNode(element.getXPosition(), lowerY);
		RoadNode leftNode = new RoadNode(leftX, element.getZPosition());

		slipNodes.add(upperNode);
		slipNodes.add(rightNode);
		slipNodes.add(lowerNode);
		slipNodes.add(leftNode);

		return slipNodes;
	}

	private List<RoadNode> calculateCornerRoadNodes(ACityElement element) {

		// 4 nodes, each per corner of area
		List<RoadNode> cornerNodes = new ArrayList<RoadNode>(4);

		// TODO
		// In ADistrictLightMapLayout/ADistrictCircularLayout Breite der Straßen
		// einbeziehen, dann kann die Breite auch hier berücksichtigt werden
		// Ist das perspektivisch sinnvoll?

		double rightX = element.getXPosition() + element.getWidth() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double leftX = element.getXPosition() - element.getWidth() / 2.0 
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		double upperY = element.getZPosition() + element.getLength() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double lowerY = element.getZPosition() - element.getLength() / 2.0
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		RoadNode upperLeftNode = new RoadNode(leftX, upperY);
		RoadNode upperRightNode = new RoadNode(rightX, upperY);

		RoadNode lowerLeftNode = new RoadNode(leftX, lowerY);
		RoadNode lowerRightNode = new RoadNode(rightX, lowerY);

		cornerNodes.add(upperLeftNode);
		cornerNodes.add(upperRightNode);
		cornerNodes.add(lowerLeftNode);
		cornerNodes.add(lowerRightNode);

		return cornerNodes;
	}
	
	private RoadNode calculateDistrictRoadNode(ACityElement district, RoadNode slipNode) {
		double x, y;		
		
		if (district.getXPosition() == slipNode.getX()) {
			x = district.getXPosition();
			y = district.getZPosition() + Math.signum(slipNode.getY() - district.getZPosition()) * (district.getLength() / 2.0 + config.getMetropolisRoadWidth() / 2.0);
		} else {
			x = district.getXPosition() + Math.signum(slipNode.getX() - district.getXPosition()) * (district.getWidth() / 2.0 + config.getMetropolisRoadWidth() / 2.0);
			y = district.getZPosition();
		}
		
		return new RoadNode(x, y);
	}
	
	private RoadNode calculateDistrictMarginNode(ACityElement district, RoadNode slipNode) {
		double x, y;		
		
		if (district.getXPosition() == slipNode.getX()) {
			x = district.getXPosition();
			y = district.getZPosition() + Math.signum(slipNode.getY() - district.getZPosition()) * (district.getLength() / 2.0 - config.getACityDistrictHorizontalMargin());
		} else {
			x = district.getXPosition() + Math.signum(slipNode.getX() - district.getXPosition()) * (district.getWidth() / 2.0 - config.getACityDistrictHorizontalMargin());
			y = district.getZPosition();
		}
		
		return new RoadNode(x, y);
	}

	private List<ACityElement> extractRoads(Map<RoadNode, ArrayList<RoadNode>> adjacencyList) {

		List<ACityElement> roads = new ArrayList<ACityElement>();

		for (RoadNode node : adjacencyList.keySet()) {
			ArrayList<RoadNode> connectedNodes = adjacencyList.get(node);

			for (RoadNode connectedNode : connectedNodes) {
				roads.add(createRoadACityElement(node, connectedNode));
				adjacencyList.get(connectedNode).remove(node); // eventuell auf einer Kopie des Graphen?
			}
		}

		return roads;
	}

	private List<ACityElement> extractRoads(List<List<RoadNode>> paths) {
		Map<RoadNode, ArrayList<RoadNode>> adjacencyList = new HashMap<RoadNode, ArrayList<RoadNode>>();

		for (List<RoadNode> path : paths) {
			for (int i = 0; i < path.size() - 1; i++) {
				RoadNode node = path.get(i);
				RoadNode successor = path.get(i + 1);

				adjacencyList.putIfAbsent(node, new ArrayList<RoadNode>());
				adjacencyList.putIfAbsent(successor, new ArrayList<RoadNode>());

				if (!adjacencyList.get(node).contains(successor)) {
					adjacencyList.get(node).add(successor);
					adjacencyList.get(successor).add(node);
				}
			}
		}

		return this.extractRoads(adjacencyList);
	}

	private ACityElement createRoadACityElement(RoadNode start, RoadNode end) {
		ACityElement road = new ACityElement(ACityType.Road);

		road.setXPosition((start.getX() + end.getX()) / 2.0);
		road.setYPosition(this.district.getYPosition() + config.getACityDistrictHeight() / 2.0
				+ config.getMetropolisRoadHeight() / 2.0);
		road.setZPosition((start.getY() + end.getY()) / 2.0);

		road.setWidth(Math.abs(start.getX() - end.getX()) + this.config.getMetropolisRoadWidth());
		road.setLength(Math.abs(start.getY() - end.getY()) + this.config.getMetropolisRoadWidth());
		road.setHeight(config.getMetropolisRoadHeight());

		return road;
	}

	private Map<ACityElement, Set<ACityElement>> getCallsRelations(Collection<ACityElement> elements) {
		Map<ACityElement, Set<ACityElement>> callsMap = new HashMap<ACityElement, Set<ACityElement>>();

		// TODO
		// Prüfen, ob bereits hier ggf. auf enthaltende Source-Distrikte gemappt werden soll

		for (ACityElement element : elements) {
			Node sourceNode = element.getSourceNode();
			if (sourceNode == null) {
				continue;
			}
			
			if (element.getSourceNodeType() == SAPNodeTypes.Namespace) {
				callsMap.putAll(getCallsRelations(element.getSubElements()));				
				continue;
			}

			Set<ACityElement> referencedElements = new HashSet<ACityElement>();
			callsMap.put(element, referencedElements);

			switch (element.getSourceNodeType()) {
			case Report:
				Collection<Node> referencedNodes = this.nodeRepository.getRelatedNodes(sourceNode,
						SAPRelationLabels.REFERENCES, true);
				for (Node referencedNode : referencedNodes) {
					ACityElement correspondingElement = repository.getElementBySourceID(referencedNode.id());
					if (!this.checkIfElementBelongsToOriginSet(correspondingElement)) {
						continue;
					}
					referencedElements.add(correspondingElement);
				}

			case FunctionGroup:
			case Class:

				Map<ACityElement, Set<ACityElement>> localClassesCallsMap = this
						.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.Class));
				callsMap.putAll(localClassesCallsMap);

				switch (element.getSourceNodeType()) {
				case Class:
					Map<ACityElement, Set<ACityElement>> methodsCallsMap = this
							.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.Method));
					callsMap.putAll(methodsCallsMap);
					break;
				case FunctionGroup:
					Map<ACityElement, Set<ACityElement>> functionModulesCallsMap = this
							.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.FunctionModule));
					callsMap.putAll(functionModulesCallsMap);
				case Report:
					Map<ACityElement, Set<ACityElement>> formroutinesCallsMap = this
							.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.FormRoutine));
					callsMap.putAll(formroutinesCallsMap);
					break;
				default:
				}
				break;

			case Method:
			case FunctionModule:
			case FormRoutine:

				referencedNodes = this.nodeRepository.getRelatedNodes(sourceNode, SAPRelationLabels.REFERENCES, true);
				for (Node referencedNode : referencedNodes) {
					ACityElement correspondingElement = repository.getElementBySourceID(referencedNode.id());
					if (!this.checkIfElementBelongsToOriginSet(correspondingElement)) {
						continue;
					}
					referencedElements.add(correspondingElement);
				}

				break;

			default:
				continue;
			}
			
			if (referencedElements.size() == 0) {
				callsMap.remove(element);
			}

		}
		
		return callsMap;
	}
	
	private Map<ACityElement, Set<ACityElement>> getCalledRelations(Collection<ACityElement> elements) {
		Map<ACityElement, Set<ACityElement>> calledMap = new HashMap<ACityElement, Set<ACityElement>>();
		
		for (ACityElement element : elements) {
			Node sourceNode = element.getSourceNode();
			if (sourceNode == null) {
				continue;
			}
			
//			if (element.getSourceNodeType() == SAPNodeTypes.Namespace) {
//				calledMap.putAll(getCalledRelations(element.getSubElements()));				
//				continue;
//			}

			Set<ACityElement> referencedElements = new HashSet<ACityElement>();
			calledMap.put(element, referencedElements);

			switch (element.getSourceNodeType()) {
			case Report:
				Collection<Node> referencedNodes = this.nodeRepository.getRelatedNodes(sourceNode,
						SAPRelationLabels.REFERENCES, false);
				for (Node referencedNode : referencedNodes) {
					ACityElement correspondingElement = repository.getElementBySourceID(referencedNode.id());
					if (!this.checkIfElementBelongsToOriginSet(correspondingElement)) {
						continue;
					}
					referencedElements.add(correspondingElement);
				}

			case FunctionGroup:
			case Class:
			case Interface:

				Map<ACityElement, Set<ACityElement>> localClassesCalledMap = this
						.getCalledRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.Class));
				calledMap.putAll(localClassesCalledMap);

				switch (element.getSourceNodeType()) {
				case Class:
				case Interface:
					Map<ACityElement, Set<ACityElement>> methodsCalledMap = this
							.getCalledRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.Method));
					calledMap.putAll(methodsCalledMap);
					break;
				case FunctionGroup:
					Map<ACityElement, Set<ACityElement>> functionModulesCalledMap = this
							.getCalledRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.FunctionModule));
					calledMap.putAll(functionModulesCalledMap);
				case Report:
					Map<ACityElement, Set<ACityElement>> formroutinesCalledMap = this
							.getCalledRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.FormRoutine));
					calledMap.putAll(formroutinesCalledMap);
					break;
				default:
				}
				break;

			case Method:
			case FunctionModule:
			case FormRoutine:

				referencedNodes = this.nodeRepository.getRelatedNodes(sourceNode, SAPRelationLabels.REFERENCES, false);
				for (Node referencedNode : referencedNodes) {
					ACityElement correspondingElement = repository.getElementBySourceID(referencedNode.id());
					if (!this.checkIfElementBelongsToOriginSet(correspondingElement)) {
						continue;
					}
					referencedElements.add(correspondingElement);
				}

				break;

			default:
				continue;
			}
			
			if (referencedElements.size() == 0) {
				calledMap.remove(element);
			}

		}
		
		return calledMap;
	}

	private boolean checkIfElementBelongsToOriginSet(ACityElement element) {
		ACityElement parentElement = element.getParentElement();
		
		while (true) {
			if (parentElement.getSourceNodeType() == SAPNodeTypes.Namespace) {
				String creator = parentElement.getSourceNodeProperty(SAPNodeProperties.creator);
	            int iteration = Integer.parseInt(parentElement.getSourceNodeProperty(SAPNodeProperties.iteration));
	            
	            // iteration == 0 && creator <> SAP => origin set (to be analyzed custom code)
	            // iteration > 0 					=> further referenced custom code
	            // creator == SAP 					=> coding of SAP standard
	            if (iteration == 0 && !creator.equals("SAP")) {
	            	return true;
	            } else {
	            	return false;
	            }
			}
			parentElement = parentElement.getParentElement();
		}
	}

	private ACityElement getContainingDistrict(ACityElement element) {
		if (this.subElements.contains(element)) {
			return element;
		}
		
		if (this.mainElementConnectors.containsKey(element)) {
			return element;
		}
		
		if (element.getParentElement() == null) {
			return null;
		}

		return getContainingDistrict(element.getParentElement());
	}
	
	private List<RoadNode> getShortestPath(RoadNode startNode, RoadNode destinationNode) {
		RoadGraphDijkstraAlgorithm dijkstra = new RoadGraphDijkstraAlgorithm(this.roadGraph.getGraph());
		return dijkstra.calculateShortestPath(startNode, destinationNode);
	}
	
	private List<List<RoadNode>> getAllShortestPaths(RoadNode startNode, RoadNode destinationNode) {
		RoadGraphDijkstraAlgorithm dijkstra = new RoadGraphDijkstraAlgorithm(this.roadGraph.getGraph());
		return dijkstra.calculateAllShortestPaths(startNode, destinationNode);		
	}

}
