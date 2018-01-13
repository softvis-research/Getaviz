package org.svis.app.analyzer.metrics.statistics

import org.neo4j.graphdb.GraphDatabaseService
import org.svis.lib.database.Database
import org.svis.app.analyzer.metrics.queries.rd.RDSystemQueries
import org.svis.app.analyzer.metrics.queries.SystemQueries
import java.io.FileWriter
import au.com.bytecode.opencsv.CSVWriter
import org.svis.app.analyzer.metrics.queries.rd.RDPackageQueries
import org.svis.app.analyzer.metrics.queries.famix.FamixPackageQueries
import org.svis.app.analyzer.metrics.queries.city.CityPackageQueries
import java.io.File

class PackageStatistics {
	
	var GraphDatabaseService graph
	var SystemQueries system
	var RDSystemQueries rdSystem
	var RDPackageQueries rd
	var FamixPackageQueries famix
	var CityPackageQueries city
	var File outputFile
	
	new(File outputDirectory) {
		graph = Database::instance
		rdSystem = new RDSystemQueries()
		rd = new RDPackageQueries()
		famix = new FamixPackageQueries()
		city = new CityPackageQueries()
		system = new SystemQueries()
		outputFile = new File(outputDirectory, "packages.csv")
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
		rdSystem = new RDSystemQueries(graph)
		rd = new RDPackageQueries(graph)
		famix = new FamixPackageQueries(graph)
		city = new CityPackageQueries(graph)
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
			"RD Density", "RD AVG Nesting", "RD Net Area", "RD Gross Area", "RD AVG CDS",
			"RD AVG NOL", "RD Package Level", "RD Data Share", "RD Method Share", "RD Share of Simple Disks",
			"NOC", "NOA", "NOM", "NOS",
			"AVG NOS", "CC", "AVG CC", "SOIC", "SOCWM",
			"City Density", "City Area", "City AVG Building Area", "City AVG Height", "City AVG Volume", "City District Level"]
		writer.writeNext(names)
		val results = #[famix.systemID, famix.snapshotID, famix.commitOrder,
			rd.density,  rd.nestingPerDisk, rd.netArea, rd.grossArea, rd.avgClassDiskSize,
			rd.numberOfLayersOfClassDisks, rd.packageLevel, rd.dataShare, rd.methodShare, rd.shareOfSimpleDisks,
			famix.numberOfClasses, famix.numberOfAttributes, famix.numberOfMethods, famix.sumNumberOfStatements,
			famix.avgNumberOfStatements, famix.sumCyclomaticComplexity, famix.avgCyclomaticComplexity, famix.shareOfInnerClasses,
		  	famix.shareOfClassesWithMethods,
		 	city.density, city.area, city.avgBuildingArea, city.avgHeight, city.avgVolume, city.districtLevel
		]
		results.get(0).forEach[key, value|
			val list = newLinkedList
			list += key
			results.forEach[r|
				list += r.get(key)
			]
			writer.writeNext(list)
		]
		writer.close
	}
}