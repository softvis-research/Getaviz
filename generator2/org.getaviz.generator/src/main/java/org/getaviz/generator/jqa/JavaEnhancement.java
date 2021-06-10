package org.getaviz.generator.jqa;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import java.util.List;

public class JavaEnhancement implements Step {
	private Log log = LogFactory.getLog(this.getClass());
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private boolean skipScan;
	private List<ProgrammingLanguage> languages;


	public JavaEnhancement(boolean skipScan, List<ProgrammingLanguage> languages) {
		this.skipScan = skipScan;
		this.languages = languages;
	}

	public boolean checkRequirements() {
		if(!languages.contains(ProgrammingLanguage.JAVA)) return false;
		return !skipScan;
	}

	public void run() {
		if(checkRequirements()) {
			log.info("Java enhancement started.");
			connector.executeWrite(labelGetter(), labelSetter(), labelPrimitives(), labelInnerTypes());
			connector.executeWrite(labelAnonymousInnerTypes());
			addHashes();
			log.info("Java enhancement finished");
		}
	}

	private void addHashes() {
		connector.executeRead(collectAllPackages()).forEachRemaining(this::enhanceNode);
		connector.executeRead(collectAllTypes()).forEachRemaining(this::enhanceNode);
		connector.executeRead(collectAllFields()).forEachRemaining(this::enhanceNode);
		connector.executeRead(collectAllMethods()).forEachRemaining(this::enhanceNode);
	}

	private String createHash(String fqn) {
		return "ID_" + DigestUtils.sha1Hex(fqn);
	}

	private String labelPrimitives() {
		return "MATCH (n:Type) WHERE n.name =~ \"[a-z]+\" SET n:Primitive";
	}

	private String labelGetter() {
		return "MATCH (o:Type)-[:DECLARES]->(method:Method)-[getter:READS]->(attribute:Field)<-[:DECLARES]-(q:Type) " + 
				"WHERE method.name =~ \"get[A-Z]+[A-Za-z]*\" " + 
				"AND toLower(method.name) contains(attribute.name) AND ID(o) = ID(q) " + 
				"SET method:Getter";
	}

	private String labelSetter() {
		return "MATCH (o:Type)-[:DECLARES]->(method:Method)-[setter:WRITES]->(attribute:Field)<-[:DECLARES]-(q:Type) " + 
				"WHERE method.name =~ \"set[A-Z]+[A-Za-z]*\" " +
				"AND toLower(method.name) contains(attribute.name) AND ID(o) = ID(q) " + 
				"SET method:Setter";
	}

	private String labelInnerTypes() {
		return "MATCH (:Type)-[:DECLARES]->(innerType:Type) SET innerType:Inner";
	}

	private String labelAnonymousInnerTypes() {
		return "MATCH (innerType:Inner:Type) WHERE innerType.name =~ \".*\\\\$[0-9]*\" SET innerType:Anonymous";
	}

	private String collectAllPackages() {
		return "MATCH (n:Package) RETURN n";
	}

	private String collectAllTypes() {
		return "MATCH (n:Type) " +
				"WHERE (n:Interface OR n:Class OR n:Enum OR n:Annotation) " + 
				"AND NOT n:Anonymous AND NOT (n)<-[:CONTAINS]-(:Method) " +
				"RETURN n";
	}

	private String collectAllFields() {
		return "MATCH (n:Field)<-[:DECLARES]-(f:Type) " +
				"WHERE (NOT n.name CONTAINS '$') AND (NOT f:Anonymous) RETURN DISTINCT n";
	}

	private String collectAllMethods() {
		return "MATCH (n:Method)<-[:DECLARES]-(f:Type) " +
				"WHERE (NOT n.name CONTAINS '$') AND (NOT f:Anonymous) RETURN DISTINCT n";
	}

	private void enhanceNode(Record record) {
		Node node = record.get("n").asNode();
		String signatureForHash = getSignature(node);
		Value fqnValue = node.get("fqn");
		String fqn = fqnValue.asString();
		if (fqnValue.isNull()) {
			Node container = connector.executeRead(
				"MATCH (n)<-[:DECLARES]-(container) " +
				"WHERE ID(n) = " + node.id() + " " +
				"RETURN container"
			).single().get("container").asNode();
			String containerFqn = container.get("fqn").asString();
			String name = node.get("name").asString();
			String signature = node.get("signature").asString();
			int index = signature.indexOf(" ") + 1;
			if (node.hasLabel("Method")) {
				int indexOfBracket = signature.indexOf("(");
				if (name.isEmpty()) {
					name = signature.substring(index, indexOfBracket);
				}
				fqn = containerFqn + "." + signature.substring(index);
			} else {
				if (name.isEmpty()) {
					name = signature.substring(index);
				}
				fqn = containerFqn + "." + name;
			}
			connector.executeWrite(
 	"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.name = '" + name + "', n.fqn = '" + fqn + "'");
		}
		connector.executeWrite(
	"MATCH (n) WHERE ID(n) = " + node.id() + " SET n.hash = '" + createHash(fqn + signatureForHash) + "'");
	}

	private String getSignature(Node node) {
		if(node.containsKey("signature")) {
			return node.get("signature").asString();
		} else {
			return "";
		}
	}
}
