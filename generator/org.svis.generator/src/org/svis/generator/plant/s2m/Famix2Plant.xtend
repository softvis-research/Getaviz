package org.svis.generator.plant.s2m

import java.util.List
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.famix.Document
import org.svis.xtext.famix.FAMIXAnnotationType
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXEnum
import org.svis.xtext.famix.FAMIXEnumValue
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXParameterizableClass
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.plant.impl.PlantFactoryImpl
import org.svis.xtext.plant.Entity
import org.svis.xtext.famix.impl.FAMIXAttributeImpl
import org.svis.xtext.famix.impl.FAMIXMethodImpl
import org.svis.generator.plant.WorkflowComponentWithPlantConfig
import org.svis.xtext.plant.Junction
import org.svis.generator.SettingsConfiguration
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot

class Famix2Plant extends WorkflowComponentWithModelSlot {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	val static plantFactory = new PlantFactoryImpl()
	var Document famixDocument
	var org.svis.generator.FamixUtils fu = new org.svis.generator.FamixUtils();
	var org.svis.xtext.plant.Document document

	// given stuff from FAMIX:
	var List<FAMIXNamespace> rootPackages = newArrayList
	var List<FAMIXNamespace> subPackages = newArrayList
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXMethod> methods = newArrayList
	val List<FAMIXAttribute> attributes = newArrayList
	val List<FAMIXEnumValue> enumValues = newArrayList
	var List<FAMIXInheritance> inheritances = newArrayList

	// TODO solve it with injection
	// @Inject extension XtendUtils util
//	extension FamixUtils util = new FamixUtils
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2Plant has started.")

//		famixDocument = (ctx.get("famix") as Root).document
		famixDocument = (ctx.get("famix") as org.svis.xtext.famix.Root).document
		rootPackages += famixDocument.elements.filter(FAMIXNamespace).filter[parentScope === null]
		subPackages += famixDocument.elements.filter(FAMIXNamespace).filter[parentScope !== null]
		structures += famixDocument.elements.filter(FAMIXStructure)
		methods += famixDocument.elements.filter(FAMIXMethod)
		attributes += famixDocument.elements.filter(FAMIXAttribute)
		enumValues += famixDocument.elements.filter(FAMIXEnumValue)
		inheritances += famixDocument.elements.filter(FAMIXInheritance)

		val root = plantFactory.createRoot;
		document = plantFactory.createDocument;
		root.document = document;

		// do the important stuff here...
		// for every element in the root:
		rootPackages.forEach[mapIt(1)];

		val rootList = newArrayList;
		rootList += root;

		// prepare rootList for the writer:
		ctx.set("plantwriter", rootList);

		// prapare the slot with the rootList (m2m):
		ctx.set("plant", root)

