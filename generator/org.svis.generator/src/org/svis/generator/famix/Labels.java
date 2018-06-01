package org.svis.generator.famix;

import org.neo4j.graphdb.Label;

public enum Labels implements Label {
	Package, Member, Type, Method, Field, Class, Interface, Enum, Annotation
}
