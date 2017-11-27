package org.svis.generator.multisphere.m2m;

import org.svis.generator.multisphere.m2m.DeterministicSphere;

public class SphereGenerator {

	
	public static Sphere generateSphere(int pointAmount){
		
				
		//DeterministicSpheres
		Sphere bestSphere = new DeterministicSphere(pointAmount, 1, 0, 0, 0, false);	
		double bestPointSize = bestSphere.getSurfacePointSize();
			
	    
	    //Random Spheres		
		if( pointAmount < 24 || bestPointSize < 0.01){ 		
		    for( int i = 0; i  < 10000; ++i) {
		    	
		    	Sphere mySphere = new RandomSphere(pointAmount, 1, 0, 0, 0, false);	   		    	
		    	double minimalPointSize = mySphere.getSurfacePointSize();
		    	
		    	if (minimalPointSize >= bestPointSize){
		    		bestPointSize = minimalPointSize;
		    		bestSphere = mySphere;
		    	}		    	
		    }   	    
		}    
		
		
		return bestSphere;
	}
	
}
