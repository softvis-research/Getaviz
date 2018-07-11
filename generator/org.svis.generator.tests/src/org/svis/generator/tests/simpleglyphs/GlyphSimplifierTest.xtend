package org.svis.generator.tests.simpleglyphs

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import org.custommonkey.xmlunit.XMLUnit
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
import org.svis.generator.rd.RDSettings
//import org.svis.generator.rd.RDSettings.OutputFormat
import org.svis.generator.SettingsConfiguration

class GlyphSimplifierTest {
	val static path = "./output/simple_glyphs/"
	
	@BeforeClass
	def static void launch() {
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		//RDSettings::OUTPUT_FORMAT = OutputFormat::SimpleGlyphsJson
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/simple_glyphs/input/GlyphSimplifierTest.properties")
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Famix2RD.mwe2", "-p", "inputPath=testdata/bank/input/famix", "outputPath=" + path])
		//RDSettings::OUTPUT_FORMAT = OutputFormat::X3D
	}
	
	@Test
	def testGlyphJson() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File( path + "simple-glyphs.json")
			file2 = new File("./testdata/simple_glyphs/simple-glyphs.json")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}
        
		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}
}