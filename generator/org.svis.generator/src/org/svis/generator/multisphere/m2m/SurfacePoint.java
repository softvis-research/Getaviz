package org.svis.generator.multisphere.m2m;

import java.util.HashMap;
import java.util.Map;

public class SurfacePoint {

	private double x;
	private double y;
	private double z;

	private double alpha;
	private double beta;

	
	private Sphere sphere;
	
	private Map<SurfacePoint, Double> otherSurfacePoints = new HashMap<SurfacePoint, Double>();

	
	public SurfacePoint(double x, double y, double z, Sphere sphere) {
		this.sphere =  sphere;
		
			
		this.x = x;
		this.y = y;
		this.z = z;
		
		
		alpha = Math.acos( y );		
		beta = Math.acos( z / Math.sin(alpha) );
		
		//check interval of beta, because Math.arcos only first possible angle		
		double checkX = Math.sin(alpha) * Math.sin(beta);
		if ( round( checkX, 4 ) != round( x, 4 ) && trunc( checkX, 4 ) != trunc( x, 4 )) {
			if( x > 0 ){
				System.out.println("ahhh");
			}
			beta = ( 2 * Math.PI ) - beta;	
		}
			
		
		calculatePoint();		
		
	}
	
	public SurfacePoint(double alpha, double beta, Sphere sphere) {
		this.sphere =  sphere;
		
		this.alpha = alpha;
		this.beta = beta;
		
		calculatePoint();
	}
	
	public void calculatePoint(){		

		z = Math.sin(alpha) * Math.cos(beta);
		x = Math.sin(alpha) * Math.sin(beta);
		y = Math.cos(alpha);
		
		z = z * sphere.getRadius();
		x = x * sphere.getRadius();
		y = y * sphere.getRadius();
		
		z = z + sphere.getZ();
		x = x + sphere.getX();
		y = y + sphere.getY();
	}
	



	public void addOtherSurfacePoint(SurfacePoint otherSurfacePoint) {
		otherSurfacePoints.put(otherSurfacePoint, 0.0);
	}

	
	
	public void calculateDistances() {

		Map<SurfacePoint, Double> calculatedSurfacePoints = new HashMap<SurfacePoint, Double>();

		for (SurfacePoint otherSurfacePoint : otherSurfacePoints.keySet()) {
			calculatedSurfacePoints.put(otherSurfacePoint,
					getDistance(this, otherSurfacePoint));
		}

		otherSurfacePoints = calculatedSurfacePoints;		
	}
	
	
	public double getDistance(SurfacePoint point1, SurfacePoint point2) {
		
		double relativeX1 = point1.getX() - sphere.getX();
		double relativeY1 = point1.getY() - sphere.getY();
		double relativeZ1 = point1.getZ() - sphere.getZ();
		
		double relativeX2 = point2.getX() - sphere.getX();
		double relativeY2 = point2.getY() - sphere.getY();
		double relativeZ2 = point2.getZ() - sphere.getZ();
		
		double scalar = relativeX1 * relativeX2 + relativeY1
				* relativeY2 + relativeZ1 * relativeZ2;

		double scalarradius = scalar / Math.pow(sphere.getRadius(), 2);

		double alpha = Math.acos(scalarradius);

		double distance = alpha * sphere.getRadius();

		return distance;
	}
	
	
	public double getMinimalDistance(){
		
		double minimum = 0.0;
		
		for (SurfacePoint otherSurfacePoint : otherSurfacePoints.keySet()) {									
			if ( otherSurfacePoints.get(otherSurfacePoint) < minimum || minimum == 0 ){
				minimum = otherSurfacePoints.get(otherSurfacePoint);
			}
		}
		
		return round(minimum, 4);
	}

	
	
	
	public boolean checkPoint() {

		double length = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
		length = Math.sqrt(length);
		
		double radius = sphere.getRadius();
		
		double nearValue = radius * 0.999999;

		if (length == radius) {
			return true;
		}
		if (length >= nearValue) {
			System.out.println("nearValue");
			return true;
		}

		return false;
	}
	
	
	
	private double round(final double value, final int frac) {
		return Math.round(Math.pow(10.0, frac) * value) / Math.pow(10.0, frac);
	}
	
	private double trunc(final double value, final int frac) {
		return Math.floor(Math.pow(10.0, frac) * value) / Math.pow(10.0, frac);
	}

	public double getX() {
		return round(x, 4);
	}

	public double getY() {
		return round(y, 4);
	}

	public double getZ() {
		return round(z, 4);
	}

	public double getAlpha() {
		return round(alpha, 4);
	}

	public double getBeta() {
		return round(beta, 4);
	}
	
	

}
