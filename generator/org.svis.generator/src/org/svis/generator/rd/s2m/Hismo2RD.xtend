package org.svis.generator.rd.s2m

import java.util.List
import java.util.Set
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.hismo.HISMOAttributeHistory
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMOMethodHistory
import org.svis.xtext.hismo.HISMONamespaceHistory
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.xtext.hismo.HismoDocument
import org.svis.xtext.hismo.HismoRoot
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.DiskSegment
import org.svis.xtext.rd.Document
import org.svis.xtext.rd.impl.RdFactoryImpl
import org.svis.xtext.hismo.HISMOMethodVersion
import org.svis.xtext.hismo.HISMOAttributeVersion
import org.svis.xtext.rd.DiskVersion
import org.svis.xtext.rd.Version
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.beanutils.BeanComparator
import org.svis.generator.FamixUtils
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXEnum
import org.svis.xtext.famix.FAMIXEnumValue
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXParameterizableClass
import org.svis.xtext.famix.FAMIXAnnotationType
import org.svis.xtext.famix.FAMIXStructure
import org.svis.generator.famix.Famix2Famix
import org.svis.generator.rd.RDSettings.EvolutionRepresentation
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.svis.xtext.famix.FAMIXFileAnchor
import java.util.ArrayList
import org.svis.generator.SettingsConfiguration

class Hismo2RD extends WorkflowComponentWithModelSlot {
	val config = new SettingsConfiguration
	val static diskFactory = new RdFactoryImpl()
	var HismoDocument hismoDocument
	var Document diskDocument
	val log = LogFactory::getLog(class)
	val famix = new Famix2Famix
	extension FamixUtils util = new FamixUtils
	val nameComparator = new BeanComparator("name")
	val Set<HISMONamespaceHistory> hismoPackages = newTreeSet(nameComparator)
	val Set<HISMONamespaceVersion> hismoPackageVersions = newTreeSet(nameComparator)
	val Set<HISMOClassVersion> hismoClassVersions = newTreeSet(nameComparator)
	val Set<HISMOMethodVersion> hismoMethodVersions = newTreeSet(nameComparator)
	val Set<HISMOAttributeVersion> hismoAttributeVersions = newTreeSet(nameComparator)
	val Set<HISMONamespaceHistory> hismoRootPackages = newTreeSet(nameComparator)
	val Set<HISMONamespaceHistory> hismoSubPackages = newTreeSet(nameComparator)
	val Set<HISMOClassHistory> hismoClasses = newTreeSet(nameComparator)
	val Set<HISMOMethodHistory> hismoMethods = newTreeSet(nameComparator)
	val Set<HISMOAttributeHistory> hismoAttributes = newTreeSet(nameComparator)
	val List<FAMIXNamespace> rootPackages = newArrayList
	val List<FAMIXNamespace> subPackages = newArrayList
	val List<FAMIXStructure> structures = newArrayList
	val List<FAMIXMethod> methods = newArrayList
	val List<FAMIXAttribute> attributes = newArrayList
	val List<FAMIXEnumValue> enumValues = newArrayList
	val List<FAMIXInheritance> inheritances = newArrayList
	val Set<String> timestamps = newHashSet;
	val List<String> sortedtimestamps = newLinkedList;
	// TODO solve it with injection
	// @Inject extension XtendUtils util
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Hismo2RD has started")
		var hismoRoot = ctx.get("hismo") as List<HismoRoot>
		hismoDocument = hismoRoot.head.hismoDocument
		
		hismoClassVersions += hismoDocument.elements.filter(HISMOClassVersion)
		hismoPackageVersions += hismoDocument.elements.filter(HISMONamespaceVersion)
		hismoMethodVersions += hismoDocument.elements.filter(HISMOMethodVersion)
		hismoAttributeVersions += hismoDocument.elements.filter(HISMOAttributeVersion)
		
		hismoAttributeVersions.forEach[v| timestamps += v.timestamp]
		hismoMethodVersions.forEach[v| timestamps += v.timestamp]
		hismoClassVersions.forEach[v| timestamps += v.timestamp]
		hismoPackageVersions.forEach[v| timestamps += v.timestamp]
		hismoMethods.toSet
				
