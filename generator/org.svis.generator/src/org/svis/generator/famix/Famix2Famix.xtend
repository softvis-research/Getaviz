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
import org.svis.generator.WorkflowComponentWithConfig
import java.util.Map
import org.svis.generator.famix.FAMIXSettings
import org.svis.xtext.famix.impl.FamixFactoryImpl
import java.util.Comparator
import org.svis.xtext.famix.MethodType
import org.svis.generator.rd.RDSettings
import org.svis.xtext.famix.FAMIXAntipattern
import org.svis.xtext.famix.FAMIXPath
import org.svis.xtext.famix.FAMIXComponent
import org.svis.xtext.famix.IntegerReference

class Famix2Famix extends WorkflowComponentWithConfig {
	val Set<FAMIXNamespace> rootPackages = newLinkedHashSet
	val Set<FAMIXNamespace> subPackages = newLinkedHashSet
	val List<FAMIXStructure> allStructures = newArrayList
	val List<FAMIXMethod> methods = newArrayList
	val List<FAMIXMethod> antiMethods = newArrayList
	val List<FAMIXAttribute> attributes = newArrayList
	val List<FAMIXEnumValue> enumValues = newArrayList
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXNamespace> packagesToMerge = newArrayList
	val Map<FAMIXMethod, List<FAMIXParameter>> parameters = newHashMap 
	val List<FAMIXInvocation> invocations = newArrayList
	val static famixFactory = new FamixFactoryImpl()
	var List<FAMIXAntipattern> antipattern = newArrayList()
	var List<FAMIXComponent> components = newArrayList()
	var int i = 0
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2Famix has started.")
		
		val resourceList = ctx.get("famix") as List<?>
		
