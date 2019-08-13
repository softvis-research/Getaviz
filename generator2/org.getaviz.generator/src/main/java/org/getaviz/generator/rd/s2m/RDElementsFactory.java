package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.Record;

class RDElementsFactory {

    private boolean methodTypeMode;
    private boolean methodDisks;
    private boolean dataDisks;

    RDElementsFactory(boolean methodTypeMode, boolean methodDisks, boolean dataDisks) {
        this.methodTypeMode = methodTypeMode;
        this.methodDisks = methodDisks;
        this.dataDisks = dataDisks;
    }

    RDElement createFromMethod(Record result, double height, double ringWidth, double ringWidthAD,
                                      double transparency, double minArea, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if (methodTypeMode) {
            if (result.get("node").asNode().hasLabel(Labels.Constructor.name())) {
                return new DiskSegment(id, typeID, height, transparency, minArea, color, result.get("line").asInt(0));
            } else {
                return new Disk(id, typeID, ringWidth, height, transparency, color);
            }
        } else {
            if (methodDisks) {
                return new Disk(id, typeID, ringWidthAD, height, transparency, color);
            } else {
                return new DiskSegment(id, typeID, height, transparency, minArea, color, result.get("line").asInt(0));
            }
        }
    }

    RDElement createFromField(Record result, double ringWidthAD, double height, double transparency, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if (methodTypeMode) {
            return new Disk(id, typeID, ringWidthAD, height, transparency, color);
        } else {
            if (dataDisks) {
                return new Disk(id, typeID, ringWidthAD, height, transparency, color);
            } else {
                return new DiskSegment(id, typeID, height, transparency, color);
            }
        }
    }
}
