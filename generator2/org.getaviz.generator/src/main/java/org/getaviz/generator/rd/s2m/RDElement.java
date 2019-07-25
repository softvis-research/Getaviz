package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;

public interface RDElement {

    long getParentVisualizedNodeID();
    long getVisualizedNodeID();
    void setParentVisualizedNodeID(long id);
    void write(DatabaseConnector connector);
}
