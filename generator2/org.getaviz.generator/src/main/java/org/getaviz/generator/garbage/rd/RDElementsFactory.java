package org.getaviz.generator.garbage.rd;

import org.getaviz.generator.garbage.Labels;
import org.neo4j.driver.v1.Record;

public class RDElementsFactory {

    private boolean methodTypeMode;
    private boolean methodDisks;
    private boolean dataDisks;

    public RDElementsFactory(boolean methodTypeMode, boolean methodDisks, boolean dataDisks) {
        this.methodTypeMode = methodTypeMode;
        this.methodDisks = methodDisks;
        this.dataDisks = dataDisks;
    }

    public RDElement createFromMethod(Record result, double height, double ringWidth, double ringWidthAD,
                                      double transparency, double minArea, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if (methodTypeMode) {
            if (result.get("node").asNode().hasLabel(Labels.Constructor.name())) {
                return new DiskSegment(id, typeID, height, transparency, minArea, color, result.get("line").asInt(0));
            } else {
                return new SubDisk(id, typeID, ringWidth, height, transparency, color);
            }
        } else {
            if (methodDisks) {
                return new SubDisk(id, typeID, ringWidthAD, height, transparency, color);
            } else {
                return new DiskSegment(id, typeID, height, transparency, minArea, color, result.get("line").asInt(0));
            }
        }
    }

    public RDElement createFromField(Record result, double ringWidthAD, double height, double transparency, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if (methodTypeMode) {
            return new SubDisk(id, typeID, ringWidthAD, height, transparency, color);
        } else {
            if (dataDisks) {
                return new SubDisk(id, typeID, ringWidthAD, height, transparency, color);
            } else {
                return new DiskSegment(id, typeID, height, transparency, color);
            }
        }
    }

    public RDElement createFromFunction(Record result, double height, double ringWidthAD,
                                        double transparency, double minArea, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if(methodDisks) {
            return new SubDisk(id, typeID, ringWidthAD, height, transparency, color);
        } else {
            return new DiskSegment(id, typeID, height, transparency, minArea, color, result.get("lineCount").asInt(0));
        }
    }

    public RDElement createFromVariable(Record result, double height, double ringWidthAD, double transparency, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if (dataDisks) {
            return new SubDisk(id, typeID, ringWidthAD, height, transparency, color);
        } else {
            return new DiskSegment(id, typeID, height, transparency, color);
        }
    }
}
