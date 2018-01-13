package org.svis.generator.city;

import org.svis.generator.city.m2m.BuildingSegmentComparator;
import org.svis.generator.city.m2m.RGBColor;

/**
 * Options for the city metaphor.
 */
public enum CitySettings {;
	
	/**
	 * Sets the variant of the city visualization which can
	 * either be panels, bricks or originals
	 *
	 */
	public static OutputFormat OUTPUT_FORMAT =  OutputFormat.X3D;
	
	public static enum OutputFormat {
		X3D, X3DOM, AFrame
	}
	
	public static BuildingType BUILDING_TYPE = BuildingType.CITY_ORIGINAL;

	public static enum BuildingType{
		CITY_ORIGINAL, CITY_PANELS, CITY_BRICKS, CITY_FLOOR , CITY_DYNAMIC; 
	}

	// Compile settings - change here to get different city layouts, schemes and sorting variations, etc.
	
	/**
	 * The active mode to structure and color the methods and attributes.
	 * 
	 * @see Schemes
	 */
	public static Schemes SCHEME = Schemes.TYPES;

	/**
	 * Switch to control the elements of the classes to show. Methods and/or
	 * attributes can be shown.
	 * 
	 * @see ClassElementsModes
	 */
	public static ClassElementsModes CLASS_ELEMENTS_MODE = ClassElementsModes.METHODS_AND_ATTRIBUTES;

	/**
	 * Sets the arrangement of the elements of the classes.<br>
	 * Elements which are placed {@code FIRST} usually are placed on the bottom
	 * and the other elements on top of them.<br>
	 * If {@link CitySettings#SET_CLASS_ELEMENTS_MODE SET_CLASS_ELEMENTS_MODE}
	 * is set to an {@code ..._ONLY}-mode, this sort mode has no effect.
	 * 
	 * @see ClassElementsSortModesCoarse
	 * @see ClassElementsModes
	 * @see BuildingSegmentComparator
	 */
	public static ClassElementsSortModesCoarse CLASS_ELEMENTS_SORT_MODE_COARSE = ClassElementsSortModesCoarse.METHODS_FIRST;

	/**
	 * The active mode, how to sort the methods or attributes separately among
	 * each other.<br>
	 * This means a method is only compared to another method and an attribute
	 * is only compared to another attribute in this comparison, according their
	 * values.<br>
	 * If it is set to {@code SCHEME}, a secondary sorting is performed to place
	 * methods with high numbers of statements to the bottom.
	 * 
	 * @see ClassElementsSortModesFine ClassElementsSortModesFine
	 * @see SortPriorities_Visibility SortPriorities_Visibility
	 * @see #SET_CLASS_ELEMENTS_SORT_MODE_FINE_DIRECTION_REVERSED
	 * @see Methods.SortPriorities_Types
	 * @see Attributes.SortPriorities_Types
	 * @see BuildingSegmentComparator
	 */
	public static ClassElementsSortModesFine CLASS_ELEMENTS_SORT_MODE_FINE = ClassElementsSortModesFine.SCHEME;

	/**
	 * If {@code TRUE}, the order of the sorting, defined in
	 * {@link #SET_CLASS_ELEMENTS_SORT_MODE_FINE} is reversed.<br>
	 * If {@link SET_CLASS_ELEMENTS_SORT_MODE_FINE} is set to {@code SCHEME}, a
	 * secondary sorting is performed to place methods with high numbers of
	 * statements to the bottom. This behavior isn't influenced by this switch.
	 */
	public static boolean CLASS_ELEMENTS_SORT_MODE_FINE_DIRECTION_REVERSED = false;

	/**
	 * Switch to show or hide building base in panels or bricks mode.<br>
	 * If set to {@code FALSE}, only districts and buildingSegments are visible.
	 */
	public static boolean SHOW_BUILDING_BASE = true;

	/**
	 * The active mode for the layout of the bricks/methods.<br>
	 * This setting has only an affect in brick-mode.
	 * 
	 * @see Bricks.Layout Layout
	 */
	public static Bricks.Layout BRICK_LAYOUT = Bricks.Layout.PROGRESSIVE;

	/**
	 * Switch for showing attributes as cylinders instead of boxes.<br>
	 * This setting has only an affect in panels-mode.
	 */
	public static boolean SHOW_ATTRIBUTES_AS_CYLINDERS = true;

