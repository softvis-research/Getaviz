package org.svis.app.analyzer.metrics.statistics

import org.neo4j.graphdb.GraphDatabaseService
import org.svis.app.analyzer.metrics.queries.famix.FamixSystemQueries
import org.svis.lib.database.Database
import org.svis.app.analyzer.metrics.queries.SystemQueries
import java.io.FileWriter
import au.com.bytecode.opencsv.CSVWriter
import org.svis.app.analyzer.metrics.queries.city.CitySystemQueries
import org.svis.app.analyzer.metrics.queries.rd.RDSystemQueries
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.getFirst
import static extension org.svis.app.analyzer.metrics.helper.ResultParser.getSecond
import java.io.File

class SystemStatistics {
	
	var GraphDatabaseService graph
	var SystemQueries system
	var CitySystemQueries city
	var FamixSystemQueries famix
	var RDSystemQueries rd
	var File outputFile
	
	new(File outputDirectory) {
		graph = Database::instance
		city = new CitySystemQueries()
		famix = new FamixSystemQueries()
		system = new SystemQueries()
		rd = new RDSystemQueries()
		outputFile = new File(outputDirectory, "system.csv")
		
	}
	
	new(GraphDatabaseService graph) {
		this.graph = graph
		city = new CitySystemQueries(graph)
		famix = new FamixSystemQueries(graph)
		system = new SystemQueries(graph)
		rd = new RDSystemQueries(graph)
	}
	
	 def run() {
	 	panelData()
	 }
	
	def panelData() {
		val writer = new CSVWriter(new FileWriter(outputFile), '\t')
		val names = #["Snapshot ID", "System ID",
			"RD Density", "RD Data Share", "RD Net Area", "RD Gross Area", "RD AVG CDS", "RD AVG NOL",
			"RD AVG Package Level", "RD MAX Package Level", "RD Method Share", "RD SOSD", "RD Thickness Root Disks",
			"NOIP", "NOP", "NORP", "NOM", "SUM NOS", "SUM CC", "NOA", "NOEIRP", "SOIC", "SOCWM", "AVG NOCP",
			"City Density", "City Area", "City AVG Building Area", "City AVG District Area", "City AVG Package Density",
			"City AVG Height", "City AVG Volume", "City AVG Level", "City MAX Level",
			"RD Centrod X", "RD Centroid Y", "RD Data Centroid X", "RD Data Centroid Y",
			"City Centroid X", "City Centroiy Y"
		]
		writer.writeNext(names)
		
		val rdCentroids = rd.centroid
		val rdDataCentroids = rd.dataCentroid
		val cityCentroids = city.centroid
		val snapshots = system.snapshotID
		val results = #[
			rd.density, rd.dataShare, rd.netArea, rd.grossArea, rd.avgClassDiskSize, rd.avgNumberOfLayersOfClassDisks,
			rd.avgPackageLevel, rd.maxPackageLevel, rd.methodShare, rd.shareOfSimpleDisks, rd.thicknessOfRootDisks,
			famix.numberOfInnerPackages, famix.numberOfPackages, famix.numberOfRootPackages, famix.numberOfMethods,
			famix.sumNumberOfStatements, famix.sumCyclomaticComplexity, famix.numberOfAttributes, famix.numberOfElementsInRootPackages,
			famix.shareOfInnerClasses, famix.shareOfClassesWithMethods, famix.avgNumberOfClassesPerPackage,
			city.density, city.area, city.avgBuildingArea, city.avgDistrictArea, city.avgPackageDensity,
			city.avgHeight, city.avgVolume, city.avgDistrictLevel, city.maxDistrictLevel
		]
		snapshots.forEach[key, value |
			val list = newLinkedList
			list += key
			list += value
			results.forEach[r|
				list += r.get(key)
			]
			val rdCentroid = rdCentroids.get(key)
			val rdDataCentroid = rdDataCentroids.get(key)
			val cityCentroid = cityCentroids.get(key)
			list += rdCentroid.first
			list += rdCentroid.second
			list += rdDataCentroid.first
			list += rdDataCentroid.second
			list += cityCentroid.first
			list += cityCentroid.second
			writer.writeNext(list)
		]
		writer.close
	}
	

}