package org.svis.generator.rd.m2t

import java.util.List
import org.eclipse.emf.common.util.EList
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.DiskSegment
import org.svis.xtext.rd.DiskInstance
import org.svis.xtext.rd.DiskSegmentInvocation
import org.apache.commons.logging.LogFactory
import org.svis.xtext.rd.Version
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import java.util.ArrayList
import org.svis.generator.famix.Famix2Famix
import org.svis.generator.rd.m2m.RD2RD4Dynamix
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.EvolutionRepresentation

class RD2X3D {
	val config = SettingsConfiguration.instance
	RD2RD4Dynamix rd2rd4dy = new RD2RD4Dynamix
	val log = LogFactory::getLog(class)
	val multipleDisks = new ArrayList<Disk>
	val multipleDiskSegments = new ArrayList<DiskSegment>
	Famix2Famix famix = new Famix2Famix

	def toX3DBody(Resource resource) {
		log.info("RD2X3D has started")
		var disks = EcoreUtil2::getAllContentsOfType(resource.contents.head, Disk)
		for (root : resource.contents) {
			multipleDisks += EcoreUtil2::getAllContentsOfType(root, Disk)
			multipleDiskSegments += EcoreUtil2::getAllContentsOfType(root, DiskSegment)
		}
		if (config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_TIME_LINE) {
			disks = multipleDisks
		}
		val diskSegmentInvocations = EcoreUtil2::getAllContentsOfType(resource.contents.head, DiskSegmentInvocation).
			clone.toList
		if (!diskSegmentInvocations.isEmpty) {
			rd2rd4dy.prepareDiskSegmentInvocations(1, 18, diskSegmentInvocations)
		}
		var body = toRD(disks)
		log.info("RD2X3D has finished")
		return body
	}

	def String toRD(List<Disk> disks) '''
		«FOR disk : disks»
		  	«IF(config.showHistories)»
				«toDisk(disk)»
				«toSegment(disk.data)»
				«toSegment(disk.methods)»
				«FOR segment : disk.methods»
					«segment.invocations.toSegmentInvocation(segment)»
				«ENDFOR»
			«ENDIF»
			«toDiskVersions(disk)»
		«ENDFOR»
	'''

	def String toDisk(Disk disk) '''
		<Transform translation='«disk.position.x + " " + disk.position.y + " " + disk.position.z»' 
			rotation='0 0 1 1.57' 
			scale='1 1 «disk.height»'>
		<Transform DEF='«disk.id»'>
			<Shape>
				<Extrusion
					convex='true'
					solid='true'
					crossSection='«disk.crossSection»'
					spine='«disk.spine»'
					creaseAngle='1'
					beginCap='true'
					endCap='true'></Extrusion>
				<Appearance>
						<Material
							diffuseColor='«disk.color»'
							transparency='«disk.transparency»'
						></Material>
				</Appearance>
			</Shape>
		</Transform>
		</Transform>
	'''

	def String toSegment(EList<DiskSegment> segments) '''
		«FOR segment : segments»
			<Transform  translation='«(segment.
			eContainer as Disk).position.x + " " + (segment.eContainer as Disk).position.y + " " +
			(segment.eContainer as Disk).position.z»' rotation='0 0 1 1.57'>
			<Transform DEF='«segment.id»'>	
				<Shape>
					<Extrusion
						convex='true'
						solid='true'
						crossSection='«segment.crossSection»'
						spine='«segment.spine»'
						creaseAngle='1'
						beginCap='true'
						endCap='true'></Extrusion>
					<Appearance>
							<Material
								diffuseColor='«segment.color»'
								transparency='«segment.transparency»'
							></Material>
					</Appearance>
				</Shape>
			</Transform>	
			</Transform>
		«ENDFOR»
	'''

	def String toInstance(List<DiskInstance> instances) '''
		«FOR instance : instances»
			«try {
			if(instance.position !== null){'''
				<Transform DEF='«famix.createID(instance.fqn)»'
					translation='«instance.position.x + " " + instance.position.y + " " + instance.position.z»' 
					rotation='0 0 1 1.57' scale='1 1 «instance.length»'>
					<Shape>
						<Extrusion
							convex='true'
							solid='true'
							crossSection='«(instance.eContainer as Disk).crossSection»'
							spine='«(instance.eContainer as Disk).spine»'
							creaseAngle='1'
							beginCap='true'
							endCap='true'/>
						<Appearance>
							<Material 
								diffuseColor='«(instance.eContainer as Disk).color»'
								transparency='0'/>
						</Appearance>
					</Shape>
				</Transform>'''}
			} catch(Exception e) {}»	
				«instance.invocations.toMethodInvocation(instance)»
		«ENDFOR»
	'''

