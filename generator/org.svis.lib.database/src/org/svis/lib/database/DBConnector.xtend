package org.svis.lib.database

import org.svis.lib.database.DBLabel
import org.svis.lib.database.DBRelationships
import org.svis.xtext.city.District
import org.svis.xtext.city.Building
import org.neo4j.graphdb.ConstraintViolationException
import org.neo4j.graphdb.GraphDatabaseService
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.DiskSegment
import java.util.List
import org.apache.commons.logging.LogFactory
import org.neo4j.graphdb.Node
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXEnumValue
import java.util.Map
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXInvocation
import org.svis.xtext.famix.FAMIXInheritance
import org.svis.xtext.famix.FAMIXAccess
import org.svis.lib.database.utils.RDUtils

class DBConnector {
	var public GraphDatabaseService graphDb
	protected val log = LogFactory::getLog(class)
	val Map<String, Node> nodes = newHashMap
	extension RDUtils util = new RDUtils
	
	new(GraphDatabaseService graphDb) {
		this.graphDb = graphDb
	}
	
	def public void createCityNode(String configSnapshotId) {
		val famixNode = graphDb.findNode(DBLabel::FAMIX, "snapshotID", configSnapshotId)
		val relations = famixNode.getRelationships(DBRelationships::VISUALIZED_BY)
		relations.forEach[r|
			val labels = r.endNode.labels
			labels.forEach[l|
				if (l.name.equals(DBLabel::CITY.name)) {
					throw new ConstraintViolationException("")
				}
			]
		]
		val node = graphDb.createNode(DBLabel::CITY)
		famixNode.createRelationshipTo(node, DBRelationships::VISUALIZED_BY)
	}
	
	def void createRDNode(List<Disk> rootDisks,String configSnapshotId) {
       	val famixNode = graphDb.findNode(DBLabel::FAMIX, "snapshotID", configSnapshotId)
       	if(famixNode === null) {
       		log.error("famixNode is null")
       		log.error("snapshodID: " + configSnapshotId)
       	}
       	val relations = famixNode.getRelationships(DBRelationships::VISUALIZED_BY)
		relations.forEach[r|
			val labels = r.endNode.labels
			labels.forEach[l|
				if (l.name.equals(DBLabel::RD.name)) {
					throw new ConstraintViolationException("")
				}
			]
		]
		val node = graphDb.createNode(DBLabel::RD)
		famixNode.createRelationshipTo(node, DBRelationships::VISUALIZED_BY)
		node.setProperty("rootDisks", rootDisks.size)
		if (rootDisks.empty) {
			return
		}
		if(rootDisks.size > 1) {
			node.setProperty("thicknessOfOuterRing", rootDisks.last.ringWidth )
		} else {
			node.setProperty("thicknessOfOuterRing", calculateThickness(rootDisks.last))
		}
	}
	
	def Node createFamixNode(Map<String, String> nodeProperties) {
		var systemNode = graphDb.findNode(DBLabel::SYSTEM, "systemID", nodeProperties.get("systemId"))
		if (systemNode === null) {
			systemNode = graphDb.createNode(DBLabel::SYSTEM)
   			systemNode.setProperty("name", nodeProperties.get("repositoryName"))
   			systemNode.setProperty("owner", nodeProperties.get("repositoryOwner"))
   			systemNode.setProperty("systemID", nodeProperties.get("systemId"))
   			systemNode.setProperty("url", nodeProperties.get("repositoryUrl"))
   			systemNode.setProperty("language", nodeProperties.get("language"))
		}
		
		val snapshotNode = graphDb.createNode(DBLabel::FAMIX)
  		snapshotNode.setProperty("commitOrder", Integer.valueOf(nodeProperties.get("commitOrder")))
  		snapshotNode.setProperty("snapshotID", nodeProperties.get("snapshotId"))
  		snapshotNode.setProperty("commitID", nodeProperties.get("commit"))
 		systemNode.createRelationshipTo(snapshotNode, DBRelationships::HAS_SNAPSHOT)
  		return snapshotNode
	}
	
