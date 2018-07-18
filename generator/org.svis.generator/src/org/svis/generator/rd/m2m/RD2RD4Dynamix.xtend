package org.svis.generator.rd.m2m

import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.EcoreUtil2
import org.svis.xtext.rd.Disk
import java.util.TreeMap
import org.svis.xtext.rd.DiskInstance
import org.svis.xtext.rd.DiskSegmentInvocation
import org.svis.xtext.rd.Root
import org.svis.xtext.rd.impl.RdFactoryImpl
import org.svis.xtext.rd.DiskSegment
import java.util.List
import org.svis.generator.SettingsConfiguration

class RD2RD4Dynamix extends WorkflowComponentWithModelSlot {
	val config = SettingsConfiguration.instance
	val rdFactory = new RdFactoryImpl
	var long invocationStartTime
	var long invocationStopTime
	var long absoluteDuration
	var long absoluteLength
	private double minHeight = -1
	var long minTime

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		val log = LogFactory::getLog(class)
		log.info("RD2RD4Dynamix has started.")
		val object = ctx.get("rdextended") as Resource

		val diskRoot = object.contents.head as Root
		val diskDocument = diskRoot.document

		// get instances and invocations
		val disks = EcoreUtil2::getAllContentsOfType(diskDocument, Disk)
		val instances = EcoreUtil2::getAllContentsOfType(diskDocument, DiskInstance)
		val invocations = EcoreUtil2::getAllContentsOfType(diskDocument, DiskSegmentInvocation)
		val diskSegments = EcoreUtil2::getAllContentsOfType(diskDocument, typeof(DiskSegment))
		invocationStartTime = invocations.sortBy[start].head.start
		invocationStopTime = invocations.sortBy[-stop].head.stop
		absoluteDuration = invocationStopTime - invocationStartTime
		absoluteLength = ((absoluteDuration / 1000) % 60) * 10
		instances.forEach[length = config.RDHeight.intValue]

		disks.forEach [ disk |
			disk.instances.forEach [ instance |
				instance.position = disk.calculatePosition(instance.start)
				instance.invocations.forEach[position = disk.calculatePosition(start)]
			]
			disk.data.forEach [ segment |
				segment.invocations.forEach[position = disk.calculatePosition(start)]
			]
			disk.methods.forEach [ segment |
				segment.invocations.forEach[position = disk.calculatePosition(start)]
			]

		]
		instances.forEach[length = config.RDHeight.intValue]
		invocations.forEach[length = (((stop - start) * absoluteLength) / absoluteDuration).intValue]
		// put created target model in slot
		val resource = new ResourceImpl()
		setInvocationHeight(diskSegments, disks)

		resource.contents += diskRoot
		ctx.set("rdextended", resource)

		log.info("RD2RD4Dynamix has finished.")
	}

	def setInvocationHeight(List<DiskSegment> diskSegments, List<Disk> disks) {
		disks.forEach[height = config.RDHeight]
		diskSegments.forEach[height = config.RDHeight]
		var maxHeight = 0.0
		for (ds : diskSegments) {
			for (i : ds.invocations) {
				val invocationHeight = i.position.z + toHeight(i, ds)
				if (invocationHeight > maxHeight) {
					maxHeight = invocationHeight
				}
				if (minHeight == -1) {
					minHeight = i.position.z
				}
				if (minHeight > i.position.z) {
					minHeight = i.position.z
				}
			}
		}
	}

	def Double toHeight(DiskSegmentInvocation invocation, DiskSegment segment) {
		val height = segment.height
		val minHeight = height / 20.0
		val calculatedHeight = ((1.0 * invocation.length) / (height * 1.0))
		if (calculatedHeight < minHeight) {
			return minHeight
		} else {
			return calculatedHeight
		}
	}

	def calculatePosition(Disk disk, long startTime) {
		val newPosition = rdFactory.createPosition
		newPosition.x = disk.position.x
		newPosition.y = disk.position.y
		newPosition.z = (((startTime - invocationStartTime) * absoluteLength) / absoluteDuration)
		return newPosition
	}

	def prepareDiskSegmentInvocations(int timePerAnimation, double targetTime,
		List<DiskSegmentInvocation> diskSegmentInvocations) {
		minTime = diskSegmentInvocations.sortBy[start].get(0).start
		diskSegmentInvocations.forEach [
			start = start - minTime
			stop = stop - minTime
		]
		var maxTime = diskSegmentInvocations.sortBy[stop].last.stop
		val timemultiplier = targetTime / maxTime

		// logarithmic scaling
		var sortedMap = new TreeMap()
		sortedMap.put(0 as long, 0 as long)

		for (i : diskSegmentInvocations) {
			sortedMap.put(i.start, i.start)
			sortedMap.put(i.stop, i.stop)
		}
		var offset = 0.0
		var lastEntry = sortedMap.firstEntry
		for (entry : sortedMap.entrySet()) {
			var key = entry.key.longValue
			var lastKey = lastEntry.key.longValue
			var value = (Math::log10(key - lastKey + 1) + 1) + offset
			offset = value + 2 * (timePerAnimation / timemultiplier)
			entry.value = (offset) as long
		}
		for (entry : sortedMap.entrySet()) {
			entry.value = (entry.value.longValue * timemultiplier) as long
		}

		for (segment : diskSegmentInvocations) {
			segment.start = sortedMap.get(segment.start).longValue
			segment.stop = sortedMap.get(segment.stop).longValue
			segment.length = (segment.stop - segment.start) as int
			segment.position.z = segment.start
		}
		return diskSegmentInvocations
	}
}
