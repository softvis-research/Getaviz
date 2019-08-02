package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.StatementResult;


public class JQA2RD implements Step {

	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private Model model;
	private  RDElementsFactory factory;
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
		model = new Model(methodTypeMode, methodDisks, dataDisks);
		factory = new RDElementsFactory(model);
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
        });
	}

	private void addMethods() {
		StatementResult methods = connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type)-[:DECLARES]->(m:Method)" +
				" WHERE EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) AND NOT t:Inner AND EXISTS(m.hash)" +
				" RETURN m AS node, m.effectiveLineCount AS line, ID(t) AS tID");
		factory.create(methods, height, ringWidth, ringWidthAD, methodTransparency,
				classTransparency, minArea, methodColor, classColor);
	}

	private void addFields() {
		StatementResult fields = connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type)-[:DECLARES]->(f:Field)" +
				" WHERE EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) AND NOT t:Inner AND EXISTS(f.hash)" +
				" RETURN f AS node, ID(t) AS tID");
		factory.create(fields, ringWidthAD, height, dataTransparency, dataColor);
	}

	private void queriesToRDElementList() {
	    addPackagesNoRoot();
		addPackagesWithRoot();
		addTypes();
		addMethods();
		addFields();
	}

	private void writeToDatabase() {
		model.writeToDatabase(connector);
	}
}

