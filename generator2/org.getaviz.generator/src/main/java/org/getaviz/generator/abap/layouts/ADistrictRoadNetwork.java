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
import org.getaviz.generator.abap.layouts.road.network.Road;
import org.getaviz.generator.abap.layouts.road.network.RoadGraph;
import org.getaviz.generator.abap.layouts.road.network.RoadGraphDijkstraAlgorithm;
import org.getaviz.generator.abap.layouts.road.network.RoadNode;
import org.getaviz.generator.abap.layouts.road.network.RoadNodeBuilder;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityReferenceMapper;
import org.getaviz.generator.abap.repository.ACityElement.ACitySubType;
import org.getaviz.generator.abap.repository.ACityElement.ACityType;

public class ADistrictRoadNetwork {

	private RoadGraph roadGraph;
	
	private ACityElement district;
	private Map<ACityElement, RoadNode> mainElementConnectors;
	
	private Set<ACityElement> subElements;
	private Map<ACityElement, HashMap<ACityElement, RoadNode>> subElementConnectors;
	
	private ACityReferenceMapper referenceMapper;
	
	private SettingsConfiguration config;

	public ADistrictRoadNetwork(ACityElement mainElement, HashMap<ACityElement, RoadNode> mainElementConnectors, ACityReferenceMapper referenceMapper, SettingsConfiguration config) {
		
		this.roadGraph = new RoadGraph();
		
		this.district = mainElement;
		this.mainElementConnectors = mainElementConnectors;
		
		this.subElements = new HashSet<ACityElement>(district.getSubElements());
		this.subElementConnectors = new HashMap<ACityElement, HashMap<ACityElement,RoadNode>>();
		
		for (ACityElement subElement : this.subElements) {
			this.subElementConnectors.put(subElement, new HashMap<ACityElement, RoadNode>());			
		}
		
		this.referenceMapper = referenceMapper;
		
		this.config = config;
	}
	
	public HashMap<ACityElement, RoadNode> getSubElementConnectors(ACityElement subElement) {
		return subElementConnectors.get(subElement);
	}

