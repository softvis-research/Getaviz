package org.svis.generator.multisphere.m2m;

public class RandomSphere extends Sphere {

	public RandomSphere(int pointAmount, double radius, double x, double y, double z) {
		super(pointAmount,radius, x, y, z);
	}
	
	public RandomSphere(int pointAmount, double radius, double x, double y, double z, boolean logOn) {
		super(pointAmount,radius, x, y, z, logOn);
	}

	
	protected void createPoints(int pointAmount) {
		
					
		//create complete random surfacePoints 
//		completeRandom(pointAmount);
		
		//create points by approximation of cylinder
		randomByCylinder(pointAmount);
	}
	
	
	
	
	
	protected void completeRandom(int pointAmount) {
		
					
		//create random surfacePoints 
		for(int j=0; j<pointAmount; j++){
			
			double alpha = Math.random() * Math.PI;
			double beta = Math.random() * Math.PI * 2;
			
			surfacePoints.add(new SurfacePoint(alpha, beta, this));
							
		}		
		
	}
	
	
	protected void randomByCylinder(int pointAmount) {
		//http://de.sci.mathematik.narkive.com/6b5o9CPy/gleichverteilung-auf-kugeloberflache
		
		//create random surfacePoints		
		for(int j=0; j<pointAmount; j++){
			
			double phi = 2 * Math.PI * Math.random();
			double h = ( 2 * Math.random() ) - 1;
			double c = Math.sqrt( 1  - ( h * h ) );
			
			double x = c * Math.cos(phi);
			double y = c * Math.sin(phi);
			double z = h;			
			
			
			surfacePoints.add(new SurfacePoint(x, y, z, this));
							
		}		
		
	}
}
