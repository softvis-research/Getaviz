package org.svis.generator.multisphere.m2m

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.graph.Model
import org.svis.xtext.graph.Node
import org.svis.xtext.graph.impl.GraphFactoryImpl
import org.svis.generator.WorkflowComponentWithConfig

class Graph2MultiSphereTD extends WorkflowComponentWithConfig {
	
	var Model graphRoot
	var myGraphFactory = new GraphFactoryImpl
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Graph2SphereLayout has started.")
		
		// get source model famix
		graphRoot = ctx.get("graph") as Model
		
		val nodes = graphRoot.eContents.filter(typeof(Node)).toList 
		
		
		//layout
		var graphRootSphere = myGraphFactory.createModel				
		
		if( nodes.size == 1 ){
			
			var rootNode = toSphereLayoutTD(nodes.get(0), 1000.0, 0.0, 0.0, 0.0)	
			graphRootSphere.elements.add(rootNode)
			
		} else {
			
			var dummyNode = myGraphFactory.createNode	
			dummyNode.setType("PACKAGE")
			dummyNode.setName("DUMMY")		
			dummyNode.childNodes.addAll(nodes)

			var rootNode = toSphereLayoutTD(dummyNode, 1000.0, 0.0, 0.0, 0.0)		
			graphRootSphere.elements.add(rootNode)
			
		}
						
		
		// set graphmodel to context 
		ctx.set("graphwriter", graphRootSphere.toResource)
		
		// set graphmodel to context 
		ctx.set("graph", graphRootSphere)
		
		
				
		log.info("Graph2SphereLayout has finished.")
	}
	
		
	
	
	
	
	
	
	
	
	
	//SphereLayout (fixed size of rootElement, scaling down for nested elements)
	//**************************************************************************
	
	
	def Node toSphereLayoutTD(Node node, Double radius, Double x, Double y, Double z){										
		
		var myNode = myGraphFactory.createNode
		
		myNode.id = node.id
						
		myNode.setXPosition(x)
		myNode.setYPosition(y)
		myNode.setZPosition(z)
						
		myNode.setXSize(radius)
		myNode.setYSize(radius)
		myNode.setZSize(radius)	
		
		myNode.setLabel(node.label)		
		myNode.setType(node.type)
		
		
		if( node.childNodes.size > 500 ){
			return myNode
		}
		
		
		//children?
		if( node.childNodes.size != 0 ){
			
			var myGeneratedSphere = new GeneratedSphere(node.childNodes.size, 1, x, y, z)			
			var childSize = myGeneratedSphere.surfacePointSize
					
			var innerSphereSize = 1 + ( childSize * 1 )		
						
			var innerSphereRadius = radius / innerSphereSize							
			
			var childRadius = innerSphereRadius * ( childSize / 1.5)
			
			
			myGeneratedSphere.setRadius(innerSphereRadius)
			
			
			
			
			if( node.type == "PACKAGE" ){ 				
				myNode.setXSize(childRadius * 0.1)
				myNode.setYSize(childRadius * 0.1)
				myNode.setZSize(childRadius * 0.1)
			} 
			
			if( node.type == "CLASS" ){
				myNode.setXSize(childRadius * 0.5)
				myNode.setYSize(childRadius * 0.5)
				myNode.setZSize(childRadius * 0.5)
				
				//myNode.setTransparency(0.8)				
			}
			
			var childNodeIndex = 0		
			for(childNode : node.childNodes){
				
				var mySurfacePoint =  myGeneratedSphere.surfacePoints.get(childNodeIndex)
				
				var myChildNode = toSphereLayoutTD(childNode, childRadius, mySurfacePoint.x, mySurfacePoint.y, mySurfacePoint.z)
				myNode.childNodes.add(myChildNode)
				
				childNodeIndex = childNodeIndex + 1
			}
			
		}
		
		return myNode
	}
	
	
	
	
	
	def Resource toResource(Model modelRoot) {
		var Resource resource = new ResourceImpl()
		resource.contents.add(modelRoot)

		return resource
	}	
	
	
	
}
