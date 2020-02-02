package org.getaviz.generator.rd.s2m;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.rd.*;
import org.neo4j.driver.v1.StatementResult;

import java.util.List;

public class C2RD implements Step {
	private final DatabaseConnector connector = DatabaseConnector.getInstance();
	private final Log log = LogFactory.getLog(C2RD.class);
	private boolean methodTypeMode;
	private boolean methodDisks;
	private boolean dataDisks;
	private double height;
	private double namespaceTransparency;
	private double ringWidth;
	private double classTransparency;
	private double minArea;
	private String classColor;
	private String methodColor;
	private double methodTransparency;
	private String dataColor;
	private  double ringWidthAD;
	private double dataTransparency;
	private Model model;
	private RDElementsFactory factory;
	private List<ProgrammingLanguage> languages;

	public C2RD(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
		methodTypeMode = config.isMethodTypeMode();
		methodDisks = config.isMethodDisks();
		dataDisks = config.isDataDisks();
		height = config.getRDHeight();
		namespaceTransparency = config.getRDNamespaceTransparency();
		minArea = config.getRDMinArea();
		methodTransparency = config.getRDMethodTransparency();
		dataColor = config.getRDDataColor();
		dataTransparency = config.getRDDataTransparency();
		ringWidthAD = config.getRDRingWidthAD();
		methodColor = config.getRDMethodColor();
		ringWidth = config.getRDRingWidth();
		classTransparency = config.getRDClassTransparency();
		classColor = config.getRDClassColor();
		this.languages = languages;
	}

	public void run() {
		log.info("C2RD started");
		deleteOldNodes();
		model = new Model(methodTypeMode, methodDisks, dataDisks);
		factory = new RDElementsFactory(methodTypeMode, methodDisks, dataDisks);
		queriesToRDElementList();
		writeToDatabase();
		log.info("C2RD finished");
	}

	@Override
	public boolean checkRequirements() {
		return languages.contains(ProgrammingLanguage.C);
	}

	private void deleteOldNodes() {
		connector.executeWrite("MATCH (n:RD) DETACH DELETE n");
	}

	private void queriesToRDElementList() {
		addFiles();
		addTranslationUnits();
		addFunctions();
		addVariables();
		addStructs();
	}

	private void addFiles() {
		try {
			StatementResult translationUnits = connector.executeRead("MATCH (n:File) RETURN ID(n) as id");
			translationUnits.forEachRemaining((node) -> {
				MainDisk disk = new MainDisk(node.get("id").asLong(), -1, ringWidth, height, namespaceTransparency);
				model.addRDElement(disk);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addTranslationUnits() {
	//	MainDisk root = new MainDisk(-1, -1, ringWidth, height, namespaceTransparency);
	//	model.addRDElement(root);
		try {
			StatementResult translationUnits = connector.executeRead("MATCH (f:File)-[:CONTAINS]->(n:TranslationUnit) RETURN ID(n) as id, ID(f) as pId");
			translationUnits.forEachRemaining((node) -> {
				SubDisk disk = new SubDisk(node.get("id").asLong(), node.get("pId").asLong(), ringWidth, height, classTransparency, classColor);
				model.addRDElement(disk);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addFunctions() {
		try {
			String query = "MATCH (t:TranslationUnit)-[:DECLARES]->(f:Function) WHERE EXISTS(f.hash) RETURN f as node, ID(t) as tID";
			StatementResult translationUnits = connector.executeRead(query);
			translationUnits.forEachRemaining((result) -> {
				RDElement element = factory.createFromFunction(result, height, ringWidthAD, methodTransparency, minArea, methodColor);
				model.addRDElement(element);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addVariables() {
		try {
			String query = "MATCH (t:TranslationUnit)-[:DECLARES]->(v:Variable) WHERE EXISTS(v.hash) RETURN v as node, ID(t) as tID";
			StatementResult translationUnits = connector.executeRead(query);
			translationUnits.forEachRemaining((result) -> {
				RDElement element = factory.createFromVariable(result, height, ringWidthAD, dataTransparency, dataColor);
				model.addRDElement(element);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void addStructs() {
		try {
			String query = "MATCH (p)-[:DECLARES]->(el) WHERE EXISTS(el.hash) " +
					"AND (el:Union OR el:Struct OR el:Enum) RETURN el as node, ID(p) as tID";
			StatementResult translationUnits = connector.executeRead(query);
			translationUnits.forEachRemaining((result) -> {
				RDElement element = factory.createFromVariable(result, height, ringWidthAD, dataTransparency, dataColor);
				model.addRDElement(element);
			});
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void writeToDatabase() { model.writeToDatabase(connector); }

}
