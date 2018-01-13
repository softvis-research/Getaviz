package org.svis.generator.rd.s2m

import java.util.List
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.EcoreUtil2
import org.svis.generator.SignatureConverter
import org.svis.xtext.dynamix.DYNAMIXActivation
import org.svis.xtext.dynamix.DYNAMIXInstance
import org.svis.xtext.dynamix.FAMIXClass
import org.svis.xtext.dynamix.FAMIXMethod
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.Root
import org.svis.xtext.rd.impl.RdFactoryImpl
import org.apache.commons.lang.StringUtils
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.svis.xtext.rd.DiskSegmentInvocation
import java.util.HashMap
import org.svis.xtext.rd.DiskSegment
import java.util.ArrayList

class Dynamix2RD extends WorkflowComponentWithModelSlot {
	val rdFactory = new RdFactoryImpl()
	val log = LogFactory::getLog(class)
	private double minStep = 0
	private long minTime = 0
	private double HEIGHT_PER_STEP = 0.003
	private double minHeight = 1 //DISK_GAP * maxHeightMultiplicator
	private double maxTime = 0
	var HashMap<String,Integer> invocationCounter = new HashMap<String,Integer>()

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Dynamix2RD has started.")

		// get source model dynamix
		val dynamixRoot = (ctx.get("dynamix") as List<org.svis.xtext.dynamix.Root>).head
		val dynamixDocument = dynamixRoot.document
		maxTime = dynamixDocument.elements.filter(DYNAMIXActivation).sortBy[stop].last.stop ;
		minTime = dynamixDocument.elements.filter(DYNAMIXActivation).sortBy[start].get(0).start ;
		
		val shortestMethod = dynamixDocument.elements.filter(DYNAMIXActivation).sortBy[stop-start].get(0)
		minStep = shortestMethod.stop -shortestMethod.start
			
		if (minStep == 0) {
			minStep = 1
		}
		val rdRoots = ctx.get("rd") as List<Root> 
		val rdRoot = rdRoots.get(0)
		val disks = EcoreUtil2::getAllContentsOfType(rdRoot.document, Disk)
		//	  val activations = EcoreUtil2::getAllContentsOfType(dynamixDocument, DYNAMIXActivation).sortBy[start]
		val instanceActivations = EcoreUtil2::getAllContentsOfType(dynamixDocument, DYNAMIXActivation).filter[receiver !== null].toList
		val invocationActivations = EcoreUtil2::getAllContentsOfType(dynamixDocument, DYNAMIXActivation).filterNull.filter[receiver === null].toList
		
		toInstance(instanceActivations, disks)
		toInvocation(invocationActivations, disks)
		
		var dynamixResource = new ResourceImpl()
		dynamixResource.contents += dynamixRoot
		
		//val invocations = EcoreUtil2::getAllContentsOfType(rdRoot.document, DiskSegmentInvocation)
		//println(invocations.length)
		
