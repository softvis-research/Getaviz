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
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXEnum
import org.svis.xtext.famix.FAMIXEnumValue
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXLocalVariable
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
import org.svis.generator.SettingsConfiguration.AbapCityRepresentation
import org.svis.xtext.city.Entity
import org.eclipse.xtext.EcoreUtil2

//ABAP
import org.svis.xtext.famix.FAMIXReport
import org.svis.xtext.famix.FAMIXDataElement
import org.svis.xtext.famix.FAMIXTable
import org.svis.xtext.famix.FAMIXMessageClass
import org.svis.xtext.famix.FAMIXFunctionGroup
import org.svis.xtext.famix.FAMIXFunctionModule
import org.svis.xtext.famix.FAMIXStrucElement
import org.svis.xtext.famix.FAMIXABAPStruc
import org.svis.xtext.famix.FAMIXFormroutine



class Famix2City_abap {
	//Import: cityDocument, to store all elements from famixDocument
	new(org.svis.xtext.city.Document imp_cityDocument, Document imp_famixDocument){
		cityDocument  = imp_cityDocument
		famixDocument = imp_famixDocument
		
		rootPackages 		+= famixDocument.elements.filter(FAMIXNamespace).filter[parentScope === null]
		subPackages 		+= famixDocument.elements.filter(FAMIXNamespace).filter[parentScope !== null]
		structures 			+= famixDocument.elements.filter(FAMIXStructure)
 		classes 			+= famixDocument.elements.filter(FAMIXClass)
//		pClasses += famixDocument.elements.filter(FAMIXParameterizableClass)
		methods 			+= famixDocument.elements.filter(FAMIXMethod)
		localVariables 		+= famixDocument.elements.filter(FAMIXLocalVariable)
		attributes 			+= famixDocument.elements.filter(FAMIXAttribute)
		enums 			    += famixDocument.elements.filter(FAMIXEnum)
		enumValues			+= famixDocument.elements.filter(FAMIXEnumValue)
		primitiveTypes 		+= famixDocument.elements.filter(FAMIXPrimitiveType)
		parameterizedTypes  += famixDocument.elements.filter(FAMIXParameterizedType)
		inheritances 		+= famixDocument.elements.filter(FAMIXInheritance)
		references 			+= famixDocument.elements.filter(FAMIXReference)
		parameters 			+= famixDocument.elements.filter(FAMIXParameter)
		
		//ABAP
		reports 	 	 += famixDocument.elements.filter(FAMIXReport) 
		tables 		     += famixDocument.elements.filter(FAMIXTable)
		functionGroups   += famixDocument.elements.filter(FAMIXFunctionGroup)
		functionModules  += famixDocument.elements.filter(FAMIXFunctionModule)
		dataElements	 += famixDocument.elements.filter(FAMIXDataElement)
		messageClasses   += famixDocument.elements.filter(FAMIXMessageClass)
		strucs 			 += famixDocument.elements.filter(FAMIXABAPStruc)
		strucElements 	 += famixDocument.elements.filter(FAMIXStrucElement)
		formRoutines 	 += famixDocument.elements.filter(FAMIXFormroutine)
	}
		
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	val cityFactory = new CityFactoryImpl
	var Document famixDocument
	var org.svis.xtext.city.Document cityDocument
	
	extension FamixUtils util = new FamixUtils

	val Set<FAMIXNamespace> rootPackages = newLinkedHashSet
	val Set<FAMIXNamespace> subPackages = newLinkedHashSet
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXClass> classes = newArrayList
//	val List<FAMIXParameterizableClass> pClasses = newArrayList
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
	
	var org.svis.xtext.city.District originDistrict = cityFactory.createDistrict
	var List<Entity> entityList = newArrayList
	
