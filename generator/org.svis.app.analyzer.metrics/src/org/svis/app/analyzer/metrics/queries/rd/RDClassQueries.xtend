package org.svis.app.analyzer.metrics.queries.rd

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.lib.database.Database
import org.svis.app.analyzer.metrics.queries.famix.FamixPackageQueries

class RDClassQueries {
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
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.density as result, s.fid as id
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
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.netArea as result, s.fid as id
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
		val result = graph.execute('''
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.dataArea as result, s.fid as id
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
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.methodArea as result, s.fid as id
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
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.grossArea as result, s.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean package level of a RD visualization of a famix snapshot.
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return				Mean package level
	 */
	
	def packageLevel() {
		val result = graph.execute('''
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(d:DISK)
			RETURN d.level as result, s.fid as id
		''')
		return result.parsePackage
	}
}