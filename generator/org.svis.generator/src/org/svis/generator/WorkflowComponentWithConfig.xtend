package org.svis.generator

import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import java.io.FileReader
import org.eclipse.xtend.lib.annotations.Accessors
import org.apache.commons.logging.LogFactory
import org.svis.generator.Configuration

abstract class WorkflowComponentWithConfig extends WorkflowComponentWithModelSlot{
	@Accessors(PUBLIC_GETTER) Configuration config
	protected val log = LogFactory::getLog(class)
	
	new() {
		config = new Configuration()
	}
	
	override preInvoke() {
	}
	
	override postInvoke() {
	}
	
	def setConfig(String path) {
		config = new Configuration (new FileReader(path))
	}
}
