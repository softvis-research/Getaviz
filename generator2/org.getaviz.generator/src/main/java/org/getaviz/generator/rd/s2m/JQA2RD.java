package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.Labels;
import java.util.GregorianCalendar;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

public class JQA2RD implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());

	private double namespaceTransparency;

	private String classColor;
	private double classTransparency;

	private String methodColor;
	private double methodTransparency;
	private boolean methodTypeMode;
	private boolean methodDisks;

	private String dataColor;
	private double dataTransparency;
	private double ringWidthAD;
	private boolean dataDisks;

	private double  height;
	private double ringWidth;
	private double minArea;

	public JQA2RD(SettingsConfiguration config) {
		this.methodTypeMode = config.isMethodTypeMode();
		this.methodDisks = config.isMethodDisks();
		this.dataDisks = config.isDataDisks();
		this.height = config.getRDHeight();
		this.namespaceTransparency = config.getRDNamespaceTransparency();
		this.ringWidth = config.getRDRingWidth();
		this.classColor = config.getRDClassColor();
		this.classTransparency = config.getRDClassTransparency();
		this.methodColor = config.getRDMethodColor();
		this.methodTransparency = config.getRDMethodTransparency();
		this.minArea = config.getRDMinArea();
		this.dataColor = config.getRDDataColor();
		this.dataTransparency = config.getRDDataTransparency();
		this.ringWidthAD = config.getRDRingWidthAD();
	}

	public void run() {
		log.info("JQA2RD started");
		connector.executeWrite("MATCH (n:RD) DETACH DELETE n");
		long model = connector.addNode(
				String.format(
						"CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', method_disks: \'%s\', data_disks:\'%s\'})",
						new GregorianCalendar().getTime().toString(), methodTypeMode, methodDisks, dataDisks),
				"m").id();
		StatementResult results = connector.executeRead(
				"MATCH (n:Package) " +
						"WHERE NOT (n)<-[:CONTAINS]-(:Package) " +
						"RETURN n"
		);
		results.forEachRemaining((node) -> {
			namespaceToDisk(node.get("n").asNode().id(), model);
		});
		log.info("JQA2RD finished");
	}

	private void namespaceToDisk(Long namespace, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f", ringWidth,
			height, namespaceTransparency);
		long disk = connector.addNode(cypherCreateNode(parent, namespace, Labels.Disk.name(), properties), "n").id();
		connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + namespace +
			" AND EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) AND NOT t:Inner RETURN t").
			forEachRemaining((result) -> {
				structureToDisk(result.get("t").asNode(), disk);
			});
		connector.executeRead("MATCH (n)-[:CONTAINS]->(p:Package) WHERE ID(n) = " + namespace +
			" AND EXISTS(p.hash) RETURN p").
			forEachRemaining((result) -> {
				namespaceToDisk(result.get("p").asNode().id(), disk);
			});
	}

	private void structureToDisk(Node structure, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
			height, classTransparency, classColor);
		long disk = connector.addNode(cypherCreateNode(parent, structure.id(), Labels.Disk.name(), properties), "n").id();
		StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = " + structure.id() +
			" AND EXISTS(m.hash) RETURN m");
		StatementResult fields = connector.executeRead("MATCH (n)-[:DECLARES]->(f:Field) WHERE ID(n) = " + structure.id() +
			" AND EXISTS(f.hash) RETURN f");

		if (methodTypeMode) {
			methods.forEachRemaining((result) -> {
				Node method = result.get("m").asNode();
				if (method.hasLabel(Labels.Constructor.name())) {
					methodToDiskSegment(method, disk);
				} else {
					methodToDisk(method.id(), disk);
				}
			});
			fields.forEachRemaining((result) -> {
				if (structure.hasLabel(Labels.Enum.name())) {
					enumValueToDisk(result.get("f").asNode().id(), disk);
				} else {
					attributeToDisk(result.get("f").asNode().id(), disk);
				}
			});
		} else {
			if (dataDisks) {
				fields.forEachRemaining((result) -> {
					if (structure.hasLabel(Labels.Enum.name() )) {
						enumValueToDisk(result.get("f").asNode().id(), disk);
					} else {
						attributeToDisk(result.get("f").asNode().id(), disk);
					}
				});
			} else {
				fields.forEachRemaining((result) -> {
					if (structure.hasLabel(Labels.Enum.name())) {
						enumValueToDiskSegment(result.get("f").asNode().id(), disk);
					} else {
						attributeToDiskSegment(result.get("f").asNode().id(), disk);
					}
				});
			}
			if (methodDisks) {
				methods.forEachRemaining((result) -> {
					methodToDisk(result.get("m").asNode().id(), disk);
				});
			} else {
				methods.forEachRemaining((result) -> {
					methodToDiskSegment(result.get("m").asNode(), disk);
				});
			}
		}
		connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + structure.id() +
			" AND EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) RETURN t").
			forEachRemaining((result) -> {
				structureToDisk(result.get("t").asNode(), disk);
			});
	}

	private void methodToDisk(Long method, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", ringWidth,
			height, methodTransparency, methodColor);
		connector.executeWrite(cypherCreateNode(parent, method, Labels.Disk.name(), properties));
	}

	private void methodToDiskSegment(Node method, Long parent) {
		double frequency = 0.0;
		double luminance = 0.0;
		String color = methodColor;

		Integer numberOfStatements = method.get("effectiveLineCount").asInt(0);
		double size = numberOfStatements.doubleValue();
		if (numberOfStatements <= minArea) {
			size = minArea;
		}
		String properties = String.format(
			"frequency: %f, luminance: %f, height: %f, transparency: %f, size: %f, color: \'%s\'", frequency, luminance,
			height, methodTransparency, size, color);
		connector.executeWrite(cypherCreateNode(parent, method.id(), Labels.DiskSegment.name(), properties));
	}

	private void attributeToDisk(Long attribute, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'",
			ringWidthAD, height, dataTransparency, dataColor);
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.Disk.name(), properties));
	}

	private void attributeToDiskSegment(Long attribute, Long parent) {
		String properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", 1.0, height,
			dataTransparency, dataColor);
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.DiskSegment.name(), properties));
	}

	private void enumValueToDisk(Long enumValue, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'",
			ringWidthAD, height, dataTransparency, dataColor);
		connector.executeWrite(cypherCreateNode(parent, enumValue, Labels.Disk.name(), properties));
	}

	private void enumValueToDiskSegment(Long enumValue, Long parent) {
		String properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", 1.0, height,
			dataTransparency, dataColor);
		connector.executeWrite(cypherCreateNode(parent, enumValue, Labels.DiskSegment.name(), properties));

	}

	private String cypherCreateNode(Long parent, Long visualizedNode, String label, String properties) {
		return String.format(
			"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->(n:RD:%s {%s})-[:VISUALIZES]->(s)",
			parent, visualizedNode, label, properties);
	}
}
