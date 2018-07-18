package org.svis.app.analyzer.metrics

//import org.svis.metrics.charts.Charts
//import org.svis.app.analyzer.metrics.statistics.SystemStatistics
//import org.svis.app.analyzer.metrics.statistics.PackageStatistics
import org.svis.app.analyzer.metrics.statistics.ClassStatistics
import org.svis.lib.database.Database
import java.io.File

class Metrics {
	
	def static void main(String[] args) {
		Database::databases.forEach[
			Database::getInstance(it)
			val outputDirectory = new File("output/csv/" + Database::name)
			outputDirectory.mkdirs
			//val packageStat = new PackageStatistics(outputDirectory)
			//val systemStat = new SystemStatistics(outputDirectory)
			val classStat = new ClassStatistics(outputDirectory)
			//packageStat.run
			//systemStat.run
			classStat.run
		]
	}
}