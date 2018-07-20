package org.svis.generator.city.m2t

import org.svis.lib.database.Database 
import org.svis.xtext.city.District
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.emf.mwe.core.issues.Issues
import org.neo4j.graphdb.GraphDatabaseService
import org.svis.xtext.city.Root
import org.neo4j.graphdb.ConstraintViolationException
import org.svis.lib.database.DBConnector
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.logging.LogFactory
import org.svis.generator.SettingsConfiguration

class City2DB extends WorkflowComponentWithModelSlot {
	
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var GraphDatabaseService graphDb
	var DBConnector dbconnector

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("City2DB has started.")
		graphDb = Database::getInstance(config.repositoryOwner, config.repositoryName)
		dbconnector = new DBConnector(graphDb)
		val resource = (ctx.get("CITYv2writer") as Root).document
		val tx = graphDb.beginTx
		val configSnapshotId = ""//config.snapshotID
		try {
			dbconnector.createCityNode(configSnapshotId)
			resource.entities.filter(District).filter[level == 1].forEach[dbconnector.addToDB(it)] 
			tx.success
		} catch (ConstraintViolationException e) {
			log.warn("Visualization already in Database. Skip it.")
		} finally {
			tx.close
		}
		log.info("City2DB has finished.")
	}
}