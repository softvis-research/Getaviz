package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;

public interface RDElement {

    long getParentVisualizedNodeID();
    long getVisualizedNodeID();
    long getId();
    void setParentID(long id);
    void createNodeForVisualization(DatabaseConnector connector);

}
