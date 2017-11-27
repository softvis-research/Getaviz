package org.svis.generator.plant

import org.eclipse.xtend.lib.annotations.Accessors
import org.json.simple.parser.JSONParser
import java.io.FileReader
import org.json.simple.JSONObject
import java.io.FileNotFoundException
import org.apache.commons.logging.LogFactory
import java.io.FileWriter
import org.svis.generator.plant.m2m.RGBColor
import org.svis.generator.plant.m2t.PlantShapeManager
import org.svis.generator.Configuration
/**
 * Manage configuration for the plant metaphor and the config.json file.
 */
class PlantConfiguration extends Configuration {
	val log = LogFactory::getLog(class)
	@Accessors String outputDirectoryFamix
	@Accessors String path
	/**
	 * Read values from config.json.
	 */
	new(FileReader reader){
		super()
		val parser = new JSONParser()
		try {
			val jsonObject = parser.parse(reader) as JSONObject
			//println(path + ": " + jsonObject.toString)
			reader.close
			
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
			
			// Generally:
			switchAttributeMethodMapping = jsonObject.getOrDefault("switchAttributeMethodMapping", switchAttributeMethodMapping) as String
			// Package:	
			packageOddTexture = jsonObject.getOrDefault("packageOddTexture", packageOddTexture) as String
			packageEvenTexture = jsonObject.getOrDefault("packageEvenTexture", packageEvenTexture) as String
			packageEvenColor = jsonObject.getOrDefault("packageEvenColor", packageEvenColor) as String
			packageOddColor = jsonObject.getOrDefault("packageOddColor", packageOddColor) as String
			// Class: 
			classTexture02 = jsonObject.getOrDefault("classTexture02", classTexture02) as String
			classTexture03 = jsonObject.getOrDefault("classTexture03", classTexture03) as String
			classColor02 = jsonObject.getOrDefault("classColor02", classColor02) as String
			classColor03 = jsonObject.getOrDefault("classColor03", classColor03) as String
			// Method:
			methodTexture02 = jsonObject.getOrDefault("methodTexture02", methodTexture02) as String
			methodColor02 = jsonObject.getOrDefault("methodColor02", methodColor02) as String
			// Inner class:
			innerClassShape = jsonObject.getOrDefault("innerClassShape", "") as String
//			innerClassUseTextures = Boolean.valueOf(jsonObject.getOrDefault("innerClassUseTextures", "") as String)
			innerClassUseTextures = jsonObject.getOrDefault("innerClassUseTextures", innerClassUseTextures) as Boolean
			innerClassTexture = jsonObject.getOrDefault("innerClassTexture", innerClassTexture) as String
			innerClassTexture02 = jsonObject.getOrDefault("innerClassTexture02",innerClassTexture02) as String
			innerClassTexture03 = jsonObject.getOrDefault("innerClassTexture03", innerClassTexture03) as String
			innerClassColor = jsonObject.getOrDefault("innerClassColor", innerClassColor) as String
			innerClassColor02 = jsonObject.getOrDefault("innerClassColor02", innerClassColor02) as String
			innerClassColor03 = jsonObject.getOrDefault("innerClassColor03", innerClassColor03) as String
			// Inner class attribute:
			innerClassAttributeShape = jsonObject.getOrDefault("innerClassAttributeShape", innerClassAttributeShape) as String
			innerClassAttributeTexture = jsonObject.getOrDefault("innerClassAttributeTexture", innerClassAttributeTexture) as String
			innerClassAttributeColor = jsonObject.getOrDefault("innerClassAttributeColor", innerClassAttributeColor) as String
			// Inner class method:
			innerClassMethodShape = jsonObject.getOrDefault("innerClassMethodShape", innerClassMethodShape) as String
			innerClassMethodTexture = jsonObject.getOrDefault("innerClassMethodTexture", innerClassMethodTexture) as String
			innerClassMethodColor = jsonObject.getOrDefault("innerClassMethodColor", innerClassMethodColor) as String
			
	
		  	val tmpPath = jsonObject.getOrDefault("famixPath", "") as String
		  	if (tmpPath != "") {
		  		outputDirectoryFamix = System::getProperty("user.dir") + "/" + tmpPath
		  	}
		} catch (FileNotFoundException e) {
			log.warn("configuration not found")
		}
	}
	/**
	 * Constructor.
	 */
	new() {		
	}
	/**
	 * Create and write config.json.
	 */
	override toJSON(String path) {
//		super.toJSON(path)
	
		this.path = path
//		val json = new JSONObject()
//		val json = super.createJSON
		val json = new JSONObject()
		json.put("packageShape", packageShape)
		json.put("packageUseTextures", packageUseTextures)
		json.put("packageTexture", packageTexture)
		json.put("packageColor", packageColor)
		json.put("classSize", classSize)
		json.put("classShape", classShape)
		json.put("classUseTextures", classUseTextures)
		json.put("classTexture", classTexture)
		json.put("classColor", classColor)
		json.put("showAttributes", showAttributes)
		json.put("attributeShape", attributeShape)
		json.put("attributeUseTextures", attributeUseTextures)
		json.put("attributeTexture", attributeTexture)
		json.put("attributeColor", attributeColor)
		json.put("showMethods", showMethods)
		json.put("attributeShape", attributeShape)
		json.put("methodUseTextures", methodUseTextures)
		json.put("methodTexture", methodTexture)
		json.put("methodColor", methodColor)

		json.put("switchAttributeMethodMapping", switchAttributeMethodMapping)
		json.put("packageOddTexture", packageOddTexture)
		json.put("packageEvenTexture", packageEvenTexture)
		json.put("packageEvenColor", packageEvenColor)
		json.put("packageOddColor", packageOddColor)
		json.put("classTexture02", classTexture02)
		json.put("classTexture03", classTexture03)
		json.put("classColor02", classColor02)
		json.put("classColor03", classColor03)
		json.put("methodTexture02", methodTexture02)
		json.put("methodColor02", methodColor02)
		json.put("innerClassShape", innerClassShape)
		json.put("innerClassUseTextures", innerClassUseTextures)
		json.put("innerClassTexture", innerClassTexture)
		json.put("innerClassTexture02", innerClassTexture02)
		json.put("innerClassTexture03", innerClassTexture03)
		json.put("innerClassColor", innerClassColor)
		json.put("innerClassColor02", innerClassColor02)
		json.put("innerClassColor03", innerClassColor03)
		json.put("innerClassAttributeShape", innerClassAttributeShape)
		json.put("innerClassAttributeTexture", innerClassAttributeTexture)
		json.put("innerClassAttributeColor", innerClassAttributeColor)
		json.put("innerClassMethodShape", innerClassMethodShape)
		json.put("innerClassMethodTexture", innerClassMethodTexture)
		json.put("innerClassMethodColor", innerClassMethodColor)
		val file = new FileWriter(path)
		file.write(json.toJSONString)
		file.flush
		file.close
		  	
	}