		log.info("Famix2Plant has finished.")
	}

	/**
	 * Map sub packages and recursive their content.
	 */
	def private Entity mapIt(FAMIXNamespace famixNamespace, int level) {

		val entity = plantFactory.createArea;
		entity.name = famixNamespace.name;
		entity.id = famixNamespace.id;
		entity.value = famixNamespace.value;
		entity.fqn = famixNamespace.fqn;
		entity.type = "FAMIX.Namespace";
		entity.level = level;
		entity.position = plantFactory.createPosition();
		entity.height = config.areaHeight;

		structures.filter[container.ref === famixNamespace].forEach[entity.entities += mapStructure(level + 1)]
		subPackages.filter[parentScope.ref === famixNamespace].forEach[entity.entities += mapIt(level + 1)];
		document.entity += entity;

		return entity;
	}

	/**
	 * Map class structure. 
	 */
	def private Entity mapStructure(FAMIXStructure el, int level) {

		val entity = plantFactory.createStem
		entity.name = el.name
		entity.id = el.id
		entity.value = el.value
		entity.fqn = el.fqn
		entity.type = el.typeAsString
		entity.level = level
		entity.loc = fu.loc(el as FAMIXClass)
		entity.position = plantFactory.createPosition();
		entity.dataCounter = 0
		entity.methodCounter = 0

		attributes.filter[parentType.ref === el].forEach[entity.dataCounter = entity.dataCounter + 1]
		methods.filter[parentType.ref === el].forEach[entity.methodCounter = entity.methodCounter + 1]

		// mapping attribute and methods to PETAL || POLLSTEM:		
		if (config.switchAttributeMethodMapping == "PETAL_POLLSTEM") {
			if (config.showAttributes) {
				attributes.filter[parentType.ref === el].forEach[entity.petals += toPetals(entity)]
			}
			if (config.showMethods) {
				methods.filter[parentType.ref === el].forEach[entity.pollstems += toPollStem]
			}
		} else {
			if (config.showAttributes) {
				attributes.filter[parentType.ref === el].forEach[entity.pollstems += toPollStem]
			}
			if (config.showMethods) {
				methods.filter[parentType.ref === el].forEach[entity.petals += toPetals(entity)]
			}
		}

		structures.filter[container.ref === el].forEach[entity.junctions += toJunction(level + 1)]
		enumValues.filter[parentEnum.ref === el].forEach[entity.petals += toPetals(entity)]

		return entity
	}

	/**
	 * Map an attribute to poll stem.
	 */
	def private toPollStem(FAMIXAttribute attribute) {
		var pollstem = plantFactory.createPollStem
		pollstem.name = attribute.name
		pollstem.id = attribute.id
		pollstem.value = attribute.value
		pollstem.fqn = attribute.fqn
		pollstem.type = "FAMIX.Attribute"
		pollstem.loc = fu.loc(attribute as FAMIXAttributeImpl)
		pollstem.position = plantFactory.createPosition();
		pollstem.ballPosition = plantFactory.createPosition();
		return pollstem
	}

	/**
	 * Map a method to petals.
	 */
	def private toPetals(FAMIXMethod method, Entity entity) {
		val petal = plantFactory.createPetal
		petal.name = method.name
		petal.id = method.id
		petal.value = method.value
		petal.fqn = method.fqn
		petal.type = "FAMIX.Method"
		petal.loc = fu.loc(method as FAMIXMethodImpl)
		petal.position = plantFactory.createPosition();
		return petal
	}

	/**
	 * Map an method to poll stem V2.
	 */
	def private toPollStem(FAMIXMethod m) {
		val pollstem = plantFactory.createPollStem
		pollstem.name = m.name
		pollstem.id = m.id
		pollstem.value = m.value
		pollstem.fqn = m.fqn
		pollstem.type = "FAMIX.Method"
		pollstem.loc = fu.loc(m as FAMIXMethodImpl)
		pollstem.position = plantFactory.createPosition();
		pollstem.ballPosition = plantFactory.createPosition();
		return pollstem
	}

	/**
	 * Map a attribute to petals V2.
	 */
	def private toPetals(FAMIXAttribute a, Entity entity) {
		val petal = plantFactory.createPetal
		petal.name = a.name
		petal.id = a.id
		petal.value = a.value
		petal.fqn = a.fqn
		petal.type = "FAMIX.Attribute"
		petal.loc = fu.loc(a as FAMIXAttributeImpl)
		petal.position = plantFactory.createPosition();
		return petal
	}

	/**
	 * Map a attribute to petals V2.
	 */
	def private toPetals(FAMIXEnumValue a, Entity entity) {
		val petal = plantFactory.createPetal
		petal.name = a.name
		petal.id = a.id
		petal.value = a.value
		petal.fqn = a.fqn
		petal.type = "FAMIX.Attribute"
		if (a instanceof FAMIXAttributeImpl) {
			petal.loc = fu.loc(a as FAMIXAttributeImpl)
		}

		petal.position = plantFactory.createPosition();
		return petal
	}

	/**
	 * Get the FAMIX structure type as String.
	 */
	def private getTypeAsString(FAMIXStructure el) {
		switch el {
			FAMIXClass: return "FAMIX.Class"
			FAMIXParameterizableClass: return "FAMIX.ParameterizableClass"
			FAMIXEnum: return "FAMIX.Enum"
			FAMIXAnnotationType: return "FAMIX.AnnotationType"
		}
	}

	/**
	 * Map a inner class.
	 */
	def private Junction toJunction(FAMIXStructure el, int level) {

		val junction = plantFactory.createJunction
		junction.name = el.name
		junction.id = el.id
		junction.value = el.value
		junction.fqn = el.fqn
		junction.level = level
		junction.type = el.typeAsString
		if (el instanceof FAMIXEnum) {
			junction.loc = fu.loc(el)
		} else {
			junction.loc = fu.loc(el as FAMIXClass)
		}

		junction.position = plantFactory.createPosition();
		junction.headPosition = plantFactory.createPosition();
		junction.dataCounter = 0
		junction.methodCounter = 0

		attributes.filter[parentType.ref === el].forEach[junction.dataCounter = junction.dataCounter + 1]
		methods.filter[parentType.ref === el].forEach[junction.methodCounter = junction.methodCounter + 1]

		if (config.switchAttributeMethodMapping == "PETAL_POLLSTEM") {
			if (config.showAttributes) {
				attributes.filter[parentType.ref === el].forEach[junction.petals += toPetals(junction)]
			}
			if (config.showMethods) {
				methods.filter[parentType.ref === el].forEach[junction.pollstems += toPollStem]
			}
		} else {
			if (config.showAttributes) {
				attributes.filter[parentType.ref === el].forEach[junction.pollstems += toPollStem]
			}
			if (config.showMethods) {
				methods.filter[parentType.ref === el].forEach[junction.petals += toPetals(junction)]
			}
		}

		structures.filter[container.ref === el].forEach[junction.junctions += Junction.cast(toJunction(level + 1))]
		enumValues.filter[parentEnum.ref === el].forEach[junction.petals += toPetals(junction)]

		return junction
	}
}
