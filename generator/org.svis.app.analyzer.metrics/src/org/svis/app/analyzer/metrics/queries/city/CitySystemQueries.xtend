package org.svis.app.analyzer.metrics.queries.city

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.lib.database.Database

class CitySystemQueries {
	var GraphDatabaseService graph 
	
	new() {
		graph = Database::instance
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
	}
	
	/**
	 * Calculates the density of the City visualization for every famix snapshot of the system. 
	 * Density is the share of district area that is occupied by buildings. 
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and the density (value)
	 */
	
	def density() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1]->(r:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT), (f)-[:HAS_PACKAGE|HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN sum(b.area)/sum(d.area) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean density of every district of the City visualization for every famix snapshot of the system. 
	 * Density is the share of district area that is occupied by buildings. 
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean density (value)
	 */
	
	def avgPackageDensity() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT), (p)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			WITH sum(b.area) as sumArea, d, f
			RETURN avg(sumArea/d.area) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the area of the City visualization for every famix snapshot of the system. 
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and area (value)
	 */
	
	def area() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE]->(p:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT)
			RETURN sum(d.area) as result,f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean area of buildings of the City visualization for every famix snapshot of the system. 
	 * 
	 * @param systemID	ID of the system
	 * @return 			snupshot number (key) and mean area of buildings (value)
	 */
	
	def avgBuildingArea() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]-(:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN avg(b.area) as result,f.snapshotID as id
			''')	
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean area of districts of the City visualization for every famix snapshot of the system. 
	 * 
	 * @param systemID	ID of the system
	 * @return 			snupshot number (key) and mean area of districts (value)
	 */
	
	def avgDistrictArea() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT)
			RETURN avg(d.area) as result,f.snapshotID as id
			''')	
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean height of buildings of the City visualization for every famix snapshot of the system.
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean height of buildings (value)
	 */
	
	def avgHeight() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]-(:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN avg(b.height) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean volume of buildings of the City visualization for every famix snapshot of the system. 
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean volume of buildings (value)
	 */
	
	def avgVolume() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]-(:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN avg(b.area * b.height) as result,f.snapshotID as id
			''')	
		return result.parsePackage
	}
	
	/**
	 * Calculates the centroid of the City visualization for every famix snapshot of the system. 
	 * 
	 * @see https://en.wikipedia.org/wiki/Centroid
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and the centroid (value)
	 */
	
	def centroid() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]-(:PACKAGE)-[:HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:VISUALIZED_BY]->(b:BUILDING)
			RETURN sum(b.x * b.area * b.height)/sum(b.area * b.height) as x, sum(b.y * b.area * b.height)/sum(b.area * b.height) as y, f.snapshotID as id
		''')
		return result.parsePoint
	}
	
	/**
	 * Calculates the mean district level of the City visualization for every famix snapshot of the system. 
	 * The level is the hierarchical level of a district.
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and mean district level (value)
	 */
	
	def avgDistrictLevel() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT)
			RETURN avg(d.level) as result,f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the max district level of the City visualization for every famix snapshot of the system. 
	 * The level is the hierarchical level of a district.
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and max district level (value)
	 */
	
	def maxDistrictLevel() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:VISUALIZED_BY]->(d:DISTRICT)
			RETURN max(d.level) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
}
