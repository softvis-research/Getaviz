package org.getaviz.generator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration.Bricks.Layout;
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric;
import org.getaviz.generator.SettingsConfiguration.Panels.SeparatorModes;
import org.getaviz.generator.abap.repository.ACityElement;

public class SettingsConfiguration {
	private static PropertiesConfiguration config;
	private static SettingsConfiguration instance = null;
	private static Log log = LogFactory.getLog(SettingsConfiguration.class);
	private static String defaultBoldAddress = "bolt://localhost:7687";
	//private static String defaultBoldAddress = "bolt://localhost:11002";

	public static SettingsConfiguration getInstance() {
		if (instance == null) {
			instance = new SettingsConfiguration();
			loadConfig("settings.properties");
		}
		return instance;
	}

	public static void getInstance(String path) {
		if (instance == null) {
			instance = new SettingsConfiguration();
		}
		loadConfig(path);
	}
	
	public static SettingsConfiguration getInstance(HttpServletRequest request) {
		if (instance == null) {
			instance = new SettingsConfiguration();
		}
		loadConfig(request);
		return instance;
	}

	public String getDefaultBoldAddress() {
		return defaultBoldAddress;
	}
	
	private static void loadConfig(HttpServletRequest request) {
		config = new PropertiesConfiguration();
		Enumeration<String> parameters = request.getParameterNames();
		while(parameters.hasMoreElements()) {
			String parameter = parameters.nextElement();
			config.setProperty(parameter, request.getParameter(parameter));
		}
		new File(instance.getOutputPath()).mkdirs();
	}

	private static void loadConfig(String path) {
		File file = new File(path);
		try {
			Configurations configs = new Configurations();
			config = configs.properties(file);
			new File(instance.getOutputPath()).mkdirs();
		} catch (ConfigurationException cex) {
			log.error(cex);
		}
	}

	public boolean isSkipScan() {
		return config.getBoolean("input.skip_scan", false);
	}

