package org.svis.generator.plant.m2t;

import org.svis.xtext.plant.Petal
import org.svis.xtext.plant.Entity

/**
 * Paint the petal and its variants in X3D.
 */
class PlantShapePetal {

	public final String DEFAULT = "DEFAULT";
	public final String DEFAULT_WITH_CYLINDER = "DEFAULT_WITH_CYLINDER";
	public final String REALITIC_PETAL = "REALITIC_PETAL";
	
	/**
	 * Get shape string for shapeID. 
	 */
	def String getShape(String shapeID, Petal petal, Entity entity){
		
		if(shapeID.equals(DEFAULT)){
			return toDefaultPetal(petal);
		}
		if(shapeID.equals(DEFAULT_WITH_CYLINDER)){
			return toDefaultWithCylinderPetal(petal);
		}
		if(shapeID.equals(REALITIC_PETAL)){
			return toRealisticPetal(petal);
		}
		return null;
	}

	private def String toDefaultPetal(Petal petal) '''
	<!-- pental -->
	<transform DEF='«petal.id»' 
	translation='«petal.position.x» «petal.position.y» «petal.position.z»' 
	scale='2 2 2' 
	rotation='1 0 0 «(3.14/2)»' > 
	<transform rotation='0 0 1 «petal.angular»'>
		<transform rotation='0 1 0 «petal.currentPetalAngle»'>
			<Shape>
			<Appearance>
				«petal.texture»
				«petal.color»
			</Appearance>
			<box size='1.8 0.6 0.1'  />
			</Shape>
		</transform>
		</transform>
	</transform>
	'''
	private def String toDefaultWithCylinderPetal(Petal petal) '''
	<!-- pental -->
	<transform DEF='«petal.id»' 
	translation='«petal.position.x» «petal.position.y» «petal.position.z»' 
	scale='2 2 2' 
	rotation='1 0 0 «(3.14/2)»' > 
	<transform rotation='0 0 1 «petal.angular»'>
		<transform rotation='0 1 0 «petal.currentPetalAngle»'>
			<Shape>
				<Appearance>
					«petal.texture»
					«petal.color»
				</Appearance>
				<box size='1.8 0.6 0.1'  />
			</Shape>
		</transform>
			<Shape>				
				<Appearance>
					«petal.texture»
					«petal.color»
				</Appearance>
				<Cylinder height="1" radius="0.5"/>
			</Shape>
		</transform>
	</transform>
	'''
	private def String toRealisticPetal(Petal petal) '''
	<!-- pental -->
	<transform DEF='«petal.id»' translation='«petal.position.x» «petal.position.y» «petal.position.z»' 
	scale='2 2 2' 
	rotation='1 0 0 «(3.14/2)»' > 
		<transform rotation='0 0 1 «petal.angular»'>
			<transform rotation='0 1 0 «petal.currentPetalAngle»'>
				<transform rotation='1 0 0 «(3.14/2)»' scale="4 2 1" > 
				<Shape>				
					<Appearance>
						«petal.texture»
						«petal.color»
					</Appearance>
					<Cylinder height="0.1" radius="0.25"/>
				</Shape>
				</transform>				
			</transform>
		</transform>
	</transform>
	'''
}