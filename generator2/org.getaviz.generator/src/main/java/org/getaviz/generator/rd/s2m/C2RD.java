package org.getaviz.generator.rd.s2m;

import java.util.GregorianCalendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

public class C2RD {

	private final SettingsConfiguration config = SettingsConfiguration.getInstance();
	private final DatabaseConnector connector = DatabaseConnector.getInstance();
	private final Log log = LogFactory.getLog(C2RD.class);
	
	public C2RD() {
		log.info("C2RD started");
			StatementResult translationUnits = connector.executeRead("MATCH (n:TranslationUnit) RETURN n");
			long modelId = createModelNode();
			while(translationUnits.hasNext()) {
				Node unit = translationUnits.next().get("n").asNode();
				translationUnitToDisk(unit.id(), modelId);
			}
		log.info("C2RD finished");
	}

	private long createModelNode() {
		return connector.addNode(
				String.format(
						"CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', method_disks: \'%s\', data_disks:\'%s\'})",
						new GregorianCalendar().getTime().toString(), config.isMethodTypeMode(), config.isMethodDisks(), config.isDataDisks()),
				"m").id();
	}
	
	private void translationUnitToDisk(Long translationUnit, Long modelId) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", config.getRDRingWidth(),
				config.getRDHeight(), config.getRDNamespaceTransparency(), config.getRDNamespaceColorHex());
		long disk = connector.addNode(cypherCreateNode(modelId, translationUnit, Labels.Disk.name(), properties), "n").id();

		connector.executeRead("MATCH (n)-[:DECLARES]->(f:Function) WHERE ID(n) = " + translationUnit +
				" AND EXISTS(f.hash) RETURN f").
				forEachRemaining((result) -> {
					Node element = result.get("f").asNode();
					if (config.isMethodDisks()) {
						functionToDisk(element.id(), disk);
					} else {
						functionToDiskSegment(element, disk);
					}
				});
		connector.executeRead("MATCH (n)-[:DECLARES]->(v:Variable) WHERE ID(n) = " + translationUnit +
				" AND EXISTS(v.hash) RETURN v").
				forEachRemaining((result) -> {
					Node element = result.get("v").asNode();
					if (config.isDataDisks()) {
						variableToDisk(element.id(), disk);
					} else {
						variableToDiskSegment(element.id(), disk);
					}
				});
		connector.executeRead("MATCH (n)-[:DECLARES]->(t) WHERE ID(n) = " + translationUnit +
				" AND EXISTS(t.hash) AND (t:Union OR t:Struct OR t:Enum)  RETURN t").
				forEachRemaining((result) -> {
					Node element = result.get("t").asNode();
					unionStructEnumToDisk(element, disk);
				});
	}

	private void functionToDisk(Long function, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: %s", config.getRDRingWidth(),
				config.getRDHeight(), config.getRDNamespaceTransparency(), config.getRDMethodColorHex());
		connector.executeWrite(cypherCreateNode(parent, function, Labels.Disk.name(), properties));
	}

	private void functionToDiskSegment(Node function, Long parent) {
		double height = config.getRDHeight();
		double size ;
		Integer numberOfStatements = function.get("lineCount").asInt(0);
		if (numberOfStatements <= config.getRDMinArea()) {
			size = config.getRDMinArea();
		} else {
			size = numberOfStatements;
		}
		String properties = String.format(
				"height: %f, transparency: %f, size: %f, color: \'%s\'", height, config.getRDMethodTransparency(), size, config.getRDMethodColorHex()
		);

		connector.executeWrite(cypherCreateNode(parent, function.id(), Labels.DiskSegment.name(), properties));
	}

	private void variableToDisk(Long variable, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", config.getRDRingWidthAD(),
				config.getRDHeight(), config.getRDDataTransparency(), config.getRDDataColorHex());

		connector.executeWrite(cypherCreateNode(parent, variable, Labels.Disk.name(), properties));
	}

	private void variableToDiskSegment(Long variable, Long parent) {
		String properties = String.format("size: %f, height: %f, transparency: %f, color: \'%s\'", 1.0, config.getRDHeight(),
				config.getRDDataTransparency(), config.getRDDataColorHex());
		connector.executeWrite(cypherCreateNode(parent, variable, Labels.DiskSegment.name(), properties));
	}
	
	private void unionStructEnumToDisk(Node element, Long parent) {
		String properties = String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", config.getRDRingWidth(),
				config.getRDHeight(), config.getRDClassTransparency(), config.getRDClassColorHex());
		connector.executeWrite(cypherCreateNode(parent, element.id(), Labels.Disk.name(), properties));

		connector.executeRead("MATCH (n)-[:DECLARES]->(v:Variable) WHERE ID(n) = " + element.id() +
				" AND EXISTS(v.hash) RETURN v").
				forEachRemaining((result) -> {
					Node subElement = result.get("v").asNode();
					if (config.isDataDisks()) {
						variableToDisk(subElement.id(), element.id());
					} else {
						variableToDiskSegment(subElement.id(), element.id());
					}
				});
		connector.executeRead("MATCH (n)-[:DECLARES]->(t) WHERE ID(n) = " + element.id() +
				" AND EXISTS(t.hash) AND (t:Union OR t:Struct OR t:Enum)  RETURN t").
				forEachRemaining((result) -> {
					Node subElement = result.get("t").asNode();
					unionStructEnumToDisk(subElement, element.id());
				});
	}

	private String cypherCreateNode(Long parent, Long visualizedNode, String label, String properties) {
		return String.format(
				"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->(n:RD:%s {%s})-[:VISUALIZES]->(s)",
				parent, visualizedNode, label, properties);
	}
}
