package org.svis.app.analyzer.metrics.queries

import org.neo4j.graphdb.GraphDatabaseService
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.*
import org.neo4j.graphdb.Node
import org.svis.lib.database.Database

class SystemQueries {
	var GraphDatabaseService graph
	
	new() {
		graph = Database::instance
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
	}
	
	/**
	 * Return a list of all famix snapshots that are available for a system
	 * 
	 * @param systemID	ID of the overall system
	 * @return 			List of snapshot IDs of a system
	 */
	 
	def snapshotID() {
		val	result = graph.execute('''
			MATCH (s:SYSTEM)-[:HAS_SNAPSHOT]->(f:FAMIX)
			RETURN s.systemID as result, f.snapshotID as id
		''')
		return result.parsePackage
	}
	
//	def snapshotIDs(String systemID) {
//		val result = graph.execute('''
//			MATCH(:SYSTEM {systemID :"«systemID»"})-[:HAS]->(f:FAMIX)
//			RETURN f.snapshotID as result
//			ORDER BY f.commitOrder
//		''')
//		return result.parseAsStringList
//	}
	
	/**
	 * Return a list of all systems in the database
	 * 
	 * @return list of all systems of the database
	 */
	
//	def systemIDs() {
//		val result = graph.execute('''
//			MATCH (s:SYSTEM)
//			RETURN s.systemID as result
//		''')
//		return result.parseAsStringList
//	}
	
	/**
	 * Return the node of the system in the database. 
	 * The node can be used to obtain the saved metadata of the system, such as owner, name, and url. 
	 * 
	 * @pparam systemID	ID of the system
	 * @return	Node of the given system 
	 */
	
	def node(String systemID) {
		val result = graph.execute('''
			MATCH (s:SYSTEM {systemID :"«systemID»"})
			RETURN s as result
		''')
		val nodes = newLinkedList
		result.forEach[row|
			nodes += row.get("result") as Node
		]
		return nodes.last
	}
	
	def getMetadata(Node node, String property) {
		val tx = graph.beginTx
		try {
			val result = node.getProperty(property) as String
			return result
		} finally {
			tx.close
		}
	}
	
	def getName(Node node) {
		return getMetadata(node, "name")
	}
	
	def getURL(Node node) {
		return getMetadata(node, "url")
	}
	
	def getOwner(Node node) {
		return getMetadata(node, "owner")
	}
}