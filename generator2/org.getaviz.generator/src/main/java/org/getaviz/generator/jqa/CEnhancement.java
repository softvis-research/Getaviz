package org.getaviz.generator.jqa;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.graphdb.QueryExecutionException;

import java.util.List;

public class CEnhancement implements Step {
	private final Log log = LogFactory.getLog(CEnhancement.class);
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private String fileNameTranslationUnit;
	private boolean skipScan;
	private List<ProgrammingLanguage> languages;

	public CEnhancement(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
		skipScan = config.isSkipScan();
		this.languages = languages;
	}

	public boolean checkRequirements() {
		if(!languages.contains(ProgrammingLanguage.C)) return false;
		return !skipScan;
	}

	public void run() {
		if(checkRequirements()) {
			log.info("CEnhancement has started.");
			addHashes();
			log.info("CEnhancement finished");
		}
	}

	private void addHashes() throws QueryExecutionException {
		connector.executeRead("MATCH (n:TranslationUnit)<-[:CONTAINS]-(f:File) RETURN n, f.fileName").forEachRemaining(this::enhanceTranslationUnit);
		connector.executeRead("MATCH (p)-[:DECLARES]->(e) RETURN e, p").forEachRemaining(this::enhanceNode);
		connector.executeRead("MATCH (n:Condition) RETURN n").forEachRemaining(this::enhanceCondition);
	}

	private void enhanceTranslationUnit(Record record) {
		Node node = record.get("n").asNode();
		String fileName = record.get("f.fileName").asString();
		if(fileName.endsWith(".ast")) {
			fileName = fileName.replaceAll(".ast", "");
			if(!fileName.endsWith(".h")) {
				fileName = fileName + ".c";
			}
		}

		this.fileNameTranslationUnit = fileName;
		connector.executeWrite(
				"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.name = \'" + fileName + "\', n.fqn = \'" + fileName + "\', n.hash = \'" + createHash(fileName) +  "\'"
		);

	}

	private void enhanceNode(Record record) {
		Node node = record.get("e").asNode();
		Node declaringParent = record.get("p").asNode();

		fileNameTranslationUnit = connector.executeRead("MATCH (t:TranslationUnit)-[:DECLARES*]->(e) WHERE ID(e) = " + node.id() + " RETURN t.fileName").single().get("t.fileName").asString();

		// this was included to exclude C elements declared outside the translation unit (usage of #include)
		// but this does not work with the current jqassistant setup, since the files gets downloaded and thereby the filename is changed which is crucial
		//if (fileNameChild.equals(fileNameTranslationUnit)) {
		//If variable/constant is part of a struct, union or enum it could have the same name as another variable/constant therefore add parent name to name.
		String fqn = "";
		if ((node.hasLabel(Labels.Variable.name()) || node.hasLabel(Labels.EnumConstant.name())) && declaringParent != null
				&& (declaringParent.hasLabel(Labels.Struct.name()) || declaringParent.hasLabel(Labels.Union.name()) || declaringParent.hasLabel(Labels.Enum.name()))) {
			fqn = fileNameTranslationUnit + "_" + declaringParent.get("name") + "_" + node.get("name");
		} else if (node.get("fqn").isNull()) {
			fqn = fileNameTranslationUnit + "_" + node.get("name");
		}

		if (node.get("hash").isNull()) {
			String hash = createHash(node.get("fqn").toString());
			connector.executeWrite(
					"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.hash = \'" + hash + "\', n.fqn = \'" + fqn + "\'");
		} else {
			connector.executeWrite(
					"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.fqn = \'" + fqn + "\'");
		}
	}

	private void enhanceCondition(Record record) {
		Node node = record.get("n").asNode();
		if(node.get("hash").isNull()){
			String hash = createHash(Long.toString(node.id()));
			connector.executeWrite(
					"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.hash = \'" + hash + "\'"
			);
		}
	}

	private String createHash(String fqn) {
		return "ID_" + DigestUtils.sha1Hex(fqn);
	}
}
