package org.svis.lib.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphdb.Label;
import org.svis.xtext.famix.FAMIXAnnotationType;
import org.svis.xtext.famix.FAMIXAttribute;
import org.svis.xtext.famix.FAMIXClass;
import org.svis.xtext.famix.FAMIXEnum;
import org.svis.xtext.famix.FAMIXMethod;
import org.svis.xtext.famix.FAMIXNamespace;
import org.svis.xtext.famix.FAMIXParameterizableClass;
import org.svis.xtext.famix.FAMIXStructure;

public enum DBLabel implements Label {
	SYSTEM,
	// Metamodels
	FAMIX,
	DYNAMIX,
	HISMO,
	// RD Elements
	RD,
	DISK,
	DISKSEGMENT,
	// City Elements
	CITY,
	BUILDING,
	DISTRICT,
	// Famix Elements
	FAMIXELEMENT,
	FAMIXSTRUCTURE,
	PACKAGE, 
	CLASS,
	PCLASS,
	ANNOTATIONTYPE,
	ENUM,
	METHOD,
	ATTRIBUTE,
	ENUMVALUE,
	// Debug
	UNKNOWN;
	
	static Log log = LogFactory.getLog(DBLabel.class);
	
	public static DBLabel getLabel(FAMIXStructure structure) {
		if (structure instanceof FAMIXNamespace) {
			return PACKAGE;
		}
		
		if (structure instanceof FAMIXClass) {
			return CLASS;
		}
		
		if (structure instanceof FAMIXParameterizableClass) {
			return PCLASS;
		}
		
		if (structure instanceof FAMIXAnnotationType) {
			return ANNOTATIONTYPE;
		}
		
		if (structure instanceof FAMIXEnum) {
			return ENUM;
		}
		
		if (structure instanceof FAMIXAttribute) {
			return ATTRIBUTE;
		}
		
		if (structure instanceof FAMIXMethod) {
			return METHOD;
		}
		
		log.warn ("Unknown DBLabel for " + structure.getClass());
		return UNKNOWN;
	}
}
