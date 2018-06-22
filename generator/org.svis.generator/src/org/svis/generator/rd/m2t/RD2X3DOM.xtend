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
import org.svis.generator.rd.RDSettings.Variant
import java.util.ArrayList
import org.apache.commons.logging.LogFactory
import org.svis.generator.rd.RDSettings.EvolutionRepresentation
import org.svis.generator.SettingsConfiguration

class RD2X3DOM {
	val config = new SettingsConfiguration
	// TODO remove colors
	// TODO solve it with injection
	// @Inject extension FamixUtils
	RD2RD4Dynamix rd2rd4dynamix = new RD2RD4Dynamix
	val int offset = 10
	val int heightMultiplier = 60
	val multipleDisks = new ArrayList<Disk>
	val multipleDiskSegments = new ArrayList<DiskSegment>
	val log = LogFactory::getLog(class)

	def toX3DOMBody(Resource resource) {
		log.info("RD2X3DOM has started")
		var disks = EcoreUtil2::getAllContentsOfType(resource.contents.head, Disk)
		var diskSegments = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskSegment)
		val diskVersions = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskVersion)
		for (root : resource.contents) {
			multipleDisks += EcoreUtil2::getAllContentsOfType(root, Disk)
			multipleDiskSegments += EcoreUtil2::getAllContentsOfType(root, DiskSegment)
		}
		var boolean withScale = false
		switch (config.evolutionRepresentation) {
			case MULTIPLE_DYNAMIC_EVOLUTION,
			case MULTIPLE_TIME_LINE: {
				disks = multipleDisks
				if (config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION) {
					diskSegments = multipleDiskSegments
					withScale = true
				}
			}
			case DYNAMIC_EVOLUTION: {
				diskVersions.forEach[v|if (v.scale < 0.001) {v.scale = 0.001}]
				withScale = true
			}
			default: {
			}
		}
		val body = toX3DOMRD(disks, withScale) +
			toDiskSegmentInvocation(diskSegmentInvocationsX3Dom(resource, diskSegments), diskSegments)
		log.info("RD2X3DOM has finished")
		return body
	}

	def private toX3DOMRD(List<Disk> disks, boolean withScale) '''
		«FOR disk : disks»
			«toX3DOMDisk(disk,withScale)»
			«IF(disk.diskVersions.size != 0 && !(config.evolutionRepresentation == EvolutionRepresentation::DYNAMIC_EVOLUTION))»«toDiskVersions(disk.diskVersions,heightMultiplier,offset)»«ENDIF»
		«ENDFOR»
	'''

	def private toX3DOMDisk(Disk disk, boolean withScale) '''
		<Transform id='«disk.id»' translation='«disk.position.x + " " + disk.position.y + " " +
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
						innerradius='«disk.radius - config.RDRingWidth»'></RectangularTorus>
					<Appearance>
							<Material id='«disk.id»__MATERIAL'
								diffuseColor='«disk.color»'
								transparency='«disk.transparency»'
							></Material>
					</Appearance>
					</Shape>
					«toX3DOMSegment(disk.data)»
					«toX3DOMSegment(disk.methods)»
						«IF (withScale == true)»
							</Transform>
						«ENDIF»		
				</Transform>
	'''

	def private toX3DOMSegment(EList<DiskSegment> segments) '''
		«FOR segment : segments»
			<Transform id='«segment.id»'>
				<Transform rotation='0 1 0 «segment.anglePosition»'> 
				<Transform translation='0 « (segment.height- config.RDHeight)/2.0 » 0'>
				<Transform id='«segment.id»_SCALE' 
				scale='1 1 1'
				>
				<Shape id='«segment.id»_SHAPE'>
				<RectangularTorus id='«segment.id»__RECTANGULARTORUS'
					solid='true'
					height='«segment.height»'
					angle='«segment.angle»'
					«IF(config.evolutionRepresentation == EvolutionRepresentation::DYNAMIC_EVOLUTION
						|| config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION)»
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
	'''

	def private diskSegmentInvocationsX3Dom(Resource resource, List<DiskSegment> diskSegments) {
		val diskSegmentInvocations = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskSegmentInvocation).
			clone.toList
		if (diskSegmentInvocations.size == 0 || config.variant == Variant::DYNAMIC) {
			return newLinkedList
		}
		return rd2rd4dynamix.prepareDiskSegmentInvocations(1, 18, diskSegmentInvocations)

	}

	def String toDiskSegmentInvocation(List<DiskSegmentInvocation> invocations, List<DiskSegment> diskSegments) '''
		«FOR invocation : invocations»
			«val segment = diskSegments.findFirst[ds| ds.invocations.contains(invocation)] »
				<Transform translation='«(segment.eContainer as Disk).position.x + " " + (segment.eContainer as Disk).position.y + " " +
							invocation.position.z»' rotation='1 0 0 1.57'>
				<Transform rotation='0 1 0 «segment.anglePosition»'> 
				<Transform translation='0 «invocation.length/2.0» 0' scale="1 «rd2rd4dynamix.toHeight(invocation, segment)» 1">	
					<Shape>
					<Group USE='«segment.id»__RECTANGULARTORUS'></Group>
										<Appearance>
												<Material diffuseColor='«config.RDMethodInvocationColorPercentage»'></Material>
										</Appearance>
					
					</Shape>
				</Transform>
				</Transform>
				</Transform>
		«ENDFOR»
	'''

	def private toDiskVersions(EList<DiskVersion> diskVersions, int heightMultiplier, int offset) '''
		«FOR diskVersion : diskVersions.sortBy[v| v.level]»
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
	'''

	def private toDiskSegmentVersion(Version version) '''
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
	'''
}
