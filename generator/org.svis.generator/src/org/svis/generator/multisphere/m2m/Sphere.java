package org.svis.generator.multisphere.m2m;

import java.util.ArrayList;
import java.util.List;

public abstract class Sphere {

	protected List<SurfacePoint> surfacePoints = new ArrayList<SurfacePoint>();
	
	private boolean logOn = false; 

	private double radius;

	private double x;
	private double y;
	private double z;
	
	private double surfacePointSize;

	
	
	
	
	
	public Sphere(int pointAmount, double radius, double x, double y, double z) {
		constructor(pointAmount,radius, x, y, z);
	}
	
	public Sphere(int pointAmount, double radius, double x, double y, double z, boolean logOn) {
		this.logOn = logOn; 
		
		constructor(pointAmount,radius, x, y, z);
	}
	
	private void constructor(int pointAmount, double radius, double x, double y, double z){
		this.radius = radius;

		this.x = x;
		this.y = y;
		this.z = z;
		
		createPoints(pointAmount);
		
		linkPointToPoints();
		
		checkPoints();
	}
	
	
	
	
	protected abstract void createPoints(int pointAmount);

	
	
	protected void linkPointToPoints(){
		// link points to points
		for (SurfacePoint surfacePoint : surfacePoints) {
			for (SurfacePoint otherSurfacePoint : surfacePoints) {
				if (otherSurfacePoint != surfacePoint) {
					surfacePoint.addOtherSurfacePoint(otherSurfacePoint);
				}
			}
		}
	}
	
	
	protected void checkPoints(){
		
		if( logOn ){		
			for (SurfacePoint surfacePoint : surfacePoints) {
				if (!surfacePoint.checkPoint()) {
					System.out.println("Fehler");
					System.out.println(surfacePoint.getAlpha());
					System.out.println(surfacePoint.getBeta());
					System.out.println(surfacePoint.getX());
					System.out.println(surfacePoint.getY());
					System.out.println(surfacePoint.getZ());
					System.out.println(Math.sin(Math.PI));
					System.out.println(Math.cos(Math.PI));
				}
			}
			for (SurfacePoint surfacePoint : surfacePoints) {
				System.out
						.println("(" + surfacePoint.getX() + ") - ("
								+ surfacePoint.getY() + ") - ("
								+ surfacePoint.getZ() + ")");
			}
		}
		
	}
	
	
	protected double round(final double value, final int frac) {
		return Math.round(Math.pow(10.0, frac) * value) / Math.pow(10.0, frac);
	}
	
	
	
	
	private void calculateSurfacePointSize(){
				
		surfacePointSize = round( radius * 0.9 , 4);	
		
		if( surfacePoints == null || surfacePoints.size() == 1){
			return;
		}
		
		double minimum = 0;					
		for (SurfacePoint surfacePoint : surfacePoints) {
			
			surfacePoint.calculateDistances();
			
			double surfacePointMinimum = surfacePoint.getMinimalDistance();
									
			if( surfacePointMinimum < minimum || minimum == 0){
				minimum = surfacePointMinimum;
			}
		}
		
		double minimumSize = round( minimum / 2, 4 );				
		
		if(minimumSize < surfacePointSize) {
			surfacePointSize = minimumSize;
		} 
	}
	

	public double getSurfaceArea() {
		return 4 * Math.PI * radius;
	}
		

	public List<SurfacePoint> getSurfacePoints() {
		return surfacePoints;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;

		for (SurfacePoint surfacePoint : surfacePoints) {
			surfacePoint.calculatePoint();
		}

	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
		
		for (SurfacePoint surfacePoint : surfacePoints) {
			surfacePoint.calculatePoint();
		}
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
		
		for (SurfacePoint surfacePoint : surfacePoints) {
			surfacePoint.calculatePoint();
		}
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
		
		for (SurfacePoint surfacePoint : surfacePoints) {
			surfacePoint.calculatePoint();
		}
	}

	public double getSurfacePointSize() {
		if( surfacePointSize == 0 ){
			calculateSurfacePointSize();
		}
		
		return surfacePointSize;
	}
	
	

}
