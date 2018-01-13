package org.svis.app.analyzer.metrics.queries.rd

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.app.analyzer.metrics.queries.famix.FamixSystemQueries
import org.svis.lib.database.Database

class RDSystemQueries {
	var GraphDatabaseService graph
	var FamixSystemQueries famix
	
	new() {
		graph = Database::instance
		famix = new FamixSystemQueries() 
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
		famix = new FamixSystemQueries(graph)
	}
	
	/**
	 * Calculates the mean density of the RD visualization for every famix snapshot of the system
	 * Density is the occupied area of a visualization (net area / gross area).
	 *  
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean density (value)
	 */
	
	def density () {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN sum(d.grossArea * d.density)/sum(d.grossArea) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	
	/**
	 * Calculates mean data share for every package disk of the RD visualization for every famix snapshot of the system
	 * Data Share is the occupied space by attributes in relation to net area
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean data share (value)
	 */
	
//	def dataShare (String systemID) {
//		val result = graph.execute('''
//			MATCH (:SYSTEM {systemID: "«systemID»"})-[:HAS]->(f:FAMIX)-[:HAS*0..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
//			RETURN sum(d.dataArea*d.netArea) as result,f.commitOrder as order
//			ORDER BY f.commitOrder
//		''')
//		return result.parseAsDoubleMap(netArea(systemID))
//	}
	
	def dataShare () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK), (p)-[:VISUALIZED_BY]->(pd:DISK)
			RETURN (sum(d.dataArea*d.netArea))/pd.netArea as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates mean method share for every package disk of the RD visualization for every famix snapshot of the system
	 * Method share is the occupied space by methods in relation to net area
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean method share of the RD visualization of the famix snapshot
	 */
	
//	def methodShare (String systemID) {
//		val result = graph.execute('''
//			MATCH (:SYSTEM {systemID: "«systemID»"})-[:HAS]->(f:FAMIX)-[:HAS*0..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
//			RETURN sum(d.methodArea*d.netArea) as result, f.commitOrder as order
//			ORDER BY f.commitOrder
//		''')
//		return result.parseAsDoubleMap(netArea(systemID))
//	}
	
	def methodShare () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK), (p)-[:VISUALIZED_BY]->(pd:DISK)
			RETURN (sum(d.methodArea*d.netArea))/pd.netArea as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the net area of the RD visualization for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and net area (value)
	 */
	

	def netArea () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN sum(d.netArea) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the gross area of the RD visualization for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and gross area (value)
	 */
	
	def grossArea () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN sum(d.grossArea) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**	
	 * 	Fetches the thickness of the root package disk of the RD visualization for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and thickness of root package disks (value)
	 */
	
	def thicknessOfRootDisks () {
		val	result = graph.execute('''
				MATCH (f:FAMIX)-[:VISUALIZED_BY]->(rd:RD)
				RETURN rd.thicknessOfOuterRing as result, f.snapshotID as id
			''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the centroid of the RD visualization for every famix snapshot of the system
	 * 
	 * @see https://en.wikipedia.org/wiki/Centroid
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and the centroid of the RD visualization (value)
	 */
	 
	// FIXME		innere Klassen werden aktuell doppelt gewertet
	def centroid () {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE), (p)-[:HAS_STRUCTURE]->(s:FAMIXSTRUCTURE),(s)-[:VISUALIZED_BY]->(d:DISK)
			RETURN sum(d.x*d.netArea)/sum(d.netArea) as x, sum(d.y * d.netArea)/sum(d.netArea) as y, f.snapshotID as id
		''')
		return result.parsePoint
	}
	
	/**	
	 * Calculates the centroid of all attributes of the RD visualization for every famix snapshot of the system
	 *  
	 * @see https://en.wikipedia.org/wiki/Centroid
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and the centroid of all attributes of the RD visualization (value)
	 */
	 
	def dataCentroid () {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			WHERE d.dataArea > 0
			RETURN  sum(d.x*d.dataArea)/sum(d.dataArea) as x, sum(d.y * d.dataArea)/sum(d.dataArea) as y, f.snapshotID as id
		''')
		return result.parsePoint
	}
	
	/**
	 * Calculates the mean size of class disks (gross area) of the RD visualization for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean gross area of class disks (value)
	 */
	
	def avgClassDiskSize () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN avg(d.grossArea) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean number of class disk layers of packages for every famix snapshot of the system.
	 * If a package contains many classes, these classes are placed in several layers, from the inside to the outside.
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and the mean number of class disk layers (value)
	 */
	
	def avgNumberOfLayersOfClassDisks () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN avg(d.numberOfLayers) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates share of simple disks (only methods or only attributes) for every famix snapshot of the system.
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and proportion of simple class disks to all class disks (value) 
	 */
	
	def shareOfSimpleDisks () {
			val divisor = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)
			RETURN count(DISTINCT s) as result, f.snapshotID as id
		''')
		val dividend = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			WHERE d.methodArea = 0 OR d.dataArea = 0
			RETURN f.snapshotID as id, count(DISTINCT s) as result
		''')
		return dividend.parsePackage(divisor)
	}
	
	/**
	 * Calculates the mean package level of a RD visualization for every famix snapshot of the system.
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and mean package level (value)
	 */
	
	def avgPackageLevel () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN avg(d.level) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the max package level of a RD visualization for every famix snapshot of the system.
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and max package level (value)
	 */
	
	def maxPackageLevel () {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN max(d.level) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
}