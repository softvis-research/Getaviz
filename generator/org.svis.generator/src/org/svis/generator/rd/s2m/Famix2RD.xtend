package org.svis.generator.rd.s2m

import java.util.List
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.generator.FamixUtils
import org.svis.xtext.famix.Document
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXEnumValue
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.Root
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.impl.RdFactoryImpl
import org.svis.xtext.famix.FAMIXStructure
import org.apache.commons.logging.LogFactory
import org.svis.generator.rd.RDSettings.MetricRepresentation
import org.svis.generator.SettingsConfiguration
import org.svis.generator.SettingsConfiguration.OutputFormat
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot

class Famix2RD extends WorkflowComponentWithModelSlot {
	val config = SettingsConfiguration.getInstance
	val log = LogFactory::getLog(class)
	val static diskFactory = new RdFactoryImpl()
	var Document famixDocument
	var org.svis.xtext.rd.Document diskDocument
	
	var List<FAMIXNamespace> rootPackages = newArrayList
	var List<FAMIXNamespace> subPackages = newArrayList
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXMethod> methods = newArrayList
	val List<FAMIXAttribute> attributes = newArrayList
	val List<FAMIXEnumValue> enumValues = newArrayList
	var List<FAMIXInheritance> inheritances = newArrayList
	private float heightMultiplicator = 5.0f
		
	// TODO solve it with injection
	// @Inject extension XtendUtils util
	extension FamixUtils util = new FamixUtils
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Famix2RD has started.")
		
		famixDocument = (ctx.get("famix") as Root).document
		val diskRoot = diskFactory.createRoot
		diskDocument = diskFactory.createDocument
		diskRoot.document = diskDocument
		
		rootPackages += famixDocument.elements.filter(FAMIXNamespace).filter[parentScope === null]
		subPackages += famixDocument.elements.filter(FAMIXNamespace).filter[parentScope !== null]
		structures += famixDocument.elements.filter(FAMIXStructure)
		methods += famixDocument.elements.filter(FAMIXMethod)
		attributes += famixDocument.elements.filter(FAMIXAttribute)
		enumValues += famixDocument.elements.filter(FAMIXEnumValue)
		inheritances += famixDocument.elements.filter(FAMIXInheritance)
		
		rootPackages.forEach[toDisk(1)]
		val diskList = newArrayList
		diskList += diskRoot
		
		// put diskroot into list (for writer)
		ctx.set("rdwriter", diskList)		

		// put diskroot into slot (for rd2rd)
		ctx.set("rd", diskList)
	
