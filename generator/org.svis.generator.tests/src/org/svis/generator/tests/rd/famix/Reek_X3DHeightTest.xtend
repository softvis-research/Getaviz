package org.svis.generator.tests.rd.famix

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import org.custommonkey.xmlunit.XMLUnit
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import com.google.common.io.Files
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.junit.Assert
import org.apache.commons.io.FileUtils
import org.svis.generator.tests.helper.JSONUtil
import org.svis.generator.rd.RDSettings
import org.junit.AfterClass
import org.svis.generator.rd.RDSettings.MetricRepresentation
import org.svis.generator.SettingsConfiguration

class Reek_X3DHeightTest {
	
	val static path = "./output/rd/famix/reek/reek_x3d_height/"
	val static engine = XMLUnit.newXpathEngine
	var static String json
	val rd = XMLUnit::buildControlDocument(Files::toString(new File(path + "rd.xml"), Charset.forName("UTF-8")))

	@BeforeClass
	def static void launch() {
		//RDSettings::METRIC_REPRESENTATION = MetricRepresentation::HEIGHT
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/reek/input/ReekX3DHeightTest.properties")
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Famix2RD.mwe2", "-p","inputPath=testdata/reek/input/famix", "outputPath=" + path])
		json = JSONUtil::read("./testdata/reek/output/rd/famix/reek_x3d_height/metaData.json")
	}

	@Test
	def testMetaData() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File(path + "metaData.json")
			file2 = new File("./testdata/reek/output/rd/famix/reek_x3d_height/metaData.json")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}

	@Test
	def testX3D() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File(path + "model.x3d")
			file2 = new File("./testdata/reek/output/rd/famix/reek_x3d_height/model.x3d")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}

	@Test
	def rdNumberOfClasses() {
		val classes = engine.getMatchingNodes("//disks[@type=\"FAMIX.Class\"]", rd)
		Assert.assertEquals(138, classes.length)
	}

	@Test
	def rdNumberOfNamespaces() {
		val namespaces = engine.getMatchingNodes("//disks[@type=\"FAMIX.Namespace\"]", rd)
		Assert.assertEquals(52, namespaces.length)
	}

	@Test
	def rdNumberOfData() {
		val attributes = engine.getMatchingNodes("//data", rd)
		Assert.assertEquals(0, attributes.length)
	}

	@Test
	def rdNumberOfMethods() {
		val methods = engine.getMatchingNodes("//methods", rd)
		Assert.assertEquals(740, methods.length)
	}

	@Test
	def rd() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File("./testdata/reek/output/rd/famix/reek_x3d_height/rd.xml")
			file2 = new File(path + "rd.xml")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}

	@Test
	def rdExtended() {
		var File file1 = null
		var File file2 = null

		try {
			file1 = new File("./testdata/reek/output/rd/famix/reek_x3d_height/rdextended.xml")
			file2 = new File(path + "rdextended.xml")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}

	@AfterClass
	def static void end() {
//		RDSettings::METRIC_REPRESENTATION = MetricRepresentation::NONE
	}
}
