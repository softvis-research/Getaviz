package org.svis.generator.city.m2t

import java.util.List
import java.util.ArrayList
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import java.util.TreeMap
import org.svis.xtext.city.Building
import org.svis.xtext.city.Invocation

class City2JS {
	val log = LogFactory::getLog(class)
	var long minTime
	
	def toJSBody(Resource resource) {
		log.info("City2JS has started")
		val actionsList = new ArrayList<String> 
		val Invocations = EcoreUtil2::getAllContentsOfType(resource.contents.head, Invocation)
		
		actionsList += testInvocations(diskSegmentInvocations(250,3600000.0,Invocations))
		actionsList.removeAll("")
		log.info("City2JS has finished")
		
		return actionsList.join(",\n")
		
	}
	
	def private testInvocations(List<Invocation> invocations) {
		val actions = new ArrayList<String>()
		for (i: invocations.sortBy[start]) {
			actions.add('''{"time" : «i.start», "action" : "changeScale", "parameters":{"node_id": "«(i.eContainer as Building).id»", "type": "show", "only_y": "true" , "duration":"10" ,"target_value": "1.0 «((i.stop - i.start)/30)» 1.0" }}''')
			actions.add('''{"time" : «i.start», "action" : "changeTransparency", "parameters":{"node_id": "«(i.eContainer as Building).id»_MATERIAL", "duration":"10" ,"target_value": "0.5" }}''')
			actions.add('''{"time" : «i.stop», "action" : "changeTransparency", "parameters":{"node_id": "«(i.eContainer as Building).id»_MATERIAL", "duration":"10" ,"target_value": "0.0" }}''')
			actions.add('''{"time" : «i.stop», "action" : "changeScale", "parameters":{"node_id": "«(i.eContainer as Building).id»", "type": "show", "only_y": "true" , "duration":"10" ,"target_value": "1.0 1.0 1.0"}}''')
		}	
		actions.join(",\n")
	}
	
	def diskSegmentInvocations(int timePerAnimation, double targetTime, List<Invocation> diskSegmentInvocations) {
		minTime = diskSegmentInvocations.sortBy[start].get(0).start
		diskSegmentInvocations.forEach[
			start = start - minTime
			stop = stop - minTime
		]
		var maxTime = diskSegmentInvocations.sortBy[stop].last.stop
		val timemultiplier = targetTime / maxTime
		
		// logarithmic scaling
		var sortedMap = new TreeMap()
		sortedMap.put(0 as long, 0 as long)
		
		for (i: diskSegmentInvocations) {
			sortedMap.put(i.start, i.start)
			sortedMap.put(i.stop, i.stop)
		}
		var offset = 0.0
		var lastEntry = sortedMap.firstEntry
		for (entry: sortedMap.entrySet()) {
			var key = entry.key.longValue
			var lastKey = lastEntry.key.longValue
			var value = (Math::log10(key - lastKey + 1) + 1) + offset
			offset = value + 2*(timePerAnimation/timemultiplier)
			entry.value = (offset) as long
		}
		for (entry: sortedMap.entrySet()) {
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