package org.svis.generator.rd.m2t

import java.util.List
import org.svis.xtext.rd.DiskSegment
import java.util.Collections
import java.util.Comparator
import java.util.ArrayList
import org.svis.generator.rd.RDSettings.InvocationRepresentation
import org.svis.xtext.rd.DiskSegmentInvocation
import org.svis.xtext.rd.Disk
import java.util.HashMap
import org.svis.xtext.rd.DiskVersion
import java.util.HashSet
import org.svis.xtext.rd.Version
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.svis.generator.rd.RDSettings.Variant
import org.svis.generator.rd.RDSettings.EvolutionRepresentation
import java.util.TreeMap
import org.svis.generator.rd.m2m.RD2RD4Dynamix
import org.svis.generator.SettingsConfiguration

class RD2JS {
	val config = new SettingsConfiguration
	RD2RD4Dynamix rd2rd4dy = new RD2RD4Dynamix
	val startTime = 5000
	val versionSpeed = 2000
	val diskVersionAnimationSpeed = 500
	val segmentVersionAnimationSpeed = 800
	val log = LogFactory::getLog(class)
	val timePerAnimation = 250
	val multipleDisks = new ArrayList<Disk>
	val multipleDiskSegments = new ArrayList<DiskSegment>

	def toJSBody(Resource resource) {
		log.info("RD2JS has started")
		var disks = EcoreUtil2::getAllContentsOfType(resource.contents.head, Disk)
		var diskSegments = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskSegment)
		var diskVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskVersion)
		var diskSegmentVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, Version)
		for (root : resource.contents) {
			multipleDisks += EcoreUtil2::getAllContentsOfType(root, Disk)
			multipleDiskSegments += EcoreUtil2::getAllContentsOfType(root, DiskSegment)
		}
		switch (config.evolutionRepresentation) {
			case MULTIPLE_TIME_LINE,
			case MULTIPLE_DYNAMIC_EVOLUTION: {
				disks = multipleDisks
				if (config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION) {
					diskSegments = multipleDiskSegments
				}
			}
			case DYNAMIC_EVOLUTION: {
				diskVersions.forEach[v|if (v.scale < 0.001) {v.scale = 0.001}]
			}
			default: {
			}
		}
		val actionsList = new ArrayList<String>
		actionsList += toEvents(diskSegments)
		actionsList += toEventsInvocations(diskSegmentInvocationsJs(resource, diskSegments))
		actionsList += toSegmentVersionEvents(diskSegmentVersions, diskSegments)
		actionsList += toDiskVersionEvents(diskVersions, disks)
		actionsList += toMultipleDiskVersionEvents(disks, diskSegments, diskVersions)
		actionsList.removeAll("")
		log.info("RD2JS has finished")

		return actionsList.join(",\n")

	}

	def private toEvents(List<DiskSegment> segments) {
		var actions = new ArrayList<String>()
		for (segment : segments) {
			if (segment.frequency != 0) {
				actions.
					add('''{"time" : 0, "action" : "changeColor", "parameters":{"node_id": "«segment.diskSegmentMaterialId»", "type": "show_and_return", "duration": "«segment.frequency»", "target_value": "1.0 1.0 1.0" }}''')
			}
			if (segment.luminance != 0) {
				actions.
					add('''{"time" : 0, "action" : "changeColor", "parameters":{"node_id": "«segment.diskSegmentMaterialId»", "type": "show_and_return", "duration":1000, "target_value": "«((1/segment.luminance))» «((1/segment.luminance))» «((1/segment.luminance))»" }}''')
			}
		}
		Collections.sort(actions, new Comparator<String>() {

			override compare(String o1, String o2) {
				val i1 = Integer::parseInt(o1.replaceAll("[^:]*\"time\" : ([0-9]+).*", "$1"))
				val i2 = Integer::parseInt(o2.replaceAll("[^:]*\"time\" : ([0-9]+).*", "$1"))
				return i1.compareTo(i2)
			}
		})
		actions.join(",\n")
	}

	def String diskSegmentMaterialId(DiskSegment segment) {
		return segment.id + '_MATERIAL'
	}

	def diskSegmentInvocationsJs(Resource resource, List<DiskSegment> diskSegments) {
		val diskSegmentInvocations = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskSegmentInvocation)
		if (diskSegmentInvocations.size == 0 || config.variant == Variant::STATIC) {
			return diskSegmentInvocations
		}
		return rd2rd4dy.prepareDiskSegmentInvocations(250, 3600000.0, diskSegmentInvocations)
	}

	def private toEventsInvocations(List<DiskSegmentInvocation> invocations) {
		var actions = new ArrayList<String>()
		for (invocation : invocations.sortBy[start]) {
			val caller = invocations.findFirst[name == invocation.caller]
			if (config.invocationRepresentation == InvocationRepresentation::MOVING_SPHERES ||
				config.invocationRepresentation == InvocationRepresentation::MOVING_FLASHING) {
				if (caller !== null) {
					actions +=
						'''{"time" : «invocation.start», "action" : "addNode", "parameters":{"node_id": "invocation_«invocation.name»", "size": "2", "node_type":"Sphere", "initial_position": "«caller.diskSegmentCenter»", "use_label" : true, "label": "«invocation.fqn.replaceAll("^.*\\.","")»" }}'''
					actions +=
						'''{"time" : «invocation.start», "action" : "moveNodeCircular", "parameters":{"node_id": "invocation_«invocation.name»", "duration": "«timePerAnimation»", "target_value": "«invocation.diskSegmentCenter»" }}'''
					actions +=
						'''{"time" : «invocation.stop - timePerAnimation», "action" : "moveNodeCircular", "parameters":{"node_id": "invocation_«invocation.name»", "duration": "«timePerAnimation»", "target_value": "«caller.diskSegmentCenter»" }}'''
				} else {
					actions +=
						'''{"time" : «invocation.start», "action" : "addNode", "parameters":{"node_id": "invocation_«invocation.name»", "size": "2", "node_type":"Sphere", "initial_position": "«invocation.diskSegmentCenter»", "use_label" : true, "label": "«invocation.fqn.replaceAll("^.*\\.","")»" }}'''
				}
				actions +=
					'''{"time" : «invocation.stop», "action" : "removeNode", "parameters":{"node_id": "invocation_«invocation.name»"}}'''
			}

			if (config.invocationRepresentation == InvocationRepresentation::FLASHING_METHODS ||
				config.invocationRepresentation == InvocationRepresentation::MOVING_FLASHING) {
				actions +=
					'''{"time" : «invocation.start», "action" : "changeColor", "parameters":{"node_id": "«invocation.diskSegmentMaterialId»", "count": 1, "duration": "«timePerAnimation»", "target_value": "1.0 1.0 1.0" }}'''
				actions +=
					'''{"time" : «invocation.stop - timePerAnimation», "action" : "changeColor", "parameters":{"node_id": "«invocation.diskSegmentMaterialId»", "count": 1, "duration": "«timePerAnimation»", "target_value": "«invocation.diskSegmentColor»" }}'''
			}
		}

		Collections::sort(actions, new Comparator<String>() {
			override compare(String o1, String o2) {
				val i1 = Integer::parseInt(o1.replaceAll("[^:]*\"time\" : ([0-9]+).*", "$1"))
				val i2 = Integer::parseInt(o2.replaceAll("[^:]*\"time\" : ([0-9]+).*", "$1"))
				return i1.compareTo(i2)
			}
		})
		actions.join(",\n")
	}

	def private diskSegmentCenter(DiskSegmentInvocation segmentInvocation) {
		val segment = segmentInvocation.eContainer as DiskSegment
		val targetAngle = segment.anglePosition + (segment.angle / 2.0)
		val pointX = (Math::cos(targetAngle) * segment.outerRadius - (Math::cos(targetAngle) * segment.innerRadius)) /
			2.0 + (Math::cos(targetAngle) * segment.innerRadius)
		val pointY = (Math::sin(targetAngle) * segment.outerRadius - (Math::sin(targetAngle) * segment.innerRadius)) /
			2.0 + (Math::sin(targetAngle) * segment.innerRadius)
		val pointZ = 2.0
		val center = (segment.eContainer as Disk).position

		return (center.x + pointX) + "," + (center.y + pointY) + "," + (center.z + pointZ)
	}

	def private diskSegmentMaterialId(DiskSegmentInvocation segmentInvocation) {
		val segment = segmentInvocation.eContainer as DiskSegment
		return segment.id + '_MATERIAL'
	}

	def private diskSegmentColor(DiskSegmentInvocation segmentInvocation) {
		val segment = segmentInvocation.eContainer as DiskSegment
		return segment.color
	}

	def private toDiskVersionEvents(List<DiskVersion> diskVersions, List<Disk> disks) {
		if (config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION) {
			return ""
		}
		val actions = new ArrayList<String>()
		val diskTargetValues = new HashMap<Disk, String>()
		val TreeMap<Integer, List<DiskVersion>> sortedVersions = new TreeMap<Integer, List<DiskVersion>>()
		sortedVersions.putAll(diskVersions.groupBy[level])
		sortedVersions.forEach [ level, versions |
			var availableDisks = new HashSet<Disk>()

			for (version : versions) {
				val disk = (version.eContainer as Disk)
				availableDisks += disk
				val nodeId = (version.eContainer as Disk).id + "_SCALE"
				val targetValue = version.scale + " " + version.scale + " " + version.scale

				if (diskTargetValues.get(disk) != targetValue) {
					actions +=
						('''{"time" : «(((version.level * versionSpeed))) + startTime», "action" : "changeScale", "parameters":{"count": 1, "node_id": "«nodeId»", "duration":"«diskVersionAnimationSpeed»", "target_value":"«targetValue»" }}''')
				}
				diskTargetValues.put(disk, targetValue)
			}

			for (disk : disks) {
				if (!availableDisks.contains(disk)) {
					val nodeId = disk.id + "_SCALE"
					var targetValue = "0.00001 0.00001 0.00001"
					if (diskTargetValues.get(disk) != targetValue) {
						actions +=
							('''{"time" : «(((level * versionSpeed))) + startTime», "action" : "changeScale", "parameters":{"count": 1, "node_id": "«nodeId»", "duration":"«diskVersionAnimationSpeed»", "target_value":"«targetValue»" }}''')
					}
					diskTargetValues.put(disk, targetValue)
				}
			}
		]
		Collections.sort(actions, new Comparator<String>() {
			override compare(String o1, String o2) {
				val i1 = Integer::parseInt(o1.replaceAll("(?s)[^:]*\"time\" : ([0-9]+).*", "$1"))
				val i2 = Integer::parseInt(o2.replaceAll("(?s)[^:]*\"time\" : ([0-9]+).*", "$1"))
				return i1.compareTo(i2)
			}
		})
		actions.join(",\n")
	}

	def private toSegmentVersionEvents(List<Version> versions, List<DiskSegment> diskSegments) {
		if (config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION) {
			return ""
		}
		val actions = new ArrayList<String>()
		val TreeMap<Integer, List<Version>> sortedVersions = new TreeMap<Integer, List<Version>>()
		val segmentTargetValues = new HashMap<DiskSegment, String>()
		sortedVersions.putAll(versions.groupBy[level])
		sortedVersions.forEach [ level, segmentVersions |
			var availableSegments = new HashSet<DiskSegment>()
			for (version : segmentVersions) {
				val diskSegment = (version.eContainer as DiskSegment)
				availableSegments += diskSegment
				val nodeId = diskSegment.id + "__RECTANGULARTORUS"
				var targetValue = diskSegment.innerRadius + ""
				if (version.scale > 0.001) {
					targetValue = (diskSegment.outerRadius * version.scale) + ""
				}
				if (segmentTargetValues.get(diskSegment) != targetValue) {
					actions +=
						'''{"time" : «version.level * versionSpeed + startTime», "action" : "changeOuterRadius", "parameters":{"count": 1, "node_id": "«nodeId»", "duration":"«segmentVersionAnimationSpeed»", "target_value":"«targetValue»" }}'''
				}
				segmentTargetValues.put(diskSegment, targetValue)
			}

			for (segment : diskSegments) {
				if (!availableSegments.contains(segment)) {
					val nodeId = segment.id + "__RECTANGULARTORUS"
					var targetValue = (segment).innerRadius + ""
					if (segmentTargetValues.get(segment) != targetValue) {
						actions +=
							'''{"time" : «level * versionSpeed + startTime», "action" : "changeOuterRadius", "parameters":{"count": 1, "node_id": "«nodeId»", "duration":"«segmentVersionAnimationSpeed»", "target_value":"«targetValue»" }}'''
					}
					segmentTargetValues.put(segment, targetValue)
				}
			}
		]
		Collections.sort(actions, new Comparator<String>() {
			override compare(String o1, String o2) {
				val i1 = Integer::parseInt(o1.replaceAll("(?s)[^:]*\"time\" : ([0-9]+).*", "$1"))
				val i2 = Integer::parseInt(o2.replaceAll("(?s)[^:]*\"time\" : ([0-9]+).*", "$1"))
				return i1.compareTo(i2)
			}
		})
		actions.join(",\n")
	}

	def toMultipleDiskVersionEvents(List<Disk> multipleDisks, List<DiskSegment> multipleDiskSegments,
		List<DiskVersion> diskVersions) {
		if (!(config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION)) {
			return ""
		}
		val timestampsDisk = new HashMap<String, ArrayList<String>>
		val timestampsDiskSegment = new HashMap<String, ArrayList<String>>

		multipleDisks.forEach [ d |
			if (!timestampsDisk.containsKey(d.diskVersion.timestamp)) {
				timestampsDisk.put(d.diskVersion.timestamp, new ArrayList<String>)
			}
			timestampsDisk.get(d.diskVersion.timestamp).add(d.id)
		]
		multipleDiskSegments.forEach [ d |
			if (!timestampsDiskSegment.containsKey(d.version.timestamp)) {
				timestampsDiskSegment.put(d.version.timestamp, new ArrayList<String>)
			}
			timestampsDiskSegment.get(d.version.timestamp).add(d.id)
		]
		val allTimestamps = (timestampsDisk.keySet + timestampsDiskSegment.keySet).sort
		val tmpactions = toDiskEvents(timestampsDisk, allTimestamps) +
			toDiskSegmentEvents(timestampsDiskSegment, allTimestamps, multipleDiskSegments)
		val actions = tmpactions.toList
		Collections.sort(actions, new Comparator<String>() {
			override compare(String o1, String o2) {
				val i1 = Integer::parseInt(o1.replaceAll("(?s)[^:]*\"time\" : ([0-9]+).*", "$1"))
				val i2 = Integer::parseInt(o2.replaceAll("(?s)[^:]*\"time\" : ([0-9]+).*", "$1"))
				return i1.compareTo(i2)
			}
		})
		multipleDisks.forEach[d|log.debug("###Class: " + d.fqn + "	Radius:" + d.radius)]
		multipleDiskSegments.forEach[d|log.debug("###Method: " + d.fqn + "	Radius:" + d.outerRadius)]

		return actions.join(",\n")
	}

	def toDiskEvents(HashMap<String, ArrayList<String>> timestamps, List<String> allTimestamps) {
		var actions = new ArrayList<String>()
		var time = startTime
		for (String timestamp : allTimestamps) {
			for (String tmpNodeId : timestamps.get(timestamp)) {
				val nodeId = tmpNodeId
				actions +=
					'''{"time" : «time», "action" : "changeScale", "parameters":{"count": 1, "node_id": "«nodeId»_SCALE", "duration":"«diskVersionAnimationSpeed»", "target_value":"1.0 1.0 1.0" }}'''
			}
			time += segmentVersionAnimationSpeed + 200
			for (String tmpNodeId : timestamps.get(timestamp)) {
				val nodeId = tmpNodeId
				actions +=
					'''{"time" : «time», "action" : "changeScale", "parameters":{"count": 1, "node_id": "«nodeId»_SCALE", "duration":"«diskVersionAnimationSpeed»", "target_value":"0.00001 0.00001 0.00001" }}'''
			}
			time += versionSpeed
		}
		return actions
	}

	def toDiskSegmentEvents(HashMap<String, ArrayList<String>> timestamps, List<String> allTimestamps,
		List<DiskSegment> diskSegments) {
		var actions = new ArrayList<String>()
		var time = startTime
		for (String timestamp : allTimestamps) {
			for (String tmpNodeId : timestamps.get(timestamp)) {
				val diskSegment = diskSegments.findFirst[ds|ds.id == tmpNodeId]
				val nodeId = tmpNodeId
				actions +=
					'''{"time" : «time», "action" : "changeOuterRadius", "parameters":{"count": 1, "node_id": "«nodeId»__RECTANGULARTORUS", "duration":"«segmentVersionAnimationSpeed»", "target_value":"«diskSegment.outerRadius»" }}'''
			}
			time += segmentVersionAnimationSpeed + 200
			for (String tmpNodeId : timestamps.get(timestamp)) {
				val nodeId = tmpNodeId
				actions +=
					'''{"time" : «time», "action" : "changeOuterRadius", "parameters":{"count": 1, "node_id": "«nodeId»__RECTANGULARTORUS", "duration":"«segmentVersionAnimationSpeed»", "target_value":"0.0" }}'''
			}
			time += versionSpeed
		}
		return actions
	}
}
