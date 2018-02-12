package org.svis.extractor.hismo.tests

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
import org.svis.generator.rd.RDSettings
import org.svis.generator.rd.RDSettings.OutputFormat

class RoboCoPTest {
	
	@BeforeClass
	def static void launch() {
		new Mwe2Launcher().run(#["../org.svis.extractor.hismo/src/org/svis/extractor/hismo/run/Famix2Hismo.mwe2", "-p", "configPath=testdata/RoboCoP/input/config.json"])
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3DOM
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Hismo2RD.mwe2", "-p", "inputPath=../org.svis.extractor.hismo/output","outputPath=../org.svis.extractor.hismo/output/time_line/"])
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3D
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Hismo2RD.mwe2", "-p", "inputPath=../org.svis.extractor.hismo/output","outputPath=../org.svis.extractor.hismo/output/time_line/"])
	}
	
    @Test
    def hismo() {
    	var File file1 = null
    	var File file2 = null
        try {
			file1 = new File("testdata/RoboCoP/output/model.hismo")
			file2 = new File("output/model.hismo")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }
        
   		assertEquals(FileUtils::checksumCRC32(file1), FileUtils::checksumCRC32(file2))
	}
	
	@Test
	def x3dom(){
		    	var File file1 = null
    	var File file2 = null
        try {
			file1 = new File("testdata/RoboCoP/output/time_line/x3dom-model.html")
			file2 = new File("output/time_line/x3dom-model.html")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }
        
   		assertEquals(FileUtils::checksumCRC32(file1), FileUtils::checksumCRC32(file2))
	}
	
	@Test
	def x3d(){
		    	var File file1 = null
    	var File file2 = null
        try {
			file1 = new File("testdata/RoboCoP/output/time_line/model.x3d")
			file2 = new File("output/time_line/model.x3d")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }
        
   		assertEquals(FileUtils::checksumCRC32(file1), FileUtils::checksumCRC32(file2))
	}
}