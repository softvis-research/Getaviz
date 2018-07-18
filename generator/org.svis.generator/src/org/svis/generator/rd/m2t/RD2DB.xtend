package org.svis.generator.rd.m2t

import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.emf.mwe.core.issues.Issues
import org.svis.xtext.rd.Root
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.ConstraintViolationException
import org.svis.lib.database.Database
import org.svis.lib.database.DBConnector
import java.util.ArrayList
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.svis.generator.SettingsConfiguration
import org.apache.commons.logging.LogFactory

class RD2DB extends WorkflowComponentWithModelSlot {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	var GraphDatabaseService graphDb
	var DBConnector dbconnector

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("RD2DB has started.")
		graphDb = Database::getInstance(config.repositoryOwner, config.repositoryName)
		dbconnector = new DBConnector(graphDb)
		val resource = ((ctx.get("rd") as ArrayList).get(0) as Root).document
		val rootDisks = resource.disks.filter[level == 1].toList
		val tx = graphDb.beginTx
		val configSnapshotId = ""//config.snapshotID
		try {
			dbconnector.createRDNode(rootDisks,configSnapshotId)
			try {
				rootDisks.forEach[dbconnector.addToDB(it)]
			} catch(ConstraintViolationException e) {
				log.info("Constraint Violation disk")
			}
			tx.success
		} catch (ConstraintViolationException e) {
			log.warn("Visualization already in Database. Skip it.")
		} finally {
			tx.close
		}
		log.info("RD2DB has finished.")
	}
}