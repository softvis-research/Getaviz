package org.svis.extractor.hismo

import java.util.Collections
import java.util.HashMap
import java.util.List
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import org.svis.lib.repository.repo.api.Commit
import org.svis.lib.repository.repo.api.Repository
import org.svis.generator.FamixUtils
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXElement
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.hismo.HISMOAttributeHistory
import org.svis.xtext.hismo.HISMOAttributeVersion
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMOMethodHistory
import org.svis.xtext.hismo.HISMOMethodVersion
import org.svis.xtext.hismo.HISMONamespaceHistory
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.xtext.hismo.impl.HismoFactoryImpl
import org.svis.generator.WorkflowComponentWithConfig
import org.apache.commons.beanutils.BeanComparator
import org.eclipse.emf.common.util.ECollections
import org.svis.xtext.hismo.HismoDocument
import org.svis.xtext.famix.impl.FamixFactoryImpl
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import java.util.ArrayList
import java.util.Comparator
import org.svis.xtext.famix.FAMIXStructure

class Famix2Hismo extends WorkflowComponentWithConfig {
	
	val hismoFactory = new HismoFactoryImpl()
	var org.svis.xtext.famix.Document famixDoc
	var HismoDocument hismoDocument
	var i = 0
	var Commit currentCommit
	extension FamixUtils util = new FamixUtils
	extension Helper helper = new Helper
	val static famixFactory = new FamixFactoryImpl()
	val resource = new ResourceImpl()
	val List<HISMOClassHistory> savedhistories = newArrayList
	val List<HISMOMethodHistory> savedMethodHistories = newArrayList
	val List<HISMOAttributeHistory> savedAttributeHistories = newArrayList
	
	override invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2Hismo has started.")
		if (!config.recreateFamix) {
			log.info("According to the configuration, no famix models are recreated.")
			return
		}
		// list with all famix models
		val famixResources = ctx.get("famix") as List<LazyLinkingResource>
		val famixResourceMap = new HashMap<String, LazyLinkingResource>
		for (resource : famixResources) {
			famixResourceMap.put(resource.URI.lastSegment, resource)
		}
		// init target model hismo
		var hismoRoot = hismoFactory.createHismoRoot
		hismoDocument = hismoFactory.createHismoDocument
		hismoRoot.hismoDocument = hismoDocument
		val repository = ctx.get("repository") as Repository
		val orderedCommits = repository.defaultBranch.getCommits(20)
		Collections::sort(orderedCommits)
		Collections::reverse(orderedCommits)

