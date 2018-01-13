package org.svis.app.analyzer.metrics.queries.rd

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.lib.database.Database
import org.svis.app.analyzer.metrics.queries.famix.FamixPackageQueries

class RDPackageQueries {
	var GraphDatabaseService graph
	var FamixPackageQueries famix

	new(){
		graph = Database::instance
		famix = new FamixPackageQueries()
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
		this.famix = new FamixPackageQueries(graph) 
	}
	
	/**
	 * Calculates the mean density of the RD visualization of the famix snapshot.
	 * Density is the occupied area of a visualization (net area / gross area).
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return 				mean density of the RD visualization of the famix snapshot
	 */
	
	def density() {
		val	result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.density as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the net area of the RD visualization of the famix snapshot
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return				The net area of the RD visualization of the famix snapshot
	 */
	
	def netArea() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.netArea as result, p.fid as id
		''')
		return result.parsePackage
	}

	/**
	 *	Calculates the depth of nesting for every package. This is not the same as the level of the package!
	 * 	The depth nesting is the highest level of all famix elements that belong to a package.
	 *  
	 * @param snapshotID	ID of the famix snapshot node
	 * @return 				A list containing the depth of nesting for every package of the RD visualization of the famix snapshot ordered by famix id of the package
	 */
	
	def nestingPerDisk() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK), (p)-[:HAS_PACKAGE|HAS_STRUCTURE|HAS_METHOD|HAS_ATTRIBUTE*1..]->(el:FAMIXELEMENT), (el)-[:VISUALIZED_BY]->(e:DISK)
			WITH DISTINCT p, max(e.level)  as max, d
			RETURN max - d.level as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates mean data share for every package disk of the RD visualization of the famix snapshot.
	 * Data Share is the occupied space by attributes in relation to net area
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return 				The average data share of the RD visualization of the famix snapshot
	 */
	
	def dataShare() {
//		val netArea = netArea(snapshotID)
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(pd:DISK), (p)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN (sum(d.dataArea*d.netArea))/pd.netArea as result, p.fid as id
		''')
		return result.parsePackage
	}

	/**
	 * Calculates mean method share for every package disk of the RD visualization of the famix snapshot.
	 * Method share is the occupied space by methods in relation to net area
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return 				The average method share of the RD visualization of the famix snapshot
	 */
	
	def methodShare() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(pd:DISK), (p)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN (sum(d.methodArea*d.netArea))/pd.netArea as result, p.fid as id
		''')
		return result.parsePackage
	}

	
	/**
	 * 
	 * Calculates the gross area of the RD visualization of the famix snapshot
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return				The gross area of the RD visualization of the famix snapshot
	 */
	
	def grossArea () {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.grossArea as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean size of class disks (gross area) of the RD visualization of the famix snapshot
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return 				Mean gross area of class disks of the RD visualization of the famix snapshot
	 */
	
	def avgClassDiskSize() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN avg(d.grossArea) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Fetches the number of class disk layers for every package of the RD visualization of the famix snapshot
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return				Number of class disk layers for every package of the RD visualization of the famix snapshot, ordered by famix id of the package
	 */
	
	def numberOfLayersOfClassDisks () {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.numberOfLayers as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates share of simple disks (only methods or only attributes)
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return				Proportion of simple class disks to all class disks 
	 */
	
	def shareOfSimpleDisks() {
		val divisor = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)
			RETURN count(DISTINCT s) as result, p.fid as id
		''')
		val dividend = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			WHERE d.methodArea = 0 OR d.dataArea = 0
			RETURN p.fid as id, count(DISTINCT s) as result
		''')
		return dividend.parsePackage(divisor)
	}
	
	/**
	 * Calculates the mean package level of a RD visualization of a famix snapshot.
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return				Mean package level
	 */
	
	def packageLevel() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.level as result, p.fid as id
		''')
		return result.parsePackage
	}
}