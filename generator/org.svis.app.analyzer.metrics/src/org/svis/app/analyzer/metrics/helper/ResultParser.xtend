package org.svis.app.analyzer.metrics.helper

import org.neo4j.graphdb.Result
import java.util.Map

class ResultParser {
	
	def static parsePackage(Result result) {
		val Map<String, String> ret = newLinkedHashMap
		result.forEach[row | 
			val obj = row.get("result")
			var value = obj.toString2
			val id = row.get("id") as String
			ret.put(id, value)
		]
		return ret
	}
	
	def static parsePoint(Result result) {
		val Map<String, Pair<String, String>> ret = newLinkedHashMap
		result.forEach[row | 
			val x = row.get("x").toString2
			val y = row.get("y").toString2
			val id = row.get("id").toString2
			ret.put(id, x -> y)
		]
		return ret
	}
	
	def static toString2 (Object value) {
		if (value === null) {
			return ""
		}
		switch value {
			Double: 	return String::format("%.4f", value)
			Integer: 	return value.toString
			Long: 		return value.toString
			String: 	return value 
			Boolean:	if(value) return "1" else return "0"
		}
	}
	
	def static parsePackage(Result dividend, Result divisor) {
		val Map<String, Long> tmp = newLinkedHashMap
		divisor.forEach[row | 
			val test = row.get("result") as Long
			val id = row.get("id") as String			
			tmp.put(id, test)
		]
		
		val Map<String, String> ret = newLinkedHashMap
		dividend.forEach[row |
			val id = row.get("id") as String 
			var test = row.get("result") as Long
			if (test === null) {
				test = 0l
			}
			
			val String value = toString2(test.doubleValue/tmp.get(id))
			ret.put(id, value)
		]
		return ret
	}
	
	def static getFirst(Pair<String, String> pair) {
		if(pair !== null) {
				return pair.key
			} else {
				return ""
			}
	}
	
	def static getSecond(Pair<String, String> pair) {
		if(pair !== null) {
				return pair.value
			} else {
				return ""
			}
	}
}
