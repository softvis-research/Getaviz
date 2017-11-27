package org.svis.generator.city.m2t

import java.util.List
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.svis.generator.city.CitySettings
import org.svis.generator.city.CitySettings.Panels
import org.svis.xtext.city.Building
import org.svis.xtext.city.BuildingSegment
import org.svis.xtext.city.Entity
import org.svis.xtext.city.PanelSeparatorBox
import org.svis.xtext.city.PanelSeparatorCylinder
import org.svis.generator.city.CitySettings.BuildingType
import org.svis.xtext.city.District

class City2AFrame {
	
	val log = LogFactory::getLog(getClass)

	def	toAFrameBody(Resource resource) {
		log.info("City2AFrame has started")
		val entities = EcoreUtil2::getAllContentsOfType(resource.contents.head, Entity)
		val Body = entities.toAFrameModel()
		log.info("City2AFrame has finished")
		return Body
	}
	
		// transform logic
	def String toAFrameModel(List<Entity> entities) '''
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
				«ENDIF»
				«IF(CitySettings::BUILDING_TYPE == BuildingType::CITY_FLOOR)»
					«FOR floor: (entity as Building).methods»
						«toFloor(floor)»
					«ENDFOR»	
					«FOR chimney: (entity as Building).data»
						«toChimney(chimney)»
					«ENDFOR»
				«ENDIF»	
				«IF(CitySettings::BUILDING_TYPE == BuildingType::CITY_BRICKS || CitySettings::BUILDING_TYPE == BuildingType::CITY_PANELS)»
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
		<a-box position="«district.position.x + " " + district.position.y + " " + district.position.z»"
			width="«district.width»"
			height="«district.height»"
			depth="«district.length»"
			color="«district.color»"
			shader="flat"
			fog="false"
			flat-shading="true">
		</a-box>
	'''
	
	def String toBuilding(Entity building) '''
		<a-box position="«building.position.x + " " + building.position.y + " " + building.position.z»"
				width="«building.width»"
				height="«building.height»"
				depth="«building.length»"
				color="«building.color»"
				shader="flat"
				fog="false"
				flat-shading="true">
		</a-box>
	'''

	def String toBuildingSegment(BuildingSegment bs) '''
			«IF CitySettings::BUILDING_TYPE == BuildingType.CITY_PANELS
					&& bs.type == "FAMIX.Attribute"
					&& CitySettings::SHOW_ATTRIBUTES_AS_CYLINDERS»
				<a-cylinder position="«bs.position.x + " " + bs.position.y + " " + bs.position.z»"
					 radius="«bs.width/2»"
					 height="«bs.height»" 
					 color="«bs.color»"
					 shader="flat"
					 fog="false"
					 flat-shading="true"
					 segments-height="2"
					 segments-radial="20">
				</a-cylinder>
			«ELSE»
				<a-box position="«bs.position.x + " " + bs.position.y + " " + bs.position.z»"
						width="«bs.width»"
						height="«bs.height»"
						depth="«bs.length»"
						color="«bs.color»"
						shader="flat"
						fog="false"
						flat-shading="true">
				</a-box>
			«ENDIF»
		«FOR separator : bs.separator»
			«IF separator instanceof PanelSeparatorCylinder»
				«val separatorC = separator»
				<a-cylinder position="«separator.position.x + " " + separator.position.y + " " + separator.position.z»"
					 radius="«separatorC.radius»" 
					 height="«Panels::SEPARATOR_HEIGHT»" 
					 color="«CitySettings::COLOR_BLACK_HEX»"
					 shader="flat"
					 fog="false"
					 flat-shading="true"
					 segments-height="2"
					 segments-radial="20">
				</a-cylinder>
			«ELSE»
				«val separatorB = separator as PanelSeparatorBox»
				<a-box position="«separator.position.x + " " + separator.position.y + " " + separator.position.z»"
						width="«separatorB.width»"
						height="«Panels::SEPARATOR_HEIGHT»"
						depth="«separatorB.length»"
						color="«CitySettings::COLOR_BLACK_HEX»"
						shader="flat"
						fog="false"
						flat-shading="true">
				</a-box>
			«ENDIF»
		«ENDFOR»
	'''
		def toFloor(BuildingSegment floor) '''
				<a-box position="«floor.position.x + " " + floor.position.y + " " + floor.position.z»"
					width="«floor.width»"
					height="«floor.height»"
					depth="«floor.length»"
					color="«floor.color»"
					shader="flat"
					fog="false"
					flat-shading="true">
				</a-box>
	'''
	
	def toChimney(BuildingSegment chimney) '''
				<a-box position="«chimney.position.x + " " + chimney.position.y + " " + chimney.position.z»"
					width="«chimney.width»"
					height="«chimney.height»"
					depth="«chimney.length»"
					color="«chimney.color»"
					shader="flat"
					fog="false"
					flat-shading="true">
				</a-box>
	'''
}