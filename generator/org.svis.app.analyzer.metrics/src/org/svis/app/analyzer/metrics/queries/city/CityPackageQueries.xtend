package org.svis.app.analyzer.metrics.queries.city

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.lib.database.Database

class CityPackageQueries {
	var GraphDatabaseService graph
	
	new() {
		graph = Database::instance
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
	}
	
	/**
	 * Calculates the density of the City visualization of the famix snapshot
	 * 
	 * @return 				density of the City visualization of the famix snapshot
	 */
	
	def density() {
		val	result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING), (p)-[:VISUALIZED_BY]->(d:DISTRICT)
			RETURN (sum(b.area)/d.area) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the area of the City visualization of the famix snapshot
	 * 
	 * @return 				area of the City visualization of the famix snapshot
	 */
	
	def area() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT)
			RETURN sum(d.area) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean area of buildings of the City visualization of the famix snapshot
	 * 
	 * @return 				mean area of buildings of the City visualization of the famix snapshot
	 */
	
	def avgBuildingArea() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN avg(b.area) as result, p.fid as id
			''')	
		return result.parsePackage
	}
	

	/**
	 * Calculates the mean height of buildings of the City visualization of the famix snapshot
	 * 
	 * @return 				mean height of buildings of the City visualization of the famix snapshot
	 */
	
	def avgHeight() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN avg(b.height) as result, p.fid as id 
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean volume of buildings of the City visualization of the famix snapshot
	 * 
	 * @return 				mean volume of buildings of the City visualization of the famix snapshot
	 */
	
	def avgVolume() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN avg(b.area * b.height) as result, p.fid as id
			''')	
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean district level of the City visualization of the famix snapshot
	 * 
	 * @param snapshotID	ID of the famix snapshot node
	 * @return				mean district level of the City visualization of the famix snapshot
	 */
	
	def districtLevel() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT)
			RETURN d.level as result, p.fid as id
		''')
		return result.parsePackage
	}
}
