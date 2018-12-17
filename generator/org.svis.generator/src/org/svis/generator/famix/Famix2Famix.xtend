package org.svis.generator.famix
import java.util.List
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.famix.FAMIXNamespace
import java.util.Set
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXParameterizableClass
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXEnum
import org.svis.xtext.famix.FAMIXParameter
import org.svis.xtext.famix.FAMIXParameterizedType
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXEnumValue
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.famix.Root
import java.util.Collections
import org.svis.xtext.famix.FAMIXComment
import org.svis.xtext.famix.FAMIXLocalVariable
import org.svis.xtext.famix.FAMIXAnnotationType
import org.svis.xtext.famix.FAMIXAnnotationTypeAttribute
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.famix.FAMIXAccess
import org.svis.xtext.famix.FAMIXInvocation
import org.svis.xtext.famix.FAMIXPrimitiveType
import org.svis.xtext.famix.FAMIXType
import org.svis.xtext.famix.FAMIXParameterType
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.apache.commons.beanutils.BeanComparator
import org.eclipse.emf.common.util.ECollections
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import java.util.Map
import org.svis.xtext.famix.impl.FamixFactoryImpl
import java.util.Comparator
import org.svis.xtext.famix.MethodType
import org.svis.xtext.famix.FAMIXAntipattern
import org.svis.xtext.famix.FAMIXPath
import org.svis.xtext.famix.FAMIXComponent
import org.svis.xtext.famix.IntegerReference
import org.neo4j.graphdb.GraphDatabaseService
import org.svis.lib.database.Database
import org.neo4j.graphdb.Node
import org.svis.xtext.famix.FAMIXElement
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.logging.LogFactory
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.FamixParser
import org.svis.xtext.famix.FAMIXReference

//ABAP
import org.svis.xtext.famix.FAMIXReport 
import org.svis.xtext.famix.FAMIXFormroutine
import org.svis.xtext.famix.FAMIXFunctionGroup
import org.svis.xtext.famix.FAMIXFunctionModule
import org.svis.xtext.famix.FAMIXMacro
import org.svis.xtext.famix.FAMIXDictionaryData
import org.svis.xtext.famix.FAMIXDataElement
import org.svis.xtext.famix.FAMIXDomain
import org.svis.xtext.famix.FAMIXTable
import org.svis.xtext.famix.FAMIXTableElement
import org.svis.xtext.famix.FAMIXABAPStruc
import org.svis.xtext.famix.FAMIXStrucElement
import org.svis.xtext.famix.FAMIXTableType
import org.svis.xtext.famix.FAMIXTableTypeElement
import org.svis.xtext.famix.FAMIXTypeOf
import org.svis.xtext.famix.FAMIXMessageClass

class Famix2Famix extends WorkflowComponentWithModelSlot {
	val log = LogFactory::getLog(class)
	var GraphDatabaseService graph
	val Set<FAMIXNamespace> rootPackages = newLinkedHashSet
	val Set<FAMIXNamespace> subPackages = newLinkedHashSet
	val List<FAMIXStructure> allStructures = newArrayList
	val List<FAMIXMethod> methods = newArrayList
	val List<FAMIXMethod> antiMethods = newArrayList
	val List<FAMIXAttribute> attributes = newArrayList
	val List<FAMIXEnumValue> enumValues = newArrayList
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXNamespace> packagesToMerge = newArrayList
	val List<FAMIXReference> references = newArrayList
	val List<FAMIXInheritance> inheritances = newArrayList
	
	val Map<FAMIXMethod, List<FAMIXParameter>> parameters = newHashMap 
	val List<FAMIXInvocation> invocations = newArrayList
	val static famixFactory = new FamixFactoryImpl()
	var List<FAMIXAntipattern> antipattern = newArrayList()
	var List<FAMIXComponent> components = newArrayList()
	var int i = 0
	val Map<String, Node> nodes = newHashMap
	val config = SettingsConfiguration.instance;
	
	//ABAP
	val List<FAMIXReport> reports = newArrayList 
	val List<FAMIXDataElement> dataElements = newArrayList 
	val List<FAMIXDomain> domains = newArrayList
	val List<FAMIXTable> tables = newArrayList 
	val List<FAMIXABAPStruc> abapStrucs = newArrayList 
	val List<FAMIXStrucElement> abapStrucElem = newArrayList 
	val List<FAMIXFunctionModule> functionModules = newArrayList
	val List<FAMIXFormroutine> formroutines = newArrayList
	val List<FAMIXMacro> macros = newArrayList
	val List<FAMIXMessageClass> messageClasses = newArrayList
	val List<FAMIXFunctionGroup> functionGroups = newArrayList
	val List<FAMIXTableType> tableTypes = newArrayList
	val List<FAMIXTableTypeElement> ttypeElements = newArrayList
	val List<FAMIXTableElement> tableElements = newArrayList
	val List<FAMIXTypeOf> typeOf = newArrayList
	
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2Famix has started.")
		val resourceList = ctx.get("famix") as List<?>
				
