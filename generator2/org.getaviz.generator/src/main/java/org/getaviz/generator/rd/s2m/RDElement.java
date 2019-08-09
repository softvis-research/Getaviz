package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;

public interface RDElement {

    long getParentVisualizedNodeID();
    long getVisualizedNodeID();
    long getId();
    void setParentID(long id);
    void setCrossSection (String crossSection);
    void JQA2RDWriteToDatabase(DatabaseConnector connector);
    void RD2RDWriteToDatabase(DatabaseConnector connector);

}
