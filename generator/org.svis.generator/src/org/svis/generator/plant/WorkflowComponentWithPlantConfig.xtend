package org.svis.generator.plant

import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import java.io.FileReader
import org.eclipse.xtend.lib.annotations.Accessors
import org.apache.commons.logging.LogFactory
import org.svis.generator.plant.PlantConfiguration

abstract class WorkflowComponentWithPlantConfig extends WorkflowComponentWithModelSlot{
	@Accessors(PUBLIC_GETTER) PlantConfiguration config
	protected val log = LogFactory::getLog(class)
	
	new() {
		config = new PlantConfiguration()
	}
	
	override preInvoke() {
	}
	
	override postInvoke() {
	}
	
	def setConfig(String path) {
		config = new PlantConfiguration (new FileReader(path))
	}
}