	// Generally: -------------------------------------------------- 
	// Mapping: 
	@Accessors private String switchAttributeMethodMapping = 		"PETAL_POLLSTEM"; // POLLSTEM_PETAL, PETAL_POLLSTEM, POLLSTEM_PETAL
			
	// Package:
	@Accessors private String packageShape =						PlantShapeManager.AREA.DEFAULT
	@Accessors private Boolean packageUseTextures 					= true;
	@Accessors private String packageOddTexture = 					"<ImageTexture url='pics/ground.png' scale='false' />";
	@Accessors private String packageEvenTexture = 					"<ImageTexture url='pics/freeGrass.png' scale='false' />";
	@Accessors private String packageOddColor = 					"<Material diffuseColor='"+ new RGBColor(150,67,39).asPercentage() + "' />"; 	
	@Accessors private String packageEvenColor = 					"<Material diffuseColor='"+ new RGBColor(48,186,67).asPercentage() + "' />"; 
	
	// Class: 
	@Accessors private String classSize =							"Count_AttributesAndMethods"; 
	@Accessors private String classShape =							PlantShapeManager.STEM.DEFAULT
	@Accessors private Boolean classUseTextures 					= false;
	@Accessors private String classTexture = 						"<ImageTexture url='pics/plant.png' scale='true' />";
	@Accessors private String classTexture02 = 						"<ImageTexture url='pics/plantHeadBrown.png' scale='false' />";
	@Accessors private String classTexture03 = 						"<ImageTexture url='pics/bloom.png' scale='false' />";
	@Accessors private String classColor = 							"<Material diffuseColor='"+ new RGBColor(52,102,59).asPercentage() + "' />";
	@Accessors private String classColor02 = 						"<Material diffuseColor='0.545 0.27 0.074' />";
	@Accessors private String classColor03 = 						"<Material diffuseColor='1 1 0' />";

