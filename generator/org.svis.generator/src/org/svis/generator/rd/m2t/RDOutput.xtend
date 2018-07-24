package org.svis.generator.rd.m2t

import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.svis.generator.X3DUtils
import org.svis.generator.rd.m2t.RD2X3D
import org.svis.generator.rd.m2t.RD2X3DOM
import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Paths
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.Variant

class RDOutput implements IGenerator2 {
	val config = SettingsConfiguration.instance
	@Inject extension X3DUtils util
	val log = LogFactory::getLog(class)
	RD2X3D rd2x3d = new RD2X3D
	RD2X3DCompressed rd2x3dcomp = new RD2X3DCompressed
	RD2X3DOM rd2x3dom = new RD2X3DOM
	RD2AFrame rd2aframe = new RD2AFrame
	RD2JS rd2js = new RD2JS
	RD2JSON rd2json = new RD2JSON

	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("RD2Output has started")
	}

	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("RD2Output has finished")
	}

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		switch (config.outputFormat) {
			case AFrame: {
				fsa.generateFile("model.html", toAFrameHead + rd2aframe.body(resource) + toAFrameTail)
			}
			case X3D: {
				fsa.generateFile("model.x3d", toX3DHead 
					+ rd2x3d.toX3DBody(resource)
					+ toX3DTail)
				val script = Paths.get("../org.svis.generator/resource/aopt-idmap-sapd.bat")
				fsa.generateFile("aopt-idmap-sapd.bat", Files.newInputStream(script))
				if(config.convertToMultipart) {
					fsa.convertToMultipart
			 	}
		 	}
			case X3D_COMPRESSED: {
				fsa.generateFile("model.x3d", toX3DHead + rd2x3dcomp.toX3DBody(resource) + toX3DTail)
			}
			case X3DOM: {
				fsa.generateFile("x3dom-model.html",
					toX3DOMHead() + toAnimationFramework().toString + rd2x3dom.toX3DOMBody(resource) + toX3DOMTail)

				if (config.variant == Variant::DYNAMIC) {
					fsa.generateFile("events.js", "[ " + rd2js.toJSBody(resource) + " ]")
					val anifra = Paths.get("../org.svis.generator/resource/anifra-minified.js")
					fsa.generateFile("anifra-minified.js", Files.newInputStream(anifra))
				}
			}
			case SimpleGlyphsJson: {
				fsa.generateFile("simple-glyphs.json", "[" + rd2json.toGlyphsJson(resource) + "]")
			}
		}
	}
}
