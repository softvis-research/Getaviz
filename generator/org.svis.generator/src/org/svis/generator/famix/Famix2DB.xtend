package org.svis.generator.famix

import org.neo4j.graphdb.Node
import org.svis.xtext.famix.FAMIXNamespace
import java.util.List
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXEnumValue
import org.neo4j.graphdb.ConstraintViolationException
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.emf.mwe.core.issues.Issues
import org.svis.xtext.famix.Root
import org.svis.xtext.famix.FAMIXInvocation
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXAccess
import org.svis.lib.database.Database
import org.neo4j.graphdb.GraphDatabaseService
import java.util.Map
import org.svis.lib.database.DBConnector
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.svis.generator.SettingsConfiguration
import org.apache.commons.logging.LogFactory

class Famix2DB extends WorkflowComponentWithModelSlot  {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var GraphDatabaseService graphDb
	var DBConnector dbconnector 
	var Node rootNode
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXNamespace> subPackages = newArrayList
	val List<FAMIXEnumValue> enumValues = newArrayList
	val List<FAMIXMethod> methods = newArrayList
	val List<FAMIXAttribute> attributes = newArrayList
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2DB has started.")
		val resource = (ctx.get("famix") as Root).document
		graphDb = Database::getInstance(config.repositoryOwner, config.repositoryName)
		dbconnector = new DBConnector(graphDb)
		val rootPackages = resource.elements.filter(FAMIXNamespace).filter[parentScope === null]
		subPackages += resource.elements.filter(FAMIXNamespace).filter[parentScope !== null]
		structures 	+= resource.elements.filter(FAMIXStructure)
		methods 	+= resource.elements.filter(FAMIXMethod)
		attributes 	+= resource.elements.filter(FAMIXAttribute)
		enumValues 	+= resource.elements.filter(FAMIXEnumValue)
		val tx = graphDb.beginTx
		val nodeProperties = fillNodeProperties
		try {
			try{
				rootNode = dbconnector.createFamixNode(nodeProperties)
			} catch(ConstraintViolationException e) {
				log.warn("Commit already in database. Skip it.")
			throw e
			}
			rootPackages.forEach[toDB(it,rootNode)]
			
			resource.elements.filter(FAMIXInvocation).forEach[invocation|
				dbconnector.addToDB(invocation)
			]
			resource.elements.filter(FAMIXInheritance).forEach[inheritance|
				dbconnector.addToDB(inheritance)
			]
			resource.elements.filter(FAMIXAccess).forEach[access|
				dbconnector.addToDB(access)
			]
			tx.success
		} catch(ConstraintViolationException e) {
			log.info("Constrain Violation Famix2DB")
			print(e)
		} finally {
			tx.close 
		}
		log.info("Famix2DB has finished.")
	}
	
	def private fillNodeProperties() {
		val Map<String, String> nodeProperties = newHashMap
		nodeProperties.put("repositoryName", config.repositoryName)
 	    nodeProperties.put("repositoryOwner", config.repositoryOwner)
 		//nodeProperties.put("systemId", config.systemID)
 		//nodeProperties.put("repositoryUrl", config.repositoryUrl)
 		//nodeProperties.put("language", config.language)
 		//nodeProperties.put("commitOrder", config.commitOrder.toString)
 		//nodeProperties.put("snapshotId", config.snapshotID)
 		//nodeProperties.put("commit" , config.commit)
 		return nodeProperties
	}
	
	def private void toDB(FAMIXNamespace namespace, Node parent) {
		try {
			val node = dbconnector.addToDB(namespace,parent)
			structures.filter[container.ref.equals(namespace)].forEach[toDB(node)]
			subPackages.filter[parentScope.ref.equals(namespace)].forEach[toDB(node)]
		} catch (ConstraintViolationException e) {
		}
	}
	
	def private void toDB(FAMIXStructure structure, Node parent) {
		try {
			val node = dbconnector.addToDB(structure, parent)
			structures.filter[container.ref.equals(structure)].forEach[toDB(node)]
			methods.filter[parentType.ref.equals(structure)].forEach[dbconnector.addToDB(it,node)]
			attributes.filter[parentType.ref.equals(structure)].forEach[dbconnector.addToDB(it,node)]
			enumValues.filter[parentEnum.ref.equals(structure)].forEach[dbconnector.addToDB(it,node)]
		} catch (ConstraintViolationException e) {
		}
	}
}