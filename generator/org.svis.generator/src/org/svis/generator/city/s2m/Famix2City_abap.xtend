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
import org.svis.generator.SettingsConfiguration.AbapCityRepresentation

// ABAP
import org.svis.xtext.famix.FAMIXReport 
import org.svis.xtext.famix.FAMIXDictionaryData
import org.svis.xtext.famix.FAMIXDataElement
import org.svis.xtext.famix.FAMIXDomain
import org.svis.xtext.famix.FAMIXTable
import org.svis.xtext.famix.FAMIXABAPStruc
import org.svis.xtext.famix.FAMIXStrucElement
import org.svis.xtext.famix.FAMIXFunctionGroup
import org.svis.xtext.famix.FAMIXFunctionModule
import org.svis.xtext.famix.FAMIXFormroutine
import org.svis.xtext.famix.FAMIXMessageClass 
import org.svis.xtext.famix.FAMIXTableType 
import org.svis.xtext.famix.FAMIXTableTypeElement 
import org.svis.xtext.famix.FAMIXTypeOf
import org.svis.xtext.famix.FAMIXTableElement



class Famix2City_abap {
	
   val String[] typeNames = #["CHAR","INT4","NUMC","ACCP","CLNT","CUKY","CURR","DF16_DEC","DF16_RAW","DF34_DEC","DF34_RAW",
	                          "DATS","DEC","FLTP","INT1","INT2","INT8","LANG","LCHR","LRAW","QUAN","RAW","RAWSTRING", "SSTRING",
	                          "STRING","TIMS","UNIT"]
	                           
	 
	
	//Import: cityDocument, to store all elements from famixDocument
	new(org.svis.xtext.city.Document imp_cityDocument, Document imp_famixDocument){
		cityDocument  = imp_cityDocument
		famixDocument = imp_famixDocument
		
		rootPackages 		+= famixDocument.elements.filter(FAMIXNamespace).filter[parentScope === null]
		subPackages 		+= famixDocument.elements.filter(FAMIXNamespace).filter[parentScope !== null]
		structures 			+= famixDocument.elements.filter(FAMIXStructure)
 		classes 			+= famixDocument.elements.filter(FAMIXClass)
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
		formroutines	 += famixDocument.elements.filter(FAMIXFormroutine)
		dataElements	 += famixDocument.elements.filter(FAMIXDataElement)
		domains			 += famixDocument.elements.filter(FAMIXDomain)
		tables			 += famixDocument.elements.filter(FAMIXTable)
		abapStrucs		 += famixDocument.elements.filter(FAMIXABAPStruc)
		abapStrucElem    += famixDocument.elements.filter(FAMIXStrucElement)
		functionGroups   += famixDocument.elements.filter(FAMIXFunctionGroup)
		functionModules  += famixDocument.elements.filter(FAMIXFunctionModule)
		messageClasses   += famixDocument.elements.filter(FAMIXMessageClass)
		tableTypes		 += famixDocument.elements.filter(FAMIXTableType)
		ttyElements	 	 += famixDocument.elements.filter(FAMIXTableTypeElement)
		typeOfs			 += famixDocument.elements.filter(FAMIXTypeOf)
		tableElements	 += famixDocument.elements.filter(FAMIXTableElement)
		
		
		
		
		//dcData	 		 += dataElements + domains + abapStrucs + tableTypes 
		//dcData	 		 += abapStrucs + tableTypes 
		dcData	 		     += abapStrucs + tableTypes + tables
		
		if(!config.showOwnTablesDistrict){
			dcData += tables
		}
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
	
	//ABAP
	val List<FAMIXReport> reports = newArrayList 
	val List<FAMIXDictionaryData> dcData = newArrayList
	val List<FAMIXDataElement> dataElements = newArrayList 
	val List<FAMIXDomain> domains = newArrayList
	val List<FAMIXTable> tables = newArrayList 
	val List<FAMIXABAPStruc> abapStrucs = newArrayList 
	val List<FAMIXStrucElement> abapStrucElem = newArrayList 
	val List<FAMIXFunctionModule> functionModules = newArrayList
	val List<FAMIXFormroutine> formroutines = newArrayList
	val List<FAMIXMessageClass> messageClasses = newArrayList
	val List<FAMIXFunctionGroup> functionGroups = newArrayList
	val List<FAMIXTableType> tableTypes = newArrayList
	val List<FAMIXTypeOf> typeOfs = newArrayList
	val List<FAMIXTableElement> tableElements = newArrayList
	val List<FAMIXTableTypeElement> ttyElements = newArrayList
	
	//val String[] typeNames = #["CHAR","INT4","NUMC","ACCP","CLNT","CUKY","CURR","DF16_DEC","DF16_RAW","DF34_DEC","DF34_RAW",
	//                           "DATS","DEC","FLTP","INT1","INT2","INT8","LANG","LCHR","LRAW","QUAN","RAW","RAWSTRING", "SSTRING",
	//                           "STRING","TIMS","UNIT"]
	
	def abapToModel(){		
		// Run transformation 
		if (config.abap_representation == AbapCityRepresentation::SIMPLE) {
			simpleModel()
		} else if (config.abap_representation == AbapCityRepresentation::ADVANCED) {
			advancedModel()
		}		
		
		return cityDocument
	}
	
	
	def void logElement(Object el){
		log.info(el)
	}
	
	
	/**
	 * Transform famix to City, SIMPLE representation
	 * 1. Group objects by type and art
	 * 2. Create district for groups
	 * 3. Use simple forms (city2X3D)
	 */
	def simpleModel(){
		rootPackages.forEach[toDistrict(1)]
	}
	
	
	/**
	 * Transform famix to City, ADVANCED representation
	 * 1. Build city using dependencies
	 * 2. Use more complex buildings (city2X3D)
	 */
	def advancedModel(){
		rootPackages.forEach[toAdvancedDistrict(1)]
	}
	
	
	// Mode: simple
	def private District toDistrict(FAMIXNamespace elem, int level) {	
		
		// Create main district		
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
		
		// Test mode -> Delete for dist		
		if(!config.abapCityTestMode){
			//Sub packages
			subPackages.filter[parentScope.ref === elem].forEach[newDistrict.entities += toDistrict(level + 1)]
			
			//Data Dictionary
			if (dcData.filter[container.ref == elem].length != 0){
				val dcDataDistrict = cityFactory.createDistrict
				dcDataDistrict.name = newDistrict.name + "_dcDataDistrict"
				dcDataDistrict.type = "dcDataDistrict"
				dcDataDistrict.id = elem.id + "_00002"
				dcDataDistrict.level = level + 1
				if(elem.iteration >= 1){
					dcDataDistrict.notInOrigin = "true"
				}
				
				dcData.filter[container.ref == elem].forEach[dcDataDistrict.entities += toBuilding(level + 2)]
				newDistrict.entities.add(dcDataDistrict)
			}
			
			//Reports (+forms)
			if (reports.filter[container.ref == elem].length != 0){
				val reportDistrict = cityFactory.createDistrict
				reportDistrict.name = newDistrict.name + "_reportDistrict"
				reportDistrict.type = "reportDistrict"
				reportDistrict.id = elem.id + "_00003"
				reportDistrict.level = level + 1
				if(elem.iteration >= 1){
					reportDistrict.notInOrigin = "true"
				}
				
				reports.filter[container.ref == elem].forEach[reportDistrict.entities += toBuilding(level + 2)]
				newDistrict.entities.add(reportDistrict)
			}
			
			//Classes (+included methods and attributes)
			if (classes.filter[container.ref == elem].length != 0){
				val classDistrict = cityFactory.createDistrict
				classDistrict.name = newDistrict.name + "_classDistrict"
				classDistrict.type = "classDistrict"
				classDistrict.level = level + 1
				classDistrict.id = elem.id + "_00004"
				if(elem.iteration >= 1){
					classDistrict.notInOrigin = "true"
				}
				
				classes.filter[container.ref === elem].forEach[classDistrict.entities += toBuilding(level + 2)]
				newDistrict.entities.add(classDistrict)
			}
			
			//Function Group (+function Modules)
			if (functionGroups.filter[container.ref == elem].length != 0){
				val functionGroupDistrict = cityFactory.createDistrict
				functionGroupDistrict.name = newDistrict.name + "_functionGroupDistrict"
				functionGroupDistrict.type = "functionGroupDistrict"
				functionGroupDistrict.level = level + 1
				functionGroupDistrict.id = elem.id + "_00005"
				if(elem.iteration >= 1){
					functionGroupDistrict.notInOrigin = "true"
				}
				
				functionGroups.filter[container.ref === elem].forEach[functionGroupDistrict.entities += toBuilding(level + 2)]
				newDistrict.entities.add(functionGroupDistrict)
			}
			
			//DB Tables - only if we need own district
			if(config.showOwnTablesDistrict){
				if (tables.filter[container.ref == elem].length != 0){
					val tableDistrict = cityFactory.createDistrict
					tableDistrict.name = newDistrict.name + "_tableDistrict"
					tableDistrict.type = "tableDistrict"
					tableDistrict.level = level + 1
					tableDistrict.id = elem.id + "_00006"
					if(elem.iteration >= 1){
						tableDistrict.notInOrigin = "true"
					}
					
					tables.filter[container.ref == elem].forEach[tableDistrict.entities += toBuilding(level + 2)]
					newDistrict.entities.add(tableDistrict)
				}
			}
							
		} else { // Start test mode
			if(dataElements.filter[container.ref == elem].length != 0){
				val dcDataDistrict = cityFactory.createDistrict
				dcDataDistrict.name = newDistrict.name + "_dcDataDistrict"
				dcDataDistrict.type = "dcDataDistrict"
				dcDataDistrict.id = elem.id + "_00002"
				dcDataDistrict.level = level + 1
				if(elem.iteration >= 1){
					dcDataDistrict.notInOrigin = "true"
				}
				
				dataElements.filter[container.ref == elem].forEach[dcDataDistrict.entities += toBuilding(level + 2)]
				newDistrict.entities.add(dcDataDistrict)
			}
		}		
								
		
		cityDocument.entities += newDistrict
		return newDistrict
	}
	
	
	// Mode: advanced
	def private District toAdvancedDistrict(FAMIXNamespace elem, int level){
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
		
		if (dcData.filter[container.ref == elem].length != 0){
				val dcDataDistrict = cityFactory.createDistrict
				dcDataDistrict.name = newDistrict.name + "_dcDataDistrict"
				dcDataDistrict.type = "dcDataDistrict"
				dcDataDistrict.id = elem.id + "_00001"
				dcDataDistrict.level = level + 1
				if(elem.iteration >= 1){
					dcDataDistrict.notInOrigin = "true"				
				} 
				
				dcData.filter[container.ref == elem].forEach[dcDataDistrict.entities += toBuilding(level + 2)]								
				newDistrict.entities.add(dcDataDistrict)			
        }
              
              
		  /* domains.filter[container.ref == elem].forEach[ doma |
			val newDomainDistrict = cityFactory.createDistrict
			newDomainDistrict.name = newDistrict.name + "_domainDistrict"
			newDomainDistrict.type = "dcDataDistrict"
			newDomainDistrict.id = elem.id + "_00002"
			newDomainDistrict.level = level + 1
			if(elem.iteration >= 1){
			   newDomainDistrict.notInOrigin = "true"
			   newDomainDistrict.entities += toBuilding(doma, level + 2)
			   dataElements.filter[domain == doma.value].forEach[newDomainDistrict.entities += toBuilding(level + 2)]
		       }
			// newDomainDistrict.entities += toBuilding(level + 2)	
			// TODO: find dataElements and bind them to district
	
			else {
			   newDomainDistrict.entities += toBuilding(doma, level + 2)			
	  		   dataElements.filter[container.ref == elem].filter[domain == doma.value].filter[iteration == 0].forEach[newDomainDistrict.entities += toBuilding(level + 2)] 	
	  		  }
	 
			newDistrict.entities.add(newDomainDistrict)
		]*/
		  
		  typeNames.forEach[ typeName | {
		  	if (dataElements.filter[container.ref == elem].filter[domain === null].filter[datatype == typeName].length != 0){ 
		  	val newDataElementDistrict = cityFactory.createDistrict
				newDataElementDistrict.name = newDistrict.name + "_dataElementDistrict"
				newDataElementDistrict.type = "dcDataDistrict"
				newDataElementDistrict.id = elem.id + "_00003"
				newDataElementDistrict.level = level + 1
				if(elem.iteration >= 1){
					newDataElementDistrict.notInOrigin = "true"
				}
			    //typeNames.forEach[newDataElementDistrict.entities += toBuilding (typeName, level + 2)]
				dataElements.filter[container.ref == elem].filter[domain === null].filter[datatype == typeName].forEach[newDataElementDistrict.entities += toBuilding(level + 2)]                        
				//newDataElementDistrict.entities += toBuilding(typeName, level + 2)
				newDistrict.entities.add(newDataElementDistrict)
              }
		  }]
		  
		classes.filter[container.ref == elem].forEach[ class |
			val newClassDistrict = cityFactory.createDistrict
			newClassDistrict.name = newDistrict.name + "_classDistrict"                                                           
			newClassDistrict.type = "classDistrict"
			newClassDistrict.id   = elem.id + "_00004"
			newClassDistrict.level = level + 1
			
			//newClassDistrict.entities += toBuilding(class, level + 2)

			methods.filter[parentType.ref == class].forEach[newClassDistrict.entities += toBuilding(level + 2)]
			attributes.filter[parentType.ref == class].forEach[newClassDistrict.entities += toBuilding(level + 2)]
		
			newDistrict.entities.add(newClassDistrict)
		]
		
		functionGroups.filter[container.ref == elem].forEach[ functionGroup |
			val newFunctionGroupDistrict = cityFactory.createDistrict
			newFunctionGroupDistrict.name = newDistrict.name + "_functionGroupDistrict"
			newFunctionGroupDistrict.type = "functionGroupDistrict"
			newFunctionGroupDistrict.id = elem.id + "_00003"
			newFunctionGroupDistrict.level = level + 1
			
			functionModules.filter[parentType.ref == functionGroup].forEach[newFunctionGroupDistrict.entities += toBuilding(level + 2)]
			attributes.filter[parentType.ref == functionGroup].forEach[newFunctionGroupDistrict.entities += toBuilding(level + 2)]
			
			newDistrict.entities.add(newFunctionGroupDistrict)
		]
		
		reports.filter[container.ref == elem].forEach[ report |
			val newReportDistrict = cityFactory.createDistrict
			newReportDistrict.name = newDistrict.name + "_reportDistrict"
			newReportDistrict.type = "reportDistrict"
			newReportDistrict.id = elem.id  + "_00004"
			newReportDistrict.level = level + 1
			newReportDistrict.entities += toAdvBuilding(report, level + 2)
			
			formroutines.filter[parentType.ref == report].forEach[newReportDistrict.entities += toBuilding(level + 2)]
			attributes.filter[parentType.ref == report].forEach[newReportDistrict.entities += toBuilding(level + 2)]
			
			newDistrict.entities.add(newReportDistrict)
		]
				
		cityDocument.entities += newDistrict
		return newDistrict
	}
	
	
	
	// End of Advanced Mode
	
	// Methods to build buildings
	
	/*def Building toBuilding(String[] typeNames, int level) {
	// throw new UnsupportedOperationException("TODO: auto-generated method stub")
	val newBuilding = cityFactory.createBuilding
	//newBuilding.name = typeNames.name
	//newBuilding.value = typeNames.value
	* newBuilding.fqn = typeNames.fqn
	* newBuilding.type = City.Utils.getFamixClassString(elem.class.simpleName)
	newBuilding.level = level 
	* if(typeNames.iteration >= 1){
	* newBuilding.notInOrign = "true"
	* }
	return newBuilding
	}*/
	
	def Building toBuilding(FAMIXReport elem, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		newBuilding.methodCounter = formroutines.filter[parentType.ref == elem].length
		newBuilding.dataCounter = attributes.filter[parentType.ref === elem].length
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		
		formroutines.filter[parentType.ref == elem].forEach[newBuilding.methods.add(toFloor)]
		if(config.showReportAttributes){
			newBuilding.dataCounter = attributes.filter[parentType.ref == elem].length
			attributes.filter[parentType.ref == elem].forEach[newBuilding.data.add(toChimney)]
		}
		
		return newBuilding
	}
	
	def Building toBuilding(FAMIXFunctionGroup elem, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		newBuilding.methodCounter = functionModules.filter[parentType.ref == elem].length
		newBuilding.dataCounter = attributes.filter[parentType.ref === elem].length
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		
		functionModules.filter[parentType.ref == elem].forEach[newBuilding.methods.add(toFloor)]
		if(config.showFugrAttributes){
			newBuilding.dataCounter = attributes.filter[parentType.ref == elem].length
			attributes.filter[parentType.ref == elem].forEach[newBuilding.data.add(toChimney)]
		}
		
		return newBuilding
	}
	
	def Building toBuilding(FAMIXDictionaryData elem, int level){
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		//newBuilding.dataCounter = 1  //- width/length
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		
		//ABAPStruc segments
		if(newBuilding.type == "FAMIX.ABAPStruc"){
			newBuilding.methodCounter = abapStrucElem.filter[container.ref == elem].length
			abapStrucElem.filter[container.ref == elem].forEach[newBuilding.methods.add(toFloor)]
		}
		
		//TableType segments
		if(newBuilding.type == "FAMIX.TableType"){
			newBuilding.methodCounter = ttyElements.filter[tableType.ref == elem].length
			ttyElements.filter[tableType.ref == elem].forEach[newBuilding.methods.add(toFloor)]
		}
		
		//Table segments
		if(newBuilding.type == "FAMIX.Table"){
			newBuilding.methodCounter = tableElements.filter[container.ref == elem].length
			tableElements.filter[container.ref == elem].forEach[newBuilding.methods.add(toFloor)]
		}
		
		return newBuilding
	}
	
	def private Building toBuilding(FAMIXClass elem, int level) {
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		
		// Is interface?
		if(elem.isInterface == "true") {
			newBuilding.type = "FAMIX.Interface"
		}else{
			newBuilding.type = "FAMIX.Class"
		}

		newBuilding.level = level
		newBuilding.id = elem.id

		inheritances.filter[subclass.ref === elem].forEach [ i |
			val inheritance = cityFactory.createReference
			inheritance.type = "Inheritance"
			inheritance.name = i.superclass.ref.name
			inheritance.fqn = i.superclass.ref.fqn
			newBuilding.references += inheritance
		]
		
		val currentMethods = methods.filter[parentType.ref === elem]
		val currentAttributes = attributes.filter[parentType.ref === elem]

		newBuilding.dataCounter = currentAttributes.length
		newBuilding.methodCounter = currentMethods.length

		if (config.buildingType == BuildingType::CITY_FLOOR) {
			methods.filter[parentType.ref.equals(elem)].forEach[newBuilding.methods.add(toFloor)]
			attributes.filter[parentType.ref.equals(elem)].forEach[newBuilding.data.add(toChimney)]
		} 
		return newBuilding
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
		inheritances.filter[subclass.ref === elem].forEach [ i |
			val inheritance = cityFactory.createReference
			inheritance.type = "Inheritance"
			inheritance.name = i.superclass.ref.name
			inheritance.fqn = i.superclass.ref.fqn
			newBuilding.references += inheritance
		]


		newBuilding.dataCounter = methods.filter[parentType.ref === elem].length
		newBuilding.methodCounter = attributes.filter[parentType.ref === elem].length

		if (config.buildingType == BuildingType::CITY_FLOOR) {
			methods.filter[parentType.ref.equals(elem)].forEach[newBuilding.methods.add(toFloor)]
			attributes.filter[parentType.ref.equals(elem)].forEach[newBuilding.data.add(toChimney)]
		}
		return newBuilding
	}

	def private Building toBuilding(FAMIXMethod elem, int level) {
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
		newBuilding.methodCounter = elem.numberOfStatements
		
		newBuilding.visibility = elem.modifiers.findFirst[it == "PRIVATE" || it == "PROTECTED" || it == "PUBLIC"]
		if (newBuilding.visibility === null) {
			newBuilding.visibility = "PUBLIC"	
		}			 

		return newBuilding
	}
	
	def private Building toBuilding(FAMIXAttribute elem, int level) {
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		
		val dataType = typeOfs.findFirst[element.ref == elem]

		if (dataType === null) {
			newBuilding.dataCounter = 1.0
		} else if (dataType.typeOf.ref.getClass.toString.contains("FAMIXABAPStruc")) {
			newBuilding.dataCounter = 2.0
		} else if (dataType.typeOf.ref.getClass.toString.contains("FAMIXTable") ||
			dataType.typeOf.ref.getClass.toString.contains("FAMIXTableType")) {
			newBuilding.dataCounter = 3.0
		} else if (dataType.typeOf.ref.getClass.toString.contains("FAMIXClass")) {
			newBuilding.dataCounter = 4.0
		}
		
		newBuilding.visibility = elem.modifiers.findFirst[it == "PRIVATE" || it == "PROTECTED" || it == "PUBLIC"] 
		
		return newBuilding		
	}
	
	def private Building toBuilding(FAMIXFunctionModule elem, int level) {
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		newBuilding.methodCounter = elem.numberOfStatements
		
		return newBuilding		
	}
	
	def private Building toBuilding(FAMIXFormroutine elem, int level) {
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		newBuilding.methodCounter = elem.numberOfStatements
		
		return newBuilding		
	}
	
	def private Building toAdvBuilding(FAMIXReport elem, int level) {
		val newBuilding = cityFactory.createBuilding
		newBuilding.name = elem.name
		newBuilding.value = elem.value
		newBuilding.fqn = elem.fqn
		newBuilding.type = CityUtils.getFamixClassString(elem.class.simpleName)
		newBuilding.level = level
		newBuilding.id = elem.id
		if(elem.iteration >= 1){
			newBuilding.notInOrigin = "true"
		}
		newBuilding.methodCounter = elem.numberOfStatements
		
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
	
	
	//ABAP
	def BuildingSegment create newBuildingSegment: cityFactory.createBuildingSegment toFloor(FAMIXFormroutine famixFormroutine) {
		newBuildingSegment.name = famixFormroutine.name
		newBuildingSegment.value = famixFormroutine.value
		newBuildingSegment.fqn = famixFormroutine.fqn
		newBuildingSegment.id = famixFormroutine.id
	} 
	
	def BuildingSegment create newBuildingSegment: cityFactory.createBuildingSegment toFloor(FAMIXFunctionModule famixFuncModule) {
		newBuildingSegment.name = famixFuncModule.name
		newBuildingSegment.value = famixFuncModule.value
		newBuildingSegment.fqn = famixFuncModule.fqn
		newBuildingSegment.id = famixFuncModule.id
	} 
	
	def BuildingSegment create newBuildingSegment: cityFactory.createBuildingSegment toFloor(FAMIXDictionaryData famixElement) {
		newBuildingSegment.name = famixElement.name
		newBuildingSegment.value = famixElement.value
		newBuildingSegment.fqn = famixElement.fqn
		newBuildingSegment.id = famixElement.id
	}


}