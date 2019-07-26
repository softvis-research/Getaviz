package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;


public class JQA2RD implements Step {

	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
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
	private long model;
	private static ArrayList<Disk> packages = new ArrayList<>();
	private static ArrayList<Disk> types = new ArrayList<>();
	private static ArrayList<Disk> parentNodes = new ArrayList<>();
	private static ArrayList<RDElement> methodsAndFields = new ArrayList<>();

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
		model = connector.addNode(
				String.format(
						"CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', " +
								"method_disks: \'%s\', data_disks:\'%s\'})",
						new GregorianCalendar().getTime().toString(), methodTypeMode, methodDisks, dataDisks),
				"m").id();
		packages = getPackages();
		types = getTypes();
		methodsAndFields = getMethodsAndFields();
		parentNodes.addAll(packages);
		parentNodes.addAll(types);
		writeToDB();
		log.info("JQA2RD finished");
	}

	private ArrayList<Disk> getPackages() {
		ArrayList<Disk> list = new ArrayList<>();
		StatementResult packagesNoRoot = connector.executeRead(
				"MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN ID(n) AS id");
		packagesNoRoot.forEachRemaining((node) -> list.add(new Disk(node.get("id").asLong(), model, ringWidth, height,
				namespaceTransparency)));

		StatementResult packagesRoot = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(p:Package) WHERE EXISTS (p.hash) RETURN ID(p) AS pID, ID(n) AS nID");
		packagesRoot.forEachRemaining(node -> list.add(new Disk(node.get("pID").asLong(), node.get("nID").asLong(),
				ringWidth, height, namespaceTransparency)));
		return list;
	}

	private ArrayList<Disk> getTypes() {
		ArrayList<Disk> list = new ArrayList<>();
		StatementResult result = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(t:Type) WHERE EXISTS(t.hash) AND (t:Class OR t:Interface " +
						"OR t:Annotation OR t:Enum) AND NOT t:Inner RETURN ID(t) AS tID, ID(n) AS nID");
		result.forEachRemaining(node -> list.add(new Disk(node.get("tID").asLong(), node.get("nID").asLong(),
				ringWidth, height, classTransparency, classColor)));
		return list;
	}

	private ArrayList<RDElement> getMethodsAndFields() {
		ArrayList<RDElement> list = new ArrayList<>();
		types.forEach(t -> {
			StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = "
					+ t.getVisualizedNodeID() + " AND EXISTS(m.hash) RETURN m AS m, m.effectiveLineCount AS line");
			StatementResult fields = connector.executeRead("MATCH (n)-[:DECLARES]->(f:Field) WHERE ID(n) = "
					+ t.getVisualizedNodeID() +	" AND EXISTS(f.hash) RETURN f");
					if (methodTypeMode) {
						methods.forEachRemaining((result) -> {
							if (result.get("m").asNode().hasLabel(Labels.Constructor.name())) {
								list.add(new DiskSegment(result.get("m").asNode().id(), t.getVisualizedNodeID(),
										methodTransparency, minArea,height,	methodColor, result.get("line").asInt(0)));
							} else {
								list.add(new Disk(result.get("m").asNode().id(), t.getVisualizedNodeID(), ringWidth, height, methodTransparency,
										methodColor));
							}
						});
						fields.forEachRemaining((result) ->
							list.add(new Disk(result.get("f").asNode().id(), t.getVisualizedNodeID(), ringWidthAD, height,
									dataTransparency, dataColor)));
					} else {
						if (dataDisks) {
							fields.forEachRemaining((result) ->
								list.add(new Disk(result.get("f").asNode().id(), t.getVisualizedNodeID(), ringWidthAD, height,
										dataTransparency, dataColor)));
						} else {
							fields.forEachRemaining((result) ->
								list.add(new DiskSegment(result.get("f").asNode().id(), t.getVisualizedNodeID(), dataTransparency,
										height, dataColor)));
						}
						if (methodDisks) {
							methods.forEachRemaining((result) ->
									list.add(new Disk(result.get("m").asNode().id(), t.getVisualizedNodeID(), ringWidthAD,
									height, methodTransparency, methodColor)));
						} else {
							methods.forEachRemaining((result) ->
									list.add(new DiskSegment(result.get("m").asNode().id(), t.getVisualizedNodeID(), classTransparency,
									minArea, height, classColor, result.get("line").asInt(0))));
						}
					}
					});
					return list;
				}

	private void writeToDB() {
		parentNodes.forEach(p -> {
			if (p.getParentVisualizedNodeID() == model) {
				p.setParentVisualizedNodeID(model);
			} else {
				searchForParent(p);
			}
			p.setId(p.addNode(connector));
		});
		methodsAndFields.forEach(mf -> {
			searchForParent(mf);
			mf.setId(mf.addNode(connector));
		});
	}

	private void searchForParent(RDElement element) {
		Iterator<Disk> iterator = parentNodes.iterator();
		boolean found = false;
		while ((iterator.hasNext() && (!found))) {
			Disk disk = iterator.next();
			if (element.getParentVisualizedNodeID() == disk.getVisualizedNodeID()) {
				element.setParentVisualizedNodeID(disk.getId());
				found = true;
			}
		}
	}
}

