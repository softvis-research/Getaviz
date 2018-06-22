package org.svis.generator.plant.m2t

import org.svis.xtext.plant.Stem
import org.svis.xtext.plant.Entity
import org.svis.generator.SettingsConfiguration

/**
 * Paint the stem and its variants in X3D.
 */
class PlantShapeStem {
	val config = new SettingsConfiguration
	/**
	 * Default Shape for the plant metaphor.
	 */
	public final String DEFAULT = "DEFAULT";
	public final String REALISTIC = "REALISTIC";
	public final String WITHOUT_HEAD = "WITHOUT_HEAD";
	var i = 0
	var j = 100
	
	/**
	 * Get shape string for shapeID. 
	 */
	def String getShape(String shapeID, Stem stem, Entity entity){
		
		if(shapeID.equals(DEFAULT)){
			return toDefaultStem(stem);
		}
		if(shapeID.equals(REALISTIC)){
			return toRealisticStem(stem);
		}
		if(shapeID.equals(WITHOUT_HEAD)){
			return toStemWithoutHead(stem);
		}
	return null;
	}

	private def String toStemWithoutHead(Stem stem) '''
		<!-- stem -->
		<Group DEF='«stem.id»'>
			<Transform translation='«stem.position.x +" "+ stem.position.y +" "+ stem.position.z»'>
				<Shape>
					<Box size='«stem.width +" "+ stem.height +" "+ stem.length»'></Box>
					<Appearance>
						«stem.texture»
						«stem.color»
					</Appearance>
				</Shape>
			</Transform>
		</Group>		
	'''
	
	private def String toDefaultStem(Stem stem) '''
		<!-- stem -->
		<transform DEF='«stem.id»'>
			<Transform translation='«stem.position.x +" "+ stem.position.y +" "+ stem.position.z»'>
				<Shape>
					
					<Appearance>
						«stem.texture»
						«stem.color»
					</Appearance>
					<Box size='«stem.width +" "+ stem.height +" "+ stem.length»'></Box>
				</Shape>
			</Transform>
		
			<!-- Head/Kegel/Cone -->
			<transform DEF='«stem.id + (j++).toString»' 
				translation='«stem.position.x +" "+ (stem.height+stem.level) +" "+ stem.position.z»' 
					scale='2 «config.cronHeight» 2' 
					rotation='1 0 0 3.14'>
					<Shape>
					<Appearance>
						«stem.headTexture»
						«stem.headColor»
					</Appearance>
					<cone />
				</Shape>
			</transform>
		
			<!-- plant head top part -->
			<transform DEF='«stem.id + (i++).toString»'
				translation='«stem.position.x +" "+ (stem.height + stem.level + config.cronHeight) +" "+ stem.position.z»»' 
				rotation='1 0 0 3.14'>
					<Shape>
					<Appearance>
						«stem.headTopPartTexture»
						«stem.headTopPartColor»
					</Appearance>
					<Cylinder height='«config.cronHeadHeight»' radius='2'/>
				</Shape>
			</transform>
		</transform>
	'''

	private def String toRealisticStem(Stem stem) '''
		<!-- stem -->
		<transform DEF='«stem.id»'>
			<Transform translation='«stem.position.x +" "+ stem.position.y +" "+ stem.position.z»'>
				<Shape>
					<Cylinder height="«stem.height»" radius="«(stem.width/2)»"/>
					<Appearance>
						«stem.texture»
						«stem.color»
					</Appearance>
				</Shape>
			</Transform>
		
			<!-- Head/Kegel/Cone -->
			<transform DEF='«stem.id»' 
				translation='«stem.position.x +" "+ (stem.height+stem.level) +" "+ stem.position.z»' 
					scale='2 «config.cronHeight» 2' 
					rotation='1 0 0 3.14'>
					<Shape>
					<Appearance>
						«stem.headTexture»
						«stem.headColor»
					</Appearance>
					<cone />
				</Shape>
			</transform>
		
			<!-- plant head top part -->
			<transform 
				translation='«stem.position.x +" "+ (stem.height + stem.level + config.cronHeight) +" "+ stem.position.z»»' 
				rotation='1 0 0 3.14'>
					<Shape>
					<Appearance>
						«stem.headTopPartTexture»
						«stem.headTopPartColor»
					</Appearance>
					<Cylinder height='«config.cronHeadHeight»' radius='2'/>
				</Shape>
			</transform>
		</transform>
	'''
}