package org.svis.generator.rd.m2t

import java.util.List
import org.eclipse.emf.common.util.EList
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.DiskSegment
import org.svis.xtext.rd.DiskSegmentInvocation
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.svis.generator.rd.m2m.RD2RD4Dynamix
import org.svis.xtext.rd.DiskVersion
import org.svis.xtext.rd.Version
import java.util.ArrayList
import org.apache.commons.logging.LogFactory
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.EvolutionRepresentation
import org.svis.generator.SettingsConfiguration.Variant

class RD2AFrame {
	val config = SettingsConfiguration.instance
	//TODO remove colors
	// TODO solve it with injection
	// @Inject extension FamixUtils
	RD2RD4Dynamix rd2rd4dynamix = new RD2RD4Dynamix
	val int offset = 10
	val int heightMultiplier = 60
	val multipleDisks = new ArrayList<Disk>
	val multipleDiskSegments = new ArrayList<DiskSegment>
	val log = LogFactory::getLog(class)
	
	def body(Resource resource) {
		log.info("RD2AFrame has started")
		var disks = EcoreUtil2::getAllContentsOfType(resource.contents.head, Disk)
		var diskSegments = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskSegment)
		val diskVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskVersion)
		for (root: resource.contents) {
			multipleDisks += EcoreUtil2::getAllContentsOfType(root, Disk)
			multipleDiskSegments += EcoreUtil2::getAllContentsOfType(root, DiskSegment)
		}
		var boolean withScale = false
		switch(config.evolutionRepresentation){
			case MULTIPLE_DYNAMIC_EVOLUTION,
			case MULTIPLE_TIME_LINE: {
				disks = multipleDisks
				if(config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION){
					diskSegments = multipleDiskSegments
					withScale = true
				}
			}	
			case DYNAMIC_EVOLUTION: {	
				diskVersions.forEach[v| if (v.scale < 0.001) {v.scale = 0.001}]	
				withScale = true
			}
			default: {}
		}
		val body = toX3DOMRD(disks,withScale) + ""
			//+ toDiskSegmentInvocation(diskSegmentInvocationsX3Dom(resource,diskSegments),diskSegments) 
		log.info("RD2AFrame has finished")
		return body	
	}
	
	def private toX3DOMRD(List<Disk> disks,boolean withScale) '''
		«FOR disk : disks»
			«toX3DOMDisk(disk,withScale)»
			«IF(disk.diskVersions.size != 0 && !(config.evolutionRepresentation == EvolutionRepresentation::DYNAMIC_EVOLUTION))»«/*toDiskVersions(disk.diskVersions,heightMultiplier,offset)*/»«ENDIF»
		«ENDFOR»
	'''

	def private toX3DOMDisk(Disk disk, boolean withScale) '''
		«IF disk.radius - config.RDRingWidth == 0»
			<a-circle id="«disk.id»"
			    position="«disk.position.x + " " + disk.position.y + " " + disk.position.z»"
				radius="«disk.radius»" 
				color="«disk.color »"
				shader="flat"
				buffer="true"
				flat-shading="true"
				depth-test="false"
				depth-write="false"
				segments="20">
				«toX3DOMSegment(disk.data)»
				«toX3DOMSegment(disk.methods)»
			</a-circle>
		«ELSE»
			<a-ring id="«disk.id»"
			    position="«disk.position.x + " " + disk.position.y + " " + disk.position.z»"
				radius-inner="«disk.radius - config.RDRingWidth»"
				radius-outer="«disk.radius»" 
				color="«disk.color »"
				shader="flat"
				buffer="true"
				flat-shading="true"
				depth-test="false"
				depth-write="false"
«««				segments-theta="20"
				segments-phi="1">
				«toX3DOMSegment(disk.data)»
				«toX3DOMSegment(disk.methods)»
			</a-ring>
		«ENDIF»
		'''
		/* Transform id='«disk.id»' translati0on='«disk.position.x + " " + disk.position.y + " " +
							disk.position.z»' rotation='1 0 0 1.57'>
				«IF (withScale == true)»
				<Transform id='«disk.id»_SCALE'
					scale='0.00001 0.00001 0.00001'>
				«ENDIF»
						<Shape id='«disk.id»__SHAPE'>
							<RectangularTorus id='«disk.id»__RECTANGULARTORUS'
								solid='true'
								height='«disk.height»'
								outerradius='«disk.radius»'
								innerradius='«disk.radius - RDSettings::RING_WIDTH»'></RectangularTorus>
							<Appearance>
									<Material id='«disk.id»__MATERIAL'
										diffuseColor='«disk.color»'
										transparency='«disk.transparency»'
									></Material>
							</Appearance>
							</Shape>
							«toX3DOMSegment(disk.data)»
							«toX3DOMSegment(disk.methods)»
						«IF(withScale == true)»
						</Transform>
						«ENDIF»		
				</Transform>
		'''*/
	
	def private toX3DOMSegment(EList<DiskSegment> segments) '''
		«FOR segment : segments»
			«IF segment.innerRadius == 0»
			<a-circle id="«segment.id»" 
				radius="«segment.outerRadius»" 
				color="«segment.color »"
				theta-start="«segment.anglePosition»"
				theta-length="«segment.angle»"
				shader="flat"
				buffer="true"
				flat-shading="true"
				depth-test="false"
				depth-write="false"
				segments="«(segment.angle/20).intValue+1»">
			</a-circle>
			«ELSE»
			<a-ring id="«segment.id»" 
				radius-inner="«segment.innerRadius»"
				radius-outer="«segment.outerRadius»" 
				color="«segment.color »"
				shader="flat"
				buffer="true"
				flat-shading="true"
				depth-test="false"
				depth-write="false"
				theta-start="«segment.anglePosition»"
				theta-length="«segment.angle»"
				segments-theta="«(segment.angle/20).intValue+1»"
				segments-phi="1">
			</a-ring>
			«ENDIF»
		«ENDFOR»
	'''
		/* 	<Transform id='«segment.id»'>
				<Transform rotation='0 1 0 «segment.anglePosition»'> 
				<Transform translation='0 « (segment.height- RDSettings::HEIGHT)/2.0 » 0'>
				<Transform id='«segment.id»_SCALE' 
				scale='1 1 1'
				>
				<Shape id='«segment.id»_SHAPE'>
				<RectangularTorus id='«segment.id»__RECTANGULARTORUS'
					solid='true'
					height='«segment.height»'
					angle='«segment.angle»'
					«IF(RDSettings::EVOLUTION_REPRESENTATION == EvolutionRepresentation::DYNAMIC_EVOLUTION
						|| RDSettings::EVOLUTION_REPRESENTATION == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION)»
					outerradius='0.0'
					«ELSE»
					outerradius='«segment.outerRadius»'
					«ENDIF»
					innerradius='«segment.innerRadius»'></RectangularTorus>
					<Appearance>
							<Material id='«segment.id»_MATERIAL'
								diffuseColor='«segment.color»'
								transparency='«segment.transparency»'
								></Material>
					</Appearance>
				</Shape>
				</Transform>
				</Transform>
				</Transform>
			</Transform>
		«ENDFOR»
	'''*/
	
	def private diskSegmentInvocationsX3Dom(Resource resource,List<DiskSegment> diskSegments){
		val diskSegmentInvocations = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskSegmentInvocation).clone.toList
		if(diskSegmentInvocations.size == 0 || config.variant == Variant::DYNAMIC) {
			return newLinkedList
		}
		return 	rd2rd4dynamix.prepareDiskSegmentInvocations(1,18,diskSegmentInvocations)

	}	

	
	/*def String toDiskSegmentInvocation(List<DiskSegmentInvocation> invocations,List<DiskSegment> diskSegments) '''
		«FOR invocation : invocations»
			«val segment = diskSegments.findFirst[ds| ds.invocations.contains(invocation)] »
				<Transform translation='«(segment.eContainer as Disk).position.x + " " + (segment.eContainer as Disk).position.y + " " +
							invocation.position.z»' rotation='1 0 0 1.57'>
				<Transform rotation='0 1 0 «segment.anglePosition»'> 
				<Transform translation='0 «invocation.length/2.0» 0' scale="1 «rd2rd4dynamix.toHeight(invocation, segment)» 1">	
					<Shape>
					<Group USE='«segment.id»__RECTANGULARTORUS'></Group>
										<Appearance>
												<Material diffuseColor='«config.RDMethodInvocationColorAsPercentage»'></Material>
										</Appearance>
					
					</Shape>
				</Transform>
				</Transform>
				</Transform>
		«ENDFOR»
	'''*/
		
	/*def private toDiskVersions(EList<DiskVersion> diskVersions,int heightMultiplier,int offset) '''
		«FOR diskVersion :	diskVersions.sortBy[v| v.level]»
		«IF (diskVersion.scale > 0) »
		<Transform translation='«(diskVersion.eContainer as Disk).position.x + " " + (diskVersion.eContainer as Disk).position.y + " " +
					(diskVersion.level*heightMultiplier + offset)»' rotation='1 0 0 1.57'>
			<Transform scale="«diskVersion.scale» 1 «diskVersion.scale»">	
					<Shape>
						<Group USE='«(diskVersion.eContainer as Disk).id»__RECTANGULARTORUS'></Group>
						<Appearance>
							<Group USE='«(diskVersion.eContainer as Disk).id»__MATERIAL'></Group>
						</Appearance>
					</Shape>
					«FOR method : (diskVersion.eContainer as Disk).methods»
					  «method.versions.findFirst[v| v.level == diskVersion.level].toDiskSegmentVersion()»
					«ENDFOR»
					«FOR data : (diskVersion.eContainer as Disk).data»
					  «data.versions.findFirst[v| v.level == diskVersion.level].toDiskSegmentVersion()»
					«ENDFOR»
			</Transform>
		</Transform>
		«ENDIF»
		«ENDFOR»
	'''*/

	/*def private toDiskSegmentVersion(Version version) '''
		«IF (version !== null && version.scale > 0) »
			<Transform rotation='0 1 0 «(version.eContainer as DiskSegment).anglePosition»'> 
				<Shape id='«(version.eContainer as DiskSegment).id»_MUUPSHAPE'>
					<RectangularTorus
						solid='true'
						height='«(version.eContainer as DiskSegment).height»'
						angle='«(version.eContainer as DiskSegment).angle »'
						outerradius='«(version.eContainer as DiskSegment).outerRadius * version.scale»'
						innerradius='«(version.eContainer as DiskSegment).innerRadius»'></RectangularTorus>
						<Appearance>
					<Material id='«(version.eContainer as DiskSegment).id»_MATERIAL'
						diffuseColor='«(version.eContainer as DiskSegment).color»'
						transparency='0'></Material>
						</Appearance>
					</Shape>
			</Transform>
		«ENDIF»
	'''*/
}