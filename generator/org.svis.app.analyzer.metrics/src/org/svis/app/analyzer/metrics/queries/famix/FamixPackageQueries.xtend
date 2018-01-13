package org.svis.app.analyzer.metrics.queries.famix

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.lib.database.Database

class FamixPackageQueries {
	var GraphDatabaseService graph
	
	new() {
		graph = Database::instance
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
	}
	
	def systemID() {
		val	result = graph.execute('''
			MATCH (s:SYSTEM)-[:HAS_SNAPSHOT]->(:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)
			RETURN p.fid as id, s.systemID as result
		''')
		return result.parsePackage
	}
	
	def snapshotID() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)
			RETURN p.fid as id, f.snapshotID as result
		''')
		return result.parsePackage
	}
	
	
	/**
	 * Calculates the number of classes for every famix snapshot of the system, including enums, parameterized classes, and annotation types
	 * 
	 * @return 			snapshot number (key) and number of classes (value)
	 */
	
	def numberOfClasses() {
		val	result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)
			RETURN count(s) as result, p.fid as id
		''')
		return result.parsePackage
	}
	/**
	 * Calculates the number of methods for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and number of methods (value)
	 */
	
	def numberOfMethods() {
		val	result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN count(m) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the number of statements for every famix snapshot of the system
	 * 
	 * @return 			snapshot number (key) and mean number of statements (value)
	 */
	
	def sumNumberOfStatements() {
		val	result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN sum(m.numberOfStatements) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean number of statements for every famix snapshot of the system
	 * 
	 * @return 			snapshot number (key) and mean number of statements (value)
	 */
	
	def avgNumberOfStatements() {
		val	result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN avg(m.numberOfStatements) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the cyclomatic complexity for every famix snapshot of the system
	 * 
	 * @return 			snapshot number (key) and cyclomatic complexity (value)
	 */
	
	def sumCyclomaticComplexity() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN sum(m.cyclomaticComplexity) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean cyclomatic complexity for every famix snapshot of the system
	 * 
	 * @return 			snapshot number (key) and mean cyclomatic complexity (value)
	 */
	
	def avgCyclomaticComplexity() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN avg(m.cyclomaticComplexity) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the number of attributes for every famix snapshot of the system
	 * 
	 * @return 			snapshot number (key) and number of attributes (value)
	 */
	
	def numberOfAttributes() {
		val	result = graph.execute('''
			MATCH (p:PACKAGE)-[:HAS_STRUCTURE]->(:FAMIXSTRUCTURE)-[:HAS_ATTRIBUTE*1..]->(el:FAMIXELEMENT)
			RETURN count(el) as result, p.fid as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the share of classes with inner classes in relation to all classes for every famix snapshot of the system
	 * 
	 * @return 			snapshot number (key) and proportion of classes with inner classes to all classes (value)
	 */
	
	def shareOfInnerClasses() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[a:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)
			OPTIONAL MATCH (:PACKAGE)-[b:HAS_STRUCTURE]->(s)
			RETURN p.fid as id, (toFloat(count(DISTINCT a)) - count(DISTINCT b))/count(DISTINCT a) as result
		''')
		return result.parsePackage
	}
	
	/**	
	 * Calculates the share of classes with methods in relation to all classes for every famix snapshot of the system
	 * 
	 * @return			snapshot number (key) and proportion of classes with methods to all classes (value)
	 */
	
	def shareOfClassesWithMethods() {
		val result = graph.execute('''
			MATCH (p:PACKAGE)-[a:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)
			OPTIONAL MATCH (s)-[b:VISUALIZED_BY]->(d:DISK {methodArea: 0})
			RETURN p.fid as id, (toFloat(count(DISTINCT a))-count(DISTINCT b))/count(DISTINCT a) as result
		''')
		return result.parsePackage
	}
	
	def commitOrder() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)
			RETURN p.fid as id, f.commitOrder as result
		''')
		return result.parsePackage
	}
}