package org.getaviz.generator.jqa;

import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import java.util.Collections;

public class JQA2JSON implements Step {
	private SettingsConfiguration config;
	private Log log = LogFactory.getLog(this.getClass());
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private List<ProgrammingLanguage> languages;

	public JQA2JSON(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
		this.config = config;
		this.languages = languages;
	}

	public void run() {
		log.info("JQA2JSON has started.");
		ArrayList<Node> elements = new ArrayList<>();
		connector.executeRead("MATCH (n)<-[:VISUALIZES]-() RETURN n ORDER BY n.hash").forEachRemaining(result -> {
			elements.add(result.get("n").asNode());
		});
		Writer fw = null;
		try {
			String path = config.getOutputPath() + "metaData.json";
			fw = new FileWriter(path);
			fw.write(toJSON(elements));
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		log.info("JQA2JSON has finished.");
	}

	@Override
	public boolean checkRequirements() {
		return languages.contains(ProgrammingLanguage.JAVA);
	}

	private String toJSON(List<Node> list) {
		StringBuilder builder = new StringBuilder();
		boolean hasElements = false;
		for (final Node el : list) {
			if (!hasElements) {
				hasElements = true;
				builder.append("[{");
			} else {
				builder.append("\n},{");
			}
			if (el.hasLabel(Labels.Package.name())) {
				builder.append(toMetaDataNamespace(el));
				builder.append("\n");
			}
			if ((el.hasLabel(Labels.Class.name()) || el.hasLabel(Labels.Interface.name()))) {
				builder.append(toMetaDataClass(el));
				builder.append("\n");
			}
			if ((el.hasLabel(Labels.Type.name()) && el.hasLabel(Labels.Annotation.name()))) {
				builder.append(toMetaDataAnnotation(el));
				builder.append("\n");
			}
			if ((el.hasLabel(Labels.Type.name()) && el.hasLabel(Labels.Enum.name()))) {
				builder.append(toMetaDataEnum(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Method.name())) {
				builder.append(toMetaDataMethod(el));
				builder.append("\n");
			}
			if ((el.hasLabel(Labels.Field.name()) && (!el.hasLabel(Labels.Enum.name())))) {
				builder.append(toMetaDataAttribute(el));
			}
			if ((el.hasLabel(Labels.Field.name()) && el.hasLabel(Labels.Enum.name()))) {
				builder.append(toMetaDataEnumValue(el));
				builder.append("\n");
			}
		}
		if (hasElements) {
			builder.append("}]");
		}
		return builder.toString();
	}

	private String toMetaDataNamespace(Node namespace) {
		StatementResult parentHash = connector
				.executeRead("MATCH (parent:Package)-[:CONTAINS]->(namespace) WHERE ID(namespace) = " + namespace.id()
						+ " RETURN parent.hash");
		String belongsTo = "root";
		if (parentHash.hasNext()) {
			belongsTo = parentHash.single().get("parent.hash").asString();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("\"id\":            \"" + namespace.get("hash").asString() + "\",");
		builder.append("\n");
		builder.append("\"qualifiedName\": \"" + namespace.get("fqn").asString() + "\",");
		builder.append("\n");
		builder.append("\"name\":          \"" + namespace.get("name").asString() + "\",");
		builder.append("\n");
		builder.append("\"type\":          \"FAMIX.Namespace\",");
		builder.append("\n");
		builder.append("\"belongsTo\":     \"" + belongsTo + "\"");
		builder.append("\n");
		return builder.toString();
	}

	private String toMetaDataClass(Node c) {
		String belongsTo = "";
		StatementResult parent = connector
				.executeRead("MATCH (parent:Type)-[:DECLARES]->(class) WHERE ID(class) = " + c.id() + " RETURN parent");
		if (parent.hasNext()) {
			belongsTo = parent.single().get("parent").asNode().get("hash").asString("XXX");
		} else {
			parent = connector.executeRead(
					"MATCH (parent:Package)-[:CONTAINS]->(class) WHERE ID(class) = " + c.id() + " RETURN parent");
			belongsTo = parent.single().get("parent").asNode().get("hash").asString("YYY");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("\"id\":            \"" + c.get("hash").asString() + "\",");
		builder.append("\n");
		builder.append("\"qualifiedName\": \"" + c.get("fqn").asString() + "\",");
		builder.append("\n");
		builder.append("\"name\":          \"" + c.get("name").asString() + "\",");
		builder.append("\n");
		builder.append("\"type\":          \"FAMIX.Class\",");
		builder.append("\n");
		builder.append("\"modifiers\":     \"" + getModifiers(c) + "\",");
		builder.append("\n");
		builder.append("\"subClassOf\":    \"" + getSuperClasses(c) + "\",");
		builder.append("\n");
		builder.append("\"superClassOf\":  \"" + getSubClasses(c) + "\",");
		builder.append("\n");
		builder.append("\"belongsTo\":     \"" + belongsTo + "\"");
		builder.append("\n");
		return builder.toString();
	}

	private String toMetaDataAttribute(Node attribute) {
		String belongsTo = "";
		String declaredType = "";
		StatementResult parent = connector
				.executeRead("MATCH (parent)-[:CONTAINS|DECLARES]->(attribute) WHERE ID(attribute) = " + attribute.id()
						+ " RETURN parent.hash");
		if (parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		Node type = connector
				.executeRead("MATCH (attribute)-[:OF_TYPE]->(t) WHERE ID(attribute) = " + attribute.id() + " RETURN t")
				.next().get("t").asNode();
		if (type != null) {
			declaredType = type.get("name").asString();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("\"id\":            \"" + attribute.get("hash").asString() + "\",");
		builder.append("\n");
		builder.append("\"qualifiedName\": \"" + attribute.get("fqn").asString() + "\",");
		builder.append("\n");
		builder.append("\"name\":          \"" + attribute.get("name").asString() + "\",");
		builder.append("\n");
		builder.append("\"type\":          \"FAMIX.Attribute\",");
		builder.append("\n");
		builder.append("\"modifiers\":     \"" + getModifiers(attribute) + "\",");
		builder.append("\n");
		builder.append("\"declaredType\":  \"" + declaredType + "\",");
		builder.append("\n");
		builder.append("\"accessedBy\":\t \"" + getAccessedBy(attribute) + "\",");
		builder.append("\n");
		builder.append("\"belongsTo\":     \"" + belongsTo + "\"");
		builder.append("\n");
		return builder.toString();
	}

	private String toMetaDataMethod(Node method) {
		String belongsTo = "";
		StatementResult parent = connector.executeRead(
				"MATCH (parent)-[:DECLARES]->(method) WHERE ID(method) = " + method.id() + " RETURN parent.hash");
		if (parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		String signature = method.get("signature").asString();
		if (signature.contains(".")) {
			int lBraceIndex = signature.indexOf("(");
			signature = signature.substring(0, lBraceIndex + 1) + getParameters(method) + ")";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("\"id\":            \"" + method.get("hash").asString() + "\",");
		builder.append("\n");
		builder.append("\"qualifiedName\": \"" + StringEscapeUtils.escapeHtml4(method.get("fqn").asString()) + "\",");
		builder.append("\n");
		builder.append("\"name\":          \"" + method.get("name").asString() + "\",");
		builder.append("\n");
		builder.append("\"type\":          \"FAMIX.Method\",");
		builder.append("\n");
		builder.append("\"modifiers\":     \"" + getModifiers(method) + "\",");
		builder.append("\n");
		builder.append("\"signature\":  \t \"" + signature + "\",");
		builder.append("\n");
		builder.append("\"calls\":\t\t \"" + getCalls(method) + "\",");
		builder.append("\n");
		builder.append("\"calledBy\":\t\t \"" + getCalledBy(method) + "\",");
		builder.append("\n");
		builder.append("\"accesses\":\t \t \"" + getAccesses(method) + "\",");
		builder.append("\n");
		builder.append("\"belongsTo\":     \"" + belongsTo + "\"");
		builder.append("\n");
		return builder.toString();
	}

	private String toMetaDataEnum(Node e) {
		String belongsTo = "";
		StatementResult parent = connector.executeRead("MATCH (parent)-[:DECLARES]->(enum) WHERE ID(enum) = " + e.id() 
			+ " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}		
		StringBuilder builder = new StringBuilder();
	    builder.append("\"id\":            \"" + e.get("hash").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"qualifiedName\": \"" +  e.get("fqn").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"name\":          \"" + e.get("name").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"type\":          \"FAMIX.Enum\",");
	    builder.append("\n");
	    builder.append("\"modifiers\":     \"" + getModifiers(e) + "\",");
	    builder.append("\n");
	    builder.append("\"belongsTo\":     \"" + belongsTo + "\"");
	    builder.append("\n");
	    return builder.toString();
	}

	private String toMetaDataEnumValue(Node ev) {
		String belongsTo = "";
		StatementResult parent = connector.executeRead("MATCH (parent)-[:DECLARES]->(enumValue) WHERE ID(enumValue) = " + ev.id() 
			+ " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		StringBuilder builder = new StringBuilder();
	    builder.append("\"id\":            \"" + ev.get("hash").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"qualifiedName\": \"" + ev.get("fqn").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"name\":          \"" + ev.get("name").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"type\":          \"FAMIX.EnumValue\",");
	    builder.append("\n");
	    builder.append("\"belongsTo\":     \"" + belongsTo + "\"");
	    builder.append("\n");
	    return builder.toString();
	}

	private String toMetaDataAnnotation(Node annotation) {
		String belongsTo = "";
		StatementResult parent = connector.executeRead("MATCH (parent:Package)-[:CONTAINS|DECLARES]->(annotation) WHERE ID(annotation) = " + annotation.id() 
			+ " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}		
		StringBuilder builder = new StringBuilder();
	    builder.append("\"id\":            \"" + annotation.get("hash").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"qualifiedName\": \"" + annotation.get("fqn").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"name\":          \"" + annotation.get("name").asString() + "\",");
	    builder.append("\n");
	    builder.append("\"type\":          \"FAMIX.AnnotationType\",");
	    builder.append("\n");
	    builder.append("\"modifiers\":     \"" + getModifiers(annotation) + "\",");
	    builder.append("\n");
	    builder.append("\"subClassOf\":    \"\",");
	    builder.append("\n");
	    builder.append("\"superClassOf\":  \"\",");
	    builder.append("\n");
	    builder.append("\"belongsTo\":     \"" + belongsTo + "\"");
	    builder.append("\n");
	    return builder.toString();
	}

	private String getSuperClasses(Node element) {
		ArrayList<String> tmp = new ArrayList<>();
		connector.executeRead("MATCH (super:Type)<-[:EXTENDS]-(element) WHERE ID(element) = " + element.id() + " RETURN super").forEachRemaining((result) -> {
			Node node = result.get("super").asNode();
			if(node.containsKey("hash")) {
				tmp.add(node.get("hash").asString());
			}
		});
		Collections.sort(tmp);
		return removeBrackets(tmp);
	}

	private String getSubClasses(Node element) {
		ArrayList<String> tmp = new ArrayList<>();
		connector.executeRead("MATCH (sub:Type)-[:EXTENDS]->(element) WHERE ID(element) = " + element.id() + " RETURN sub").forEachRemaining((result) -> {
			Node node = result.get("sub").asNode();
			if(node.containsKey("hash")) {
				tmp.add(node.get("hash").asString());
			}
		});
		Collections.sort(tmp);
		return removeBrackets(tmp);
	}

	private String getAccessedBy(Node element) {
		ArrayList<String> tmp = new ArrayList<>();
		connector.executeRead("MATCH (access)-[:WRITES|READS]->(element) WHERE ID(element) = " + element.id() + " RETURN access").forEachRemaining((result) -> {
			Node node = result.get("access").asNode();
			if(node.containsKey("hash")) {
				tmp.add(node.get("hash").asString());
			}			
		});
		Collections.sort(tmp);
		return removeBrackets(tmp);
	}

	private String getAccesses(Node element) {
		ArrayList<String> tmp = new ArrayList<>();
		connector.executeRead("MATCH (access)<-[:WRITES|READS]-(element) WHERE ID(element) = " + element.id() + " RETURN access").forEachRemaining((result) -> {
			Node node = result.get("access").asNode();
			if(node.containsKey("hash")) {
				tmp.add(node.get("hash").asString());
			}			
		});
		Collections.sort(tmp);
		return removeBrackets(tmp);
	}

	private String getCalls(Node element) {
		ArrayList<String> tmp = new ArrayList<>();
		connector.executeRead("MATCH (element)-[:INVOKES]->(call) WHERE ID(element) = " + element.id() + " RETURN call").forEachRemaining((result) -> {
			Node node = result.get("call").asNode();
			if(node.containsKey("hash")) {
				tmp.add(node.get("hash").asString());
			}
		});
		Collections.sort(tmp);
		return removeBrackets(tmp);
	}

	private String getCalledBy(Node element) {
		ArrayList<String> tmp = new ArrayList<>();
		connector.executeRead("MATCH (element)<-[:INVOKES]-(call) WHERE ID(element) = " + element.id() + " RETURN call").forEachRemaining((result) -> {
			Node node = result.get("call").asNode();
			if(node.containsKey("hash")) {
				tmp.add(node.get("hash").asString());
			}
		});
		Collections.sort(tmp);
		return removeBrackets(tmp);
	}

	private String getModifiers(Node element) {
		ArrayList<String> tmp = new ArrayList<>();
		if (element.containsKey("visibility")) {
			tmp.add(element.get("visibility").asString());
		}
		if (element.containsKey("final")) {
			if (element.get("final").asBoolean()) {
				tmp.add("final");
			}
		}
		if (element.containsKey("abstract")) {
			if (element.get("abstract").asBoolean()) {
				tmp.add("abstract");
			}
		}
		if (element.containsKey("static")) {
			tmp.add("static");
		}
		Collections.sort(tmp);
		return removeBrackets(tmp);
	}

	private String getParameters(Node method) {
		ArrayList<String> parameterList = new ArrayList<>();
		connector.executeRead("MATCH (method)-[:HAS]->(p:Parameter) WHERE ID(method) = " + method.id() + " RETURN p ORDER BY p.index ASC").forEachRemaining((result) -> {
			Node parameter = result.get("p").asNode();
			connector.executeRead("MATCH (parameter)-[:OF_TYPE]->(t) WHERE ID(parameter) = " + parameter.id() + " RETURN t.name").forEachRemaining((result2) -> {
				parameterList.add(result2.get("t.name").asString());
			});
		});
		return removeBrackets(parameterList);
	}

	private String removeBrackets(List<String> list) {
		return removeBrackets(list.toString());
	}

	private String removeBrackets(String string) {
		return StringUtils.remove(StringUtils.remove(string, "["), "]");
	}
}