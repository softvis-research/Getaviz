package org.svis.extractor.hismo;

import org.eclipse.emf.mwe.utils.Writer
import org.eclipse.xtend.lib.annotations.Accessors
import java.io.FileReader
import org.svis.generator.Configuration

class CustomEMFWriter extends Writer {
	@Accessors(PUBLIC_GETTER) Configuration config
	
	def setConfig(String path) {
		config = new Configuration (new FileReader(path))
		uri = config.outputDirectorySystem + "/hismo.xml"
	}
}
