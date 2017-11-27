package org.svis.generator.plant.m2t;

import org.svis.xtext.plant.Entity
import org.svis.xtext.plant.PollStem

/**
 * Paint the petal and its variants in X3D.
 */
class PlantShapePollStem {

	public final String DEFAULT = "DEFAULT";
	public final String STICK = "STICK";
	public final String SPHERE = "SPHERE";
	public final String POLLPETAL = "POLLPETAL";
	var i = 0
	/**
	 * Get shape string for shapeID. 
	 */
	def String getShape(String shapeID, PollStem pollStem, Entity entity){
		
		if(shapeID.equals(DEFAULT)){
			return toDefault(pollStem);
		}
		if(shapeID.equals(STICK)){
			return toSTICK(pollStem);
		}
		if(shapeID.equals(SPHERE)){
			return toSphere(pollStem);
		}
		if(shapeID.equals(POLLPETAL)){
			return toPollPetal(pollStem);
		}
		return null;
	}
	/**
	 * Paint the Pollstem in X3D.
	 */
	private def String toDefault(PollStem pollStem) '''	
	<!-- pollstem -->
	<transform 
	DEF='«pollStem.id + (i++).toString»' 
	translation='«pollStem.position.x» «pollStem.position.y» «pollStem.position.z»' 
	scale='2 2 2' 
	rotation='1 0 0 «(3.14/2)»' > 
	<transform rotation='0 0 1 «pollStem.angular»'>	
		<transform rotation='0 1 0 «pollStem.currentPollStemAngle»'>
			<Shape>
			<Appearance>
				«pollStem.texture»
				«pollStem.color»
			</Appearance>
			<box size='0.05 0.05 1.5'  />
			</Shape>
		</transform>
		</transform>
	</transform>
	
	<!-- poll ball -->
	<transform DEF='«pollStem.id»' 
	translation='«pollStem.ballPosition.x» «(pollStem.ballPosition.y + 1)» «pollStem.ballPosition.z»' 
	scale='2 2 2' 
	rotation='1 0 0 «(3.14/2)»' > 
	<transform rotation='0 0 1 «pollStem.currentPollStemAngle»'>
		<Shape>
			<Appearance>
				«pollStem.ballTexture»
				«pollStem.ballColor»
			</Appearance>
			<box size='0.15 0.15 0.15'  />
		</Shape>
	</transform>
	</transform>
	'''
	/**
	 * Paint the Pollstem in X3D.
	 */
	private def String toSTICK(PollStem pollStem) '''	
	<!-- pollstem -->
	<transform 
	DEF='«pollStem.id»' 
	translation='«pollStem.position.x» «pollStem.position.y» «pollStem.position.z»' 
	scale='2 2 2' 
	rotation='1 0 0 «(3.14/2)»' > 
	<transform rotation='0 0 1 «pollStem.angular»'>	
		<transform rotation='0 1 0 «(pollStem.currentPollStemAngle*3)»'>
			<Shape>
			<Appearance>
				«pollStem.texture»
				«pollStem.color»
			</Appearance>
			<box size='0.08 0.08 1.5'  />
			</Shape>
		</transform>
		</transform>
	</transform>
	'''
		/**
	 * Paint the Pollstem in X3D.
	 */
	private def String toSphere(PollStem pollStem) '''	
	<!-- pollstem -->
	<transform 
	DEF='«pollStem.id»' 
	translation='«pollStem.position.x» «pollStem.position.y» «pollStem.position.z»' 
	scale='2 2 2' > 
			<Shape>
			<Appearance>
				«pollStem.texture»
				«pollStem.color»
			</Appearance>
			<sphere radius='0.06'></sphere>  
			</Shape>
	</transform>
	'''
	private def String toPollPetal(PollStem pollStem) '''
	<!-- pental -->
	<transform DEF='«pollStem.id»' translation='«pollStem.position.x» «pollStem.position.y» «pollStem.position.z»' 
	scale='2 2 2' 
	rotation='1 0 0 «(3.14/2)»' > 
		<transform rotation='0 0 1 «pollStem.angular»'>
			<transform rotation='0 1 0 «pollStem.currentPollStemAngle*-10»'>
				<transform rotation='1 0 0 «(3.14/2)»' scale="2 0.5 0.5" > 
				<Shape>				
					<Appearance>
						«pollStem.texture»
						«pollStem.color»
					</Appearance>
					<Cylinder height="0.1" radius="0.18"/>
				</Shape>
				</transform>				
			</transform>
		</transform>
	</transform>
	'''
}