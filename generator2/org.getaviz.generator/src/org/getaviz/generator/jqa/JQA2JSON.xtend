package org.getaviz.generator.jqa

import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.util.List
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.SettingsConfiguration
import org.getaviz.lib.database.Database
import org.getaviz.lib.database.Labels
import org.getaviz.lib.database.Rels
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4

class JQA2JSON {
	val graph = Database::instance
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)

	new () {
		log.info("JQA2JSON has started.")
		val elements = newArrayList
		graph.execute("MATCH (n:Condition) RETURN n").forEach[elements.add(get("n") as Node)]
		graph.execute("MATCH (n)<-[:VISUALIZES]-() RETURN n").forEach[elements.add(get("n") as Node)]
		val tx = graph.beginTx
		var Writer fw = null
		try {
			val path = config.outputPath + "metaData.json"
			fw = new FileWriter(path)
			println("Elements: " + elements.size.toString)
			fw.write(elements.toJSON)
			tx.success
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (fw !== null)
				try {
					fw.close;
				} catch (IOException e) {
					e.printStackTrace;
				}
			tx.close
		}
		log.info("JQA2JSON has finished.")
	}	
	
	private def String toJSON (List<Node> list)  '''
		«FOR el : list BEFORE "[{" SEPARATOR "\n},{" AFTER "}]"»
			«IF el.hasLabel(Labels.Package)»
			«toMetaDataNamespace(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.Class) || el.hasLabel(Labels.Interface)»
			«toMetaDataClass(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Type) && el.hasLabel(Labels.Annotation)»
			«toMetaDataAnnotation(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Type) && el.hasLabel(Labels.Enum)»
			«toMetaDataEnum(el)»
			«ENDIF»				
			«IF el.hasLabel(Labels.Method)»
			«toMetaDataMethod(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Field) && !el.hasLabel(Labels.Enum)»
			«toMetaDataAttribute(el)»
			«ENDIF»		
			«IF el.hasLabel(Labels.Field) && el.hasLabel(Labels.Enum)»
			«toMetaDataEnumValue(el)»
			«ENDIF»	
			«IF el.hasLabel(Labels.TranslationUnit)»
			«toMetaDataTranslationUnit(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.Function)»
			«toMetaDataFunction(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.Variable)»
			«toMetaDataVariable(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.SingleCondition)»
			«toMetaDataSingleCondition(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.Not)»
			«toMetaDataNegation(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.And)»
			«toMetaDataAnd(el)»
			«ENDIF»
			«IF el.hasLabel(Labels.Or)»
			«toMetaDataOr(el)»
			«ENDIF»
		«ENDFOR»
	'''
	
	private def toMetaDataNamespace(Node namespace) {
		val parent = namespace.getRelationships(Rels.CONTAINS, Direction.INCOMING).filter[startNode.hasLabel(Labels.Package)].head
		var belongsTo = "root"
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}
		val result = '''
		"id":            "«namespace.getProperty("hash")»",
		"qualifiedName": "«namespace.getProperty("fqn")»",
		"name":          "«namespace.getProperty("name")»",
		"type":          "FAMIX.Namespace",
		"belongsTo":     "«belongsTo»"
	'''	
		return result
	}
	
	def private toMetaDataClass(Node c) {
		var belongsTo = ""
		var parent = c.getRelationships(Rels.DECLARES, Direction.INCOMING).filter[startNode.hasLabel(Labels.Type)].head
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash", "XXX") as String
		} else {
			parent = c.getRelationships(Rels.CONTAINS, Direction.INCOMING).filter[startNode.hasLabel(Labels.Package)].head
			belongsTo = parent.startNode.getProperty("hash", "YYY") as String
		}
		val result = '''
		"id":            "«c.getProperty("hash")»",
		"qualifiedName": "«c.getProperty("fqn")»",
		"name":          "«c.getProperty("name")»",
		"type":          "FAMIX.Class",
		"modifiers":     "«c.modifiers»",
		"subClassOf":    "«c.superClasses»",
		"superClassOf":  "«c.subClasses»",
		"belongsTo":     "«belongsTo»"
	'''	
		return result
	}
	
	def private toMetaDataAttribute(Node attribute) {
		var belongsTo = ""
		var declaredType = ""
		val parent = attribute.getRelationships(Direction.INCOMING, Rels.CONTAINS, Rels.DECLARES).head
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}		
		val type = attribute.getSingleRelationship(Rels.OF_TYPE, Direction.OUTGOING)
		if(type !== null) {
			declaredType = type.startNode.getProperty("name") as String
		}				
		val result = '''
		"id":            "«attribute.getProperty("hash")»",
		"qualifiedName": "«attribute.getProperty("fqn")»",
		"name":          "«attribute.getProperty("name")»",
		"type":          "FAMIX.Attribute",
		"modifiers":     "«attribute.getModifiers»",
		"declaredType":  "«declaredType»",
		"accessedBy":	 "«attribute.getAccessedBy»",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	def private toMetaDataMethod(Node method) {
		var belongsTo = ""
		val parent = method.getSingleRelationship(Rels.DECLARES, Direction.INCOMING)
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}		
		var signature = method.getProperty("signature") as String
		if(signature.contains(".")) {
			val lBraceIndex = signature.indexOf("(")
			signature = signature.substring(0,lBraceIndex + 1) + method.getParameters + ")"
		}
		val result = '''
		"id":            "«method.getProperty("hash")»",
		"qualifiedName": "«escapeHtml4(method.getProperty("fqn") as String)»",
		"name":          "«method.getProperty("name")»",
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
		var belongsTo = ""
		val parent = e.getSingleRelationship(Rels.DECLARES, Direction.INCOMING)
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}			
		val result = '''
		"id":            "«e.getProperty("hash")»",
		"qualifiedName": "«e.getProperty("fqn")»",
		"name":          "«e.getProperty("name")»",
		"type":          "FAMIX.Enum",
		"modifiers":     "«e.modifiers»",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	def private toMetaDataEnumValue(Node ev) {
		var belongsTo = ""
		val parent = ev.getSingleRelationship(Rels.DECLARES, Direction.INCOMING)
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}	
		val result = '''	
		"id":            "«ev.getProperty("hash")»",
		"qualifiedName": "«ev.getProperty("fqn")»",
		"name":          "«ev.getProperty("name")»",
		"type":          "FAMIX.EnumValue",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	def private toMetaDataAnnotation(Node annotation) {
		var belongsTo = ""
		val parent = annotation.getRelationships(Direction.INCOMING, Rels.CONTAINS, Rels.DECLARES).filter[hasProperty("Package")].head
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}			
		val result = '''
		"id":            "«annotation.getProperty("hash")»",
		"qualifiedName": "«annotation.getProperty("fqn")»",
		"name":          "«annotation.getProperty("name")»",
		"type":          "FAMIX.AnnotationType",
		"modifiers":     "«annotation.modifiers»",
		"subClassOf":    "",
		"superClassOf":  "",
		"belongsTo":     "«belongsTo»"
	'''
		return result
	}
	
	private def toMetaDataTranslationUnit(Node translationUnit) {
		var belongsTo = "root"
		val result = '''
		"id":            "«translationUnit.getProperty("hash")»",
		"qualifiedName": "«translationUnit.getProperty("fqn")»",
		"name":          "«translationUnit.getProperty("name")»",
		"type":          "TranslationUnit",
		"belongsTo":     "«belongsTo»"
		'''	
		return result
	}
	
	private def toMetaDataFunction(Node function) {
		var belongsTo = ""
		var dependsOn = ""
		val parent = function.getSingleRelationship(Rels.DECLARES, Direction.INCOMING)
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}	
		var dependent = function.getRelationships(Direction.OUTGOING, Rels.DEPENDS_ON).head
		if(dependent !== null){
			dependsOn = dependent.endNode.getProperty("hash") as String
		}	
		
		val result = '''
		"id":            "«function.getProperty("hash")»",
		"qualifiedName": "«escapeHtml4(function.getProperty("fqn") as String)»",
		"name":          "«function.getProperty("name")»",
		"type":          "FAMIX.Function",
		"modifiers":     "",
		"signature":  	 "«function.getFunctionSignature»",
		"calls":		 "",
		"calledBy":		 "",
		"accesses":	 	 "",
		"belongsTo":     "«belongsTo»",
		"dependsOn":     "«dependsOn»"
		'''
		return result
	}
	
	def toMetaDataVariable(Node variable) {
		var belongsTo = ""
		var declaredType = ""
		var dependsOn = ""
		val parent = variable.getRelationships(Direction.INCOMING, Rels.DECLARES).head
		if(parent !== null) {
			belongsTo = parent.startNode.getProperty("hash") as String
		}		
		val type = variable.getSingleRelationship(Rels.OF_TYPE, Direction.OUTGOING)
		if(type !== null) {
			declaredType = type.endNode.getProperty("name") as String
		}	
		var dependent = variable.getRelationships(Direction.OUTGOING, Rels.DEPENDS_ON).head
		if(dependent !== null){
			dependsOn = dependent.endNode.getProperty("hash") as String
		}
					
		val result = '''
		"id":            "«variable.getProperty("hash")»",
		"qualifiedName": "«variable.getProperty("fqn")»",
		"name":          "«variable.getProperty("name")»",
		"type":          "FAMIX.Variable",
		"declaredType":  "«declaredType»",
		"belongsTo":     "«belongsTo»",
		"dependsOn":     "«dependsOn»"
		'''
		return result
	}
	
	def toMetaDataSingleCondition(Node singleCondition) {
		val result = '''
		"id":            "«singleCondition.getProperty("hash")»",
		"qualifiedName": "«singleCondition.getProperty("fqn")»",
		"name":     	 "«singleCondition.getProperty("MacroName")»",
		"type":          "Macro"
		'''
		return result
	}
	
	def toMetaDataNegation(Node negation) {
		var negated = ""
		val negatedNode = negation.getRelationships(Direction.OUTGOING, Rels.NEGATES).head.endNode
		if(negatedNode !== null) {
			negated = negatedNode.getProperty("hash") as String
		}
		
		val result = '''
		"id":            "«negation.getProperty("hash")»",
		"type":          "Negation",
		"negated":		 "«negated»"
		'''
		return result
	}
	
	def toMetaDataAnd(Node andNode) {
		val connectedConditions = newArrayList
		val connections = andNode.getRelationships(Direction.OUTGOING, Rels.CONNECTS)
		connections.forEach[
			if(endNode.hasProperty("hash")){
				connectedConditions += endNode.getProperty("hash") as String
			}
		]
		
		val result = '''
		"id":            "«andNode.getProperty("hash")»",
		"type":          "And",
		"connected":	 "«connectedConditions.removeBrackets»"
		'''
		return result
	}
	
	def toMetaDataOr(Node orNode) {
		val connectedConditions = newArrayList
		val connections = orNode.getRelationships(Direction.OUTGOING, Rels.CONNECTS)
		connections.forEach[
			if(endNode.hasProperty("hash")){
				connectedConditions += endNode.getProperty("hash") as String
			}
		]
		
		val result = '''
		"id":            "«orNode.getProperty("hash")»",
		"type":          "Or",
		"connected":	 "«connectedConditions.removeBrackets»"
		'''
		return result
	}
					
	def private getSuperClasses(Node element) {
		val superClasses = element.getRelationships(Rels.EXTENDS, Direction.OUTGOING)
		val tmp = newArrayList
		superClasses.forEach[
			if(endNode.hasProperty("hash")) {
				tmp += endNode.getProperty("hash") as String
			}
		]
		return tmp.removeBrackets
	}	
	
	def private getSubClasses(Node element) {
		val subClasses = element.getRelationships(Rels.EXTENDS, Direction.INCOMING)
		val tmp = newArrayList
		subClasses.forEach[
			if(startNode.hasProperty("hash")) {
				tmp += startNode.getProperty("hash") as String
			}
		]
		return tmp.removeBrackets
	}		
	
	def private getAccessedBy(Node element) {
		val accesses = element.getRelationships(Direction.INCOMING, Rels.WRITES, Rels.READS)
		val tmp = newArrayList
		accesses.forEach[
			if(startNode.hasProperty("hash")) {
				tmp += startNode.getProperty("hash") as String
			}
		]
		return tmp.removeBrackets
	}		
	
	def private getAccesses(Node element) {
		val accesses = element.getRelationships(Direction.OUTGOING, Rels.WRITES, Rels.READS)
		val tmp = newArrayList
		accesses.forEach[
			if(endNode.hasProperty("hash")) {
				tmp += endNode.getProperty("hash") as String
			}				
		]
		return tmp.removeBrackets
	}		
		
	def private getCalls(Node element) {
		val calls = element.getRelationships(Direction.OUTGOING, Rels.INVOKES)
		val tmp = newArrayList
		calls.forEach[
			if(endNode.hasProperty("hash")) {
				tmp += endNode.getProperty("hash") as String
			}			
		]
		return tmp.removeBrackets
	}		
	
	def private getCalledBy(Node element) {
		val calls = element.getRelationships(Direction.INCOMING, Rels.INVOKES)
		val tmp = newArrayList
		calls.forEach[
			if(startNode.hasProperty("hash")) {
				tmp += startNode.getProperty("hash") as String
			}			
		]
		return tmp.removeBrackets
	}		
				
	def private getModifiers(Node element) {
		val tmp = newArrayList
		if (element.hasProperty("visibility")) {
			tmp += element.getProperty("visibility") as String
		}
		if (element.hasProperty("final")) {
			if (element.getProperty("final") === true) {
				tmp += "final"
			}
		}
		if (element.hasProperty("abstract")) {
			if (element.getProperty("abstract") === true) {
				tmp += "abstract"
			}
		}
		if (element.hasProperty("static")) {
			tmp += "static"
		}
		return tmp.removeBrackets
	}
	
	def private getParameters(Node method) {
		val parameterList = newArrayList
		val list = method.getRelationships(Rels.HAS, Direction.OUTGOING).map[endNode];
		list.filter[hasLabel(Labels.Parameter)].sortBy[p|p.getProperty("index", 0) as Integer].forEach[p|
			try {
				parameterList += p.getSingleRelationship(Rels.OF_TYPE, Direction.OUTGOING).endNode.getProperty("name") as String
			} catch (NullPointerException e) {
				
			}
		]
		return parameterList.removeBrackets
	}
	
	def removeBrackets(String[] array) {
		return removeBrackets(array.toString)
	}
	
	def removeBrackets(String string) {
		return StringUtils::remove(StringUtils::remove(string, "["), "]")
	}
	
	def private getFunctionSignature(Node function){
		var signature = ""
		var returnType = ""
		var typeList = function.getRelationships(Direction.OUTGOING, Rels.RETURNS).map[endNode]
		for(type : typeList){
			returnType += type.getProperty("name")
		}
		if(!returnType.endsWith("*")){
			returnType += " "
		}
		val functionName = function.getProperty("name")
		var parameterList = function.getRelationships(Direction.OUTGOING, Rels.HAS).map[endNode]
		//sort parameters according to their index
		parameterList = parameterList.sortBy[p|p.getProperty("index", 0) as Integer]
		var parameters = getFunctionParameters(parameterList)
		//build signature
		signature = returnType + functionName + "(" + parameters + ")"
		
		return signature
	}	
	
	def private getFunctionParameters(Iterable<Node> parameterList){
		var parameters = ""
		var counter = 0
		for(parameter: parameterList){
			//add comma after parameter
			if(counter != 0){
				parameters += ", "
			}
			var parameterTypeList = parameter.getRelationships(Direction.OUTGOING, Rels.OF_TYPE).map[endNode]
			var parameterTypeString = ""
			for(parameterType : parameterTypeList){
				parameterTypeString += parameterType.getProperty("name")
			}
			
			//put [] after the parameter name
			if(parameterTypeString.endsWith("]")){
				var String[] parts = parameterTypeString.split("\\[", 2)
				parameters += parts.get(0) + parameter.getProperty("name") + "[" + parts.get(1) 
			} else {
				parameters += parameterTypeString + " " + parameter.getProperty("name")
			}
			counter++
		}
		
		return parameters
	}
}