		for (commit : orderedCommits) {
			
			val key = commit.commitId.idRepresentation
			
			if (!famixResourceMap.containsKey(key + ".famix")) {
				log.warn("No famix model for commit " + key + " exists.")
			} else {
				// process famix document
				var resource = famixResourceMap.get(key + ".famix")
				resource.load(null)
				while (resource.loading) {
					log.info("Resource " + resource.URI + " is loading.")
				}
				if (resource.isLoaded) {
					log.info("Resource " + resource.URI + " loaded")
					famixDoc = (resource.contents.head as org.svis.xtext.famix.Root).document
					if (famixDoc === null) {
						log.error("Famix model is null: " + resource.URI)
					}
				} else {
					log.error("Resource " + resource.URI + " not loaded.")
				}
				currentCommit = commit
				//gets the highest ID of all famixElements in all commits
				if(i==0) {
					for(com: orderedCommits) {
				  		ECollections::sort(famixDoc.elements, new Comparator<FAMIXElement>() {
							override compare(FAMIXElement element1, FAMIXElement element2) {
								val e1n = Integer::parseInt(element1.name)
								val e2n = Integer::parseInt(element2.name)
								if(e1n > e2n) {
									return e1n
								} else if(e2n > e1n) {
									return e2n * -1
								} else {
									return 0
								}
							}
						})
				  		var famixID = Integer::parseInt(famixDoc.elements.get(famixDoc.elements.size -1).name)
						if(famixID > i) {
							i = famixID
						}
					}
					i++
				}
				var HISMOClassVersion currentClassVersion
				var HISMOMethodVersion currentMethodVersion
				var HISMOAttributeVersion currentAttributeVersion

				if (famixDoc.elements === null || famixDoc.elements.filter(FAMIXClass) === null) {
				// ||currentCommit.commitId.idRepresentation ==)
					log.error("No Elements found.")
				}
				var famixStructures = famixDoc.elements.filter(FAMIXStructure).sortBy[fqn]			
				for (structure : famixStructures) {
					var famixMethods = famixDoc.elements.filter(FAMIXMethod).filter[parentType.ref.equals(structure)].sortBy[fqn] // ref as FAMIXElement
					for(method : famixMethods) { method.name = method.name + i++ }
					var famixAttributes = famixDoc.elements.filter(FAMIXAttribute).filter [parentType.ref.equals(structure)].sortBy[fqn] // ref as FAMIXElement								currentClassHistory = class.GetHismoHistoryByFamixObj as HISMOClassHistory
					var famixNamespaces = famixDoc.elements.filter(FAMIXNamespace).filter [structure.container.ref.name.equals(it.name)].sortBy[fqn] // ref as FAMIXElement	
					var HISMOClassHistory currentClassHistory
					
					if (currentClassHistory === null) {
						var oldHistory = savedhistories.findFirst[value.equals(structure.fqn)]
						if(oldHistory !== null){
							currentClassHistory = oldHistory
						} else {
							currentClassHistory = structure.toHismoHistory
						}	
					}
					if(currentClassHistory.containingNamespaceHistory.ref instanceof HISMONamespaceHistory) {
				 		val actualNSHistory = currentClassHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
							famixNamespaces.forEach[
							toHismoVersion(it,actualNSHistory)
						]
					}
					savedhistories += currentClassHistory
					currentClassVersion = structure.toHismoVersion(currentClassHistory)
					currentClassHistory.createAndAddVersionWithHighestRank(currentClassVersion)	
					
					for (method : famixMethods) {
						var HISMOMethodHistory currentMethodHistory
						currentMethodHistory = method.hismoHistoryByFamixObj as HISMOMethodHistory
						// methodHistory erzeugen
						if (currentMethodHistory === null) {
							var oldMethodHistory = savedMethodHistories.findFirst[value.equals(method.fqn)]
							if(oldMethodHistory !== null) {
								currentMethodHistory = oldMethodHistory
							} else {
								currentMethodHistory = method.toHismoHistory(currentClassHistory)
							}
						}
						savedMethodHistories += currentMethodHistory
						currentClassHistory.methodHistories += currentMethodHistory.createReference
						currentMethodVersion = method.toHismoVersion(currentMethodHistory)
						// add version to referencing history
						if (currentMethodVersion !== null) {
							currentMethodHistory.createAndAddVersionWithHighestRank(currentMethodVersion)
							currentClassVersion.methodVersions += currentMethodVersion.createReference
							currentMethodVersion.parentHistory = currentMethodHistory.createReference
						}
					}
					
					for (attribute : famixAttributes) {
						var HISMOAttributeHistory currentAttributeHistory
						currentAttributeHistory = attribute.hismoHistoryByFamixObj as HISMOAttributeHistory
						if (currentAttributeHistory === null) {
							var oldAttributeHistory = savedAttributeHistories.findFirst[value.equals(attribute.fqn)]
							if(oldAttributeHistory !== null) {
								currentAttributeHistory = oldAttributeHistory
							} else {
								currentAttributeHistory = attribute.toHismoHistory(currentClassHistory)
							}
						}
						savedAttributeHistories += currentAttributeHistory							
						currentClassHistory.attributeHistories += currentAttributeHistory.createReference
						currentAttributeVersion = attribute.toHismoVersion(currentAttributeHistory)
							currentAttributeHistory.createAndAddVersionWithHighestRank(currentAttributeVersion)
							currentClassVersion.attributeVersions += currentAttributeVersion.createReference
							currentAttributeVersion.parentHistory = currentAttributeHistory.createReference
						
					}
				}

				resource.unload
			}
		}
		
