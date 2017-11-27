package org.svis.generator

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import org.svis.lib.repository.repo.api.impl.RepositoryFactorys.RepositoryType
import org.eclipse.xtend.lib.annotations.Accessors
import java.io.File
import org.json.simple.parser.JSONParser
import java.io.FileReader
import org.json.simple.JSONObject
import java.io.FileNotFoundException
import org.apache.commons.logging.LogFactory
import java.io.FileWriter
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex
import java.util.List
import org.json.simple.JSONArray

class Configuration {
	@Accessors String repositoryUrl = ""
	@Accessors String repositoryName = ""
	@Accessors String visualisationName = ""
	@Accessors String repositoryOwner = ""
	@Accessors String commit = ""
	@Accessors String systemID = ""
	@Accessors String snapshotID = ""
	@Accessors int commitOrder = 0
	@Accessors int numberOfCommits = 100
	@Accessors(PUBLIC_GETTER) RepositoryType repositoryType
	@Accessors(PUBLIC_GETTER) Date startDate
	@Accessors(PUBLIC_GETTER) Date endDate
	@Accessors Boolean recreateFamix = true
	@Accessors Boolean recreateHismo = true
	@Accessors String outputDirectory
	@Accessors String outputDirectorySystem
	@Accessors String outputDirectoryFamix
	@Accessors String language = ""
	@Accessors String path
	val log = LogFactory::getLog(class)
	val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
	@Accessors List<String> commits = newLinkedList
	
	// --------------------------
	// Package:
	@Accessors private String packageShape
	@Accessors private Boolean packageUseTextures
	@Accessors private String packageTexture
	@Accessors private String packageColor  
	// --------------------------
	// Class:
	@Accessors private String classSize
	@Accessors private String classShape
	@Accessors private Boolean classUseTextures	
	@Accessors private String classTexture
	@Accessors private String classColor
	// --------------------------
	// Attribute:
	@Accessors private Boolean showAttributes
	@Accessors private String attributeShape
	@Accessors private Boolean attributeUseTextures
	@Accessors private String attributeTexture
	@Accessors private String attributeColor
	// --------------------------
	// Method:
	@Accessors public Boolean showMethods
	@Accessors private String methodShape
	@Accessors private Boolean methodUseTextures	
	@Accessors private String methodTexture
	@Accessors private String methodColor
	
	new() {
		repositoryType = RepositoryType.GIT
		
		createDirectories
	}

	new (String name){
		repositoryType = RepositoryType.GIT
		repositoryName = name
		createDirectories
	}
	
