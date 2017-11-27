package org.svis.generator.rd.m2t

import org.eclipse.emf.ecore.resource.Resource
import org.svis.xtext.rd.Disk
import org.eclipse.xtext.EcoreUtil2
import java.util.ArrayList
import org.apache.commons.logging.LogFactory

class RD2JSON {
	val log = LogFactory::getLog(class)
	
	def toGlyphsJson(Resource resource){
		log.info("RD2GlyphJson has started")
		var disks = EcoreUtil2::getAllContentsOfType(resource.contents.head, Disk)
		val actionsList = new ArrayList<String> 
		
		
		for(disk : disks) {
			actionsList += toGlyphs(disk).toString.replaceAll("},
]","}]")
				
		}
		log.info("RD2GlyphJson has finished")
		return actionsList.join(",\n")
	}
	
	def private toGlyphs(Disk disk)'''
		{
		"ObjectMeta": {
		"className": «disk.name»,
		"attributeWidth": 1.5,
		"methodWidth": 1.5,
		"positionX": «disk.position.x»,
		"positionY": «disk.position.y»,
		"positionZ": «disk.position.z»
		},
		"attributes": [
		«FOR data : disk.data»
		{"id": «data.name»},
		«ENDFOR»
		],
		"methods": [
		«FOR method : disk.methods»
		{"id":«method.name»,"size": «method.size»},
		«ENDFOR»
		]
		}
	'''
	
}