		// calc average min max for MethodHistories
		hismoDocument.elements.filter(HISMOMethodHistory).forEach [mh|
			val methodVersionsNOS = new ArrayList
			mh.methodVersions.forEach[mv|
				val version = mv.ref as HISMOMethodVersion
				val famixVersionNOS = (version.versionEntity.ref as FAMIXMethod).numberOfStatements
				methodVersionsNOS += famixVersionNOS
			]	
			Collections::sort(methodVersionsNOS)
			val minStatements = methodVersionsNOS.get(0)
			val maxStatements = methodVersionsNOS.get(methodVersionsNOS.size() - 1)
			mh.maxNumberOfStatements = maxStatements
			mh.minNumberOfStatements = minStatements
		]
		
		//calc evolution of methodVersions
		hismoDocument.elements.filter(HISMOMethodHistory).forEach [mh|		
			mh.methodVersions.forEach[mv|
				(mv.ref as HISMOMethodVersion).evolutionNumberOfStatements = ((mv.ref as HISMOMethodVersion).versionEntity.ref as FAMIXMethod).numberOfStatements]
		]
		hismoDocument.elements.filter(HISMOClassHistory).forEach [c|
			c.calcEvolution
			c.setLastAuthor
		]
		val comparator = new BeanComparator("name")
		//hismoDocument.elements.sortInplace(comparator)
		ECollections::sort(hismoDocument.elements, comparator)