	//ABAP
	val List<FAMIXReport> reports = newArrayList
	val List<FAMIXTable> tables =newArrayList
	val List<FAMIXDataElement> dataElements = newArrayList
	val List<FAMIXMessageClass> messageClasses = newArrayList
	val List<FAMIXFunctionGroup> functionGroups = newArrayList
	val List<FAMIXFunctionModule> functionModules = newArrayList
	val List<FAMIXABAPStruc> strucs = newArrayList
	val List<FAMIXStrucElement> strucElements = newArrayList
	val List<FAMIXFormroutine> formRoutines = newArrayList
	
	
	def abapToModel(){
		
		// Run transformation 
		if(config.abap_representation == AbapCityRepresentation::SOURCE){
			sourceModel()
		}else if(config.abap_representation == AbapCityRepresentation::CODE_LOGIC){
			//TODO
		}
		
		
		return cityDocument
	}
	
	
	def void logElement(Object el){
		log.info(el)
	}
	
	
	/**
	 * Transform famix to City, represent as SOURCE
	 */
	 def sourceModel(){
	 	//collection of all originPackages
		originDistrict.name = 'originPackages'
		originDistrict.level = 1
		originDistrict.id = '0'
		originDistrict.type = 'FAMIX.Namespace'
		originDistrict.notInOrigin = 'false'
		originDistrict.hasDatabases = 'false'
	
		rootPackages.forEach[toDistrict(2)]
	 	cityDocument.entities.add(originDistrict)
	 	
	 	//entityList is used to attach references to the respecting entities
		//entityList = EcoreUtil2.getAllContentsOfType(cityDocument, Entity)
		//references.forEach[toABAPReference()]
	 }
	
	
	/**
	 * Sets values for current package and searches for nested elements
	 * 
	 * @param elem Source package for the district
	 * @param level Hierarchy level of the district
	 * @return new District
	 */
	def District toDistrict(FAMIXNamespace elem, int level) {
		
		/*
		 *	val newDistrict = cityFactory.createDistrict
			newDistrict.name = elem.name
			newDistrict.value = elem.value
			newDistrict.fqn = elem.fqn
			newDistrict.type = CityUtils.getFamixClassString(elem.class.simpleName)
			newDistrict.level = level
			newDistrict.id = elem.id
		* 
		*/
		
		val newDistrict = cityFactory.createDistrict
		newDistrict.name = elem.name
		newDistrict.value = elem.value
		newDistrict.level = level
		newDistrict.fqn = elem.fqn
		newDistrict.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newDistrict.id = elem.name.toString
		if(elem.iteration >= 1){
			newDistrict.notInOrigin = 'true'	
		}else{
			newDistrict.notInOrigin = "false";
		}
		
		val classLibrary = cityFactory.createDistrict
		classLibrary.name = newDistrict.name + "_classLibrary"
		classLibrary.level = level
		classLibrary.type = "classLibrary"
		classLibrary.id = elem.name + "00001"
		
		//val currentAttributes = attributes.filter[parentType.ref === elem]
		//classes.filter[container.ref.equals(elem)].
		classes.filter[container.ref == elem].
			forEach[classLibrary.entities.add(toBuilding_Struct(newDistrict, level + 1))]
		messageClasses.filter[container.ref == elem].
			forEach[classLibrary.entities.add(toBuilding_Struct(newDistrict, level + 1))]
			
		val functionGroupLibrary = cityFactory.createDistrict
		functionGroupLibrary.name = newDistrict.name + "_functionGroupLibrary"
		functionGroupLibrary.level = level
		functionGroupLibrary.type = "functionGroupLibrary"
		functionGroupLibrary.id = elem.name + "00002"
		
		functionGroups.filter[container.ref == elem].
			forEach[functionGroupLibrary.entities.add(toBuilding_Struct(newDistrict, level + 1))]
		
		val reportLibrary = cityFactory.createDistrict
		reportLibrary.name = newDistrict.name + "_reportLibrary"
		reportLibrary.level = level
		reportLibrary.type = "reportLibrary"
		reportLibrary.id = elem.name + "00003"
		
		reports.filter[container.ref == elem].
			forEach[reportLibrary.entities.add(toBuilding_Report(newDistrict, level + 1))]
			
		val tableLibrary = cityFactory.createDistrict
		tableLibrary.name = newDistrict.name + "_tableLibrary"
		tableLibrary.level = level
		tableLibrary.type = "tableLibrary"
		tableLibrary.id = elem.name + "00004"
		
		tables.filter[container.ref == elem].
			forEach[tableLibrary.entities.add(toBuilding_Table(newDistrict, level + 1))]
			
		val dictionary = cityFactory.createDistrict
		dictionary.name = newDistrict.name + "_dictionary"
		dictionary.level = level
		dictionary.type = "dictionaryLibrary"
		dictionary.id = elem.name + "00005"
		
		dataElements.filter[container.ref == elem].
			forEach[dictionary.entities.add(toBuilding_dataElement(newDistrict, level + 1))]
			
		strucs.filter[container.ref == elem].
			forEach[dictionary.entities.add(toBuilding_strucs(newDistrict, level + 1))]
		
		if(classLibrary.entities.size != 0 || config.showEmptyDistricts()){
			newDistrict.entities.add(classLibrary)
		}
		
		if(reportLibrary.entities.size != 0 || config.showEmptyDistricts()){
			newDistrict.entities.add(reportLibrary)
		}
		
		if(functionGroupLibrary.entities.size != 0 || config.showEmptyDistricts()){
			newDistrict.entities.add(functionGroupLibrary)
		}
		
		if(dictionary.entities.size != 0 || config.showEmptyDistricts()){
			newDistrict.entities.add(dictionary)
		}
		
		if(tableLibrary.entities.size == 0){
			newDistrict.hasDatabases = 'false'
		}
		newDistrict.entities.add(tableLibrary)
				
	//	subPackages.filter[container.ref.equals(elem)].
	//		forEach[newDistrict.entities.add(toDistrict(level + 1))]
		
		if(newDistrict.notInOrigin.equals('true')){
			cityDocument.entities.add(newDistrict)	
		}else{
			originDistrict.entities.add(newDistrict)
		}

		return newDistrict
	}
	
