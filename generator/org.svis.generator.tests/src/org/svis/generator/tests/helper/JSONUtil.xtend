package org.svis.generator.tests.helper

import net.minidev.json.JSONArray
import com.google.common.base.Splitter
import org.json.simple.parser.JSONParser
import java.io.FileReader

class JSONUtil {
/**
	 * unfortunately our metaData.json consists of something like:
	 * "accesses":	 	 "d000974f136a59142bde8224bcef9590d038eb8a, d000974f136a59142bde8224bcef9590d038eb8a",
	 * 
	 * JSON Path expression will return the accesses as one string. This method creates a list for easier access 
	 * 
	 * @param array JSON array, the result of a JSON path expression
	 * @return Linked list, 
	 */
	
	def static toList(JSONArray array) {
		val result = newLinkedList
		array.forEach[entry|
    		result += Splitter::on(", ").split(entry.toString)
    	]
    	return result
    }
    
    /**
     * Reads data from a JSON file
     * 
     * @param path URI of json file
     * @rutern JSON data
     */
    
    def static read(String path){
    	val parser = new JSONParser()
		val reader = new FileReader(path)
		return parser.parse(reader).toString
    }	
}