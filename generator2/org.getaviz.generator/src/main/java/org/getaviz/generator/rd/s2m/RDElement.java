package org.getaviz.generator.rd.s2m;

public interface RDElement {

    long getParentID();
    long getVisualizedNodeID();
    void setNewParentID (long id);
    void write();
}
