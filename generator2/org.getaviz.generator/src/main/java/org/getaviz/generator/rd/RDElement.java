package org.getaviz.generator.rd;

import org.getaviz.generator.database.DatabaseConnector;

public interface RDElement {

    long getParentVisualizedNodeID();
    long getVisualizedNodeID();
    long getID();
    void setParentID(long id);
    void updateNode(DatabaseConnector connector);
    void createNode(DatabaseConnector connector);
    void createParentRelationship(DatabaseConnector connector);
}
