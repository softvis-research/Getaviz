package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;


public class JQA2RD implements Step {

	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private Model model;
	private boolean methodTypeMode;
	private boolean methodDisks;
	private boolean dataDisks;
	private double ringWidth;
	private double ringWidthAD;
	private double height;
	private double namespaceTransparency;
	private double classTransparency;
	private double methodTransparency;
	private double dataTransparency;
	private double minArea;
	private String classColor;
	private String methodColor;
	private String dataColor;
	
    private static ArrayList<Disk> types = new ArrayList<>();


	public JQA2RD(SettingsConfiguration config) {
		this.methodTypeMode = config.isMethodTypeMode();
		this.methodDisks = config.isMethodDisks();
		this.dataDisks = config.isDataDisks();
		this.ringWidth = config.getRDRingWidth();
		this.ringWidthAD = config.getRDRingWidthAD();
		this.height = config.getRDHeight();
		this.namespaceTransparency = config.getRDNamespaceTransparency();
		this.classTransparency = config.getRDClassTransparency();
		this.methodTransparency = config.getRDMethodTransparency();
		this.dataTransparency = config.getRDDataTransparency();
		this.minArea = config.getRDMinArea();
		this.classColor = config.getClassColor();
		this.methodColor = config.getRDMethodColor();
		this.dataColor = config.getRDDataColor();
	}

	public void run() {
		log.info("JQA2RD started");
		connector.executeWrite("MATCH (n:RD) DETACH DELETE n");
		model = new Model(methodTypeMode, methodDisks,dataDisks);
		queriesToRDElementList();
		writeToDatabase();
		log.info("JQA2RD finished");
	}

	private void addPackagesNoRoot() {
		StatementResult packagesNoRoot = connector.executeRead(
				"MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN ID(n) AS id");
		packagesNoRoot.forEachRemaining((node) -> {
		    Disk disk = new Disk(node.get("id").asLong(), -1, ringWidth, height,
                    namespaceTransparency);
		    model.setList(disk);
            });
	}

	private void addPackagesWithRoot() {
		StatementResult packagesRoot = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(p:Package) WHERE EXISTS (p.hash) RETURN ID(p) AS pID, ID(n) AS nID");
		packagesRoot.forEachRemaining(node -> {
		    Disk disk =  new Disk(node.get("pID").asLong(), node.get("nID").asLong(),
                    ringWidth, height, namespaceTransparency);
		    model.setList(disk);
        });
	}

	private void addTypes() {
		StatementResult result = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(t:Type) WHERE EXISTS(t.hash) AND (t:Class OR t:Interface " +
						"OR t:Annotation OR t:Enum) AND NOT t:Inner RETURN ID(t) AS tID, ID(n) AS nID");
		result.forEachRemaining(node -> {
		    Disk disk = new Disk(node.get("tID").asLong(), node.get("nID").asLong(),
                    ringWidth, height, classTransparency, classColor);
		    model.setList(disk);
		    types.add(disk);
        });
	}

	private void addMethods() {
		types.forEach(t -> {
			StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = "
					+ t.getVisualizedNodeID() + " AND EXISTS(m.hash) RETURN m AS m, m.effectiveLineCount AS line");
					if (methodTypeMode) {
						methods.forEachRemaining((result) -> {
							if (result.get("m").asNode().hasLabel(Labels.Constructor.name())) {
								DiskSegment diskSegment = new DiskSegment(result.get("m").asNode().id(), t.getVisualizedNodeID(),
										height, methodTransparency, minArea, methodColor, result.get("line").asInt(0));
								model.setList(diskSegment);
							} else {
								Disk disk = new Disk(result.get("m").asNode().id(), t.getVisualizedNodeID(), ringWidth, height,
										methodTransparency, methodColor);
								model.setList(disk);
							}
						});
					} else {
						if (methodDisks) {
							methods.forEachRemaining((result) -> {
									Disk disk = new Disk(result.get("m").asNode().id(), t.getVisualizedNodeID(), ringWidthAD,
									height, methodTransparency, methodColor);
									model.setList(disk);
                            });
						} else {
							methods.forEachRemaining((result) -> {
									DiskSegment diskSegment = new DiskSegment(result.get("m").asNode().id(), t.getVisualizedNodeID(), height,
											classTransparency, minArea, classColor, result.get("line").asInt(0));
									model.setList(diskSegment);
                            });
						}
					}
			});
	}

	private void addFields() {
		types.forEach(t -> {
			StatementResult fields = connector.executeRead("MATCH (n)-[:DECLARES]->(f:Field) WHERE ID(n) = "
					+ t.getVisualizedNodeID() +	" AND EXISTS(f.hash) RETURN f");
					if (methodTypeMode) {
						fields.forEachRemaining((result) -> {
							Disk disk = new Disk(result.get("f").asNode().id(), t.getVisualizedNodeID(), ringWidthAD, height,
									dataTransparency, dataColor);
							model.setList(disk);
                        });
					} else {
						if (dataDisks) {
                            fields.forEachRemaining((result) -> {
                                   Disk disk = new Disk(result.get("f").asNode().id(), t.getVisualizedNodeID(), ringWidthAD, height,
                                            dataTransparency, dataColor);
                                   model.setList(disk);
                        });
						} else {
							fields.forEachRemaining((result) -> {
								DiskSegment diskSegment = new DiskSegment(result.get("f").asNode().id(), t.getVisualizedNodeID(), height,
										dataTransparency, dataColor);
								model.setList(diskSegment);
                            });
						}
					}
			});
	}

	private void queriesToRDElementList() {
	    addPackagesNoRoot();
		addPackagesWithRoot();
		addTypes();;
		addMethods();
		addFields();
	}

	private void writeToDatabase() {
		model.writeToDatabase(connector);
	}
}

