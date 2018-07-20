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
import com.jayway.jsonpath.JsonPath
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import org.svis.generator.tests.helper.JSONUtil
import org.svis.generator.SettingsConfiguration

class Bank_JdtParserTest {
	
	val static path = "./output/rd/famix/bank/bank_jdtParser/"
	val static engine = XMLUnit.newXpathEngine
	var static String json
	val rd = XMLUnit::buildControlDocument(Files::toString(new File(path + "rd.xml"), Charset.forName("UTF-8")))
	
	@BeforeClass
	def static void launch() {
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/bank/input/BankJdtParserTest.properties")
		XMLUnit::ignoreWhitespace = true
		XMLUnit::ignoreComments = true
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Famix2RD.mwe2", "-p", "inputPath=testdata/bank/input/famix_jdtParser", "outputPath=" + path])
		json = JSONUtil::read("./testdata/bank/output/rd/famix/bank_jdtParser/metaData.json")
	}
	
	@Test
	def testMetaData() {
		var File file1 = null
		var File file2 = null
		try {
			file1 = new File( path + "metaData.json")
			file2 = new File("./testdata/bank/output/rd/famix/bank_jdtParser/metaData.json")
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
      	assertThat(json, hasJsonPath("$..[?(@.id=='ID_f734e3036b271f56e1dbb9ca69f4aa920b2f98c2')].belongsTo", hasItem("ID_527aa1c76ab5cca95e6dbfcea35a5d2d9f5d737f")));
    }
    
    /**
     * Checks, if method accesses given attribute 
     */
    
    @Test
    def void testMethodAccesses() {
    	val result = JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_b9fc03fe668187e6e2a0df760fec7a785ba7be12')].accesses"))
      	assertTrue(result.contains("ID_37834fec4533797c12daa8509eb11587547caac1"))
    }
    
    /**
     * Checks, if method access given number of attributes
     */
     
    @Test
    def void testMethodNumberOfAccesses() {
    	val result = JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_feafc0fa0ada80b19459ccf9116eb24e8a40f9a7')].accesses"))
      	assertTrue(result.size == 2)
    }
   
    /**
     * Checks if attribute belongs to given class
     */
     
    @Test
    def void testAttributBelongsTo(){
    	assertThat(json, hasJsonPath("$..[?(@.id=='ID_0cf4af5bed915ef9b74c93b2c50852c21f2d5364')].belongsTo", hasItem("ID_9fa088272bab165867bfaddcfbf58d6a7d5d45a2")))
    }
  
    /*
     * checks if class is subclass of another given class
     */
     
    @Test
    def void testSubClassOf(){
    	assertThat(json, hasJsonPath("$..[?(@.id=='ID_e14c10820a526fde0ea0beee073b947e1ca67a4a')].subClassOf", hasItem("ID_ecbec7f0190fc1c903a87797d35427dc7a1f240b")))	
    }
  	
  	 /*
    * Checks if given Class has the right number of Subclasses
    */   
    
    @Test
    def void testSuperClassOf(){
    	val result= JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_ecbec7f0190fc1c903a87797d35427dc7a1f240b')].superClassOf"))
    	assertTrue(result.size==3)
    }
    
    /*
     * Checks if a method is called by another method
     */
     
    @Test
    def void testMethodCalledBy(){
    	val result=JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_19fdcf3c56394fcdd0c6179bad9ca8c87b4c8228')].calledBy"))
    	assertTrue(result.contains("ID_02001050956f6f8799e919575875a7b56d8415b1"))
    }
    
    /*
     * Checks if method calls another given method
     */
     
    @Test
    def void testMethodCalls(){
    	val result=JSONUtil::toList(JsonPath::read(json, "$..[?(@.id=='ID_61658aef236e200a9ffa30030e7cb0549bef0d4f')].calls"))
    	assertTrue(result.contains("ID_78cd3ffe06ad51757c7facd4bbfdd3130313e03b"))	
    }
    
    /* 
     * checks if class has given qualified name 
     */
     
     @Test
     def void testQualifiedNameClass(){
     	assertThat(json, hasJsonPath("$..[?(@.id=='ID_fba940eea4ca7f9002bc529bbb0cbc8fc0423985')].qualifiedName", hasItem("bank.products.Credit")))
     }
     	
     	/* 
     * checks if method has given qualified name 
     */
     
     @Test
     def void testQualifiedNameMethod(){
     	assertThat(json, hasJsonPath("$..[?(@.id=='ID_119adb8ccd123c522bc2026f3c4542a65094c714')].qualifiedName", hasItem("bank.Bank.getPrivateCustomers()")))
     }
     	
     	/* 
     * checks if attribute has given qualified name 
     */
     
     @Test
     def void testQualifiedNameAttribute(){
     	assertThat(json, hasJsonPath("$..[?(@.id=='ID_37834fec4533797c12daa8509eb11587547caac1')].qualifiedName", hasItem("bank.products.AbstractProduct.productNumber")))
     }
     	
     /*
      * check if namespace has given qualified name
      */
     
     @Test
     def void testQualifiedNameNamespace(){
     	assertThat(json, hasJsonPath("$..[?(@.id=='ID_bdd240c8fe7174e6ac1cfdd5282de76eb7ad6815')].qualifiedName", hasItem("bank")))
     }
    
    @Test
    def void testSubNamespce(){
    	assertThat(json, hasJsonPath("$..[?(@.id=='ID_4481fcdc97864a546f67c76536e0308a3058f75d')].belongsTo", hasItem("ID_bdd240c8fe7174e6ac1cfdd5282de76eb7ad6815")))
    } 	
    
    @Test
    def testX3D() {
    	var File file1 = null
        var File file2 = null
        try {
            file1 = new File(path + "model.x3d")
			file2 = new File("./testdata/bank/output/rd/famix/bank_jdtParser/model.x3d")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }

        assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
       
    @Test
    def rdNumberOfClasses() {
    	val classes = engine.getMatchingNodes("//disks[@type=\"FAMIX.Class\"]", rd)
    	Assert.assertEquals(8, classes.length)
    }
      
    @Test 
    def void testMethodNumberOfStatements(){
    	val methodsNOS = engine.getMatchingNodes("//methods[@size=\"10.0\"]",rd)
    	Assert.assertEquals(38,methodsNOS.length)
    }
    
    @Test
    def rdNumberOfNamespaces() {
    	val namespaces = engine.getMatchingNodes("//disks[@type=\"FAMIX.Namespace\"]", rd)
    	Assert.assertEquals(3, namespaces.length)
    }
    
    @Test
    def rdNumberOfData() {
    	val attributes = engine.getMatchingNodes("//data", rd)
    	Assert.assertEquals(17, attributes.length)
    }
    
    @Test
    def rdNumberOfMethods() {
    	val methods = engine.getMatchingNodes("//methods", rd)
    	Assert.assertEquals(38, methods.length)
    }
    
    @Test
    def rd() {
    	var File file1 = null
    	var File file2 = null
        try {
			file1 = new File("./testdata/bank/output/rd/famix/bank_jdtParser/rd.xml")
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
			file1 = new File("./testdata/bank/output/rd/famix/bank_jdtParser/rdextended.xml")
			file2 = new File(path + "rdextended.xml")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }
    	
    	assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
}