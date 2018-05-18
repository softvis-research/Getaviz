package org.svis.generator.famix;

public enum FAMIXSettings{;
	
	public static boolean MERGE_PACKAGES = false;
	/**
	 *  if set true there will be one root package which contains
	 *  all the other root packages
	 */
	public static boolean MASTER_ROOT = false;
	/**
	 * if set true all private Elements will not be shown in visualization
	 * This includes methods, attributes and structures
	 * this boolean also affects the METHOD_TYPE_MODE in RDSettings.java
	 * if both is set true private attributes without getters or setters will 
	 * be hidden as well as inner classes and methods
	 * 
	 */
	public static boolean HIDE_PRIVATE_ELEMENTS = false;
	/**
	 * if set true attributes will be sorted lengthwise,
	 * Longest value first in descending order 
	 */
	public static boolean ATTRIBUTE_SORT_SIZE = false;
	/**
	 * enum Value depending on which parser is used
	 */
	public static FamixParser FAMIX_PARSER = FamixParser.VERVEINEJ;
	
	public static String DATABASE_NAME = "../databases/graph.db";
	
	public static enum FamixParser {
		
		JDT2FAMIX,VERVEINEJ,JQA_BYTECODE;	
	}
}