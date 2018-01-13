package org.svis.generator.multisphere.m2t

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.IGenerator2
import org.apache.commons.logging.LogFactory
import java.util.List
import org.svis.xtext.graph.Node
import org.svis.xtext.graph.Model
import org.svis.xtext.graph.Edge

class MultiSphere2X3D implements IGenerator2 {
	
	val log = LogFactory::getLog(class)
	
	var Model graphRoot
	
	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("MultiSphere2X3D has started.")
	}
	
	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("MultiSphere2X3D has finished.")
	}
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		// get source model famix
		graphRoot = resource.contents.head as Model
		
		val nodes = graphRoot.eAllContents.filter(typeof(Node)).toList
		val edges = graphRoot.eAllContents.filter(typeof(Edge)).toList
		
		val minimalSize = getMinimalSize(nodes)
		val sizeScale = 1 / minimalSize
		
		val maximalPosition = getMaximalPosition(nodes)
		
		fsa.generateFile("model.x3d", toX3DHead(sizeScale) + toNodeList(nodes).toString + toEdgeList(nodes, edges) + toX3DTail(maximalPosition) )	
//		fsa.generateFile("model.x3d", toX3DHead(sizeScale) + toNodeList(nodes).toString + toChildEdgesX3Dom(nodes) + toX3DTail(maximalPosition) )	
//		fsa.generateFile("model.x3d", toX3DHead(sizeScale) + toNodeList(nodes).toString + toX3DTail(maximalPosition) )		
	}
	
	def getMinimalSize(List<Node> nodes){
		
		var minimalSize = 0.0
				
		for(node : nodes){			 
									
			if( node.getXSize < minimalSize  || minimalSize == 0.0 ){				
				minimalSize = node.getXSize
			}			
			
		}
		
		return minimalSize		
	} 
	
	def getMaximalPosition(List<Node> nodes){
		
		var maximalPosition = 0.0
		for(node : nodes){			 
			
			if( Math.abs(node.getXPosition) > maximalPosition ){
				maximalPosition = Math.abs(node.getXPosition)
			}
			if( Math.abs(node.getYPosition) > maximalPosition ){
				maximalPosition = Math.abs(node.getYPosition)
			}
			if( Math.abs(node.getZPosition) > maximalPosition ){
				maximalPosition = Math.abs(node.getZPosition)
			}			
		}
		
		return maximalPosition
	}
	
	
	
	
	def toX3DHead(Double sizeScale)
	'''
	<?xml version="1.0" encoding="UTF-8"?>
	<!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 3.3//EN" "http://www.web3d.org/specifications/x3d-3.3.dtd">
	<X3D profile='Immersive' version='3.3' xmlns:xsd='http://www.w3.org/2001/XMLSchema-instance' xsd:noNamespaceSchemaLocation='http://www.web3d.org/specifications/x3d-3.3.xsd'>
	    <head>
	        <meta content='sphere.x3d' name='title'/>
	        <meta content='SVIS-Generator' name='creator'/>
	    </head>	      
	    
	    <Scene>
	    	<Transform scale = '«sizeScale» «sizeScale» «sizeScale» ' >   			    	
	      
	'''

	def toX3DTail(Double sizeScale)
	'''	
			<Viewpoint DEF='Initial' description='Overview' position='0 0 « sizeScale * 10 » '/>
			
			</Transform>	
			<Background DEF='_Background' groundColor='1.0000000 1.0000000 1.0000000' skyColor='1.0000000 1.0000000 1.0000000'/>
			
			   	</Scene>
			</X3D>
	'''
	
	

	
	def toNodeList(List<Node> nodes) 
	'''
	«FOR node: nodes»
		<Transform DEF='«node.id»' translation='« node.getXPosition + " " + node.getYPosition + " " + node.getZPosition »' scale ='« node.getXSize + " " + node.getYSize + " " + node.getZSize »'>
							
			<Shape>
				<Appearance>
					<Material diffuseColor='«getColor(node)»' transparency='«getTransparency(node)»'/>
				</Appearance>		
				«getShape(node)»				             
			</Shape>
			
			«/*writeBillboard(node)*/»								
				
		</Transform>	
	«ENDFOR»
	'''
	
	
		
	def writeBillboard(Node node)
	'''
		<Transform translation='0 1 0' scale ='« node.getXSize / 2 + " " + node.getYSize / 2 + " " + node.getZSize / 2 »'>
		<Billboard>
				<Shape>
					<Appearance>
						<Material diffuseColor='0 1 0'/>
					</Appearance>
						<Text string="«node.label»">
							<FontStyle justify='"MIDDLE"'/>
						</Text>
				</Shape>
		</Billboard>
		</Transform>
	'''
	
	
	
	
	
	
	def toEdgeList(List<Node> nodes, List<Edge> edges)
	'''
		«FOR edge: edges»				
			
				«var radius = getEdgeSize(edge.sourceNode, edge.targetNode)»
				
				«createEdge(edge.sourceNode, edge.targetNode, radius)»		
			
		«ENDFOR»
	'''
	
	
	
	
	
	
	
	def drawEdge(
		Double translationXPosition, Double translationYPosition, Double translationZPosition, 
		Double scaleX, Double scaleY, Double scaleZ,
		Double rotationX, Double rotationY, Double rotationZ, Double rotationALPHA,
		Double radius
	)
	'''	
	    <Transform translation='«translationXPosition» «translationYPosition» «translationZPosition»' scale='«scaleX» «scaleY» «scaleZ»' rotation='«rotationX» «rotationY» «rotationZ» «rotationALPHA»'>      
	          <Shape>
	            <Appearance>
	              <Material transparency="0.0" diffuseColor='1 0 0' />	              
	            </Appearance>
	            <Cylinder height="1" radius ='«radius»' />
	          </Shape>		         
	    </Transform>
	'''
	
	
	def String createEdge(Node source, Node target, Double radius){
	
		var betrag = Math.sqrt( 
			Math.pow(
				target.getXPosition - source.getXPosition, 2
			) + 
			Math.pow(
				target.getYPosition - source.getYPosition, 2
			) + 
			Math.pow(
				target.getZPosition - source.getZPosition, 2
			)
		)		  	
	  	
	  	var translationXPosition = source.getXPosition + (target.getXPosition - source.getXPosition) / 2.0
	  	var translationYPosition = source.getYPosition + (target.getYPosition - source.getYPosition) / 2.0
	  	var translationZPosition = source.getZPosition + (target.getZPosition - source.getZPosition) / 2.0
	  	
	  	var scaleX = 1.0
		var scaleY = betrag
		var scaleZ = 1.0
		
		var rotationX = (target.getZPosition-source.getZPosition);
		var rotationY = 0.0;
		var rotationZ = (-1.0)*(target.getXPosition-source.getXPosition);
		var rotationALPHA = Math.acos(
			(target.getYPosition - source.getYPosition) /
			(Math.sqrt( 
				Math.pow(
					target.getXPosition - source.getXPosition, 2
				) + 
				Math.pow(
					target.getYPosition - source.getYPosition, 2
				) + 
				Math.pow(
					target.getZPosition - source.getZPosition, 2
				)
			))
		)
				
		return drawEdge(
			 translationXPosition,  translationYPosition,  translationZPosition, 
			 scaleX, scaleY, scaleZ,
			 rotationX,  rotationY,  rotationZ,  rotationALPHA,
			 radius  	
		).toString  			  		
	}		
	
	
	def Double getEdgeSize(Node source, Node target){		
		if( source.getXSize > target.getXSize){
			return target.getXSize * 0.5
		} else {
			return source.getXSize * 0.5
		}
	}		  
	
	
	
	
