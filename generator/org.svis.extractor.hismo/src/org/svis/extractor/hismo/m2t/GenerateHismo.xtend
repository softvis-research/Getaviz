package org.svis.extractor.hismo.m2t

import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.emf.ecore.resource.Resource
import org.apache.commons.logging.LogFactory

class GenerateHismo implements IGenerator2 {
	val log = LogFactory::getLog(class)
	val hismoFile = new ModellToHismoFile
	
	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Generate HismoFile has started")
	}
	
	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Generate HismoFile has finished")
	}
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		fsa.generateFile("model.hismo","(\n" + hismoFile.toHismoBody(resource) + "\n)")
	}
}