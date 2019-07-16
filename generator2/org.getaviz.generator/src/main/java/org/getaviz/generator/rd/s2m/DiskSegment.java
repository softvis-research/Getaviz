package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.neo4j.driver.v1.types.Node;

public class DiskSegment {

    private double height;
    private String properties;
    private Long visualizedNode;
    private Long parent;


    public DiskSegment (Long attribute, Long parent, SettingsConfiguration config, double transparency,
                 String color) {
        this.height = config.getRDHeight();
        this.visualizedNode = attribute;
        this.parent = parent;
        properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", 1.0, height,
                transparency, color);
    }

    public DiskSegment (Node structure, Long parent, SettingsConfiguration config, double transparency,
                 String color) {
        this.height = config.getRDHeight();
        this.visualizedNode = structure.id();
        this.parent = parent;
        double minArea = config.getRDMinArea();

        int numberOfStatements = structure.get("effectiveLineCount").asInt(0);
        double size = numberOfStatements;
        if (numberOfStatements <= minArea) {
            size = minArea;
        }
        properties = String.format(
                "height: %f, transparency: %f, size: %f, color: \'%s\'", height, transparency, size, color);
    }

    public String getProperties() {
        return properties;
    }

    public Long getParent(){
        return parent;
    }

    public Long getVisualizedNode() {
        return visualizedNode;
    }
}