	public List<Path> getInputCSVFiles() {
		String path = config.getString("input.map", "src/test/neo4jexport/");
		File currentDir = new File(path);
		String helper = currentDir.getAbsolutePath();
		List<Path> files = new ArrayList<>();
		try {
			files = Files.walk(Paths.get(helper), 1)
					.filter(Files::isRegularFile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}

	public String getInputFiles() {
		String[] fileArray = config.getStringArray("input.files");
		if(fileArray.length == 0) {
			throw new RuntimeException("There is no specified uri to a jar or war file. Check if in the settings.properties file the field input.files is set to one or more existing uris.");
		}
		StringBuilder files = new StringBuilder();
		
		ClassLoader classLoader = this.getClass().getClassLoader();
		for(int i = 0; i < fileArray.length; i++) {
			String path = fileArray[i];
			if(!path.startsWith("http") && !path.startsWith("https") && !path.startsWith("file")) {
				path = "file:" + Objects.requireNonNull(classLoader.getResource(path)).getPath();
				try {
					path = URLDecoder.decode(path, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				//path = path.replace(" ", "\\ ");
			}
			files.append(path);
			if(i < fileArray.length - 1) {
				files.append(",");
			}
		}
		return files.toString();
	}

	public String getOutputMap() { return config.getString("output.map", "src/test/neo4jexport");}
	
	public Metaphor getMetaphor() {
		String metaphor = config.getString("metaphor", "rd");
		if ("city".equals(metaphor)) {
			return Metaphor.CITY;
		}
		return Metaphor.RD;
	}

	public MetaDataOutput getMetaDataOutput() {
	    String metaDataOutput = config.getString("output.metaData", "both");
	    if ("file".equals(metaDataOutput)) {
	        return MetaDataOutput.FILE;
        } else if ("nodeProp".equals(metaDataOutput)) {
	        return MetaDataOutput.NODEPROP;
        }

	    return MetaDataOutput.BOTH;
    }

    public AFrameOutput getAframeOutput() {
		String aframeOutput = config.getString("output.aframe", "both");
		if ("file".equals(aframeOutput)) {
			return AFrameOutput.FILE;
		} else if ("nodeProp".equals(aframeOutput)) {
			return AFrameOutput.NODEPROP;
		}

		return AFrameOutput.BOTH;
	}

	//Kind of arranging the districts of the SCO
	public enum NotInOriginLayout {
		DEFAULT, CIRCULAR
	}

	public NotInOriginLayout getAbapNotInOrigin_layout() {
		String value = config.getString("city.abap.notInOrigin_layout", "default");
		switch (value) {
			case "default":
				return NotInOriginLayout.DEFAULT;
			case "circular":
				return NotInOriginLayout.CIRCULAR;
			default:
				return NotInOriginLayout.DEFAULT;
		}
	}

	public enum NotInOriginLayoutVersion {
		MINIMAL_DISTANCE, FULL_CIRCLE
	}

	public NotInOriginLayoutVersion getAbapNotInOrigin_layout_version() {
		String value = config.getString("city.abap.notInOrigin_layout_version", "minimalDistance");
		switch (value) {
			case "minimalDistance":
				return NotInOriginLayoutVersion.MINIMAL_DISTANCE;
			case "fullCircle":
				return NotInOriginLayoutVersion.FULL_CIRCLE;
			default:
				return NotInOriginLayoutVersion.MINIMAL_DISTANCE;
		}
	}

	public boolean clusterSubPackages() {
		return config.getBoolean("city.abap.clusterSubPackages", true);
	}

    //Kind of arranging the districts of the SCO
	public DistrictLayoutVersion getDistrictLayout_Version(){
		String value = config.getString("city.abap_district_layout", "new");
		switch (value) {
			case "old":
				return DistrictLayoutVersion.OLD;
			case "new":
				return DistrictLayoutVersion.NEW;
			default:
				return DistrictLayoutVersion.OLD;
		}
	}

    public String getName() {
		return config.getString("input.name", "default");
	}
	
	public String getOutputPath() {
		return config.getString("output.path", "/var/lib/jetty/data-gen/") + getName() + "/model/";
	}

	public OutputFormat getOutputFormat() {
		if ("x3d".equals(config.getString("output.format", "aframe"))) {
			return OutputFormat.X3D;
		}
		return OutputFormat.AFrame;
	}
	
	public String getBuildingTypeAsString() {
		return config.getString("city.building_type", "original");
	}

	public BuildingType getBuildingType() {
		String value = config.getString("city.building_type", "original");
		switch (value) {
		case "panels":
			return BuildingType.CITY_PANELS;
		case "bricks":
			return BuildingType.CITY_BRICKS;
		case "floor":
			return BuildingType.CITY_FLOOR;
		default:
			return BuildingType.CITY_ORIGINAL;
		}
	}

	public Schemes getScheme() {
		String value = config.getString("city.scheme", "types");
		if ("visibility".equals(value)) {
			return Schemes.VISIBILITY;
		}
		return Schemes.TYPES;
	}

	public ClassElementsModes getClassElementsMode() {
		String value = config.getString("city.class_elements_mode", "methods_and_attributes");
		switch (value) {
		case "methods_only":
			return ClassElementsModes.METHODS_ONLY;
		case "attributes_only":
			return ClassElementsModes.ATTRIBUTES_ONLY;
		default:
			return ClassElementsModes.METHODS_AND_ATTRIBUTES;
		}
	}

	public ClassElementsSortModesCoarse getClassElementsSortModeCoarse() {
		String value = config.getString("city.class_elements_sort_mode_coarse", "methods_first");
		switch (value) {
		case "unsorted":
			return ClassElementsSortModesCoarse.UNSORTED;
		case "attributes_first":
			return ClassElementsSortModesCoarse.ATTRIBUTES_FIRST;
		default:
			return ClassElementsSortModesCoarse.METHODS_FIRST;
		}
	}

	public ClassElementsSortModesFine getClassElementsSortModeFine() {
		String value = config.getString("city.elements_sort_mode_fine", "scheme");
		switch (value) {
		case "unsorted":
			return ClassElementsSortModesFine.UNSORTED;
		case "alphabetically":
			return ClassElementsSortModesFine.ALPHABETICALLY;
		case "nos":
			return ClassElementsSortModesFine.NOS;
		default:
			return ClassElementsSortModesFine.SCHEME;
		}
	}

	public boolean isClassElementsSortModeFineDirectionReversed() {
		return config.getBoolean("city.class_elements_sort_mode_fine_direction_reversed", false);
	}

	public boolean isShowBuildingBase() {
		return config.getBoolean("city.building_base", true);
	}

	public Layout getBrickLayout() {
		String brickLayout = config.getString("city.brick.layout", "progressive");
		switch (brickLayout) {
		case "straight":
			return Layout.STRAIGHT;
		case "balanced":
			return Layout.BALANCED;
		default:
			return Layout.PROGRESSIVE;
		}
	}

	public double getBrickSize() {
		return config.getDouble("city.brick.size", 1.0);
	}

	public double getBrickHorizontalMargin() {
		return config.getDouble("city.brick.horizontal_margin", 0.5);
	}

	public double getBrickHorizontalGap() {
		return config.getDouble("city.brick.horizontal_gap", 0.2);
	}

	public double getBrickVerticalMargin() {
		return config.getDouble("city.brick.vertical_margin", 0.2);
	}

	public double getBrickVerticalGap() {
		return config.getDouble("city.brick.vertical_gap", 0.2);
	}

	public boolean isShowAttributesAsCylinders() {
		return config.getBoolean("city.show_attributes_as_cylinders", true);
	}

	public SeparatorModes getPanelSeparatorMode() {
		String value = config.getString("city.panel.separator_mode", "separator");
		switch (value) {
		case "none":
			return SeparatorModes.NONE;
		case "gap":
			return SeparatorModes.GAP;
		default:
			return SeparatorModes.SEPARATOR;
		}
	}

	public int[] getPanelHeightTresholdNos() {
		int[] defaultValue = { 3, 6, 12, 24, 48, 96, 144, 192, 240 };
		String[] result = config.getStringArray("city.panel.height_treshold_nos");
		if (result.length == 0) {
			return defaultValue;
		} else {
			int[] value = new int[result.length];
			for (int i = 0; i < result.length; i++) {
				try {
					value[i] = Integer.parseInt(result[i]);
				System.out.print(value[i] +  " ");
				} catch(NumberFormatException e) {
					return defaultValue;
				}
				
			}
			return value;
		}
	}

	public double getPanelHeightUnit() {
		return config.getDouble("city.panel.height_unit", 0.5);
	}

	public double getPanelHorizontalMargin() {
		return config.getDouble("city.panel.horizontal_margin", 0.5);
	}

	public double getPanelVerticalMargin() {
		return config.getDouble("city.panel.vertical_margin", 0.25);
	}

	public double getPanelVerticalGap() {
		return config.getDouble("city.panel.vertical_gap", 0.125);
	}

	public double getPanelSeparatorHeight() {
		return config.getDouble("city.panel.separator_height", 0.125);
	}

	public BuildingMetric getOriginalBuildingMetric() {
		String value = config.getString("city.original_building_metric", "none");
		if ("nos".equals(value)) {
			return BuildingMetric.NOS;
		}
		return BuildingMetric.NONE;
	}

	public double getWidthMin() {
		return config.getDouble("city.width_min", 1.0);
	}

	public double getHeightMin() {
		return config.getDouble("city.height_min", 1.0);
	}

	public double getBuildingHorizontalMargin() {
		return config.getDouble("city.building.horizontal_margin", 3.0);
	}

	public double getBuildingHorizontalGap() {
		return config.getDouble("city.building.horizontal_gap", 3.0);
	}

	public double getBuildingVerticalMargin() {
		return config.getDouble("city.building.vertical_margin", 1.0);
	}

	public String getPackageColorStart() {
		return config.getString("city.package.color_start", "#969696");
	}

	public String getPackageColorEnd() {
		return config.getString("city.package.color_end", "#b1b1b1");
	}

	public String getClassColorStart() {
		return config.getString("city.class.color_start", "#131615");
	}

	public String getClassColorEnd() {
		return config.getString("city.class.color_end", "#00ff00");
	}

	public String getClassColor() {
		return config.getString("city.class.color", "#353559");
	}

	public String getCityColor(String name) {
		String colorName = name.toLowerCase();
		String defaultColor = "";
		switch (colorName) {
			case "aqua":
				defaultColor = "#99CCFF"; break;
			case "blue":
				defaultColor = "#99FFCC"; break;
			case "light_green":
				defaultColor = "#CCFF99"; break;
			case "dark_green":
				defaultColor = "#99FF99"; break;
			case "yellow":
				defaultColor = "#FFFF99"; break;
			case "orange":
				defaultColor = "#FFCC99"; break;
			case "red":
				defaultColor = "#FF9999"; break;
			case "pink":
				defaultColor = "#FF99FF"; break;
			case "violet":
				defaultColor = "#9999FF"; break;
			case "light_grey":
				defaultColor = "#CCCCCC"; break;
			case "dark_grey":
				defaultColor = "#999999"; break;
			case "white":
				defaultColor = "#FFFFFF"; break;
			case "black":
				defaultColor = "#000000"; break;
		}
		return config.getString("city.color." + colorName, defaultColor);
	}

	public String getCityFloorColor() {
		return config.getString("city.floor.color", "#1485CC");
	}

	public String getCityChimneyColor() {
		return config.getString("city.floor.chimney.color", "#FFFC19");
	}
	
	public double getRDDataFactor() {
		return config.getDouble("rd.data_factor", 4.0);
	}

	public double getRDHeight() {
		return config.getDouble("rd.height", 1.0);
	}

	public double getRDRingWidth() {
		return config.getDouble("rd.ring_width", 2.0);
	}

	public double getRDRingWidthAD() {
		return config.getDouble("rd.ring_width_ad", 0);
	}

	public double getRDMinArea() {
		return config.getDouble("rd.min_area", 10.0);
	}

	public double getRDNamespaceTransparency() {
		return config.getDouble("rd.namespace_transparency", 0);
	}

	public double getRDClassTransparency() {
		return config.getDouble("rd.class_transparency", 0);
	}

	public double getRDMethodTransparency() {
		return config.getDouble("rd.method_transparency", 0);
	}

	public double getRDDataTransparency() {
		return config.getDouble("rd.data_transparency", 0);
	}

	public String getRDClassColor() {
		return config.getString("rd.color.class", "#353559");
	}

	public String getRDDataColor() {
		return config.getString("rd.color.data", "#fffc19");
	}

	public String getRDMethodColor() {
		return config.getString("rd.color.method", "#1485cc");
	}

	public boolean isMethodDisks() {
		return config.getBoolean("rd.method_disks", false);
	}

	public boolean isDataDisks() {
		return config.getBoolean("rd.data_disks", false);
	}

	public boolean isMethodTypeMode() {
		return config.getBoolean("rd.method_type_mode", false);
	}

	public enum OutputFormat {
		X3D, AFrame
	}
	
	/**
	 * Sets in which way the Historic Evolution
	 * of the analyzed Software should be represented, 
	 * it can either be in a static or dynamic way 
	 */
	
	public enum BuildingType {
		CITY_ORIGINAL, CITY_PANELS, CITY_BRICKS, CITY_FLOOR
	}
	
	/**
	 * Defines how the methods and attributes are sorted and colored in the city
	 * model.
	 */
	public enum Schemes {
		/**
		 * The class elements are sorted and colored corresponding to there
		 * visibility modifiers.
		 *
		 * @see SortPriorities_Visibility
		 */
		VISIBILITY,

		/**
		 * The class elements are sorted and colored associated to
		 * type/functionality of the method.
		 *
		 * @see Methods.SortPriorities_Types
		 * @see Attributes.SortPriorities_Types
		 */
		TYPES
	}
	
	/**
	 * Defines which elements of a class are to show.
	 */
	public enum ClassElementsModes {
		METHODS_ONLY, ATTRIBUTES_ONLY, METHODS_AND_ATTRIBUTES
	}
	
	/**
	 * Defines which how the elements of a class are sorted.
	 */
	public enum ClassElementsSortModesCoarse {
		UNSORTED, ATTRIBUTES_FIRST, METHODS_FIRST
	}

	/**
	 * A list of types of a method with the associated priority value.<br>
	 * Highest priority/smallest number is placed on the bottom, lowest on top.
	 * 
	 * @see SortPriorities_Visibility
	 * @see Methods.SortPriorities_Types
	 * @see Attributes.SortPriorities_Types
	 */
	public enum ClassElementsSortModesFine {
		/** Class elements won't be sorted. */
		UNSORTED,

		/** Methods will be sorted according to the name. */
		ALPHABETICALLY,

		/**
		 * Methods will be sorted according to the active
		 * SET_CLASS_ELEMENTS_SORT_MODE_FINE}.
		 */
		SCHEME,

		/** Methods will be sorted according to there number of statements. */
		NOS
	}

	/**
	 * A list of visibility modifiers of a method with the associated priority
	 * value.<br>
	 * Highest priority/smallest number is placed on the bottom, lowest on top.
	 * 
	 * @see ClassElementsSortModesFine
	 * 
	 */
	public enum SortPriorities_Visibility {;
		public static int PRIVATE = 1;
		public static int PROTECTED = 2;
		public static int PACKAGE = 3;
		public static int PUBLIC = 4;
	}

	public enum Methods {;

		/**
		 * A list of types of a method with the associated priority value.<br>
		 * Highest priority/smallest number is placed on the bottom, lowest on
		 * top.
		 *
		 * @see ClassElementsSortModesFine
		 * @see SortPriorities_Visibility
		 */
		public enum SortPriorities_Types {;

			/**
			 * Method is a constructor.
			 */
			public static int CONSTRUCTOR = 1;

			/**
			 * The name of the method begins with "get".
			 */
			public static int GETTER = 2;

			/**
			 * The name of the method begins with "set".
			 */
			public static int SETTER = 3;

			/**
			 * Method has a {@code static} modifier.
			 */
			public static int STATIC = 4;

			/**
			 * Method has an {@code abstract} modifier.
			 */
			public static int ABSTRACT = 5;

			/**
			 * Every other type that isn't specified by the other constants in
			 * this field.
			 */
			public static int LEFTOVER = 6;
		}

	}

	public enum Attributes {;

		/**
		 * A list of types of a method with the associated priority value.<br>
		 * Highest priority/smallest number is placed on the bottom, lowest on
		 * top.
		 * @see ClassElementsSortModesFine
		 */
		public enum SortPriorities_Types {;

			/** Type is a primitive like {@code boolean}, {@code int}. */
			public static int PRIMITVE = 1;

			/** Type is a (Non-wrapper) class, collection, etc. */
			public static int COMPLEX = 2;

		}

	}

	public enum Bricks {;

		/**
		 * Defines the layout for the BuildingSegments of the city model, which
		 * represents the methods and/or attributes of a class.
		 */
		public enum Layout {

			/**
			 * One-dimensional bricks layout, where the segments simply are
			 * placed on top of the other.
			 */
			STRAIGHT,

			/**
			 * Three-dimensional brick layout, where the base area is computed
			 * depending on the {@link ClassElementsModes}.<br>
			 * If only methods are shown, the base area is computed by the
			 * number of attributes and vice versa.<br>
			 * In case of methods and attributes are shown, the base area is
			 * computed by the sum of the numbers of attributes and methods
			 * inside the class.
			 * <p>
			 * When {@link ClassElementsModes} is set to
			 * {@code METHODS_AND_ATTRIBUTES}, the {@code BALANCED} layout and
			 * {@link Layout#PROGRESSIVE PROGRESSIVE} layout are identical.
			 */
			BALANCED,

			/**
			 * Three-dimensional brick layout, where the base area is computed
			 * depending on the {@link ClassElementsModes}.<br>
			 * If only methods are shown, the base area is computed by the
			 * number of methods and vice versa. So the aspect lies on only one
			 * type of element of a class and is visualized.
			 * <p>
			 * When {@link ClassElementsModes} is set to
			 * {@code METHODS_AND_ATTRIBUTES}, the {@link Layout#BALANCED
			 * PROGRESSIVE} layout and {@code PROGRESSIVE} layout are identical.
			 */
			PROGRESSIVE

		}
	}

	public enum Panels {
		;

		/**
		 * Defines the the space between the panels.<br>
		 * The panels can either touch each other without a gap, leave a gap
		 * between them, or fill the space with a separator of a defined color.
		 */
		public enum SeparatorModes {

			/**
			 * No space between the panels and they are placed on top of each
			 * other.
			 */
			NONE,

			/**
			 * The panels have a free space between them and don't touch each
			 * other.
			 */
			GAP,

			/**
			 * Between the panels separators are placed with a fix height and
			 * color.
			 */
			SEPARATOR

		}
	}
	
	public enum Original {
		;
		public enum BuildingMetric {
			NONE,
			NOS
		}
	}
	
	public enum Metaphor {
		RD, CITY
	}

	public enum MetaDataOutput {
	    FILE, NODEPROP, BOTH
    }

    public enum  AFrameOutput {
		FILE, NODEPROP, BOTH
	}

	public enum DistrictLayoutVersion {
		OLD, NEW
	}





	/*
		ACity specific config values
	 */

	public String getACityDistrictColorHex(String type) {
		switch (type) {
			case "packageDistrict":
				return config.getString("city.abap.color.packageDistrict", "#95A5A6");
			case "classDistrict":
				return config.getString("city.abap.color.classDistrict", "#C5CEA9");
			case "reportDistrict":
				return config.getString("city.abap.color.reportDistrict", "#C5CEA9");
			case "functionGroupDistrict":
				return config.getString("city.abap.color.functionGroupDistrict", "#C5CEA9");
			case "tableDistrict":
				return config.getString("city.abap.color.tableDistrict", "#C5CEA9");
			case "dataDictionaryDistrict":
				return config.getString("city.abap.color.dataDictionaryDistrict", "#C5CEA9");
			default:
				return config.getString("#FFFFFF");
		}
	}

	public String getMetropolisDistrictColorHex(String type) {
		switch (type) {
			case "packageDistrict":
				return config.getString("city.abap.color.metropolis.packageDistrict", "#95A5A6");
			case "classDistrict":
				return config.getString("city.abap.color.metropolis.classDistrict", "#C5CEA9");
			case "localClassDistrict":
				return config.getString("city.abap.color.metropolis.localClassDistrict", "#C5CEA9");
			case "interfaceDistrict":
				return config.getString("city.abap.color.metropolis.interfaceDistrict", "#C5CEA9");
			case "localInterfaceDistrict":
				return config.getString("city.abap.color.metropolis.localInterfaceDistrict", "#C5CEA9");
			case "reportDistrict":
				return config.getString("city.abap.color.metropolis.reportDistrict", "#C5CEA9");
			case "functionGroupDistrict":
				return config.getString("city.abap.color.metropolis.functionGroupDistrict", "#C5CEA9");
			case "tableDistrict":
				return config.getString("city.abap.color.metropolis.tableDistrict", "#C5CEA9");
			case "structureDistrict":
				return config.getString("city.abap.color.metropolis.structureDistrict", "#C5CEA9");
			case "dataElementDistrict":
				return config.getString("city.abap.color.metropolis.dataElementDistrict", "#C5CEA9");
			default:
				return config.getString("#FFFFFF");
		}
	}

	public String getACityBuildingColorHex(String type) {
		switch (type) {
			case "classBuilding":
				return config.getString("city.abap.color.classBuilding", "#C5CEA9");
			case "interfaceBuilding":
				return config.getString("city.abap.color.interfaceBuilding", "#C5CEA9");
			case "reportBuilding":
				return config.getString("city.abap.color.reportBuilding", "#C5CEA9");
			case "functionGroupBuilding":
				return config.getString("city.abap.color.functionGroupBuilding", "#C5CEA9");
			case "tableBuilding":
				return config.getString("city.abap.color.tableBuilding", "#C5CEA9");
			case "dataElementBuilding":
				return config.getString("city.abap.color.dataElementBuilding", "#C5CEA9");
			case "domainBuilding":
				return config.getString("city.abap.color.domainBuilding", "#C5CEA9");
			case "structureBuilding":
				return config.getString("city.abap.color.structureBuilding", "#C5CEA9");
			case "tableTypeBuilding":
				return config.getString("city.abap.color.tableTypeBuilding", "#C5CEA9");
			default:
				return config.getString("#FFFFFF");
		}
	}

	public String getMetropolisBuildingColorHex(String type) {
		switch (type) {
			case "attributeBuilding":
				return config.getString("city.abap.metropolis.color.attributeBuilding", "#C5CEA9");
			case "interfaceBuilding":
				return config.getString("city.abap.metropolis.color.interfaceBuilding", "#C5CEA9");
			case "methodBuilding":
				return config.getString("city.abap.metropolis.color.methodBuilding", "#C5CEA9");
			case "reportBuilding":
				return config.getString("city.abap.metropolis.color.reportBuilding", "#C5CEA9");
			case "formRoutineBuilding":
				return config.getString("city.abap.metropolis.color.formRoutineBuilding", "#3ff493");
			case "functionModuleBuilding":
				return config.getString("city.abap.metropolis.color.functionModuleBuilding", "#C5CEA9");
			case "tableBuilding":
				return config.getString("city.abap.metropolis.color.tableBuilding", "#C5CEA9");
			case "dataElementBuilding":
				return config.getString("city.abap.metropolis.color.dataElementBuilding", "#C5CEA9");
			case "domainBuilding":
				return config.getString("city.abap.metropolis.color.domainBuilding", "#C5CEA9");
			case "structureBuilding":
				return config.getString("city.abap.metropolis.color.structureBuilding", "#C5CEA9");
			case "tableTypeBuilding":
				return config.getString("city.abap.metropolis.color.tableTypeBuilding", "#C5CEA9");
			case "seaReferenceBuilding":
				return config.getString("city.abap.metropolis.color.seaReferenceBuilding", "#C5CEA9");
			case "mountainReferenceBuilding":
				return config.getString("city.abap.metropolis.color.mountainReferenceBuilding","C5CEA9");
			default:
				return config.getString("#FFFFFF");
		}
	}

	public String getACityFloorColorHex(String type) {
		switch (type) {
			case "methodFloor":
				return config.getString("city.abap.color.methodFloor", "#C5CEA9");
			case "formRoutineFloor":
				return config.getString("city.abap.color.formRoutineFloor", "#C5CEA9");
			case "functionModuleFloor":
				return config.getString("city.abap.color.functionModuleFloor", "#C5CEA9");
			case "tableElementFloor":
				return config.getString("city.abap.color.tableElementFloor", "#C5CEA9");
			case "structureElementFloor":
				return config.getString("city.abap.color.structureElementFloor", "#C5CEA9");
			case "dataElementFloor":
				return config.getString("city.abap.color.dataElementFloor", "#C5CEA9");
			default:
				return config.getString("#FFFFFF");
		}
	}

	public String getACityChimneyColorHex(String type) {
		switch (type) {
			case "attributeColor":
				return config.getString("city.abap.color.attribute", "#C5CEA9");
			default:
				return config.getString("#FFFFFF");
		}
	}

	public ACityElement.ACityShape getACityBuildingShape(String type) {
		switch (type) {
			case "classBuilding":
				String value = config.getString("city.abap.shape.classBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "interfaceBuilding":
				value = config.getString("city.abap.shape.interfaceBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "reportBuilding":
				value = config.getString("city.abap.shape.reportBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "functionGroupBuilding":
				value = config.getString("city.abap.shape.functionGroupBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "tableBuilding":
				value = config.getString("city.abap.shape.tableBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "dataElementBuilding":
				value = config.getString("city.abap.shape.dataElementBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "domainBuilding":
				value = config.getString("city.abap.shape.domainBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "structureBuilding":
				value = config.getString("city.abap.shape.structureBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "tableTypeBuilding":
				value = config.getString("city.abap.shape.tableTypeBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			default:
				return ACityElement.ACityShape.Box;
		}
	}

	public ACityElement.ACityShape getMetropolisBuildingShape(String type) {
		switch (type) {
			case "attributeBuilding":
				String value = config.getString("city.abap.metropolis.shape.attributeBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "interfaceBuilding":
				value = config.getString("city.abap.metropolis.shape.interfaceBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "methodBuilding":
				value = config.getString("city.abap.metropolis.shape.methodBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "reportBuilding":
				value = config.getString("city.abap.metropolis.shape.reportBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "formRoutineBuilding":
				value = config.getString("city.abap.metropolis.shape.formRoutineBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "functionModuleBuilding":
				value = config.getString("city.abap.metropolis.shape.functionModuleBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "tableBuilding":
				value = config.getString("city.abap.metropolis.shape.tableBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "dataElementBuilding":
				value = config.getString("city.abap.metropolis.shape.dataElementBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "domainBuilding":
				value = config.getString("city.abap.metropolis.shape.domainBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "structureBuilding":
				value = config.getString("city.abap.metropolis.shape.structureBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "tableTypeBuilding":
				value = config.getString("city.abap.metropolis.shape.tableTypeBuilding", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			default:
				return ACityElement.ACityShape.Box;
		}
	}

	public ACityElement.ACityShape getACityFloorShape(String type) {
		switch (type) {
			case "methodFloor":
				String value = config.getString("city.abap.shape.methodFloor", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "formroutineFloor":
				value = config.getString("city.abap.shape.formRoutineFloor", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "functionModuleFloor":
				value = config.getString("city.abap.shape.functionModuleFloor", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "tableElementFloor":
				value = config.getString("city.abap.shape.tableElementFloor", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "structureElementFloor":
				value = config.getString("city.abap.shape.structureElementFloor", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			case "dataElementFloor":
				value = config.getString("city.abap.shape.dataElementFloor", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cone;
				}
			default:
				return ACityElement.ACityShape.Cone;
		}
	}

	public ACityElement.ACityShape getACityChimneyShape(String type) {
		switch (type) {
			case "attributeChimney":
				String value = config.getString("city.abap.shape.attributeChimney", "box");
				switch (value) {
					case "box":
						return ACityElement.ACityShape.Box;
					case "cone":
						return ACityElement.ACityShape.Cone;
					case "cylinder":
						return ACityElement.ACityShape.Cylinder;
					default:
						return ACityElement.ACityShape.Cylinder;
				}
			default:
				return ACityElement.ACityShape.Cylinder;
		}
	}

	public ACityElement.ACityShape getACityDistrictShape() {
		String value = config.getString("city.abap.shape.district", "box");
		switch (value) {
			case "box":
				return ACityElement.ACityShape.Box;
			case "cone":
				return ACityElement.ACityShape.Cone;
			case "cylinder":
				return ACityElement.ACityShape.Cylinder;
			default:
				return ACityElement.ACityShape.Cylinder;
		}
	}

	public double getACityBuildingWidth(String type) {
		switch (type) {
			case "tableTypeBuilding":
				return config.getDouble("city.abap.width.tableTypeBuilding", 0.1);
			case "structureBuilding":
				return config.getDouble("city.abap.width.structureBuilding", 0.1);
			case "classBuilding":
				return config.getDouble("city.abap.width.classBuilding", 0.1);
			default:
				return 0.1;
		}
	}

	public double getMetropolisReferenceBuildingWidth(String type) {
		switch (type) {
			case "seaReferenceBuilding":
				return config.getDouble("city.abap.metropolis.width.seaReferenceBuilding", 0.1);
			case "mountainReferenceBuilding":
				return config.getDouble("city.abap.metropolis.width.mountainReferenceBuilding", 0.1);
			case "cloudReferenceBuilding":
				return config.getDouble("city.abap.metropolis.width.cloudReferenceBuilding", 0.1);
			default:
				return 0.1;
		}
	}

	public double getMetropolisReferenceBuildingLength(String type) {
		switch (type) {
			case "seaReferenceBuilding":
				return config.getDouble("city.abap.metropolis.length.seaReferenceBuilding", 0.1);
			case "mountainReferenceBuilding":
				return config.getDouble("city.abap.metropolis.length.mountainReferenceBuilding", 0.1);
			case "cloudReferenceBuilding":
				return config.getDouble("city.abap.metropolis.length.cloudReferenceBuilding", 0.1);
			default:
				return 0.1;
		}
	}

	public double getMetropolisReferenceBuildingHeigth(String type) {
		switch (type) {
			case "seaReferenceBuilding":
				return config.getDouble("city.abap.metropolis.height.seaReferenceBuilding", 0.1);
			case "mountainReferenceBuilding":
				return config.getDouble("city.abap.metropolis.height.mountainReferenceBuilding", 0.1);
			case "cloudReferenceBuilding":
				return config.getDouble("city.abap.metropolis.height.cloudReferenceBuilding", 0.1);
			default:
				return 0.1;
		}
	}

	public double getACityBuildingLength(String type) {
		switch (type) {
			case "tableTypeBuilding":
				return config.getDouble("city.abap.length.tableTypeBuilding", 0.1);
			case "structureBuilding":
				return config.getDouble("city.abap.length.structureBuilding", 0.1);
			default:
				return 0.1;
		}
	}

	public double getACityTableTypeBuildingHeight(String type) {
		switch (type) {
			case "tableTypeBuilding_structure":
				return config.getDouble("city.abap.height.tableTypeBuilding_structure", 0.1);
			case "tableTypeBuilding_class":
			case "tableTypeBuilding_interface":
				return config.getDouble("city.abap.height.tableTypeBuilding_class", 0.1);
			case "tableTypeBuilding_tabletype":
			case "tableTypeBuilding_table":
				return config.getDouble("city.abap.height.tableTypeBuilding_table", 0.1);
			case "tableTypeBuilding_dataElement":
				return config.getDouble("city.abap.height.tableTypeBuilding_dataElement", 0.1);
			default:
				return 0.1;
		}
	}

	public String getMetropolisAssetsSourcePath(String type) {
		switch (type) {
			case "sky":
				return config.getString("city.abap.metropolis.assets.sky.sourcePath", "images/sky_pano.jpg");
			case "ground":
				return config.getString("city.abap.metropolis.assets.ground.sourcePath", "images/ground.jpg");
			case "mountain":
				return config.getString("city.abap.metropolis.assets.mountain.sourcePath", "models/polyMountain_new_Color.jpg");
			case "sea":
				return config.getString("city.abap.metropolis.assets.sea.sourcePath", "images/sea_pool.jpg");
			case "cloud":
				return config.getString("city.abap.metropolis.assets.cloud.sourcePath", "models/cloud_black.jpg");
			default:
				return config.getString("");
		}
	}

	public boolean showSeaReferenceBuilding() {return config.getBoolean("city.abap.metropolis.showSeaReferenceBuilding", false);}
	public boolean showMountainReferenceBuilding() {return config.getBoolean("city.abap.metropolis.showMountainReferenceBuilding", false);}
	public boolean showCloudReferenceBuilding() {return config.getBoolean("city.abap.metropolis.showCloudReferenceBuilding", false);}

	public boolean addMigrationFindings() {return config.getBoolean("city.abap.metropolis.addMigrationFindings", false);}


    public String getMetropolisBuildingRotation() {return config.getString("city.abap.metropolis.SeaReferenceBuildingRotation", " 0 0 0 ");}
	public String getMetropolisReferenceBuildingModelScale() {return config.getString("city.abap.metropolis.MountainReferenceBuildingModelScale", " 0 0 0 ");}

	public double getACityDistrictHeight() {return config.getDouble("city.abap.height.district", 0.2); }
	public double getMetropolisEmptyDistrictHeight() {return config.getDouble("city.abap.metropolis.height.emptyDistrict", 0.2); }
	public double getMetropolisEmptyDistrictLength() {return config.getDouble("city.abap.metropolis.length.emptyDistrict", 0.2); }
	public double getMetropolisEmptyDistrictWidth() {return config.getDouble("city.abap.metropolis.width.emptyDistrict", 0.2); }

	public double adjustACityDistrictYPosition() {return config.getDouble("city.abap.adjust.district.yPosition", 0.1); }

	public double getACityBuildingHorizontalMargin() { return config.getDouble("city.abap.building.horizontal_margin", 0.0); }
	public double getACityBuildingVerticalMargin() { return config.getDouble("city.abap.building.vertical_margin", 0.0); }
	public double adjustACityBuildingWidth() {return config.getDouble("city.abap.adjust.building.width", 0.1); }
	public double adjustACityBuildingLength() {return config.getDouble("city.abap.adjust.building.length", 0.1); }

	public double getACityChimneyWidth(){
		return config.getDouble("city.abap.width.chimney", 0.3);
	}
	public double getACityChimneyHeight(){
		return config.getDouble("city.abap.height.chimney", 0.3);
	}
	public double getACityChimneyLength(){
		return config.getDouble("city.abap.length.chimney", 0.3);
	}
	public double getACityChimneyGap() { return config.getDouble("city.abap.gap.chimney", 0.0); }
	public double getACityGroundAreaByChimneyAmount() { return config.getDouble("city.abap.groundArea_cimney", 2.0); }

	public double getACityFloorHeight(){
		return config.getDouble("city.abap.height.floor", 1);
	}
	public double getACityFloorGap(){
		return config.getDouble("city.abap.gap.floor", 0.5);
	}
	public double getFloorHeightSum() {return config.getDouble("city.abap.floorHeightSum", 1.0); }
	public double adjustACityFloorYPosition() {return config.getDouble("city.abap.adjust.floor.yPosition", 0.1); }
	public double adjustACityFloorWidth() {return config.getDouble("city.abap.adjust.floor.width", 0.1); }
	public double adjustACityFloorLength() {return config.getDouble("city.abap.adjust.floor.length", 0.1); }

	public double getAbapScoMinHeight(){return config.getDouble("city.abap.sco.min.height", 1);}
	public double getAbapScoMaxHeight(){return config.getDouble("city.abap.sco.max.height", 30);}
	public double getAbapStandardCodeHeight(){return config.getDouble("city.abap.sco.standard.height", 4);}
}
