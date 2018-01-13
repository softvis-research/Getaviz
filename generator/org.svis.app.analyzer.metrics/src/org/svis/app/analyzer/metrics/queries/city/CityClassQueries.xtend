package org.svis.app.analyzer.metrics.queries.city

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.lib.database.Database

class CityClassQueries {
	var GraphDatabaseService graph
	
	new() {
		graph = Database::instance
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
	}
	
	/**
	 * Calculates the mean area of buildings of the City visualization of the famix snapshot
	 * 
	 * @return 				mean area of buildings of the City visualization of the famix snapshot
	 */
	
	def buildingArea() {
		val result = graph.execute('''
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN b.area as result, s.fid as id
			''')	
		return result.parsePackage
	}

	/**
	 * Calculates the mean height of buildings of the City visualization of the famix snapshot
	 * 
	 * @return 				mean height of buildings of the City visualization of the famix snapshot
	 */
	
	def height() {
		val result = graph.execute('''
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN b.height as result, s.fid as id 
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean volume of buildings of the City visualization of the famix snapshot
	 * 
	 * @return 				mean volume of buildings of the City visualization of the famix snapshot
	 */
	
	def volume() {
		val result = graph.execute('''
			MATCH (s:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN (b.area * b.height) as result, s.fid as id
			''')	
		return result.parsePackage
	}
}
