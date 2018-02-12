package org.svis.extractor.hismo

import java.util.Collections
import java.util.HashMap
import java.util.List
import java.io.File
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
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
import org.svis.xtext.famix.FAMIXAntipattern
import org.svis.xtext.famix.FAMIXComponent
import org.svis.xtext.famix.FAMIXRole
import org.svis.xtext.famix.FAMIXPath
import org.svis.generator.famix.Famix2Famix
import java.util.Map
import org.svis.xtext.famix.IntegerReference

class Famix2HismoWithoutRepo extends WorkflowComponentWithConfig {
	val hismoFactory = new HismoFactoryImpl()
	var org.svis.xtext.famix.Document famixDoc
	var HismoDocument hismoDocument
	var i = 0
	var File currentDirectory
	val famix = new Famix2Famix
	extension FamixUtils util = new FamixUtils
	extension Helper helper = new Helper
	val static famixFactory = new FamixFactoryImpl()
	val resource = new ResourceImpl()
	val List<HISMOClassHistory> savedhistories = newArrayList
	val List<HISMOMethodHistory> savedMethodHistories = newArrayList
	val List<HISMOAttributeHistory> savedAttributeHistories = newArrayList
	var index = 10
	val List<FAMIXComponent> allComponents = newArrayList
	val Map<String, HISMOClassVersion> ids = newHashMap
	
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
		
		// Ordner einlesen
		
		val directories = new File("./input/famix").listFiles.filter(file|file.directory).sort
		
		/**
		 * Find the highest ID of all Famix Elements, so they do not conflict with IDs of now Hismo elements
		 */
		
