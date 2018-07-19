package org.svis.generator.hismo.m2t

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
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import javax.inject.Inject
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.hismo.HISMONamespaceHistory
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMOMethodHistory
import org.svis.xtext.hismo.HISMOAttributeHistory
import org.svis.generator.famix.Famix2Famix
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMOMethodVersion
import org.svis.xtext.hismo.HISMOAttributeVersion
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.famix.FAMIXAntipattern
import org.svis.xtext.famix.FAMIXRole
import org.svis.xtext.famix.FAMIXComponent
import org.svis.xtext.famix.FAMIXPath
import org.svis.generator.hismo.HismoUtils
import org.svis.generator.FamixUtils
import org.svis.xtext.hismo.HISMOIssue
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.time.LocalDate
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.EvolutionRepresentation
import org.svis.generator.SettingsConfiguration.ShowVersions

class Hismo2JSON implements IGenerator2 {

	@Inject extension HismoUtils hismoUtil
	@Inject extension FamixUtils famixUtil
	val famix = new Famix2Famix
	val log = LogFactory::getLog(class)
	val List<FAMIXAccess> accesses = newArrayList
	val List<FAMIXInvocation> invocations = newArrayList
	val List<FAMIXInheritance> inheritances = newArrayList
	val List<FAMIXPath> paths = newArrayList
	val config = SettingsConfiguration.instance;
	

	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		val elements = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXElement)
		elements.removeUnnecessaryFamixElements
		if (config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_TIME_LINE ||
			config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION) {
			elements.filter(FAMIXNamespace).forEach[famix.qualifiedName = it]
			elements.filter(FAMIXClass).forEach[famix.qualifiedName = it]
			elements.filter(FAMIXMethod).forEach[famix.qualifiedName = it]
			elements.filter(FAMIXAttribute).forEach[famix.qualifiedName = it]
		}
		paths += elements.filter(FAMIXPath)
		val roles = elements.filter(FAMIXRole)
		elements.removeAll(roles)
		// elements.removeAll(paths)
		if (config.showVersions == ShowVersions::LATEST) {
			val latestClassVersions = newArrayList
			val List<HISMONamespaceVersion> latestNamespaceVersions = newArrayList
			val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			var classVersions = elements.filter(HISMOClassVersion)
			var namespaceVersions = elements.filter(HISMONamespaceVersion)
			elements.filter(HISMOClassHistory).forEach [ history |
				var versions = history.classVersions.map[ref as HISMOClassVersion].clone
				Collections::sort(versions, [ s1, s2 |
					LocalDate::parse(s1.timestamp, formatter).compareTo(LocalDate::parse(s2.timestamp, formatter))
				])
				latestClassVersions += versions.last
			]
			elements.filter(HISMONamespaceHistory).forEach [ history |
				val versions = history.namespaceVersions.map[ref as HISMONamespaceVersion].clone
				Collections::sort(versions, [ s1, s2 |
					LocalDate::parse(s1.timestamp, formatter).compareTo(LocalDate::parse(s2.timestamp, formatter))
				])
				latestNamespaceVersions += versions.last
			]
			elements.removeAll(classVersions)
			elements.removeAll(namespaceVersions)
			elements.addAll(latestClassVersions)
			elements.addAll(latestNamespaceVersions)
		}
		if (config.showHistories == false) {
			elements.removeIf[it instanceof HISMONamespaceHistory]
			elements.removeIf[it instanceof HISMOClassHistory]
			elements.removeIf[it instanceof HISMOMethodHistory]
			elements.removeIf[it instanceof HISMOAttributeHistory]
		}
		fsa.generateFile("metaData.json", elements.toJSON)
	}

	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Hismo2JSON has started.")
	}

	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Hismo2JSON has finished.")
	}

	def private removeUnnecessaryFamixElements(List<FAMIXElement> elements) {
		elements.removeAll(elements.filter(FAMIXNamespace))
		elements.removeAll(elements.filter(FAMIXClass))
		elements.removeAll(elements.filter(FAMIXMethod))
		elements.removeAll(elements.filter(FAMIXAttribute))
		elements.removeAll(elements.filter(FAMIXFileAnchor))
	}

	def String toJSON(Iterable<FAMIXElement> list) '''
		«FOR el : list BEFORE "[{" SEPARATOR "\n},{" AFTER "}]"»
			«toMetaData(el)»
		«ENDFOR»
	'''

	def dispatch private toMetaData(HISMOIssue issue) '''
		"id":				"«issue.value»",
		"type":				"issue",
		"open":				"«issue.open»",
		"security":			"«issue.security»"
	'''

	def dispatch private toMetaData(FAMIXPath path) '''
		"id":				"«path.id»",
		"name":				"«path.id»",
		"qualifiedName":	"«path.id»",
		"type":				"path",
		"start":			"«path.start.ref.id»",
		"end":				"«path.end.ref.id»",
		"role":				"«path.role.checkNull»",
		«IF path.antipattern !== null»
		"belongsTo":    "«path.antipattern.ref.id.checkNull»"
		«ELSE»
		"belongsTo":	""
		«ENDIF»
	'''

	def dispatch private toMetaData(FAMIXAntipattern antipattern) '''
		"id":				"«antipattern.id»",
		"qualifiedName": 	"«antipattern.fqn»",
		"name":         	"«antipattern.fqn»",
		"type": 			"stk",
		"path":				"«toString(antipattern.path)»",
		"belongsTo": 		"root",
		"version":			"«antipattern.version»",
		"versions":			"«antipattern.versions.removeBrackets»"
	'''

	def dispatch private toMetaData(FAMIXComponent component) '''
		"id":				"«component.id»",
		"qualifiedName": 	"«component.fqn»",
		"name":         	"«component.fqn»",
		"type": 			"component",
		"components":		"«toString(component.realcomponents)»",
		"path":				"«toString(component.path)»",
		"belongsTo": 		"root",
		"version":			"«component.version»",
		"versions":			"«component.versions.removeBrackets»"
	'''

	def dispatch private toMetaData(HISMONamespaceHistory nh) '''
		"id":			 "«famix.createID(nh.qualifiedName)»",
		"qualifiedName": "«nh.qualifiedName»",
		«IF (!nh.namespaceVersions.isEmpty)»
			"name":			 "«(nh.namespaceVersions.get(0).ref as HISMONamespaceVersion).value»",
		«ELSE»
			"name":			 "",
		«ENDIF»
		"type":			 "FAMIX.Namespace",
		"belongsTo":     ""
		
	'''

	def dispatch private toMetaData(HISMOClassHistory ch) '''
		"id":			 "«famix.createID(ch.qualifiedName)»",
		"qualifiedName": "«ch.qualifiedName»",
		«IF (!ch.classVersions.isEmpty)»
			"name":			 "«(ch.classVersions.get(0).ref as HISMOClassVersion).value»",
		«ELSE»
			"name:			 "",
		«ENDIF»
		"type":			 "FAMIX.Class",
		"modifiers":     "",
		"subClassOf":    "",
		"superClassOf":  "",
		"belongsTo":     "",
		"reaches":		 "",
		"antipattern":	 "",
		"roles":	 	 "",
		"issues":		 ""
	'''

	def dispatch private toMetaData(HISMOMethodHistory mh) '''
		"id":			 "«famix.createID(mh.qualifiedName)»",
		"qualifiedName": "«mh.qualifiedName»",
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
		"id":			 "«famix.createID(ah.qualifiedName)»",
		"qualifiedName": "«ah.qualifiedName»",
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
		"id":			 "«nv.id»",
		"qualifiedName": "«nv.value»",
		"name":			 "«nv.value»",
		«IF (config.containsProjects)»
			"type":			 "FAMIX.Project",
		«ELSE»
			"type":			 "FAMIX.Namespace",
		«ENDIF»
		"version":		 "«nv.commitId»",
		«IF (nv.container !== null)»
			"belongsTo":     "«nv.container.ref.id»"
		«ELSE»
			"belongsTo":	 "root"
		«ENDIF»
	'''

	def dispatch private toMetaData(HISMOClassVersion cv) '''
		«val history = cv.parentHistory.ref as HISMOClassHistory»
		"id":			 "«cv.id»",
		"qualifiedName": "«cv.value»",
		"name":			 "«cv.value»",
		"type":			 "FAMIX.Class",
		"modifiers":     "",
		"subClassOf":    "",
		"superClassOf":  "",
		«IF (cv.container !== null)»
			"belongsTo":     "«cv.container.ref.id»",
		«ELSE»
			"belongsTo":	 "",
		«ENDIF»
		"version":		 "«cv.commitId»",
		«IF cv.antipattern !== null»
			"antipattern":	 "«FOR pattern : cv.antipattern SEPARATOR ","»«(pattern.ref as FAMIXAntipattern).id»«ENDFOR»",
		«ELSE»
			"antipattern":	 "",
		«ENDIF»
		"reaches":		 "«getPaths(cv)»",
		"roles":	 	 "«toString2(cv.antipattern, cv)»",
		«IF cv.scc !== null»
			"component":	 "«((cv.scc.ref) as FAMIXComponent).id»",
		«ELSE»
			"component":	 "",
		«ENDIF»
		«IF cv.betweennessCentrality !== null»
			"betweennessCentrality":	«cv.betweennessCentrality»,
		«ELSE»
			"betweennessCentrality":	"",
		«ENDIF»
		"numberOfClosedIssues":	«history.avgNumberOfClosedIncidents»,
		"numberOfOpenIssues": «history.avgNumberOfOpenIncidents»,
		"numberOfClosedSecurityIssues": «history.numberOfClosedSecurityIncidents»,
		"numberOfOpenSecurityIssues": 	«history.numberOfOpenSecurityIncidents»,
		"issues": "«(cv.parentHistory.ref as HISMOClassHistory).issues.removeBrackets»"
	'''

	def dispatch private toMetaData(HISMOMethodVersion mv) '''
		"id":			 "«mv.id»",
		"qualifiedName": "«escapeHtml4(qualifiedName(mv.parentHistory.ref as HISMOMethodHistory))»",
		"name":			 "«mv.value»",
		"type":			 "FAMIX.Method",
		"modifiers":     "",
		"signature":  	 "«escapeHtml4((mv.versionEntity.ref as FAMIXMethod).signature)»",
		"calls":		 "",
		"calledBy":		 "",
		"accesses":	 	 "",
		"version":		 "«mv.commitId»",
		"belongsTo":     ""
	'''

	def dispatch private toMetaData(HISMOAttributeVersion av) '''
		"id":			 "«av.id»",
		"qualifiedName": "«qualifiedName(av.parentHistory.ref as HISMOAttributeHistory)»",
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
		"belongsTo":     "«c.container.ref.id»",
		"antipattern":	 "«toString(c.antipattern)»",
		"roles":	 	 "«toString2(c.antipattern, c)»",
		"issues":		 ""
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

	def private getPaths(HISMOClassVersion version) {
		val targets = newArrayList
		paths.filter[start.ref == version].forEach [ p |
			targets += p.end
		]
		return toString(targets)
	}

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

	def private toString(List<IntegerReference> list) {
		val tmp = newArrayList
		list.forEach [ el |
			tmp += el.ref.id
		]
		return tmp.removeBrackets
	}

	def private toString2(List<IntegerReference> list, FAMIXStructure s) {
		val tmp = newArrayList
		list.forEach [ el |
			val antipattern = el.ref as FAMIXAntipattern
			val roles = antipattern.roles as List<IntegerReference>
			roles.forEach [ r |
				val role = r.ref as FAMIXRole
				if (role.element.ref == s) {
					tmp += role.role
				}
			]

		]
		return tmp.removeBrackets
	}

	def private toString2(List<IntegerReference> list, HISMOClassVersion s) {
		val tmp = newArrayList
		list.forEach [ el |
			val antipattern = el.ref as FAMIXAntipattern
			val roles = antipattern.roles as List<IntegerReference>
			roles.forEach [ r |
				val role = r.ref as FAMIXRole
				if (role.element.ref == s) {
					tmp += role.role
				}
			]
		]
		return tmp.removeBrackets
	}
}
