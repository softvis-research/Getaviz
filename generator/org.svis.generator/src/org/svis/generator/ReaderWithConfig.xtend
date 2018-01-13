package org.svis.generator

import org.eclipse.xtext.mwe.Reader
import java.io.FileReader

class ReaderWithConfig extends Reader {
	var Configuration config

	def setConfig(String path) {
		config = new Configuration (new FileReader(path))
		pathes += config.outputDirectoryFamix
	}
}
