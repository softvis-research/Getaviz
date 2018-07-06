package org.svis.generator.tests.rd.hismo

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
import org.svis.generator.rd.RDSettings.OutputFormat
import org.svis.generator.rd.RDSettings
import org.svis.generator.rd.RDSettings.Variant
import org.svis.generator.rd.RDSettings.EvolutionRepresentation
import org.svis.generator.SettingsConfiguration

class Sidekiq_DynamicEvolutionTest {
	
	//TODO reimplement tests for new hismo metamodel
	@BeforeClass
	def static void launch() {
		/*
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3DOM 
		RDSettings::EVOLUTION_REPRESENTATION = EvolutionRepresentation::DYNAMIC_EVOLUTION
		RDSettings::VARIANT = Variant::DYNAMIC
		*/
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/sidekiq/input/SidekiqDynamicEvolutionTest.properties")
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Hismo2RD.mwe2", "-p", "inputPath=testdata/sidekiq/input","outputPath=output/rd/hismo/sidekiq/sidekiq_dynamic_evolution/"])
		/*
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3D 
		RDSettings::VARIANT = Variant::STATIC
		RDSettings::EVOLUTION_REPRESENTATION = EvolutionRepresentation::TIME_LINE
		*/
	}
     
    @Test
    def testX3DOM() {
    	var File file1 = null
        var File file2 = null
        try {
            file1 = new File("./output/rd/hismo/sidekiq/sidekiq_dynamic_evolution/x3dom-model.html")
			file2 = new File("./testdata/sidekiq/output/rd/hismo/sidekiq_dynamic_evolution/x3dom-model.html")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }

        assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }	
    
	@Test
	def testEventJS() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File("./output/rd/hismo/sidekiq/sidekiq_dynamic_evolution/events.js")
			file2 = new File("./testdata/sidekiq/output/rd/hismo/sidekiq_dynamic_evolution/events.js")
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
			file1 = new File("./testdata/sidekiq/output/rd/hismo/sidekiq_dynamic_evolution/rd.xml")
			file2 = new File("./output/rd/hismo/sidekiq/sidekiq_dynamic_evolution/rd.xml")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}
}
