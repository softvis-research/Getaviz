package org.svis.generator.hismo

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
import org.svis.generator.FamixUtils
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import javax.inject.Inject
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.hismo.HISMONamespaceHistory
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMOMethodHistory
import org.svis.xtext.hismo.HISMOAttributeHistory
import org.svis.generator.rd.s2m.Hismo2RD
import org.svis.generator.famix.Famix2Famix
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMOMethodVersion
import org.svis.xtext.hismo.HISMOAttributeVersion
import org.svis.generator.rd.RDSettings.EvolutionRepresentation
import org.svis.generator.SettingsConfiguration

class Hismo2JSON implements IGenerator2 {
	val config = SettingsConfiguration.instance
	@Inject extension FamixUtils util
	Hismo2RD hismo2rd = new Hismo2RD
	val famix = new Famix2Famix
	val log = LogFactory::getLog(class)
	val List<FAMIXAccess> accesses = newArrayList
	val List<FAMIXInvocation> invocations = newArrayList
	val List<FAMIXInheritance> inheritances = newArrayList

	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		val elements = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXElement)
		if (config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_TIME_LINE ||
			config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION) {
			elements.filter(FAMIXNamespace).forEach[famix.setQualifiedName(it)]
			elements.filter(FAMIXClass).forEach[famix.setQualifiedName(it)]
			elements.filter(FAMIXMethod).forEach[famix.setQualifiedName(it)]
			elements.filter(FAMIXAttribute).forEach[famix.setQualifiedName(it)]
		}
		fsa.generateFile("metaData.json", elements.toJSON)
	}

	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Hismo2JSON has started.")
	}

	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Hismo2JSON has finished.")
	}

	def String toJSON(Iterable<FAMIXElement> list) '''
		«FOR el : list BEFORE "[{" SEPARATOR "\n},{" AFTER "}]"»
			«toMetaData(el)»
		«ENDFOR»
	'''

	def dispatch private toMetaData(HISMONamespaceHistory nh) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(nh))»",
		"qualifiedName": "«hismo2rd.qualifiedName(nh)»",
		«IF (!nh.namespaceVersions.isEmpty)»
			"name":			 "«(nh.namespaceVersions.get(0).ref as HISMONamespaceVersion).value»",
		«ELSE»
			"name":			 "",
		«ENDIF»
		"type":			 "FAMIX.Namespace",
		"belongsTo":     ""
		
	'''

	def dispatch private toMetaData(HISMOClassHistory ch) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(ch))»",
		"qualifiedName": "«hismo2rd.qualifiedName(ch)»",
		«IF (!ch.classVersions.isEmpty)»
			"name":			 "«(ch.classVersions.get(0).ref as HISMOClassVersion).value»",
		«ELSE»
			"name:			 "",
		«ENDIF»
		"type":			 "FAMIX.Class",
		"modifiers":     "",
		"subClassOf":    "",
		"superClassOf":  "",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(HISMOMethodHistory mh) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(mh))»",
		"qualifiedName": "«hismo2rd.qualifiedName(mh)»",
		«IF (mh.methodVersions !== null)»
			"name":			 "«(mh.methodVersions.get(0).ref as HISMOMethodVersion).value»",
		«ELSE»
			"name":			 "",
		«ENDIF»
		"type":			 "FAMIX.Method",
		"modifiers":     "",
		"signature":  	 "",
		"calls":		 "",
		"calledBy":		 "",
		"accesses":	 	 "",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(HISMOAttributeHistory ah) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(ah))»",
		"qualifiedName": "«hismo2rd.qualifiedName(ah)»",
		«IF (ah.attributeVersions !== null)»
			"name":			 "«(ah.attributeVersions.get(0).ref as HISMOAttributeVersion).value»",
		«ELSE»
			"name":			 "",
		«ENDIF»
		"type":			 "FAMIX.Attribute",
		"modifiers":     "",
		"declaredType":  "",
		"accessedBy":	 "",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(HISMONamespaceVersion nv) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(nv.parentHistory.ref as HISMONamespaceHistory)+nv.name.toString)»",
		"qualifiedName": "«hismo2rd.qualifiedName(nv.parentHistory.ref as HISMONamespaceHistory)»",
		"name":			 "«nv.value»",
		"type":			 "FAMIX.Namespace",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(HISMOClassVersion cv) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(cv.parentHistory.ref as HISMOClassHistory)+ cv.name.toString)»",
		"qualifiedName": "«hismo2rd.qualifiedName(cv.parentHistory.ref as HISMOClassHistory)»",
		"name":			 "«cv.value»",
		"type":			 "FAMIX.Class",
		"modifiers":     "",
		"subClassOf":    "",
		"superClassOf":  "",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(HISMOMethodVersion mv) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(mv.parentHistory.ref as HISMOMethodHistory)+mv.name.toString)»",
		"qualifiedName": "«escapeHtml4(hismo2rd.qualifiedName(mv.parentHistory.ref as HISMOMethodHistory))»",
		"name":			 "«mv.value»",
		"type":			 "FAMIX.Method",
		"modifiers":     "",
		"signature":  	 "«escapeHtml4((mv.versionEntity.ref as FAMIXMethod).signature)»",
		"calls":		 "",
		"calledBy":		 "",
		"accesses":	 	 "",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(HISMOAttributeVersion av) '''
		"id":			 "«famix.createID(hismo2rd.qualifiedName(av.parentHistory.ref as HISMOAttributeHistory)+av.name.toString)»",
		"qualifiedName": "«hismo2rd.qualifiedName(av.parentHistory.ref as HISMOAttributeHistory)»",
		"name":			 "«av.value»",
		"type":			 "FAMIX.Attribute",
		"modifiers":     "",
		"declaredType":  "",
		"accessedBy":	 "",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(FAMIXNamespace p) '''
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

	def dispatch private toMetaData(FAMIXClass c) '''
		"id":            "«c.id»",
		"qualifiedName": "«c.fqn»",
		"name":          "«c.value»",
		"type":          "FAMIX.Class",
		"modifiers":     "«c.modifiers.removeBrackets»",
		"subClassOf":    "«c.superClasses»",
		"superClassOf":  "«c.subClasses»",
		"belongsTo":     "«c.container.ref.id»"
	'''

	def dispatch private toMetaData(FAMIXAttribute a) '''
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

	def dispatch private toMetaData(FAMIXMethod m) '''
		"id":            "«m.id»",
		"qualifiedName": "«escapeHtml4(m.fqn)»",
		"name":          "«m.value»",
		"type":          "FAMIX.Method",
		"modifiers":     "«m.modifiers.removeBrackets»",
		"signature":  	 "«escapeHtml4(m.signature)»",
		"calls":		 "«m.calls»",
		"calledBy":		 "«m.calledBy»",
		"accesses":	 	 "«m.accesses»",
		"belongsTo":     "«m.parentType.ref.id»"
	'''

	def dispatch private toMetaData(FAMIXFileAnchor el) '''
	'''

	def private getSuperClasses(FAMIXElement element) {
		val tmp = newArrayList
		inheritances.filter[subclass.ref === element].forEach[tmp += superclass.ref.id]
		return tmp.removeBrackets
	}

	def private getSubClasses(FAMIXElement element) {
		val tmp = newArrayList
		inheritances.filter[superclass.ref === element].forEach[tmp += subclass.ref.id]
		return tmp.removeBrackets
	}

	def private getCalls(FAMIXMethod method) {
		val tmp = newArrayList
		invocations.filter[sender.ref === method].forEach[tmp += candidates.ref.id]
		return tmp.removeBrackets
	}

	def private getCalledBy(FAMIXMethod method) {
		val tmp = newArrayList
		invocations.filter[candidates.ref === method].forEach[tmp += sender.ref.id]
		return tmp.removeBrackets
	}

	def private getAccessedBy(FAMIXAttribute attribute) {
		val tmp = newArrayList
		accesses.filter[variable.ref === attribute].forEach[tmp += accessor.ref.id]
		return tmp.removeBrackets
	}

	def private getAccesses(FAMIXMethod method) {
		val tmp = newArrayList
		accesses.filter[accessor.ref === method].forEach[tmp += variable.ref.id]
		return tmp.removeBrackets
	}

	def private getType(FAMIXElement element) {
		switch (element) {
			FAMIXPrimitiveType:
				return element.value
			FAMIXClass:
				return element.value
			FAMIXParameterizableClass:
				return element.value
			FAMIXType:
				return element.value
			FAMIXEnum:
				return element.value
			FAMIXParameterType:
				return element.value
			FAMIXParameterizedType:
				if (element.arguments === null) {
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
			switch (ref) {
				FAMIXPrimitiveType: types += " " + ref.value
				FAMIXClass: types += " " + ref.value
				FAMIXParameterizableClass: types += " " + ref.value
				FAMIXParameterizedType: types += " " + ref.value
				FAMIXAnnotationType: types += " " + ref.value
				FAMIXEnum: types += " " + ref.value
				FAMIXType: types += " " + ref.value
				FAMIXParameterType: types += " " + ref.value
				default: log.warn("Unknown type: " + ref)
			}
		}
		return types.trim
	}
}
