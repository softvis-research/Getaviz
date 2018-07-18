package org.svis.generator.tests.city.famix

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
import com.jayway.jsonpath.JsonPath
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import org.svis.generator.tests.helper.JSONUtil
import org.svis.generator.SettingsConfiguration

class CityAFrame_PanelsAndroidPhoneTest {
	
	val static path = "./output/city/famix/aframe/panels/android_phone/"
	val static engine = XMLUnit.newXpathEngine
	var static String json
	val city2 = XMLUnit::buildControlDocument(Files::toString(new File(path + "city2.xml"), Charset.forName("UTF-8")))
	
	@BeforeClass
	def static void launch() {
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/android_phone/input/CityAFramePanelsAndroidPhoneTest.properties")
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/city/Famix2City.mwe2", "-p", "inputPath=testdata/android_phone/input/famix", "outputPath=./output/city/famix/aframe/panels/android_phone"])
		json = JSONUtil::read("./output/city/famix/aframe/panels/android_phone/metaData.json")
	}

	@Test
	def testMetaData() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File("./output/city/famix/aframe/panels/android_phone/metaData.json")
			file2 = new File("./testdata/android_phone/output/city/famix/aframe/panels/metaData.json")
		} catch (FileNotFoundException e) {
			e.printStackTrace
	}
        
		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
	}
     
    /**
	 * Checks, if method belongs to given class
	 */
	
	@Test
	def void testMethodBelongsTo() {
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_e7252952b8c4f3460ff5a7698dd870a25a246c59')].belongsTo", hasItem("ID_b493b25346511112f3f467d21bb7a3a442f8864e")));
	}
    
    /**
     * Checks, if method accesses given attribute 
     */
    
	@Test
	def void testMethodAccesses() {
		val result = JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_384cdd2a5062b26341a509ee633b799da013e64e')].accesses"))
		assertTrue(result.contains("ID_f09fba74299b41105764c3166aaccc79cebc65a4"))
	}
    
    /**
     * Checks, if method access given number of attributes
     */
    
	@Test
	def void testMethodNumberOfAccesses() {
		val result = JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_d9232a5608d15556ad830e870177d37f013c58a0')].accesses"))
		assertTrue(result.size == 19)
	}
    
   /**
    * Checks if attribute is accessed by given method
    */
   
	@Test
	def void testAttributeAccessedBy() {
		val result = JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_6a5a1ede8e2d7041d51880087e49cf76013b926d')].accessedBy"))
      	assertTrue(result.contains("ID_163b7574e4538c4966b41666d6316ffa43765967"))
	}
   
    /**
     * Checks if attribute belongs to given class
     */
   
	@Test
	def void testAttributBelongsTo(){
    	assertThat(json, hasJsonPath("$..[?(@.id=='ID_b81e6d3d89205764048e8dc6e086527b9c374002')].belongsTo", hasItem("ID_61aab191883443a4b68294edf496380d21a16cec")))
	}
    
    /*
     * checks if class is subclass of another given class
     */
   
	@Test
	def void testSubClassOf(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_780d8d04c274a8fbe92be9faafa9b9a64d9ae0e5')].subClassOf", hasItem("ID_36871de283883bc9f6794a37a8d0f95c7f7a3b27")))	
	}
   
   /*
    * Checks if given Class has the right number of Subclasses
    */ 
    
	@Test
	def void testSuperClassOf(){
		val result= JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_36871de283883bc9f6794a37a8d0f95c7f7a3b27')].superClassOf"))
		assertTrue(result.size==3)
	}
    
    /*
     * Checks if a method is called by another method
     */
     
	@Test
	def void testMethodCalledBy(){
		val result=JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_8a19b1caa9e2a1f3bdd37ce1739ffbdf31414581')].calledBy"))
		assertTrue(result.contains("ID_206be4bde836c4acb76d8aa9e72235e021c7d1ce"))
    }
    
    /*
     * Checks if method calls another given method
     */
    
	@Test
	def void testMethodCalls(){
		val result=JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_1238875d3ba7cce3a7a318e23c2d2008431763bb')].calls"))
		assertTrue(result.contains("ID_d1781f20244249e4fb91ab699fa4b4612722c497"))	
	}
    
    /* 
     * checks if class has given qualified name 
     */
     
	@Test
	def void testQualifiedNameClass(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_a01abace90c30b2cd9b4e3c49dd657bc0dcc82ac')].qualifiedName", hasItem("com.android.phone.NetworkQueryService.LocalBinder")))
	}
     	
     	/* 
     * checks if method has given qualified name 
     */
     
	@Test
	def void testQualifiedNameMethod(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_9d9b1b6c583ad71a0278e243371e566915822117')].qualifiedName", hasItem("com.android.phone.CallNotifier.onSignalInfo(AsyncResult)")))
	}
     	
     	/* 
     * checks if attribute has given qualified name 
     */
     
	@Test
	def void testQualifiedNameAttribute(){
     	assertThat(json, hasJsonPath("$..[?(@.id=='ID_d6b00fd014d6aa968839c3d2613824df95ad0086')].qualifiedName", hasItem("com.android.phone.EmergencyDialer.mVibrator")))
     }
     	
     /*
      * check if namespace has given qualified name
      */
     
	@Test
	def void testQualifiedNameNamespace(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_5fb552a76ef3c7ee67681d80e9797e088a6c9859')].qualifiedName", hasItem("com")))
	}
    
	@Test
	def void testSubNamespce(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_501b14856c0c2da26a2072a19a7020303aeb654a')].belongsTo", hasItem("ID_5fb552a76ef3c7ee67681d80e9797e088a6c9859")))
	} 
    
	@Test
	def testQualifiedNameEnumValue(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_cfc3fec68b934098d16bd70bc9c642e84d9d02b7')].qualifiedName", hasItem("com.android.phone.IccPinUnlockPanel.IccLockState.REQUIRE_PIN")))
	}	
    
	@Test
	def testQualifiedNameEnum(){
		assertThat(json, hasJsonPath("$..[?(@.id=='ID_437550476d557838e0a0baff205c2699d3aa49c4')].qualifiedName", hasItem("com.android.phone.CdmaPhoneCallState.PhoneCallState")))
	}    
          
	@Test
	def testCityX3D() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File("./output/city/famix/aframe/panels/android_phone/model.html")
			file2 = new File("./testdata/android_phone/output/city/famix/aframe/panels/model.html")
		} catch (FileNotFoundException e) {
			e.printStackTrace
		}
        
		assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    
	}
 
    /** 
     * zum umschreiben
     * pruefen ob stimmt 
     * muss an die city2 xml angepasst werden
     * 
     */
    
	@Test
	def city2NumberOfClasses() {
		val classes = engine.getMatchingNodes("//entities[@type=\"FAMIX.Class\"]", city2)
		Assert.assertEquals(73, classes.length)
	}
    
    @Test
    def city2NumberOfNamespaces() {
    	val namespaces = engine.getMatchingNodes("//entities[@type=\"FAMIX.Namespace\"]", city2)
    	Assert.assertEquals(3, namespaces.length)
    }
    
   
    @Test
    def city2() {
    	var File file1 = null
    	var File file2 = null
        try {
			file1 = new File("./output/city/famix/aframe/panels/android_phone/city2.xml")
			file2 = new File("./testdata/android_phone/output/city/famix/aframe/panels/city2.xml")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }
        
    	assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
	
}