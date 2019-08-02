package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;

class RDElementsFactory {

    private Model model;
    private boolean methodTypeMode;
    private boolean methodDisks;
    private boolean dataDisks;

    RDElementsFactory(Model model) {
        this.model = model;
        this.methodTypeMode = model.isMethodTypeMode();
        this.methodDisks = model.isMethodDisks();
        this.dataDisks = model.isDataDisks();
    }

    public void create (StatementResult results, double height, double ringWidth, double ringWidthAD,
                        double methodTransparency, double classTransparency, double minArea, String methodColor,
                        String classColor) {
        if (methodTypeMode) {
            results.forEachRemaining((result) -> {
                if (result.get("node").asNode().hasLabel(Labels.Constructor.name())) {
                    DiskSegment diskSegment = new DiskSegment(result.get("node").asNode().id(), result.get("tID").asLong(),
                            height, methodTransparency, minArea, methodColor, result.get("line").asInt(0));
                    model.setList(diskSegment);
                } else {
                    Disk disk = new Disk(result.get("node").asNode().id(), result.get("tID").asLong(), ringWidth, height,
                            methodTransparency, methodColor);
                    model.setList(disk);
                }
            });
        } else {
            if (methodDisks) {
                results.forEachRemaining((result) -> {
                    Disk disk = new Disk(result.get("node").asNode().id(), result.get("tID").asLong(), ringWidthAD,
                            height, methodTransparency, methodColor);
                    model.setList(disk);
                });
            } else {
                results.forEachRemaining((result) -> {
                    DiskSegment diskSegment = new DiskSegment(result.get("node").asNode().id(), result.get("tID").asLong(), height,
                            classTransparency, minArea, classColor, result.get("line").asInt(0));
                    model.setList(diskSegment);
                });
            }
        }
    }

    public void create(StatementResult results, double ringWidthAD, double height, double dataTransparency,
                       String dataColor) {
        if (methodTypeMode) {
            results.forEachRemaining((result) -> {
                Disk disk = new Disk(result.get("node").asNode().id(), result.get("tID").asLong(), ringWidthAD, height,
                        dataTransparency, dataColor);
                model.setList(disk);
            });
        } else {
            if (dataDisks) {
                results.forEachRemaining((result) -> {
                    Disk disk = new Disk(result.get("node").asNode().id(), result.get("tID").asLong(), ringWidthAD, height,
                            dataTransparency, dataColor);
                    model.setList(disk);
                });
            } else {
                results.forEachRemaining((result) -> {
                    DiskSegment diskSegment = new DiskSegment(result.get("node").asNode().id(), result.get("tID").asLong(),
                            height, dataTransparency, dataColor);
                    model.setList(diskSegment);
                });
            }
        }
    }
}
