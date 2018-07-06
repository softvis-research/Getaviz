package org.svis.generator.tests.rd.dynamix

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils
import org.svis.generator.rd.RDSettings.OutputFormat
import org.svis.generator.rd.RDSettings
import org.svis.generator.SettingsConfiguration

class Bank_X3DomNoneTest {
	
	//TODO reimplement tests for new dynamix metamodel
	@BeforeClass
	def static void launch() {
		//RDSettings::OUTPUT_FORMAT = OutputFormat::X3DOM 
		SettingsConfiguration.getInstance("../org.svis.generator.tests/testdata/bank/input/BankX3DOMNoneTest.properties")
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/rd/Dynamix2RD.mwe2", "-p", "famixPath=testdata/bank/input/famixDyn",
			"dynamixPath=testdata/bank/input/dynamix","outputPath=output/rd/dynamix/bank/bank_x3dom_none"])
		//RDSettings::OUTPUT_FORMAT = OutputFormat::X3D 
	}
     
    @Test
    def testX3DOM() {
    	var File file1 = null
        var File file2 = null
        try {
            file1 = new File("./output/rd/dynamix/bank/bank_x3dom_none/x3dom-model.html")
			file2 = new File("./testdata/bank/output/rd/dynamix/bank_x3dom_none/x3dom-model.html")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }

        assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
}