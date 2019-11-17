package org.getaviz.generator.rd.m2m;

import org.getaviz.generator.ColorGradient;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import java.util.ArrayList;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.rd.Disk;
import org.getaviz.generator.rd.DiskSegment;
import org.getaviz.generator.rd.MainDisk;
import org.getaviz.generator.rd.SubDisk;
import org.neo4j.driver.v1.types.Node;
import java.util.List;
import org.neo4j.driver.v1.StatementResult;

public class RD2RD implements Step {
    private DatabaseConnector connector = DatabaseConnector.getInstance();
    private Log log = LogFactory.getLog(RD2RD.class);
    private List<String> NS_colors;
    private double dataFactor;
    private ArrayList<SubDisk> subDisks = new ArrayList<>();
    private ArrayList<MainDisk> mainDisks = new ArrayList<>();
    private ArrayList<MainDisk> rootDisks = new ArrayList<>();
    private ArrayList<DiskSegment> innerSegments = new ArrayList<>();
    private ArrayList<DiskSegment> outerSegments = new ArrayList<>();

    public RD2RD(SettingsConfiguration config) {
        this.dataFactor = config.getRDDataFactor();
    }

    public void run() {
        log.info("RD2RD started");
        int namespaceMaxLevel = calculateNamespaceMaxLevel();
        createLists(namespaceMaxLevel);

        calculateData();
        calculateLayouts();
        writeToDatabase();
        log.info("RD2RD finished");
    }

    private int calculateNamespaceMaxLevel() {
        StatementResult length = connector.executeRead(
                "MATCH p=(n:Package)-[:CONTAINS*]->(m:Package) WHERE NOT (m)-[:CONTAINS]->(:Package) " +
                        "RETURN max(length(p)) AS length");
        return length.single().get("length").asInt() + 1;
    }

    private void createLists(int namespaceMaxLevel) {
        innerSegments = createInnerSegmentsList();
        outerSegments = createOuterSegmentsList();
        rootDisks = createRootDisksList();
        subDisks = createSubDisksList();
        mainDisks = createMainDisksList();
        setColorToDisk(namespaceMaxLevel);
        addSubDisks();
        addSegmentsToDisk();
    }

    private void calculateData() {
        subDisks.forEach(SubDisk::calculateAreaWithoutBorder);
        mainDisks.forEach(MainDisk::calculateAreaWithoutBorder);
    }

    private void calculateLayouts() {
        calculateDiskLayout();
        X3DSegmentLayout layout = new X3DSegmentLayout(subDisks);
        AFrameSegmentLayout aFrameSegmentLayout = new AFrameSegmentLayout(subDisks);
        layout.calculateInnerSegments();
        layout.calculateOuterSegments();
        aFrameSegmentLayout.calculateInnerSegments();
        aFrameSegmentLayout.calculateOuterSegments();
    }

    private ArrayList<DiskSegment> createInnerSegmentsList() {
        StatementResult result = connector.executeRead(
                "MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(field:Field) " +
                        "RETURN d.size AS size, ID(d) as id, ID(n) as parentID, field.name as name ORDER BY field.hash");
        return getDiskSegments(result);
    }

    private ArrayList<DiskSegment> getDiskSegments(StatementResult result) {
        ArrayList<DiskSegment> list = new ArrayList<>();
        result.forEachRemaining(f -> {
            long parentID = f.get("parentID").asLong();
            long id = f.get("id").asLong();
            double size = f.get("size").asDouble();
            String name = f.get("name").asString();
            DiskSegment innerSegment = new DiskSegment(parentID, id, size, dataFactor);
            innerSegment.setFqn(name);
            list.add(innerSegment);
        });
        return list;
    }

    private ArrayList<DiskSegment> createOuterSegmentsList() {
        StatementResult result = connector.executeRead(
                "MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(method:Method) " +
                        "RETURN d.size AS size, ID(d) as id, ID(n) as parentID ORDER BY method.hash");
        return getDiskSegments(result);
    }


    private ArrayList<MainDisk> createRootDisksList() {
        StatementResult result = connector.executeRead("MATCH (n:RD:Model)-[:CONTAINS]->(d:MainDisk)-[:VISUALIZES]->(element)" +
                " RETURN d.ringWidth AS ringWidth, d.height AS height," +
                " d.size AS size, ID(d) as id, ID(n) AS parentID, ID(element) AS visualizedID ORDER BY element.hash");
        return getMainDisks(result);
    }

    private ArrayList<MainDisk> getMainDisks(StatementResult result) {
        ArrayList<MainDisk> list = new ArrayList<>();
        result.forEachRemaining(d -> {
            long visualizedID = d.get("visualizedID").asLong();
            long parentID = d.get("parentID").asLong();
            long id = d.get("id").asLong();
            double ringWidth = d.get("ringWidth").asDouble();
            double height = d.get("height").asDouble();
            MainDisk disk = new MainDisk(visualizedID, parentID, id, ringWidth, height);
            list.add(disk);
            setPositionToPackages(disk);
        });
        return list;
    }