//
//	def Double getEdgeSize(Node root, List<Node> childNodes){
//		var minimalSize = root.XSize
//		
//		for(childNode : childNodes){
//			if( childNode.XSize < minimalSize){
//				minimalSize = childNode.XSize
//			}
//		}
//		
//		return minimalSize * 0.25
//	}		   
//
//	def toChildEdgesX3Dom(List<Node> nodes)
//	'''
//		«FOR node: nodes»
//			«IF node.type == "PACKAGE"»
//				«var radius = getEdgeSize(node, node.childNodes)»
//				
//				«FOR childNode : node.childNodes»	
//					«createEdge(node, childNode, radius)»			
//				«ENDFOR»
//			«ENDIF»
//		«ENDFOR»
//	'''
//	
//	
		
		
		
		
		
		
		
		
		
		
	def getShape(Node node){		
		if( node.type == "CLASS" || node.type == "PACKAGE" ){
			return "<Sphere/>"
		} else {
			return "<Box/>"
		}
	}
	
	
	
	
	def getTransparency(Node node)
	'''	«getTransparencyString(node)» '''
	
	
	def getTransparencyString(Node node){
		return node.transparency.toString		
	}
	
	
	def getColor(Node node)	
	'''«getColorString(node)»'''
	
	def getColorString(Node node){
				
		
		if(node.type == "CLASS"){
			return "1.0 1.0 0.0"
		}
			
		if(node.type == "ATTRIBUTE" ){
			return "0.0 1.0 1.0"
		}
		
		if(node.type == "METHOD" ){
			return "0.0 1.0 0.0"
		}
		
		if(node.type == "PACKAGE"){								
			return "0.0 0.0 1.0"			
		}
		
		return "1.0 1.0 1.0"
		
	}
	
}