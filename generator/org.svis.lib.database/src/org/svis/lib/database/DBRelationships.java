package org.svis.lib.database;

import org.neo4j.graphdb.RelationshipType;

/**
 * Specifies valid Relationships for neo4j 
 */

public enum DBRelationships implements RelationshipType  {
	
	HAS_SNAPSHOT,
	
	HAS_PACKAGE,
	
	HAS_STRUCTURE,
	
	HAS_METHOD,
	
	HAS_ATTRIBUTE,
	
	/**
	 * Famix element → Glyph
	 */
	VISUALIZED_BY, 	// famix element -> Glyph
	
	/**
	 * sub class → super class
	 */
	INHERITS,
	
	/**
	 * method → method
	 */
	CALLS,
	
	/**
	 * method → attribute
	 */
	ACCESSES
}