	// Inner class: 
	@Accessors private String innerClassShape =						PlantShapeManager.JUNCTION.DEFAULT
	@Accessors private Boolean innerClassUseTextures 				= false;
	@Accessors private String innerClassTexture = 					"<ImageTexture url='pics/plant.png' scale='true' />";
	@Accessors private String innerClassTexture02 = 				"<ImageTexture url='pics/bloom.png' scale='false' />";
	@Accessors private String innerClassTexture03 =					"<ImageTexture url='pics/junctionHeadTopPart.png' scale='false' />";
	@Accessors private String innerClassColor = 					"<Material diffuseColor='"+ new RGBColor(50,156,60).asPercentage() + "' />";
	@Accessors private String innerClassColor02 = 					"<Material diffuseColor='0.545 0.27 0.074' />";
	@Accessors private String innerClassColor03 =					"<Material diffuseColor='1 1 0' />";
	
	// Attribute:
	@Accessors private Boolean showAttributes = 					true;
	@Accessors private String attributeShape =						PlantShapeManager.PETAL.REALITIC_PETAL
	@Accessors private Boolean attributeUseTextures = 				true;
	@Accessors private String attributeTexture = 					"<ImageTexture url='pics/lilacPetal.png' scale='false' />";
	@Accessors private String attributeColor = 						"<Material diffuseColor='0.540 0.20 0.596' />"; 
	
	// Inner class attribute:
	@Accessors private String innerClassAttributeShape =			PlantShapeManager.JUNCTIONPETAL.DEFAULT
	@Accessors private String innerClassAttributeTexture =			"<ImageTexture url='pics/lilacPetal.png' scale='false' />";
	@Accessors private String innerClassAttributeColor =			"<Material diffuseColor='"+ new RGBColor(171,38,38).asPercentage() + "' />";	
	
	// Method: 
	@Accessors public Boolean showMethods = 						true;
	@Accessors private String methodShape =							PlantShapeManager.POLLSTEM.DEFAULT
	@Accessors private Boolean methodUseTextures = 					true;
	@Accessors private String methodTexture = 						"<ImageTexture url='pics/junctionGreen.png' scale='false' />";
	@Accessors private String methodTexture02 = 					"<ImageTexture url='pics/pollball.png' scale='false' />";
	@Accessors private String methodColor = 						"<Material diffuseColor='0 1 0' />";
	@Accessors private String methodColor02 = 						"<Material diffuseColor='1 1 0' />";
	
	// Inner class method:
	@Accessors private String innerClassMethodShape =				PlantShapeManager.JUNCTIONPOLLSTEM.DEFAULT
	@Accessors private String innerClassMethodTexture =				"<ImageTexture url='pics/bloom.png' scale='false' />";
	@Accessors private String innerClassMethodColor =				"<Material diffuseColor='0.545 0.27 0.074' />";
	
	// Plant specific constants: ------------------------------------
	public static final double STEM_THICKNESS = 3;
	// Junction stem length/width:
	public static final double JUNCTION_STEM_THICKNESS = STEM_THICKNESS/2;
	// Stem height:
	public static final double STEM_HEIGHT = 6;
	// head/cron height:
	public static final double CRON_HEIGHT = 2;
	// Height of the head:
	public static final double CRON_HEAD_HEIGHT = 0.5;
    // pental angle:
	public static final double PETAL_ANGLE = 0.5236;
	// pental distance multiplier:
	public static final double PETAL_DISTANCE_MULTIPLIER = 3;
    // poll stem angle:
	public static final double POLLENSTEM_ANGLE = 0.05; //= 0.4236;
	// poll stem distance multiplier:
	public static final double POLLSTEM_ANGLE_DISTANCE_MULTIPLIER = 0.3;
	// poll ball multiplier:
	public static final double POLLSTEM_BALL_MULTIPLIER = 1.57;
	// junction poll ball multiplier:
	public static final double JUNCTION_POLLSTEM_BALL_MULTIPLIER = 0.10;
	// poll ball height:
	public static final double POLLSTEM_BALL_HEIGH = CRON_HEIGHT + 3.87;	
    // plant junction angle:
	public static final double PLANT_JUNCTION_ANGLE = 1.3; //= 0.7854;
	// junction distance multiplier:
	public static final double PLANT_JUNCTION_DISTANCE_MULTIPLIER = 8;
	// Area default height:
	public static final double AREA_HEIGHT = 3.5;
	
	// Stuff from city for the layout algorithm: ------------------------
	public static final double WIDTH_MIN = 1;
	public static final double HEIGHT_MIN = 1;

	public static final double BLDG_horizontalMargin = 3;		//horizontal distance to parent
	public static final double BLDG_horizontalGap = 7;			//horizontal distance to neighbor
	public static final double BLDG_verticalMargin = 1;

}
