package org.getaviz.generator.rd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.Record;

public class RDElementsFactory {

    private final boolean methodTypeMode;
    private final boolean methodDisks;
    private final boolean dataDisks;
    private final Log log = LogFactory.getLog(this.getClass());

    public RDElementsFactory(boolean methodTypeMode, boolean methodDisks, boolean dataDisks, double dataFactor) {
        this.methodTypeMode = methodTypeMode;
        this.methodDisks = methodDisks;
        this.dataDisks = dataDisks;
    }

    public RDElement createFromMethod(Record result, double height, double ringWidth, double transparency, double minArea, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if (methodTypeMode) {
            if (result.get("node").asNode().hasLabel(Labels.Constructor.name())) {
                return new DiskSegment(id, typeID, height, transparency, color, result.get("line").asInt(0));
            } else {
                return new SubDisk(id, typeID, ringWidth, height, transparency, color);
            }
        } else {
            double loc = result.get("line").asDouble(0);
            if (methodDisks) {
                double subDiskWidth = loc;
                if (loc < minArea) {
                    subDiskWidth = minArea;
                }
                return new SubDisk(id, typeID, Math.sqrt(subDiskWidth), height, transparency, color);
            } else {
                double area = loc;
                if (loc < minArea) {
                    area = minArea;
                }
                return new DiskSegment(id, typeID, height, transparency, color, area);
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
                                        double minArea, double transparency, String color) {
        long id = result.get("node").asNode().id();
        long typeID = result.get("tID").asLong();
        if(methodDisks) {
            return new SubDisk(id, typeID, ringWidthAD, height, transparency, color);
        } else {
            double area = result.get("lineCount").asInt(0);
            if (area < minArea) {
                area = minArea;
            }
            return new DiskSegment(id, typeID, height, transparency, color, area);
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
