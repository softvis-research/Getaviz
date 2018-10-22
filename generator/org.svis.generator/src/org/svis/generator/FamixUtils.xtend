package org.svis.generator

import org.svis.xtext.famix.FAMIXAnnotationType
import org.svis.xtext.famix.FAMIXAttribute
import org.svis.xtext.famix.FAMIXClass
import org.svis.xtext.famix.FAMIXElement
import org.svis.xtext.famix.FAMIXEnum
import org.svis.xtext.famix.FAMIXFileAnchor
import org.svis.xtext.famix.FAMIXMethod
import org.svis.xtext.famix.FAMIXNamespace
import org.svis.xtext.famix.FAMIXParameterizableClass
import org.svis.xtext.famix.FAMIXParameterizedType
import org.svis.xtext.famix.FAMIXPrimitiveType
import org.svis.xtext.rd.DiskSegment
import org.eclipse.emf.common.util.EList
import org.apache.commons.logging.LogFactory
import org.apache.commons.lang3.StringUtils
import org.svis.xtext.famix.FAMIXStructure
import org.svis.xtext.famix.impl.FAMIXAttributeImpl
import org.svis.xtext.famix.impl.FAMIXMethodImpl
import org.svis.xtext.famix.FAMIXAntipattern
import org.svis.xtext.famix.FAMIXComponent
import org.svis.xtext.famix.FAMIXPath
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.generator.rd.m2m.RGBColor

//ABAP
import org.svis.xtext.famix.FAMIXReport 
import org.svis.xtext.famix.FAMIXDataElement
import org.svis.xtext.famix.FAMIXDomain
import org.svis.xtext.famix.FAMIXTable
import org.svis.xtext.famix.FAMIXTableElement
import org.svis.xtext.famix.FAMIXTableType
import org.svis.xtext.famix.FAMIXABAPStruc
import org.svis.xtext.famix.FAMIXStrucElement
import org.svis.xtext.famix.FAMIXFunctionGroup
import org.svis.xtext.famix.FAMIXFunctionModule
import org.svis.xtext.famix.FAMIXFormroutine
import org.svis.xtext.famix.FAMIXMessageClass 


class FamixUtils {
	val log = LogFactory::getLog(class)
	val typesMap = newHashMap('String' -> 128.0, 'byte' -> 8.0, 'short' -> 16.0, 'int' -> 32.0,
		'long' -> 64.0, 'float' -> 32.0, 'double' -> 64.0, 'boolean' -> 4.0, 'char' -> 16.0)
		
	def getGradient(double value) {
		val red = 255 * value
		val green = 255 * (1 - value)
		val color = new RGBColor(red, green, 0)
		return color
	}
	
	def getBlueGradient(double value){
		val red = 202 * value
		val green = 202 * value
		val color = new RGBColor(red, green, 206)
		return color
	}

	def getAttributeSize(FAMIXElement element) {
		switch(element) {
			FAMIXPrimitiveType: return typesMap.get(element.value)
			FAMIXParameterizableClass,
			FAMIXClass: return 100
			default: return 100
		}
	}

	def sum(EList<DiskSegment> segments) {
		var sum = 0.0
		for (segment : segments) {
			sum += segment.size
		}
		return sum
	}
		/**
	 * Creates hash as (hopefully) unique ID for every FAMIXElement
	 * 
	 * @param fqn full qualified name of FAMIXElement
	 * @return  sha1 hash
	 *  
	 */
		
	def removeBrackets(String[] array) {
		return removeBrackets(array.toString)
	}
	
	def removeBrackets(String string) {
		return StringUtils::remove(StringUtils::remove(string, "["), "]")
	}	
	
	def removeApostrophes(String string) {
		return StringUtils::remove(StringUtils::remove(string,"'"),"")
	}
	
