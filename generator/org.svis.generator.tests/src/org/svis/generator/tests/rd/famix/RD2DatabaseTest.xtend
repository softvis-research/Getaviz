package org.svis.generator.tests.rd.famix

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import org.custommonkey.xmlunit.XMLUnit
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils

class RD2DatabaseTest {
	
	@BeforeClass
	def static void launch() {
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Famix2RD2DB.mwe2", "-p","configPath=testdata/bank/input/famix/config.json", "path=testdata/bank/input/famix"])
	}	
	
	
	@Test
	def testDatabase() {
		val outputFolder = new File("../databases/Jahia_RoboCoP.db")
		val testdataFolder = new File("./testdata/bank/output/rd/famix/rd2database/Jahia_RoboCoP.db")
		val contents = outputFolder.listFiles()
		val othercontents = testdataFolder.listFiles()
		for(content: contents){
			if(content.name.endsWith("id")){
				var File file1 = null
				var File file2 = null
				try {
					file1 = new File(content.absolutePath)
					println(file1.name)
					for(othercontent : othercontents){
						if(file1.name == othercontent.name){
							file2 = new File(othercontent.absolutePath)
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace
				}
				assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
			}
		}	
	}		
}