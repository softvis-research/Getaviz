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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int hashCode = 1;
		
		long xAsBits = Double.doubleToLongBits(this.x);
		int hashOfX = (int)(xAsBits ^ (xAsBits >>> 32));
				
		hashCode = prime * hashCode + hashOfX;
		
		long yAsBits = Double.doubleToLongBits(this.y);
		int hashOfY = (int)(yAsBits ^ (yAsBits >>> 32));
				
		hashCode = prime * hashCode + hashOfY;
		
		return hashCode;
	}

}
