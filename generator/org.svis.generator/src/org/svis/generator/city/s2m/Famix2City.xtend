package org.svis.generator.city.s2m

import java.util.List
import java.util.Set
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.generator.FamixUtils
import org.svis.generator.city.CityUtils
import org.svis.xtext.city.Building
import org.svis.xtext.city.BuildingSegment
import org.svis.xtext.city.District
import org.svis.xtext.city.Root
import org.svis.xtext.city.impl.CityFactoryImpl
import org.svis.xtext.famix.Document
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXEnum
import org.svis.xtext.famix.FAMIXEnumValue
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXLocalVariable
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXParameter
import org.svis.xtext.famix.FAMIXParameterizedType
import org.svis.xtext.famix.FAMIXPrimitiveType
import org.svis.xtext.famix.FAMIXReference
import org.svis.xtext.famix.FAMIXStructure
import org.svis.generator.SettingsConfiguration
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.logging.LogFactory
import org.svis.generator.SettingsConfiguration.BuildingType
import org.svis.generator.SettingsConfiguration.ClassElementsModes
import org.svis.generator.SettingsConfiguration.Original.BuildingMetric
import org.svis.generator.SettingsConfiguration.FamixParser
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex


class Famix2City extends WorkflowComponentWithModelSlot {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	val cityFactory = new CityFactoryImpl
	var Document famixDocument
	var org.svis.xtext.city.Document cityDocument

	val Set<FAMIXNamespace> rootPackages = newLinkedHashSet
	val Set<FAMIXNamespace> subPackages = newLinkedHashSet
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXClass> classes = newArrayList
	val List<FAMIXMethod> methods = newArrayList
	val List<FAMIXLocalVariable> localVariables = newArrayList
	val List<FAMIXAttribute> attributes = newArrayList
	val List<FAMIXEnum> enums = newArrayList
	val List<FAMIXEnumValue> enumValues = newArrayList
	val List<FAMIXPrimitiveType> primitiveTypes = newArrayList
	val List<FAMIXParameterizedType> parameterizedTypes = newArrayList
	var List<FAMIXInheritance> inheritances = newArrayList
	var List<FAMIXReference> references = newArrayList
	val List<FAMIXParameter> parameters = newArrayList
	
	extension FamixUtils util = new FamixUtils
	

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2City has started.")

		// receive FAMIX-model from source
		famixDocument = (ctx.get("famix") as org.svis.xtext.famix.Root).document

		// initialize target-document for new CITY-model
		cityDocument = cityFactory.createDocument
		var Root cityRoot = cityFactory.createRoot // cityRoot is root-element (which contains a document (which contains all elements of CITY)),
		cityRoot.document = cityDocument // that will be further processed by City2City, later on 
		val Set<FAMIXFileAnchor> fileAnchors = newHashSet
		fileAnchors += famixDocument.elements.filter(FAMIXFileAnchor).filterNull
		
		//Logic for ABAP code
		var Famix2City_abap f2c_abap = new Famix2City_abap(cityDocument, famixDocument)

		rootPackages += famixDocument.elements.filter(FAMIXNamespace).filter[parentScope === null]
		subPackages += famixDocument.elements.filter(FAMIXNamespace).filter[parentScope !== null]
		structures += famixDocument.elements.filter(FAMIXStructure)
//		classes += famixDocument.elements.filter(FAMIXClass)
//		pClasses += famixDocument.elements.filter(FAMIXParameterizableClass)
		methods += famixDocument.elements.filter(FAMIXMethod)
		localVariables += famixDocument.elements.filter(FAMIXLocalVariable)
		attributes += famixDocument.elements.filter(FAMIXAttribute)
		enums += famixDocument.elements.filter(FAMIXEnum)
		enumValues += famixDocument.elements.filter(FAMIXEnumValue)
		primitiveTypes += famixDocument.elements.filter(FAMIXPrimitiveType)
		parameterizedTypes += famixDocument.elements.filter(FAMIXParameterizedType)
		inheritances += famixDocument.elements.filter(FAMIXInheritance)
		references += famixDocument.elements.filter(FAMIXReference)
		parameters += famixDocument.elements.filter(FAMIXParameter)
		
		
		// For ABAP use custom logic
		if(config.parser == FamixParser::ABAP){
			cityDocument = f2c_abap.abapToModel()
		} else {
			rootPackages.forEach[toDistrict(1)]
		}
		