	public List<ACityElement> calculate() {
		
		initializeRoadGraph();
		
		if (config.completeRoadNetwork()) {
			return extractRoads(roadGraph.getGraph());
		}
		
		RoadNodeBuilder nodeBuilder = new RoadNodeBuilder(config);

		// TODO
		// erstmal nur Workaround
		List<List<RoadNode>> paths = new ArrayList<List<RoadNode>>();
		
		for (ACityElement subelement : subElements) {
			
			if (subelement.getType() != ACityType.District) {
				continue;
			}
			
			List<RoadNode> slipRoadNodesSource = nodeBuilder.calculateSlipRoadNodes(subelement);
			Collection<ACityElement> referencedElements = referenceMapper.getAggregatedRelatedACityElements(subelement, referenceMapper.mapToAggregationLevel(subelement), false); 
			
			for (ACityElement referencedElement : referencedElements) {
				if (referencedElement == null || subelement == referencedElement) {
					continue;
				}
				
				if (!checkIfElementBelongsToOriginSet(referencedElement)) {
					continue;
				}
				
				List<RoadNode> slipRoadNodesTarget;
				
				if (subElements.contains(referencedElement)) {
					// von den Distrikten die Auffahrtspunkte bestimmen
					slipRoadNodesTarget = nodeBuilder.calculateSlipRoadNodes(referencedElement);
				} else {
					slipRoadNodesTarget = new ArrayList<RoadNode>();
					slipRoadNodesTarget.add(mainElementConnectors.get(referencedElement.getParentElement()));
				}

				// TODO
				// erstmal nur Workaround
				double shortestPathLength = Double.MAX_VALUE;
				List<RoadNode> shortestPathAbsolut = null;

				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
				// Aufruf von dijkstra
				for (RoadNode slipRoadNodeSource : slipRoadNodesSource) {
					for (RoadNode slipRoadNodeTarget : slipRoadNodesTarget) {
						List<List<RoadNode>> shortestPath = getAllShortestPaths(slipRoadNodeSource, slipRoadNodeTarget);
						
						double pathLength = roadGraph.calculatePathLength(shortestPath.get(0));
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
					shortestPathAbsolut.add(0, nodeBuilder.calculateDistrictSlipRoadNode(subelement, shortestPathAbsolut.get(0)));
					
					if (subElements.contains(referencedElement)) {
						// Auffahrt auf containingTargetDistrict
						shortestPathAbsolut.add(nodeBuilder.calculateDistrictSlipRoadNode(referencedElement, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
						
						subElementConnectors.get(subelement).put(referencedElement, nodeBuilder.calculateDistrictMarginRoadNode(subelement, shortestPathAbsolut.get(0)));
						subElementConnectors.get(referencedElement).put(subelement, nodeBuilder.calculateDistrictMarginRoadNode(referencedElement, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					} else {
						shortestPathAbsolut.add(nodeBuilder.calculateDistrictSlipRoadNode(district, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					}
					paths.add(shortestPathAbsolut);
				}
			}
			
			Collection<ACityElement> reverseReferencedElements = referenceMapper.getAggregatedRelatedACityElements(subelement, referenceMapper.mapToAggregationLevel(subelement), true); 
					
			// Wir müssen jetzt nur Beziehungen berücksichtigen, die von "draußen" kommen
			// Die anderen haben wir ja schon behandelt
			for (ACityElement referencedElement : reverseReferencedElements) {
				if (referencedElement == null || subelement == referencedElement || subElements.contains(referencedElement)) {
					continue;
				}
				
				if (!checkIfElementBelongsToOriginSet(referencedElement)) {
					continue;
				}

				// TODO
				// erstmal nur Workaround
				double shortestPathLength = Double.MAX_VALUE;
				List<RoadNode> shortestPathAbsolut = null;

				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
				// Aufruf von dijkstra
				for (RoadNode slipRoadNodeSource : slipRoadNodesSource) {
					List<List<RoadNode>> shortestPath = getAllShortestPaths(slipRoadNodeSource, mainElementConnectors.get(referencedElement.getParentElement()));
					
					double pathLength = roadGraph.calculatePathLength(shortestPath.get(0));
					if (pathLength < shortestPathLength) {
						shortestPathAbsolut = shortestPath.get(0);
						shortestPathLength = pathLength;
					}
				}

				// passenden Pfad bestimmen und merken
				// erstmal: kürzesten Pfad wählen
				if (shortestPathAbsolut != null) {
					
					// Auffahrt auf containingSourceDistrict
					shortestPathAbsolut.add(0, nodeBuilder.calculateDistrictSlipRoadNode(subelement, shortestPathAbsolut.get(0)));
					shortestPathAbsolut.add(nodeBuilder.calculateDistrictSlipRoadNode(district, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					paths.add(shortestPathAbsolut);
				}
			}
			
		}
		
//		return this.extractRoads(paths);
		return null;
	}
	
	public List<ACityElement> calculate2() {
		
		this.initializeRoadGraph();
		
		if (config.completeRoadNetwork()) {
			return extractRoads(roadGraph.getGraph());
		}
		
		RoadNodeBuilder nodeBuilder = new RoadNodeBuilder(config);

		// TODO
		// erstmal nur Workaround
		List<Road> roads = new ArrayList<Road>();
		
		for (ACityElement subelement : subElements) {
			
			if (subelement.getType() != ACityType.District) {
				continue;
			}
			
			List<RoadNode> slipRoadNodesSource = nodeBuilder.calculateSlipRoadNodes(subelement);
			Collection<ACityElement> referencedElements = referenceMapper.getAggregatedRelatedACityElements(subelement, referenceMapper.mapToAggregationLevel(subelement), false); 
			
			for (ACityElement referencedElement : referencedElements) {
				if (referencedElement == null || subelement == referencedElement) {
					continue;
				}
				
				if (!checkIfElementBelongsToOriginSet(referencedElement)) {
					continue;
				}
				
				List<RoadNode> slipRoadNodesTarget;
				
				if (subElements.contains(referencedElement)) {
					// von den Distrikten die Auffahrtspunkte bestimmen
					slipRoadNodesTarget = nodeBuilder.calculateSlipRoadNodes(referencedElement);
				} else {
					slipRoadNodesTarget = new ArrayList<RoadNode>();
					slipRoadNodesTarget.add(mainElementConnectors.get(referencedElement.getParentElement()));
				}

				// TODO
				// erstmal nur Workaround
				double shortestPathLength = Double.MAX_VALUE;
				List<RoadNode> shortestPathAbsolut = null;

				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
				// Aufruf von dijkstra
				for (RoadNode slipRoadNodeSource : slipRoadNodesSource) {
					for (RoadNode slipRoadNodeTarget : slipRoadNodesTarget) {
						List<List<RoadNode>> shortestPath = getAllShortestPaths(slipRoadNodeSource, slipRoadNodeTarget);
						
						double pathLength = roadGraph.calculatePathLength(shortestPath.get(0));
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
					shortestPathAbsolut.add(0, nodeBuilder.calculateDistrictSlipRoadNode(subelement, shortestPathAbsolut.get(0)));
					
					if (subElements.contains(referencedElement)) {
						// Auffahrt auf containingTargetDistrict
						shortestPathAbsolut.add(nodeBuilder.calculateDistrictSlipRoadNode(referencedElement, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
						
						subElementConnectors.get(subelement).put(referencedElement, nodeBuilder.calculateDistrictMarginRoadNode(subelement, shortestPathAbsolut.get(0)));
						subElementConnectors.get(referencedElement).put(subelement, nodeBuilder.calculateDistrictMarginRoadNode(referencedElement, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					} else {
						shortestPathAbsolut.add(nodeBuilder.calculateDistrictSlipRoadNode(this.district, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));
					}
					roads.add(new Road(subelement, referencedElement, shortestPathAbsolut));
				}
			}
			
			Collection<ACityElement> reverseReferencedElements = referenceMapper.getAggregatedRelatedACityElements(subelement, referenceMapper.mapToAggregationLevel(subelement), true); 
					
			// Wir müssen jetzt nur Beziehungen berücksichtigen, die von "draußen" kommen
			// Die anderen haben wir ja schon behandelt
			for (ACityElement referencedElement : reverseReferencedElements) {
				if (referencedElement == null || subelement == referencedElement || subElements.contains(referencedElement)) {
					continue;
				}
				
				if (!checkIfElementBelongsToOriginSet(referencedElement)) {
					continue;
				}

				// TODO
				// erstmal nur Workaround
				double shortestPathLength = Double.MAX_VALUE;
				List<RoadNode> shortestPathAbsolut = null;

				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
				// Aufruf von dijkstra
				for (RoadNode slipRoadNodeSource : slipRoadNodesSource) {
					List<List<RoadNode>> shortestPath = getAllShortestPaths(slipRoadNodeSource, mainElementConnectors.get(referencedElement.getParentElement()));
					
					double pathLength = roadGraph.calculatePathLength(shortestPath.get(0));
					if (pathLength < shortestPathLength) {
						shortestPathAbsolut = shortestPath.get(0);
						shortestPathLength = pathLength;
					}
				}

				// passenden Pfad bestimmen und merken
				// erstmal: kürzesten Pfad wählen
				if (shortestPathAbsolut != null) {
					
					// Auffahrt auf containingSourceDistrict
					shortestPathAbsolut.add(0, nodeBuilder.calculateDistrictSlipRoadNode(subelement, shortestPathAbsolut.get(0)));
					shortestPathAbsolut.add(nodeBuilder.calculateDistrictSlipRoadNode(district, shortestPathAbsolut.get(shortestPathAbsolut.size() - 1)));

					roads.add(new Road(referencedElement.getParentElement(), subelement, shortestPathAbsolut));
				}
			}
			
		}
		
		return this.extractRoads(roads);
	}

	private void initializeRoadGraph() {

		Map<Double, ArrayList<RoadNode>> nodesPerRows = new HashMap<Double, ArrayList<RoadNode>>();
		Map<Double, ArrayList<RoadNode>> nodesPerColumns = new HashMap<Double, ArrayList<RoadNode>>();

		Map<Double, ArrayList<ACityElement>> elementsPerRows = new HashMap<Double, ArrayList<ACityElement>>();
		Map<Double, ArrayList<ACityElement>> elementsPerColumns = new HashMap<Double, ArrayList<ACityElement>>();
		
		RoadNodeBuilder nodeBuilder = new RoadNodeBuilder(config);
		
		// create surrounding nodes of main district
		for (RoadNode node : nodeBuilder.calculateMarginRoadNodes(district)) {
			if (!roadGraph.hasNode(node)) {
				roadGraph.insertNode(node);
				
				nodesPerColumns.putIfAbsent(node.getX(), new ArrayList<RoadNode>());
				nodesPerColumns.get(node.getX()).add(node);
				
				nodesPerRows.putIfAbsent(node.getY(), new ArrayList<RoadNode>());
				nodesPerRows.get(node.getY()).add(node);					
			}
		}
		
		// create nodes per district subelement and group them by column and row
		for (ACityElement districtSubElement : district.getSubElements()) {
			for (RoadNode node : nodeBuilder.calculateSurroundingRoadNodes(districtSubElement)) {				
				if (!roadGraph.hasNode(node)) {
					roadGraph.insertNode(node);
					
					nodesPerColumns.putIfAbsent(node.getX(), new ArrayList<RoadNode>());
					nodesPerColumns.get(node.getX()).add(node);
					
					nodesPerRows.putIfAbsent(node.getY(), new ArrayList<RoadNode>());
					nodesPerRows.get(node.getY()).add(node);					
				}
			}
		}
		
		// group elements by column and row 
		// to check if an element is between two nodes
		for (ACityElement districtElement : district.getSubElements()) {
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
					roadGraph.insertEdge(lowerNode, upperNode);
					
				} else if (elementsInSameColumn.stream().noneMatch( 
								element -> (lowerNode.getY() < element.getZPosition() && element.getZPosition() < upperNode.getY()))) {					
					// no elements between nearby nodes -> create edge 
					roadGraph.insertEdge(lowerNode, upperNode);
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
					roadGraph.insertEdge(leftNode, rightNode);
					
				} else if (elementsInSameRow.stream().noneMatch( 
								element -> (leftNode.getX() < element.getXPosition() && element.getXPosition() < rightNode.getX()))) {
					// no elements between nearby nodes -> create edge 
					roadGraph.insertEdge(leftNode, rightNode);
				}
			}			
		}
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

//	private List<ACityElement> extractRoads(List<List<RoadNode>> paths) {
//		Map<RoadNode, ArrayList<RoadNode>> adjacencyList = new HashMap<RoadNode, ArrayList<RoadNode>>();
//
//		for (List<RoadNode> path : paths) {
//			for (int i = 0; i < path.size() - 1; i++) {
//				RoadNode node = path.get(i);
//				RoadNode successor = path.get(i + 1);
//
//				adjacencyList.putIfAbsent(node, new ArrayList<RoadNode>());
//				adjacencyList.putIfAbsent(successor, new ArrayList<RoadNode>());
//
//				if (!adjacencyList.get(node).contains(successor)) {
//					adjacencyList.get(node).add(successor);
//					adjacencyList.get(successor).add(node);
//				}
//			}
//		}
//
//		return this.extractRoads(adjacencyList);
//	}
	
	private List<ACityElement> extractRoads(List<Road> roads) {
		List<ACityElement> roadElementsUnfiltered = new ArrayList<ACityElement>();
		List<ACityElement> roadElements = new ArrayList<ACityElement>();
		
		for (Road road : roads) {
			int amountOfRelations = referenceMapper.getAmountOfRelationsToACityElement(road.getStartElement(), road.getDestinationElement(), false);
			for (int i = 0; i < road.getPath().size() - 1; i++) {
				roadElementsUnfiltered.add(createRoadACityElement(road.getPath().get(i), road.getPath().get(i + 1), amountOfRelations));
			}
		}
		
		Collections.sort(roadElementsUnfiltered, (elem1, elem2) -> {
			if (elem1.getXPosition() == elem2.getXPosition()) {
				if (elem1.getZPosition() == elem2.getZPosition()) {
					if (elem1.getWidth() == elem2.getWidth()) {
						return Double.compare(elem1.getLength(), elem2.getLength());
					} else {
						return Double.compare(elem1.getWidth(), elem2.getWidth());
					}
				} else {
					return Double.compare(elem1.getZPosition(), elem2.getZPosition());
				}
			} else {
				return Double.compare(elem1.getXPosition(), elem2.getXPosition());
			}
		});
		
		for (int i = 0; i < roadElementsUnfiltered.size() - 1; i++) {
			if (i == roadElementsUnfiltered.size() - 2) {
				roadElements.add(roadElementsUnfiltered.get(i + 1));
			}
			
			if (roadElementsUnfiltered.get(i).getXPosition() != roadElementsUnfiltered.get(i + 1).getXPosition()
					|| roadElementsUnfiltered.get(i).getZPosition() != roadElementsUnfiltered.get(i + 1).getZPosition()) {
				roadElements.add(roadElementsUnfiltered.get(i));
			}
		}
		
		return roadElements;
	}

	private ACityElement createRoadACityElement(RoadNode start, RoadNode end) {
		ACityElement road = new ACityElement(ACityType.Road);

		road.setXPosition((start.getX() + end.getX()) / 2.0);
		road.setYPosition(district.getYPosition() + config.getACityDistrictHeight() / 2.0
				+ config.getMetropolisRoadHeight() / 2.0);
		road.setZPosition((start.getY() + end.getY()) / 2.0);

		road.setWidth(Math.abs(start.getX() - end.getX()) + config.getMetropolisRoadWidth(ACitySubType.Street));
		road.setLength(Math.abs(start.getY() - end.getY()) + config.getMetropolisRoadWidth(ACitySubType.Street));
		road.setHeight(config.getMetropolisRoadHeight());

		return road;
	}

	private ACityElement createRoadACityElement(RoadNode start, RoadNode end, int amountOfRelations) {
		ACityElement road = new ACityElement(ACityType.Road);
		
		if (amountOfRelations < 5) {
			road.setSubType(ACitySubType.Lane);
		} else if (amountOfRelations < 10) {
			road.setSubType(ACitySubType.Street);
		} else {
			road.setSubType(ACitySubType.Freeway);
		}

		road.setXPosition((start.getX() + end.getX()) / 2.0);
		road.setYPosition(district.getYPosition() + config.getACityDistrictHeight() / 2.0
				+ config.getMetropolisRoadHeight() / 2.0);
		road.setZPosition((start.getY() + end.getY()) / 2.0);

		road.setWidth(Math.abs(start.getX() - end.getX()) + config.getMetropolisRoadWidth(road.getSubType()));
		road.setLength(Math.abs(start.getY() - end.getY()) + config.getMetropolisRoadWidth(road.getSubType()));
		road.setHeight(config.getMetropolisRoadHeight());

		return road;
	}

	private boolean checkIfElementBelongsToOriginSet(ACityElement element) {
		ACityElement parentElement = element;
		
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
	
	private List<RoadNode> getShortestPath(RoadNode startNode, RoadNode destinationNode) {
		RoadGraphDijkstraAlgorithm dijkstra = new RoadGraphDijkstraAlgorithm(this.roadGraph.getGraph());
		return dijkstra.calculateShortestPath(startNode, destinationNode);
	}
	
	private List<List<RoadNode>> getAllShortestPaths(RoadNode startNode, RoadNode destinationNode) {
		RoadGraphDijkstraAlgorithm dijkstra = new RoadGraphDijkstraAlgorithm(this.roadGraph.getGraph());
		return dijkstra.calculateAllShortestPaths(startNode, destinationNode);		
	}

}
