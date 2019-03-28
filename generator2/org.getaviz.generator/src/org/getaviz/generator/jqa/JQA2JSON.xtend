package org.getaviz.generator.jqa

import java.io.Writer
import java.io.FileWriter
import java.io.IOException
import java.util.List
import org.apache.commons.lang3.StringUtils
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4
import org.getaviz.generator.database.Labels
import org.getaviz.generator.SettingsConfiguration
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node

class JQA2JSON {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	val connector = DatabaseConnector::instance

	new () {
		log.info("JQA2JSON has started.")
		val elements = newArrayList
		connector.executeRead("MATCH (n)<-[:VISUALIZES]-() RETURN n").forEach[elements.add(get("n").asNode)]
		var Writer fw = null
		try {
			val path = config.outputPath + "metaData.json"
			fw = new FileWriter(path)
			fw.write(elements.toJSON)
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (fw !== null)
				try {
					fw.close;
				} catch (IOException e) {
					e.printStackTrace;
				}
		}
		log.info("JQA2JSON has finished.")
	}	
	
	private def String toJSON (List<Node> list)  '''
		«FOR el : list BEFORE "[{" SEPARATOR "\n},{" AFTER "}]"»
			«IF el.hasLabel(Labels.Package.name)»
			«toMetaDataNamespace(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.Class.name) || el.hasLabel(Labels.Interface.name)»
			«toMetaDataClass(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Type.name) && el.hasLabel(Labels.Annotation.name)»
			«toMetaDataAnnotation(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Type.name) && el.hasLabel(Labels.Enum.name)»
			«toMetaDataEnum(el)»
			«ENDIF»				
			«IF el.hasLabel(Labels.Method.name)»
			«toMetaDataMethod(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Field.name) && !el.hasLabel(Labels.Enum.name)»
			«toMetaDataAttribute(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Field.name) && el.hasLabel(Labels.Enum.name)»
			«toMetaDataEnumValue(el)»
			«ENDIF»								
		«ENDFOR»
	'''	
	
	private def toMetaDataNamespace(Node namespace) {
		log.info("Namespace")
		val parentHash = connector.executeRead("MATCH (parent:Package)-[:CONTAINS]->(namespace) WHERE ID(namespace) = " + namespace.id 
			+ " RETURN parent.hash")
		var belongsTo = "root"
		if(parentHash.hasNext) {
			belongsTo = parentHash.single.get("hash").asString
		}
		val result = '''
		"id":            "«namespace.get("hash").asString»",
		"qualifiedName": "«namespace.get("fqn").asString»",
		"name":          "«namespace.get("name").asString»",
		"type":          "FAMIX.Namespace",
		"belongsTo":     "«belongsTo»"
	'''	
		return result
	}
	
	def private toMetaDataClass(Node c) {
		log.info("Class")
		var belongsTo = ""
		var parent = connector.executeRead("MATCH (parent:Type)-[:DECLARES]->(class) WHERE ID(class) = " + c.id 
			+ " RETURN parent")
		if(parent.hasNext) {
			belongsTo = parent.single.get("parent").asNode.get("hash").asString("XXX")
		} else {
			parent = connector.executeRead("MATCH (parent:Package)-[:CONTAINS]->(class) WHERE ID(class) = " + c.id 
			+ " RETURN parent")
			belongsTo = parent.single.get("parent").asNode.get("hash").asString("YYY")
		}
		val result = '''
		"id":            "«c.get("hash").asString»",
		"qualifiedName": "«c.get("fqn").asString»",
		"name":          "«c.get("name").asString»",
		"type":          "FAMIX.Class",
		"modifiers":     "«c.modifiers»",
		"subClassOf":    "«c.superClasses»",
		"superClassOf":  "«c.subClasses»",
		"belongsTo":     "«belongsTo»"
	'''	
		return result
	}
	
