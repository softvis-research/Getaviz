package org.svis.generator.multisphere.m2m

import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.graph.Edge
import org.svis.xtext.graph.Model
import org.svis.xtext.graph.Node
import org.svis.xtext.graph.impl.GraphFactoryImpl
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.logging.LogFactory

class Graph2MultiSphere extends WorkflowComponentWithModelSlot {
	//val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var Model graphRoot
	var myGraphFactory = new GraphFactoryImpl
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Graph2SphereLayout has started.")
		
		// get source model famix
		graphRoot = ctx.get("graph") as Model
		
		// get nodes
		val nodes = graphRoot.eContents.filter(typeof(Node)).toList 		
				
		//layout	
		var graphRootSphere = toMultiSpherelayout(nodes)						
		
		// set graphmodel to context 
		ctx.set("graphwriter", graphRootSphere.toResource)
		
		// set graphmodel to context 
		ctx.set("graph", graphRootSphere)
				
		log.info("Graph2SphereLayout has finished.")
	}
	
	
		
	
	
	def Model toMultiSpherelayout(List<Node> nodes){
		
		var graphRootSphere = myGraphFactory.createModel				
		
		
		var dummyNode = myGraphFactory.createNode			
		dummyNode.childNodes.addAll(nodes)
		
		
		var packages = dummyNode.eAllContents.filter(typeof(Node)).filter[n | n.type == "PACKAGE"].toList			
		//var packagesWithClasses = packages.filter[n | n.childNodes.filter[c | c.type == "CLASS"].size != 0].toList			
		
		
		for(singlePackage : packages){
			
			var childrenPackage = singlePackage.childNodes.filter[c | c.type == 'PACKAGE'].toList()
			
			for(packageChild : childrenPackage){
				var packageEdge = myGraphFactory.createEdge
				packageEdge.setSourceNode(singlePackage)
				packageEdge.setTargetNode(packageChild)
				graphRootSphere.elements.add(packageEdge) 
			}
			
			var childrenNoPackage = singlePackage.childNodes.filter[c | c.type != 'PACKAGE'].toList()
			
			singlePackage.childNodes.removeAll(singlePackage.childNodes)
			singlePackage.childNodes.addAll(childrenNoPackage)
		}
		
		dummyNode = myGraphFactory.createNode	
		dummyNode.setType("PACKAGE")
		dummyNode.setName("DUMMY")		
		dummyNode.childNodes.addAll(packages)
		
		var rootNode = toSphereLayoutStandardBU(dummyNode, 1.5)		
		graphRootSphere.elements.add(rootNode)
		
		
		//Package Edges
		var layoutedNodes = rootNode.eAllContents.filter(typeof(Node)).toList()
		var packageEdges = graphRootSphere.elements.filter(typeof(Edge)).toList()
		
		for(edge : packageEdges ){
			var layoutedSourceNode = layoutedNodes.filter[n|n.id == edge.sourceNode.id].toList().get(0)			
			var layoutedTargetNode = layoutedNodes.filter[n|n.id == edge.targetNode.id].toList().get(0)
			
			edge.setSourceNode(layoutedSourceNode)
			edge.setTargetNode(layoutedTargetNode)			 
		}
		
		
		return graphRootSphere
	}	
	

	
	
	
	
	
	
	
	
	
	
	//SphereLayout Bottom Up (smallest Item = 1)
	//**************************************************************************
	
	def Node toSphereLayoutStandardBU(Node node, Double sizeFactor){		
		
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
		//resource.resourceSet.URIConverter
		return resource
	}	
	
}