	/**
	 * The active mode for the area between panels/methods.
	 * 
	 * @see Panels.SeparatorModes SeparatorModes
	 * @see Panels#SEPARATOR_HEIGHT SEPARATOR_HEIGHT
	 */
	public static Panels.SeparatorModes PANEL_SEPARATOR_MODE = Panels.SeparatorModes.SEPARATOR;

	// /Compile settings

	/**
	 * Defines how the methods and attributes are sorted and colored in the city
	 * model.
	 * 
	 * @see CitySettings#SET_SCHEME SET_SCHEME
	 */
	public static enum Schemes {
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
		TYPES;
	};

	/**
	 * Defines which elements of a class are to show.
	 * 
	 * @see CitySettings#SET_CLASS_ELEMENTS_MODE SET_CLASS_ELEMENTS_MODE
	 */
	public static enum ClassElementsModes {
		METHODS_ONLY, ATTRIBUTES_ONLY, METHODS_AND_ATTRIBUTES;
	}

	/**
	 * Defines which how the elements of a class are sorted.
	 * 
	 * @see CitySettings#SET_CLASS_ELEMENTS_SORT_MODE_COARSE
	 *      SET_CLASS_ELEMENTS_SORT_MODE_COARSE
	 */
	public static enum ClassElementsSortModesCoarse {
		UNSORTED, ATTRIBUTES_FIRST, METHODS_FIRST;
	}

	/**
	 * A list of types of a method with the associated priority value.<br>
	 * Highest priority/smallest number is placed on the bottom, lowest on top.
	 * 
	 * @see #SET_CLASS_ELEMENTS_SORT_MODE_FINE SET_CLASS_ELEMENTS_SORT_MODE_FINE
	 * @see SortPriorities_Visibility
	 * @see Methods.SortPriorities_Types
	 * @see Attributes.SortPriorities_Types
	 */
	public static enum ClassElementsSortModesFine {
		/** Class elements won't be sorted. */
		UNSORTED,

		/** Methods will be sorted according to the name. */
		ALPHABETICALLY,

		/**
		 * Methods will be sorted according to the active
		 * {@link CitySettings#SET_CLASS_ELEMENTS_SORT_MODE_FINE
		 * SET_CLASS_ELEMENTS_SORT_MODE_FINE}.
		 */
		SCHEME,

		/** Methods will be sorted according to there number of statements. */
		NOS;
	}

	/**
	 * A list of visibility modifiers of a method with the associated priority
	 * value.<br>
	 * Highest priority/smallest number is placed on the bottom, lowest on top.
	 * 
	 * @see #SET_CLASS_ELEMENTS_SORT_MODE_FINE SET_CLASS_ELEMENTS_SORT_MODE_FINE
	 * @see ClassElementsSortModesFine
	 * 
	 */
	public static enum SortPriorities_Visibility {;
		public static int PRIVATE = 1;
		public static int PROTECTED = 2;
		public static int PACKAGE = 3;
		public static int PUBLIC = 4;
	}

	public static enum Methods {;

		/**
		 * A list of types of a method with the associated priority value.<br>
		 * Highest priority/smallest number is placed on the bottom, lowest on
		 * top.
		 * 
		 * @see CitySettings#SET_CLASS_ELEMENTS_SORT_MODE_FINE
		 *      SET_CLASS_ELEMENTS_SORT_MODE_FINE
		 * @see ClassElementsSortModesFine
		 * @see SortPriorities_Visibility
		 */
		public static enum SortPriorities_Types {;

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

	public static enum Attributes {;

		/**
		 * A list of types of a method with the associated priority value.<br>
		 * Highest priority/smallest number is placed on the bottom, lowest on
		 * top.
		 * 
		 * @see CitySettings#SET_CLASS_ELEMENTS_SORT_MODE_FINE
		 *      SET_CLASS_ELEMENTS_SORT_MODE_FINE
		 * @see ClassElementsSortModesFine
		 */
		public static enum SortPriorities_Types {;

			/** Type is a primitive like {@code boolean}, {@code int}. */
			public static int PRIMITVE = 1;

			/** Type is a (Non-wrapper) class, collection, etc. */
			public static int COMPLEX = 2;

		}

	}

	public static enum Bricks {;

