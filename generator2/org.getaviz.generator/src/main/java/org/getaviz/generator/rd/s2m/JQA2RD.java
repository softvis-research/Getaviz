package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;


public class JQA2RD implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private SettingsConfiguration config;
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
	private static ArrayList<Disk> parentNodes = new ArrayList();
	private static ArrayList<RDElement> methodsAndFields = new ArrayList<>();

	public JQA2RD(SettingsConfiguration config) {
		this.config = config;
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
		packages = addPackages();
		types = addTypes();
		methodsAndFields = addMethodsAndFields();
		parentNodes.addAll(packages);
		parentNodes.addAll(types);
		writeToDB();
		log.info("JQA2RD finished");
	}

	private ArrayList<Disk> addPackages() {
		ArrayList<Disk> list = new ArrayList<>();
		StatementResult packagesNoRoot = connector.executeRead(
				"MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN ID(n) AS id");
		packagesNoRoot.forEachRemaining((node) -> list.add(new Disk(node.get("id").asLong(), model, ringWidth, height,
				namespaceTransparency, connector)));
		StatementResult packagesRoot = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(p:Package) WHERE EXISTS (p.hash) RETURN ID(p) AS pID, ID(n) AS nID");
		packagesRoot.forEachRemaining(node -> list.add(new Disk(node.get("pID").asLong(), node.get("nID").asLong(),
				ringWidth, height, namespaceTransparency, connector)));
		return list;
	}

	private ArrayList<Disk> addTypes() {
		ArrayList<Disk> list = new ArrayList<>();
		StatementResult result = connector.executeRead(
				"MATCH (n)-[:CONTAINS]->(t:Type) WHERE EXISTS(t.hash) AND (t:Class OR t:Interface " +
						"OR t:Annotation OR t:Enum) AND NOT t:Inner RETURN ID(t) AS tID, ID(n) AS nID");
		result.forEachRemaining(node -> list.add(new Disk(node.get("tID").asLong(), node.get("nID").asLong(),
				ringWidth, height, classTransparency, classColor, connector)));
		return list;
	}

	private ArrayList<RDElement> addMethodsAndFields() {
		ArrayList<RDElement> list = new ArrayList<>();
		types.forEach(t -> {
			StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = "
					+ t.getVisualizedNodeID() + " AND EXISTS(m.hash) RETURN m");
			StatementResult fields = connector.executeRead("MATCH (n)-[:DECLARES]->(f:Field) WHERE ID(n) = "
					+ t.getVisualizedNodeID() +	" AND EXISTS(f.hash) RETURN f");
					if (methodTypeMode) {
						methods.forEachRemaining((result) -> {
							Node method = result.get("m").asNode();
							if (method.hasLabel(Labels.Constructor.name())) {
								list.add(new DiskSegment(method, t.getVisualizedNodeID(), methodTransparency, minArea,height,
										methodColor, connector));
							} else {
								list.add(new Disk(method.id(), t.getVisualizedNodeID(), ringWidth, height, methodTransparency,
										methodColor, connector));
							}
						});
						fields.forEachRemaining((result) ->
							list.add(new Disk(result.get("f").asNode().id(), t.getVisualizedNodeID(), ringWidthAD, height,
									dataTransparency, dataColor, connector)));
					} else {
						if (dataDisks) {
							fields.forEachRemaining((result) ->
								list.add(new Disk(result.get("f").asNode().id(), t.getVisualizedNodeID(), ringWidthAD, height,
										dataTransparency, dataColor, connector)));
						} else {
							fields.forEachRemaining((result) ->
								list.add(new DiskSegment(result.get("f").asNode().id(), t.getVisualizedNodeID(), dataTransparency,
										height, dataColor, connector)));

						}
						if (methodDisks) {
							methods.forEachRemaining((result) ->
									list.add(new Disk(result.get("m").asNode().id(), t.getVisualizedNodeID(), ringWidthAD,
									height, methodTransparency, methodColor, connector)));
						} else {
							methods.forEachRemaining((result) ->
									list.add(new DiskSegment(result.get("m").asNode(), t.getVisualizedNodeID(), classTransparency,
									minArea, height, classColor, connector)));
						}
					}
					});
					return list;
				}

	private void writeToDB() {
		parentNodes.forEach(p -> {
			if (p.getParentID() == model) {
				p.setNewParentID(model);
			} else {
				Iterator<Disk> iterator = parentNodes.iterator();
				boolean found = false;
				while ((iterator.hasNext() && (!found))) {
					Disk disk = iterator.next();
					if (p.getParentID() == disk.getVisualizedNodeID()) {
						p.setNewParentID(disk.getInternID());
						found = true;
					}
				}
			}
			p.setInternID(connector.addNode(String.format(
					"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
							"(n:RD:%s {%s})-[:VISUALIZES]->(s)",
					p.getNewParentID(), p.getVisualizedNodeID(), Labels.Disk.name(), p.getProperties()), "n")
					.id());
		});
		methodsAndFields.forEach(mf -> {
			Iterator<Disk> iterator = parentNodes.iterator();
			boolean found = false;
			while ((iterator.hasNext() && (!found))) {
				Disk disk = iterator.next();
				if (mf.getParentID() == disk.getVisualizedNodeID()) {
					mf.setNewParentID(disk.getInternID());
					found = true;
				}
			}
			if (mf instanceof Disk) {
				connector.executeWrite(String.format(
						"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->(" +
								"n:RD:%s {%s})-[:VISUALIZES]->(s)",
						mf.getNewParentID(), mf.getVisualizedNodeID(), Labels.Disk.name(), mf.getProperties()));
			} else {
				connector.executeWrite(String.format(
						"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->" +
								"(n:RD:%s {%s})-[:VISUALIZES]->(s)",
						mf.getNewParentID(), mf.getVisualizedNodeID(), Labels.DiskSegment.name(), mf.getProperties()));
				}
		});
	}
}