		// cityRoot is added to a list to be available for *.xml-output (mainly testing purpose)
		var cityList = newArrayList
		cityList += cityRoot
		ctx.set("CITYwriter", cityList)

		// cityRoot enters slot, to be available for City2City-transformation
		ctx.set("CITY", cityRoot)

		log.info("Famix2City has finished.")
	}


	/**
	 * Sets values for current namespace and searches for nested elements
	 * 
	 * @param elem Source package for the district
	 * @param level Hierarchy level of the district
	 * @return new District
	 */
	def private District toDistrict(FAMIXNamespace elem, int level) {	
		val newDistrict = cityFactory.createDistrict
		newDistrict.name = elem.name
		newDistrict.value = elem.value
		newDistrict.fqn = elem.fqn
		newDistrict.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newDistrict.level = level
		newDistrict.id = elem.id
		if(elem.iteration >= 1){
			newDistrict.notInOrigin = "true"
		}

		if (config.buildingType == BuildingType::CITY_DYNAMIC) {
			structures.filter[container.ref === elem].forEach[newDistrict.entities += toDistrict(level + 1)]
			subPackages.filter[parentScope.ref === elem].forEach[newDistrict.entities += toDistrict(level + 1)]
		} else {
			structures.filter[container.ref === elem].forEach[newDistrict.entities += toBuilding(level + 1)]
			subPackages.filter[parentScope.ref === elem].forEach[newDistrict.entities += toDistrict(level + 1)]
		}
		
		cityDocument.entities += newDistrict
		return newDistrict
	}

	
	def private District toDistrict(FAMIXStructure elem, int level) {
		val newDistrict = cityFactory.createDistrict
		newDistrict.name = elem.name
		newDistrict.value = elem.value
		newDistrict.fqn = elem.fqn
		newDistrict.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newDistrict.level = level
		newDistrict.id = elem.id


		val currentMethods = methods.filter[parentType.ref === elem]
		val currentAttributes = attributes.filter[parentType.ref === elem]

		newDistrict.dataCounter = currentAttributes.length
		newDistrict.methodCounter = currentMethods.length

		if (config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
			config.classElementsMode === ClassElementsModes::METHODS_ONLY) {
			currentMethods.forEach[m|newDistrict.entities += toBuilding(m, newDistrict, level + 1)]
		}
		return newDistrict
	}

	/**
	 * Sets values for current class and searches for nested elements
	 * 
	 * @param elem Source class for the building
	 * @param level Hierarchy level of the building
	 * @return new Building
	 */
	def private Building toBuilding(FAMIXStructure elem, int level) {
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		
		newBuilding.level = level
		newBuilding.id = elem.id
//		newBuilding.dataCounter = 0
//		newBuilding.methodCounter = 0
		inheritances.filter[subclass.ref === elem].forEach [ i |
			val inheritance = cityFactory.createReference
			inheritance.type = "Inheritance"
			inheritance.name = i.superclass.ref.name
			inheritance.fqn = i.superclass.ref.fqn
			newBuilding.references += inheritance
		]

//		Currently no further class specifics, such as inner classes, handled
//		structures.filter[container.ref.equals(elem)].
//			forEach[newBuilding.entities.add(toBuilding(level + 1))]
		val currentMethods = methods.filter[parentType.ref === elem]
		val currentAttributes = attributes.filter[parentType.ref === elem]

		newBuilding.dataCounter = currentAttributes.length
		newBuilding.methodCounter = currentMethods.length

		if (config.buildingType == BuildingType::CITY_FLOOR) {
			methods.filter[parentType.ref.equals(elem)].forEach[newBuilding.methods.add(toFloor)]
			attributes.filter[parentType.ref.equals(elem)].forEach[newBuilding.data.add(toChimney)]
		} else {
			if (config.originalBuildingMetric == BuildingMetric::NOS) {
				newBuilding.numberOfStatements = currentMethods.fold(0)[sum, method|sum + method.numberOfStatements]
			}

			if (config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
				config.classElementsMode === ClassElementsModes::METHODS_ONLY) {
				currentMethods.forEach[newBuilding.methods += toBuildingSegment_Method(newBuilding, level + 1)]
			}

			// methods.filter[parentType.ref.equals(elem)].
			// forEach[newBuilding.methods.add(toBuildingSegment_Method(newBuilding, level + 1))]
			if (config.classElementsMode === ClassElementsModes::METHODS_AND_ATTRIBUTES ||
				config.classElementsMode === ClassElementsModes::ATTRIBUTES_ONLY) {
				currentAttributes.forEach[newBuilding.data += toBuildingSegment_Attribute(newBuilding, level + 1)]
			}
		// attributes.filter[parentType.ref.equals(elem)].
		// forEach[newBuilding.data.add(toBuildingSegment_Attribute(newBuilding, level + 1))]
		}
		return newBuilding
	}

	def private Building toBuilding(FAMIXMethod elem, District parent, int level) {
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.signature = elem.signature
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}

		return newBuilding
	}
	
	/**
	 * Sets values for current method of the {@code parent} class.
	 * 
	 * @param elem Source method for the buildingSegment
	 * @param level Hierarchy level of the underlying district/package
	 * @return new BuildingSegment
	 * @see toBuildingSegment_Attribute(FAMIXAttribute, Building, int)
	 */
	def private BuildingSegment toBuildingSegment_Method(FAMIXMethod elem, Building parent, int level) {
		val newBuildingSegment = cityFactory.createBuildingSegment
		newBuildingSegment.name = elem.name
		newBuildingSegment.value = elem.value
		newBuildingSegment.fqn = elem.fqn
		newBuildingSegment.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuildingSegment.level = level
		newBuildingSegment.id = elem.id
		newBuildingSegment.signature = elem.signature
		newBuildingSegment.modifiers = elem.modifiers.toString
		newBuildingSegment.numberOfStatements = elem.numberOfStatements
		newBuildingSegment.parent = parent
		newBuildingSegment.methodKind = elem.kind
		newBuildingSegment.declaredType = CityUtils.fillDeclaredType(cityFactory.createDeclaredType, elem.declaredType)

		newBuildingSegment.localVariableCounter = localVariables.filter[parentBehaviouralEntity.ref === elem].length

		newBuildingSegment.parameterCounter = parameters.filter[parentBehaviouralEntity.ref === elem].length

		return newBuildingSegment
	}

	/**
	 * Sets values for current attribute of the {@code parent} class.
	 * 
	 * @param elem Source method for the buildingSegment
	 * @param level Hierarchy level of the underlying district/package
	 * @return new BuildingSegment
	 * @see toBuildingSegment_Method(FAMIXMethod, Building, int)
	 */
	def private BuildingSegment toBuildingSegment_Attribute(FAMIXAttribute elem, Building parent, int level) {
		val newBuildingSegment = cityFactory.createBuildingSegment
		newBuildingSegment.name = elem.name
		newBuildingSegment.value = elem.value
		newBuildingSegment.fqn = elem.fqn
		newBuildingSegment.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuildingSegment.level = level
		newBuildingSegment.id = elem.id
		newBuildingSegment.modifiers = elem.modifiers.toString
		newBuildingSegment.parent = parent
		newBuildingSegment.declaredType = CityUtils.fillDeclaredType(cityFactory.createDeclaredType, elem.declaredType)

		return newBuildingSegment
	}

	// pko 2016
	def BuildingSegment create newBuildingSegment: cityFactory.createBuildingSegment toFloor(FAMIXMethod famixMethod) {
		newBuildingSegment.name = famixMethod.name
		newBuildingSegment.value = famixMethod.value
		newBuildingSegment.fqn = famixMethod.fqn
		newBuildingSegment.id = famixMethod.id
	}

	// pko 2016
	def BuildingSegment create newBuildingSegment: cityFactory.createBuildingSegment toChimney(
		FAMIXAttribute famixAttribute) {
		newBuildingSegment.name = famixAttribute.name
		newBuildingSegment.value = famixAttribute.value
		newBuildingSegment.fqn = famixAttribute.fqn
		newBuildingSegment.id = famixAttribute.id
	}
	
}
