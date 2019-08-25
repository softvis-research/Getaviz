package org.getaviz.generator.jqa;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.graphdb.QueryExecutionException;

import java.io.IOException;

public class JQACEnhancement {
	private final Log log = LogFactory.getLog(JQACEnhancement.class);
	private final SettingsConfiguration config = SettingsConfiguration.getInstance();
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private String fileNameTranslationUnit;
	private Runtime runtime = Runtime.getRuntime();


	public JQACEnhancement() {
		log.info("JQACEnhancement has started.");
		scan();
		addHashes();
		log.info("JQACEnhancement finished");
	}

	public void scan() {
		log.info("jQA scan started.");
		log.info("Scanning from URI(s) " + config.getInputFiles());
		try {
			Process pScan = runtime.exec("/opt/jqassistant/bin/jqassistant.sh scan -reset -u " + config.getInputFiles() + " -storeUri " +
					DatabaseConnector.getDatabaseURL());
			pScan.waitFor();
		} catch (InterruptedException e) {
			log.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		}
		log.info("jQA scan ended.");
	}

	private void enhanceNode(Record record) {
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
				"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.name = \'" + fileName + "\', n.fqn = \'" + fileName + "\', n.hash = \'" + createHash(fileName) +  "\'");

	}

	private void enhanceNode3(Record record) {
		Node node = record.get("n").asNode();
		if(node.get("hash").isNull()){
			String hash = createHash(Long.toString(node.id()));
			connector.executeWrite(
					"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.hash = \'" + hash + "\'");
		}
	}

	private void enhanceNode2(Record record) {



		Node node = record.get("e").asNode();
		Node declaringParent = record.get("p").asNode();

		fileNameTranslationUnit = connector.executeRead("MATCH (t:TranslationUnit)-[:DECLARES*]->(e) WHERE ID(e) = " + node.id() + " RETURN t.fileName").single().get("t.fileName").asString();


		String fileNameChild = node.get("fileName").toString();
		//fileNameTranslationUnit = record.get("n.fileName").toString();

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
		//}
	}
	
	private void addHashes() throws QueryExecutionException {
		connector.executeRead("MATCH (n:TranslationUnit)<-[:CONTAINS]-(f:File) RETURN n, f.fileName").forEachRemaining(this::enhanceNode);
		connector.executeRead("MATCH (p)-[:DECLARES]->(e) RETURN e, p").forEachRemaining(this::enhanceNode2);
		connector.executeRead("MATCH (n:Condition) RETURN n").forEachRemaining(this::enhanceNode3);
	}
	
	private String createHash(String fqn) {
		return "ID_" + DigestUtils.sha1Hex(fqn + config.getRepositoryName() + config.getRepositoryOwner());
	}
}