		if (resourceList.size == 1) {
			var Root famixRoot
			if(config.parser == FamixParser::ABAP){
				famixRoot = runAbap((resourceList as List<Root>).head)
			}else{
				famixRoot = run((resourceList as List<Root>).head)
			}
			ctx.set("famix", famixRoot)
			val resource = new ResourceImpl()
			resource.contents += famixRoot
			ctx.set("metadata", resource)
		} else {
			val resources = newLinkedList
			for (famixRoot : resourceList as List<LazyLinkingResource>) {
				famixRoot.load(null)
				while (famixRoot.loading) {
					log.info("famixRoot " + famixRoot.URI + " is loading.")
				}
				if (famixRoot.isLoaded) {
					famixRoot.contents += run(famixRoot.contents.head as Root)
					i++;
				}
				resources += famixRoot
			}
			ctx.set("famix", resources)
		}
		log.info("Famix2Famix has finished.")
	}
	
	//ABAP logic. Delete after updating extractor in CCLM
	def private runAbap(Root famixRoot){
		val famixDocument = famixRoot.document
		famixDocument.elements.removeAll(Collections::singleton(null))
		
		val Set<FAMIXNamespace> packages = newLinkedHashSet
		val List<FAMIXABAPStruc> abapStrucsTmp = newArrayList
		
		famixDocument.elements.filter(FAMIXPath).forEach[path|
			path.id = createID(path.name + path.start.ref.name + path.end.ref.name);
		]
		
		
		famixDocument.elements.forEach[element|
			switch element {
				FAMIXAttribute: attributes.add(element)					
				FAMIXMethod: methods.add(element)
				FAMIXReport: reports.add(element)
				FAMIXDataElement: dataElements.add(element)
				FAMIXDomain: domains.add(element)
				FAMIXTable: tables.add(element)
				FAMIXABAPStruc: abapStrucsTmp.add(element)
				FAMIXStrucElement: abapStrucElem.add(element)
				FAMIXTableType: tableTypes.add(element)
				FAMIXFunctionModule: functionModules.add(element)
				FAMIXFormroutine: formroutines.add(element)
				FAMIXMacro: macros.add(element)
				FAMIXMessageClass: messageClasses.add(element)
				FAMIXFunctionGroup: functionGroups.add(element)
				FAMIXTableElement: tableElements.add(element)
				FAMIXTypeOf: typeOf.add(element)
				FAMIXReference: references.add(element)
				FAMIXInheritance: inheritances.add(element)
				FAMIXStructure: {
					if(element.container !== null){
						structures.add(element)
					}
				}
			}
			
		]
		
		
		
		val allPackages = famixDocument.elements.filter(FAMIXNamespace).toSet
		allStructures += famixDocument.elements.filter(FAMIXStructure).filter[container !== null]
		
		
		allPackages.forEach[getPackages]
		rootPackages.forEach[setQualifiedName]
		methods.forEach[setQualifiedName]
		enumValues.forEach[setQualifiedName]
		messageClasses.forEach[setQualifiedName]
		reports.forEach[updParameters]
		formroutines.forEach[updParameters]
		functionGroups.forEach[setQualifiedName]
		functionModules.forEach[updParameters]
		macros.forEach[updParameters]
		tables.forEach[updParameters]
		tableElements.forEach[updParameters]
		abapStrucsTmp.forEach[updParameters]
		abapStrucElem.forEach[updParameters]
		abapStrucsTmp.forEach[updateAbapStrucs]
		tableTypes.forEach[ tty | 
			updParameters(tty)
			createTableTypeElements(tty)
		]
		dataElements.forEach[updParameters]		
		domains.forEach[updParameters]				
		attributes.forEach[setQualifiedNameAbap]
		
		famixDocument.elements.clear
		famixDocument.elements.addAll(rootPackages)
		famixDocument.elements.addAll(subPackages)
		famixDocument.elements.addAll(structures)
		famixDocument.elements.addAll(references)
		famixDocument.elements.addAll(inheritances)
				
		famixDocument.elements.addAll(methods)
		famixDocument.elements.addAll(reports)
		famixDocument.elements.addAll(attributes)
		famixDocument.elements.addAll(dataElements)
		famixDocument.elements.addAll(domains)
		famixDocument.elements.addAll(tables)
		famixDocument.elements.addAll(abapStrucs)
		famixDocument.elements.addAll(abapStrucElem)
		famixDocument.elements.addAll(tableTypes)
		famixDocument.elements.addAll(enumValues)
		famixDocument.elements.addAll(invocations)
		famixDocument.elements.addAll(antipattern)
		famixDocument.elements.addAll(components)
		famixDocument.elements.addAll(functionModules)
		famixDocument.elements.addAll(functionGroups)
		famixDocument.elements.addAll(formroutines)
		famixDocument.elements.addAll(macros)
		famixDocument.elements.addAll(messageClasses)
		famixDocument.elements.addAll(tableElements)
		famixDocument.elements.addAll(ttypeElements)
		famixDocument.elements.addAll(typeOf)
		
		rootPackages.clear
		subPackages.clear
		allStructures.clear
		references.clear
		inheritances.clear
		methods.clear
		reports.clear
		dataElements.clear
		domains.clear
		tables.clear
		abapStrucsTmp.clear
		abapStrucs.clear
		abapStrucElem.clear
		tableTypes.clear
		attributes.clear
		enumValues.clear
		structures.clear
		antiMethods.clear
		antipattern.clear
		components.clear
		functionModules.clear
		functionGroups.clear
		formroutines.clear
		macros.clear
		messageClasses.clear
		tableElements.clear
		ttypeElements.clear
		typeOf.clear
		return famixRoot
	} //End of ABAP logic
		
	
	//Default 
	def private toInvocation(FAMIXMethod m1, FAMIXMethod m2) {
		val invocation = famixFactory.createFAMIXInvocation
		invocation.name = m2.name + 2000000
		invocation.sender = famixFactory.createIntegerReference
		invocation.sender.ref = m1
		invocation.candidates = famixFactory.createIntegerReference
		invocation.candidates.ref = m2
		
		invocations += invocation
	}
	
	def private toMethod(FAMIXStructure s, int j, int index, String pattern, String id) {
		val method = famixFactory.createFAMIXMethod
		method.numberOfStatements = 20
		method.parentType = famixFactory.createIntegerReference
		method.parentType.ref = s
		method.id = s.id + "_" + j + "_" + index
		method.fqn = pattern
		method.signature = pattern
		method.value = (i + j).toString
		method.name = (i + j).toString
		method.antipattern = id
		antiMethods.add(method)
		return method
	}
	
	def private addAntipattern(FAMIXStructure elem, FAMIXAntipattern antipattern) {
		switch elem {
			FAMIXClass: if(!elem.antipattern.contains(antipattern)) {
							val antipatternref = famixFactory.createIntegerReference
							antipatternref.ref = antipattern
							elem.antipattern.add(antipatternref)
						}
			FAMIXParameterizableClass: if(!elem.antipattern.contains(antipattern)) {
							val antipatternref = famixFactory.createIntegerReference
							antipatternref.ref = antipattern
							elem.antipattern.add(antipatternref)
						}
			FAMIXEnum: if(!elem.antipattern.contains(antipattern)) {
							val antipatternref = famixFactory.createIntegerReference
							antipatternref.ref = antipattern
							elem.antipattern.add(antipatternref)
						}
			FAMIXAnnotationType: if(!elem.antipattern.contains(antipattern)) {
							val antipatternref = famixFactory.createIntegerReference
							antipatternref.ref = antipattern
							elem.antipattern.add(antipatternref)
						}
		}
		
	}
	
	def private run(Root famixRoot) {
		val famixDocument = famixRoot.document
		famixDocument.elements.removeAll(Collections::singleton(null))
		// elements we do not want
		val List<FAMIXStructure> structuresToDelete = newArrayList
		val fileAnchors = famixDocument.elements.filter(FAMIXFileAnchor).filter[element !== null].toList
		antipattern = famixDocument.elements.filter(FAMIXAntipattern).toList
		components = famixDocument.elements.filter(FAMIXComponent).toList
		val Set<FAMIXNamespace> packages = newLinkedHashSet
		
		famixDocument.elements.filter(FAMIXPath).forEach[path|
			path.id = createID(path.name + path.start.ref.name + path.end.ref.name);
		]
		
		
		if (config.hasAnchors()){
			fileAnchors.forEach[f|
				val element = f.element.ref
				switch element {
					FAMIXAttribute: attributes.add(element)
					FAMIXMethod: methods.add(element)
					FAMIXStructure: {
						if(element.container !== null) {
							structures.add(element)
						}
					}
					FAMIXComment, FAMIXLocalVariable, FAMIXAnnotationTypeAttribute: {} // do nothing, just to prevent them from being treaded in default
					default: {
						log.warn("Famix2Famix: forgot " + element.class)
						log.info(element.name)
						log.info(element.toString)
					}
				}
			]
		 }else{		 	
			famixDocument.elements.forEach[element|
				switch element {
					FAMIXAttribute: attributes.add(element)
					FAMIXMethod: methods.add(element)
					FAMIXStructure: {
						if(element.container !== null){
							structures.add(element)
						}
					}
				}
			]
		}
		
		
		structures.forEach [ s |
			val ref = s.container.ref
			switch ref {
				FAMIXNamespace: packages.add(ref)
				FAMIXMethod: structuresToDelete.add(s)
				FAMIXEnum: structuresToDelete.add(s)
			}
		]
		// delete anonymous classes and their methods and attributes
		methods.removeIf([
			structuresToDelete.contains(parentType.ref) || parentType.ref instanceof FAMIXParameterizedType
		])
		attributes.removeIf([structuresToDelete.contains(parentType.ref)])
		structures.removeIf([structuresToDelete.contains(container.ref)])
		structures -= structuresToDelete

		enumValues += famixDocument.elements.filter(FAMIXEnumValue).filter[structures.contains(parentEnum.ref)]
		famixDocument.elements.filter(FAMIXParameter).filter[methods.contains(parentBehaviouralEntity.ref)].forEach [ p |
			val m = p.parentBehaviouralEntity.ref as FAMIXMethod
			val l = parameters.get(m)
			if (l === null) {
				val l2 = #[p]
				parameters.put(m, l2)
			} else {
				val List<FAMIXParameter> l2 = newLinkedList
				l2.addAll(l)
				l2.add(p)
				parameters.replace(m, l2)
			}
		]
		if (config.hidePrivateElements) {
			deletePrivates
		}
		val allPackages = famixDocument.elements.filter(FAMIXNamespace).toSet
		allStructures += famixDocument.elements.filter(FAMIXStructure).filter[container !== null]
		val BeanComparator<FAMIXElement> nameComparator = new BeanComparator("name")
		val accesses = famixDocument.elements.filter(FAMIXAccess)
							.filter[methods.contains(accessor.ref)]
							.filter[attributes.contains(variable.ref)]
							.sortWith(nameComparator)
		invocations += famixDocument.elements.filter(FAMIXInvocation)
							.filter[methods.contains(candidates.ref)]
							.filter[methods.contains(sender.ref)]
							.sortWith(nameComparator)
		val inheritances = famixDocument.elements.filter(FAMIXInheritance)
							.filter[structures.contains(superclass.ref)]
							.filter[structures.contains(subclass.ref)]
							.sortWith(nameComparator)
		allPackages.forEach[getPackages]
		rootPackages.forEach[setQualifiedName]
		methods.forEach[setQualifiedName]
		attributes.forEach[setQualifiedName]
		enumValues.forEach[setQualifiedName]

		removeDuplicates()
		if (config.methodTypeMode) {
			methods.forEach[setMethodType(accesses)]
			attributes.forEach[setCalledBy]
			deletePrivateAttributes(accesses)
		}
		val BeanComparator<FAMIXElement> comparator = new BeanComparator("fqn")
		if(config.parser == FamixParser::JDT2FAMIX) {
			methods.forEach [
				val sourceAnchor = it.sourceAnchor.ref as FAMIXFileAnchor
				val numberOfLines = sourceAnchor.endline - sourceAnchor.startline + 1
				it.numberOfStatements = numberOfLines
			]
		}
		if (config.attributeSortSize) {
			Collections::sort(attributes, new Comparator<FAMIXAttribute>() {
				override compare(FAMIXAttribute attribute1, FAMIXAttribute attribute2) {
					val diffLength = attribute1.value.length - attribute2.value.length
					if (diffLength == 0) {
						return attribute1.value.compareToIgnoreCase(attribute2.value)
					}
					return diffLength * -1
				}
			})
		} else {
			Collections::sort(attributes, comparator)
		}

		structures.removeIf[fqn === null]
		Collections::sort(structures, comparator)
		Collections::sort(enumValues, comparator)

		val comparator2 = new BeanComparator("numberOfStatements")
		Collections::sort(methods, comparator2)
		rootPackages.clear
		subPackages.clear
		packages.forEach[getPackages]
		if (config.isMasterRoot && rootPackages.size > 1) {
			val Master = createMasterRoot
			rootPackages.forEach[setParentScopeRoots(it, Master)]
			rootPackages.forEach[subPackages += it]
			rootPackages.clear
			rootPackages += Master
		}
		if (config.mergePackages) {
			rootPackages.forEach[findPackagesToMerge]
			packagesToMerge.forEach[removePackages]
			packagesToMerge.forEach[mergePackages]
		}
		
		val paths = famixDocument.elements.filter(FAMIXPath).filter[start.ref !== null].filter[end.ref !== null].toList
		
		famixDocument.elements.clear
		famixDocument.elements.addAll(rootPackages)
		famixDocument.elements.addAll(subPackages)
		ECollections::sort(famixDocument.elements, comparator)
		famixDocument.elements.addAll(paths)
		famixDocument.elements.addAll(structures)
		
		val componentsToDelete = newHashSet
		components.forEach[component|
			val oldPaths = newLinkedList
			val classes = newHashSet
			component.path.forEach[ref|
				val path = ref.ref as FAMIXPath
				val start = path.start.ref as FAMIXClass
				val end = path.end.ref as FAMIXClass
				if(start.container !== null && end.container !== null) {
					if(start.container.ref == end || end.container.ref == start){
						oldPaths += ref
					} else {
						classes += start
						classes += end
					}
				}
			]
			component.path.removeAll(oldPaths)			
			component.elements.removeIf[el| classes.contains(el.ref) == false]
			if(component.path.isNullOrEmpty || component.elements.isNullOrEmpty) {
				componentsToDelete += component
			}
		]
		
		famixDocument.elements.removeAll(componentsToDelete)
		components.removeAll(componentsToDelete)
		
		val List<String> strings = newLinkedList
		components.forEach[component |
			component.id = component.name.createID
			component.elements.forEach[el, index |
				strings += el.ref.name
				val structure = el.ref as FAMIXClass
				val ref = famixFactory.createIntegerReference
				ref.ref = component
				structure.scc = ref
			]
		]
		
		antipattern.forEach[p, j|
			val currentPattern = p
			currentPattern.id = currentPattern.name.createID
			val Map<String, FAMIXMethod> methods_ = newHashMap
			val List<IntegerReference> pathsToDelete = newLinkedList
			p.path.forEach[n, index|
				val node  = n.ref as FAMIXPath
				val start_id = (node.start.ref as FAMIXStructure).name
				var foundStart = true
				var foundEnd = true
				var FAMIXMethod start_method
				var FAMIXMethod end_method
				if (methods_.containsKey(start_id)) {
					start_method = methods_.get(start_id)
				} else {
					foundStart = !structures.filter[name == start_id].isNullOrEmpty
					if(foundStart) {
						val start_class = structures.filter[name == start_id].get(0)
						start_method = toMethod(start_class, j, index, p.type, currentPattern.id)
						methods_.put(start_id, start_method)
						addAntipattern(start_class, currentPattern)
					}
				}
				
				val end_id = (node.end.ref as FAMIXStructure).name
				if (methods_.containsKey(end_id)) {
					end_method = methods_.get(end_id)
				} else {
					foundEnd = !structures.filter[name == end_id].nullOrEmpty
					if(foundEnd) {					
						val end_class = structures.filter[name == end_id].get(0)
						end_method = toMethod(end_class, j, index, p.type, currentPattern.id)
						methods_.put(end_id, end_method)
						addAntipattern(end_class, currentPattern)
					}
				}
				if (foundStart && foundEnd) {
					toInvocation(start_method, end_method)
				} else {
					pathsToDelete += n
				}
			]
			
			p.path.removeAll(pathsToDelete)
			paths.removeAll(pathsToDelete)
		]
	
		famixDocument.elements.addAll(methods)
		famixDocument.elements.addAll(attributes)
		famixDocument.elements.addAll(enumValues)
		famixDocument.elements.addAll(invocations)
		famixDocument.elements.addAll(accesses)
		famixDocument.elements.addAll(inheritances)
		famixDocument.elements.addAll(fileAnchors)
		famixDocument.elements.addAll(antipattern)
		famixDocument.elements.addAll(components)

		if (config.writeToDatabase) {
			graph = Database::getInstance(config.databaseName)
			val document = famixRoot.document
			var tx = graph.beginTx
			try {
				rootPackages.forEach[toDB(it, null)]
				document.elements.filter(FAMIXInvocation).forEach[toDB(it)]
				document.elements.filter(FAMIXInheritance).forEach[toDB(it)]
				document.elements.filter(FAMIXAccess).forEach[toDB(it)]
				tx.success
			} finally {
				tx.close
			}
		}

		rootPackages.clear
		subPackages.clear
		allStructures.clear
		methods.clear
		attributes.clear
		enumValues.clear
		structures.clear
		antiMethods.clear
		antipattern.clear
		components.clear
		return famixRoot	
	}

	def private deletePrivates() {
		methods.removeIf([modifiers.contains("private")])
		structures.removeIf([modifiers.contains("private")])
		if (!config.methodTypeMode) {
			attributes.removeIf([modifiers.contains("private")])
		}
	// EnumValues are always public
	}

	def private deletePrivateAttributes(List<FAMIXAccess> accesses) {
		attributes.removeIf([modifiers.contains("private") && getterSetter.empty])
		accesses.removeIf[!attributes.contains(variable.ref)]
	}

	def private void getPackages(FAMIXNamespace namespace) {
		if (namespace.parentScope === null) {
			rootPackages += namespace
		} else {
			subPackages += namespace
			getPackages(namespace.parentScope.ref as FAMIXNamespace)
		}
	}

	/**
	 * creates the "master package" which contains all the root packages
	 */
	def private createMasterRoot() {
		val masterNamespace = famixFactory.createFAMIXNamespace
		// masterNamespace.parentScope = namespaceFactory.createIntegerReference
		masterNamespace.parentScope = null
		masterNamespace.name = "MasterRoot"
		masterNamespace.value = "MasterRoot"
		masterNamespace.fqn = masterNamespace.name
		masterNamespace.isStub = "false"
		masterNamespace.id = createID(masterNamespace.fqn)
		return masterNamespace
	}

	def private setParentScopeRoots(FAMIXNamespace namespace, FAMIXNamespace Master) {
		namespace.parentScope = famixFactory.createIntegerReference
		namespace.parentScope.ref = Master
	}

	/**
	 * checks if there is exactly one child element and if its of type FamixNamespace
	 */
	def private checkMergeStatus(FAMIXNamespace namespace) {
		val subs = subPackages.filter[parentScope.ref == namespace].toList
		val sub = structures.filter[container.ref == namespace].toList
		if ((subs.size == 1) && (sub.size == 0)) {
			return true
		}
		return false
	}

	/**
	 * depth first search for candidates to merge
	 */
	def private void findPackagesToMerge(FAMIXNamespace namespace) {
		subPackages.filter[parentScope.ref == namespace].forEach [
			if (namespace.checkMergeStatus) {
				packagesToMerge += it
			}
			findPackagesToMerge
		]
	}

	/**
	 * removes packagesToMerge Elements from sub- and rootPackage List
	 * so that they will not get visualized 
	 */
	def private removePackages(FAMIXNamespace namespace) {
		val parent = namespace.parentScope.ref as FAMIXNamespace
		subPackages -= parent
		rootPackages -= parent
	}

	/**
	 * actual merge of packages 
	 */
	def private mergePackages(FAMIXNamespace namespace) {
		var parent = namespace.parentScope.ref as FAMIXNamespace
		namespace.parentScope = parent.parentScope
		namespace.value = parent.value + "." + namespace.value
	}

	def void setQualifiedName(FAMIXNamespace el) {
		if (el.parentScope === null) {
			el.fqn = el.value
		} else {
			el.fqn = (el.parentScope.ref as FAMIXNamespace).fqn + "." + el.value
		}

		el.id = createID(el.fqn)

		allStructures.filter[container.ref.equals(el)].forEach[setQualifiedName]
		subPackages.filter[parentScope.ref.equals(el)].forEach[setQualifiedName]
	}

	def void setQualifiedName(FAMIXStructure el) {
		val ref = el.container.ref
		var name = ""
		switch ref {
			FAMIXNamespace: name = ref.fqn
			FAMIXStructure: name = ref.fqn
			FAMIXMethod: name = ref.fqn
			default: log.error("ERROR qualifiedName(FAMIXStructure): " + el.value)
		}
		el.fqn = name + "." + el.value
		el.id = createID(el.fqn)

		allStructures.filter[container.ref.equals(el)].forEach[setQualifiedName]
	}

	def void setQualifiedName(FAMIXMethod method) {
		var parameters = parameters.getOrDefault(method, newLinkedList).sortBy[value]
		val ref = method.parentType.ref
		var result = ""
		switch ref {
			FAMIXParameterizableClass: result += ref.fqn + "." + method.value
			FAMIXClass: result += ref.fqn + "." + method.value
			// FAMIXParameterizedType: 	result += ref.fqn + "." + method.value
			FAMIXEnum: result += ref.fqn + "." + method.value
			default: log.error("ERROR qualifiedName(FAMIXMethod famixMethod): " + method.value)
		}
		result += parameters.toParameterList

		method.fqn = result
		method.id = createID(method.fqn)
		if(method.numberOfStatements >= 2){
			var nos = method.numberOfStatements - 2
			method.numberOfStatements = nos
		}
	}

	/** 
	 * Sets the EnumValue method type which can either be Getter,Setter
	 * Constructor or Unknown by default
	 * Stores the accessed variable in method.accessesVar
	 */
	def private void setMethodType(FAMIXMethod method, List<FAMIXAccess> accesses) {
		val Variables = newArrayList
		accesses.filter[accessor.ref.equals(method)].forEach[Variables += variable.ref as FAMIXAttribute]
		val parentClass = method.parentType.ref as FAMIXStructure
		val methodValueLowerCase = method.value.toLowerCase
		val isConstructor = method.value.equals(parentClass.value)
		val accessedVariableOfGetter = isGetter(methodValueLowerCase, Variables)
		val accessedVariableOfSetter = isSetter(methodValueLowerCase, Variables)
		if (accessedVariableOfGetter !== null) {
			method.methodType = MethodType::GETTER
			method.accessesVar = famixFactory.createIntegerReference
			method.accessesVar.ref = accessedVariableOfGetter
		}
		if (accessedVariableOfSetter !== null) {
			method.methodType = MethodType::SETTER
			method.accessesVar = famixFactory.createIntegerReference
			method.accessesVar.ref = accessedVariableOfSetter
		}
		if (isConstructor) {
			method.methodType = MethodType::CONSTRUCTOR
		}
	}

	/**
	 * checks if method is a getter and if yes it returns the accessed Variable
	 */
	def private FAMIXAttribute isGetter(String methodValueLowerCase, List<FAMIXAttribute> Variables) {
		if ((Variables.size == 1 && methodValueLowerCase.startsWith("get")) || (Variables.size > 1 && Variables.filter [
			"get" + it.value.toLowerCase == methodValueLowerCase
		].size == 1)) {
			return Variables.get(0)
		} else {
			return null
		}
	}

	/**
	 * checks if method is a setter and if yes it returns the accessed Variable
	 */
	def private FAMIXAttribute isSetter(String methodValueLowerCase, List<FAMIXAttribute> Variables) {
		if ((Variables.size == 1 && methodValueLowerCase.startsWith("set")) || (Variables.size > 1 && Variables.filter [
			"set" + it.value.toLowerCase == methodValueLowerCase
		].size == 1)) {
			return Variables.get(0)
		} else {
			return null
		}
	}

	/**
	 *  filters getters and setters of attributes and
	 * Stores them in Reference List getterSetter
	 */
	def private setCalledBy(FAMIXAttribute attribute) {
		methods.filter[it.methodType == MethodType::GETTER || it.methodType == MethodType::SETTER].forEach [
			if (it.accessesVar.ref == attribute) {
				val getterSetter = famixFactory.createIntegerReference
				getterSetter.ref = it
				attribute.getterSetter += getterSetter
			}
		]
	}

	def private String toParameterList(Iterable<FAMIXParameter> parameters) {
		'''(«FOR p : parameters SEPARATOR ","»«p.setQualifiedName»«ENDFOR»)'''
	}

	def private String setQualifiedName(FAMIXParameter parameter) {
		val ref = parameter.declaredType.ref
		switch (ref) {
			FAMIXPrimitiveType:
				return ref.value
			FAMIXClass:
				return ref.fqn
			FAMIXParameterizableClass: {
				if (ref.fqn === null) {
					return ref.value
				} else {
					return ref.fqn
				}
			}
			FAMIXType:
				return ref.value // TODO container?
			FAMIXParameterizedType: {
				if (ref.fqn === null) {
					return ref.value
				} else {
					return ref.fqn
				}
			}
			FAMIXEnum:
				return ref.fqn
			FAMIXParameterType:
				return ref.value // TODO container?
			FAMIXAnnotationType:
				return ref.fqn
			default:
				log.error("ERROR qualifiedName(FAMIXParameter famixParameter): " + parameter.value)
		}
		return ""
	}

	def setQualifiedName(FAMIXAttribute attribute) {
		val ref = attribute.parentType.ref
		switch (ref) {
			FAMIXClass: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXParameterizableClass: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXEnum: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXAnnotationType: attribute.fqn = ref.fqn + "." + attribute.value
			default: log.error("ERROR qualifiedName(FAMIXAttribute famixAttribute): " + attribute.value)
		}
		attribute.id = createID(attribute.fqn)
	}
	
	
	//ABAP
	//Check if structure has elements. Proceed with those, that aren't empty 
	def updateAbapStrucs(FAMIXABAPStruc struc){
		if(abapStrucElem.filter[container.ref.name == struc.name].length != 0 || struc.iteration != 0){
			abapStrucs.add(struc)
		}
	}
	
	
	def setQualifiedNameAbap(FAMIXAttribute attribute) {
		val ref = attribute.parentType.ref
		switch (ref) {
			FAMIXClass: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXReport: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXFormroutine: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXFunctionGroup: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXFunctionModule: attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXMethod: attribute.fqn = ref.fqn + "." + attribute.value
			default: log.error("ERROR qualifiedName(FAMIXAttribute famixAttribute): " + attribute.value)
		}
		attribute.id = createID(attribute.fqn + "Attribute")
	}
	
	//ABAP	
	def updParameters(FAMIXDictionaryData dd){
		val ref = dd.container.ref
		
		// set right ref and id
		if (ref instanceof FAMIXDictionaryData) {
			dd.fqn = ref.fqn + "." + dd.value
		} else if (ref instanceof FAMIXNamespace) {
			dd.fqn = ref.fqn + "." + dd.value
		}		
		dd.id = createID(dd.fqn + dd.class.toString)
	}  
	
	def createTableTypeElements(FAMIXTableType tt){
		val tableTypeOf = typeOf.filter[element.ref == tt]
				
		// find "parent" elements and add them to the TableTypeElem Array
		for(tty : tableTypeOf){
			if (tty.typeOf.ref instanceof FAMIXABAPStruc) {
				abapStrucElem.filter[container.ref == tty.typeOf.ref].forEach[ 
					createTableTypeElement(tty.element)
				]
			} else if (tty.typeOf.ref instanceof FAMIXTable) {
				tableElements.filter[container.ref == tty.typeOf.ref].forEach[ 
					createTableTypeElement(tty.element)
				]
			}
		}
	}
	
	def createTableTypeElement(FAMIXDictionaryData dd, IntegerReference element){
		var ttyElement  = famixFactory.createFAMIXTableTypeElement		
		if (dd instanceof FAMIXStrucElement || dd instanceof FAMIXTableElement){
			ttyElement.id = createID(dd.id + "TableTypeElement")
			ttyElement.name = dd.name
			ttyElement.value = dd.value
			ttyElement.fqn = dd.fqn
			ttyElement.container = famixFactory.createIntegerReference
			ttyElement.container.ref = dd.container.ref
			ttyElement.tableType = famixFactory.createIntegerReference
			ttyElement.tableType.ref = element.ref
			ttypeElements += ttyElement
		}
	}
	
	def updParameters(FAMIXFunctionModule fm){
		val ref = fm.parentType.ref
		if (ref instanceof FAMIXFunctionGroup) {
			fm.fqn = ref.fqn + "." + fm.value
		}
		fm.id = createID(fm.fqn + fm.class.toString)
		if (fm.numberOfStatements >= 2) {
			var nos = fm.numberOfStatements - 2
			fm.numberOfStatements = nos
		}
	}
  
	def updParameters(FAMIXMacro ma){
		var ref = ma.parentType.ref
		if(ref instanceof FAMIXMethod){
			ma.fqn = ref.fqn + "." + ma.value
		}
	}
    
	def updParameters(FAMIXReport re){
		val ref = re.container.ref
		if (ref instanceof FAMIXNamespace) {
			re.fqn = ref.fqn + "." + re.value
		}
		re.id = createID(re.fqn + re.class.toString)
	}
	
	def updParameters(FAMIXFormroutine fr){
		var ref = fr.parentType.ref
		if (ref instanceof FAMIXReport) {
			fr.fqn = ref.fqn + "." + fr.value
		}		
		fr.id = createID(fr.fqn + fr.class.toString)
		if (fr.numberOfStatements >= 2) {
			var nos = fr.numberOfStatements - 2
			fr.numberOfStatements = nos
		}
	
	}

	def private setQualifiedName(FAMIXEnumValue enumValue) {
		val ref = enumValue.parentEnum.ref
		if (ref instanceof FAMIXEnum) {
			enumValue.fqn = ref.fqn + "." + enumValue.value
		}
		enumValue.id = createID(enumValue.fqn)
	}

	/**
	 * Creates hash as (hopefully) unique ID for every FAMIXElement
	 * 
	 * @param fqn full qualified name of FAMIXElement
	 * @return  sha1 hash
	 *  
	 */
	def createID(String fqn) {
		return "ID_" + sha1Hex(fqn + config.repositoryName + config.repositoryOwner)
	}

	/**
	 * Removes entities with the same fqn
	 * workaround for: https://bitbucket.org/rimue/generator/issues/30/multiple-entities-with-same-fqn
	 */
	def private removeDuplicates() {
		val List<FAMIXMethod> duplicateMethods = newArrayList
		val List<FAMIXAttribute> duplicateAttributes = newArrayList
		val List<FAMIXEnumValue> duplicateEnumValues = newArrayList
		val List<FAMIXAnnotationType> duplicateAnnotationTypes = newArrayList
		val Set<String> methodFqn = newHashSet
		val Set<String> attributeFqn = newHashSet
		val Set<String> enumValueFqn = newHashSet
		val Set<String> annotationTypeFqn = newHashSet

		structures.forEach [ s |
			switch s {
				FAMIXAnnotationType:
					if (!annotationTypeFqn.add(s.fqn)) {
						duplicateAnnotationTypes.add(s)
					}
			}
		]

		methods.forEach [ m |
			if (!methodFqn.add(m.fqn)) {
				duplicateMethods.add(m)
			}
		]

		attributes.forEach [ a |
			if (!attributeFqn.add(a.fqn)) {
				duplicateAttributes.add(a)
			}
		]

		enumValues.forEach [ e |
			if (!enumValueFqn.add(e.fqn)) {
				duplicateEnumValues.add(e)
			}
		]

		methods.removeAll(duplicateMethods)
		attributes.removeAll(duplicateAttributes)
		enumValues.removeAll(duplicateEnumValues)
		structures.removeAll(duplicateAnnotationTypes)
	}

	def Node toDB(FAMIXNamespace namespace, Node parent) {
		val node = graph.createNode(Labels.Package)
		node.setProperty("name", namespace.value)
		node.setProperty("fqn", namespace.fqn)
		if (parent !== null) {
			parent.createRelationshipTo(node, Rels.CONTAINS)
		}
		structures.filter[container.ref.equals(namespace)].forEach[toDB(node)]
		subPackages.filter[parentScope.ref.equals(namespace)].forEach[toDB(node)]
		return node
	}

	def Node toDB(FAMIXStructure structure, Node parent) {
		val node = graph.createNode(Labels.Type)
		node.setProperty("fqn", structure.fqn)
		node.setProperty("name", structure.value)
		var String fileName
		switch structure {
			FAMIXClass: {
				node.addLabel(Labels.Class)
				// md5 von JQAssisstant?
				node.setProperty("md5", structure.id)
				fileName = (structure.type.ref as FAMIXFileAnchor).filename
				if (structure.isInterface == "true") {
					node.addLabel(Labels.Interface)
				} else {
					node.addLabel(Labels.Class)
				}
			}
			FAMIXEnum: {
				node.addLabel(Labels.Enum)
				fileName = (structure.sourceAnchor.ref as FAMIXFileAnchor).filename
				enumValues.filter[parentEnum.ref.equals(structure)].forEach[toDB(node)]
			}
			FAMIXAnnotationType: {
				node.addLabel(Labels.Annotation)
				fileName = (structure.sourceAnchor.ref as FAMIXFileAnchor).filename
			}
		}
		node.setProperty("sourceFileName", fileName)
		if (structure.modifiers.size != 0) {
			setModifierProperties(node, structure.modifiers)
		}
		nodes.put(structure.id, node)
		parent.createRelationshipTo(node, Rels.CONTAINS)
		structures.filter[container.ref.equals(structure)].forEach[toDB(node)]
		methods.filter[parentType.ref.equals(structure)].forEach[toDB(node)]
		attributes.filter[parentType.ref.equals(structure)].forEach[toDB(node)]
		return node
	}

	def toDB(FAMIXMethod method, Node parent) {
		val node = graph.createNode(Labels.Method, Labels.Member)
		val fileAnchor = method.sourceAnchor.ref as FAMIXFileAnchor
		node.setProperty("name", method.value)
		node.setProperty("effectiveLineCount", method.numberOfStatements.longValue)
		node.setProperty("cyclomaticComplexity", method.cyclomaticComplexity.longValue)
		node.setProperty("firstLineNumber", fileAnchor.startline.longValue)
		node.setProperty("lastLineNumber", fileAnchor.endline.longValue)
		node.setProperty("sourceFileName", fileAnchor.filename)
		var signature = method.signature
		var FAMIXElement declaredType
		try {
			declaredType = method.declaredType.ref
		} catch (NullPointerException e) {
			declaredType = null
		}
		var String type
		switch declaredType {
			FAMIXPrimitiveType: type = (declaredType as FAMIXPrimitiveType).value + " "
			FAMIXParameterizedType: type = (declaredType as FAMIXParameterizedType).value + " "
			default: type = ""
		}
		signature = type + signature
		node.setProperty("signature", signature)
		if (method.modifiers.size != 0) {
			setFinalProperty(node, method.modifiers)
			setVisibilityProperty(node, method.modifiers)
			setStaticProperty(node, method.modifiers)
		}
		nodes.put(method.id, node)
		parent.createRelationshipTo(node, Rels.DECLARES)
	}

	def toDB(FAMIXAttribute attribute, Node parent) {
		val node = graph.createNode(Labels.Field, Labels.Member)
		node.setProperty("name", attribute.value)
		if (attribute.modifiers.size != 0) {
			setFinalProperty(node, attribute.modifiers)
			setVisibilityProperty(node, attribute.modifiers)
			setStaticProperty(node, attribute.modifiers)
		}
		nodes.put(attribute.id, node)
		parent.createRelationshipTo(node, Rels.DECLARES)
	}

	def toDB(FAMIXInvocation invocation) {
		val sender = invocation.sender.ref as FAMIXMethod
		val receiver = invocation.candidates.ref as FAMIXMethod
		val senderNode = nodes.get(sender.id)
		val receiverNode = nodes.get(receiver.id)
		if (senderNode !== null && receiverNode !== null) {
			senderNode.createRelationshipTo(receiverNode, Rels.INVOKES)
		}
	}

	def toDB(FAMIXInheritance inheritance) {
		val subClass = inheritance.subclass.ref as FAMIXStructure
		val superClass = inheritance.superclass.ref as FAMIXStructure
		val subClassNode = nodes.get(subClass.id)
		val superClassNode = nodes.get(superClass.id)
		if (subClassNode !== null && superClassNode !== null) {
			subClassNode.createRelationshipTo(superClassNode, Rels.EXTENDS)
		}
	}

	def toDB(FAMIXAccess access) {
		val method = access.accessor.ref as FAMIXMethod
		val attribute = access.variable.ref as FAMIXAttribute
		val methodNode = nodes.get(method.id)
		val attributeNode = nodes.get(attribute.id)
		if (methodNode !== null && attributeNode !== null) {
			var Rels relationship
			if (access.isWrite == "true") {
				relationship = Rels.WRITES
			} else {
				relationship = Rels.READS
			}
			methodNode.createRelationshipTo(attributeNode, relationship)
		}
	}

	def toDB(FAMIXEnumValue enumVal, Node parent) {
		val node = graph.createNode(Labels.Field, Labels.Member)
		node.setProperty("name", enumVal.value)
		parent.createRelationshipTo(node, Rels.DECLARES)
	}

	def setModifierProperties(Node node, EList<String> modifiers) {
		setAbstractProperty(node, modifiers)
		setFinalProperty(node, modifiers)
		setVisibilityProperty(node, modifiers)
		setStaticProperty(node, modifiers)
	}

	def setAbstractProperty(Node node, EList<String> modifiers) {
		val abstract = modifiers.findFirst[it == "abstract"]
		if (abstract !== null) {
			node.setProperty("abstract", true)
		}
	}

	def setFinalProperty(Node node, EList<String> modifiers) {
		val final = modifiers.findFirst[it == "final"]
		if (final !== null) {
			node.setProperty("final", true)
		}
	}

	def setVisibilityProperty(Node node, EList<String> modifiers) {
		val visibility = modifiers.findFirst[it == "public" || it == "private" || it == "protected" || it == "package"]
		if (visibility !== null) {
			node.setProperty("visibility", visibility)
		}
	}

	def setStaticProperty(Node node, EList<String> modifiers) {
		val static = modifiers.findFirst[it == "static"]
		if (static !== null) {
			node.setProperty("static", true)
		}
	}
}