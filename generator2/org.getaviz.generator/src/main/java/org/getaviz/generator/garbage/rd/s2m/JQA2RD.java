package org.getaviz.generator.garbage.rd.s2m;

import org.getaviz.generator.garbage.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.garbage.Step;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.garbage.rd.*;
import org.neo4j.driver.v1.StatementResult;

import java.util.List;

public class JQA2RD implements Step {

	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private Model model;
	private RDElementsFactory factory;
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
	private List<ProgrammingLanguage> languages;

	public JQA2RD(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
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
		this.languages = languages;
	}

	@Override
	public boolean checkRequirements() {
		return languages.contains(ProgrammingLanguage.JAVA);
	}

	public void run() {
		log.info("JQA2RD started");
		deleteOldNodes();
		model = new Model(methodTypeMode, methodDisks, dataDisks);
		factory = new RDElementsFactory(methodTypeMode, methodDisks, dataDisks);
		queriesToRDElementList();
		writeToDatabase();
		log.info("JQA2RD finished");
	}

	private void deleteOldNodes() {
		connector.executeWrite("MATCH (n:RD) DETACH DELETE n");
	}

	private void addPackagesNoRoot() {
		try {
			StatementResult packagesNoRoot = connector.executeRead(
					"MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN ID(n) AS id");
			packagesNoRoot.forEachRemaining((node) -> {
				MainDisk disk = new MainDisk(node.get("id").asLong(), -1, ringWidth, height,
						namespaceTransparency);
				model.addRDElement(disk);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addPackagesWithRoot() {
		try {
			StatementResult packagesRoot = connector.executeRead(
					"MATCH (n:Package)-[:CONTAINS]->(p:Package) WHERE EXISTS (p.hash) RETURN ID(p) AS pID, ID(n) AS nID");
			packagesRoot.forEachRemaining(node -> {
				MainDisk disk = new MainDisk(node.get("pID").asLong(), node.get("nID").asLong(),
						ringWidth, height, namespaceTransparency);
				model.addRDElement(disk);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addTypes() {
		try {
			StatementResult result = connector.executeRead(
					"MATCH (n:Package)-[:CONTAINS]->(t:Type) WHERE EXISTS(t.hash) AND (t:Class OR t:Interface " +
							"OR t:Annotation OR t:Enum) AND NOT t:Inner RETURN ID(t) AS tID, ID(n) AS nID");
			result.forEachRemaining(node -> {
				SubDisk disk = new SubDisk(node.get("tID").asLong(), node.get("nID").asLong(),
						ringWidth, height, classTransparency, classColor);
				model.addRDElement(disk);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addMethods() {
		try {
			StatementResult methods = connector.executeRead("MATCH (n:Package)-[:CONTAINS]->(t:Type)-[:DECLARES]->(m:Method)" +
					" WHERE EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) AND NOT t:Inner AND EXISTS(m.hash)" +
					" RETURN m AS node, m.effectiveLineCount AS line, ID(t) AS tID");
			methods.forEachRemaining(result -> {
				RDElement element = factory.createFromMethod(result, height, ringWidth, ringWidthAD, methodTransparency, minArea, methodColor);
				model.addRDElement(element);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addFields() {
		try {
			StatementResult fields = connector.executeRead("MATCH (n:Package)-[:CONTAINS]->(t:Type)-[:DECLARES]->(f:Field)" +
					" WHERE EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) AND NOT t:Inner AND EXISTS(f.hash)" +
					" RETURN f AS node, ID(t) AS tID");
			fields.forEachRemaining(result -> {
				RDElement element = factory.createFromField(result, ringWidthAD, height, dataTransparency, dataColor);
				model.addRDElement(element);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void queriesToRDElementList() {
	    addPackagesNoRoot();
		addPackagesWithRoot();
		addTypes();
		addMethods();
		addFields();
	}

	private void writeToDatabase() { model.writeToDatabase(connector); }

}

