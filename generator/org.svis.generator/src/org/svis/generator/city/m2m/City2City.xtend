package org.svis.generator.city.m2m

import org.apache.commons.logging.LogFactory
import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.EcoreUtil2
import org.svis.generator.city.CityUtils
import org.svis.xtext.city.Building
import org.svis.xtext.city.BuildingSegment
import org.svis.xtext.city.District
import org.svis.xtext.city.Root
import org.svis.xtext.city.impl.CityFactoryImpl
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.OutputFormat
import org.svis.generator.SettingsConfiguration.BuildingType
import org.svis.generator.SettingsConfiguration.Original.BuildingMetric
import org.svis.generator.SettingsConfiguration.ClassElementsModes
import org.svis.generator.SettingsConfiguration.Panels.SeparatorModes
import org.codehaus.plexus.logging.console.ConsoleLogger
import org.svis.generator.SettingsConfiguration.FamixParser
import org.eclipse.emf.common.util.EList
import org.svis.generator.SettingsConfiguration.AbapCityRepresentation

class City2City extends WorkflowComponentWithModelSlot {

	val cityFactory = new CityFactoryImpl()
	var RGBColor[] PCKG_colors
	var RGBColor[] NSP_colors
	var RGBColor[] NOS_colors
	var RGBColor[] CLSS_colors
	val log = LogFactory::getLog(class)
	val config = SettingsConfiguration.instance

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("City2City has started.")

		// receive input from CITY-slot
		val cityRoot = ctx.get("CITY") as Root

		val districts = EcoreUtil2::getAllContentsOfType(cityRoot, District)
		val buildings = EcoreUtil2::getAllContentsOfType(cityRoot, Building)

		if (!districts.empty) {
			if (config.buildingType == BuildingType::CITY_BRICKS || config.buildingType == BuildingType::CITY_PANELS) {
				val buildingSegments = EcoreUtil2::getAllContentsOfType(cityRoot, BuildingSegment)
				buildingSegments.forEach[setBuildingSegmentAttributes]
			}

			if (config.buildingType == BuildingType::CITY_DYNAMIC) {
				val NSP_maxLevel = districts.filter[type == "FAMIX.Namespace"].sortBy[-level].head.level
				val CLSS_maxLevel = districts.filter[type == "FAMIX.Class"].sortBy[-level].head.level
				NSP_colors = createColorGradiant(new RGBColor(config.dynamicPackageColorStart),
					new RGBColor(config.dynamicPackageColorEnd), NSP_maxLevel)
				CLSS_colors = createColorGradiant(new RGBColor(config.dynamicClassColorStart),
					new RGBColor(config.dynamicClassColorEnd), CLSS_maxLevel)
			} else {
				val PCKG_maxLevel = districts.sortBy[-level].head.level
				PCKG_colors = createColorGradiant(new RGBColor(config.packageColorStart),
					new RGBColor(config.packageColorEnd), PCKG_maxLevel)
			}
			if (config.originalBuildingMetric == BuildingMetric::NOS) {
				val NOS_max = buildings.sortBy[-numberOfStatements].head.numberOfStatements
				NOS_colors = createColorGradiant(new RGBColor(config.classColorStart),
					new RGBColor(config.classColorEnd), NOS_max + 1)
			}
			districts.forEach[setDistrictAttributes]
			buildings.forEach[setBuildingAttributes]

			CityLayout::cityLayout(cityRoot)
			switch (config.buildingType) {
				case CITY_BRICKS:
					BrickLayout.brickLayout(cityRoot) // Layout for buildingSegments
				case CITY_PANELS:
					buildings.forEach[setBuildingSegmentPositions(cityRoot)]
				case CITY_FLOOR: {
					buildings.forEach[calculateFloors]
					buildings.forEach[calculateChimneys]
				}
				default: {
				} // CityDebugUtils.infoEntities(cityRoot.document.entities, 0, true, true)	
			}
		}
		ctx.set("CITYv2writer", cityRoot)

