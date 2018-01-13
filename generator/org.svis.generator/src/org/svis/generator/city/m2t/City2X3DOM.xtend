package org.svis.generator.city.m2t;

import java.util.List
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.svis.generator.city.CitySettings
import org.svis.generator.city.CitySettings.Panels
import org.svis.generator.city.m2m.CityLayout
import org.svis.generator.city.m2m.Rectangle
import org.svis.xtext.city.Building
import org.svis.xtext.city.BuildingSegment
import org.svis.xtext.city.Entity
import org.svis.xtext.city.PanelSeparatorBox
import org.svis.xtext.city.PanelSeparatorCylinder
import org.svis.generator.city.CitySettings.BuildingType
import org.svis.xtext.city.District

class City2X3DOM {
	val log = LogFactory::getLog(getClass)

	def	toX3DOMBody(Resource resource) {
		log.info("City2X3DOM has started")
		val entities = EcoreUtil2::getAllContentsOfType(resource.contents.head, Entity)
		val rootEntity = CityLayout::rootRectangle
		val Body = viewports(rootEntity) + entities.toX3DOMModel()
		log.info("City2X3DOM has finished")
		return Body
	}

	def String settingsInfo() '''
		<SettingsInfo ClassElements='«CitySettings::CLASS_ELEMENTS_MODE»' SortModeCoarse='«CitySettings::CLASS_ELEMENTS_SORT_MODE_COARSE»' SortModeFine='«CitySettings::CLASS_ELEMENTS_SORT_MODE_FINE»' SortModeFineReversed='«CitySettings::CLASS_ELEMENTS_SORT_MODE_FINE_DIRECTION_REVERSED»' Scheme='«CitySettings::SCHEME»' ShowBuildingBase='«CitySettings::SHOW_BUILDING_BASE»'
		«IF CitySettings::BUILDING_TYPE == BuildingType.CITY_BRICKS»
			BrickLayout='«CitySettings::BRICK_LAYOUT»'
		«ELSEIF CitySettings::BUILDING_TYPE == BuildingType.CITY_PANELS»
			AttributesAsCylinders='«CitySettings::SHOW_ATTRIBUTES_AS_CYLINDERS»' PanelSeparatorMode='«CitySettings::PANEL_SEPARATOR_MODE»'
		«ELSE»
		«ENDIF»
		/>
	'''

	def String viewports(Rectangle rootEntity) '''
		«var width = rootEntity.width»
		«var length = rootEntity.length»
		<Group DEF='Viewpoints'>
			<Viewpoint description='Initial' position='«-width*0.5 +" "+ ((width+length)/2)*0.25 +" "+ -length*0.5»' orientation='0 1 0 4' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
			<Viewpoint description='Opposite Side' position='«width*1.5 +" "+ ((width+length)/2)*0.25 +" "+ length*1.5»' orientation='0 1 0 0.8' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
			<Viewpoint description='Screenshot' position='«-width*0.5 +" "+ ((width+length)/2)*0.75 +" "+ -length*0.5»' orientation='0.1 0.95 0.25 3.8' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
			<Viewpoint description='Screenshot Opposite Side' position='«width*1.5 +" "+ ((width+length)/2)*0.75 +" "+ length*1.5»' orientation='-0.5 0.85 0.2 0.8' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
		</Group>
	'''

	// transform logic
	def String toX3DOMModel(List<Entity> entities) '''
		«FOR entity : entities»
			«IF entity.type == "FAMIX.Namespace"»
				«toDistrict(entity)»
			«ENDIF»
			«IF entity.type == "FAMIX.Class" || entity.type == "FAMIX.ParameterizableClass"»
				«IF CitySettings::BUILDING_TYPE == BuildingType.CITY_ORIGINAL || CitySettings::SHOW_BUILDING_BASE»
					«toBuilding(entity)»
				«ENDIF»
				«IF CitySettings::BUILDING_TYPE === BuildingType.CITY_DYNAMIC»
					«FOR bs: (entity as District).entities»
						«toBuilding(bs)»
					«ENDFOR»
				«ELSE»	
					«FOR bs: (entity as Building).methods»
						«toBuildingSegment(bs)»
					«ENDFOR»
					«FOR bs: (entity as Building).data»
						«toBuildingSegment(bs)»
					«ENDFOR»
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''

	def String toDistrict(Entity district) '''
			<Transform id='«district.id»' translation='«district.position.x +" "+ district.position.y +" "+ district.position.z»'>
				<Shape>
					<Box size='«district.width +" "+ district.height +" "+ district.length»'></Box>
					<Appearance>
						<Material diffuseColor='«district.color»'></Material>
					</Appearance>
				</Shape>
			</Transform>
	'''
	
	def String toBuilding(Entity building) '''
			<Transform id='«building.id»' translation='«building.position.x +" "+ building.position.y +" "+ building.position.z»'>
				<Shape>
					<Box size='«building.width +" "+ building.height +" "+ building.length»'></Box>
					<Appearance>
						<Material id='«building.id»_MATERIAL' diffuseColor='«building.color»'></Material>
					</Appearance>
				</Shape>
			</Transform>
	'''

	def String toBuildingSegment(BuildingSegment bs) '''
		«var x = bs.position.x»
		«var y = bs.position.y»
		«var z = bs.position.z»
		«var width = bs.width»
		«var height = bs.height»
		«var length = bs.length»
			<Transform id='«bs.id»' translation='«x +" "+ y +" "+ z»'>
				<Shape>
				«IF CitySettings::BUILDING_TYPE == BuildingType.CITY_PANELS
						&& bs.type == "FAMIX.Attribute"
						&& CitySettings::SHOW_ATTRIBUTES_AS_CYLINDERS»
					<Cylinder radius='«width/2»' height='«height»'></Cylinder>
				«ELSE»
					<Box size='«width +" "+ height +" "+ length»'></Box>
				«ENDIF»
					<Appearance>
						<Material diffuseColor='«bs.color»'></Material>
					</Appearance>
				</Shape>
			</Transform>
			«FOR separator : bs.separator»
			<Transform translation='«separator.position.x +" "+ separator.position.y +" "+ separator.position.z»'>
				<Shape>
				«IF separator instanceof PanelSeparatorCylinder»
					«val separatorC = separator»
					<Cylinder radius='«separatorC.radius»' height='«Panels::SEPARATOR_HEIGHT»'></Cylinder>
				«ELSE»
					«val separatorB = separator as PanelSeparatorBox»
					<Box size='«separatorB.width +" "+ Panels::SEPARATOR_HEIGHT + " "+ separatorB.length»'></Box>
				«ENDIF»
					<Appearance>
						<Material diffuseColor='«CitySettings::COLOR_BLACK.asPercentage»'></Material>
					</Appearance>
				</Shape>
			</Transform>
			«ENDFOR»
	'''
}