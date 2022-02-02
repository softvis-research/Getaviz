package org.getaviz.generator.abap.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.Methods;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.layouts.road.network.RoadGraph;
import org.getaviz.generator.abap.layouts.road.network.RoadNode;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityElement.ACityShape;
import org.getaviz.generator.abap.repository.ACityElement.ACityType;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.neo4j.driver.v1.types.Node;

public class ADistrictRoadNetwork {
	
	private RoadGraph roadGraph;
	
	private SourceNodeRepository nodeRepository;
	private ACityRepository repository;
	private ACityElement district;
    private SettingsConfiguration config;
	
	public ADistrictRoadNetwork(SourceNodeRepository nodeRepository, ACityRepository repository, ACityElement district, SettingsConfiguration config) {
		this.roadGraph = new RoadGraph(config);
		this.nodeRepository = nodeRepository;
		this.repository = repository;
		this.district = district;
		this.config = config;
	}
	
	public List<ACityElement> calculate() {
		
		this.roadGraph.initializeGraph(this.district.getSubElements());
		
		Map<ACityElement, Set<ACityElement>> callsMap = this.getCallsRelations(this.district.getSubElements());
		
		// TODO
		// erstmal nur Workaround
		List<List<RoadNode>> paths = new ArrayList<List<RoadNode>>();
		
		for (Entry<ACityElement, Set<ACityElement>> callsRelations: callsMap.entrySet()) {
			ACityElement containingSourceDistrict = this.getContainingSourceDistrict(callsRelations.getKey());
			RoadNode[] slipRoadNodesSource = this.calculateSlipRoadNodes(containingSourceDistrict);
			
			for (ACityElement target : callsRelations.getValue()) {
				// Top-Level-Quellcode-Distrikte für ACityElemente bestimmen
				ACityElement containingTargetDistrict = this.getContainingSourceDistrict(target);
				
				// Aufrufbeziehungen auf dem gleichen Distrikt sind (erstmal) irrelevant 
				if (containingTargetDistrict == null || containingSourceDistrict == containingTargetDistrict) {
					continue;
				}
				
				// von den Distrikten die Auffahrtspunkte bestimmen
				// siehe getSurroundingNodes() aus RoadGraph.java
				RoadNode[] slipRoadNodesTarget = this.calculateSlipRoadNodes(containingTargetDistrict);
				
				// TODO
				// erstmal nur Workaround
				double shortestPathLength = Double.MAX_VALUE; 
				List<RoadNode> shortestPathAbsolut = null;
				
				// kürzesten Pfad aller (4 * 4 =) 16 Kombinationen berechnen
				// Aufruf von dijkstra
				for (RoadNode slipRoadNodeSource : slipRoadNodesSource) {
					for (RoadNode slipRoadNodeTarget : slipRoadNodesTarget) {
						List<List<RoadNode>> shortestPath = this.roadGraph.getAllShortestPaths(slipRoadNodeSource, slipRoadNodeTarget);
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
					paths.add(shortestPathAbsolut);
				}
				
			}
			
		}
		
//		return this.extractRoads(this.roadGraph.getGraph());
		return this.extractRoads(paths);
	}
	
	private List<ACityElement> extractRoads(Map<RoadNode, LinkedList<RoadNode>> adjacencyList) {
		
		List<ACityElement> roads = new ArrayList<ACityElement>();
		
		for (RoadNode node : adjacencyList.keySet()) {
			LinkedList<RoadNode> connectedNodes = adjacencyList.get(node);
			
			for (RoadNode connectedNode : connectedNodes) {
				roads.add(createRoadACityElement(node, connectedNode));
				adjacencyList.get(connectedNode).remove(node); // eventuell auf einer Kopie des Graphen?
			}
		}		
		
		return roads;
	}
	
	private List<ACityElement> extractRoads(List<List<RoadNode>> paths) {
		Map<RoadNode, LinkedList<RoadNode>> adjacencyList = new HashMap<RoadNode, LinkedList<RoadNode>>();
				
		for (List<RoadNode> path : paths) {
			for (int i = 0; i < path.size() - 1; i++) {
				RoadNode node = path.get(i);
				RoadNode successor = path.get(i + 1);
				
				adjacencyList.putIfAbsent(node, new LinkedList<RoadNode>());
				adjacencyList.putIfAbsent(successor, new LinkedList<RoadNode>());
				
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
		road.setYPosition(this.district.getYPosition() + config.getACityDistrictHeight() / 2.0 + config.getMetropolisRoadHeight() / 2.0);
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
			
			Set<ACityElement> referencedElements = new HashSet<ACityElement>();
			callsMap.put(element, referencedElements);
			
			switch (element.getSourceNodeType()) {
			case Report:				
				Collection<Node> referencedNodes = this.nodeRepository.getRelatedNodes(sourceNode, SAPRelationLabels.REFERENCES, true);
				for (Node referencedNode : referencedNodes) {
					ACityElement correspondingElement = repository.getElementBySourceID(referencedNode.id());					
					if (!this.checkIfElementBelongsToOriginSet(correspondingElement)) {
						continue;
					}
					referencedElements.add(correspondingElement);					
				}
			
			case FunctionGroup:
			case Class:
				
				Map<ACityElement, Set<ACityElement>> localClassesCallsMap = this.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.Class));
				callsMap.putAll(localClassesCallsMap);
								
				switch (element.getSourceNodeType()) {
				case Class:
					Map<ACityElement, Set<ACityElement>> methodsCallsMap = this.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.Method));
					callsMap.putAll(methodsCallsMap);					
					break;
				case FunctionGroup:
					Map<ACityElement, Set<ACityElement>> functionModulesCallsMap = this.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.FunctionModule));
					callsMap.putAll(functionModulesCallsMap);
				case Report:
					Map<ACityElement, Set<ACityElement>> formroutinesCallsMap = this.getCallsRelations(element.getSubElementsOfSourceNodeType(SAPNodeTypes.FormRoutine));				
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
			
		}
		
		return callsMap;
	}
	
	private boolean checkIfElementBelongsToOriginSet(ACityElement element) {
		ACityElement parentElement = element.getParentElement();
		// TODO
		// Prüfung erweitern auf Grundmenge
		while(true) {
			if (parentElement.getSourceNodeType() == SAPNodeTypes.Namespace) {
				return parentElement == this.district;
			}
			parentElement = parentElement.getParentElement();			
		}
	}
	
	private ACityElement getContainingSourceDistrict(ACityElement element) {
		if (element.getParentElement() == null) {
			return null;
		}
		
		if (element.getType() == ACityType.District && element.getParentElement().getSourceNodeType() == SAPNodeTypes.Namespace) {
			return element;
		}
		
		return getContainingSourceDistrict(element.getParentElement());
	}
	
	private RoadNode[] calculateSlipRoadNodes(ACityElement element) {
		
		// 4 slips, one per direction
		RoadNode[] slipRoadNodes = new RoadNode[4];
		
		// TODO
		// In ADistrictLightMapLayout/ADistrictCircularLayout Breite der Straßen einbeziehen, dann kann die Breite auch hier berücksichtigt werden
		// Ist das perspektivisch sinnvoll?
		
		double rightX = element.getXPosition() + element.getWidth() / 2.0 + config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		double leftX = element.getXPosition() - element.getWidth() / 2.0 - config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;
		
		double upperY = element.getZPosition() + element.getLength() / 2.0 + config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		double lowerY = element.getZPosition() - element.getLength() / 2.0 - config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;
		
		RoadNode upperNode = new RoadNode(element.getXPosition(), upperY);
		RoadNode rightNode = new RoadNode(rightX, element.getZPosition());
		RoadNode lowerNode = new RoadNode(element.getXPosition(), lowerY);
		RoadNode leftNode = new RoadNode(leftX, element.getZPosition());
		
		if (this.roadGraph.getGraph().containsKey(upperNode)) {
			slipRoadNodes[0] = upperNode;
		}
		
		if (this.roadGraph.getGraph().containsKey(rightNode)) {
			slipRoadNodes[1] = rightNode;
		}
		
		if (this.roadGraph.getGraph().containsKey(lowerNode)) {
			slipRoadNodes[2] = lowerNode;
		}
		
		if (this.roadGraph.getGraph().containsKey(leftNode)) {
			slipRoadNodes[3] = leftNode;
		}
		
		return slipRoadNodes;
	}

}
