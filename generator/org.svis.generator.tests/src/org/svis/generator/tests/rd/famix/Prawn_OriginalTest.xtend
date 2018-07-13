package org.svis.generator.tests.rd.famix

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import org.custommonkey.xmlunit.XMLUnit
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
import org.svis.generator.tests.helper.JSONUtil
import org.svis.generator.SettingsConfiguration

class Prawn_OriginalTest {
	
	val static path = "./output/rd/famix/prawn/original/"
	var static String json
	
	
	@BeforeClass
	def static void launch() {
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/prawn/input/PrawnOriginalTest.properties")
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Famix2RD.mwe2", "-p", "inputPath=testdata/prawn/input/famix", "outputPath=" + path])
		json = JSONUtil::read("./testdata/prawn/output/rd/famix/original/metaData.json")
	}
	
	
	@Test
	def testMetaData() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File( path + "metaData.json")
			file2 = new File("./testdata/prawn/output/rd/famix/original/metaData.json")
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
			file2 = new File("./testdata/prawn/output/rd/famix/original/model.x3d")
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
			file1 = new File("./testdata/prawn/output/rd/famix/original/rd.xml")
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
			file1 = new File("./testdata/prawn/output/rd/famix/original/rdextended.xml")
			file2 = new File(path + "rdextended.xml")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }
    	
    	assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
	
}