    private ArrayList<SubDisk> createSubDisksList() {
        ArrayList<SubDisk> list = new ArrayList<>();
        StatementResult result =  connector.executeRead("MATCH (p)-[:CONTAINS]->(d:SubDisk)-[:VISUALIZES]->(element) "
                + "RETURN d.ringWidth AS ringWidth, d.height as height," +
                " d.size AS size, ID(d) AS id, ID(p) AS parentID, ID(element) AS visualizedID ORDER BY element.hash");
        result.forEachRemaining(d -> {
            long visualizedID = d.get("visualizedID").asLong();
            long parentID = d.get("parentID").asLong();
            long id = d.get("id").asLong();
            double ringWidth = d.get("ringWidth").asDouble();
            double height = d.get("height").asDouble();
            SubDisk disk = new SubDisk(visualizedID, parentID, id, ringWidth, height);
            list.add(disk);
            setPositionToPackages(disk);
        });
        return list;
    }

    private ArrayList<MainDisk> createMainDisksList() {
        ArrayList<MainDisk> list = new ArrayList<>(rootDisks);
        StatementResult result = connector.executeRead("MATCH (p:MainDisk)-[:CONTAINS]->(d:MainDisk)-[:VISUALIZES]->(element) "
                + "RETURN d.grossArea AS areaWithBorder, d.ringWidth AS ringWidth, d.height as height," +
                " d.size AS size, ID(d) AS id, ID(p) AS parentID, ID(element) AS visualizedID ORDER BY element.hash");
        ArrayList<MainDisk> list2 = getMainDisks(result);
        list.addAll(list2);
        return list;
    }

    private void setPositionToPackages(Disk disk) {
        StatementResult position = connector
                .executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + disk.getID() + " RETURN p");
        if (!position.list().isEmpty()) {
            Node node = position.single().get("p").asNode();
            disk.setPosition(new Position(node.get("x").asDouble(), node.get("y").asDouble(), node.get("z").asDouble()));
        }
    }

    private void setColorToDisk(int namespaceMaxLevel) {
        String NS_colorStart = "#969696";
        String NS_colorEnd = "#F0F0F0";
        System.out.println(namespaceMaxLevel);
        NS_colors = ColorGradient.createColorGradient(NS_colorStart, NS_colorEnd, namespaceMaxLevel);
                connector.executeRead(
                "MATCH p = (n:Model:RD)-[:CONTAINS*]->(d:MainDisk)-[:VISUALIZES]->(e) RETURN e, d, length(p)-1 AS length," +
                        " ID(d) AS id")
                .forEachRemaining((result) -> {
                    if (result.get("e").asNode().hasLabel(Labels.Package.name())) {
                        String color = "\'" + setNamespaceColor(result.get("length").asInt()) + "\'";
                        mainDisks.forEach(disk -> {
                            long diskID = disk.getID();
                            if (result.get("id").asLong() == diskID) {
                                disk.setColor(color);
                            }
                        });
                    }
                });
    }

    private String setNamespaceColor(int level) {
        return NS_colors.get(level - 1);
    }

    private void addSubDisks() {
        subDisks.forEach(disk -> {
            ArrayList<Disk> list = new ArrayList<>();
            long id = disk.getID();
            subDisks.forEach(d -> {
                long parentID = d.getParentID();
                if (id == parentID) {
                    list.add(d);
                }
            });
            disk.setInnerDisks(list);
        });
        mainDisks.forEach(disk -> {
            ArrayList<Disk> list = new ArrayList<>();
            long id = disk.getID();
            subDisks.forEach(d -> {
                long parentID = d.getParentID();
                if (id == parentID) {
                    list.add(d);
                }
            });
            mainDisks.forEach(d -> {
                long parentID = d.getParentID();
                if (id == parentID) {
                    list.add(d);
                }
            });
            disk.setInnerDisks(list);
        });
    }

    private void addSegmentsToDisk() {
        subDisks.forEach(disk -> {
            long id = disk.getID();
            ArrayList<DiskSegment> innerSegmentsList = createDiskSegmentList(id, innerSegments);
            ArrayList<DiskSegment> outerSegmentsList = createDiskSegmentList(id, outerSegments);
            disk.setInnerSegmentsList(innerSegmentsList);
            disk.setOuterSegmentsList(outerSegmentsList);
        });
    }

    private static <T extends DiskSegment> ArrayList<T> createDiskSegmentList(long id, ArrayList<T> diskSegments) {
        ArrayList<T> list = new ArrayList<>();
        diskSegments.forEach(d -> {
            long parentID = d.getParentID();
            if (id == parentID) {
                list.add(d);
            }
        });
        return list;
    }

    private void calculateDiskLayout() {
        DiskLayout layout = new DiskLayout(rootDisks, mainDisks, subDisks);
        layout.run();
        rootDisks.forEach(Disk::calculateSpines);
    }

    private void writeToDatabase() {
        innerSegments.forEach(segment -> segment.updateNode(connector));
        outerSegments.forEach(segment -> segment.updateNode(connector));
        subDisks.forEach(disk -> disk.updateNode(connector));
        mainDisks.forEach(disk -> disk.updateNode(connector));
    }
}
