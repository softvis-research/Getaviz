package org.svis.generator.plant.m2t;

import java.util.List
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.IGenerator2
import org.svis.generator.X3DUtils
import org.svis.xtext.plant.Entity
import javax.inject.Inject
import org.svis.xtext.plant.Stem
import org.svis.xtext.plant.Petal
import org.svis.xtext.plant.Junction
import org.svis.xtext.plant.PollStem
import org.svis.xtext.plant.Area
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot

class Plant2X3D extends WorkflowComponentWithModelSlot implements IGenerator2 {
	
	
	@Inject extension X3DUtils util
	val log = LogFactory::getLog(getClass)
	
	override beforeGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Plant2X3D has started.")
	}

	override afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		log.info("Plant2X3D has finished.")
	}
	
	
	override doGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext ig) {
		val entities = EcoreUtil2::getAllContentsOfType(input.contents.head, Entity)
		fsa.generateFile("model.x3d", toX3DHead + entities.toX3DModel + toX3DTail)
	}
	
	// transform logic
	def String toX3DModel(List<Entity> entities) '''
		«FOR entity: entities»
			«IF entity.type == "FAMIX.Namespace"»
				«PlantShapeManager.AREA.getShape(entity.shapeID, Area.cast(entity), entity)»
			«ENDIF»
			«IF entity.type == "FAMIX.Class"»				
				«// Create all stems:
				if(entity instanceof Stem){
					PlantShapeManager.STEM.getShape(Stem.cast(entity).shapeID, Stem.cast(entity), null);
				}»
				«// Create all petals:
				if(entity instanceof Stem){		
					petalIt(Stem.cast(entity).petals, Stem.cast(entity))
				}»
				«// Create poll stems:				
				if(entity instanceof Stem){
					pollstemIt(Stem.cast(entity).pollstems, Stem.cast(entity))
				}»
				«// Create all junctions:				
				if(entity instanceof Stem){
					junctionIt(Stem.cast(entity).junctions, Stem.cast(entity))
				}»
				«// Create all junction petals:				
				if(entity instanceof Stem){
					junctionPetalIt(Stem.cast(entity).junctions, Stem.cast(entity))
				}»
				«// Create all junction poll stems:				
				if(entity instanceof Stem){
					junctionPollStemIt(Stem.cast(entity).junctions, entity)
				}»
			«ENDIF»
			«IF entity.type == "FAMIX.ParameterizableClass"»
				«PlantShapeManager.STEM.getShape(Stem.cast(entity).shapeID, Stem.cast(entity), null)»
			«ENDIF»
		«ENDFOR»
	'''
	
	/**
	 * Create petals from petal list.
	 */
	def String petalIt(List<Petal> petals, Stem parent) '''
		«FOR p : petals»		
			«if(parent !== null){
				PlantShapeManager.PETAL.getShape(p.shapeID, p, parent);
			}»
		«ENDFOR»
	'''
	/**
	 * Create petals from junction list.
	 */
	def String junctionPetalIt(List<Junction> junctions, Stem parent) '''
		«FOR j : junctions»
			«FOR p : j.petals»		
			«if(parent !== null){
				PlantShapeManager.PETAL.getShape(p.shapeID, p, parent);
			}»
			«ENDFOR»
		«ENDFOR»
	'''
	/**
	 * Create poll stems from junction list.
	 */
	def String junctionPollStemIt(List<Junction> junctions, Entity parent) '''
		«FOR j : junctions»
			«FOR p : j.pollstems»		
			«PlantShapeManager.POLLSTEM.getShape(p.shapeID, p, parent)»
			«ENDFOR»
		«ENDFOR»
	'''
	/**
	 * Create junction stem from junction list.
	 */
	def String junctionIt(List<Junction> junctions, Stem parent) '''
		«FOR j : junctions»
			«if(parent !== null){PlantShapeManager.JUNCTION.getShape(j.shapeID, j, parent)}»
		«ENDFOR»	
	'''
	/**
	 * Create pollStems from pollStem list.
	 */
	def String pollstemIt(List<PollStem> pollStem, Stem parent) '''
		«FOR p : pollStem»
				«if(parent !== null){
					PlantShapeManager.POLLSTEM.getShape(p.shapeID, p, parent)
				}»
		«ENDFOR»	
	'''
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
	}
	
}