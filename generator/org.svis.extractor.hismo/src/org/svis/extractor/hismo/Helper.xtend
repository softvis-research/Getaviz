package org.svis.extractor.hismo

import java.util.ArrayList
import java.util.Collections
import org.svis.lib.repository.repo.api.Commit
import org.svis.lib.repository.repo.api.Operation
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.hismo.HISMOAttributeHistory
import org.svis.xtext.hismo.HISMOAttributeVersion
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMOMethodHistory
import org.svis.xtext.hismo.HISMOMethodVersion
import org.svis.xtext.hismo.HISMONamespaceHistory
import org.svis.xtext.famix.impl.FamixFactoryImpl

//TODO: Refactoring

public class Helper {
	val static famixFactory = new FamixFactoryImpl()
	
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
		if(r === null) {
			throw new NullPointerException("Reference ist 'null'")
		}		
		return r
	}
	
	def createReference(HISMOClassHistory he) {
		if(he === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = he
		if(r === null) {
			throw new NullPointerException("Reference ist 'null'")
		}		
		return r
	}

	def createReference(HISMOMethodHistory he) {
		if(he === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = he
		if(r === null) {
			throw new NullPointerException("Reference ist 'null'")
		}		
		return r
	}
	
	def createReference(HISMOAttributeHistory he) {
		if(he === null){
			throw new NullPointerException("createReference wurde 'null' übergeben")
		}
		val r = famixFactory.createIntegerReference
		r.ref = he
		if(r === null) {
			throw new NullPointerException("Reference ist 'null'")
		}		
		return r
	}
	
//Helper for Measurements
	def averageNumberOfStatements(Iterable<HISMOMethodVersion> methods){
		var sum = 0
		var result = 0
		
		for (method : methods) {
			val famixMethod = method.versionEntity as FAMIXMethod
			sum += famixMethod.numberOfStatements
			println("methode: "+famixMethod.value+": Zeilen:"+famixMethod.numberOfStatements)
		}				
		if(methods.size > 0){
			result = sum/methods.size
		}
		return result
	}
	
	def evolutionNumberOfStatements(Iterable<HISMOMethodVersion> methods){
		//Sum of absolute Changes in subsequent Versions
		var result =0
		for(method : methods){
			if(method.evolutionNumberOfStatements < 0){
				result += -1 * method.evolutionNumberOfStatements
			} else {
				result += method.evolutionNumberOfStatements
			}
		}
		return result
	}	
			
	def calcEvolutionNumberOfStatements(HISMOMethodVersion method, HISMOMethodHistory history){
		if(history.methodVersions === null || history.methodVersions.length == 0){
			throw new Exception("Die Auflistung darf nicht null sein")
		}	
		
		val	thisVersion = history.methodVersions.filter[it.ref.name == method.name].head //TODO war davorrank version name und nach Rank gefiltert
		val prevVersion = ((history.methodVersions.filter[(it.ref as HISMOMethodVersion).timestamp == thisVersion].head.ref as HISMOMethodVersion).versionEntity as FAMIXMethod).numberOfStatements 
		
		return prevVersion - (method.versionEntity as FAMIXMethod).numberOfStatements
	}
	
	def calcMinMaxEvolution(HISMOMethodHistory hist) {
		val temp = new ArrayList<Integer>()
		var evolutionSum = 0		
		for(version: hist.methodVersions){		
			temp += (version.ref as HISMOMethodVersion).evolutionNumberOfStatements
			evolutionSum += (version.ref as HISMOMethodVersion).evolutionNumberOfStatements
		}
		hist.maxNumberOfStatements = Collections.max(temp)
		hist.minNumberOfStatements = Collections.min(temp)		
		hist.evolutionNumberOfStatements = evolutionSum			
	}
	
	def calcEvolution(HISMOClassHistory history){
		//sum of Changes of whole History
		var temp = new ArrayList<Integer>()
		for( hist: history.classVersions){
			for(meth: (hist.ref as HISMOClassVersion).methodVersions){
				temp += (meth.ref as HISMOMethodVersion).evolutionNumberOfStatements	
			}
		}
		var sum = 0
		for(i:temp){
			sum += i
		}
		
		history.evolutionNumberOfStatements = sum
	}
	
//Helper to identify modified elements from Repo/Commit
	def isModified(FAMIXClass cla, Commit commit){
	 	if(cla === null || commit === null){
			return false
		}
		var result = false
		val diffs = commit.diff.diffs
		//if(diffs === null)
		//result = false
		//unschöner Hack, damit der Initiale Commit (ohne Unterschiede zur Vorversion) auch als Version hinzugefügt wird
		if(diffs.size == 0) {
			return true
		}
	 	for(entry: diffs.entrySet){	
			//die folgende if ist nur ein hack, da muss noch geprüft werden, warum cla.type manchmal null ist	
			if(cla.type === null || cla.type.ref === null || entry === null || entry.value === null || entry.value.newPath === null){
				return false
			} else {
				if((cla.type.ref as FAMIXFileAnchor).filename.replace("\\","/").endsWith(entry.value.newPath)){
					if(entry.value.typeOfOperation.equals(Operation.ChangeOperation.MODIFY)){
						//println("~~~geänderte Klassen in Commit "+commit.commitId.idRepresentation)
//						println("* "+cla.qualifiedName)
						return true
					}
					if(entry.value.typeOfOperation.equals(Operation.ChangeOperation.ADD)){
						//println("~~~hinzugefügte Klassen in Commit "+commit.commitId.idRepresentation)
//						println("* "+cla.qualifiedName)
						return true
					}
					if(entry.value.typeOfOperation.equals(Operation.ChangeOperation.RENAME)){
						return true
					}
					if(entry.value.typeOfOperation.equals(Operation.ChangeOperation.COPY)){
						return true
					}				
				} else if((cla.type.ref as FAMIXFileAnchor).filename.replace("\\","/").endsWith(entry.value.oldPath)) {
					if(entry.value.typeOfOperation.equals(Operation.ChangeOperation.DELETE)){
						//println("~~~gelöschte Klassen in Commit "+commit.commitId.idRepresentation)
						//println("* "+cla.qualifiedName)
						return true
					}
				}
			}
		}
		return result
	}
	
	def String qualifiedName(HISMONamespaceHistory hismoNamespaceHistory) {
		if (hismoNamespaceHistory.containingNamespaceHistory !== null){
			return (hismoNamespaceHistory.containingNamespaceHistory.ref as HISMONamespaceHistory).qualifiedName + "." + hismoNamespaceHistory.value
		} else {
			hismoNamespaceHistory.value //TODO war mal der value
		}
	}
	
	def String qualifiedName(HISMOClassHistory hismoClassHistory) {
		if (hismoClassHistory.containingNamespaceHistory !== null){
			return ((hismoClassHistory.containingNamespaceHistory.ref as HISMONamespaceHistory)).qualifiedName + "." + hismoClassHistory.value
		} else {
			hismoClassHistory.value
		}
	}
	
	def String qualifiedName(HISMOMethodHistory hismoMethodHistory) {
		if (hismoMethodHistory.containingClassHistory !== null){
			return (hismoMethodHistory.containingClassHistory.ref as HISMOClassHistory).qualifiedName + "." + hismoMethodHistory.value
		} else {
			hismoMethodHistory.value
		}
	}
	
	def String qualifiedName(HISMOAttributeHistory hismoAttributeHistory) {
		if (hismoAttributeHistory.containingClassHistory !== null){
			return (hismoAttributeHistory.containingClassHistory.ref as HISMOClassHistory).qualifiedName + "." + hismoAttributeHistory.value
		} else {
			hismoAttributeHistory.value
		}
	}
	
}