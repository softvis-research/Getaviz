package org.svis.generator.multisphere.m2m

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.graph.Model
import org.svis.xtext.graph.Node
import org.svis.xtext.graph.impl.GraphFactoryImpl
import java.awt.geom.Rectangle2D
import org.abego.treelayout.util.DefaultTreeForTreeLayout
import org.abego.treelayout.util.DefaultConfiguration
import org.abego.treelayout.util.FixedNodeExtentProvider
import org.abego.treelayout.TreeLayout
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.logging.LogFactory

class Graph2MultiSphereRings extends WorkflowComponentWithModelSlot {
	//val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var Model graphRoot
	var myGraphFactory = new GraphFactoryImpl
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Graph2SphereLayout has started.")
		
		// get source model famix
		graphRoot = ctx.get("graph") as Model
		
		// get Nodes
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
		
		//use dummyNode 
		var Node dummyNode = myGraphFactory.createNode
		dummyNode.childNodes.addAll(nodes)		

		//define hierarchy 		
		var Map<Integer, List<Node>> hierarchyPackages = getHierachyPackages(dummyNode)				
				
		//all packages
		var packages = dummyNode.eAllContents.filter(typeof(Node)).filter[n | n.type == "PACKAGE"].toList			
				
		//create treeLayout for packages
		var packageTreeLayout = createTreeLayout(dummyNode)
		
				
		//create package relations		
		for(singlePackage : packages){			
			var childrenPackages = singlePackage.childNodes.filter[c | c.type == 'PACKAGE'].toList()
			
			for(packageChild : childrenPackages){
				var packageEdge = myGraphFactory.createEdge
				packageEdge.setSourceNode(singlePackage)
				packageEdge.setTargetNode(packageChild)
				graphRootSphere.elements.add(packageEdge) 
			}			
		}
							
		
//		remove contained packages	
		for(singlePackage : packages){	
			var childrenNoPackage = singlePackage.childNodes.filter[c | c.type != 'PACKAGE'].toList()
			
			singlePackage.childNodes.removeAll(singlePackage.childNodes)
			singlePackage.childNodes.addAll(childrenNoPackage)	
		}
		
		
//		layout a sphere for every package
		for(singlePackage : packages){	
			
			var packageSphere = singlePackage.toSphereLayoutBU(1.5)					
			
			graphRootSphere.elements.add(packageSphere)
		}
	
		//compute surrounding sphere		
		var maxPackageSize = nodes.getMaxSize
		
		var biggestLayer = 0.0
		var biggestLayerSize = 0.0
		var layerSize = 0.0
		
		
		//get biggest x and y
		for( nodeBound : packageTreeLayout.values){
			if( nodeBound.x > biggestLayerSize){
				biggestLayerSize = nodeBound.x
				biggestLayer = nodeBound.y
			}
			if( nodeBound.y > layerSize){
				layerSize = nodeBound.y
			}
		}
		
//		//Get biggest Layer and the biggest Layer Size
//		for( layer : hierarchyPackages.keySet()){
//			
//			var packagesOfLayer = hierarchyPackages.get(layer)
//			
//			if( packagesOfLayer.size > biggestLayerSize){
//				biggestLayerSize = packagesOfLayer.size
//				biggestLayer = layer
//			}
//			
//			if( layer > layerSize ){
//				layerSize = layer
//			}
//		}
		
		//Compute surrounding Sphere Radius
		var angleSteps = Math.PI / ( layerSize + 1 )	
		
		var referenceRingAngle  = angleSteps * biggestLayer		
		
		var referenceRingRadius = ( biggestLayerSize * maxPackageSize * 3 ) / (2 * Math.PI )
		
		var surroundingSphereRadius = referenceRingRadius / Math.sin(referenceRingAngle)
		
		
		
		//create surrounding sphere by tree layout
		var angleBetaStep = 2 * Math.PI / ( biggestLayerSize + 1 )
		
		for( singlePackage : packageTreeLayout.keySet()){
			
			var nodeBounds = packageTreeLayout.get(singlePackage)
			
			var angleAlpha 	= angleSteps 	* nodeBounds.y
			var angleBeta 	= angleBetaStep * nodeBounds.x
			
			var zPosition = surroundingSphereRadius * Math.sin(angleAlpha) * Math.cos(angleBeta)
			var xPosition = surroundingSphereRadius * Math.sin(angleAlpha) * Math.sin(angleBeta)				
			var yPosition = surroundingSphereRadius * Math.cos(angleAlpha)
			
			singlePackage.moveNode(xPosition, yPosition, zPosition)		
			
		}
		

		
		
		//Add head package dummy		
		var headPackage = myGraphFactory.createNode
		
		headPackage.setType("PACKAGE")
		
		headPackage.setXSize(maxPackageSize)
		headPackage.setYSize(maxPackageSize)
		headPackage.setZSize(maxPackageSize)
		
		headPackage.setXPosition(0)
		headPackage.setYPosition(surroundingSphereRadius)
		headPackage.setZPosition(0)
		
		graphRootSphere.elements.add(headPackage)
		
		
		//Create hierarchy Edges
		for( singlePackage : hierarchyPackages.get(1) ){
			var packageEdge = myGraphFactory.createEdge
			packageEdge.setSourceNode(headPackage)
			packageEdge.setTargetNode(singlePackage)
			graphRootSphere.elements.add(packageEdge) 
		}
	
		
		graphRootSphere.setCustomFields
		