		log.info("Famix2RD has finished.")
	}

	def private Disk toDisk(FAMIXNamespace famixNamespace, int level) {
		val disk = diskFactory.createDisk
		disk.name = famixNamespace.name
		disk.id = famixNamespace.id
		disk.value = famixNamespace.value
		disk.fqn = famixNamespace.fqn
		disk.type = "FAMIX.Namespace"
		disk.level = level
		disk.ringWidth = config.RDRingWidth
		disk.height = config.RDHeight
		disk.transparency = config.RDNamespaceTransparency
		
		structures.filter[container.ref === famixNamespace]
			.forEach[disk.disks += toDisk(level + 1)]
			
		subPackages.filter[parentScope.ref === famixNamespace]
			.forEach[disk.disks += toDisk(level + 1)]

		diskDocument.disks += disk
		
		return disk
	}

	def private Disk toDisk(FAMIXStructure el, int level) {
		val disk = diskFactory.createDisk
		disk.name = el.name
		disk.id = el.id
		disk.value = el.value
		disk.fqn = el.fqn
		disk.type = el.typeString
		disk.level = level
		disk.ringWidth = config.RDRingWidth
		disk.height = config.RDHeight
		if(config.outputFormat == OutputFormat::AFrame) {
			disk.color = config.RDClassColorHex
		} else {
			disk.color = config.RDClassColorAsPercentage
		}
		disk.transparency = config.RDClassTransparency
		
		// references
		inheritances.filter[subclass.ref === el].forEach[i|
			val inheritance = diskFactory.createReference
			inheritance.type = "Inheritance"
			inheritance.name = i.superclass.ref.name
			inheritance.fqn = i.superclass.ref.fqn
			disk.references += inheritance
		]
		if(config.methodTypeMode){
			methods.filter[parentType.ref === el].forEach[
				switch (methodType){
					case CONSTRUCTOR:disk.methods += toDiskSegment()
					case UNKNOWN: disk.disks += toDisk()
					default: {}
				}	
			]
			attributes.filter[parentType.ref === el].forEach[disk.disks += toDisk()]
			enumValues.filter[parentEnum.ref === el].forEach[disk.disks += toDisk]	
		} else {	
			if (config.dataDisks){
				attributes.filter[parentType.ref === el].forEach[disk.disks += toDisk]
				enumValues.filter[parentEnum.ref === el].forEach[disk.disks += toDisk]
			} else {
				attributes.filter[parentType.ref === el].forEach[disk.data += toDiskSegment]
				enumValues.filter[parentEnum.ref === el].forEach[disk.data += toDiskSegment]
			}
			if(config.methodDisks){
				methods.filter[parentType.ref === el].forEach[disk.disks += toDisk()]
			} else {
				methods.filter[parentType.ref === el].forEach[disk.methods += toDiskSegment()]
			}
		} 
			structures.filter[container.ref === el].forEach[disk.disks += toDisk(level + 1)]
		
		if(!(config.metricRepresentation === MetricRepresentation::NONE)) {	
			var sumCompl = 0
			for (m : methods.filter[parentType.ref === el]) {
				sumCompl += m.cyclomaticComplexity
			} 
			log.debug("###Class: " + disk.fqn + "	complexity: " + sumCompl)
		}	
		return disk
	}
	
	def private toDiskSegment(FAMIXMethod method) {
		val diskSegment = diskFactory.createDiskSegment
		diskSegment.name = method.name
		diskSegment.id = method.id
		diskSegment.value = method.value
		diskSegment.fqn = method.fqn
		diskSegment.signature = method.signature
		diskSegment.frequency = 0
		diskSegment.luminance = 0
		diskSegment.height = config.RDHeight
		if(config.outputFormat == OutputFormat::AFrame) {
			diskSegment.color = config.RDMethodColorHex
		} else {
			diskSegment.color = config.RDMethodColorAsPercentage
		}
		diskSegment.transparency = config.RDMethodTransparency
		
		switch(config.metricRepresentation)  {
			case HEIGHT: {
				diskSegment.height = ((method.cyclomaticComplexity - 1.0 )* heightMultiplicator) + 1.0
			}
			case FREQUENCY: {
				diskSegment.frequency = 10000/((method.cyclomaticComplexity - 1.0 )* heightMultiplicator + 1.0)
				if(diskSegment.frequency == 10000) {
					diskSegment.frequency = 0			
				}
			}
			case LUMINANCE: {
				diskSegment.luminance = ((method.cyclomaticComplexity - 1.0 )* heightMultiplicator) + 1.0
				if(diskSegment.luminance == 1) {
					diskSegment.luminance = 0
				}
			}
			default: {}
		}		
		if (method.numberOfStatements <= config.RDMinArea) {
			diskSegment.size = config.RDMinArea
		} else {
			diskSegment.size = method.numberOfStatements
		}
		return diskSegment
	}
	
	def private Disk toDisk(FAMIXMethod method){
		val disk = diskFactory.createDisk
		disk.name = method.name
		disk.id = method.id
		disk.value= method.value
		disk.fqn= method.fqn
		disk.signature = method.signature
		disk.type = "FAMIX.Method"
		disk.ringWidth = config.RDRingWidthMD 
		disk.height = config.RDHeight
		disk.transparency = config.RDMethodTransparency
		if(config.outputFormat == OutputFormat::AFrame) {
			disk.color = config.RDMethodColorHex
		} else {
			disk.color = 153/255.0 + " " + 0/255.0 + " " + 0/255.0
		}
		disk.methods += toDiskSegment(method)	
		
		return disk
	}
	
	def private toDiskSegment(FAMIXAttribute attribute) {
		val diskSegment = diskFactory.createDiskSegment
		diskSegment.name = attribute.name
		diskSegment.id = attribute.id
		diskSegment.value = attribute.value
		diskSegment.fqn = attribute.fqn
		diskSegment.size = 1 //attribute.declaredType.ref.attributeSize
		diskSegment.height = config.RDHeight
		if(config.outputFormat == OutputFormat::AFrame) {
			diskSegment.color = config.RDDataColorHex
		} else {
			diskSegment.color = config.RDDataColorAsPercentage
		}
		diskSegment.transparency = config.RDDataTransparency
		
		return diskSegment
	}
	
	def private Disk toDisk(FAMIXAttribute attribute) {
		val disk = diskFactory.createDisk
		disk.name = attribute.name
		disk.id = attribute.id
		disk.value = attribute.value
		disk.fqn= attribute.fqn
		disk.type = "FAMIX.Attribute"
		disk.ringWidth = config.RDRingWidthAD
		disk.height = config.RDHeight
		disk.transparency = config.RDDataTransparency
		if(config.outputFormat == OutputFormat::AFrame) {
			disk.color = config.RDDataColorHex
		} else {
			disk.color = 153/255.0 + " " + 0/255.0 + " " + 0/255.0
		}
		for(getterOrSetter : attribute.getterSetter){
			disk.methods += toDiskSegment(getterOrSetter.ref as FAMIXMethod)	
		}
		disk.data += toDiskSegment(attribute)
		
		return disk	
	}

	def private toDiskSegment(FAMIXEnumValue enumValue) {
		val diskSegment = diskFactory.createDiskSegment
		diskSegment.name = enumValue.name
		diskSegment.id = enumValue.id
		diskSegment.value = enumValue.value
		diskSegment.fqn = enumValue.fqn
		diskSegment.size = 1 // TODO size
		diskSegment.height = config.RDHeight
		if(config.outputFormat == OutputFormat::AFrame) {
			diskSegment.color = config.RDDataColorHex
		} else {
			diskSegment.color = config.RDDataColorAsPercentage
		}
		diskSegment.transparency = config.RDDataTransparency
	
		return diskSegment
	}
	
	def private Disk toDisk(FAMIXEnumValue enumValue) {
		val disk = diskFactory.createDisk
		disk.name = enumValue.name
		disk.id = enumValue.id
		disk.value = enumValue.value
		disk.fqn= enumValue.fqn
		disk.type = "FAMIX.EnumValue"
		disk.ringWidth = config.RDRingWidthAD
		disk.height = config.RDHeight
		disk.transparency = config.RDDataTransparency
		if(config.outputFormat == OutputFormat::AFrame) {
			disk.color = config.RDDataColorHex
		} else {
			disk.color = 153/255.0 + " " + 0/255.0 + " " + 0/255.0 
		}
		
		disk.data += toDiskSegment(enumValue)
		return disk	
	}
}