package org.svis.generator.tests.rd.dynamix

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
//import org.svis.generator.rd.RDSettings.OutputFormat
import org.svis.generator.rd.RDSettings
import org.svis.generator.rd.RDSettings.InvocationRepresentation
import org.svis.generator.rd.RDSettings.Variant
import org.svis.generator.SettingsConfiguration

class Prawn_X3DomFlashingMethodsTest {
	
				//TODO reimplement tests for new dynamix metamodel
	@BeforeClass
	def static void launch() {
		/*
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3DOM
		RDSettings::INVOCATION_REPRESENTATION = InvocationRepresentation::FLASHING_METHODS  
		RDSettings::VARIANT = Variant::DYNAMIC 
		*/
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/prawn/input/PrawnFlashingMethodsTest.properties")
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Dynamix2RD.mwe2", "-p", "famixPath=testdata/prawn/input/famixDyn",
			"dynamixPath=testdata/prawn/input/dynamix","outputPath=output/rd/dynamix/prawn/prawn_x3dom_flashing_methods"])
		/*
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3D 
		RDSettings::VARIANT = Variant::STATIC
		RDSettings::INVOCATION_REPRESENTATION = InvocationRepresentation::NONE
		*/
	}
     
    @Test
    def testX3DOM() {
    	var File file1 = null
        var File file2 = null
        try {
            file1 = new File("./output/rd/dynamix/prawn/prawn_x3dom_flashing_methods/x3dom-model.html")
			file2 = new File("./testdata/prawn/output/rd/dynamix/prawn_x3dom_flashing_methods/x3dom-model.html")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }

        assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
    
    @Test
	def testMetaData() {
        var File file1 = null
        var File file2 = null
        try {
            file1 = new File("./output/rd/dynamix/prawn/prawn_x3dom_flashing_methods/metaData.json")
			file2 = new File("./testdata/prawn/output/rd/dynamix/prawn_x3dom_flashing_methods/metaData.json")
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
			file1 = new File("./output/rd/dynamix/prawn/prawn_x3dom_flashing_methods/events.js")
			file2 = new File("./testdata/prawn/output/rd/dynamix/prawn_x3dom_flashing_methods/events.js")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}
	
}