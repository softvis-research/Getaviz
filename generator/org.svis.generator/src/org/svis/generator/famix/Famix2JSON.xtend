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
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.FamixParser
import org.svis.xtext.famix.FAMIXReference

//ABAP
import org.svis.xtext.famix.FAMIXReport
import org.svis.xtext.famix.FAMIXDataElement
import org.svis.xtext.famix.FAMIXDomain
import org.svis.xtext.famix.FAMIXTable
import org.svis.xtext.famix.FAMIXABAPStruc
import org.svis.xtext.famix.FAMIXStrucElement
import org.svis.xtext.famix.FAMIXFunctionGroup
import org.svis.xtext.famix.FAMIXFunctionModule
import org.svis.xtext.famix.FAMIXFormroutine
import org.svis.xtext.famix.FAMIXMacro
import org.svis.xtext.famix.FAMIXMessageClass
import org.svis.xtext.famix.FAMIXTableType
import org.svis.xtext.famix.FAMIXTableTypeElement
import org.svis.xtext.famix.FAMIXTypeOf
import org.svis.xtext.famix.FAMIXTableElement


class Famix2JSON implements IGenerator2 {

	@Inject extension FamixUtils util
	
	val config = SettingsConfiguration.instance;
	val log = LogFactory::getLog(class)
	val List<FAMIXAccess> accesses = newArrayList
	val List<FAMIXInvocation> invocations = newArrayList
	val List<FAMIXInheritance> inheritances = newArrayList
	val List<FAMIXTypeOf> typeOfs = newArrayList
	val List<FAMIXReference> references = newArrayList
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		val elements = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXElement)
		accesses.addAll(elements.filter(FAMIXAccess))
		invocations.addAll(elements.filter(FAMIXInvocation))
		inheritances.addAll(elements.filter(FAMIXInheritance))
		references.addAll(elements.filter(FAMIXReference))
		typeOfs.addAll(elements.filter(FAMIXTypeOf))
		
		elements.removeAll(elements.filter(FAMIXFileAnchor))
		
		elements.removeAll(accesses)
		elements.removeAll(invocations)
		elements.removeAll(inheritances)
		elements.removeAll(typeOfs)
		elements.removeAll(references)
		
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
		"belongsTo":     "«p.parentScope.ref.id»",
		«ELSE»
		"belongsTo":     "root",
		«ENDIF»
		"iteration":     "«p.iteration»"
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
		«IF c.isInterface == "true"»
		"type": 		 "FAMIX.Interface",
		«ELSE»
		"type": 		 "FAMIX.Class",
		«ENDIF»
		"modifiers":     "«c.modifiers.removeBrackets»",
		"subClassOf":    "«c.superClasses»",
		"superClassOf":  "«c.subClasses»",
		"belongsTo":     "«c.container.ref.id»",
		"antipattern":	 "«toString(c.antipattern)»",
		"roles":	 	 "«toString2(c.antipattern, c)»",
		«IF c.scc !== null»
		"component":	 "«((c.scc.ref) as FAMIXComponent).id»",
		«ELSE»
		"component":	 "",
		«ENDIF»
		"iteration":     "«c.iteration»"
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
		"id":             "«a.id»",
		"qualifiedName":  "«a.fqn»",
		"name":           "«a.value»",
		"type":           "FAMIX.Attribute",
		"modifiers":      "«a.modifiers.removeBrackets»",
		«IF config.parser == FamixParser::ABAP»
		«IF a.sourceCodeDefined !== null»
		"sourceCodeDefined": "«a.sourceCodeDefined»",
		«ENDIF»
		«IF a.dataType !== null»
		"dataType":   	  "«a.dataType»",
		«ENDIF»	
		«IF a.typeOf !== null»
		"typeOf":   	  "«a.typeOf»",
		«ENDIF»	
		«ENDIF»	
		"accessedBy":	  "«a.accessedBy»",
		"belongsTo":      "«a.parentType.ref.id»",
		"iteration":      "«a.iteration»"
	'''
	
	def dispatch private toMetaData(FAMIXMethod m)'''
		"id":            "«m.id»",
		"qualifiedName": "«escapeHtml4(m.fqn)»",
		"name":          "«m.value»",
		"type":          "FAMIX.Method",
		"modifiers":     "«m.modifiers.removeBrackets»",
		«IF m.numberOfStatements > 0»
		"numberOfStatements": "«m.numberOfStatements»",
		«ENDIF»
		«IF m.declaredType !== null»
		"signature":  	 "«m.declaredType.ref.type» «m.signature.replace('"', '\\"')»",
		«ELSE»
		"signature":  	 "void «m.signature.replace('"', '\\"')»",
		«ENDIF»		
		"calls":		 "«m.calls»",
		"calledBy":		 "«m.calledBy»",
		"accesses":	 	 "«m.accesses»",
		"belongsTo":     "«m.parentType.ref.id»",
		"iteration":     "«m.iteration»"
	'''
	
	
	def dispatch private toMetaData(FAMIXEnum e)'''
		"id":            "«e.id»",
		"qualifiedName": "«e.fqn»",
		"name":          "«e.value»",
		"type":          "FAMIX.Enum",
		"modifiers":     "«e.modifiers.removeBrackets»",
		"belongsTo":     "«e.container.ref.id»"
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
	'''
	
	
	def dispatch private toMetaData(FAMIXFileAnchor el) '''
		
	'''
	
	//ABAP	
	def dispatch private toMetaData(FAMIXReport r)'''
		"id":            "«r.id»",
		"qualifiedName": "«r.fqn»",
		"name":          "«r.value»",
		"type":          "FAMIX.Report",
		"belongsTo":     "«r.container.ref.id»",
		"calledBy":		 "«r.calledBy»",
		«IF r.numberOfStatements > 0»
		"numberOfStatements": "«r.numberOfStatements»",
		«ENDIF»
		"iteration": 	 "«r.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXDataElement de)'''
		"id":            "«de.id»",
		"qualifiedName": "«de.fqn»",
		"name":          "«de.value»",
		"type":          "FAMIX.DataElement",
		"belongsTo":     "«de.container.ref.id»",
		«IF de.domain !== null»
		"domain":		 "«de.domain»",
		«ENDIF»
		"typeOf":		 "«de.typeOf»",
		"iteration": 	 "«de.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXTableType tt)'''
		"id":            "«tt.id»",
		"qualifiedName": "«tt.fqn»",
		"name":          "«tt.value»",
		"type":          "FAMIX.TableType",
		"belongsTo":     "«tt.container.ref.id»",
		"typeOf":		 "«tt.typeOf»",
		"iteration": 	 "«tt.iteration»"
	'''
	
	def dispatch private toMetaData(FAMIXTableTypeElement tty)'''
		"id":            "«tty.id»",
		"qualifiedName": "«tty.fqn»",
		"name":          "«tty.value»",
		"type":          "FAMIX.TableTypeElement",
		"belongsTo":     "«tty.tableType.ref.id»",
		"structure":	 "«tty.container.ref.id»",
		"typeOf":		 "«tty.typeOf»",
		"iteration": 	 "«tty.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXDomain d)'''
		"id":            "«d.id»",
		"qualifiedName": "«d.fqn»",
		"name":          "«d.value»",
		"type":          "FAMIX.Domain",
		"belongsTo":     "«d.container.ref.id»",
		"datatype":		 "«d.datatype»",
		"length":		 "«d.length»",
		"decimals":		 "«d.decimals»",
		"iteration": 	 "«d.iteration»"
	'''
	 
	//ABAP 
	def dispatch private toMetaData(FAMIXTable t)'''
		"id":            "«t.id»",
		"qualifiedName": "«t.fqn»",
		"name":          "«t.value»",
		"type":          "FAMIX.Table",
		"belongsTo":     "«t.container.ref.id»",
		"iteration": 	 "«t.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXTableElement te)'''
		"id":            "«te.id»",
		"qualifiedName": "«te.fqn»",
		"name":          "«te.value»",
		"type":          "FAMIX.TableElement",
		"belongsTo":     "«te.container.ref.id»",
		«IF te.dataElement !== null»
		"dataElement":   "«te.dataElement»",
		«ENDIF»
		"typeOf":		 "«te.typeOf»",
		«IF te.structure !== null»
		"structure": 	 "«te.structure»",
		«ENDIF»
		"iteration": 	 "«te.iteration»"
	'''
	
	//ABAP 
	def dispatch private toMetaData(FAMIXABAPStruc abs)'''
		"id":            "«abs.id»",
		"qualifiedName": "«abs.fqn»",
		"name":          "«abs.value»",
		"type":          "FAMIX.ABAPStructure",
		"belongsTo":     "«abs.container.ref.id»",
		"iteration": 	 "«abs.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXStrucElement se)'''
		"id":            "«se.id»",
		"qualifiedName": "«se.fqn»",
		"name":          "«se.value»",
		"type":          "FAMIX.StrucElement",
		"belongsTo":     "«se.container.ref.id»",
		"typeOf":		 "«se.typeOf»",
		«IF se.structure !== null»
		"structure": 	 "«se.structure»",
		«ENDIF»
		"iteration": 	 "«se.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXFunctionGroup fg)'''
		"id":            "«fg.id»",
		"qualifiedName": "«fg.fqn»",
		"name":          "«fg.value»",
		"type":          "FAMIX.FunctionGroup",
		"belongsTo":     "«fg.container.ref.id»",
		"iteration": 	 "«fg.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXFunctionModule fm)'''
		"id":            "«fm.id»",
		"qualifiedName": "«fm.fqn»",
		"name":          "«fm.value»",
		"type":          "FAMIX.FunctionModule",
		"belongsTo":     "«fm.parentType.ref.id»",
		"calledBy":		 "«fm.calledBy»",
		«IF fm.numberOfStatements > 0»
		"numberOfStatements": "«fm.numberOfStatements»",
		«ENDIF»
		"iteration": 	 "«fm.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXFormroutine fr)'''
		"id":            "«fr.id»",
		"qualifiedName": "«fr.fqn»",
		"name":          "«fr.value»",
		"type":          "FAMIX.Formroutine",
		"belongsTo":     "«fr.parentType.ref.id»",
		"calledBy":		 "«fr.calledBy»",
		«IF fr.numberOfStatements > 0»
		"numberOfStatements": "«fr.numberOfStatements»",
		«ENDIF»
		"iteration": 	 "«fr.iteration»"
	'''

	//ABAP
	def dispatch private toMetaData(FAMIXMacro ma)'''
		"id":			 "«ma.id»",
		"qualifiedName": "«ma.fqn»",
		"name":          "«ma.value»",
		"type":          "FAMIX.Macro",
		"belongsTo":     "«ma.parentType.ref.id»",
		"calledBy":		 "«ma.calledBy»",
		«IF ma.numberOfStatements > 0»
		"numberOfStatements": "«ma.numberOfStatements»",
		«ENDIF»
		"iteration": 	 "«ma.iteration»
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXMessageClass mc)'''
		"id":            "«mc.id»",
		"qualifiedName": "«mc.fqn»",
		"name":          "«mc.value»",
		"type":          "FAMIX.MessageClass",
		"numberOfMessages": "«mc.numberOfMessages»",
		"belongsTo":     "«mc.container.ref.id»",
		"iteration": 	 "«mc.iteration»"
	'''
	
	//ABAP
	def dispatch private toMetaData(FAMIXTypeOf to)'''
		"id":            "«to.id»",
		"type":          "FAMIX.TypeOf",
		"element":     	 "«to.element.ref.id»",
		"typeOf": 	     "«to.typeOf.ref.id»"
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
		references.filter[source.ref.id == method.id].forEach[ tmp += target.ref.id ]
		
		return tmp.removeBrackets
	}
	
	def private getCalledBy(FAMIXMethod method) {
		val tmp = newArrayList
		invocations.filter[candidates.ref === method].forEach[ tmp += sender.ref.id ]
		references.filter[target.ref.id == method.id].forEach[ tmp += source.ref.id ]

		return tmp.removeBrackets
	}
	
	def private getCalledBy(FAMIXElement element){
		val tmp = newArrayList
		references.filter[target.ref.id == element.id].forEach[ tmp += source.ref.id ]		
		return tmp.removeBrackets
	}
	
	def private getTypeOf(FAMIXElement famixElement){
		val tmp = newArrayList
		typeOfs.filter[element.ref.id == famixElement.id].forEach[ tmp += typeOf.ref.id ]
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