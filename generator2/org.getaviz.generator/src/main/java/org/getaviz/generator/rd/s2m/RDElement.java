package org.getaviz.generator.rd.s2m;

public interface RDElement {

    long getParentID();
    void setNewParentID (long id);
    long getVisualizedNodeID();
    long getNewParentID();
    String getProperties();
}
