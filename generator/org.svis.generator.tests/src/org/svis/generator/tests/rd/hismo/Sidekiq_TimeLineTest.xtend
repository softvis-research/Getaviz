package org.svis.generator.tests.rd.hismo

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
//import org.svis.generator.rd.RDSettings.OutputFormat
import org.svis.generator.rd.RDSettings
import org.svis.generator.SettingsConfiguration

class Sidekiq_TimeLineTest {
	//TODO reimplement tests for new hismo metamodel
	@BeforeClass
	def static void launch() {
		//RDSettings::OUTPUT_FORMAT = OutputFormat::X3DOM 
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/sidekiq/input/SidekiqTimeLineTest.properties")
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Hismo2RD.mwe2", "-p", "inputPath=testdata/sidekiq/input","outputPath=output/rd/hismo/sidekiq/sidekiq_time_line/"])
		//RDSettings::OUTPUT_FORMAT = OutputFormat::X3D 
	}
     
    @Test
    def testX3DOM() {
    	var File file1 = null
        var File file2 = null
        try {
            file1 = new File("./output/rd/hismo/sidekiq/sidekiq_time_line/x3dom-model.html")
			file2 = new File("./testdata/sidekiq/output/rd/hismo/sidekiq_time_line/x3dom-model.html")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }

        assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
    
	@Test
	def rd() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File("./testdata/sidekiq/output/rd/hismo/sidekiq_time_line/rd.xml")
			file2 = new File("./output/rd/hismo/sidekiq/sidekiq_time_line/rd.xml")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}
	
}