		ctx.set("dynamix", dynamixResource)
		ctx.set("rd", rdRoot)
		ctx.set("rdwriter", newArrayList(rdRoot))
		log.info("Dynamix2RD has finished.")
	}

	def private toInstance(List<DYNAMIXActivation> activations, List<Disk> disks) {
		activations.forEach[activation, index |
			val instance = activation.receiver.idref as DYNAMIXInstance
			val disk = disks.filter[fqn == (instance.instanceOf.idref as FAMIXClass).toFQNInstance].last
			if (disk !== null) {
				val diskInstance = rdFactory.createDiskInstance
				val method = activation.method.idref as FAMIXMethod
				if (method.value.startsWith("<init>")) {
					diskInstance.name = instance.name
					diskInstance.fqn = (instance.instanceOf.idref as FAMIXClass).value
					diskInstance.start = activation.start
					diskInstance.position = rdFactory.createPosition
					diskInstance.position.x = disk.position.x
					diskInstance.position.y = disk.position.y
					diskInstance.position.z = index
					disk.instances += diskInstance
				} else {
					val segment = disk.methods.filter[fqn == method.toMethodIDInstance].last
					if (segment !== null) {
						//println("method:" + method.toMethodID)
						//disk.methods.forEach[println(fqn)]
						val diskSegmentInvocation = rdFactory.createDiskSegmentInvocation
						diskSegmentInvocation.name = activation.name
						diskSegmentInvocation.fqn = method.toMethodIDInstance
						diskSegmentInvocation.start = activation.start
						diskSegmentInvocation.stop = activation.stop
						diskSegmentInvocation.position = rdFactory.createPosition
						diskSegmentInvocation.position.x = disk.position.x
						diskSegmentInvocation.position.y = disk.position.y
						diskSegmentInvocation.position.z = index
						diskInstance.invocations += diskSegmentInvocation
						segment.invocations += diskSegmentInvocation
					} else {
						log.warn("No matching method for : " + method.toMethodIDInstance)
					}	
				}
			}
		]
	}

	def private toInvocation(List<DYNAMIXActivation> activations, List<Disk> disks) {
		var dsis = new ArrayList<DiskSegmentInvocation>();
		for (DYNAMIXActivation activation : activations) {
			// static call
			val method = activation.method.idref as FAMIXMethod
			var disk = disks.filter[fqn.equals((method.belongsTo.idref as FAMIXClass).toFQN)].last
			if (disk !== null) {
				var DiskSegment segment;
				if (method.signature !== null) {
					segment = disk.methods.filter[signature.equals(method.signature)].last
				} else {
					segment = disk.methods.filter[fqn.equals(method.toMethodID)].last
				}
				if (segment !== null) {
					// create invocation
					var diskSegmentInvocation = rdFactory.createDiskSegmentInvocation
					diskSegmentInvocation.name = activation.name
					diskSegmentInvocation.fqn = segment.fqn
					if (invocationCounter.get(diskSegmentInvocation.fqn) !== null) {
						invocationCounter.put((diskSegmentInvocation.fqn),invocationCounter.get(diskSegmentInvocation.fqn) + 1) 
					} else {
						invocationCounter.put((diskSegmentInvocation.fqn),1)
					}
					diskSegmentInvocation.start = activation.start
					diskSegmentInvocation.stop = activation.stop
					var heightStart = minHeight + ((activation.start - minTime)/minStep) * HEIGHT_PER_STEP
					var heightStop =  minHeight + ((activation.stop - minTime)/minStep) *  HEIGHT_PER_STEP
					diskSegmentInvocation.length = (heightStop - heightStart + 0.2) as int
					diskSegmentInvocation.position = rdFactory.createPosition 
					diskSegmentInvocation.position.x = 0.0
					diskSegmentInvocation.position.y = 0.0
					diskSegmentInvocation.position.z = heightStart
					if (activation.parent !== null) {
						val parentDsis = dsis.findFirst[dsi|dsi.name == (activation.parent.idref as DYNAMIXActivation).name]
						if (parentDsis !== null) {
							diskSegmentInvocation.caller = parentDsis.name
						}
					}
					segment.invocations.add(diskSegmentInvocation)
					dsis.add(diskSegmentInvocation)
					
				} else {
					/*log.info("Invocation: " + disk.fqn)
					disk.methods.forEach[log.info("Invocation:   " + method.signature)]
					log.info("Invocation: No matching method for : " + method.signature)
					log.warn("Invocation: No matching method for : " + method.toMethodID)*/
				}
			} else {
			//	log.warn("Invocation: No matching class in for class: " + (method.belongsTo.idref as FAMIXClass).value)
			}
		}
	}
		def String toFQN(FAMIXClass famixClass) {
		val occurenceOfDollarSign = StringUtils.countMatches(famixClass.value, "$")
		switch(occurenceOfDollarSign){
			case 0: {return famixClass.value.replaceAll("::", ".")}
			case 1: {return famixClass.value.replace("$", ".").replaceAll("::", ".")}
			case 2: {return famixClass.value.replace("$", ".").replaceAll("::", ".").substring(famixClass.value.indexOf("$"))}
			default: {log.warn("Problems resolving FQN for class: " + famixClass.value)}
		}
		return ""
	}

	def String toMethodID(FAMIXMethod method) {
		/*if (method.value.startsWith("main")) {
		 * 	// exception: famix v1.0 does not provide parameters in main
		 * 	return (method.belongsTo.idref as FAMIXClass).value + "." + method.value.split("\\(").get(0) + "__"
		 } else */
		if (method.value.contains("()")) {
			// signature without parameters
			return (method.belongsTo.idref as FAMIXClass).toFQN + "." + method.value.split("\\(").get(0) + "__"
		} else {
			// signature with parameters
			if (method.signature !== null) {
				val className = (method.belongsTo.idref as FAMIXClass).toFQN
				return className + method.signature.replaceAll("def ","").replaceAll("\\(", "__").replaceAll("\\)", "_____").replaceAll(", ", "___").
					replaceAll("\\[\\]", "")
			} else {
				val className = (method.belongsTo.idref as FAMIXClass).toFQN
				val methodName = method.value.substring(0, method.value.indexOf("("))
				val signature = method.value.substring(method.value.indexOf("("),method.value.indexOf(")"))
//				log.error("unconverted: " + className + " " + methodName + " " + signature)
//				log.error("converted: " + SignatureConverter.convertMethodSignature(className, methodName, signature))
				var methodID = className + methodName + signature.replaceAll(",", "_").replaceAll(" ", "")//SignatureConverter.convertMethodSignature(className, methodName, signature)
				return methodID.replaceAll("\\(", "__").replaceAll("\\)", "_____").replaceAll(", ", "___").
					replaceAll("\\[\\]", "")
			}
		}
	}
	
 	def private String toFQNInstance(FAMIXClass famixClass) {
		val occurenceOfDollarSign = StringUtils::countMatches(famixClass.value, "$")
		switch(occurenceOfDollarSign){
			case 0: return famixClass.value
			case 1: return famixClass.value.replace("$", ".")
			case 2: return famixClass.value.replace("$", ".").substring(famixClass.value.indexOf("$"))
			default: log.warn("Problems resolving FQN for class: " + famixClass.value)
		}
		return "".replaceAll("::",".")
	}

	def private String toMethodIDInstance(FAMIXMethod method) {
		/*if (method.value.startsWith("main")) {
		 * 	// exception: famix v1.0 does not provide parameters in main
		 * 	return (method.belongsTo.idref as FAMIXClass).value + "." + method.value.split("\\(").get(0) + "__"
		 } else */
	 	if (method.value.contains("()")) {
			// signature without parameters
			return (method.belongsTo.idref as FAMIXClass).toFQNInstance + "." + method.value.split("\\(").get(0)+ "()"
		} else {
			// signature with parameters
			val className = (method.belongsTo.idref as FAMIXClass).toFQNInstance
			val methodName = method.value.substring(0, method.value.indexOf("("))
			val signature = method.value.substring(method.value.indexOf("("))
//			log.error("unconverted: " + className + " " + methodName + " " + signature)
//			log.error("converted: " + SignatureConverter.convertMethodSignature(className, methodName, signature))
			var methodID = SignatureConverter.convertMethodSignature(className, methodName, signature)
			println("ho")
			return methodID//.replaceAll("\\(", "__").replaceAll("\\)", "_____").replaceAll(", ", "___").
				//replaceAll("\\[\\]", "")
		}
	}
}