		// cityRoot enters slot, to be available for City2X3D-transformation
		var resource = new ResourceImpl()
		resource.contents += cityRoot
		ctx.set("CITYv2", resource)
		log.info("City2City has finished.")
	}

	def private RGBColor[] createColorGradiant(RGBColor start, RGBColor end, int maxLevel) {
		var steps = maxLevel - 1
		if (maxLevel == 1) {
			steps++
		}
		val r_step = (end.r - start.r) / steps
		val g_step = (end.g - start.g) / steps
		val b_step = (end.b - start.b) / steps

		val colorRange = newArrayOfSize(maxLevel)
		for (i : 0 ..< maxLevel) {
			val newR = start.r + i * r_step
			val newG = start.g + i * g_step
			val newB = start.b + i * b_step
			colorRange.set(i, new RGBColor(newR, newG, newB))
		}
		return colorRange
	}

	def private void setDistrictAttributes(District d) {
		d.height = config.heightMin
		if (config.buildingType == BuildingType::CITY_DYNAMIC) {
			if (d.type == "FAMIX.Class") {
				d.color = CLSS_colors.get(d.level - 1).asPercentage
			} else if (d.type == "FAMIX.Namespace") {
				d.color = NSP_colors.get(d.level - 1).asPercentage
			}
		} else if (config.outputFormat == OutputFormat::AFrame) {
			d.color = config.packageColorHex
		} else {
			if (config.parser == FamixParser::ABAP) {

				// Set color, if defined
				if (config.getAbapDistrictColor(d.type) !== null) {
					d.color = new RGBColor(config.getAbapDistrictColor(d.type)).asPercentage;
					d.textureURL = config.getAbapDistrictTexture(d.type);
				} else {
					d.color = PCKG_colors.get(d.level - 1).asPercentage
				}

				// Set transparency
				if (config.isNotInOriginTransparent() && d.notInOrigin == "true") {
					d.transparency = config.getNotInOriginTransparentValue()
				}
			} else {
				d.color = PCKG_colors.get(d.level - 1).asPercentage
			}
		}
	}

	def private setBuildingAttributes(Building b) {
		switch (config.buildingType) {
			case CITY_DYNAMIC,
			case CITY_ORIGINAL: setBuildingAttributesOriginal(b)
			case CITY_PANELS: setBuildingAttributesPanels(b)
			case CITY_BRICKS: setBuildingAttributesBricks(b)
			case CITY_FLOOR: setBuildingAttributesFloors(b)
		}
	}

	def private setBuildingAttributesOriginal(Building b) {
		if (b.dataCounter == 0) {
			b.width = config.widthMin
			b.length = config.widthMin
		} else {
			b.width = b.dataCounter
			b.length = b.dataCounter
		}
		if (b.methodCounter == 0) {
			b.height = config.heightMin
		} else {
			b.height = b.methodCounter
		}

		// set counters to zero, to let them vanish in city2.xml (optional)
		b.dataCounter = 0
		b.data.clear
		b.methodCounter = 0
		b.methods.clear
		if (config.originalBuildingMetric == BuildingMetric::NOS) {
			b.color = NOS_colors.get(b.numberOfStatements).asPercentage
		} else if (config.buildingType == BuildingType::CITY_DYNAMIC) {
			b.color = new RGBColor(config.dynamicMethodColor).asPercentage
		} else if (config.outputFormat == OutputFormat::AFrame) {
			b.color = config.classColorHex
		} else {
			b.color = new RGBColor(config.classColor).asPercentage
		}
	}

	def void setBuildingAttributesFloors(Building b) {

		if (b.dataCounter < 2) { // pko 2016
			b.width = 2 // TODO in settings datei aufnehmen
			b.length = 2
		} else {
			b.width = Math.ceil(b.dataCounter / 4.0) + 1 // pko 2016
			b.length = Math.ceil(b.dataCounter / 4.0) + 1 // pko 2016
		}

		if (b.methodCounter == 0) {
			b.height = config.heightMin
		} else {
			b.height = b.methodCounter
		}

		// b.dataCounter = 0 // set counters to zero, to let them vanish in city2.xml (optional)
		// we use dataCounter to show attributes under buildings. 
		b.color = 53 / 255.0 + " " + 53 / 255.0 + " " + 89 / 255.0 // pko 2016
		if (config.outputFormat == OutputFormat::AFrame) {
			b.color = config.classColorHex
		}

		// ABAP Logic
		if(config.parser == FamixParser::ABAP){
			if(config.abap_representation == AbapCityRepresentation::ADVANCED){		
				
				// We use custom models in advanced mode. Adjust sizes: 
				if(b.type == "FAMIX.DataElement"){
					b.width = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)
					b.length = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)
					b.height = b.height - (1 + config.getAbapAdvBuldingScale(b.type))
					
				} else if(b.type == "FAMIX.Domain"){
					b.width = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)
					b.length = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)
					
				} else if(b.type == "typeNames"){
					b.width = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)
					b.length = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)	
          
				} else if(b.type == "FAMIX.Attribute") {
          			if (b.dataCounter == 2.0) {
						b.height = 4
					}
					else if (b.dataCounter == 3.0) {
						b.height = 7
					}
					else if (b.dataCounter == 4.0) {
						b.height = 10
					}
				} else if (b.type == "FAMIX.Method") {
					b.width = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)
					b.length = config.getAbapAdvBuildingDefSize(b.type) * config.getAbapAdvBuldingScale(b.type)
					if (b.methodCounter == 0) {
						b.height = config.heightMin
					} else {
						b.height = b.methodCounter
					}
				}
						
			 // End of AbapCityRepresentation::ADVANCED
			} else { //AbapCityRepresentation::SIMPLE
				
				// Edit height and width
				if(b.type == "FAMIX.ABAPStruc" || b.type == "FAMIX.TableType"){
					b.width = 1.75
					
					b.height = b.methodCounter * config.strucElemHeight 
					if(config.strucElemHeight <= 1 || b.methodCounter == 0){
						b.height = b.height + 1
					}
					
				} else if(b.type == "FAMIX.DataElement"){
					b.height = 1
					b.width = 1.25
				}
				
				// If not in origin, set new min height
				if(b.notInOrigin == "true"){
					if((b.type == "FAMIX.Class" || b.type == "FAMIX.Interface" || b.type == "FAMIX.Report" 
						|| b.type == "FAMIX.FunctionGroup") && b.height < config.getNotInOriginSCBuildingHeight()){
						b.height = config.getNotInOriginSCBuildingHeight()
					}
				}
											
							
				// Use custom colors form settings
				if(config.getAbapBuildingColor(b.type) !== null){
					b.color = new RGBColor(config.getAbapBuildingColor(b.type)).asPercentage;
				}


				// If not in origin, set new min height
				if (b.notInOrigin == "true") {
					if ((b.type == "FAMIX.Class" || b.type == "FAMIX.Interface" || b.type == "FAMIX.Report" ||
						b.type == "FAMIX.FunctionGroup") && b.height < config.getNotInOriginSCBuildingHeight()) {
						b.height = config.getNotInOriginSCBuildingHeight()
					}
				}

				// Use color for building, if it's set
				if (config.getAbapBuildingColor(b.type) !== null) {
					b.color = new RGBColor(config.getAbapBuildingColor(b.type)).asPercentage;
				}
	
				// Edit transparency 	
				if (config.isNotInOriginTransparent() && b.notInOrigin == "true") {
					b.transparency = config.getNotInOriginTransparentValue()
				}

			} // End of AbapCityRepresentation::SIMPLE		
		} // End of ABAP logic
	}

	def private setBuildingAttributesPanels(Building b) {
		if (config.showBuildingBase) {
			b.height = config.heightMin
		} else {
			b.height = 0
		}
		var int areaUnit = 1
		if (config.classElementsMode == ClassElementsModes::ATTRIBUTES_ONLY) {
			areaUnit = b.methodCounter
		} else {
			areaUnit = b.dataCounter
		}
		if (areaUnit <= 1) {
			b.width = config.widthMin + config.panelHorizontalMargin * 2
			b.length = config.widthMin + config.panelHorizontalMargin * 2
		} else {
			b.width = config.widthMin * areaUnit + config.panelHorizontalMargin * 2
			b.length = config.widthMin * areaUnit + config.panelHorizontalMargin * 2
		}
		if (config.outputFormat == OutputFormat::AFrame) {
			b.color = config.classColorHex
		} else {
			b.color = new RGBColor(config.classColor).asPercentage
		}
	}

	def setBuildingAttributesBricks(Building b) {
		if (config.showBuildingBase) {
			b.height = config.heightMin
		} else {
			b.height = 0
		}
		if (config.outputFormat == OutputFormat::AFrame) {
			b.color = config.classColorHex
		} else {
			b.color = new RGBColor(config.classColor).asPercentage;
		}
		// Setting width, height & sideCapacity
		switch (config.brickLayout) {
			case STRAIGHT: {
				b.sideCapacity = 1;
			}
			case BALANCED: {
				switch (config.classElementsMode) {
					case ATTRIBUTES_ONLY: b.sideCapacity = calculateSideCapacity(b.methodCounter)
					case METHODS_AND_ATTRIBUTES: b.sideCapacity = calculateSideCapacity(b.dataCounter + b.methodCounter)
					default: b.sideCapacity = calculateSideCapacity(b.dataCounter)
				}
			}
			case PROGRESSIVE: {
				switch (config.classElementsMode) {
					case METHODS_ONLY: b.sideCapacity = calculateSideCapacity(b.methodCounter)
					case METHODS_AND_ATTRIBUTES: b.sideCapacity = calculateSideCapacity(b.dataCounter + b.methodCounter)
					default: b.sideCapacity = calculateSideCapacity(b.dataCounter)
				}
			}
			default: {
				b.sideCapacity = 1;
			}
		}
		b.width = config.brickSize * b.sideCapacity + config.brickHorizontalMargin * 2 +
			config.brickHorizontalGap * (b.sideCapacity - 1)
		b.length = config.brickSize * b.sideCapacity + config.brickHorizontalMargin * 2 +
			config.brickHorizontalGap * (b.sideCapacity - 1)

	}

	// Calculates side capacity for progressive/balanced bricks layout
	def private int calculateSideCapacity(double value) {
		var sc = 0 // side capacity
		var lc = 0 // layer capacity
		var nolMin = 0 // number of layers
		var bcMin = 0 // building capacity min
		var bcMax = 0 // building capacity max
		do {
			sc++
			lc = sc * 4
			nolMin = sc * 2
			bcMin = lc * nolMin
			bcMax = bcMin - 1
		} while (bcMax < value)

		return sc;
	}

	def private void setBuildingSegmentAttributes(BuildingSegment bs) {
		switch (config.buildingType) {
			case CITY_PANELS:
				setBuildingSegmentAttributesPanels(bs)
			case CITY_BRICKS:
				setBuildingSegmentAttributesBricks(bs)
			default: {
			}
		}
	}

	def private setBuildingSegmentAttributesPanels(BuildingSegment bs) {
		// Test whether method or function, e.g. in case functional programming language was source
		// if (bs.parent.type.equals("FAMIX.Class") || bs.parent.type.equals("FAMIX.ParameterizableClass")) {
		val b = bs.parent as Building
		// Setting up base area
		var int areaUnit = 1
		if (config.classElementsMode == ClassElementsModes::ATTRIBUTES_ONLY) {
			areaUnit = b.methodCounter
		} else {
			areaUnit = b.dataCounter
		}
		if (areaUnit <= 1) {
			bs.width = config.widthMin
			bs.length = config.widthMin
		} else {
			bs.width = config.widthMin * areaUnit
			bs.length = config.widthMin * areaUnit
		}
		// } else {
		// bs.width = Panels.PANEL_HEIGHT_UNIT
		// bs.length = Panels.PANEL_HEIGHT_UNIT
		// }
		// Setting up panel height
		var index = 0
		while (index < config.panelHeightTresholdNos.size &&
			bs.numberOfStatements >= config.panelHeightTresholdNos.get(index)) {
			index = index + 1
		}
		bs.height = config.panelHeightUnit * (index + 1)

		CityUtils.setBuildingSegmentColor(bs);
	}

	def private setBuildingSegmentAttributesBricks(BuildingSegment bs) {
		bs.width = config.brickSize
		bs.height = config.brickSize
		bs.length = config.brickSize

		CityUtils.setBuildingSegmentColor(bs);
	}

	def private void setBuildingSegmentPositions(Building b, Root cityRoot) {
		// Sorting elements
		val classElements = new BasicEList<BuildingSegment>()
		switch (config.classElementsMode) {
			case ATTRIBUTES_ONLY:
				classElements += b.data
			case METHODS_AND_ATTRIBUTES: {
				classElements += b.data
				classElements += b.methods
			}
			default:
				classElements += b.methods
		}
		CityUtils.sortBuildingSegments(classElements)

		// upper bound of the panel below the actual panel inside the loop
		var lowerBsPosY = b.position.y + b.height / 2 + config.panelVerticalMargin

		// Correcting the initial gap on top of building depending on SeparatorMode
		if (config.panelSeparatorMode == SeparatorModes::GAP || config.panelSeparatorMode == SeparatorModes::SEPARATOR)
			lowerBsPosY = lowerBsPosY - config.panelVerticalGap
		// System.out.println("")
		// Looping through methods of building
		for (bs : classElements) {
			// System.out.println(bs.getType() + " " + bs.getValue() + " " + bs.getModifiers() + " " + bs.getNumberOfStatements());
			val bsPos = cityFactory.createPosition
			bsPos.x = b.position.x
			bsPos.z = b.position.z
			switch (config.panelSeparatorMode) {
				case NONE: { // place segments on top of each other
					bsPos.y = lowerBsPosY + bs.height / 2
					bs.position = bsPos
					lowerBsPosY = bsPos.y + bs.height / 2
				}
				case GAP: { // Leave a free space between segments
					bsPos.y = lowerBsPosY + config.panelVerticalGap + bs.height / 2
					bs.position = bsPos
					lowerBsPosY = bsPos.y + bs.height / 2
				}
				case SEPARATOR: { // Placing additional separators
					bsPos.y = lowerBsPosY + bs.height / 2
					bs.position = bsPos

					// Placing a separator on top of the current method if it is not last method
					if (classElements.last != bs) {
						val sepPos = cityFactory.createPosition
						sepPos.x = b.position.x
						sepPos.y = bsPos.y + bs.height / 2 + config.panelSeparatorHeight / 2
						sepPos.z = b.position.z

						// Deciding which shape the separator has to have
						val nextElementType = classElements.get(classElements.indexOf(bs) + 1).type
						if ((bs.type == "FAMIX.Method" && nextElementType == "FAMIX.Method") ||
							!config.showAttributesAsCylinders) {
							val panelSeparator = cityFactory.createPanelSeparatorBox
							panelSeparator.position = sepPos
							panelSeparator.width = bs.width
							panelSeparator.length = bs.length
							bs.separator += panelSeparator
						} else {
							val panelSeparator = cityFactory.createPanelSeparatorCylinder
							panelSeparator.position = sepPos
							panelSeparator.radius = bs.width / 2
							bs.separator += panelSeparator
						}

						lowerBsPosY = sepPos.y + config.panelSeparatorHeight / 2
					}
				}
			}
		}
	}

	// pko 2016
	def void calculateFloors(Building b) {

		val cityFactory = new CityFactoryImpl

		val bHeight = b.height
		val bWidth = b.width
		val bLength = b.length

		val bPosX = b.position.x
		val bPosY = b.position.y
		val bPosZ = b.position.z
		
		val floors = b.methods
		val floorNumber = floors.length

		var floorCounter = 0

		for (floor : floors) {
			floorCounter++
		
		// Set standard values
			floor.height = bHeight / ( floorNumber + 2 ) * 0.80
			floor.width = bWidth * 1.1
			floor.length = bLength * 1.1
			floor.color = 20 / 255.0 + " " + 133 / 255.0 + " " + 204 / 255.0
			floor.position = cityFactory.createPosition
			floor.position.y = (bPosY - ( bHeight / 2) ) + bHeight / ( floorNumber + 2 ) * floorCounter
								
		//Edit values for ABAP	
			if(config.parser == FamixParser::ABAP){
				
				// Type is used to define shape in x3d
				floor.parentType = b.type
				
				var newBHeight = bHeight + config.strucElemHeight				 
				var newYPos = (bPosY - ( newBHeight / 2) ) + newBHeight / ( floorNumber + 2 ) * floorCounter
				
				//Make changes for specific types 
				if(b.type == "FAMIX.ABAPStruc"){
					floor.height = config.strucElemHeight
					floor.position.y = newYPos + 0.5
					
				}else if(b.type == "FAMIX.TableType"){
					floor.height = config.strucElemHeight
					floor.position.y = newYPos + 0.5
					
				}else if(b.type == "FAMIX.Table"){
					floor.width = bWidth * 0.55
				}
						
				
				// Use color for building segments, if it's set
				if(config.getAbapBuildingSegmentColor(b.type) !== null){
					floor.color = new RGBColor(config.getAbapBuildingSegmentColor(b.type)).asPercentage;
				}			
				
				
				// Edit floor height for source-code buildings in "not in origin" districts
				if(b.notInOrigin == "true"){
					if(b.type == "FAMIX.Class" || b.type == "FAMIX.Interface" || b.type == "FAMIX.Report" 
					|| b.type == "FAMIX.FunctionGroup"){
					
						floor.height = 0.4	
					}
				}
						
		// End of ABAP logic
				
		// Edit values for other languages
			}else{
				if (config.outputFormat == OutputFormat::AFrame) {
					floor.color = "#1485CC"
				}
				
				floor.position.y = (bPosY - ( bHeight / 2) ) + bHeight / ( floorNumber + 2 ) * floorCounter
			}
			
			floor.position.x = bPosX
			floor.position.z = bPosZ			

		}

	}

	// pko 2016
	def void calculateChimneys(Building b) {

		val cityFactory = new CityFactoryImpl

		val bHeight = b.height
		val bWidth = b.width
		// val bLength = b.length
		
		val bPosX = b.position.x
		val bPosY = b.position.y
		val bPosZ = b.position.z

		
		val chimneys = b.data
		// val chimneyNumber = chimneys.length
		var courner1 = newArrayList()
		var courner2 = newArrayList()
		var courner3 = newArrayList()
		var courner4 = newArrayList()

		var chimneyCounter = 0

		for (chimney : chimneys) {
	
			if(config.parser == FamixParser::ABAP && config.showAttributesBelowBuildings){
				chimney.height = config.attributesBelowBuildingsHeight - 0.5
			}else{
				chimney.height = config.attributesHeight
			}
			chimney.width = 0.5
			chimney.length = 0.5
			
			

			if (config.outputFormat == OutputFormat::AFrame) {
				chimney.color = "#FFFC19"
			} else {
				chimney.color = 255 / 255.0 + " " + 252 / 255.0 + " " + 25 / 255.0
			}
			chimney.position = cityFactory.createPosition

			if (chimneyCounter % 4 == 0) {
				courner1.add(chimney)
			}
			if (chimneyCounter % 4 == 1) {
				courner2.add(chimney)
			}
			if (chimneyCounter % 4 == 2) {
				courner3.add(chimney)
			}
			if (chimneyCounter % 4 == 3) {
				courner4.add(chimney)
			}
			chimneyCounter++
		}
		

		chimneyCounter = 0
		for (chimney : courner1) {
			chimney.position.x = (bPosX - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter)
			chimney.position.y = getYforChimney(b, chimney)
			chimney.position.z = (bPosZ - ( bWidth / 2) ) + 0.5
			chimneyCounter++
		}

		chimneyCounter = 0
		for (chimney : courner2) {
			chimney.position.x = (bPosX + ( bWidth / 2) ) - 0.5
			chimney.position.y = getYforChimney(b, chimney)
			chimney.position.z = (bPosZ - ( bWidth / 2) ) + 0.5 + (1 * chimneyCounter)
			chimneyCounter++
		}

		chimneyCounter = 0
		for (chimney : courner3) {
			chimney.position.x = (bPosX + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter)
			chimney.position.y = getYforChimney(b, chimney)
			chimney.position.z = (bPosZ + ( bWidth / 2) ) - 0.5
			chimneyCounter++
		}

		chimneyCounter = 0
		for (chimney : courner4) {
			chimney.position.x = (bPosX - ( bWidth / 2) ) + 0.5
			chimney.position.y = getYforChimney(b, chimney)
			chimney.position.z = (bPosZ + ( bWidth / 2) ) - 0.5 - (1 * chimneyCounter)
			chimneyCounter++
		}	
	}
	
	
	// Display chimneys at top/bottom (depends on settings)
	def double getYforChimney(Building b, BuildingSegment chimney){
		
		if(config.parser == FamixParser::ABAP && config.showAttributesBelowBuildings){
			return (b.position.y - ( b.height / 2) ) - (chimney.height / 2 + 0.25)
		}else{
			return (b.position.y + ( b.height / 2) ) + 0.5 //Original
		}

	}
}