	/**
	 * Sets values for current class or function group and searches for nested elements
	 * 
	 * @param elem Source class for the building
	 * @param level Hierarchy level of the building
	 * @return new Building
	 */
	def Building toBuilding_Struct(FAMIXStructure elem, District parent, int level) {
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
	
		if (newBuilding.type.equals('FAMIX.Class')){
			newBuilding.dataCounter = attributes.filter[parentType.ref == elem].length
			newBuilding.methodCounter = methods.filter[parentType.ref == elem].length
			
			attributes.filter[parentType.ref == elem].
				forEach[newBuilding.data.add(toBuildingSegment_Attribute(newBuilding, level + 1))]
			methods.filter[parentType.ref == elem]
				.forEach[newBuilding.methods.add(toBuildingSegment_Method(newBuilding, level + 1))]
							
		}else if (newBuilding.type.equals('FAMIX.FunctionGroup')){
			newBuilding.methodCounter = functionModules.filter[parentType.ref.equals(elem)].length
			
			functionModules.filter[parentType.ref.equals(elem)].
				forEach[newBuilding.methods.add(toBuildingSegment_functionModule(newBuilding, level + 1))]
		}
 
		return newBuilding
	}

	/**
	 * Sets values for current message class
	 * 
	 * @param elem Source message class for the building
	 * @param level Hierarchy level of the building
	 * @return new Building
	 */
	def Building toBuilding_messageClass(FAMIXMessageClass elem, District parent, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		newBuilding.dataCounter = elem.numberOfMessages
		newBuilding.parent = parent
		
		return newBuilding
	}
	
	/**
	 * Sets values for current report and searches for nested elements
	 * 
	 * @param elem Source report for the building
	 * @param level Hierarchy level of the building
	 * @return new Building
	 */
	def Building toBuilding_Report(FAMIXReport elem, District parent, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		newBuilding.methodCounter = formRoutines.filter[parentType.ref.equals(elem)].length
		newBuilding.parent = parent
		
		formRoutines.filter[parentType.ref.equals(elem)].
			forEach[newBuilding.methods.add(toBuildingSegment_formRoutine(newBuilding, level + 1))]
		
		return newBuilding
	}
	
	/**
	 * Sets values for current table
	 * 
	 * @param elem Source class for the building
	 * @param level Hierarchy level of the building
	 * @return new Building
	 */
	def Building toBuilding_Table(FAMIXTable elem, District parent, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		newBuilding.dataCounter = elem.numberOfColumns
		newBuilding.parent = parent
		
		return newBuilding
	}

/**
	 * Sets values for current data element
	 * 
	 * @param elem Source class for the building
	 * @param level Hierarchy level of the building
	 * @return new Building
	 */
	def Building toBuilding_dataElement(FAMIXDataElement elem, District parent, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		newBuilding.parent = parent

		return newBuilding
	}

