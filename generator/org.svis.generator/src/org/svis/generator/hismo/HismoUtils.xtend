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
import org.svis.xtext.famix.impl.FamixFactoryImpl

class HismoUtils {
	val log = LogFactory::getLog(class)
	val static famixFactory = new FamixFactoryImpl()
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
	
	//Gibt den höchsten vergebenen Rang einer History zurück
	def getHighestRank(HISMOClassHistory he){
		if(he.classVersions === null || he.classVersions.length == 0){
			return 0
		} else {
			return he.classVersions.sortBy[(it.ref as HISMOClassVersion).timestamp]			
		}
	}
	 
	 //gib einen neuen Rang für eine Version zurück (+1)
	def createAndAddVersionWithHighestRank(HISMOClassHistory he, HISMOClassVersion ve) {
	 //	val r = hismoFactory.createHISMORank
	 //	r.rank = he.highestRank + 1	 	
	 //	r.version = ve.createReference
	 	he.classVersions += ve.createReference
	 	return 
	}
	
	def createAndAddVersionWithHighestRank(HISMOMethodHistory me,HISMOMethodVersion ve) {
		me.methodVersions += ve.createReference
	}
	
	def createAndAddVersionWithHighestRank(HISMOAttributeHistory ae, HISMOAttributeVersion ve) {
		ae.attributeVersions += ve.createReference
	}
	
	def createReference(HISMOClassVersion ve){
		if(ve === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = ve
		return r	
	}
	
	def createReference(HISMONamespaceVersion ve){
		if(ve === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = ve
		return r	
	}
	
	def createReference(HISMOMethodVersion ve) {
		if(ve === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = ve
		return r
	}
	
	def createReference(HISMOAttributeVersion ve) {
		if(ve === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = ve
		return r
	}
	
	def createReference(HISMONamespaceHistory he) {
		if(he === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = he	
		return r
	}
	
	def createReference(HISMOClassHistory he) {
		if(he === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = he		
		return r
	}

	def createReference(HISMOMethodHistory he) {
		if(he === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = he
		return r
	}
	
	def createReference(HISMOAttributeHistory he) {
		if(he === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = he
		return r
	}
	
	/**	/sum of Changes of whole History
	 * 
	 */
	
	def calcEvolution(HISMOClassHistory history){
		val sum = history.classVersions.map[ref as HISMOClassVersion].map[methodVersions].reduce[].
		map[ref as HISMOMethodVersion].map[evolutionNumberOfStatements].reduce[a,b | a + b]
		
		history.evolutionNumberOfStatements = sum
	}
}