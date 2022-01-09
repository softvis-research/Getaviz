package org.getaviz.generator.abap.layouts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.layouts.road.network.RoadGraph;
import org.getaviz.generator.abap.layouts.road.network.RoadNode;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityElement.ACityShape;
import org.getaviz.generator.abap.repository.ACityElement.ACityType;
import org.getaviz.generator.abap.repository.ACityRepository;

public class ADistrictRoadNetwork {

	private ACityRepository repository;
	private ACityElement district;
    private SettingsConfiguration config;
	
	public ADistrictRoadNetwork(ACityRepository repository, ACityElement district, SettingsConfiguration config) {
		this.repository = repository;
		this.district = district;
		this.config = config;
	}
	
	public void calculate() {
		RoadGraph roadGraph = new RoadGraph(config);
		
		roadGraph.initializeGraph(this.district.getSubElements());
		
		List<ACityElement> roads = this.extractRoads(roadGraph.getGraph());
		
		for (ACityElement road : roads) {
			this.district.addSubElement(road);
			this.repository.addElement(road);
		}
	}
	
	private List<ACityElement> extractRoads(Map<RoadNode, LinkedList<RoadNode>> adjacencyList) {
		
		List<ACityElement> roads = new ArrayList<ACityElement>();
		
		for (RoadNode node : adjacencyList.keySet()) {
			LinkedList<RoadNode> connectedNodes = adjacencyList.get(node);
			
			for (RoadNode connectedNode : connectedNodes) {
				roads.add(createRoadACityElement(node, connectedNode));
				adjacencyList.get(connectedNode).remove(node);
			}
		}		
		
		return roads;
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

}
