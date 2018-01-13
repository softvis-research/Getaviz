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

class Graph2MultiSphereBU extends WorkflowComponentWithConfig {
	
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
			
			var rootNode = toSphereLayoutBU(nodes.get(0), 1.1)	
			graphRootSphere.elements.add(rootNode)
			
		} else {
			
			var dummyNode = myGraphFactory.createNode	
			dummyNode.setType("PACKAGE")
			dummyNode.setName("DUMMY")		
			dummyNode.childNodes.addAll(nodes)

			var rootNode = toSphereLayoutBU(dummyNode, 1.1)		
			graphRootSphere.elements.add(rootNode)
			
		}
						
		
		// set graphmodel to context 
		ctx.set("graphwriter", graphRootSphere.toResource)
		
		// set graphmodel to context 
		ctx.set("graph", graphRootSphere)
		
		
				
		log.info("Graph2SphereLayout has finished.")
	}
	
	
		
	
	
	
	
	//SphereLayout Bottom Up (smallest Item = 1)
	//**************************************************************************
	
	def Node toSphereLayoutBU(Node node, Double sizeFactor){		
		
		return toSphereLayoutBUSize(node, sizeFactor).toSphereLayoutBUPosition	
	}
	
	
	def Node toSphereLayoutBUSize(Node node, Double sizeFactor){		
		
		var myNode = myGraphFactory.createNode
		
		var maxChildSize = 1.0
		
		if( node.childNodes.size != 0 && node.childNodes.size < 500){		
			
//			getMaxChildSize
			for(childNode : node.childNodes){
				
				var myChildNode = toSphereLayoutBUSize(childNode, sizeFactor)
				myNode.childNodes.add(myChildNode)
				
				var childSize = myChildNode.getXSize
																
				if( childSize > maxChildSize ){
					maxChildSize = childSize			
				}
			}
			
//			standardSphere
			var myGeneratedSphere = new GeneratedSphere(node.childNodes.size, 1, 0, 0, 0)			
			var surfacePointSize = myGeneratedSphere.surfacePointSize
			
			
			var surfacePointFactor = maxChildSize / surfacePointSize		
			
			maxChildSize = surfacePointFactor * sizeFactor
		} 
		
		
		
//		set fields
		myNode.setXSize(maxChildSize)
		myNode.setYSize(maxChildSize)
		myNode.setZSize(maxChildSize)	
		
		myNode.setLabel(node.label)		
		myNode.setType(node.type)
		
		myNode.id = node.id
				
		return myNode
	}
	
	
	
	
	def Node toSphereLayoutBUPosition(Node node){
		
		if( node.childNodes.size == 0 || node.childNodes.size > 500 ){
			return node
		}
		
		var myGeneratedSphere = new GeneratedSphere(node.childNodes.size, node.getXSize, node.getXPosition, node.getYPosition, node.getZPosition)
		
		var childNodeIndex = 0		
		for(childNode : node.childNodes){
			
			var mySurfacePoint =  myGeneratedSphere.surfacePoints.get(childNodeIndex)
			
			childNode.setXPosition(mySurfacePoint.x)
			childNode.setYPosition(mySurfacePoint.y)
			childNode.setZPosition(mySurfacePoint.z)
			
			toSphereLayoutBUPosition(childNode)					
			
			childNodeIndex = childNodeIndex + 1
		}		
		
		
		
//		customfields
		if( node.type == "PACKAGE" ){ 	
			
			node.setXSize(node.getXSize * 0.1)
			node.setYSize(node.getYSize * 0.1)
			node.setZSize(node.getZSize * 0.1)
						
			//node.setTransparency(1.0)		
		} 
		
		if( node.type == "CLASS" ){
			
//			node.setXSize(node.XSize * 0.5)
//			node.setYSize(node.YSize * 0.5)
//			node.setZSize(node.ZSize * 0.5)
			
//			node.setTransparency(0.8)				
		}					
		
		return node
	}	
	
	
	
	def Resource toResource(Model modelRoot) {
		var Resource resource = new ResourceImpl()
		resource.contents.add(modelRoot)
		
		return resource
	}
	
}
