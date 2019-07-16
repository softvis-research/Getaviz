package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

public class Disk {

    private DatabaseConnector connector;

    private double transparency;
    private boolean methodTypeMode;
    private boolean methodDisks;

    private boolean dataDisks;

    private double height;
    private double ringWidth;
    private double minArea;
    private Long parent;
    private Long attribute;
    private String color;

    private SettingsConfiguration config;

    // Für namespace aufgerufen
    public Disk(SettingsConfiguration config, DatabaseConnector connector) {
        this.transparency = config.getRDNamespaceTransparency();
        this.height = config.getRDHeight();
        this.ringWidth = config.getRDRingWidth();
        this.config = config;
        this.connector = connector;

    }

    public Disk(Node structure, Long parent, SettingsConfiguration config, DatabaseConnector connector, double transparency) {
        this.methodTypeMode = config.isMethodTypeMode();
        this.methodDisks = config.isMethodDisks();
        this.dataDisks = config.isDataDisks();
        this.height = config.getRDHeight();
        this.ringWidth = config.getRDRingWidth();
        this.minArea = config.getRDMinArea();
        this.config = config;
        this.connector = connector;
        this.transparency = transparency;
        this.parent = parent;
        run(structure, parent);
    }

    public Disk(Long attribute, Long parent, SettingsConfiguration config, DatabaseConnector connector, double transparency,
                String color, double ringWidth) {
        this.height = config.getRDHeight();
        this.ringWidth = ringWidth;
        this.config = config;
        this.connector = connector;
        this.transparency = transparency;
        this.parent = parent;
        this.attribute = attribute;
        this.color = color;
    }

    public Long getParent() {
        return this.parent;
    }

    public Long getAttribute() {
        return this.attribute;
    }

    public String getColor() {
        return this.color;
    }

    private void run(Node structure, Long parent) {
        String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
                height, transparency, config.getRDClassColor());
        long disk = connector.addNode(CypherCreateNode.create(parent, structure.id(), Labels.Disk.name(), properties), "n").id();
        StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = " + structure.id() +
                " AND EXISTS(m.hash) RETURN m");
        StatementResult fields = connector.executeRead("MATCH (n)-[:DECLARES]->(f:Field) WHERE ID(n) = " + structure.id() +
                " AND EXISTS(f.hash) RETURN f");

        if (methodTypeMode) {
            methods.forEachRemaining((result) -> {
                Node method = result.get("m").asNode();
                if (method.hasLabel(Labels.Constructor.name())) {
                    JQA2RD.toStack(new DiskSegment(method, disk, config, config.getRDMethodTransparency(),
                            config.getRDMethodColor()));
                } else {
                    JQA2RD.toStack((new Disk(method.id(), disk, config, connector, config.getRDMethodTransparency(),
                            config.getRDMethodColor(), config.getRDRingWidth())));
                }
            });
            fields.forEachRemaining((result) -> {
                JQA2RD.toStack((new Disk(result.get("f").asNode().id(), disk, config, connector, config.getRDDataTransparency(),
                        config.getRDDataColor(), config.getRDRingWidthAD())));
            });
        } else {
            if (dataDisks) {
                fields.forEachRemaining((result) -> {
                    JQA2RD.toStack((new Disk(result.get("f").asNode().id(), disk, config, connector, config.getRDDataTransparency(),
                            config.getRDDataColor(), config.getRDRingWidthAD())));
                });
            } else {
                fields.forEachRemaining((result) -> {
                    JQA2RD.toStack((new DiskSegment(result.get("f").asNode().id(), disk, config, config.getRDDataTransparency(),
                            config.getRDDataColor())));
                });
            }
            if (methodDisks) {
                methods.forEachRemaining((result) -> JQA2RD.toStack((new Disk(result.get("m").asNode().id(), disk, config, connector,
                        config.getRDMethodTransparency(), config.getRDMethodColor(), config.getRDRingWidth()))));
            } else {
                methods.forEachRemaining((result) -> JQA2RD.toStack(new DiskSegment(result.get("m").asNode(), disk, config,
                        config.getRDClassTransparency(), config.getRDMethodColor())));
            }
        }
        connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + structure.id() +
                " AND EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) RETURN t").
                forEachRemaining((result) -> JQA2RD.toStack((new Disk(result.get("t").asNode(), disk, config, connector,
                        config.getRDClassTransparency()))));
    }
}
