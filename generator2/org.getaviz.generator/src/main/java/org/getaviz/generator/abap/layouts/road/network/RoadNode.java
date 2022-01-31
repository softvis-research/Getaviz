package org.getaviz.generator.abap.layouts.road.network;

public class RoadNode {
	
	private double x;
	private double y;
	
	public String label;
	
	public RoadNode(double x, double y) {
		this.setX(x);
		this.setY(y);
	}
	
	public RoadNode(double x, double y, String label) {
		this.setX(x);
		this.setY(y);
		this.label = label;
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
		int result = 1;
		
		long lX = Double.doubleToLongBits(this.x);
		int hashX = (int)(lX ^ (lX >>> 32));
				
		result = prime * result + hashX;
		
		long lY = Double.doubleToLongBits(this.y);
		int hashY = (int)(lY ^ (lY >>> 32));
				
		result = prime * result + hashY;
		
		return result;
	}

}
