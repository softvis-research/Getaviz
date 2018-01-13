package org.svis.generator.tests.rd.famix

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import org.custommonkey.xmlunit.XMLUnit
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
import org.svis.generator.rd.RDSettings
import org.junit.AfterClass
import org.svis.generator.rd.RDSettings.OutputFormat

class Bank_X3DomNoneTest {
	
	val static path = "./output/rd/famix/bank/bank_x3dom_none/"

	@BeforeClass
	def static void launch() {
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3DOM
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Famix2RD.mwe2", "-p","inputPath=testdata/bank/input/famix", "outputPath=" + path])
	}

	@Test
	def testX3DOM() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File(path + "x3dom-model.html")
			file2 = new File("./testdata/bank/output/rd/famix/bank_x3dom_none/x3dom-model.html")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}

	@AfterClass
	def static void end() {
		RDSettings::OUTPUT_FORMAT = OutputFormat::X3D
	}
}