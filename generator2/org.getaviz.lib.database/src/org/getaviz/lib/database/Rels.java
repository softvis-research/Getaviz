package org.getaviz.lib.database;

import org.neo4j.graphdb.RelationshipType;

public enum Rels implements RelationshipType {
	CONTAINS, DECLARES, EXTENDS, INVOKES, WRITES, READS, HAS, USED, VISUALIZES, OF_TYPE, RETURNS, DEPENDS_ON, NEGATES, CONNECTS
}