	def String toMethodInvocation(EList<DiskSegmentInvocation> invocations, DiskInstance instance) {
		'''
			«FOR invocation : invocations»
				<Transform DEF='«famix.createID(invocation.fqn)»' 
					translation='«invocation.position.x + " " + invocation.position.y + " " +
				invocation.position.z»' rotation='0 0 1 1.57'  scale='1 1 «invocation.length»'>
				<Shape>
					<Extrusion
						convex='true'
						solid='true'
						crossSection='«(instance.eContainer as Disk).crossSection»'
						spine='«(instance.eContainer as Disk).spine»'
						creaseAngle='1'
						beginCap='true'
						endCap='true'/>
					<Appearance>
						<Material
							diffuseColor='«(instance.eContainer as Disk).color»'
							transparency='0'/>
					</Appearance>
				</Shape>
			</Transform>
		«ENDFOR»
	'''	}
	

	def String toSegmentInvocation(EList<DiskSegmentInvocation> invocations, DiskSegment segment) '''		
		«FOR invocation : invocations»
			<Transform DEF='«famix.createID(invocation.fqn)»' 
				translation='«invocation.position.x + " " + invocation.position.y + " " + invocation.position.z»' 
				rotation='0 0 1 1.57' scale='1 1 «invocation.length»'>
				<Shape>
					<Extrusion
						convex='true'
						solid='true'
						crossSection='«(invocation.eContainer as DiskSegment).crossSection»'
						spine='«(invocation.eContainer as DiskSegment).spine»'
						creaseAngle='1'
						beginCap='true'
						endCap='true'/>
					<Appearance>
						<Material
							diffuseColor='«config.RDMethodInvocationColorAsPercentage»'
							transparency='0'/>
					</Appearance>
				</Shape>
			</Transform>
		«ENDFOR»
	''' 

	def String toDiskVersions(Disk disk) '''
		«FOR version : disk.diskVersions.sortBy[level]»
			«IF (version.scale > 0)»
				<Transform translation='«disk.position.x + " " + disk.position.y + " " + (version.level*60 + 10)»' rotation='0 0 1 1.57'>
				<Transform scale='«version.scale» «version.scale» 1'>
					<Transform DEF='«version.id»'>
						<Shape>
							<Extrusion
								convex='true'
								solid='true'
								crossSection='«disk.crossSection»'
								spine='«disk.spine»'
								creaseAngle='1'
								beginCap='true'
								endCap='true'></Extrusion>
							<Appearance>
									<Material
										diffuseColor='«version.color»'
										transparency='«disk.transparency»'
									></Material>
							</Appearance>
						</Shape>
					</Transform>	
				«FOR method : disk.methods»
				  «method.versions.findFirst[v| v.level == version.level].toVersions()»
				«ENDFOR»
				«FOR data : disk.data»
				  «data.versions.findFirst[v| v.level == version.level].toVersions()»
				«ENDFOR»
				</Transform>
				</Transform>
			«ENDIF»
		«ENDFOR»
	'''

	def private toVersions(Version version) '''
		«IF (version !== null && version.scale > 0) »
			<Transform rotation='0 1 0 «(version.eContainer as DiskSegment).anglePosition»'> 
			<Transform DEF='«version.id»'>
				<Shape>
					<Extrusion
						convex='true'
						solid='true'
						crossSection='«(version.eContainer as DiskSegment).crossSection»'
						spine='«(version.eContainer as DiskSegment).spine»'
						creaseAngle='1'
						beginCap='true'
						endCap='true'/>
						<Appearance>
							<Material
							diffuseColor='«(version.eContainer as DiskSegment).color»'
							transparency='0'></Material>
						</Appearance>
					</Shape>
			</Transform>	
			</Transform>
		«ENDIF»
	'''
}
