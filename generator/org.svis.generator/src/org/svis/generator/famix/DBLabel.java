package org.svis.generator.famix;

import org.neo4j.graphdb.Label;

public class DBLabel {

	public static Label PACKAGE = createLabel("Package");
	public static Label MEMBER = createLabel("Member");
	public static Label TYPE = createLabel("Type");
	public static Label METHOD = createLabel("Method");
	public static Label FIELD = createLabel("Field");
	public static Label CLASS = createLabel("Class");
	public static Label INTERFACE = createLabel("Interface");
	public static Label ENUM = createLabel("Enum");
	public static Label ANNOTATION = createLabel("Annotation");
	
	public static Label createLabel(String name) {
		return Label.label(name);
	}
}