	def void addToDB(District district) {
		val famixNode = graphDb.findNode(DBLabel::FAMIXELEMENT, "fid", district.id)
		val relations = famixNode.getRelationships(DBRelationships::VISUALIZED_BY)
		relations.forEach[r|
			val labels = r.endNode.labels
			labels.forEach[l|
				if (l.name.equals(DBLabel::DISTRICT.name)) {
					throw new ConstraintViolationException("")
				}
			]
		]
		val node = graphDb.createNode(DBLabel::DISTRICT)
		node.setProperty("width", district.width)
		node.setProperty("area", district.width * district.width)
		node.setProperty("color", district.color)
		node.setProperty("level", district.level)
		node.setProperty("x", district.position.x)
		node.setProperty("y", district.position.y)
		node.setProperty("z", district.position.z)
		famixNode.createRelationshipTo(node, DBRelationships::VISUALIZED_BY)
		
		district.entities.forEach[entity|
			switch entity {
				Building: addToDB(entity)
				District: addToDB(entity)
			}
		]
	}
	
	def private void addToDB(Building building) {
		val node = graphDb.createNode(DBLabel::BUILDING)
			
		val famixNode = graphDb.findNode(DBLabel::FAMIXELEMENT, "fid", building.id)
		famixNode.createRelationshipTo(node, DBRelationships::VISUALIZED_BY)
			
		node.setProperty("level", building.level)
		node.setProperty("width", building.width)
		node.setProperty("area", building.width * building.width)
		node.setProperty("color", building.color)
		node.setProperty("height", building.height)
		node.setProperty("x", building.position.x)
		node.setProperty("y", building.position.y)
		node.setProperty("z", building.position.z)
	}
	
	def void addToDB(Disk disk) {
		val famixNode = graphDb.findNode(DBLabel::FAMIXELEMENT, "fid", disk.id)
		val relations = famixNode.getRelationships(DBRelationships::VISUALIZED_BY)
		relations.forEach[r|
			val labels = r.endNode.labels
			labels.forEach[l|
				if (l.name.equals(DBLabel::DISK.name)) {
					throw new ConstraintViolationException("")
				}
			]
		]
		val node = graphDb.createNode(DBLabel::DISK)
		famixNode.createRelationshipTo(node, DBRelationships::VISUALIZED_BY)
		node.setProperty("level", disk.level)
		node.setProperty("grossArea", disk.grossArea)
		node.setProperty("netArea", disk.netArea)
		node.setProperty("density", disk.netArea / disk.grossArea)
		node.setProperty("color", disk.color)
		node.setProperty("height", disk.height)
		node.setProperty("dataArea", disk.dataArea)
		node.setProperty("methodArea", disk.methodArea)
		node.setProperty("x", disk.position.x)
		node.setProperty("y", disk.position.y)
		node.setProperty("z", disk.position.z)
		node.setProperty("crossSection", disk.crossSection)
		node.setProperty("spine", disk.spine)
		node.setProperty("transparency", disk.transparency)
			
		if (disk.type == "FAMIX.Namespace") {
			val layers = calculateNumberOfLayers(disk)
			node.setProperty("numberOfLayers", layers)
		}
		disk.disks.forEach[addToDB]
		disk.methods.forEach[addToDB]
		disk.data.forEach[addToDB]
	}
	
	def private void addToDB(DiskSegment segment) {
		val node = graphDb.createNode(DBLabel::DISKSEGMENT)
		node.setProperty("area", segment.size)
		node.setProperty("color", segment.color)
		node.setProperty("crossSection", segment.crossSection)
		node.setProperty("spine=", segment.spine)
		node.setProperty("transparency", segment.transparency)
		val famixNode = graphDb.findNode(DBLabel::FAMIXELEMENT, "fid", segment.id)
		try {
			famixNode.createRelationshipTo(node, DBRelationships::VISUALIZED_BY)
		} catch (NullPointerException e) {
			if(famixNode == null) {
				println("famixnode")
			}
			if(node == null) {
				println("node")
			}
		}
	}
	
