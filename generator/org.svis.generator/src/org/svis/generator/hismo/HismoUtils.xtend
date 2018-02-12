package org.svis.generator.hismo

import org.apache.commons.logging.LogFactory
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.xtext.hismo.HISMOMethodHistory
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMOMethodVersion
import org.svis.xtext.hismo.HISMOAttributeHistory
import org.svis.xtext.hismo.HISMOAttributeVersion
import org.svis.xtext.hismo.HISMONamespaceHistory
import org.svis.generator.FamixUtils

class HismoUtils {
	val log = LogFactory::getLog(class)
	extension FamixUtils util = new FamixUtils
	
	def String qualifiedNameMultiple(HISMOMethodHistory hismoMethodHistory) {
		val ref = hismoMethodHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoMethodHistory.methodVersions.nullOrEmpty) {
			return qualifiedNameMultiple(ref) + "." + hismoMethodHistory.name
		} else {
			val firstMethodVersion = hismoMethodHistory.methodVersions.get(0).ref as HISMOMethodVersion
			return qualifiedNameMultiple(ref) + "." + ("" + firstMethodVersion.value).removeApostrophes 
		}
	}
	
	def String qualifiedNameMultiple(HISMOAttributeHistory hismoAttributeHistory) {
		val ref = hismoAttributeHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoAttributeHistory.attributeVersions.length > 0) {
			val firstAttributeVersion = hismoAttributeHistory.attributeVersions.get(0).ref as HISMOAttributeVersion
			return qualifiedNameMultiple(ref) + "." + ("" + firstAttributeVersion.value).removeApostrophes
		} else {
			return qualifiedNameMultiple(ref) + "." + hismoAttributeHistory.name 
		}
	}
	
	def String qualifiedNameMultiple(HISMONamespaceHistory hismoNamespaceHistory) {
		if (hismoNamespaceHistory.containingNamespaceHistory !== null) {
			val ref = hismoNamespaceHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return qualifiedNameMultiple(ref) + "." + ("" + firstNamespaceVersion.value).removeApostrophes
			} else {
				return qualifiedNameMultiple(ref) + "." + hismoNamespaceHistory.name 
			}	
		} else {
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return ("" + firstNamespaceVersion.value).removeApostrophes 	
			} else {
				return "unknown"  + "+++"
			}
		}
	}
	
	def String qualifiedNameMultiple(HISMOClassHistory hismoClassHistory) {
		if (hismoClassHistory.containingNamespaceHistory !== null) {
			if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMONamespaceHistory) {
				val ref = hismoClassHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
				if (hismoClassHistory.classVersions.length > 0) {
					val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
					return qualifiedNameMultiple(ref) + "." + ("" + firstClassVersion.value).removeApostrophes 
				} else {
					return qualifiedNameMultiple(ref) + "." + hismoClassHistory.name 
				}
			} else if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMOClassHistory) {
				val ref = hismoClassHistory.containingNamespaceHistory.ref as HISMOClassHistory
				//log.info(ref)
				if (hismoClassHistory.classVersions.length > 0) {
					val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
					return qualifiedNameMultiple(ref) + "." + ("" + firstClassVersion.value).removeApostrophes 
				} else {
					return qualifiedNameMultiple(ref) + "." + hismoClassHistory.name 
				}
			}
		} else {
			return 'root'
		}
	}
	
	def String qualifiedName(HISMOMethodHistory hismoMethodHistory) {
		val ref = hismoMethodHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoMethodHistory.methodVersions.length > 0) {
			val firstMethodVersion = hismoMethodHistory.methodVersions.get(0).ref as HISMOMethodVersion
			return qualifiedName(ref) + "." + ("" + firstMethodVersion.value).removeApostrophes + "+++<" + hismoMethodHistory.name + ">+++"
		} else {
			return qualifiedName(ref) + "." + hismoMethodHistory.name + "+++"
		}
	}
	
	def String qualifiedName(HISMOAttributeHistory hismoAttributeHistory) {
		val ref = hismoAttributeHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoAttributeHistory.attributeVersions.length > 0) {
			val firstAttributeVersion = hismoAttributeHistory.attributeVersions.get(0).ref as HISMOAttributeVersion
			return qualifiedName(ref) + "." + ("" + firstAttributeVersion.value).removeApostrophes + "+++<" + hismoAttributeHistory.name + ">+++"
		} else {
			return qualifiedName(ref) + "." + hismoAttributeHistory.name + "+++"
		}
	}
	
	def String qualifiedName(HISMONamespaceHistory hismoNamespaceHistory) {
		if (hismoNamespaceHistory.containingNamespaceHistory !== null) {
			val ref = hismoNamespaceHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return qualifiedName(ref) + "." + ("" + firstNamespaceVersion.value).removeApostrophes + "+++<" + hismoNamespaceHistory.name + ">+++"
			}
			else {
				return qualifiedName(ref) + "." + hismoNamespaceHistory.name + "+++"
			}
				
		} else {
			
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return ("" + firstNamespaceVersion.value).removeApostrophes + "+++<" + hismoNamespaceHistory.name + ">+++"	
			} else {
				return "unknown"  + "+++"
			}
		}
	}
	
	def String qualifiedName(HISMOClassHistory hismoClassHistory) {
		if (hismoClassHistory.containingNamespaceHistory !== null) {
			if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMONamespaceHistory){
				var ref = hismoClassHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
				if (hismoClassHistory.classVersions.length > 0) {
					
				val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
				return qualifiedName(ref) + "." + ("" + firstClassVersion.value).removeApostrophes + "+++<" +
					hismoClassHistory.name + ">+++"
				} else {
					return qualifiedName(ref) + "." + hismoClassHistory.name + "+++"
				}
			} else if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMOClassHistory) {
				var ref = hismoClassHistory.containingNamespaceHistory.ref as HISMOClassHistory
					// log.info(ref)
				if (hismoClassHistory.classVersions.length > 0) {
				val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
				return qualifiedName(ref) + "." + ("" + firstClassVersion.value).removeApostrophes + "+++<" +
					hismoClassHistory.name + ">+++"
				} else {
					return qualifiedName(ref) + "." + hismoClassHistory.name + "+++"
				}
			} else {
				return 'root'
			}
			
		}
	}
}