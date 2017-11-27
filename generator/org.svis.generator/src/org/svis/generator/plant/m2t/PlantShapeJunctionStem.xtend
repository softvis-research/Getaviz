package org.svis.generator.plant.m2t

import org.svis.xtext.plant.Entity
import org.svis.xtext.plant.Junction

/**
 * Paint the stem and its variants in X3D.
 */
class PlantShapeJunctionStem {
	public final String DEFAULT = "DEFAULT";
	public final String REALISTIC = "REALISTIC";
	
	/**
	 * Get shape string for shapeID. 
	 */
	def String getShape(String shapeID, Junction junction, Entity entity){
		
		if(shapeID.equals(DEFAULT)){
			return toDefaultJunction(junction);
		}
		if(shapeID.equals(REALISTIC)){
			return toRealisticJunction(junction);
		}
	return null;
	}

	/**
	 * Paint the junction in X3D.
	 */
	private def String toDefaultJunction(Junction j) '''	
	<!-- Junction -->
	<transform DEF='«j.id»' 
	translation='«j.position.x» «j.position.y» «j.position.z»' 
	scale='2 2 «(j.height+2)»'
	rotation='1 0 0 «(3.14/2)»' > 
	<transform rotation='0 0 1 «j.angular»'>
		<transform rotation='0 1 0 «j.currentJunctionAngle»'>
		<Shape>
		<Appearance>
			«j.texture»
			«j.color»
		</Appearance>
		<box size='0.6 0.6 7.267944042220666'  />
		</Shape>
		</transform>
		</transform>
	</transform>
	
	<!-- Head/Kegel/Cone -->
	<transform DEF='«j.id»' 
		translation='«j.headPosition.x» «(j.headPosition.y)» «j.headPosition.z»' 
			scale='2 2 2' rotation='1 0 0 3.14'>
			<Shape>
			<Appearance>
				«j.headTexture»
				«j.headColor»
			</Appearance>
			<cone />
		</Shape>
	</transform>
	
	<!-- junction plant head part -->
		<transform 
		translation='«j.headPosition.x» «(j.headPosition.y + 2)» «(j.headPosition.z)»' 
		rotation='1 0 0 3.14'>
			<Shape>
			<Appearance>
				«j.headTopPartTexture»
				«j.headTopPartColor»
			</Appearance>
			<Cylinder height='0.5' radius='2'/>
		</Shape>
	</transform>
	'''
	/**
	 * Paint the junction in X3D.
	 */
	private def String toRealisticJunction(Junction j) '''	
	<!-- Junction -->
		<transform DEF='«j.id»' 
		translation='«j.position.x» «j.position.y» «j.position.z»' 
		rotation='1 0 0 «(3.14/2)»' > 
		<transform rotation='0 0 1 «(j.angular + (3.14/2))»'>
			<transform rotation='1 0 0 «(j.currentJunctionAngle * -0.7)»'>
			<Shape>
			<Appearance>
				«j.texture»
				«j.color»
			</Appearance>
			<Cylinder height="«(2 * 10)»" radius="«1.2»"/>
			</Shape>
			</transform>
			</transform>
		</transform>
	
	<!-- Head/Kegel/Cone -->
	<transform DEF='«j.id»' 
		translation='«j.headPosition.x» «(j.headPosition.y)» «j.headPosition.z»' 
			scale='2 2 2' rotation='1 0 0 3.14'>
			<Shape>
			<Appearance>
				«j.headTexture»
				«j.headColor»
			</Appearance>
			<cone />
		</Shape>
	</transform>
	
	<!-- junction plant head part -->
		<transform 
		translation='«j.headPosition.x» «(j.headPosition.y + 2)» «(j.headPosition.z)»' 
		rotation='1 0 0 3.14'>
			<Shape>
			<Appearance>
				«j.headTopPartTexture»
				«j.headTopPartColor»
			</Appearance>
			<Cylinder height='0.5' radius='2'/>
		</Shape>
	</transform>
	'''
	
}