package org.svis.generator.famix

import java.util.List
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.IGenerator2
import org.svis.xtext.famix.FAMIXAnnotationType
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXElement
import org.svis.xtext.famix.FAMIXEnum
import org.svis.xtext.famix.FAMIXEnumValue
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXInvocation
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXParameterType
import org.svis.xtext.famix.FAMIXParameterizableClass
import org.svis.xtext.famix.FAMIXParameterizedType
import org.svis.xtext.famix.FAMIXPrimitiveType
import org.svis.xtext.famix.FAMIXType
import org.svis.xtext.famix.IntegerReference
import org.svis.xtext.famix.FAMIXAccess
import org.eclipse.xtext.EcoreUtil2
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import javax.inject.Inject
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.famix.FAMIXAntipattern
import org.svis.xtext.famix.FAMIXRole
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.famix.FAMIXComponent
import org.svis.generator.FamixUtils

class Famix2JSON implements IGenerator2 {

	@Inject extension FamixUtils util

	val log = LogFactory::getLog(class)
	val List<FAMIXAccess> accesses = newArrayList
	val List<FAMIXInvocation> invocations = newArrayList
	val List<FAMIXInheritance> inheritances = newArrayList
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		val elements = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXElement)
		accesses.addAll(elements.filter(FAMIXAccess))
		invocations.addAll(elements.filter(FAMIXInvocation))
		inheritances.addAll(elements.filter(FAMIXInheritance))
		elements.removeAll(elements.filter(FAMIXFileAnchor))
		
		elements.removeAll(accesses)
		elements.removeAll(invocations)
		elements.removeAll(inheritances)
		
		fsa.generateFile("metaData.json", elements.toJSON)
	}
	
	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Famix2JSON has started.")
	}
	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Famix2JSON has finished.")
	}
	
	def String toJSON2 (Iterable<FAMIXAntipattern> list)  '''
		«FOR el : list BEFORE "[{" SEPARATOR "\n},{" AFTER "}]"»
			«toMetaData(el)»
		«ENDFOR»
	'''
	
	def String toJSON (Iterable<FAMIXElement> list)  '''
		«FOR el : list BEFORE "[{" SEPARATOR "\n},{" AFTER "}]"»
			«toMetaData(el)»
		«ENDFOR»
	'''
	def dispatch private toMetaData(FAMIXNamespace p)'''
		"id":            "«p.id»",
		"qualifiedName": "«p.fqn»",
		"name":          "«p.value»",
		"type":          "FAMIX.Namespace",
		«IF p.parentScope !== null»
		"belongsTo":     "«p.parentScope.ref.id»"
		«ELSE»
		"belongsTo":     "root"
		«ENDIF»
	'''
	
	def dispatch private toMetaData(FAMIXAntipattern antipattern) '''
		"id":				"«antipattern.id»",
		"qualifiedName": 	"antipattern.«antipattern.type».«antipattern.name»",
		"name":         	"antipattern.«antipattern.type».«antipattern.name»",
		"type": 			"«antipattern.type»",
		"belongsTo": 		""
	'''
	
	def dispatch private toMetaData(FAMIXComponent component) '''
		"id":				"«component.id»",
		"qualifiedName": 	"«component.id»",
		"name":         	"«component.id»",
		"type": 			"component",
		"belongsTo": 		""
	'''
	
	def dispatch private toMetaData(FAMIXClass c)'''
		"id":            "«c.id»",
		"qualifiedName": "«c.fqn»",
		"name":          "«c.value»",
		"type":          "FAMIX.Class",
		"modifiers":     "«c.modifiers.removeBrackets»",
		"subClassOf":    "«c.superClasses»",
		"superClassOf":  "«c.subClasses»",
		"belongsTo":     "«c.container.ref.id»",
		"antipattern":	 "«toString(c.antipattern)»",
		"roles":	 	 "«toString2(c.antipattern, c)»",
		«IF c.scc !== null»
		"component":	 "«((c.scc.ref) as FAMIXComponent).id»"
		«ELSE»
		"component":	 ""
		«ENDIF»
	'''
	
	def dispatch private toMetaData(FAMIXParameterizableClass pc)'''
		"id":            "«pc.id»",
		"qualifiedName": "«pc.fqn»",
		"name":          "«pc.value»",
		"type":          "FAMIX.ParameterizableClass",
		"modifiers":     "«pc.modifiers.removeBrackets»",
		"subClassOf":    "«pc.superClasses»",
		"superClassOf":  "«pc.subClasses»",
		"belongsTo":     "«pc.container.ref.id»"
«««		"antipattern":	 "«toString(pc.antipattern)»"
	'''
	
	def dispatch private toMetaData(FAMIXAttribute a)'''
		"id":            "«a.id»",
		"qualifiedName": "«a.fqn»",
		"name":          "«a.value»",
		"type":          "FAMIX.Attribute",
		"modifiers":     "«a.modifiers.removeBrackets»",
		«IF a.declaredType !== null»
		"declaredType":  "«a.declaredType.ref.type»",
		«ENDIF»		
		"accessedBy":	 "«a.accessedBy»",
		"belongsTo":     "«a.parentType.ref.id»"
	'''
	
	def dispatch private toMetaData(FAMIXMethod m)'''
		"id":            "«m.id»",
		"qualifiedName": "«escapeHtml4(m.fqn)»",
		"name":          "«m.value»",
		"type":          "FAMIX.Method",
		"modifiers":     "«m.modifiers.removeBrackets»",
		«IF m.declaredType !== null»
		"signature":  	 "«m.declaredType.ref.type» «m.signature.replace('"', '\\"')»",
		«ELSE»
		"signature":  	 "void «m.signature.replace('"', '\\"')»",
		«ENDIF»		
		"calls":		 "«m.calls»",
		"calledBy":		 "«m.calledBy»",
		"accesses":	 	 "«m.accesses»",
		«IF(FAMIXSettings::ANTIPATTERN)»
		"antipattern":	 "«m.antipattern»",
		«ENDIF»
		"belongsTo":     "«m.parentType.ref.id»"
	'''
	
	def dispatch private toMetaData(FAMIXEnum e)'''
		"id":            "«e.id»",
		"qualifiedName": "«e.fqn»",
		"name":          "«e.value»",
		"type":          "FAMIX.Enum",
		"modifiers":     "«e.modifiers.removeBrackets»",
		"belongsTo":     "«e.container.ref.id»"
«««		"antipattern":	 "«toString(e.antipattern)»"
	'''
	
	def dispatch private toMetaData(FAMIXEnumValue ev)'''
		"id":            "«ev.id»",
		"qualifiedName": "«ev.fqn»",
		"name":          "«ev.value»",
		"type":          "FAMIX.EnumValue",
		"belongsTo":     "«ev.parentEnum.ref.id»"
	'''
	
	def dispatch private toMetaData(FAMIXAnnotationType el)'''
		"id":            "«el.id»",
		"qualifiedName": "«el.fqn»",
		"name":          "«el.value»",
		"type":          "FAMIX.AnnotationType",
		"modifiers":     "«el.modifiers.removeBrackets»",
		"subClassOf":    "",
		"superClassOf":  "",
		"belongsTo":     "«el.container.ref.id»"
«««		"antipattern":	 "«toString(el.antipattern)»"
	'''
	
	def dispatch private toMetaData(FAMIXFileAnchor el) '''
		
	'''

	def private toString (List<IntegerReference> list) { 
    	val tmp = newArrayList 
    	list.forEach[el| 
      	tmp += el.ref.id 
    	]     
    	return tmp.removeBrackets
	}
	
	def private toString2 (List<IntegerReference> list, FAMIXStructure s) { 
    	val tmp = newArrayList 
    		list.forEach[el|
    			val antipattern = el.ref as FAMIXAntipattern
    			val roles = antipattern.roles as List<IntegerReference>
    			roles.forEach[ r |
    				val role = r.ref as FAMIXRole
    				if(role.element.ref == s) {
    					tmp += role.role
   					}
    			]
    			 
 	 		]     
    	return tmp.removeBrackets
   	}
   	
	def private getSuperClasses(FAMIXElement element) {
		val tmp = newArrayList
		inheritances.filter[subclass.ref === element].forEach[ tmp += superclass.ref.id ]
		return tmp.removeBrackets
	}
	
	def private getSubClasses(FAMIXElement element) {
		val tmp = newArrayList
		inheritances.filter[superclass.ref === element].forEach[ tmp += subclass.ref.id ]
		return tmp.removeBrackets
	}
	
	def private getCalls(FAMIXMethod method) {
		val tmp = newArrayList
		invocations.filter[sender.ref === method].forEach[ tmp += candidates.ref.id ]
		return tmp.removeBrackets
	}
	
	def private getCalledBy(FAMIXMethod method) {
		val tmp = newArrayList
		invocations.filter[candidates.ref === method].forEach[ tmp += sender.ref.id ]
		return tmp.removeBrackets
	}
	
	def private getAccessedBy(FAMIXAttribute attribute) {
		val tmp = newArrayList
		accesses.filter[variable.ref === attribute].forEach[ tmp += accessor.ref.id ]
		return tmp.removeBrackets
	}
	
	def private getAccesses(FAMIXMethod method) {
		val tmp = newArrayList
		accesses.filter[accessor.ref === method].forEach[ tmp += variable.ref.id ]
		return tmp.removeBrackets
	}
	
	def private getType(FAMIXElement element) {
		switch(element) {
			FAMIXPrimitiveType: 		return element.value
			FAMIXClass:					return element.value
			FAMIXParameterizableClass: 	return element.value
			FAMIXType: 					return element.value
			FAMIXEnum: 					return element.value
			FAMIXParameterType: 		return element.value
			FAMIXParameterizedType:
				if(element.arguments === null){
					return element.value
				} else {
					return element.value + "<" + element.arguments.types + ">"
				}
		}
	}
	
	def private getTypes(List<IntegerReference> irefs) {
		var types = ""
		for (iref : irefs) {
			val ref = iref.ref
			switch(ref) {
				FAMIXPrimitiveType: 		types += " " + ref.value
				FAMIXClass: 				types += " " + ref.value
				FAMIXParameterizableClass: 	types += " " + ref.value
				FAMIXParameterizedType: 	types += " " + ref.value
				FAMIXAnnotationType:		types += " " + ref.value
				FAMIXEnum: 					types += " " + ref.value
				FAMIXType: 					types += " " + ref.value
				FAMIXParameterType: 		types += " " + ref.value
				default: log.warn("Unknown type: " + ref)
			}
		}
		return types.trim
	}
}