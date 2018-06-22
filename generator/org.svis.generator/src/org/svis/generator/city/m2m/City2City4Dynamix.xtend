package org.svis.generator.city.m2m

import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.EcoreUtil2
import org.svis.xtext.city.Root
import org.svis.xtext.city.Invocation
import org.svis.xtext.city.Building
import org.svis.generator.SettingsConfiguration

class City2City4Dynamix extends WorkflowComponentWithModelSlot {
	val config = new SettingsConfiguration
//	val cityFactory = new CityFactoryImpl
	var long invocationStartTime
	var long invocationStopTime
	var long absoluteDuration
	var long absoluteLength

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		val log = LogFactory::getLog(class)
		log.info("City2City4Dynamix has started.")
		val object = ctx.get("CITYv2") as Resource

		val cityRoot = object.contents.head as Root
		val cityDocument = cityRoot.document

		// get instances and invocations
//		val districts = EcoreUtil2::getAllContentsOfType(cityDocument, District)
		val invocations = EcoreUtil2::getAllContentsOfType(cityDocument, Invocation)
		val buildings = EcoreUtil2::getAllContentsOfType(cityDocument, Building)

		invocationStartTime = invocations.sortBy[start].head.start
		invocationStopTime = invocations.sortBy[-stop].head.stop
		absoluteDuration = invocationStopTime - invocationStartTime
		absoluteLength = ((absoluteDuration / 1000) % 60) * 10

		invocations.forEach[length = (((stop - start) * absoluteLength) / absoluteDuration).intValue]

//		buildings.forEach[b|
//			b.invocations.forEach[i|
//				i.position = calculatePosition(b, i.start)
//			]
//		]
//		
		// put created target model in slot
		val resource = new ResourceImpl()
		buildings.forEach[height = config.RDHeight]
		resource.contents += cityRoot
		ctx.set("CITYv2", resource)

		log.info("City2City4Dynamix has finished.")
	}

	def Double toHeight(Invocation invocation, Building building) {
		val height = building.height
		val minHeight = height / 20.0
		val calculatedHeight = ((1.0 * invocation.length) / (height * 1.0))
		if (calculatedHeight < minHeight) {
			return minHeight
		} else {
			return calculatedHeight
		}
	}

//	def calculatePosition(Building building, long startTime) {
//		val newPosition = cityFactory.createPosition
//		newPosition.x = building.position.x
//		newPosition.y = building.position.y
//		newPosition.z = (((startTime - invocationStartTime) * absoluteLength) / absoluteDuration)
//		println(newPosition.z)
//		return newPosition
//	}
}
