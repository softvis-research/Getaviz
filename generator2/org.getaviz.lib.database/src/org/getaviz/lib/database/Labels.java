package org.getaviz.lib.database;

import org.neo4j.graphdb.Label;

public enum Labels implements Label {
	Anonymous, Inner, Dummy, Primitive, Parameterized, Package, Member, Type, Method, Constructor, Getter, Setter, Parameter, Field, Class, Interface, Enum, Annotation, 
	Model, RD, Disk, DiskSegment, City, District, Building, BuildingSegment, Floor, Chimney, Configuration, Position, PanelSeparator, 
	Cylinder, Box, TRANSFORMED, Function, TranslationUnit, Variable, Condition, SingleCondition, Not, And, Or, Struct, Union, EnumConstant,
	C
}