package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import java.util.GregorianCalendar;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;


public class JQA2RD implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private SettingsConfiguration config;
	private boolean methodTypeMode;
	private boolean methodDisks;
	private boolean dataDisks;
	private double ringWidth;
	private double height;
	private double transparency;
	private static Stack<Disk>  diskStack = new Stack<>();
	private static Stack<DiskSegment> diskSegmentStack = new Stack<>();

	public JQA2RD(SettingsConfiguration config) {
		this.config = config;
		this.methodTypeMode = config.isMethodTypeMode();
		this.methodDisks = config.isMethodDisks();
		this.dataDisks = config.isDataDisks();
		this.ringWidth = config.getRDRingWidth();
		this.height = config.getRDHeight();
		this.transparency = config.getRDNamespaceTransparency();
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
			toStack(new Disk (config, connector));
			nameSpace(node.get("n").asNode().id(), model);
		});
		writeDisk();
		writeDiskSegment();
		log.info("JQA2RD finished");
	}

	public static void toStack(Disk disk) {
		diskStack.push(disk);
	}

	public static void toStack(DiskSegment segment){
		diskSegmentStack.push(segment);
	}

	private void nameSpace(Long namespace, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f", ringWidth,
				height, transparency);
		long disk = connector.addNode(CypherCreateNode.create(parent, namespace, Labels.Disk.name(), properties), "n").id();
		connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + namespace +
				" AND EXISTS(t.hash) AND (t:Class OR t:Interface OR t:Annotation OR t:Enum) AND NOT t:Inner RETURN t").
				forEachRemaining((result) -> toStack(new Disk(result.get("t").asNode(), disk, config, connector,
						config.getRDClassTransparency())));
		connector.executeRead("MATCH (n)-[:CONTAINS]->(p:Package) WHERE ID(n) = " + namespace +
				" AND EXISTS(p.hash) RETURN p").
				forEachRemaining((result) -> {
					diskStack.push(new Disk(config, connector));
					nameSpace(result.get("p").asNode().id(), disk);
				});
	}

	private void writeDisk() {
		while (!diskStack.empty()) {
			Disk nextDisk = diskStack.pop();
			String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'",
					ringWidth, height, transparency, nextDisk.getColor());
			connector.executeWrite(CypherCreateNode.create(nextDisk.getParent(), nextDisk.getAttribute(), Labels.Disk.name(), properties));
		}
	}

	private void writeDiskSegment() {
		while(!diskSegmentStack.empty()) {
			DiskSegment nextDiskSegment = diskSegmentStack.pop();
			connector.executeWrite(CypherCreateNode.create(nextDiskSegment.getParent(), nextDiskSegment.getVisualizedNode(),
					Labels.DiskSegment.name(), nextDiskSegment.getProperties()));
		}
	}
}

