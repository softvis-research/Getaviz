package org.svis.app.analyzer.metrics.queries.famix

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.svis.lib.database.Database

class FamixSystemQueries {
	var GraphDatabaseService graph
	
	new() {
		graph = Database::instance
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
	}
	
	/**
	 * Calculates the number of classes for every famix snapshot of the system, including enums, parameterized classes, and annotation types
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and number of classes (value)
	 */
	
	def numberOfClasses() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:FAMIXPACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)
			RETURN count(s) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the number of inner packages for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and number of inner packages (value)
	 */
	 
	 def numberOfInnerPackages() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE]->(p:PACKAGE)-[:HAS_PACKAGE*1..]->(i:PACKAGE)
			RETURN count(i) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the number of packages for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and number of packages
	 */
	 
	def numberOfPackages() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)
			RETURN count(p) as result , f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	
	/**
	 * Calculates the number of root packages for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and number of root packages (valuer
	 */
	 
	def numberOfRootPackages() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE]->(p:PACKAGE)
			RETURN count(p) as result, f.snapshotID as id
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
			MATCH (f:FAMIX)-[:HAS_PACKAGE|HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN count(m) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the number of statements for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean number of statements (value)
	 */
	
	def sumNumberOfStatements() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE|HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN sum(m.numberOfStatements) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean number of statements for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean number of statements (value)
	 */
	
	def avgNumberOfStatements() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE|HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN avg(m.numberOfStatements) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the cyclomatic complexity for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and cyclomatic complexity (value)
	 */
	
	def sumCyclomaticComplexity() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE|HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN sum(m.cyclomaticComplexity) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the mean cyclomatic complexity for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and mean cyclomatic complexity (value)
	 */
	
	def avgCyclomaticComplexity() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE|HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:HAS_METHOD*1..]->(m:METHOD)
			RETURN avg(m.cyclomaticComplexity) as result, f.snapshotID a id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the number of attributes for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and number of attributes (value)
	 */
	
	def numberOfAttributes() {
		val	result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE|HAS_STRUCTURE*1..]->(:FAMIXSTRUCTURE)-[:HAS_ATTRIBUTE*1..]->(el:FAMIXELEMENT)
			RETURN count(el) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/** 
	 * Calculates the number of elements (namespaces, classes ...)  within all root packages for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and number of elements within all root packages (value)
	 */
	
	def numberOfElementsInRootPackages() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE]->(p:PACKAGE)-[:HAS_PACKAGE|:HAS_STRUCTURE]->(el:FAMIXELEMENT)
			RETURN count(el) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**
	 * Calculates the share of classes with inner classes in relation to all classes for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return 			snapshot number (key) and proportion of classes with inner classes to all classes (value)
	 */
	
	def shareOfInnerClasses() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[cr:HAS_STRUCTURE]->(s:FAMIXSTRUCTURE)
			OPTIONAL MATCH (s)-[ir:HAS_STRUCTURE]->(i:FAMIXSTRUCTURE)
			RETURN toFloat(count(ir))/count(cr) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
	/**	
	 * Calculates the share of classes with methods in relation to all classes for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and proportion of classes with methods to all classes (value)
	 */
	
	def shareOfClassesWithMethods() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(:PACKAGE)-[:HAS_STRUCTURE*1..]->(s:FAMIXSTRUCTURE)
			OPTIONAL MATCH (s)-[:HAS_METHOD]->(m:METHOD)
			RETURN toFloat(count(m))/count(s) as result, f.snapshotID as id
		''')
		return result.parsePackage
	}

	/**
	 * Calculates the mean number of classes per package for every famix snapshot of the system
	 * 
	 * @param systemID	ID of the system
	 * @return			snapshot number (key) and mean number of classes per package (value)
	 */
	
	def avgNumberOfClassesPerPackage() {
		val result = graph.execute('''
			MATCH (f:FAMIX)-[:HAS_PACKAGE*1..]->(p:PACKAGE)-[:HAS_STRUCTURE]->(s:FAMIXSTRUCTURE)
			WITH f.snapshotID as id, p, count(DISTINCT s) as nocpp
			RETURN id, avg(nocpp) as result
		''')
		return result.parsePackage
	}
}