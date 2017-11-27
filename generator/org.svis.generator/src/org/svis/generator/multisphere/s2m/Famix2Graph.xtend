package org.svis.generator.multisphere.s2m

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.famix.Document
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXParameter
import org.svis.xtext.famix.Root
import org.svis.xtext.graph.Model
import org.svis.xtext.graph.Node
import org.svis.xtext.graph.impl.GraphFactoryImpl
import org.svis.generator.WorkflowComponentWithConfig

class Famix2Graph extends WorkflowComponentWithConfig{
	
	val graphFactory = new GraphFactoryImpl()
	var Document famixDocument
	var Model graphRoot
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2Graph has started.")
		
		// get source model famix
		famixDocument = (ctx.get("famix") as Root).document

		
		// init target model node
		graphRoot = graphFactory.createModel
		
		 
		// process root-packages
		val namespaces = famixDocument.elements.filter(typeof(FAMIXNamespace)).filter[parentScope === null]
		namespaces.forEach[graphRoot.elements.add(toNode)]
		
				
		// set graphmodel to stats 
		ctx.set("graphstats", graphRoot.toResource)

		// set graphmodel to writer				
		val graphList = newArrayList
		graphList.add(graphRoot)
		ctx.set("graphwriter", graphList)
		
		// set graphmodel for layout 
		ctx.set("graph", graphRoot)
				
		log.info("Famix2Graph has finished.")
	}
	
	
	def Node toNode(FAMIXNamespace famixNamespace) {
		//log.info("Create Package")
		
		val node = graphFactory.createNode
		node.name = famixNamespace.fqn.replaceAll('\\.', '_')
		node.label = famixNamespace.value
		node.type = "PACKAGE"
		node.id = famixNamespace.id
		
		
		famixDocument.elements.filter(typeof(FAMIXNamespace)).
			filter[parentScope !== null].
			filter[parentScope.ref.equals(famixNamespace)].
			forEach[cp|node.childNodes.add(cp.toNode)]
		
		var packageNodes = node.childNodes.size
		
		famixDocument.elements.filter(typeof(FAMIXClass)).
			filter[container.ref.equals(famixNamespace)].
			forEach[cp|node.childNodes.add(cp.toClassNode)]		
			
		var classNodes = node.childNodes.size - packageNodes
		
		//Sonderfall nur ein Unterpaket -> zusammenziehen
		if( classNodes == 0 && packageNodes == 1){
			return node.childNodes.get(0)
		} else {
			return node
		}
	}
	
	def Node create node: graphFactory.createNode toClassNode(FAMIXClass famixClass) {
		//log.info("Create Class")
		node.name = famixClass.fqn.replaceAll('\\.', '_')
		node.label = famixClass.value
		node.type = "CLASS"
		node.id = famixClass.id
		
		famixDocument.elements.filter(typeof(FAMIXAttribute)).
			filter[parentType.ref.equals(famixClass)].
			forEach[att|node.childNodes.add(att.toAttributeNode)]
			
		famixDocument.elements.filter(typeof(FAMIXMethod)).
			filter[parentType.ref.equals(famixClass)].
			forEach[meth|node.childNodes.add(meth.toMethodNode)]		
	}
	
	
	def Node create node: graphFactory.createNode toAttributeNode(FAMIXAttribute famixAttribute) {
		//log.info("Create Attribute")
		node.name = famixAttribute.fqn.replaceAll('\\.', '_')
		node.label = famixAttribute.value
		node.type = "ATTRIBUTE"
		node.id = famixAttribute.id				
	}
	
	def Node create node: graphFactory.createNode toMethodNode(FAMIXMethod famixMethod) {
		//log.info("Create Method")
		
		val parameters = newArrayList
		for (p : famixDocument.elements.filter(typeof(FAMIXParameter)).filter[parentBehaviouralEntity.ref.equals(famixMethod)]) {
			parameters += p
		}
		
		node.name = famixMethod.fqn.replaceAll('\\.', '_')
		node.label = famixMethod.value
		node.type = "METHOD"
		node.id = famixMethod.id
	}
	
	def Resource toResource(Model modelRoot) {
		val Resource resource = new ResourceImpl()
		resource.contents.add(modelRoot)
		return resource
	}
}
