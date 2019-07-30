package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;


public class JQA2RD implements Step {

	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private DBModel model;
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
	private static ArrayList<RDElement> allNodesToVisualize = new ArrayList<>();

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
		model = new DBModel(methodTypeMode, methodDisks,dataDisks, connector);
		QueriesToDiskElement();
		writeToDB();
		log.info("JQA2RD finished");
	}

	private ArrayList<Disk> getPackagesNoRoot() {
		ArrayList<Disk> list = new ArrayList<>();
		StatementResult packagesNoRoot = connector.executeRead(
				"MATCH (n:Package) WHERE NOT (n)<-[:CONTAINS]-(:Package) RETURN ID(n) AS id");
		packagesNoRoot.forEachRemaining((node) -> list.add(new Disk(node.get("id").asLong(), -1, ringWidth, height,
				namespaceTransparency)));
		return list;
	}

	private ArrayList<Disk> getPackagesWithRoot() {
		ArrayList<Disk> list = new ArrayList<>();
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

	private ArrayList<RDElement> getMethods(ArrayList<Disk> types) {
		ArrayList<RDElement> list = new ArrayList<>();
		types.forEach(t -> {
			StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = "
					+ t.getVisualizedNodeID() + " AND EXISTS(m.hash) RETURN m AS m, m.effectiveLineCount AS line");
					if (methodTypeMode) {
						methods.forEachRemaining((result) -> {
							if (result.get("m").asNode().hasLabel(Labels.Constructor.name())) {
								list.add(new DiskSegment(result.get("m").asNode().id(), t.getVisualizedNodeID(),
										height, methodTransparency, minArea, methodColor, result.get("line").asInt(0)));
							} else {
								list.add(new Disk(result.get("m").asNode().id(), t.getVisualizedNodeID(), ringWidth, height, methodTransparency,
										methodColor));
							}
						});
					} else {
						if (methodDisks) {
							methods.forEachRemaining((result) ->
									list.add(new Disk(result.get("m").asNode().id(), t.getVisualizedNodeID(), ringWidthAD,
									height, methodTransparency, methodColor)));
						} else {
							methods.forEachRemaining((result) ->
									list.add(new DiskSegment(result.get("m").asNode().id(), t.getVisualizedNodeID(), height,
											classTransparency, minArea, classColor, result.get("line").asInt(0))));
						}
					}
			});
		return list;
	}

	private ArrayList<RDElement> getFields (ArrayList<Disk> types) {
		ArrayList<RDElement> list = new ArrayList<>();
		types.forEach(t -> {
			StatementResult fields = connector.executeRead("MATCH (n)-[:DECLARES]->(f:Field) WHERE ID(n) = "
					+ t.getVisualizedNodeID() +	" AND EXISTS(f.hash) RETURN f");
					if (methodTypeMode) {
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
								list.add(new DiskSegment(result.get("f").asNode().id(), t.getVisualizedNodeID(), height,
										dataTransparency, dataColor)));
						}
					}
			});
		return list;
	}

	private void QueriesToDiskElement() {
		ArrayList<Disk> types;
		allNodesToVisualize.addAll(getPackagesNoRoot());
		allNodesToVisualize.addAll(getPackagesWithRoot());
		types = getTypes();
		allNodesToVisualize.addAll(types);
		allNodesToVisualize.addAll(getMethods(types));
		allNodesToVisualize.addAll(getFields(types));
	}

	private void writeToDB() {
		model.createModel();
		long modelID = model.getId();
		allNodesToVisualize.forEach(p -> {
			if (p.getParentVisualizedNodeID() == -1) {
				p.setParentID(modelID);
				write(p);
			} else {
				search(p);
				write(p);
			}
		});
	}

	private void search(RDElement element) {
		Iterator<RDElement> iterator = allNodesToVisualize.iterator();
		while ((iterator.hasNext())) {
			RDElement disk = iterator.next();
			if (element.getParentVisualizedNodeID() == disk.getVisualizedNodeID()) {
				element.setParentID(disk.getId());
				return;
			}
		}
	}

	private void write(RDElement p) {
		p.createNodeForVisualization(connector);
	}
}