	def void addToDB(FAMIXInvocation invocation) {
		val sender = invocation.sender.ref as FAMIXMethod
		val candidate = invocation.candidates.ref as FAMIXMethod
		val nodeSender = nodes.get(sender.id)
		val nodeCandidate = nodes.get(candidate.id)
		if (nodeSender !== null && nodeCandidate !== null) {
			nodeSender.createRelationshipTo(nodeCandidate, DBRelationships::CALLS)
		}
	}
	
	def void addToDB(FAMIXInheritance inheritance) {
		val subClass = inheritance.subclass.ref as FAMIXStructure
		val superClass = inheritance.superclass.ref as FAMIXStructure
		val nodeSubClass = nodes.get(subClass.id)
		val nodeSuperClass = nodes.get(superClass.id)
		if (nodeSubClass !== null && nodeSuperClass !== null) {
			nodeSubClass.createRelationshipTo(nodeSuperClass, DBRelationships::INHERITS)
		}
	}
	
	def void addToDB(FAMIXAccess access){
		val method = access.accessor.ref as FAMIXMethod
		val attribute = access.variable.ref as FAMIXAttribute
		val nodeMethod = nodes.get(method.id)
		val nodeAttribute = nodes.get(attribute.id)
		if (nodeMethod !== null && nodeAttribute !== null) {
			nodeMethod.createRelationshipTo(nodeAttribute, DBRelationships::ACCESSES)
		}
	}
	
	def addToDB(FAMIXNamespace namespace, Node parent) {
		val node = graphDb.createNode(DBLabel::PACKAGE, DBLabel::FAMIXELEMENT)				
		node.setProperty("name", namespace.fqn)
		node.setProperty("fid", namespace.id)
		parent.createRelationshipTo(node, DBRelationships::HAS_PACKAGE)
		return node
	}
	
	def addToDB(FAMIXStructure structure, Node parent) {
		val node = graphDb.createNode(DBLabel.getLabel(structure), DBLabel::FAMIXELEMENT, DBLabel::FAMIXSTRUCTURE)
		node.setProperty("fid", structure.id)
		node.setProperty("name", structure.fqn)
		parent.createRelationshipTo(node, DBRelationships::HAS_STRUCTURE)
		nodes.put(structure.id, node)
		return node
	}
		
	def void addToDB(FAMIXMethod method, Node parent) {
		val node = graphDb.createNode(DBLabel::METHOD, DBLabel::FAMIXELEMENT)
		node.setProperty("fid", method.id)
		node.setProperty("name", method.fqn)
		node.setProperty("cyclomaticComplexity", method.cyclomaticComplexity)
		node.setProperty("numberOfStatements", method.numberOfStatements)
		parent.createRelationshipTo(node, DBRelationships::HAS_METHOD)
		nodes.put(method.id, node)
	}
	
	def void addToDB(FAMIXAttribute attribute, Node parent) {
		val node = graphDb.createNode(DBLabel::ATTRIBUTE, DBLabel::FAMIXELEMENT)
		
		node.setProperty("fid", attribute.id)
		node.setProperty("name", attribute.fqn)
		parent.createRelationshipTo(node, DBRelationships::HAS_ATTRIBUTE)
		nodes.put(attribute.id, node)
	}
	
	def void addToDB(FAMIXEnumValue value, Node parent) {
		val node = graphDb.createNode (DBLabel::ENUMVALUE, DBLabel::FAMIXELEMENT)
		node.setProperty("fid", value.id)
		node.setProperty("name", value.fqn)
		parent.createRelationshipTo(node, DBRelationships::HAS_ATTRIBUTE)
		nodes.put(value.id, node)
	}
}