package org.svis.generator.city.m2t

import org.eclipse.xtext.generator.IGenerator2
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.svis.generator.X3DUtils
import javax.inject.Inject
import org.svis.generator.city.CitySettings.BuildingType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.svis.generator.SettingsConfiguration

class CityOutput implements IGenerator2 {
	val log = LogFactory::getLog(class)
	val config = new SettingsConfiguration
	City2JS city2js = new City2JS
	City2X3DOM city2x3dom = new City2X3DOM
	City2AFrame city2aframe = new City2AFrame
	City2X3D city2x3d = new City2X3D
	@Inject extension X3DUtils util

	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("CityOutput has started")
	}

	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("CityOutput has finished")
	}

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		switch (config.cityOutputFormat) {
			case AFrame: {
				switch (config.buildingType) {
					case BuildingType::CITY_ORIGINAL,
					case BuildingType::CITY_PANELS,
					case BuildingType::CITY_FLOOR,
					case BuildingType::CITY_BRICKS: {
						fsa.generateFile("model.html", toAFrameHead + city2aframe.toAFrameBody(resource) + toAFrameTail)
					}
					default: {
					}
				}
			}
			case X3D: {
				switch (config.buildingType) {
					case BuildingType::CITY_ORIGINAL:
						fsa.generateFile("model.x3d", toX3DHead + city2x3d.toX3DBody(resource) + toX3DTail)
					case BuildingType::CITY_PANELS,
					case BuildingType::CITY_FLOOR,
					case BuildingType::CITY_BRICKS:
						fsa.generateFile("model.x3d",
							toX3DHead + city2x3d.settingsInfo + city2x3d.toX3DBody(resource) + toX3DTail)
					case BuildingType::CITY_DYNAMIC: {
						fsa.generateFile("model.html",
							toX3DOMHead + toAnimationFramework().toString + city2x3dom.toX3DOMBody(resource) +
								toX3DOMTail)
						fsa.generateFile("events.js", "[ " + city2js.toJSBody(resource) + " ]")
						val Path p1 = Paths.get("../org.svis.generator/resource/anifra-minified.js")
						fsa.generateFile("anifra-minified.js", Files.newInputStream(p1))

					}
				}
			}
			default: {
			}
		}
	}
}
