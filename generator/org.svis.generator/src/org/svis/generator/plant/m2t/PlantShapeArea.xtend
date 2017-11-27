package org.svis.generator.plant.m2t;

import org.svis.xtext.plant.Entity
import org.svis.xtext.plant.Area

/**
 * Paint the petal and its variants in X3D.
 */
class PlantShapeArea {

	public final String DEFAULT = "DEFAULT";
	public final String CYLINDER = "CYLINDER";
	
	/**
	 * Get shape string for shapeID. 
	 */
	def String getShape(String shapeID, Area area, Entity entity){
		
		if(shapeID.equals(DEFAULT)){
			return toDefaultArea(area);
		}
		if(shapeID.equals(CYLINDER)){
			return toCircle(area);
		}
	
		return null;
	}

	private def String toDefaultArea(Entity entity) '''
		<Group DEF='«entity.id»'>
			<Transform translation='«entity.position.x +" "+ entity.position.y +" "+ entity.position.z»'>
				<Shape>
					<Box size='«entity.width +" "+ entity.height +" "+ entity.length»'></Box>
					<Appearance>
						«entity.texture»
						«entity.color»
					</Appearance>
				</Shape>
			</Transform>
		</Group>
	'''
	private def String toCircle(Entity entity) '''
		<Group DEF='«entity.id»'>
			<Transform translation='«entity.position.x +" "+ entity.position.y +" "+ entity.position.z»'>
				<Shape>			
					<Cylinder height="«entity.height»" radius="«(entity.width * 3/4)»"/>
					<Appearance>
						«entity.texture»
						«entity.color»
					</Appearance>
				</Shape>
			</Transform>
		</Group>
	'''
}