package org.svis.extractor.hismo.m2t

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.svis.xtext.hismo.HISMONamespaceHistory
import java.util.ArrayList
import org.svis.generator.FamixUtils
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.xtext.hismo.HISMOMethodHistory
import org.svis.xtext.hismo.HISMOAttributeHistory
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMOMethodVersion
import org.svis.xtext.hismo.HISMOAttributeVersion
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXComponent
import org.svis.xtext.famix.FAMIXAntipattern
import org.svis.xtext.famix.FAMIXPath
import org.svis.xtext.famix.FAMIXRole
import org.svis.xtext.famix.FAMIXStructure

class ModellToHismoFile {
	extension FamixUtils util = new FamixUtils

	def toHismoBody(Resource resource) {
		val namespaceHistories =  EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMONamespaceHistory)
		val classHistories = EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMOClassHistory)
		val methodHistories = EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMOMethodHistory)
		val attributeHistories = EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMOAttributeHistory)
		val namespaceVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMONamespaceVersion)
		val classVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMOClassVersion)
		val methodVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMOMethodVersion)
		val attributeVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, HISMOAttributeVersion)
		val namespaces = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXNamespace)
		val methods = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXMethod)
		val classes =  EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXClass)
		val attributes =  EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXAttribute)
		val components = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXComponent)
		val antipattern = EcoreUtil2::getAllContentsOfType(resource.contents.head, FAMIXAntipattern)
		val actionsList = new ArrayList<String>
		
		antipattern.forEach [ a |
			a.path.forEach[p |
				val path = p.ref as FAMIXPath 
				actionsList += toPath(path).toString
			]
			a.roles.forEach[r |
				val role = r.ref as FAMIXRole
				actionsList += toRole(role).toString
			]
			actionsList += toAntipattern(a).toString
		]
		components.forEach[c|
			c.path.forEach[p |
				val path = p.ref as FAMIXPath 
				actionsList += toPath(path).toString
			]
			actionsList += toComponent(c).toString
		]
		namespaceHistories.forEach[nh|
			actionsList +=	toHismoNamespaceHistory(nh).toString
		]		
		classHistories.forEach[ch|
			actionsList += toHismoClassHistory(ch).toString
		]
		methodHistories.forEach[mh|
			actionsList += toHismoMethodHistory(mh).toString
		]
		attributeHistories.forEach[ah|
			actionsList += toHismoAttributeHistory(ah).toString
		]
		namespaceVersions.forEach[nv|
			actionsList += toHismoNamespaceVersion(nv).toString
		]
		namespaces.forEach[n|
			actionsList += toFamixNamespace(n).toString
		]
		classVersions.forEach[cv|
			actionsList += toHismoClassVersion(cv).toString
		]
		classes.forEach[c|
			actionsList += toFamixClass(c).toString
		]
		methodVersions.forEach[mv|
			actionsList += toHismoMethodVersion(mv).toString
		]
		methods.forEach[m|
			actionsList += toFamixMethod(m).toString
		]
		attributeVersions.forEach[av|
			actionsList += toHismoAttributeVersion(av).toString
		]
		attributes.forEach[a|
			actionsList += toFamixAttribute(a).toString
		]
		return actionsList.removeBrackets.replaceAll(", ","")
	}
	
	def private toRole(FAMIXRole role) '''
	(FAMIX.Role ( id: «role.name»)
		( role '«role.role»')
		( element (ref: «((role.element.ref) as FAMIXStructure).name»))
	)
	'''
	
	def private toPath(FAMIXPath path) '''
	(FAMIX.Path ( id: «path.name»)
		( hash "«path.id»" )
		( role '«path.role»')
		( start (ref: «path.start.ref.name»))
		( end (ref: «path.end.ref.name»))
		«IF (path.antipattern !== null)»
		( antipattern (ref: «path.antipattern.ref.name»))
		«ENDIF»
	)
	'''
	
	def private toComponent(FAMIXComponent component) '''
	(FAMIX.Component
		( id:  «component.name»)
		( hash "«component.id»" )
		( elements «FOR element: component.elements»(ref: «element.ref.name»)«ENDFOR»)
		( fqn '«component.fqn»' )
		( realcomponents «FOR element: component.realcomponents»(ref: «element.ref.name»)«ENDFOR»)
		( path «FOR element: component.path»(ref: «element.ref.name»)«ENDFOR»)
		( version '«component.version»' )
		( versions «FOR version:component.versions»'«version»' «ENDFOR» )
	)
	'''
	
	def private toAntipattern(FAMIXAntipattern antipattern) '''
	(FAMIX.Antipattern
		( id:  «antipattern.name»)
		( name '«antipattern.id»' )
		( fqn '«antipattern.fqn»' )
		( hash "«antipattern.id»" )
		( type '«antipattern.type»' )
		( elements «FOR element: antipattern.elements»(ref: «element.ref.name»)«ENDFOR»)
		( realcomponents «FOR element: antipattern.realcomponents»(ref: «element.ref.name»)«ENDFOR»)
		( path «FOR element: antipattern.path»(ref: «element.ref.name»)«ENDFOR»)
		( roles «FOR element: antipattern.roles»(ref: «element.ref.name»)«ENDFOR»)
		( version '«antipattern.version»' )
		( versions «FOR version:antipattern.versions»'«version»' «ENDFOR» )
	)
	'''
	
	def private toHismoNamespaceHistory(HISMONamespaceHistory nh) '''
	(HISMO.NamespaceHistory
		(id:«nh.name»)
		«IF nh.containingNamespaceHistory !== null»
		(containingNamespaceHistory (ref: «nh.containingNamespaceHistory.ref.name»))
		«ENDIF»
		«IF (nh.namespaceHistories.size > 0)»
		(namespaceHistories«FOR history : nh.namespaceHistories»(ref: «(history.ref as HISMONamespaceHistory).name»)«ENDFOR»)
		«ENDIF»
		«IF (nh.namespaceVersions.size > 0)»
		(namespaceVersions«FOR version : nh.namespaceVersions»(ref: «version.ref.name»)«ENDFOR»)
		«ENDIF»
		«IF (nh.classHistories.size > 0)»
		(classHistories«FOR ch : nh.classHistories»(ref: «(ch.ref as HISMOClassHistory).name»)«ENDFOR»)
	«ENDIF»
	)
	'''
	
	def private toHismoClassHistory(HISMOClassHistory ch) '''
	(HISMO.ClassHistory
		(id:«ch.name»)
		«IF (ch.classVersions.size > 0)»
		(classVersions«FOR version : ch.classVersions»(ref: «version.ref.name»)«ENDFOR»)
		«ENDIF»
		«IF (ch.containingNamespaceHistory !== null)»
		(containingNamespaceHistory (ref: «ch.containingNamespaceHistory.ref.name»))
		«ENDIF»
		«IF (ch.methodHistories.size > 0)»
		(methodHistories«FOR history : ch.methodHistories»(ref: «history.ref.name»)«ENDFOR»)
	«	ENDIF»
		«IF (ch.attributeHistories.size > 0)»
		(attributeHistories«FOR history : ch.attributeHistories»(ref: «history.ref.name»)«ENDFOR»)
	«ENDIF»
	)
	'''
	def toHismoMethodHistory(HISMOMethodHistory mh) '''
	(HISMO.MethodHistory
		(id:«mh.name»)
		(methodVersions	«FOR version: mh.methodVersions»(ref: «version.ref.name»)«ENDFOR»)
		(containingClassHistory (ref: «mh.containingClassHistory.ref.name»))
		(maxNumberOfStatements «mh.maxNumberOfStatements»)
		(minNumberOfStatements «mh.minNumberOfStatements»)
	)	
	'''
	def toHismoAttributeHistory(HISMOAttributeHistory ah)'''
	(HISMO.AttributeHistory
		(id:«ah.name»)
		(attributeVersions «FOR version: ah.attributeVersions»(ref: «version.ref.name»)«ENDFOR»)	
		(containingClassHistory (ref:«ah.containingClassHistory.ref.name»))
	)
	'''
	
	def private toHismoNamespaceVersion(HISMONamespaceVersion nv) '''
	(HISMO.NamespaceVersion
		(id:«nv.name»)
		(name '«nv.value»')
		(hash "«nv.id»")
		«IF nv.parentHistory !== null»	
		(parentHistory (ref: «nv.parentHistory.ref.name»))
		«ENDIF»
		«IF nv.versionEntity !== null»
		(versionEntity (ref: «nv.versionEntity.ref.name»))
		«ENDIF»
		(author '«nv.author»')
		(timestamp '«nv.timestamp»')
		(commitId '«nv.commitId»')	
	)
	'''
	
	def private toHismoClassVersion(HISMOClassVersion cv)'''
	(HISMO.ClassVersion
		(id: «cv.name»)
		(name '«cv.value»')
		(hash "«cv.id»")
		«IF cv.container.ref !== null»
		(container (ref: «cv.container.ref.name»))
		«ENDIF»
		«IF cv.parentHistory !== null»	
		(parentHistory (ref: «cv.parentHistory.ref.name»))
		«ENDIF»
		«IF cv.versionEntity !== null»
		(versionEntity (ref: «cv.versionEntity.ref.name»))
		«ENDIF»
		(author '«cv.author»')
		(timestamp '«cv.timestamp»')
		«IF cv.scc !== null»
		(scc (ref: «((cv.scc.ref) as FAMIXComponent).name»))
		«ENDIF»
		«IF cv.antipattern !== null»
			(antipattern «FOR pattern : cv.antipattern»(ref: «((pattern.ref as FAMIXAntipattern).name)»)«ENDFOR»)
		«ENDIF»
		(commitId '«cv.commitId»')
		«IF cv.betweennessCentrality !== null»
		(betweennessCentrality '«cv.betweennessCentrality»')
		«ENDIF»
		«IF cv.stkRank !== null»
		(STK-Rank '«cv.stkRank»')
		«ENDIF»
	)
	
	'''
	def private toHismoMethodVersion(HISMOMethodVersion mv)'''
	(HISMO.MethodVersion
		(id: «mv.name»)
		(name '«mv.value»')
		(hash "«mv.id»")
		«IF mv.parentHistory !== null»	
		(parentHistory (ref: «mv.parentHistory.ref.name»))
		«ENDIF»
		«IF mv.versionEntity !== null»
		(versionEntity (ref: «mv.versionEntity.ref.name»))
		«ENDIF»
		(author '«mv.author»')
		(timestamp '«mv.timestamp»')
		(commitId '«mv.commitId»')	
		(evolutionNumberOfStatements «mv.evolutionNumberOfStatements»)
	)
	'''
	def private toHismoAttributeVersion(HISMOAttributeVersion av)'''
	(HISMO.AttributeVersion
		(id: «av.name»)
		(name '«av.value»')
		(hash "«av.id»")
		«IF av.parentHistory !== null»	
		(parentHistory (ref: «av.parentHistory.ref.name»))
		«ENDIF»
		«IF av.versionEntity !== null»
		(versionEntity (ref: «av.versionEntity.ref.name»))
		«ENDIF»
		(author '«av.author»')
		(timestamp '«av.timestamp»')
		(commitId '«av.commitId»')	
	)
	'''
	def private toFamixNamespace(FAMIXNamespace n)'''
	(FAMIX.Namespace
		( id: «n.name»)
		( name '«n.value»')
	)
	'''
	
	def private toFamixMethod(FAMIXMethod m)'''
	(FAMIX.Method
		( id: «m.name» )
		( name '«m.value»')
	«««	( hasClassScope «m.hasClassScope»)
		( modifiers '«m.modifiers»')
		( parentType (ref: «m.parentType.ref.name»))
		( signature '«m.signature»')
	)
	'''
	
	def private toFamixClass(FAMIXClass c)'''
	(FAMIX.Class
		( id: «c.name»)
		( name '«c.value»')
		( container (ref: «c.container.ref.name»))
	)
	'''
	
	def private toFamixAttribute(FAMIXAttribute a) '''
	(FAMIX.Attribute
		( id: «a.name» )
		( name '«a.value»')
		( parentType (ref: «a.parentType.ref.name»))
	)
	'''
}

