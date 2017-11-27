package org.svis.lib.database

import org.neo4j.graphdb.GraphDatabaseService
import java.io.File
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

class Database {
	static GraphDatabaseService graph
	var static GraphDatabaseService testGraph
	@Accessors(PUBLIC_GETTER) static String path
	@Accessors(PUBLIC_GETTER) static List<File> databases= {
		val dir = new File("../databases/")
		dir.mkdir
		dir.listFiles.filter(file | file.directory).toList
	}
	
	def static getInstance() {
		return graph 
	}
	
	def static getInstance(String owner, String name){
		val newPath = "../databases/" + owner + "_" + name + ".db"
    	return getInstance(newPath)
	}
	
	def static getInstance(File newFile) {
		return getInstance(newFile.toString)
	}
	
	def static getInstance(String newPath) {
		if (graph === null || path != newPath) {
			if (graph !== null) {
				graph.shutdown
			}
			path  = newPath
			graph = initializeDatabase(path)
		}
		return graph
	}
	
	def static getName() {
		return path.substring(13)
	}
	
	def static getTestInstance() {
		if (testGraph === null) {
			testGraph = initializeDatabase("testdata/databases/softvis.db")
		}
		return testGraph
	}
	
	
    /**
     * Initializes the database. 
     * If a database already exists, the existing database is used.
     * Otherwise a new embedded database is created and the schema constraints are applied.
     * Constraints: Define IDs as unique
     * 
     * @param path	Path of the database
     * @return graph database instance
     */
    
    def private static initializeDatabase(String path) {
    	val neofile = new File(path)
    	val graph = new GraphDatabaseFactory().newEmbeddedDatabase(neofile)
    	graph.registerShutdownHook
		val tx = graph.beginTx
		try {
			if (graph.schema.constraints.size < 3) {
				graph.schema.constraintFor(DBLabel::FAMIXELEMENT).assertPropertyIsUnique("fid").create
				graph.schema.constraintFor(DBLabel::FAMIX).assertPropertyIsUnique("snapshotID").create
				graph.schema.constraintFor(DBLabel::SYSTEM).assertPropertyIsUnique("systemID").create
			}
			tx.success
		} finally {
			tx.close
		}
		
		return graph
    }
    
    /**
	 * Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits 
	 * (even if you "Ctrl-C" the running application). 
	 * http://neo4j.com/docs/java-reference/current/#tutorials-java-embedded-setup-startstop
	 * 
	 * @param graphDb neo4j database
	 */
	
	def private static registerShutdownHook(GraphDatabaseService graphDb ) {
    	Runtime.runtime.addShutdownHook( new Thread() {
        	override public void run() {
           		if (graphDb !== null) {	
            		graphDb.shutdown
            	}
        	}
    	} )
    }	
}