		/**
		 * Defines the layout for the BuildingSegments of the city model, which
		 * represents the methods and/or attributes of a class.
		 * 
		 * @see CitySettings#SET_BRICK_LAYOUT SET_BRICK_LAYOUT
		 */
		public static enum Layout {

			/**
			 * One-dimensional bricks layout, where the segments simply are
			 * placed on top of the other.
			 */
			STRAIGHT,

			/**
			 * Three-dimensional brick layout, where the base area is computed
			 * depending on the {@link CitySettings#SET_CLASS_ELEMENTS_MODE
			 * SET_CLASS_ELEMENTS_MODE}.<br>
			 * If only methods are shown, the base area is computed by the
			 * number of attributes and vice versa.<br>
			 * In case of methods and attributes are shown, the base area is
			 * computed by the sum of the numbers of attributes and methods
			 * inside the class.
			 * <p>
			 * When {@link CitySettings#SET_CLASS_ELEMENTS_MODE
			 * SET_CLASS_ELEMENTS_MODE} is set to
			 * {@code METHODS_AND_ATTRIBUTES}, the {@code BALANCED} layout and
			 * {@link Layout#PROGRESSIVE PROGRESSIVE} layout are identical.
			 */
			BALANCED,

			/**
			 * Three-dimensional brick layout, where the base area is computed
			 * depending on the {@link CitySettings#SET_CLASS_ELEMENTS_MODE
			 * SET_CLASS_ELEMENTS_MODE}.<br>
			 * If only methods are shown, the base area is computed by the
			 * number of methods and vice versa. So the aspect lies on only one
			 * type of element of a class and is visualized.
			 * <p>
			 * When {@link CitySettings#SET_CLASS_ELEMENTS_MODE
			 * SET_CLASS_ELEMENTS_MODE} is set to
			 * {@code METHODS_AND_ATTRIBUTES}, the {@link Layout#BALANCED
			 * PROGRESSIVE} layout and {@code PROGRESSIVE} layout are identical.
			 */
			PROGRESSIVE;

		}

		// Measurements Bricks
		public static double BRICK_SIZE = 1;
		public static double BRICK_HORIZONTAL_MARGIN = 0.5;	//horizontal distance to parent
		public static double BRICK_HORIZONTAL_GAP = 0.2;		//horizontal distance to neighbor
		public static double BRICK_VERTICAL_MARGIN = 0.2;		//vertical distance to parent
		public static double BRICK_VERTICAL_GAP = 0.2;		//vertical distance to neighbor

	}

	public enum Panels {
		;

		/**
		 * Defines the the space between the panels.<br>
		 * The panels can either touch each other without a gap, leave a gap
		 * between them, or fill the space with a separator of a defined color.
		 * 
		 * @see CitySettings#SET_PANEL_SEPARATOR_MODE SET_PANEL_SEPARATOR_MODE
		 */
		public static enum SeparatorModes {

			/**
			 * No space between the panels and they are placed on top of each
			 * other.
			 */
			NONE,

			/**
			 * The panels have a free space between them and don't touch each
			 * other.
			 * 
			 * @see Panels#PANEL_VERTICAL_GAP PANEL_VERTICAL_GAP
			 */
			GAP,

			/**
			 * Between the panels separators are placed with a fix height and
			 * color.
			 * 
			 * @see Panels#SEPARATOR_HEIGHT SEPARATOR_HEIGHT
			 */
			SEPARATOR;

		}

		/**
		 * Multiplier for height of a panel, declared in
		 * {@link Panels#PANEL_HEIGHT_UNIT PANEL_HEIGHT_UNIT}. The elements of
		 * this array are threshold values for the number of statements inside
		 * the method and are multiplied with the index+1, so the product will
		 * be the actual height of the panel.<br>
		 * The values are inclusive.
		 * 
		 * <p>
		 * Examples:<br>
		 * - The method has 3 statements inside 3 smaller or equal 5 = index == 0 
		 * multiplier == 1.<br>
		 * - The method has 5 statements inside 5 smaller or equal 5 = index == 0 
		 * multiplier == 1.<br>
		 * - The method has 35 statements inside  35 smaller or equal 50 = index == 2
		 * multiplier == 3.<br>
		 */
//		public static int[] PANEL_HEIGHT_THRESHOLD_NOS = { 5, 20, 50, 200 };
//		public static int[] PANEL_HEIGHT_THRESHOLD_NOS = { 3, 7, 17, 40, 100, 230 };
		public static int[] PANEL_HEIGHT_THRESHOLD_NOS = { 3, 6, 12, 24, 48, 96, 144, 192, 240 };

