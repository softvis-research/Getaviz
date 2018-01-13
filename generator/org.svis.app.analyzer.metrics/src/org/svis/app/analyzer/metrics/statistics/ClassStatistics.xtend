package org.svis.app.analyzer.metrics.statistics

import org.neo4j.graphdb.GraphDatabaseService
import org.svis.lib.database.Database
import org.svis.app.analyzer.metrics.queries.rd.RDSystemQueries
import org.svis.app.analyzer.metrics.queries.SystemQueries
import java.io.FileWriter
import au.com.bytecode.opencsv.CSVWriter
import org.svis.app.analyzer.metrics.queries.rd.RDClassQueries
import org.svis.app.analyzer.metrics.queries.famix.FamixClassQueries
import org.svis.app.analyzer.metrics.queries.city.CityClassQueries
import java.io.File

class ClassStatistics {
	
	var GraphDatabaseService graph
	var SystemQueries system
	var RDSystemQueries rdSystem
	var RDClassQueries rd
	var FamixClassQueries famix
	var CityClassQueries city
	var File outputFile
	
	new(File outputDirectory) {
		graph = Database::instance
		rdSystem = new RDSystemQueries()
		rd = new RDClassQueries()
		famix = new FamixClassQueries()
		city = new CityClassQueries()
		system = new SystemQueries()
		outputFile = new File(outputDirectory, "classes.csv")
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
		rdSystem = new RDSystemQueries(graph)
		rd = new RDClassQueries(graph)
		famix = new FamixClassQueries(graph)
		city = new CityClassQueries(graph)
		system = new SystemQueries(graph)
	}
	
	 def run() {
//		timeSeries()
		panelPackages()
	 }
	 
//	def timeSeries() {
//		val writer = new CSVWriter(new FileWriter("output/csv/timeseries.csv"), '\t')
//		val systemID = famix.systemID
//		val density = rd.density
//		
//		systemID.forEach[key, value |
////			val density 	 = rdSystem.density(systemID)
//			val ids = density.filter[]
//			val list = newLinkedList
//			list += systemID
//			density.values.forEach[v |
//				list += v.toString
//			]
//			writer.writeNext(list)
//		]
//		writer.close
//	}
	
	def panelPackages() {
		val writer = new CSVWriter(new FileWriter(outputFile), '\t')
		val names = #["ID", "System", "Snapshot", "Commit Order",
			"RD Density", "RD Net Area", "RD Gross Area", "RD Package Level", "RD Data Share", "RD Method Share",
			"NOA", "NOM", "SUM NOS", "AVG NOS", "SUM CC", "AVG CC",
			"Has Methods", "Has Inner Classes",
			"City Building Area", "City Height", "City Volume"]
		writer.writeNext(names)
		val results = #[famix.classID, famix.snapshotID, famix.commitOrder,
			rd.density, rd.netArea, rd.grossArea, rd.packageLevel, rd.dataShare, rd.methodShare, 
			famix.numberOfAttributes, famix.numberOfMethods, famix.sumNumberOfStatements,
			famix.avgNumberOfStatements, famix.sumCyclomaticComplexity, famix.avgCyclomaticComplexity, 
			famix.hasMethods, famix.hasInnerClasses,
		 	city.buildingArea, city.height, city.volume
		]
		results.get(0).forEach[key, value|
			val list = newLinkedList
			list += key
			results.forEach[r|
				val tmp = r.get(key)
				list += tmp
			]
			writer.writeNext(list)
		]
		writer.close
	}
}