	/**
	 * Sets values for current abap structures and searches for nested elements
	 * 
	 * @param elem Source class for the building
	 * @param level Hierarchy level of the building
	 * @return new Building
	 */
	def Building toBuilding_strucs(FAMIXABAPStruc elem, District parent, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		newBuilding.parent = parent
		
		strucElements.filter[container.ref.equals(elem)].
			forEach[newBuilding.entities.add(toBuildingSegment_strucElement(newBuilding, level + 1))]
	
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
	def BuildingSegment toBuildingSegment_Method(FAMIXMethod elem, Building parent, int level) {
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
	
	/**
	 * Sets values for current function module of the {@code parent} function group.
	 * 
	 * @param elem Source function module for the buildingSegment
	 * @param level Hierarchy level of the underlying district/package
	 * @return new BuildingSegment
	 */
	def BuildingSegment toBuildingSegment_functionModule(FAMIXFunctionModule elem, Building parent, int level){
		val newBuildingSegment = cityFactory.createBuildingSegment
		newBuildingSegment.name = elem.name
		newBuildingSegment.value = elem.value
		newBuildingSegment.fqn = elem.fqn
		newBuildingSegment.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuildingSegment.level = level
		newBuildingSegment.id = elem.id
		newBuildingSegment.parent = parent
		newBuildingSegment.declaredType = CityUtils.fillDeclaredType(cityFactory.createDeclaredType, elem.declaredType)

		return newBuildingSegment
	}
	
	/**
	 * Sets values for current formroutine of the {@code parent} report.
	 * 
	 * @param elem Source formroutine for the buildingSegment
	 * @param level Hierarchy level of the underlying district/package
	 * @return new BuildingSegment
	 */
	def BuildingSegment toBuildingSegment_formRoutine(FAMIXFormroutine elem, Building parent, int level){
		val newBuildingSegment = cityFactory.createBuildingSegment
		newBuildingSegment.name = elem.name
		newBuildingSegment.value = elem.value
		newBuildingSegment.fqn = elem.fqn
		newBuildingSegment.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuildingSegment.level = level
		newBuildingSegment.id = elem.id
		newBuildingSegment.parent = parent
		newBuildingSegment.declaredType = CityUtils.fillDeclaredType(cityFactory.createDeclaredType, elem.declaredType)
		

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
	def BuildingSegment toBuildingSegment_Attribute(FAMIXAttribute elem, Building parent, int level) {
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
	
	/**
	 * Sets values for current strucElement of the {@code parent} structure.
	 * 
	 * @param elem Source method for the buildingSegment
	 * @param level Hierarchy level of the underlying district/package
	 * @return new BuildingSegment
	 */
	def BuildingSegment toBuildingSegment_strucElement(FAMIXStrucElement elem, Building parent, int level) {
		val newBuildingSegment = cityFactory.createBuildingSegment
		newBuildingSegment.name = elem.name
		newBuildingSegment.value = elem.value
		newBuildingSegment.fqn = elem.fqn
		newBuildingSegment.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuildingSegment.level = level
		newBuildingSegment.id = elem.id
		newBuildingSegment.parent = parent
		newBuildingSegment.declaredType = CityUtils.fillDeclaredType(cityFactory.createDeclaredType, elem.declaredType)
		
		return newBuildingSegment
	}
	
	/**
	 * Adds reference to {@code sourceEntity} entity that targets another entity.
	 * 
	 * @param elem Source reference
	 */
	def void toABAPReference(FAMIXReference elem){
		/*val newABAPReference = cityFactory.createABAPReference
		newABAPReference.name = elem.name
		if(entityList.filter[id.equals(elem.target.ref.name)].size != 0){
			newABAPReference.target = entityList.filter[id.equals(elem.target.ref.name)].get(0)
		}
		
		var sourceEntity = cityFactory.createEntity
		if(entityList.filter[id.equals(elem.source.ref.name)].size != 0){
			sourceEntity = entityList.filter[id.equals(elem.source.ref.name)].get(0)	
		}
		
		newABAPReference.parent = sourceEntity
		
		if(newABAPReference.parent != null && newABAPReference.target != null){
			sourceEntity.abapReferences.add(newABAPReference)	
		}*/
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