	new(FileReader reader) {
		repositoryType = RepositoryType.GIT
		val parser = new JSONParser()
		try {
			val jsonObject = parser.parse(reader) as JSONObject
			println(jsonObject.toString)
			reader.close
			commit = jsonObject.getOrDefault("commit", "") as String
			val long c = 0
			commitOrder = (jsonObject.getOrDefault("commitOrder", c)  as Long).intValue
		  	repositoryName = jsonObject.get("name") as String
		  	repositoryUrl = jsonObject.get("url") as String
		  	repositoryType = RepositoryType.valueOf(jsonObject.get("type") as String)
		  	startDate = jsonObject.get("startDate") as String
		  	endDate = jsonObject.get("endDate") as String
		  	repositoryOwner = jsonObject.get("owner") as String
		  	recreateFamix = jsonObject.getOrDefault("recreateFamix", true) as Boolean
		  	recreateHismo = jsonObject.getOrDefault("recreateHismo", true) as Boolean
		  	val long noc = 100
		  	numberOfCommits = (jsonObject.getOrDefault("numberOfCommits", noc) as Long).intValue
		  	language = jsonObject.get("language") as String
		   	var array = jsonObject.get("commits") as JSONArray
		   	
		  	if (array !== null) {
		  		array.forEach[entry|
		  			commits.add(entry as String)
		  		]
			}
			
			// Package:
			packageShape = jsonObject.getOrDefault("packageShape", packageShape) as String
			packageUseTextures = jsonObject.getOrDefault("packageUseTextures", packageUseTextures) as Boolean
			packageTexture = jsonObject.getOrDefault("packageTexture", packageTexture) as String
			packageColor = jsonObject.getOrDefault("packageColor", packageColor) as String
		
			// Class:
			classSize = jsonObject.getOrDefault("classSize", classSize) as String
			classShape = jsonObject.getOrDefault("classShape", classShape) as String
			classUseTextures = jsonObject.getOrDefault("classUseTextures", classUseTextures) as Boolean
			classTexture = jsonObject.getOrDefault("classTexture", classTexture) as String
			classColor = jsonObject.getOrDefault("classColor", classColor) as String	
			
			// Attribute:
			showAttributes = jsonObject.getOrDefault("showAttributes", showAttributes) as Boolean
			attributeShape = jsonObject.getOrDefault("attributeShape", attributeShape) as String		
			attributeUseTextures = jsonObject.getOrDefault("attributeUseTextures", attributeUseTextures) as Boolean			
			attributeTexture = jsonObject.getOrDefault("attributeTexture", attributeTexture) as String
			attributeColor = jsonObject.getOrDefault("attributeColor", attributeColor) as String
			
			// Method:
			showMethods = jsonObject.getOrDefault("showMethods", showMethods) as Boolean
			attributeShape = jsonObject.getOrDefault("attributeShape", attributeShape) as String		
			methodUseTextures = jsonObject.getOrDefault("methodUseTextures", methodUseTextures) as Boolean			
			methodTexture = jsonObject.getOrDefault("methodTexture", methodTexture) as String
			methodColor = jsonObject.getOrDefault("methodColor", methodColor) as String	
			
		  	createDirectories
		  	
		  	val tmpPath = jsonObject.getOrDefault("famixPath", "") as String
		  	if (tmpPath != "") {
		  		outputDirectoryFamix = System::getProperty("user.dir") + "/" + tmpPath
		  	}
		} catch (FileNotFoundException e) {
			log.warn("configuration not found")
		}
		
		systemID = sha1Hex(repositoryName + repositoryOwner)
		snapshotID = sha1Hex(repositoryName + repositoryOwner + commit)
	}
	
	def private createDirectory (String path) {
		val dir = new File(path)
		if (!dir.exists) {
        	dir.mkdir
    	}        
	}
	
	def toJSON(String path) {
		this.path = path
		val json = new JSONObject()
		json.put("url", repositoryUrl)
		json.put("name", repositoryName)
		json.put("owner", repositoryOwner)
		json.put("commit", commit)
		json.put("commitOrder", commitOrder)
		json.put("type", RepositoryType::GIT.name)
		json.put("startDate", dateFormat.format(startDate))
		json.put("endDate", dateFormat.format(endDate))
		json.put("numberOfCommits", numberOfCommits)
		json.put("language", language)
		json.put("commits", commits)
		val file = new FileWriter(path)
		file.write(json.toJSONString)
		file.flush
		file.close
	}
	
	def toJSON(String path, boolean value) {
		this.path = path
		val json = new JSONObject()
		json.put("url", repositoryUrl)
		json.put("name", repositoryName)
		json.put("owner", repositoryOwner)
		json.put("commit", commit)
		json.put("commitOrder", commitOrder)
		json.put("type", RepositoryType::GIT.name)
		json.put("startDate", dateFormat.format(startDate))
		json.put("endDate", dateFormat.format(endDate))
		json.put("numberOfCommits", numberOfCommits)
		json.put("language", language)
		val file = new FileWriter(path)
		file.write(json.toJSONString)
		file.flush
		file.close
	}
	
	def private createDirectories () {
		outputDirectory = System::getProperty("user.dir") + "/output"
		outputDirectory.createDirectory
		if(repositoryOwner != "" && repositoryName != "") {
			outputDirectorySystem = outputDirectory + "/" + repositoryOwner + "_" + repositoryName
			outputDirectoryFamix = outputDirectorySystem + "/famix/"
			outputDirectorySystem.createDirectory
			outputDirectoryFamix.createDirectory
		}	
	}
	
	def setStartDate(String value) throws ParseException {
		startDate = dateFormat.parse(value)
	}
	
	def setEndDate(String value) throws ParseException {
		endDate = dateFormat.parse(value)
	}
}