	def getTypeString(FAMIXStructure el) {
		switch el {
			FAMIXClass:	 				return "FAMIX.Class"
			FAMIXParameterizableClass: 	return "FAMIX.ParameterizableClass"
			FAMIXEnum: 					return "FAMIX.Enum"
			FAMIXAnnotationType: 		return "FAMIX.AnnotationType"
			FAMIXMessageClass:		    return "FAMIX.MessageClass"
			FAMIXFunctionGroup:			return "FAMIX.FunctionGroup"
		}
	}
	
	def int loc(FAMIXMethodImpl fm) {
		(fm.sourceAnchor.ref as FAMIXFileAnchor).endline - (fm.sourceAnchor.ref as FAMIXFileAnchor).startline
	}

	def int loc(FAMIXAttributeImpl fm) {
		(fm.sourceAnchor.ref as FAMIXFileAnchor).endline - (fm.sourceAnchor.ref as FAMIXFileAnchor).startline
	}

	def int loc(FAMIXClass famixClass) {
		(famixClass.type.ref as FAMIXFileAnchor).endline - (famixClass.type.ref as FAMIXFileAnchor).startline
	}

	def int loc(FAMIXEnum fe) {
		(fe.sourceAnchor.ref as FAMIXFileAnchor).endline - (fe.sourceAnchor.ref as FAMIXFileAnchor).startline
	}

	def int loc(FAMIXParameterizableClass famixPClass) {
		(famixPClass.type.ref as FAMIXFileAnchor).endline - (famixPClass.type.ref as FAMIXFileAnchor).startline
	}
	
	def getId (FAMIXElement el) {
		switch el {
			FAMIXNamespace: 			return el.id
			FAMIXClass:					return el.id
			FAMIXParameterizableClass: 	return el.id
			FAMIXMethod:				return el.id
			FAMIXAttribute:				return el.id
			FAMIXEnum:					return el.id
			FAMIXParameterizedType:		return el.id
			FAMIXAnnotationType:		return el.id
			FAMIXAntipattern:			return el.id
			FAMIXComponent:				return el.id
			FAMIXPath:					return el.id
			HISMOClassVersion:			return el.id
			HISMONamespaceVersion:		return el.id
			
			FAMIXReport:				return el.id
			FAMIXDataElement:			return el.id
			FAMIXDomain:				return el.id
			FAMIXTable:					return el.id
			FAMIXTableElement:			return el.id
			FAMIXTableType:				return el.id
			FAMIXABAPStruc:				return el.id
			FAMIXStrucElement:			return el.id
			FAMIXFunctionGroup:			return el.id
			FAMIXFunctionModule:		return el.id
			FAMIXFormroutine:			return el.id
			FAMIXMessageClass:			return el.id
			default: log.warn("Forgot" + el.class + " in FamixUtils.getId")
		}
	}
	
	def getFqn(FAMIXElement el) {
		switch el {
			FAMIXNamespace: 			return el.fqn
			FAMIXClass:					return el.fqn
			FAMIXParameterizableClass: 	return el.fqn
			FAMIXMethod:				return el.fqn
			FAMIXAttribute:				return el.fqn
			FAMIXEnum:					return el.fqn
			FAMIXParameterizedType:		return el.fqn
			FAMIXAnnotationType:		return el.fqn
			FAMIXComponent:				return el.fqn
			FAMIXAntipattern:			return el.fqn
			
			FAMIXReport:				return el.fqn
			FAMIXDataElement:			return el.fqn
			FAMIXDomain:				return el.fqn
			FAMIXTable:					return el.fqn
			FAMIXTableElement:			return el.fqn
			FAMIXTableType:				return el.fqn
			FAMIXABAPStruc:				return el.fqn
			FAMIXStrucElement:			return el.fqn
			FAMIXFunctionGroup:			return el.fqn
			FAMIXFunctionModule:		return el.fqn
			FAMIXFormroutine:			return el.fqn
			FAMIXMessageClass:			return el.fqn
			default: log.warn("Forgot" + el.class + " in FamixUtils.getFqn")
		}
	}
	
	def checkNull(String string) {
		if(string === null) {
			return ""
		}
		return string
	}
}