		// Measurements Panels
		/** height is multiplied by {@link Panels#PANEL_HEIGHT_THRESHOLD_NOS PANEL_HEIGHT_THRESHOLD_NOS}*/ 
		public static double PANEL_HEIGHT_UNIT = 0.5;
		public static double PANEL_HORIZONTAL_MARGIN = 0.5;	//horizontal distance to parent
		public static double PANEL_VERTICAL_MARGIN = 0.25;	//vertical distance to parent
		public static double PANEL_VERTICAL_GAP = 0.125;		//vertical distance to neighbor

		public static double SEPARATOR_HEIGHT = 0.125;
	}
	
	public static Original.BuildingMetric ORIGINAL_BuildingMetric = Original.BuildingMetric.NONE;

	public static enum Original {
		;
		public static enum BuildingMetric {
			NONE,
			NOS;
		}
	}

	// Measurements
	public static double WIDTH_MIN = 1;
	public static double HEIGHT_MIN = 1;

	public static double BLDG_horizontalMargin = 3;		//horizontal distance to parent
	public static double BLDG_horizontalGap = 3;			//horizontal distance to neighbor
	public static double BLDG_verticalMargin = 1;

	// Colors
	public static RGBColor PCKG_colorStart = new RGBColor(150, 150, 150);
	public static RGBColor PCKG_colorEnd = new RGBColor(240, 240, 240); //from CodeCity
	public static String   PCKG_COLOR_HEX = "#969696";
	public static RGBColor CLSS_colorStart = new RGBColor(19, 22, 21);
	public static RGBColor CLSS_colorEnd = new RGBColor(0, 255, 0); //from CodeCity
	public static RGBColor CLSS_color = new RGBColor(53, 53, 89);
	public static String   CLSS_COLOR_HEX =  "#353559";

	public static RGBColor COLOR_BLUE = new RGBColor(153, 255, 204);
	public static String   COLOR_BLUE_HEX = "#99FFCC";
	public static RGBColor COLOR_AQUA = new RGBColor(153, 204, 255);
	public static String   COLOR_AQUA_HEX = "#99CCFF";
	public static RGBColor COLOR_LIGHT_GREEN = new RGBColor(204, 255, 153);
	public static String   COLOR_LIGHT_GREEN_HEX = "#CCFF99";
	public static RGBColor COLOR_DARK_GREEN = new RGBColor(153, 255, 153);
	public static String   COLOR_DARK_GREEN_HEX = "#99FF99";
	public static RGBColor COLOR_YELLOW = new RGBColor(255, 255, 153);
	public static String   COLOR_YELLOW_HEX = "#FFFF99";
	public static RGBColor COLOR_ORANGE = new RGBColor(255, 204, 153);
	public static String   COLOR_ORANGE_HEX = "#FFCC99";
	public static RGBColor COLOR_RED = new RGBColor(255, 153, 153);
	public static String   COLOR_RED_HEX = "#FF9999";
	public static RGBColor COLOR_PINK = new RGBColor(255, 153, 255);
	public static String   COLOR_PINK_HEX = "#FF99FF";
	public static RGBColor COLOR_VIOLET = new RGBColor(153, 153, 255);
	public static String   COLOR_VIOLET_HEX = "#9999FF";
	public static RGBColor COLOR_LIGHT_GREY = new RGBColor(204, 204, 204);
	public static RGBColor COLOR_DARK_GREY = new RGBColor(153, 153, 153);
	public static RGBColor COLOR_WHITE = new RGBColor(255, 255, 255);
	public static RGBColor COLOR_BLACK = new RGBColor(0, 0, 0);
	public static String   COLOR_BLACK_HEX = "#000000";
	
	
	//Dynamic City Colors
	
	public static RGBColor DYNAMIC_CLSS_colorStart = new RGBColor(250,150,92);
	public static RGBColor DYNAMIC_CLSS_colorEnd   = new RGBColor(254,178,128);
	public static RGBColor DYNAMIC_METHOD = new RGBColor(115,94,185);
	public static RGBColor DYNAMIC_PCKG_colorStart = new RGBColor(35,134,44);
	public static RGBColor DYNAMIC_PCKG_colorEnd = new RGBColor(123,205,141);
}