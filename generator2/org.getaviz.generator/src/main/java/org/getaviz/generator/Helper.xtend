package org.getaviz.generator

//import org.apache.commons.logging.LogFactory
import org.apache.commons.lang3.StringUtils
import org.getaviz.generator.city.m2m.RGBColor
import java.util.List

class Helper {
//	val log = LogFactory::getLog(class)
//	val typesMap = newHashMap('String' -> 128.0, 'byte' -> 8.0, 'short' -> 16.0, 'int' -> 32.0,
//		'long' -> 64.0, 'float' -> 32.0, 'double' -> 64.0, 'boolean' -> 4.0, 'char' -> 16.0)
		
	def getGradient(double value) {
		val red = 255 * value
		val green = 255 * (1 - value)
		val color = new RGBColor(red, green, 0)
		return color
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
	
		
	def removeBrackets(List<String> list) {
		return removeBrackets(list.toString)
	}
	
	
	def removeBrackets(String string) {
		return StringUtils::remove(StringUtils::remove(string, "["), "]")
	}	
	
	def removeApostrophes(String string) {
		return StringUtils::remove(StringUtils::remove(string,"'"),"")
	}
	
	def checkNull(String string) {
		if(string === null) {
			return ""
		}
		return string
	}
}
