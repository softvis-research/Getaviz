package org.getaviz.generator.abap.layouts.road.network;

import java.util.ArrayList;
import java.util.List;

import org.getaviz.generator.abap.repository.ACityElement;

public class Road {
	
	private ACityElement startElement;
	private ACityElement destinationElement;
	private List<RoadNode> path;

	public Road(ACityElement startElement, ACityElement destinationElement) {
		this.startElement = startElement;
		this.destinationElement = destinationElement;
		
		this.path = new ArrayList<RoadNode>();
	}
	
	public Road(ACityElement startElement, ACityElement destinationElement, List<RoadNode> path) {
		this.startElement = startElement;
		this.destinationElement = destinationElement;
		
		this.path = path;
	}
	
	public void addRoadNodeToPath(RoadNode roadNode) {
		this.path.add(roadNode);
	}
	
	public void addRoadNodesToPath(List<RoadNode> roadNodes) {
		this.path.addAll(roadNodes);
	}
	
	public ACityElement getStartElement() {
		return this.startElement;
	}

	public ACityElement getDestinationElement() {
		return this.destinationElement;
	}

	public List<RoadNode> getPath() {
		return this.path;
	}
	
	public double calculateLength() {
		double pathLength = 0.0;
		
		for (int i = 0; i < this.path.size() - 1; i++) {
			pathLength += this.distance(this.path.get(i), this.path.get(i + 1));
		}
		
		return pathLength;
	}

	private double distance(RoadNode start, RoadNode destination) {

		// use Manhattan metric instead of Euclid metric due to performance
		return Math.abs(destination.getX() - start.getX() + destination.getY() - start.getY());
	}

}
