package org.getaviz.generator.abap.layouts.road.network;

public class RoadNode {
	
	private double x;
	private double y;
	
	public RoadNode(double x, double y) {
		this.setX(x);
		this.setY(y);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (o == null || this.getClass() != o.getClass())
			return false;
		
		RoadNode otherNode = (RoadNode) o;
		
		return (this.x == otherNode.getX()) && (this.y == otherNode.getY());
	}

}