		for(directory : directories) {
			index++
			val key = directory.name
			
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
			}
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
			if(!famixDoc.elements.empty) {
	  			var famixID = Integer::parseInt(famixDoc.elements.get(famixDoc.elements.size -1).name)
				if(famixID > i) {
					i = famixID
				}
			}
		}
		i++
		
		for (directory : directories) {
			index++
			
			val key = directory.name
			
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
				currentDirectory = directory

				var HISMOClassVersion currentClassVersion
				var HISMOMethodVersion currentMethodVersion
				var HISMOAttributeVersion currentAttributeVersion

				if (famixDoc.elements === null || famixDoc.elements.filter(FAMIXClass) === null) {
				// ||currentCommit.commitId.idRepresentation ==)
					log.error("No Elements found.")
				}
				
				val components = famixDoc.elements.filter(FAMIXComponent).filterNull.toList
				val antipattern = famixDoc.elements.filter(FAMIXAntipattern).filterNull.toList
				val roles = famixDoc.elements.filter(FAMIXRole).filterNull.toList
				val paths = famixDoc.elements.filter(FAMIXPath).filterNull.toList

				hismoDocument.elements += components
				allComponents += components
				hismoDocument.elements += antipattern				
				hismoDocument.elements += roles
				hismoDocument.elements += paths
				
				var famixStructures = famixDoc.elements.filter(FAMIXStructure).sortBy[fqn]
				
				//var famixpattern = famixDoc.elements.filter(FAMIXAntipattern).sortBy[fqn]
				//famixStructures.removeAll(famixpattern)		
				for (structure : famixStructures) {
					var famixMethods = famixDoc.elements.filter(FAMIXMethod).filter[parentType.ref.equals(structure)].sortBy[fqn] // ref as FAMIXElement
					
					for(method : famixMethods) { 
						method.name = method.name + i++
					}
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
							var oldMethodHistory = savedMethodHistories.findFirst[value.equals(method.value)]
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
				updatePaths(paths)
				resource.unload
			}
		}
		
		updateComponents
		updateAntipattern
		
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
		
		updateParents

		// put hismoRoot into list (for writer)
		var hismoList = newArrayList
		hismoList += hismoRoot
		resource.contents += hismoList
		ctx.set("hismoextended", resource)
		ctx.set("hismowriter", hismoList)
		log.info("Famix2Hismo finished")
	}
	
	def private updateAntipattern() {
		val antipatterns = hismoDocument.elements.filter(FAMIXAntipattern)
		antipatterns.forEach[antipattern|
			antipattern.versions += antipattern.version
			antipattern.components.forEach[el | 
				val ref = famixFactory.createIntegerReference
				val realcomponent = antipatterns.findFirst[name == el.ref]
				if(realcomponent !== null) {
					ref.ref = realcomponent
					antipattern.realcomponents += ref
				}
			]
			antipattern.realcomponents.forEach[el |
				val target = el.ref as FAMIXAntipattern
				if(!target.realcomponents.contains(antipattern)) {
					var intref = famixFactory.createIntegerReference
					intref.ref = antipattern
					target.realcomponents += intref 
				}
			]
		]
		
		val Map<FAMIXAntipattern, FAMIXAntipattern> replacedComponents = newHashMap

		antipatterns.forEach[antipattern|
			val futureComponents = newLinkedList 
			futureComponents += antipattern.realcomponents.map[ref.name]
			futureComponents += antipattern.name

			if(!replacedComponents.containsKey(antipattern)) {
				antipatterns.filter[c| c.realcomponents.map[ref.name].contains(antipattern.name)].filter[c| c != antipattern].forEach[relatedComponent|
					val numberOfUnrelatedComponents = relatedComponent.realcomponents.filter[r|!futureComponents.contains(r.ref.name)].isNullOrEmpty
					if(numberOfUnrelatedComponents) {
						replacedComponents.put(relatedComponent, antipattern)
					} else {
						log.warn("Unrelated Component (component splitting) detected. This case is not implemented.")
					}
				]
			}
		]
		
		replacedComponents.forEach[oldComponent, newComponent |
			newComponent.path += oldComponent.path
			newComponent.elements += oldComponent.elements
			newComponent.versions += oldComponent.versions
			newComponent.versions
			val setVersions = newHashSet
			setVersions += newComponent.versions
			newComponent.versions.clear
			newComponent.versions += setVersions.toList
			antipatterns.map[realcomponents].forEach[s|
				s.removeIf[r|r.ref == oldComponent]
				//s.filter[rc|rc.ref == oldComponent].forEach[rc|		
					//rc.ref = newComponent
				//]
			]
			hismoDocument.elements.filter(HISMOClassVersion).filter[cv|cv.antipattern !== null].forEach[cv|
			//.filter[cv|cv.antipattern.map[ref].contains(oldComponent)].forEach[cv|
				var IntegerReference found = null;
				for(pattern : cv.antipattern) {
				//cv.antipattern.forEach[pattern |
					if (pattern.ref == oldComponent) {
						//val reference = famixFactory.createIntegerReference
						//reference.ref = newComponent
						//cv.antipattern += reference
						found = pattern
				//		cv.antipattern -= pattern
					}
					//cv.antipattern += oldComponent.eContainer
				}
				if(found !== null) {
					val reference = famixFactory.createIntegerReference
					reference.ref = newComponent
					cv.antipattern += reference
					cv.antipattern -= found
					//hismoDocument.elements.remove(oldComponent)
				}
			]
			hismoDocument.elements.filter(FAMIXPath).filter[p|p.antipattern !== null].forEach[path, index|
				if (path.antipattern.ref == oldComponent) {
					path.antipattern.ref = newComponent
				}
		 	]
			hismoDocument.elements.remove(oldComponent)
		]
		
		 hismoDocument.elements.filter(FAMIXAntipattern).sortBy[stk|stk.elements.length].reverse.forEach[stk, index|
		 	stk.fqn = "STK " + (index + 1) 
		 ]
	}
	
	/** Update Components
	 * 
	 * Components have references to components of another system version. These references are only stored as Integer and not as IntegerReference because otherwise the famix document is not consistent.
	 * This function replaces the Integers with IntegerReferences. 
	 * Further, it merges components of the different versions if they belong together.
	 * 
	 */
	
	def private updateComponents() {
		val components = hismoDocument.elements.filter(FAMIXComponent)
		components.forEach[component|
			component.versions += component.version
			component.components.forEach[el | 
				val ref = famixFactory.createIntegerReference
				val realcomponent = components.findFirst[name == el.ref]
				if(realcomponent !== null) {
					ref.ref = realcomponent
					component.realcomponents += ref
				}
			]
			component.realcomponents.forEach[el |
				val target = el.ref as FAMIXComponent
				if(!target.realcomponents.contains(component)) {
					var intref = famixFactory.createIntegerReference
					intref.ref = component
					target.realcomponents += intref 
				}
			]
		]
		
		val Map<FAMIXComponent, FAMIXComponent> replacedComponents = newHashMap

		components.forEach[component|
			val futureComponents = newLinkedList 
			futureComponents += component.realcomponents.map[ref.name]
			futureComponents += component.name

			if(!replacedComponents.containsKey(component)) {
				components.filter[c| c.realcomponents.map[ref.name].contains(component.name)].filter[c| c != component].forEach[relatedComponent|
					val numberOfUnrelatedComponents = relatedComponent.realcomponents.filter[r|!futureComponents.contains(r.ref.name)].isNullOrEmpty
					if(numberOfUnrelatedComponents) {
						replacedComponents.put(relatedComponent, component)
					} else {
						log.warn("Unrelated Component (component splitting) detected. This case is not implemented.")
					}
				]
			}
		]
		
		replacedComponents.forEach[oldComponent, newComponent |
			newComponent.path += oldComponent.path
			newComponent.elements += oldComponent.elements
			newComponent.versions += oldComponent.versions
			newComponent.versions
			val setVersions = newHashSet
			setVersions += newComponent.versions
			newComponent.versions.clear
			newComponent.versions += setVersions.toList
			components.map[realcomponents].forEach[s|
				s.removeIf[r|r.ref == oldComponent]
				//s.filter[rc|rc.ref == oldComponent].forEach[rc|		
					//rc.ref = newComponent
				//]
			]
			hismoDocument.elements.filter(HISMOClassVersion).filter[scc !== null].filter[scc.ref == oldComponent].forEach[cv|
				cv.scc.ref = newComponent
			]
			hismoDocument.elements.remove(oldComponent)
		]
		
		hismoDocument.elements.filter(FAMIXComponent).sortBy[c|c.elements.length/c.versions.length].reverse.forEach[component, index|
			component.fqn = "Component " + (index + 1) 
		]
	}
	
	def private updatePaths(List<FAMIXPath> list) {
		list.forEach[path|
			val oldstart = path.start.ref.name
			val oldend =  path.end.ref.name
			val newstart = ids.get(oldstart)
			val newend = ids.get(oldend)
			path.start.ref = newstart
			path.end.ref = newend
		]
	}
	
	def private updateParents() {
		hismoDocument.elements.filter(HISMOClassVersion).forEach[version|
			val ref = version.versionEntity.ref
			switch ref {
				FAMIXClass: { 
					val containerRef = ref.container.ref
					val ir = famixFactory.createIntegerReference
					switch (containerRef) {
						FAMIXNamespace: ir.ref = hismoDocument.elements.filter(HISMONamespaceVersion).findFirst[p|p.versionEntity.ref == containerRef]
						FAMIXClass: ir.ref = hismoDocument.elements.filter(HISMOClassVersion).findFirst[p|p.versionEntity.ref == containerRef]
					}
					version.container = ir
				}
			}
		]
	}

	def getHismoHistoryByFamixObj(FAMIXElement famixElement) {
		switch famixElement {
			FAMIXAttribute:	return hismoDocument.elements.filter(HISMOAttributeHistory).findFirst[value.equals(famixElement.fqn)]
			FAMIXMethod: 	{
				val ret = hismoDocument.elements.filter(HISMOMethodHistory).findFirst[el|
					el.value == famixElement.name
				]
				return ret
			}
			FAMIXNamespace: return hismoDocument.elements.filter(HISMONamespaceHistory).findFirst [value.equals(famixElement.fqn)]
			FAMIXClass:		return hismoDocument.elements.filter(HISMOClassHistory).findFirst [value.equals(famixElement.fqn)]
		}
	}
	
	/**
	 * Crease Class History from FAMIXClass
	 */
	 
	def private HISMOClassHistory toHismoHistory(FAMIXStructure st) {
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

	def private toHismoHistory(FAMIXMethod mt, HISMOClassHistory classhistory) {
		val hismomethodhistory = hismoFactory.createHISMOMethodHistory
		hismomethodhistory.value = mt.name
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
		val hismoclassversion =  hismoFactory.createHISMOClassVersion
		hismoclassversion.timestamp = "2017-12-12 12:" + (index)
		hismoclassversion.commitId = currentDirectory.name
		hismoclassversion.name = (i++).toString
		hismoclassversion.id = famix.createID(st.id + hismoclassversion.name)
		switch(st) {
			FAMIXClass: {
				hismoclassversion.scc = st.scc
				hismoclassversion.antipattern += st.antipattern
			}
		}
		ids.put(st.name, hismoclassversion)
		hismoclassversion.value = st.fqn
		hismoclassversion.betweennessCentrality = st.betweennessCentrality
		hismoclassversion.stkRank = st.stkRank

		// set additional Information from Versioning, like author
		//hismoclassversion.author = currentCommit.author.name
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
		hismonamespaceversion.timestamp = "2017-12-12 12:" + (index)
		hismonamespaceversion.commitId = currentDirectory.name
		hismonamespaceversion.name = (i++).toString
		hismonamespaceversion.id = famix.createID(fns.id + hismonamespaceversion.name)

		// set additional Information from Versioning, like author
		//hismonamespaceversion.author = currentCommit.author.name
		hismonamespaceversion.value = fns.fqn
		val ph = famixFactory.createIntegerReference
		ph.ref = history
		hismonamespaceversion.parentHistory = ph
		val r = famixFactory.createIntegerReference
		r.ref = fns
		hismonamespaceversion.versionEntity = r

		// add Element to elements collection
		hismoDocument.elements += hismonamespaceversion
		hismoDocument.elements += fns
		
		if(fns.parentScope !== null) {
			val parent = fns.parentScope.ref as FAMIXNamespace
			val parentHistory = parent.hismoHistoryByFamixObj as HISMONamespaceHistory
			toHismoVersion(parent,parentHistory)
		}
		return hismonamespaceversion
	}

	/**
	 * Creates MethodVersion from MethodHistory
	 */
	 
	def HISMOMethodVersion toHismoVersion(FAMIXMethod mt, HISMOMethodHistory methodhistory) {
			val hismomethodversion = hismoFactory.createHISMOMethodVersion
			hismomethodversion.timestamp = "2017-12-12 12:" + (index)
			hismomethodversion.commitId = currentDirectory.name
			hismomethodversion.name = (i++).toString
			hismomethodversion.id = famix.createID(mt.id + hismomethodversion.name)
			println("mv: " + hismomethodversion.id)
			mt.numberOfStatements = 30
			hismomethodversion.evolutionNumberOfStatements = 30
			// set additional Information from Versioning, like author
			//hismomethodversion.author = currentCommit.author.name
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
		hismoattributeversion.timestamp = "2017-12-12 12:" + (index)
		hismoattributeversion.commitId = currentDirectory.name
		hismoattributeversion.name = (i++).toString
		hismoattributeversion.id = famix.createID(at.id + hismoattributeversion.name)

		// set additional Information from Versioning, like author
		//hismoattributeversion.author = currentCommit.author.name
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
		println("####Calc Rank Evolution: " + history.name)
		val prevVersion = (history.methodVersions.sortBy[(it.ref as HISMOMethodVersion).timestamp].head.ref as HISMOMethodVersion) // .filter[it.version.ref.name == method.name]
		val prevVersionEntity = prevVersion.versionEntity.ref as FAMIXMethod

		result = prevVersionEntity.numberOfStatements// - currentVersionEntity.numberOfStatements

		return result
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