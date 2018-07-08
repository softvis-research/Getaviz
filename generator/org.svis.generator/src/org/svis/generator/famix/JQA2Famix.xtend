package org.svis.generator.famix

import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.generator.WorkflowComponentWithConfig
import java.util.Map
import org.svis.generator.famix.FAMIXSettings
import org.svis.xtext.famix.impl.FamixFactoryImpl
import org.neo4j.graphdb.GraphDatabaseService
import org.svis.lib.database.Database
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.traversal.Uniqueness
import org.svis.xtext.famix.FAMIXElement
import org.neo4j.graphdb.Relationship
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.svis.xtext.famix.Document

class JQA2Famix extends WorkflowComponentWithConfig {
	var GraphDatabaseService graph
	val static famixFactory = new FamixFactoryImpl()
	val famixEvaluator = new FamixEvaluator()
	val Map<Long, FAMIXNamespace> namespaces = newHashMap
	val Map<Long, FAMIXStructure> structures = newHashMap
	val Map<Long, FAMIXMethod> methods = newHashMap
	val Map<Long, FAMIXAttribute> attributes = newHashMap

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("JQA2Famix has started.")
		val famixRoot = famixFactory.createRoot
		val famixDocument = famixFactory.createDocument
		famixRoot.document = famixDocument
		graph = Database::getInstance(FAMIXSettings::DATABASE_NAME)
		run(famixDocument)
		ctx.set("famix", famixRoot)
		val resource = new ResourceImpl()
		resource.contents += famixRoot
		ctx.set("metadata", resource)
		log.info("JQA2Famix has finished.")
	}

	def private run(Document famixDocument) {
		var tx = graph.beginTx
		try {
			// get roots packages
			val result = graph.execute("MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package)RETURN n")
			result.forEach [ row |
				val rootnode = row.get("n") as Node
				// traverse though complete graph
				graph.traversalDescription.relationships(Rels.CONTAINS, Direction.OUTGOING).relationships(Rels.DECLARES,
					Direction.OUTGOING) // filters relevant relationships
				.uniqueness(Uniqueness.NONE).evaluator(famixEvaluator) // filters relevant nodes
				.traverse(rootnode).forEach [ path |
					// this loop is executed for every path
					// for every end node an own path exists
					// therefore we are just interested in the end node, the other nodes have already been handled
					val node = path.endNode
					var Node parent = null
					if (path.length != 0) {
						parent = path.nodes.get(path.length - 1)
					}
					if (node.hasLabel(Labels.Package)) {
						var FAMIXNamespace parentNamespace = null
						if (parent !== null) {
							parentNamespace = namespaces.get(parent.id)
						}
						val namespace = createNamespace(node, parentNamespace)
						namespaces.put(node.id, namespace)
						famixDocument.elements += namespace
					} else if (node.hasLabel(Labels.Class) || node.hasLabel(Labels.Interface)) {
						var FAMIXElement container
						if (parent.hasLabel(Labels.Package)) {
							container = namespaces.get(parent.id)
						}
						if (isStructure(parent)) {
							container = structures.get(parent.id)
						}
						val class = createClass(node, container)
						structures.put(node.id, class)
						famixDocument.elements += class
					} else if (node.hasLabel(Labels.Enum)) {
						var FAMIXElement container
						if (parent.hasLabel(Labels.Package)) {
							container = namespaces.get(parent.id)
						}
						if (isStructure(parent)) {
							container = structures.get(parent.id)
						}
						val enum = createEnum(node, container)
						structures.put(node.id, enum)
						famixDocument.elements += enum
					} else if (node.hasLabel(Labels.Annotation)) {
						var FAMIXElement container
						if (parent.hasLabel(Labels.Package)) {
							container = namespaces.get(parent.id)
						}
						if (isStructure(parent)) {
							container = structures.get(parent.id)
						}
						val annotation = createAnnotation(node, container)
						structures.put(node.id, annotation)
						famixDocument.elements += annotation
					} else if (node.hasLabel(Labels.Method)) {
						var FAMIXStructure container
						if (isStructure(parent)) {
							container = structures.get(parent.id)
						}
						val fileAnchor = famixFactory.createFAMIXFileAnchor
						fileAnchor.filename = parent.getProperty("sourceFileName") as String
						val method = createMethod(node, container, fileAnchor)
						methods.put(node.id, method)
						famixDocument.elements += method
					} else if (node.hasLabel(Labels.Field)) {
						var FAMIXStructure container
						if (isStructure(parent)) {
							container = structures.get(parent.id)
						}
						val fileAnchor = famixFactory.createFAMIXFileAnchor
						fileAnchor.filename = parent.getProperty("sourceFileName") as String
						val attribute = createAttribute(node, container, fileAnchor)
						attributes.put(node.id, attribute)
						famixDocument.elements += attribute
					}
				]
			]
			graph.execute("MATCH p=()-[r:INVOKES]->() RETURN r").forEach [ row |
				val rel = row.get("r") as Relationship
				if (methods.containsKey(rel.startNode.id) && methods.containsKey(rel.endNode.id)) {
					val invocation = famixFactory.createFAMIXInvocation
					val senderRef = famixFactory.createIntegerReference
					val receiverRef = famixFactory.createIntegerReference
					senderRef.ref = methods.get(rel.startNode.id)
					receiverRef.ref = methods.get(rel.endNode.id)
					invocation.sender = senderRef
					invocation.candidates = receiverRef
					famixDocument.elements += invocation
				}
			]
			graph.execute("MATCH p=()-[r:READS|WRITES]->() RETURN r").forEach [ row |
				val rel = row.get("r") as Relationship
				if (methods.containsKey(rel.startNode.id) && attributes.containsKey(rel.endNode.id)) {
					val access = famixFactory.createFAMIXAccess
					val attributeRef = famixFactory.createIntegerReference
					val accessorRef = famixFactory.createIntegerReference
					attributeRef.ref = attributes.get(rel.endNode.id)
					accessorRef.ref = methods.get(rel.startNode.id)
					access.variable = attributeRef
					access.accessor = accessorRef
					famixDocument.elements += access
				}

			]
			graph.execute("MATCH p=()-[r:EXTENDS]->() RETURN r").forEach [ row |
				val rel = row.get("r") as Relationship
				if (structures.containsKey(rel.startNode.id) && structures.containsKey(rel.endNode.id)) {
					val inheritance = famixFactory.createFAMIXInheritance
					val subclassRef = famixFactory.createIntegerReference
					val superclassRef = famixFactory.createIntegerReference
					subclassRef.ref = structures.get(rel.startNode.id)
					superclassRef.ref = structures.get(rel.endNode.id)
					inheritance.subclass = subclassRef
					inheritance.superclass = superclassRef
					famixDocument.elements += inheritance
				}
			]
			tx.success
		} finally {
			tx.close
		}
	}

	def createNamespace(Node node, FAMIXNamespace parent) {
		val namespace = famixFactory.createFAMIXNamespace
		val id = node.id.toString
		namespace.name = id
		namespace.id = id
		namespace.value = node.getProperty("name") as String
		namespace.fqn = node.getProperty("fqn") as String
		if (parent !== null) {
			var ref = famixFactory.createIntegerReference
			ref.ref = parent
			namespace.parentScope = ref
		}
		return namespace
	}

	def createClass(Node node, FAMIXElement parent) {
		val class = famixFactory.createFAMIXClass
		class.name = node.id.toString
		class.value = node.getProperty("name") as String
		class.fqn = node.getProperty("fqn") as String
		class.id = node.getProperty("md5") as String
		addModifiers(node, class.modifiers)
		var ref = famixFactory.createIntegerReference
		ref.ref = parent
		class.container = ref
		val anchorRef = famixFactory.createIntegerReference
		val classRef = famixFactory.createIntegerReference
		val fileAnchor = famixFactory.createFAMIXFileAnchor
		fileAnchor.filename = node.getProperty("sourceFileName") as String
		anchorRef.ref = class
		classRef.ref = fileAnchor
		class.type = anchorRef
		fileAnchor.element = classRef
		return class
	}

	def createEnum(Node node, FAMIXElement parent) {
		val enum = famixFactory.createFAMIXEnum
		enum.name = node.id.toString
		enum.id = node.id.toString
		enum.value = node.getProperty("name") as String
		enum.fqn = node.getProperty("fqn") as String
		var ref = famixFactory.createIntegerReference
		ref.ref = parent
		enum.container = ref
		val anchorRef = famixFactory.createIntegerReference
		val enumRef = famixFactory.createIntegerReference
		val fileAnchor = famixFactory.createFAMIXFileAnchor
		fileAnchor.filename = node.getProperty("sourceFileName") as String
		anchorRef.ref = enum
		enumRef.ref = fileAnchor
		enum.sourceAnchor = anchorRef
		fileAnchor.element = enumRef
		return enum
	}

	def createAnnotation(Node node, FAMIXElement parent) {
		val annotation = famixFactory.createFAMIXAnnotationType
		annotation.name = node.id.toString
		annotation.id = node.id.toString
		annotation.value = node.getProperty("name") as String
		annotation.fqn = node.getProperty("fqn") as String
		var ref = famixFactory.createIntegerReference
		ref.ref = parent
		annotation.container = ref
		val anchorRef = famixFactory.createIntegerReference
		val annoRef = famixFactory.createIntegerReference
		val fileAnchor = famixFactory.createFAMIXFileAnchor
		fileAnchor.filename = node.getProperty("sourceFileName") as String
		anchorRef.ref = annotation
		annoRef.ref = fileAnchor
		annotation.sourceAnchor = anchorRef
		fileAnchor.element = annoRef
		return annotation
	}

	def createMethod(Node node, FAMIXStructure parent, FAMIXFileAnchor fileAnchor) {
		val method = famixFactory.createFAMIXMethod
		method.name = node.id.toString
		method.id = node.id.toString
		if (node.hasProperty("name")) {
			method.value = node.getProperty("name") as String
		}
		if (node.hasProperty("effectiveLineCount")) {
			val numberOfStatements = node.getProperty("effectiveLineCount") as Long
			method.numberOfStatements = numberOfStatements.intValue
			val cyclomaticComplexity = node.getProperty("cyclomaticComplexity") as Long
			method.cyclomaticComplexity = cyclomaticComplexity.intValue
			val firstLineNumber = node.getProperty("firstLineNumber") as Long
			fileAnchor.startline = firstLineNumber.intValue
			val lastLineNumber = node.getProperty("lastLineNumber") as Long
			fileAnchor.endline = lastLineNumber.intValue
		}
		var signature = node.getProperty("signature") as String
		val index = signature.indexOf(" ") + 1
		signature = signature.substring(index)
		method.signature = signature
		method.fqn = parent.fqn + "." + signature
		addModifiers(node, method.modifiers)
		var ref = famixFactory.createIntegerReference
		ref.ref = parent
		method.parentType = ref
		var anchorRef = famixFactory.createIntegerReference
		anchorRef.ref = method
		var methodRef = famixFactory.createIntegerReference
		methodRef.ref = fileAnchor
		fileAnchor.element = methodRef
		method.sourceAnchor = anchorRef
		return method
	}

	def createAttribute(Node node, FAMIXElement parent, FAMIXFileAnchor fileAnchor) {
		val attribute = famixFactory.createFAMIXAttribute
		attribute.name = node.id.toString
		attribute.id = node.id.toString
		if (node.hasProperty("name")) {
			attribute.value = node.getProperty("name") as String
		}
		addModifiers(node, attribute.modifiers)
		var ref = famixFactory.createIntegerReference
		ref.ref = parent
		attribute.parentType = ref
		val anchorRef = famixFactory.createIntegerReference
		val attributeRef = famixFactory.createIntegerReference
		anchorRef.ref = attribute
		attributeRef.ref = fileAnchor
		fileAnchor.element = attributeRef
		attribute.sourceAnchor = anchorRef
		return attribute
	}

	def isStructure(Node node) {
		if (node.hasLabel(Labels.Type)) {
			return true
		}
	}

	def addModifiers(Node node, EList<String> modifiers) {
		if (node.hasProperty("visibility")) {
			modifiers += node.getProperty("visibility") as String
		}
		if (node.hasProperty("final")) {
			if (node.getProperty("final") === true) {
				modifiers += "final"
			}
		}
		if (node.hasProperty("abstract")) {
			if (node.getProperty("abstract") === true) {
				modifiers += "abstract"
			}
		}
		if (node.hasProperty("static")) {
			modifiers += "static"
		}
	}
}
