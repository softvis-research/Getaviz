package org.svis.generator.tests.rd.famix

import org.svis.generator.famix.FAMIXSettings
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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import org.svis.generator.tests.helper.JSONUtil
import org.junit.AfterClass
import org.svis.generator.SettingsConfiguration

class MasterRootTest {
	
	val static path = "./output/rd/famix/nested_package_test/master_root/"
	val static engine = XMLUnit.newXpathEngine
	var static String json
	val rd = XMLUnit::buildControlDocument(Files::toString(new File(path + "rd.xml"), Charset.forName("UTF-8")))
	
	@BeforeClass
	def static void launch() {
		//FAMIXSettings::MASTER_ROOT = true
		//FAMIXSettings::MERGE_PACKAGES = true
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/nested_package_test/input/MasterRootTest.properties")
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Famix2RD.mwe2", "-p", "inputPath=testdata/nested_package_test/input/famix", "outputPath=" + path])
		json = JSONUtil::read("./testdata/nested_package_test/output/rd/famix/master_root/metaData.json")
	}
	
	@Test
	def testParentScopeNamespace() {
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_e6eaa85d988ccdb20784352ac915323a70329d38')].belongsTo", hasItem("ID_c0e9d0e280889bde49789686c98848f7221f3538")))
	}
	
	@Test
	def testParentScopeClass(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_c22afd5ca0dfea117c9d967816570e570364159a')].belongsTo", hasItem("ID_6eb61200a20ae42f11b03b5f9910f38a35687a23")))	
	}
	
	@Test
	def testParentScopeDoubleMerge(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_6eb61200a20ae42f11b03b5f9910f38a35687a23')].belongsTo", hasItem("ID_881b4c2d9709012d406a9d47d303bfc54dfa09f3")))	
	}
	
	@Test
	def testMetaData() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File( path + "metaData.json")
			file2 = new File("./testdata/nested_package_test/output/rd/famix/master_root/metaData.json")
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
			file2 = new File("./testdata/nested_package_test/output/rd/famix/master_root/model.x3d")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}

		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}
       
    @Test
    def rdNumberOfClasses() {
    	val classes = engine.getMatchingNodes("//disks[@type=\"FAMIX.Class\"]", rd)
    	Assert.assertEquals(5, classes.length)
    }
    
    @Test
    def rdNumberOfNamespaces() {
    	val namespaces = engine.getMatchingNodes("//disks[@type=\"FAMIX.Namespace\"]", rd)
    	Assert.assertEquals(6, namespaces.length)
    }
    
    @Test
    def rdNumberOfData() {
    	val attributes = engine.getMatchingNodes("//data", rd)
    	Assert.assertEquals(0, attributes.length)
    }
    
    @Test
    def rdNumberOfMethods() {
    	val methods = engine.getMatchingNodes("//methods", rd)
    	Assert.assertEquals(1, methods.length)
    }
    
    @Test
    def rd() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File("./testdata/nested_package_test/output/rd/famix/master_root/rd.xml")
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
			file1 = new File("./testdata/nested_package_test/output/rd/famix/master_root/rdextended.xml")
			file2 = new File(path + "rdextended.xml")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}
    	
		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}
    
    @AfterClass
    def static void end() {
//    	FAMIXSettings::MASTER_ROOT = false
//    	FAMIXSettings::MERGE_PACKAGES = false
    }
}