		sortedtimestamps.addAll(timestamps.toList.sort)
		val diskList = newArrayList
		if(config.evolutionRepresentation == EvolutionRepresentation::TIME_LINE
			|| config.evolutionRepresentation == EvolutionRepresentation::DYNAMIC_EVOLUTION) {
			val diskRoot = createRoot
			fillLists()
			hismoPackages.forEach[getPackages(it)]
			hismoRootPackages.forEach[toDisk(it,1)]	
			removeUnnecessaryFamixElements(hismoRoot)
			diskList += diskRoot
		} else {
			for (timestamp: sortedtimestamps) {
				val diskRoot = createRoot
				fillLists(timestamp)
				rootPackages.forEach[toDisk(1, timestamp)]
				removeUnnecessaryHismoElements(hismoRoot)
				diskList += diskRoot		
			}	
		}
		val resource = new ResourceImpl()
		resource.contents += hismoRoot
		ctx.set("metadata", resource)	
		// put diskroot into list (for writer)
		ctx.set("rdwriter", diskList)		
		// put diskroot into slot (for rd2rd)
		ctx.set("rd", diskList)
			
		log.info("Hismo2RD has finished.")
	}
	
	def createRoot() {
		val diskRoot = diskFactory.createRoot
		diskDocument = diskFactory.createDocument
		diskRoot.document = diskDocument
		return diskRoot
	}	
	
	def removeUnnecessaryFamixElements(List<HismoRoot> hismoroot){
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(FAMIXNamespace))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(FAMIXClass))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(FAMIXMethod))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(FAMIXAttribute))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(FAMIXFileAnchor))
	}
	
	def removeUnnecessaryHismoElements(List<HismoRoot> hismoroot){
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMONamespaceVersion))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMOClassVersion))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMOMethodVersion))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMOAttributeVersion))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMONamespaceHistory))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMOClassHistory))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMOMethodHistory))
		hismoDocument.elements.removeAll(hismoDocument.elements.filter(HISMOAttributeHistory))
	}
	
	def fillLists() {
		hismoClasses += hismoDocument.elements.filter(HISMOClassHistory)
		hismoPackages += hismoDocument.elements.filter(HISMONamespaceHistory)
		hismoMethods += hismoDocument.elements.filter(HISMOMethodHistory)
		hismoAttributes += hismoDocument.elements.filter(HISMOAttributeHistory)
	}
	
	def fillLists(String timestamp) {
		rootPackages.clear
		rootPackages += hismoDocument.elements
			.filter(FAMIXNamespace)
			.filter[parentScope === null]
			.filter[pack|hismoPackageVersions.exists[hpv|
				hpv.timestamp == timestamp && hpv.versionEntity.ref === pack  
			]]
		subPackages.clear
		subPackages += hismoDocument.elements
			.filter(FAMIXNamespace)
			.filter[parentScope !== null]
			.filter[pack|hismoPackageVersions.exists[hpv|
				hpv.timestamp == timestamp && hpv.versionEntity.ref === pack  
			]]
		structures.clear
		structures += hismoDocument.elements
			.filter(FAMIXStructure)
			.filter[class|hismoClassVersions.exists[hcv|
				hcv.timestamp == timestamp && hcv.versionEntity.ref === class  
			]]
		methods.clear				
		methods += hismoDocument.elements
			.filter(FAMIXMethod)
			.filter[method|hismoMethodVersions.exists[hmv|
				hmv.timestamp == timestamp && hmv.versionEntity.ref === method  
			]]
		attributes.clear				
		attributes += hismoDocument.elements
			.filter(FAMIXAttribute)
			.filter[attribute|hismoAttributeVersions.exists[hav|
				hav.timestamp == timestamp && hav.versionEntity.ref === attribute  
			]]
	}
	
	def private void getPackages(HISMONamespaceHistory namespace) {
		if (namespace.containingNamespaceHistory === null) {
			val SubPacks = new ArrayList<HISMONamespaceHistory>
			hismoPackages.forEach[nh|
				if(nh.containingNamespaceHistory !== null){
					if(nh.containingNamespaceHistory.ref === namespace){
						SubPacks += nh
					}
				}
			]
			if (!SubPacks.empty || !namespace.classHistories.empty) {
          		hismoRootPackages += namespace 
      		} 
		} else {
			hismoSubPackages += namespace
		}
	}

	def private Disk create disk: diskFactory.createDisk toDisk(HISMONamespaceHistory hismoNamespace, int level) {
		disk.name = hismoNamespace.name
		disk.value = hismoNamespace.name//hismoNamespace.value
		disk.fqn = hismoNamespace.qualifiedName
		disk.type = "FAMIX.Namespace"
		disk.level = level
		disk.ringWidth = config.RDRingWidth
		disk.id = famix.createID(disk.fqn)
		disk.height = config.RDHeight
		disk.transparency = config.RDNamespaceTransparency
		
		//hismoNamespace.namespaceVersions.forEach[v| disk.diskVersions.add((v.ref as HISMONamespaceVersion).toDiskVersion())]
		
		hismoClasses.filter[containingNamespaceHistory.ref === hismoNamespace].filterNull.forEach[c|disk.disks += c.toDisk(level + 1)]

		hismoSubPackages.filter[containingNamespaceHistory.ref === hismoNamespace].filterNull.forEach[disk.disks += toDisk(it,level + 1)]
	 	val versions = hismoPackageVersions.filter[
			parentHistory.ref === hismoNamespace
		].filterNull
		
		versions.forEach[v|disk.diskVersions += toDiskVersion(v)]
			diskDocument.disks += disk
	}
	
	def private DiskVersion create diskVersion: diskFactory.createDiskVersion toDiskVersion(HISMONamespaceVersion version) {
		diskVersion.height = sortedtimestamps.indexOf(version.timestamp) * config.RDHeightBoost
		diskVersion.level =sortedtimestamps.indexOf(version.timestamp) 
		diskVersion.commitId = version.commitId
		diskVersion.author = version.author
		diskVersion.timestamp = version.timestamp
		diskVersion.name = version.name
	}

	def private Disk create disk:	diskFactory.createDisk toDisk(HISMOClassHistory hismoClass, int level) {
		disk.name = hismoClass.name
		disk.value = hismoClass.name
		disk.fqn = hismoClass.qualifiedName
		disk.type = "FAMIX.Class"
		disk.level = level
		disk.ringWidth = config.RDRingWidth
		disk.id = famix.createID(disk.fqn)
		disk.height = config.RDHeight
		disk.color = config.RDClassColorPercentage
		disk.transparency = config.RDClassTransparency
		
		//TODO any side effects?
		//disk.setLoc(famixClass.loc)

		// references
		/* TODO: implement
		for (i : hismoDocument.elements.filter(typeof(FAMIXInheritance)).filter[subclass.ref.equals(famixClass)].
			filterNull) {
			//println("Class (Inheritance): subclass "+ famixClass.value + " ("+famixClass.name+") " + "superclass " + i.superclass.ref  + " ("+i.superclass.ref.name+")")
			val inheritance = diskFactory.createReference
			inheritance.type = "Inheritance"
			inheritance.name = i.superclass.ref.name
			inheritance.fqn = i.superclass.ref.qualifiedName
			disk.references.add(inheritance)
		}
		 */
		hismoClasses.filter[containingNamespaceHistory.ref == hismoClass].forEach[disk.disks += toDisk(level + 1)]
		hismoAttributes.filter[containingClassHistory.ref === hismoClass].filterNull.forEach[disk.data += toDiskSegment()]	
		hismoMethods.filter[
			containingClassHistory.ref === hismoClass
		].filterNull.forEach[disk.methods += toDiskSegment(disk)]


	  	val versions = hismoClassVersions.filter[
			parentHistory.ref === hismoClass
		].filterNull
		//log.info(hismoClass.qualifiedName)
		//versions.forEach[v|log.info("Version " + v)]
		versions.forEach[disk.diskVersions += toVersion(it,hismoClass)]
	}

	def private DiskVersion create diskversion: diskFactory.createDiskVersion toVersion(HISMOClassVersion classVersion,HISMOClassHistory history) {
		diskversion.height = sortedtimestamps.indexOf(classVersion.timestamp) * config.RDHeightBoost
		diskversion.level = sortedtimestamps.indexOf(classVersion.timestamp) 
		diskversion.commitId = classVersion.commitId
		diskversion.author = classVersion.author
		diskversion.timestamp = classVersion.timestamp
		diskversion.name = classVersion.name
		if (history.getMaxNOS() > 0) {
			diskversion.scale = classVersion.getNOS(history)*1.0/history.getMaxNOS();
		} else 	{
			diskversion.scale = 1
		}
		// log.info("classnos: " + classVersion.getNOS(history))
		// log.info("classscale: " + diskversion.scale)
	}
	
	private def int getMaxNOS(HISMOClassHistory history) {
		return hismoClassVersions.filter[
			parentHistory.ref === history
		].filterNull.maxBy[getNOS(history)].getNOS(history) 
	}  
	
	private def int getNOS(HISMOClassVersion version, HISMOClassHistory classHistory) {
		//log.info("Methods for ClassHistory " + classHistory.methodHistories.length)
		val mhRefs = newLinkedList;
		classHistory.methodHistories.forEach[mhRefs += ref]
		
		val methodVersions = hismoMethodVersions.filter[
				timestamp == version.timestamp
			].filter[
				mhRefs.contains(parentHistory.ref)
			]
		//log.info("methods for " + version.name + ": " + methodVersions.length)
		var sum = 0;
		for (methodVersion : methodVersions) {
			sum += methodVersion.evolutionNumberOfStatements
		}
		return sum;
	} 

	def private DiskSegment create diskSegment: diskFactory.createDiskSegment toDiskSegment(HISMOMethodHistory hismoMethod, Disk disk) {
		diskSegment.name = hismoMethod.name
		diskSegment.value = hismoMethod.name
		diskSegment.fqn =  hismoMethod.qualifiedName //"AAA"//method.qualifiedName(parameters)
		diskSegment.signature = hismoMethod.signature
		diskSegment.id = famix.createID(diskSegment.fqn)
		diskSegment.height = config.RDHeight
		diskSegment.color = config.RDMethodColorPercentage
		diskSegment.transparency = config.RDMethodTransparency

		if (hismoMethod.maxNumberOfStatements  <= config.RDMinArea) {
			diskSegment.size = config.RDMinArea
		} else {
			diskSegment.size = 1//hismoMethod.maxNumberOfStatements//method.numberOfStatements
		}
 	 	val versions = hismoMethodVersions.filter[
			parentHistory.ref === hismoMethod
		].filterNull
		//log.info(hismoMethod.qualifiedName)
		//versions.forEach[v|log.info("Version " + v)]
		versions.forEach[diskSegment.versions += toVersion(hismoMethod)]
	}
	
	def private Version create version: diskFactory.createVersion toVersion(HISMOMethodVersion methodVersion, HISMOMethodHistory history) {
		version.height = sortedtimestamps.indexOf(methodVersion.timestamp) * config.RDHeightBoost
		
		version.scale = methodVersion.evolutionNumberOfStatements*1.0/history.maxNumberOfStatements;
		version.level = sortedtimestamps.indexOf(methodVersion.timestamp)
		version.commitId = methodVersion.commitId
		version.author = methodVersion.author
		version.timestamp = methodVersion.timestamp
		version.name = methodVersion.name
	}

	def private DiskSegment create diskSegment: diskFactory.createDiskSegment toDiskSegment(HISMOAttributeHistory attribute) {
		diskSegment.name = attribute.name
		diskSegment.value = attribute.name //attribute.value
		diskSegment.fqn = attribute.qualifiedName
		diskSegment.id = famix.createID(diskSegment.fqn)
		diskSegment.size = 1 //attribute.declaredType.ref.attributeSize
		diskSegment.height = config.RDHeight
		diskSegment.color = config.RDDataColorPercentage
		diskSegment.transparency = config.RDDataTransparency
		
   	val versions = hismoAttributeVersions.filter[
			parentHistory.ref === attribute
		].filterNull
		//versions.forEach[v|log.info("Version " + v)]
		versions.forEach[diskSegment.versions += toVersion(attribute)]
		
	}

	def private Version create version: diskFactory.createVersion toVersion(HISMOAttributeVersion attributeVersion, HISMOAttributeHistory history) {
		version.height = sortedtimestamps.indexOf(attributeVersion.timestamp) * config.RDHeightBoost
		version.level = sortedtimestamps.indexOf(attributeVersion.timestamp)
		version.commitId = attributeVersion.commitId
		version.author = attributeVersion.author
		version.timestamp = attributeVersion.timestamp
		version.scale = 1
		version.name = attributeVersion.name
	}

	def String qualifiedName(HISMOMethodHistory hismoMethodHistory) {
		val ref = hismoMethodHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoMethodHistory.methodVersions.length > 0) {
			val firstMethodVersion = hismoMethodHistory.methodVersions.get(0).ref as HISMOMethodVersion
			return qualifiedName(ref) + "." + ("" + firstMethodVersion.value).removeApostrophes + "+++<" + hismoMethodHistory.name + ">+++"
		} else {
			return qualifiedName(ref) + "." + hismoMethodHistory.name + "+++"
		}
	}
	
	def String qualifiedName(HISMOAttributeHistory hismoAttributeHistory) {
		val ref = hismoAttributeHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoAttributeHistory.attributeVersions.length > 0) {
			val firstAttributeVersion = hismoAttributeHistory.attributeVersions.get(0).ref as HISMOAttributeVersion
			return qualifiedName(ref) + "." + ("" + firstAttributeVersion.value).removeApostrophes + "+++<" + hismoAttributeHistory.name + ">+++"
		} else {
			return qualifiedName(ref) + "." + hismoAttributeHistory.name + "+++"
		}
	}
	
	def String qualifiedName(HISMONamespaceHistory hismoNamespaceHistory) {
		if (hismoNamespaceHistory.containingNamespaceHistory !== null) {
			val ref = hismoNamespaceHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return qualifiedName(ref) + "." + ("" + firstNamespaceVersion.value).removeApostrophes + "+++<" + hismoNamespaceHistory.name + ">+++"
			}
			else {
				return qualifiedName(ref) + "." + hismoNamespaceHistory.name + "+++"
			}
				
		} else {
			
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return ("" + firstNamespaceVersion.value).removeApostrophes + "+++<" + hismoNamespaceHistory.name + ">+++"	
			} else {
				return "unknown"  + "+++"
			}
		}
	}
	
	def String qualifiedName(HISMOClassHistory hismoClassHistory) {
		// log.info("BB")
		if (hismoClassHistory.containingNamespaceHistory !== null) {
			if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMONamespaceHistory){
				var ref = hismoClassHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
				// log.info(ref)
				if (hismoClassHistory.classVersions.length > 0) {
				val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
				return qualifiedName(ref) + "." + ("" + firstClassVersion.value).removeApostrophes + "+++<" +
					hismoClassHistory.name + ">+++"
				} else {
					return qualifiedName(ref) + "." + hismoClassHistory.name + "+++"
				}
			} else if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMOClassHistory) {
				var ref = hismoClassHistory.containingNamespaceHistory.ref as HISMOClassHistory
					// log.info(ref)
				if (hismoClassHistory.classVersions.length > 0) {
				val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
				return qualifiedName(ref) + "." + ("" + firstClassVersion.value).removeApostrophes + "+++<" +
					hismoClassHistory.name + ">+++"
				} else {
					return qualifiedName(ref) + "." + hismoClassHistory.name + "+++"
				}
			} else {
				return 'root'
			}
			
		}
	}

	def private Disk toDisk(FAMIXNamespace famixNamespace, int level, String timestamp) {
		val disk = diskFactory.createDisk
		disk.name = famixNamespace.name
		disk.value = famixNamespace.value
		disk.ringWidth = config.RDRingWidth
		disk.height = config.RDHeight
		disk.position = diskFactory.createPosition
		disk.position.z = sortedtimestamps.indexOf(timestamp) * config.RDHeightMultiplicator
		
		val nsh = hismoPackageVersions.findFirst[hcv|
			hcv.timestamp == timestamp && hcv.versionEntity.ref === famixNamespace  
		].parentHistory.ref as HISMONamespaceHistory
		
		disk.fqn = nsh.qualifiedNameMultiple
		disk.id = famix.createID(disk.fqn)
		disk.type = "FAMIX.Namespace"
		disk.transparency = config.RDNamespaceTransparency
		disk.level = level
		
		structures.filter[container.ref === famixNamespace]
			.forEach[disk.disks += toDisk(level + 1, timestamp)]
			
		subPackages.filter[parentScope.ref === (famixNamespace)]
			.forEach[disk.disks += toDisk(level + 1, timestamp)]

		val versions = hismoPackageVersions.filter[
			parentHistory.ref === nsh
		].filterNull
		
		versions.forEach[v|
			if(v.timestamp == timestamp){
				disk.diskVersion = toDiskVersion(v)
			}
		]
		diskDocument.disks += disk
		
		return disk
	}

	def private Disk toDisk(FAMIXStructure el, int level, String timestamp) {
		val disk = diskFactory.createDisk
		disk.name = el.name
		disk.value = el.value
		disk.ringWidth = config.RDRingWidth
		disk.position = diskFactory.createPosition
		disk.position.z = sortedtimestamps.indexOf(timestamp) * config.RDHeightMultiplicator
		disk.height = config.RDHeight
		disk.transparency = config.RDClassTransparency
		disk.color = config.RDClassColorPercentage
		
		val ch = hismoClassVersions.findFirst[hpv|
			hpv.timestamp == timestamp && hpv.versionEntity.ref === el  
		].parentHistory.ref as HISMOClassHistory
		
		disk.fqn = ch.qualifiedNameMultiple
		disk.id = famix.createID(disk.fqn)
		disk.type = el.typeString
		disk.level = level
		
		// references
		inheritances.filter[subclass.ref === el].forEach[i|
			val inheritance = diskFactory.createReference
			inheritance.type = "Inheritance"
			inheritance.name = i.superclass.ref.name
			inheritance.fqn = i.superclass.ref.fqn
			disk.references += inheritance
		]
		
		attributes.filter[parentType.ref === el].forEach[disk.data += toDiskSegment(timestamp)]
		methods.filter[parentType.ref === el].forEach[disk.methods += toDiskSegment(timestamp)]
		structures.filter[container.ref === el].forEach[disk.disks += toDisk(level + 1, timestamp)]
		enumValues.filter[parentEnum.ref === el].forEach[disk.data += toDiskSegment(timestamp)]
		var sumCompl = 0
		for (m : methods.filter[parentType.ref === el]) {
			sumCompl += m.cyclomaticComplexity
		} 
		
		val versions = hismoClassVersions.filter[
			parentHistory.ref === ch
		].filterNull
		versions.forEach[v|
			if(v.timestamp == timestamp) {
				disk.diskVersion = toVersion(v,ch)
			}
		]
		
		return disk
	}
	
	def private getTypeString(FAMIXStructure el) {
		switch el {
			FAMIXClass: return "FAMIX.Class"
			FAMIXParameterizableClass: return "FAMIX.ParameterizableClass"
			FAMIXEnum: return "FAMIX.Enum"
			FAMIXAnnotationType: return "FAMIX.AnnotationType"
		}
	}

	def private toDiskSegment(FAMIXMethod method, String timestamp) {
		val diskSegment = diskFactory.createDiskSegment
		diskSegment.name = method.name
		diskSegment.value = method.value
		diskSegment.height = config.RDHeight
		diskSegment.color = config.RDMethodColorPercentage
		diskSegment.transparency = config.RDMethodTransparency
		
		val mh = hismoMethodVersions.findFirst[hmv|
			hmv.timestamp == timestamp && hmv.versionEntity.ref === method  
		].parentHistory.ref as HISMOMethodHistory
		
		diskSegment.fqn = mh.qualifiedNameMultiple
		diskSegment.id = famix.createID(diskSegment.fqn)
		diskSegment.signature = method.signature

		if (method.numberOfStatements <= config.RDMinArea) {
			diskSegment.size = config.RDMinArea
		} else {
			diskSegment.size = method.numberOfStatements
		}
		val versions = hismoMethodVersions.filter[
			parentHistory.ref === mh
		].filterNull
		
		versions.forEach[v|
			if(v.timestamp == timestamp){
				diskSegment.version = toVersion(v,mh)
			}
		]
		return diskSegment
	}
	
	def private toDiskSegment(FAMIXAttribute attribute, String timestamp) {
		val diskSegment = diskFactory.createDiskSegment
		diskSegment.name attribute.name
		diskSegment.value = attribute.value
		diskSegment.height = config.RDHeight
		diskSegment.color = config.RDDataColorPercentage
		diskSegment.transparency = config.RDDataTransparency
	  	val a = hismoAttributeVersions.findFirst[hav|
			hav.timestamp == timestamp && hav.versionEntity.ref === attribute
		]
		 val ah = a.parentHistory.ref as HISMOAttributeHistory
		diskSegment.fqn = ah.qualifiedNameMultiple
		diskSegment.id = famix.createID(diskSegment.fqn)
		diskSegment.size = 1 //attribute.declaredType.ref.attributeSize
		val versions = hismoAttributeVersions.filter[
		parentHistory.ref === ah
		].filterNull
		
		versions.forEach[v|
			if(v.timestamp == timestamp) {
				diskSegment.version = toVersion(v,ah)
			}
		]
		return diskSegment
	}	

	def private toDiskSegment(FAMIXEnumValue enumValue, String timestamp) {
		val diskSegment = diskFactory.createDiskSegment
		diskSegment.name = enumValue.name
		diskSegment.value = enumValue.value
		diskSegment.fqn = enumValue.fqn
		diskSegment.id = famix.createID(diskSegment.fqn)
		diskSegment.size = 1 // TODO size
		diskSegment.height = config.RDHeight
		diskSegment.color = config.RDDataColorPercentage
		diskSegment.transparency = config.RDDataTransparency
		return diskSegment
	}
	
	def private String qualifiedNameMultiple(HISMOMethodHistory hismoMethodHistory) {
		val ref = hismoMethodHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoMethodHistory.methodVersions.length > 0) {
			val firstMethodVersion = hismoMethodHistory.methodVersions.get(0).ref as HISMOMethodVersion
			return qualifiedNameMultiple(ref) + "." + ("" + firstMethodVersion.value).removeApostrophes 
		} else {
			return qualifiedNameMultiple(ref) + "." + hismoMethodHistory.name 
		}
	}
	
	def private String qualifiedNameMultiple(HISMOAttributeHistory hismoAttributeHistory) {
		val ref = hismoAttributeHistory.containingClassHistory.ref as HISMOClassHistory
		if (hismoAttributeHistory.attributeVersions.length > 0) {
			val firstAttributeVersion = hismoAttributeHistory.attributeVersions.get(0).ref as HISMOAttributeVersion
			return qualifiedNameMultiple(ref) + "." + ("" + firstAttributeVersion.value).removeApostrophes
		} else {
			return qualifiedNameMultiple(ref) + "." + hismoAttributeHistory.name 
		}
	}
	
	def String qualifiedNameMultiple(HISMONamespaceHistory hismoNamespaceHistory) {
		if (hismoNamespaceHistory.containingNamespaceHistory !== null) {
			val ref = hismoNamespaceHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return qualifiedNameMultiple(ref) + "." + ("" + firstNamespaceVersion.value).removeApostrophes
			} else {
				return qualifiedNameMultiple(ref) + "." + hismoNamespaceHistory.name 
			}	
		} else {
			if (hismoNamespaceHistory.namespaceVersions.length > 0) {
				val firstNamespaceVersion = hismoNamespaceHistory.namespaceVersions.get(0).ref as HISMONamespaceVersion
				return ("" + firstNamespaceVersion.value).removeApostrophes 	
			} else {
				return "unknown"  + "+++"
			}
		}
	}
	
	def private String qualifiedNameMultiple(HISMOClassHistory hismoClassHistory) {
		//log.info("BB")
		if (hismoClassHistory.containingNamespaceHistory !== null) {
			if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMONamespaceHistory) {
				val ref = hismoClassHistory.containingNamespaceHistory.ref as HISMONamespaceHistory
				//log.info(ref)
				if (hismoClassHistory.classVersions.length > 0) {
					val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
					return qualifiedNameMultiple(ref) + "." + ("" + firstClassVersion.value).removeApostrophes 
				} else {
					return qualifiedNameMultiple(ref) + "." + hismoClassHistory.name 
				}
			} else if(hismoClassHistory.containingNamespaceHistory.ref instanceof HISMOClassHistory) {
				val ref = hismoClassHistory.containingNamespaceHistory.ref as HISMOClassHistory
				//log.info(ref)
				if (hismoClassHistory.classVersions.length > 0) {
					val firstClassVersion = hismoClassHistory.classVersions.get(0).ref as HISMOClassVersion
					return qualifiedNameMultiple(ref) + "." + ("" + firstClassVersion.value).removeApostrophes 
				} else {
					return qualifiedNameMultiple(ref) + "." + hismoClassHistory.name 
				}
			}
		} else {
			return 'root'
		}
	}
}