		return graphRootSphere
	}	
	
	
	
	
	
	def Map<Node, Rectangle2D.Double> createTreeLayout(Node dummyNode){
		
		//create Tree
		var DefaultTreeForTreeLayout<Node> tree = createNodeTree(dummyNode)
		
		//create TreeLayout		
		var TreeLayout<Node> myTreeLayout = new TreeLayout<Node>(tree, new FixedNodeExtentProvider<Node>(), new DefaultConfiguration<Node>(1, 1))
		
		//compute TreeLayout 
		var Map<Node, Rectangle2D.Double> nodeBounds = myTreeLayout.getNodeBounds()		 	
		
		return nodeBounds
	}
	
	def DefaultTreeForTreeLayout<Node> createNodeTree(Node dummyNode){
		var DefaultTreeForTreeLayout<Node> tree =
				 new DefaultTreeForTreeLayout<Node>(dummyNode);		
		
		var packages = dummyNode.childNodes.filter[c | c.type == 'PACKAGE'].toList()
		
		for(singlePackage : packages){			
			tree.addChild(dummyNode, singlePackage);
			createNodeTree(tree, singlePackage)
		}		
		
		return tree
	}
	
	def void createNodeTree(DefaultTreeForTreeLayout<Node> tree, Node parentPackage){		
		
		var childrenPackages = parentPackage.childNodes.filter[c | c.type == 'PACKAGE'].toList()
		
		for(singlePackage : childrenPackages){			
			tree.addChild(parentPackage, singlePackage);
			createNodeTree(tree, singlePackage)
		}	
	}
	
	
	
	def Map<Integer, List<Node>> getHierachyPackages(Node dummyNode){
		var Map<Integer, List<Node>> hierarchyPackages = new HashMap<Integer, List<Node>>
		
		var rootPackages = dummyNode.childNodes.filter(typeof(Node)).filter[n | n.type == "PACKAGE"].toList
		
		hierarchyPackages.getHierachyPackages(rootPackages, 1)		
		
		return hierarchyPackages
	}	
	
	def void getHierachyPackages(Map<Integer, List<Node>> hierarchyPackages, List<Node> packages, int hierarchyDepth){
		
		hierarchyPackages.put(hierarchyDepth, packages)		
		
		var childPackages = new ArrayList<Node>
		for(singlePackage : packages){
			var singleChildPackages = singlePackage.childNodes.filter[n | n.type == "PACKAGE"].toList
			childPackages.addAll(singleChildPackages)
		}
		
		if( childPackages.size == 0){
			return
		}
		
		hierarchyPackages.getHierachyPackages(childPackages, hierarchyDepth + 1)			
	}
	
	
	
	def void moveNode(Node node, double xPosition, double yPosition, double zPosition){
		
		node.setXPosition(node.XPosition + xPosition)
		node.setYPosition(node.YPosition + yPosition)
		node.setZPosition(node.ZPosition + zPosition)
		
		node.childNodes.forEach[child | child.moveNode(xPosition, yPosition, zPosition)]		
	}
	
	
	def void setCustomFields(Model graphRootSphere){
		
		var packages = graphRootSphere.elements.filter(typeof(Node)).filter[c | c.type == 'PACKAGE'].toList()
			
		for( singlePaackage : packages){
			
			if( singlePaackage.childNodes.size != 0){
			
				singlePaackage.setXSize(singlePaackage.getXSize * 0.1)
				singlePaackage.setYSize(singlePaackage.getYSize * 0.1)
				singlePaackage.setZSize(singlePaackage.getZSize * 0.1)
				
			}		
	
		}		
	}
	
	
	
	
	
	
	def Double getMaxSize(List<Node> nodes){
		var maxSize = 0.0
		
		for(node : nodes){
			var nodeSize = node.maxSize
			
			if(nodeSize > maxSize){
				maxSize = nodeSize
			} 
		}
		
		return maxSize
	}
	
	def Double getMaxSize(Node node){
				
		var maxSize = 0.0
		
		var xSize = Math.abs(node.XSize) + Math.abs(node.XPosition)	
		if(xSize > maxSize){
			maxSize = xSize
		}
		
		var ySize = Math.abs(node.YSize) + Math.abs(node.YPosition)	
		if(ySize > maxSize){
			maxSize = ySize
		}
		
		var zSize = Math.abs(node.ZSize) + Math.abs(node.ZPosition)
		if(zSize > maxSize){
			maxSize = zSize
		}	
		
		for(childNodes : node.childNodes){
			var childSize = childNodes.getMaxSize
			
			if(childSize > maxSize){
				maxSize = childSize
			} 
		}		
		
		return maxSize
	}
	
	
	
	
	
	
	
	
	
	//SphereLayout Bottom Up (smallest Item = 1)
	//**************************************************************************
	
	def Node toSphereLayoutBU(Node node, Double sizeFactor){		
		
		return toSphereLayoutBUSize(node, sizeFactor).toSphereLayoutBUPosition	
	}
	
	
	def Node toSphereLayoutBUSize(Node node, Double sizeFactor){		
		
		var maxChildSize = 1.0
		
		if( node.childNodes.size != 0 && node.childNodes.size < 500){		
			
//			getMaxChildSize
			for(childNode : node.childNodes){
				
				childNode.toSphereLayoutBUSize(sizeFactor)				
				
				var childSize = childNode.getXSize
																
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
		node.setXSize(maxChildSize)
		node.setYSize(maxChildSize)
		node.setZSize(maxChildSize)	
		
		node.setLabel(node.label)		
		node.setType(node.type)
		
		//node.setId(node.name)
		
		return node
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
				
		
		return node
	}
	
	
	
	
	def Resource toResource(Model modelRoot) {
		var Resource resource = new ResourceImpl()
		resource.contents.add(modelRoot)
		//resource.resourceSet.URIConverter
		return resource
	}	
	
}