		// put hismoRoot into list (for writer)
		var hismoList = newArrayList
		hismoList += hismoRoot
		resource.contents += hismoList
		ctx.set("hismoextended", resource)
		ctx.set("hismowriter", hismoList)
		log.info("Famix2Hismo finished")
	}
	


	def getHismoHistoryByFamixObj(FAMIXElement famixElement) {
		switch famixElement {
			FAMIXAttribute:	return hismoDocument.elements.filter(HISMOAttributeHistory).findFirst[value.equals(famixElement.fqn)]
			FAMIXMethod: 	return hismoDocument.elements.filter(HISMOMethodHistory).findFirst[value.equals(famixElement.fqn)]
			FAMIXNamespace: return hismoDocument.elements.filter(HISMONamespaceHistory).findFirst [value.equals(famixElement.fqn)]
			FAMIXClass:		return hismoDocument.elements.filter(HISMOClassHistory).findFirst [value.equals(famixElement.fqn)]
		}
	}
	
	/**
	 * Crease Class History from FAMIXClass
	 */
	 
	def HISMOClassHistory toHismoHistory(FAMIXStructure st) {
		val hismoclasshistory = hismoFactory.createHISMOClassHistory 
		hismoclasshistory.value = st.fqn
		hismoclasshistory.name = (i++).toString

		var HISMONamespaceHistory actualNamespaceHistory
		// create NamespaceHistory
		val famixelement = st.container.ref
		if (famixelement instanceof FAMIXNamespace) {
			actualNamespaceHistory = hismoDocument.elements.filter(HISMONamespaceHistory).findFirst [
				value.equals(famixelement.fqn)
			]
			if (actualNamespaceHistory === null) {
				actualNamespaceHistory = famixelement.toHismoHistory
			}
			// set classHistory as child
			actualNamespaceHistory.classHistories += hismoclasshistory.createReference
	
			// set hierachical properties
			hismoclasshistory.containingNamespaceHistory = actualNamespaceHistory.createReference
		}

		if (famixelement instanceof FAMIXClass) {
			val famixclass = famixelement
			var classHistory = famixclass.hismoHistoryByFamixObj as HISMOClassHistory
			if (classHistory === null) {
				classHistory = famixclass.toHismoHistory
			}		
			hismoclasshistory.containingNamespaceHistory = classHistory.createReference
			
			//	hismoclasshistory.containingNamespaceHistory = (classHistory.containingNamespaceHistory.
			//			ref as HISMONamespaceHistory).createReference
		}
		
		hismoDocument.elements += hismoclasshistory
		return hismoclasshistory
	}

	/**
	 * Creates Namespace History from FAMIXNamespace
 	*/
 	
	def HISMONamespaceHistory toHismoHistory(FAMIXNamespace fns) {
		val hismonamespacehistory = hismoFactory.createHISMONamespaceHistory 
		hismonamespacehistory.value = fns.fqn
		hismonamespacehistory.name = (i++).toString
		// create parent namespace, if there is one
		var HISMONamespaceHistory actualNamespaceHistory
		if (fns.parentScope !== null) {
			actualNamespaceHistory = hismoDocument.elements.filter(HISMONamespaceHistory).findFirst [
				value.equals(fns.parentScope.ref.fqn)
			]
			if (actualNamespaceHistory === null) {
				actualNamespaceHistory = (fns.parentScope.ref as FAMIXNamespace).toHismoHistory
			}
			hismonamespacehistory.containingNamespaceHistory = actualNamespaceHistory.createReference
		} 
		hismoDocument.elements += hismonamespacehistory
		return hismonamespacehistory
	}
	
	/**
	 * Creates MethodHistory from FAMIXMethod
	 */

	def HISMOMethodHistory toHismoHistory(FAMIXMethod mt, HISMOClassHistory classhistory) {
		val hismomethodhistory = hismoFactory.createHISMOMethodHistory 
		hismomethodhistory.value = mt.fqn
		hismomethodhistory.containingClassHistory = classhistory.createReference
		hismomethodhistory.name = (i++).toString
		hismomethodhistory.signature = mt.signature

		hismoDocument.elements += hismomethodhistory
		return hismomethodhistory
	}

	/**
	 * Creates Attribute History from FAMIXAttribute
	 */
	 
	def HISMOAttributeHistory toHismoHistory(FAMIXAttribute at, HISMOClassHistory classhistory) {
		val hismoattributehistory = hismoFactory.createHISMOAttributeHistory
		hismoattributehistory.value = at.fqn
		hismoattributehistory.containingClassHistory = classhistory.createReference

		hismoattributehistory.name = (i++).toString

		hismoDocument.elements += hismoattributehistory
		return hismoattributehistory
	}

	/**
	 * Creates ClassVersion from ClassHistory
	 */
	def HISMOClassVersion toHismoVersion(FAMIXStructure st, HISMOClassHistory classhistory) {
		val hismoclassversion = hismoFactory.createHISMOClassVersion 
		hismoclassversion.timestamp = currentCommit.timestamp.toString
		hismoclassversion.commitId = currentCommit.commitId.idRepresentation
		hismoclassversion.name = (i++).toString
		switch(st) {
			FAMIXClass: hismoclassversion.scc = st.scc
		}
		hismoclassversion.value = st.fqn + "." + hismoclassversion.name

		// set additional Information from Versioning, like author
		hismoclassversion.author = currentCommit.author.name
//		hismoclassversion.parentHistory = classhistory.createReference
		// set Snapshot as VersionEntity
		val ph = famixFactory.createIntegerReference
		ph.ref = classhistory
		hismoclassversion.parentHistory = ph
		val r = famixFactory.createIntegerReference
		r.ref = st
		hismoclassversion.versionEntity = r

		// add Element to elements collection
		hismoDocument.elements += hismoclassversion
		hismoDocument.elements += st
		return hismoclassversion
	}

	/**
	 * Creates NamespaceVersion from NamespaceHistory
	 */
	def HISMONamespaceVersion toHismoVersion(FAMIXNamespace fns, HISMONamespaceHistory history) {
		val hismonamespaceversion = hismoFactory.createHISMONamespaceVersion 
		hismonamespaceversion.timestamp = currentCommit.timestamp.toString
		hismonamespaceversion.commitId = currentCommit.commitId.idRepresentation
		hismonamespaceversion.name = (i++).toString

		// set additional Information from Versioning, like author
		hismonamespaceversion.author = currentCommit.author.name
		hismonamespaceversion.value = fns.fqn + "." + hismonamespaceversion.name
		val ph = famixFactory.createIntegerReference
		ph.ref = history
		hismonamespaceversion.parentHistory = ph
		val r = famixFactory.createIntegerReference
		r.ref = fns
		hismonamespaceversion.versionEntity = r

		// add Element to elements collection
		hismoDocument.elements += hismonamespaceversion
		hismoDocument.elements += fns
		return hismonamespaceversion
	}

	/**
	 * Creates MethodVersion from MethodHistory
	 */
	 
	def HISMOMethodVersion toHismoVersion(FAMIXMethod mt, HISMOMethodHistory methodhistory) {
			val hismomethodversion = hismoFactory.createHISMOMethodVersion
			hismomethodversion.timestamp = currentCommit.timestamp.toString
			hismomethodversion.commitId = currentCommit.commitId.idRepresentation
			hismomethodversion.name = (i++).toString
			// set additional Information from Versioning, like author
			hismomethodversion.author = currentCommit.author.name
			hismomethodversion.value = hismomethodversion.name + "-" + mt.fqn
			val ph = famixFactory.createIntegerReference
			ph.ref = methodhistory
			hismomethodversion.parentHistory = ph
			
			// set Snapshot as VersionEntity
			val r = famixFactory.createIntegerReference
			r.ref = mt
			hismomethodversion.versionEntity = r

			hismoDocument.elements += hismomethodversion
			hismoDocument.elements += mt
			return hismomethodversion
	}

	/**
	 * Creates Attribute Version from AttributeHistory
	 */

	def HISMOAttributeVersion toHismoVersion(FAMIXAttribute at, HISMOAttributeHistory attributehistory) {
		val hismoattributeversion = hismoFactory.createHISMOAttributeVersion 
		hismoattributeversion.timestamp = currentCommit.timestamp.toString
		hismoattributeversion.commitId = currentCommit.commitId.idRepresentation
		hismoattributeversion.name = (i++).toString

		// set additional Information from Versioning, like author
		hismoattributeversion.author = currentCommit.author.name
		hismoattributeversion.value = at.fqn + "." + hismoattributeversion.name
		val ph = famixFactory.createIntegerReference
		ph.ref = attributehistory
		hismoattributeversion.parentHistory = ph

		// set Snapshot as VersionEntity
		val r = famixFactory.createIntegerReference
		r.ref = at
		hismoattributeversion.versionEntity = r

		hismoDocument.elements += hismoattributeversion
		hismoDocument.elements += at
		return hismoattributeversion
	}
	
	/**
	 * Calculate Number of Statements of predecessors method
	 */

	def calcEvolutionNumberOfStatements(HISMOMethodVersion method, HISMOMethodHistory history) {
		var result = 0
		if (history.methodVersions === null || history.methodVersions.length == 0) {
			return result
		}
		if (history.methodVersions.length == 1) {
			return 0
			// TODO: das ist hier Ansichtssache, ob die gesamten statements die evolution vom null-Zustand darstellen, oder eben 0 
			// return ((history.versions.filter(typeof(HISMORank)).head.version.ref as HISMOMethodVersion).versionEntity as FAMIXMethod).numberOfStatements
		}
		val prevVersion = (history.methodVersions.sortBy[(it.ref as HISMOMethodVersion).timestamp].head.ref as HISMOMethodVersion) // .filter[it.version.ref.name == method.name]
		val prevVersionEntity = prevVersion.versionEntity.ref as FAMIXMethod
		val currentVersionEntity = method.versionEntity as FAMIXMethod

		return prevVersionEntity.numberOfStatements - currentVersionEntity.numberOfStatements
	}
	
	/**
	 * Calculate Average Number of Statements for Method
	 * 
	 * @param history	MethodHistoryy, representing the complete history of a FAMIX Method
	 */

	def void calcAverageNOS(HISMOMethodHistory history) {
		var int temp
		for (v : history.methodVersions) {
			println(((v.ref as HISMOMethodVersion).versionEntity.ref as FAMIXMethod).numberOfStatements)
			temp += ((v.ref as HISMOMethodVersion).versionEntity.ref as FAMIXMethod).numberOfStatements
		}
		history.averageNumberOfStatements = temp / history.methodVersions.length
	}
	
	/**
	 * Set last author of ClassHistory from last version of class
	 * 
	 * @param history ClassHistory, representing the complete history of a FAMIX Class
	 */

	def void setLastAuthor(HISMOClassHistory history) {
		if (history.classVersions !== null && history.classVersions.length != 0) {
			history.lastAuthor = (history.classVersions.sortBy[(it.ref as HISMOClassVersion).timestamp].head.ref as HISMOClassVersion).author
		}
	}
}