	def private toMetaDataAttribute(Node attribute) {
		log.info("Attribute")
		var belongsTo = ""
		var declaredType = ""
		var parent = connector.executeRead("MATCH (parent)-[:CONTAINS|DECLARES]->(attribute) WHERE ID(attribute) = " + attribute.id 
			+ " RETURN parent.hash")
		if(parent.hasNext) {
			belongsTo = parent.single.get("hash").asString
		}	
		val type = connector.executeRead("MATCH (attribute)-[:OF_TYPE]->(t) WHERE ID(attribute) = " + attribute.id 
			+ " RETURN t").next.get("t").asNode
		if(type !== null) {
			declaredType = type.get("name").asString
		}				
		val result = '''
		"id":            "«attribute.get("hash").asString»",
		"qualifiedName": "«attribute.get("fqn").asString»",
		"name":          "«attribute.get("name").asString»",
		"type":          "FAMIX.Attribute",
		"modifiers":     "«attribute.getModifiers»",
		"declaredType":  "«declaredType»",
		"accessedBy":	 "«attribute.getAccessedBy»",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	def private toMetaDataMethod(Node method) {
		log.info("Method: " + method.id)
		var belongsTo = ""
		val parent = connector.executeRead("MATCH (parent)-[:DECLARES]->(method) WHERE ID(method) = " + method.id 
			+ " RETURN parent.hash")
		if(parent.hasNext) {
			belongsTo = parent.single.get("hash").asString
		}		
		var signature = method.get("signature").asString
		if(signature.contains(".")) {
			val lBraceIndex = signature.indexOf("(")
			signature = signature.substring(0,lBraceIndex + 1) + method.getParameters + ")"
		}
		val result = '''
		"id":            "«method.get("hash").asString»",
		"qualifiedName": "«escapeHtml4(method.get("fqn").asString)»",
		"name":          "«method.get("name").asString»",
		"type":          "FAMIX.Method",
		"modifiers":     "«method.modifiers»",
		"signature":  	 "«signature»",
		"calls":		 "«method.getCalls»",
		"calledBy":		 "«method.getCalledBy»",
		"accesses":	 	 "«method.getAccesses»",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	def private toMetaDataEnum(Node e) {
		log.info("Enum")
		var belongsTo = ""
		val parent = connector.executeRead("MATCH (parent)-[:DECLARES]->(enum) WHERE ID(enum) = " + e.id 
			+ " RETURN parent.hash")
		if(parent.hasNext) {
			belongsTo = parent.single.get("hash").asString
		}		
		val result = '''
		"id":            "«e.get("hash").asString»",
		"qualifiedName": "«e.get("fqn").asString»",
		"name":          "«e.get("name").asString»",
		"type":          "FAMIX.Enum",
		"modifiers":     "«e.modifiers»",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	def private toMetaDataEnumValue(Node ev) {
		log.info("EnumValue")
		var belongsTo = ""
		val parent = connector.executeRead("MATCH (parent)-[:DECLARES]->(enumValue) WHERE ID(enumValue) = " + ev.id 
			+ " RETURN parent.hash")
		if(parent.hasNext) {
			belongsTo = parent.single.get("hash").asString
		}
		val result = '''	
		"id":            "«ev.get("hash").asString»",
		"qualifiedName": "«ev.get("fqn").asString»",
		"name":          "«ev.get("name").asString»",
		"type":          "FAMIX.EnumValue",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	def private toMetaDataAnnotation(Node annotation) {
		log.info("Annotation")
		var belongsTo = ""
		val parent = connector.executeRead("MATCH (parent:Package)-[:CONTAINS|DECLARES]->(annotation) WHERE ID(annotation) = " + annotation.id 
			+ " RETURN parent.hash")
		if(parent.hasNext) {
			belongsTo = parent.single.get("hash").asString
		}		
		val result = '''
		"id":            "«annotation.get("hash").asString»",
		"qualifiedName": "«annotation.get("fqn").asString»",
		"name":          "«annotation.get("name").asString»",
		"type":          "FAMIX.AnnotationType",
		"modifiers":     "«annotation.modifiers»",
		"subClassOf":    "",
		"superClassOf":  "",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
					
	def private getSuperClasses(Node element) {
		val tmp = newArrayList
		connector.executeRead("MATCH (super:Type)<-[:EXTENDS]-(element) WHERE ID(element) = " + element.id + " RETURN super").forEach[
			val node = get("super").asNode
			if(node.containsKey("hash")) {
				tmp += node.get("hash").asString
			}
		]
		return tmp.removeBrackets
	}	
	
	def private getSubClasses(Node element) {
		val tmp = newArrayList
		connector.executeRead("MATCH (sub:Type)-[:EXTENDS]->(element) WHERE ID(element) = " + element.id + " RETURN sub").forEach[
			val node = get("sub").asNode
			if(node.containsKey("hash")) {
				tmp += node.get("hash").asString
			}
		]
		return tmp.removeBrackets
	}		
	
	def private getAccessedBy(Node element) {
		val tmp = newArrayList
		connector.executeRead("MATCH (access)-[:WRITES|READS]->(element) WHERE ID(element) = " + element.id + " RETURN access").forEach[
			val node = get("access").asNode
			if(node.containsKey("hash")) {
				tmp += node.get("hash").asString
			}			
		]
		return tmp.removeBrackets
	}		
	
	def private getAccesses(Node element) {
		val tmp = newArrayList
		connector.executeRead("MATCH (access)<-[:WRITES|READS]-(element) WHERE ID(element) = " + element.id + " RETURN access").forEach[
			val node = get("access").asNode
			if(node.containsKey("hash")) {
				tmp += node.get("hash").asString
			}			
		]
		return tmp.removeBrackets
	}		
		
	def private getCalls(Node element) {
		val tmp = newArrayList
		connector.executeRead("MATCH (element)-[:INVOKES]->(call) WHERE ID(element) = " + element.id + " RETURN call").forEach[
			val node = get("call").asNode
			if(node.containsKey("hash")) {
				tmp += node.get("hash").asString
			}
		]
		return tmp.removeBrackets
	}		
	
	def private getCalledBy(Node element) {
		val tmp = newArrayList
		connector.executeRead("MATCH (element)<-[:INVOKES]-(call) WHERE ID(element) = " + element.id + " RETURN call").forEach[
			val node = get("call").asNode
			if(node.containsKey("hash")) {
				tmp += node.get("hash").asString
			}
		]		
		return tmp.removeBrackets
	}		
				
	def private getModifiers(Node element) {
		val tmp = newArrayList
		if (element.containsKey("visibility")) {
			tmp += element.get("visibility").asString
		}
		if (element.containsKey("final")) {
			if (element.get("final").asBoolean === true) {
				tmp += "final"
			}
		}
		if (element.containsKey("abstract")) {
			if (element.get("abstract").asBoolean === true) {
				tmp += "abstract"
			}
		}
		if (element.containsKey("static")) {
			tmp += "static"
		}
		return tmp.removeBrackets
	}
	
	def private getParameters(Node method) {
		val parameterList = newArrayList
		connector.executeRead("MATCH (method)-[:HAS]->(p:Parameter) WHERE ID(method) = " + method.id + " RETURN p ORDER BY p.index ASC").forEach[
			val parameter = get("p").asNode
			connector.executeRead("MATCH (parameter)-[:OF_TYPE]->(t) WHERE ID(parameter) = " + parameter.id + " RETURN t.name").forEach[
				parameterList += get("name").asString
			]
		]
		return parameterList.removeBrackets
	}
	
	def removeBrackets(String[] array) {
		return removeBrackets(array.toString)
	}
	
	def removeBrackets(String string) {
		return StringUtils::remove(StringUtils::remove(string, "["), "]")
	}	
}