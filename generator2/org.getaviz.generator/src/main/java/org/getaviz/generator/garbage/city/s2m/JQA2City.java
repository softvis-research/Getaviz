package org.getaviz.generator.garbage.city.s2m;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.garbage.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.SettingsConfiguration.ClassElementsModes;
import org.getaviz.generator.SettingsConfiguration.Original.BuildingMetric;
import org.getaviz.generator.garbage.Step;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.garbage.Labels;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import java.util.GregorianCalendar;
import java.util.List;

public class JQA2City implements Step {
	private Log log = LogFactory.getLog(this.getClass());
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private ClassElementsModes classElementsMode;
	private String buildingTypeAsString;
	private BuildingType buildingType;
	private BuildingMetric originalBuildingMetric;
	private List<ProgrammingLanguage> languages;

	public JQA2City(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
		this.classElementsMode = config.getClassElementsMode();
		this.buildingTypeAsString = config.getBuildingTypeAsString();
		this.buildingType = config.getBuildingType();
		this.originalBuildingMetric = config.getOriginalBuildingMetric();
		this.languages = languages;
	}

	@Override
	public boolean checkRequirements() {
		return languages.contains(ProgrammingLanguage.JAVA);
	}

	public void run() {
		log.info("JQA2City started");
		connector.executeWrite("MATCH (n:City) DETACH DELETE n");
		long model = connector.addNode(
				String.format("CREATE (n:Model:City {date: '%s', building_type: '%s'})",
						new GregorianCalendar().getTime().toString(), buildingTypeAsString),"n").id();
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

	private void namespaceToDistrict(Long namespace, Long parent) {
		long district = connector.addNode(cypherCreateNode(parent,namespace,Labels.District.name()),"n").id();
		StatementResult subPackages = connector.executeRead("MATCH (n)-[:CONTAINS]->(p:Package) WHERE ID(n) = " + namespace +
			" RETURN p");
		StatementResult subTypes = connector.executeRead("MATCH (n)-[:CONTAINS]->(t:Type) WHERE ID(n) = " + namespace +
			" AND EXISTS(t.hash) AND (t:Class OR t:Interface) AND NOT t:Inner RETURN t");
		subPackages.forEachRemaining((result) -> namespaceToDistrict(result.get("p").asNode().id(), district));
		subTypes.forEachRemaining((result) -> structureToBuilding(result.get("t").asNode().id(), district));
	}

	private void structureToBuilding(Long structure, Long parent) {
		long building = connector.addNode(cypherCreateNode(parent, structure, Labels.Building.name()),"n").id();
		StatementResult methods = connector.executeRead("MATCH (n)-[:DECLARES]->(m:Method) WHERE ID(n) = " + structure +
			" AND EXISTS(m.hash) RETURN m");
		StatementResult attributes = connector.executeRead("MATCH (n)-[:DECLARES]->(a:Field) WHERE ID(n) = " + structure +
			" AND EXISTS(a.hash) RETURN a");
		if (buildingType == BuildingType.CITY_FLOOR && methods != null) {
			methods.forEachRemaining((result) -> methodToFloor(result.get("m").asNode().id(), building));
			attributes.forEachRemaining((result) -> attributeToChimney(result.get("a").asNode().id(), building));
		} else {
			if (originalBuildingMetric == BuildingMetric.NOS) {
				int numberOfStatements = 0;
				if (methods != null) {
					for(Record record : methods.list()) {
						Node method = record.get("m").asNode();
						int effectiveLineCount = 0;
						if (method.containsKey("effectiveLineCount")) {
							effectiveLineCount = method.get("effectiveLineCount").asInt();
						}
						numberOfStatements += effectiveLineCount;
					}
				}
				connector.executeWrite("MATCH(n) WHERE ID(n) = " + building + " SET n.numberOfStatements = " +
					numberOfStatements);
			}

			if ((classElementsMode == ClassElementsModes.METHODS_AND_ATTRIBUTES ||
				classElementsMode == ClassElementsModes.METHODS_ONLY)) {
				if (methods != null) {
					methods.forEachRemaining((result) -> methodToBuildingSegment(result.get("m").asNode().id(), building));
				}
			}

			if (classElementsMode == ClassElementsModes.METHODS_AND_ATTRIBUTES ||
				classElementsMode == ClassElementsModes.ATTRIBUTES_ONLY) {
				attributes.forEachRemaining((result) -> attributeToBuildingSegment(result.get("a").asNode().id(), building));
			}
		}
		StatementResult subStructures = connector.executeRead("MATCH (n)-[:DECLARES]->(t:Type:Inner) WHERE ID(n) = " + structure +
			" AND EXISTS(t.hash) RETURN t");
		subStructures.forEachRemaining((result) -> structureToBuilding(result.get("t").asNode().id(), parent));
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
