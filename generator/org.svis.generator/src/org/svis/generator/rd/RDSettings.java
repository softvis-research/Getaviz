package org.svis.generator.rd;

/** Set options  
 * 
 * With this enum you can set all options that affect the visualization
 * like colors, fix properties and so on
 */

public enum RDSettings {;
	public static final boolean SHOW_HISTORIES = false;
	public static final boolean SHOW_CLASS_MEMBERS = false;
	public static final double DATA_FACTOR = 4;
	public static final double METHOD_FACTOR = 1;
	public static final double HEIGHT = 1;
	public static final double RING_WIDTH = 2;
	/**
	* Sets the ring width of the method disks
	* Only relevant if disk of type FAMIX.Method exist
	*/
	public static final double RING_WIDTH_MD = 0;
	/**
	 * Equal to RING_WIDTH_MD but for attribute disks
	 */
	public static final double RING_WIDTH_AD = 0;
	public static final double MIN_AREA = 10;
	public static final double NAMESPACE_TRANSPARENCY = 0;
	public static final double CLASS_TRANSPARENCY = 0;
	public static final double METHOD_TRANSPARENCY = 0;
	public static final double DATA_TRANSPARENCY = 0;
	public static final String CLASS_COLOR = 53/255.0 + " " + 53/255.0 + " " + 89/255.0;
	public static final String CLASS_COLOR_HEX = "#353559";
	public static final String DATA_COLOR = 255/255.0 + " " + 252/255.0 + " " + 25/255.0;
	public static final String DATA_COLOR_HEX = "#FFFC19";
	public static final String METHOD_COLOR = 20/255.0 + " " + 133/255.0 + " " + 204/255.0;
	public static final String METHOD_COLOR_HEX = "#1485CC";
	public static final String NAMESPACE_COLOR = 150/255.0 + " " + 150/255.0 + " " + 150/255.0;
	public static final String NAMESPACE_COLOR_HEX = "#969696";
	public static final String METHOD_INVOCATION_COLOR = 120/255.0 + " " + 10/255.0 + " " + 50/255.0;
	public static final int HEIGHT_BOOST = 8;
	public static final float HEIGHT_MULTIPLICATOR = 50.0f;
	/**
	 * If true the Methods will be visualized as Disks instead of DiskSegments
	 */
	public static boolean METHOD_DISKS = false;
	/**
	 * If true Attributes will be visualized as disks
	 */ 
	public static boolean DATA_DISKS = false;
	/**
	 * If set true visualization will be based on the method type
	 * --> check HIDE_PRIVATE_ELEMENTS in FAMIXSettings for 
	 * 		visualization of Privates
	 */
	public static boolean METHOD_TYPE_MODE = false;   
	/**
	 * Depending on which value is set X3D or X3DOM will be generated
	 */
	public static OutputFormat OUTPUT_FORMAT = OutputFormat.X3D;
	
	public static enum OutputFormat {
		X3D,X3DOM,SimpleGlyphsJson,X3D_COMPRESSED,AFrame, D3
	}
	
	public static ClassSize CLASS_SIZE = ClassSize.BETWEENNESS_CENTRALITY;
	
	public static enum ClassSize {
		NONE, BETWEENNESS_CENTRALITY
	}
	
	/**
	 * Depending on the value set metrics will not represented, by height
	 * or in a dynamic way which can either be Luminance or Frequency 
	 * --> Output Files differ depending on the value chosen
	 */
	public static MetricRepresentation METRIC_REPRESENTATION = MetricRepresentation.NONE;
	
	public static enum MetricRepresentation {
		NONE,HEIGHT,LUMINANCE,FREQUENCY
	}	
	/**
	 * Depending on this Variable the Dynamix Visualization will be created,
	 * it can either be in a static or dynamic way 
	 */
	public static InvocationRepresentation INVOCATION_REPRESENTATION = InvocationRepresentation.NONE;

	public static enum InvocationRepresentation {
		NONE,MOVING_SPHERES,FLASHING_METHODS,MOVING_FLASHING
	}
	/**
	 * Sets in which way the Historic Evolution
	 * of the analyzed Software should be represented, 
	 * it can either be in a static or dynamic way 
	 */
	public static EvolutionRepresentation EVOLUTION_REPRESENTATION = EvolutionRepresentation.TIME_LINE;
	
	public static enum EvolutionRepresentation {
		TIME_LINE,DYNAMIC_EVOLUTION,MULTIPLE_TIME_LINE,MULTIPLE_DYNAMIC_EVOLUTION
	}
	public static Variant VARIANT = Variant.STATIC;
	
	public static enum Variant {
		STATIC,DYNAMIC
	}
	/* 
	 *TODO at the Moment Metrics are based on Method complexity add more ways to do that 
	 *for example LOC
	 *public static Metrics METRICS = Metrics.COMPLEXITY;
	 *public static enum Metrics {
	 *COMPLEXITY
	}*/

}