		if(resourceList.size == 1) {
			val famixRoot = run((resourceList as List<Root>).head)
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

		structures.forEach[s|
			val ref = s.container.ref
			switch ref {
				FAMIXNamespace: packages.add(ref)
				FAMIXMethod: structuresToDelete.add(s)
				FAMIXEnum: structuresToDelete.add(s)
			}
		]
		// delete anonymous classes and their methods and attributes
		methods.removeIf([structuresToDelete.contains(parentType.ref) || parentType.ref instanceof FAMIXParameterizedType])
		attributes.removeIf([structuresToDelete.contains(parentType.ref)])
		structures.removeIf([structuresToDelete.contains(container.ref)])
		structures -= structuresToDelete

		enumValues += famixDocument.elements.filter(FAMIXEnumValue).filter[structures.contains(parentEnum.ref)]
		famixDocument.elements.filter(FAMIXParameter).filter[methods.contains(parentBehaviouralEntity.ref)].forEach[p|
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
		if(FAMIXSettings::HIDE_PRIVATE_ELEMENTS) {
			deletePrivates	
		}
		val allPackages = famixDocument.elements.filter(FAMIXNamespace).toSet
		allStructures += famixDocument.elements.filter(FAMIXStructure)
		val nameComparator = new BeanComparator("name")
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
		if(RDSettings::METHOD_TYPE_MODE){	
			methods.forEach[setMethodType(accesses)]
			attributes.forEach[setCalledBy]	
			deletePrivateAttributes(accesses)
		}
		val comparator = new BeanComparator("fqn")
		if(FAMIXSettings::FAMIX_PARSER == FAMIXSettings::FamixParser::JDT2FAMIX) {
			methods.forEach[
				val sourceAnchor = it.sourceAnchor.ref as FAMIXFileAnchor
				val numberOfLines = sourceAnchor.endline - sourceAnchor.startline + 1
				it.numberOfStatements = numberOfLines
			]
		}
		if(FAMIXSettings::ATTRIBUTE_SORT_SIZE) {
			Collections::sort(attributes, new Comparator<FAMIXAttribute>() {
				override compare(FAMIXAttribute attribute1, FAMIXAttribute attribute2) {
					val diffLength = attribute1.value.length - attribute2.value.length
					if(diffLength == 0) {
						return attribute1.value.compareToIgnoreCase(attribute2.value)
					}
					return diffLength  * -1
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
		if(FAMIXSettings::MASTER_ROOT && rootPackages.size >1){
			val Master = createMasterRoot
			rootPackages.forEach[setParentScopeRoots(it,Master)]
			rootPackages.forEach[subPackages += it ]
			rootPackages.clear
			rootPackages += Master
		}
		if(FAMIXSettings::MERGE_PACKAGES){
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
		if(!RDSettings::METHOD_TYPE_MODE) {
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
	
	def private createMasterRoot(){
		val masterNamespace = famixFactory.createFAMIXNamespace
		//masterNamespace.parentScope = namespaceFactory.createIntegerReference
		masterNamespace.parentScope = null
		masterNamespace.name = "MasterRoot" 
		masterNamespace.value = "MasterRoot"
		masterNamespace.fqn = masterNamespace.name
		masterNamespace.isStub = "false"
		masterNamespace.id = createID(masterNamespace.fqn)
		return masterNamespace		
	}
	
	def private setParentScopeRoots(FAMIXNamespace namespace, FAMIXNamespace Master){
		namespace.parentScope = famixFactory.createIntegerReference
		namespace.parentScope.ref = Master
	}
	
	/**
	 * checks if there is exactly one child element and if its of type FamixNamespace
	 */
	
	def private checkMergeStatus(FAMIXNamespace namespace){	
		val subs = subPackages.filter[parentScope.ref == namespace].toList
		val sub = structures.filter[container.ref == namespace].toList
		if((subs.size == 1) && (sub.size == 0)){
			return true	
		}
		return false	
	}
	
	/**
	 * depth first search for candidates to merge
	 */
	
	def private void findPackagesToMerge(FAMIXNamespace namespace) {
		subPackages.filter[parentScope.ref == namespace].forEach[
			if(namespace.checkMergeStatus){
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
			FAMIXNamespace: 			name = ref.fqn
			FAMIXStructure: 			name = ref.fqn
			FAMIXMethod: 				name = ref.fqn
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
				FAMIXParameterizableClass: 	result += ref.fqn + "." + method.value
				FAMIXClass:				 	result += ref.fqn + "." + method.value
				//FAMIXParameterizedType: 	result += ref.fqn + "." + method.value
				FAMIXEnum: 					result += ref.fqn + "." + method.value 
				default: log.error("ERROR qualifiedName(FAMIXMethod famixMethod): " + method.value)
		}
		result += parameters.toParameterList

		method.fqn = result
		method.id = createID(method.fqn)
	}
	
	/** 
	 * Sets the EnumValue method type which can either be Getter,Setter
	 * Constructor or Unknown by default
	 * Stores the accessed variable in method.accessesVar
	 */ 
	
	def private void setMethodType(FAMIXMethod method,List<FAMIXAccess> accesses) {
		val Variables = newArrayList
		accesses.filter[accessor.ref.equals(method)].forEach[Variables += variable.ref as FAMIXAttribute] 
		val parentClass = method.parentType.ref as FAMIXStructure
		val methodValueLowerCase = method.value.toLowerCase
		val isConstructor = method.value.equals(parentClass.value)
		val accessedVariableOfGetter  = isGetter(methodValueLowerCase,Variables)
		val	accessedVariableOfSetter = isSetter(methodValueLowerCase,Variables)
		if(accessedVariableOfGetter !== null){
			method.methodType = MethodType::GETTER
			method.accessesVar = famixFactory.createIntegerReference
			method.accessesVar.ref = accessedVariableOfGetter
		}
		if(accessedVariableOfSetter !== null){
			method.methodType = MethodType::SETTER
			method.accessesVar = famixFactory.createIntegerReference
			method.accessesVar.ref = accessedVariableOfSetter
		}
		if(isConstructor){
			method.methodType = MethodType::CONSTRUCTOR 
		}	
	}
	
	/**
	 * checks if method is a getter and if yes it returns the accessed Variable
	 */
	
	def private FAMIXAttribute isGetter(String methodValueLowerCase,List<FAMIXAttribute> Variables) {
		if((Variables.size == 1 && methodValueLowerCase.startsWith("get"))
			||(Variables.size > 1 && Variables.filter["get" + it.value.toLowerCase == methodValueLowerCase].size == 1)) {
			return Variables.get(0)
		} else {
			return null
		}	
	}
	
	/**
	 * checks if method is a setter and if yes it returns the accessed Variable
	 */
	
	def private FAMIXAttribute isSetter(String methodValueLowerCase,List<FAMIXAttribute> Variables) {
		if((Variables.size == 1 && methodValueLowerCase.startsWith("set"))
			||(Variables.size > 1 && Variables.filter["set" + it.value.toLowerCase == methodValueLowerCase].size == 1)) {
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
		methods.filter[it.methodType == MethodType::GETTER || it.methodType == MethodType::SETTER]
			.forEach[
				if(it.accessesVar.ref == attribute) {
					val getterSetter = famixFactory.createIntegerReference	
					getterSetter.ref = it
					attribute.getterSetter += getterSetter
				}
			]		
	}
	
	def private String toParameterList(Iterable<FAMIXParameter> parameters) {
		'''(«FOR p:parameters SEPARATOR  ","»«p.setQualifiedName»«ENDFOR»)'''
	}
		
	def private String setQualifiedName(FAMIXParameter parameter) {
		val ref = parameter.declaredType.ref
		switch(ref) {
			FAMIXPrimitiveType: return ref.value
			FAMIXClass:			return ref.fqn
			FAMIXParameterizableClass: { if(ref.fqn === null) {return ref.value} else { return ref.fqn}}
			FAMIXType: 			return ref.value//TODO container?
			FAMIXParameterizedType: { if(ref.fqn === null) {return ref.value} else { return ref.fqn}}
			FAMIXEnum: 			return ref.fqn
			FAMIXParameterType: return ref.value//TODO container?
			FAMIXAnnotationType: return ref.fqn
			default: log.error("ERROR qualifiedName(FAMIXParameter famixParameter): " + parameter.value) 
		}
		return ""
	}

	def setQualifiedName(FAMIXAttribute attribute) {
		val ref = attribute.parentType.ref
		switch(ref) {
			FAMIXClass: 				attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXParameterizableClass:  attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXEnum: 					attribute.fqn = ref.fqn + "." + attribute.value
			FAMIXAnnotationType:		attribute.fqn = ref.fqn + "." + attribute.value
			default: log.error("ERROR qualifiedName(FAMIXAttribute famixAttribute): " + attribute.value) 
		}
		attribute.id = createID(attribute.fqn)
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
	
	def createID (String fqn) {
		return "ID_"+ sha1Hex(fqn + config.repositoryName + config.repositoryOwner + config.commit)
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
		
		structures.forEach[s|
			switch s {
				FAMIXAnnotationType: if(!annotationTypeFqn.add(s.fqn)) {
					duplicateAnnotationTypes.add(s)
				}
			}
		]
		
		methods.forEach[m|
			if(!methodFqn.add(m.fqn)) {
				duplicateMethods.add(m)
			}
		]
		
		attributes.forEach[a|
			if(!attributeFqn.add(a.fqn)) {
				duplicateAttributes.add(a)
			}
		]
		
		enumValues.forEach[e|
			if(!enumValueFqn.add(e.fqn)) {
				duplicateEnumValues.add(e)
			}
		]
		
		methods.removeAll(duplicateMethods)
		attributes.removeAll(duplicateAttributes)
		enumValues.removeAll(duplicateEnumValues)
		structures.removeAll(duplicateAnnotationTypes)
	}
}