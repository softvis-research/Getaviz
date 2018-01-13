package org.svis.generator.tests.multisphere.famix

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import java.io.File
import java.io.FileNotFoundException
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher
import org.apache.commons.io.FileUtils

class RingBankTest {
	
	@BeforeClass
	def static launch() {
		new Mwe2Launcher().run(#["../org.svis.generator.run/src/org/svis/generator/run/multisphere/Famix2MultiSphereRings.mwe2", "-p", "inputPath=testdata/bank/input/famix"])
	}
    
    @Test
    def testX3D() {
    	var File file1 = null
        var File file2 = null
        try {
            file1 = new File("./output/multisphere/rings/model.x3d")
			file2 = new File("./testdata/bank/output/multisphere/ring/model.x3d")
        } catch (FileNotFoundException e) {
            e.printStackTrace
        }

        assertEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2))
    }
}