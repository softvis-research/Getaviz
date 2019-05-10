package org.getaviz.generator.city.s2m;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import java.util.GregorianCalendar;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes;
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;

public class JQA2City {
	private SettingsConfiguration config = SettingsConfiguration.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private DatabaseConnector connector = DatabaseConnector.getInstance();

	public JQA2City() {
		log.info("JQA2City started");
		connector.executeWrite("MATCH (n:City) DETACH DELETE n");
		long model = connector.addNode(
			String.format("CREATE (n:Model:City {date: \'%s\', building_type: \'%s\'})",
				new GregorianCalendar().getTime().toString(), config.getBuildingTypeAsString()),"n").id();
		connector.executeRead(
			"MATCH (n:Package) " +
			"WHERE NOT (n)<-[:CONTAINS]-(:Package) " + 
			"RETURN n" 
		).forEachRemaining((result) -> {
			long namespace = result.get("n").asNode().id();
			namespaceToDistrict(namespace, model);
		});
		log.info("JQA2City finished");
	}

	private Long namespaceToDistrict(Long namespace, Long parent) {
		long district = connector.addNode(cypherCreateNode(parent,namespace,Labels.District.name()),"n").id();
		StatementResult subPackages = connector.executeRead("MATCH (n)-[:CONTAINS]->(p:Package) WHERE ID(n) = " + namespace +
			" RETURN p");
		StatementResult subTypes = connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + namespace +
			" AND EXISTS(t.hash) AND (t:Class OR t:Interface) AND NOT t:Inner RETURN t");
		subPackages.forEachRemaining((result) -> {
			namespaceToDistrict(result.get("p").asNode().id(), district);
		});
		subTypes.forEachRemaining((result) -> {
			structureToBuilding(result.get("t").asNode().id(), district);
		});
		return district;
	}

	private Long structureToBuilding(Long structure, Long parent) {
		long building = connector.addNode(cypherCreateNode(parent, structure, Labels.Building.name()),"n").id();
		StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = " + structure +
			" AND EXISTS(m.hash) RETURN m");
		StatementResult attributes = connector.executeRead("MATCH (n)-[:DECLARES]->(a:Field) WHERE ID(n) = " + structure +
			" AND EXISTS(a.hash) RETURN a");
		if (config.getBuildingType() == BuildingType.CITY_FLOOR && methods != null) {
			methods.forEachRemaining((result) -> {
				methodToFloor(result.get("m").asNode().id(), building);
			});
			attributes.forEachRemaining((result) -> {
				attributeToChimney(result.get("a").asNode().id(), building);
			});
		} else {
			if (config.getOriginalBuildingMetric() == BuildingMetric.NOS) {
				int numberOfStatements = 0;
				for(Record record : methods.list()) {
					Node method = record.get("m").asNode();
					int effectiveLineCount = 0;
					if (method.containsKey("effectiveLineCount")) {
						effectiveLineCount = method.get("effectiveLineCount").asInt();
					}
					numberOfStatements += effectiveLineCount;
				}
				connector.executeWrite("MATCH(n) WHERE ID(n) = " + building + " SET n.numberOfStatements = " +
					numberOfStatements);
			}

			if ((config.getClassElementsMode() == ClassElementsModes.METHODS_AND_ATTRIBUTES ||
				config.getClassElementsMode() == ClassElementsModes.METHODS_ONLY)) {
				methods.forEachRemaining((result) -> {
					methodToBuildingSegment(result.get("m").asNode().id(), building);
				});
			}

			if (config.getClassElementsMode() == ClassElementsModes.METHODS_AND_ATTRIBUTES ||
				config.getClassElementsMode() == ClassElementsModes.ATTRIBUTES_ONLY) {
				attributes.forEachRemaining((result) -> {
					attributeToBuildingSegment(result.get("a").asNode().id(), building);
				});
			}
		}
		StatementResult subStructures = connector.executeRead("MATCH (n)-[:DECLARES]->(t:Type:Inner) WHERE ID(n) = " + structure +
			" AND EXISTS(t.hash) RETURN t");
		subStructures.forEachRemaining((result) -> {
			structureToBuilding(result.get("t").asNode().id(), parent);
		});
		return building;
	}

	private void methodToBuildingSegment(Long method, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, method, Labels.BuildingSegment.name()));
	}

	private void methodToFloor(Long method, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, method, Labels.BuildingSegment.name() + ":" + Labels.Floor.name()));
	}

	private void attributeToBuildingSegment(Long attribute, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.BuildingSegment.name()));
	}

	private void attributeToChimney(Long attribute, Long parent) {
		connector.executeWrite(cypherCreateNode(parent, attribute, Labels.BuildingSegment.name() + ":" + Labels.Chimney.name()));
	}

	private String cypherCreateNode(Long parent, Long visualizedNode, String label) {
		return String.format(
			"MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->(n:City:%s)-[:VISUALIZES]->(s)",
			parent, visualizedNode, label);
	}
}
