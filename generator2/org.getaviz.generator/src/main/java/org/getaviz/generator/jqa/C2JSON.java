package org.getaviz.generator.jqa;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

public class C2JSON implements Step {
	private Log log = LogFactory.getLog(this.getClass());
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private SettingsConfiguration config;
	private List<ProgrammingLanguage> languages;

	public C2JSON(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
		this.config = config;
		this.languages = languages;
	}

	public boolean checkRequirements() {
		return languages.contains(ProgrammingLanguage.C);
	}

	public void run() {
		if(checkRequirements()) {
			log.info("C2JSON has started.");
			ArrayList<Node> elements = new ArrayList<>();
			connector.executeRead("MATCH (n:Condition) RETURN n").forEachRemaining((result) -> {
				elements.add(result.get("n").asNode());
			});

			connector.executeRead("MATCH (n)<-[:VISUALIZES]-() WHERE EXISTS(n.hash) RETURN n ORDER BY n.hash").forEachRemaining((result) -> {
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
			log.info("C2JSON has finished.");
		}
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
			if (el.hasLabel(Labels.TranslationUnit.name())) {
				builder.append(toMetaDataTranslationUnit(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Function.name())) {
				builder.append(toMetaDataFunction(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Variable.name())) {
				builder.append(toMetaDataVariable(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.SingleCondition.name())) {
				builder.append(toMetaDataSingleCondition(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Not.name())) {
				builder.append(toMetaDataNegation(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.And.name())) {
				builder.append(toMetaDataAnd(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Or.name())) {
				builder.append(toMetaDataOr(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Struct.name())) {
				builder.append(toMetaDataStruct(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Union.name())) {
				builder.append(toMetaDataUnion(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.Enum.name())) {
				builder.append(toMetaDataEnum(el));
				builder.append("\n");
			}
			if (el.hasLabel(Labels.EnumConstant.name())) {
				builder.append(toMetaDataEnumValue(el));
				builder.append("\n");
			}
		}
		if (hasElements) {
			builder.append("}]");
		}
		return builder.toString();
	}

	private String toMetaDataTranslationUnit(Node translationUnit) {
		return formatLine("id", translationUnit.get("hash").asString()) +
				formatLine("qualifiedName", translationUnit.get("fqn").asString()) +
				formatLine("name", translationUnit.get("name").asString()) +
				formatLine("type", "TranslationUnit") +
				formatLine("belongsTo", "root") +
				formatEndline("filename", translationUnit.get("fileName").asString());
	}

	private String toMetaDataFunction(Node function) {
		String belongsTo = "";
		String dependsOn = "";
		StatementResult parent =  connector.executeRead(
				"MATCH (parent)-[:DECLARES]->(function:Function) WHERE ID(function) = " + function.id() + " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		String dependent = "";
		StatementResult dependentList = connector.executeRead(
				"MATCH (function:Function)-[:DEPENDS_ON]->(el) WHERE ID(function) = " + function.id() + " RETURN el");
		if(dependentList.hasNext()) {
			dependent = dependentList.single().get("el.hash").asString();
		}

		return formatLine("id", function.get("hash").asString()) +
				formatLine("qualifiedName", escapeHtml4(function.get("fqn").asString())) +
				formatLine("name", function.get("name").asString()) +
				formatLine("type", "FAMIX.Function") +
				formatLine("signature", getFunctionSignature(function)) +
				formatLine("calls", getCalls(function)) +
				formatLine("calledBy", getCalledBy(function)) +
				formatLine("accesses", getAccesses(function)) +
				formatLine("belongsTo", belongsTo) +
				formatLine("dependsOn", dependsOn) +
				formatEndline("filename", function.get("fileName").asString());
	}

	private String toMetaDataVariable(Node variable) {
		String belongsTo = "";
		String declaredType = "";
		String dependsOn = "";
		StatementResult parent =  connector.executeRead(
				"MATCH (parent)-[:DECLARES]->(variable:Variable) WHERE ID(variable) = " + variable.id() + " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		StatementResult type =  connector.executeRead(
				"MATCH (variable:Variable)-[:OF_TYPE]->(type) WHERE ID(variable) = " + variable.id() + " RETURN type.name");
		if(type.hasNext()) {
			declaredType = type.single().get("type.name").asString();
		}
		StatementResult dependent =  connector.executeRead(
				"MATCH (variable:Variable)-[:DEPENDS_ON]->(type) WHERE ID(variable) = " + variable.id() + " RETURN type.hash");
		if(dependent.hasNext()){
			dependsOn = dependent.single().get("type.hash").asString();
		}

		return formatLine("id", variable.get("hash").asString()) +
				formatLine("qualifiedName", variable.get("fqn").asString()) +
				formatLine("name", variable.get("name").asString()) +
				formatLine("type", "FAMIX.Variable") +
				formatLine("declaredType", declaredType) +
				formatLine("accessedBy", getAccessedBy(variable)) +
				formatLine("belongsTo", belongsTo) +
				formatLine("dependsOn", dependsOn) +
				formatEndline("fileName", variable.get("fileName").asString());
	}

	private String toMetaDataSingleCondition(Node singleCondition) {
		return formatLine("id", singleCondition.get("hash").asString()) +
				formatLine("qualifiedName", singleCondition.get("fqn").asString()) +
				formatLine("name", singleCondition.get("MacroName").asString()) +
				formatEndline("type", "Macro");
	}

	private String toMetaDataNegation(Node negation) {
		String negated = "";
		Node negatedNode;
		try {
			StatementResult negations =  connector.executeRead(
					"MATCH (negation:Negation)-[:NEGATES]->(condition) WHERE ID(negation) = " + negation.id() + " RETURN condition.hash");
			if(negations.hasNext()) {
				negated = negations.single().get("condition.hash").asString();
			}
		} catch (Exception e) {
			negated = "";
		}

		return formatLine("id", negation.get("hash").asString()) +
				formatLine("type", "Negation") +
				formatEndline("negated", negated);
	}

	private String toMetaDataAnd(Node andNode) {
		ArrayList<String> connectedConditions = new ArrayList<>();
		StatementResult connections = connector.executeRead(
				"MATCH (andNode:And)-[:CONNECTS]->(condition) WHERE ID(andNode) = " + andNode.id() + " RETURN condition.hash");
		connections.forEachRemaining(condition -> {
			connectedConditions.add(condition.get("hash").asString());
		});

		return formatLine("id", andNode.get("hash").asString()) +
				formatLine("type", "And") +
				formatEndline("connected", removeBrackets(connectedConditions));
	}

	private String toMetaDataOr(Node orNode) {
		ArrayList<String> connectedConditions = new ArrayList<>();
		StatementResult connections = connector.executeRead(
				"MATCH (orNode:And)-[:CONNECTS]->(condition) WHERE ID(orNode) = " + orNode.id() + " RETURN condition.hash");
		connections.forEachRemaining(condition -> {
			connectedConditions.add(condition.get("hash").asString());
		});

		return formatLine("id", orNode.get("hash").asString()) +
				formatLine("type", "Or") +
				formatEndline("connected", removeBrackets(connectedConditions));
	}

	private String toMetaDataStruct(Node struct) {
		String belongsTo = "";
		String dependsOn = "";
		StatementResult parent =  connector.executeRead(
				"MATCH (parent)-[:DECLARES]->(struct:Struct) WHERE ID(struct) = " + struct.id() + " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		StatementResult dependent =  connector.executeRead(
				"MATCH (struct:Struct)-[:DEPENDS_ON]->(type) WHERE ID(struct) = " + struct.id() + " RETURN type.hash");
		if(dependent.hasNext()){
			dependsOn = dependent.single().get("type.hash").asString();
		}

		return formatLine("id", struct.get("hash").asString()) +
				formatLine("qualifiedName", struct.get("fqn").asString()) +
				formatLine("name", struct.get("name").asString()) +
				formatLine("type", "Struct") +
				formatLine("belongsTo", belongsTo) +
				formatLine(dependsOn, dependsOn) +
				formatLine("filename", struct.get("fileName").asString());
	}

	private String toMetaDataUnion(Node union) {
		String belongsTo = "";
		String dependsOn = "";
		StatementResult parent =  connector.executeRead(
				"MATCH (parent)-[:DECLARES]->(union:Union) WHERE ID(union) = " + union.id() + " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		StatementResult dependent =  connector.executeRead(
				"MATCH (union:Union)-[:DEPENDS_ON]->(type) WHERE ID(union) = " + union.id() + " RETURN type.hash");
		if(dependent.hasNext()){
			dependsOn = dependent.single().get("type.hash").asString();
		}

		return formatLine("id", union.get("hash").asString()) +
				formatLine("qualifiedName", union.get("fqn").asString()) +
				formatLine("name", union.get("name").asString()) +
				formatLine("type", "Union") +
				formatLine("belongsTo", belongsTo) +
				formatLine(dependsOn, dependsOn) +
				formatLine("filename", union.get("fileName").asString());
	}

	private String toMetaDataEnum(Node enumNode) {
		String belongsTo = "";
		String dependsOn = "";
		StatementResult parent =  connector.executeRead(
				"MATCH (parent)-[:DECLARES]->(enum:Enum) WHERE ID(enum) = " + enumNode.id() + " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		StatementResult dependent =  connector.executeRead(
				"MATCH (enum:Enum)-[:DEPENDS_ON]->(type) WHERE ID(enum) = " + enumNode.id() + " RETURN type.hash");
		if(dependent.hasNext()){
			dependsOn = dependent.single().get("type.hash").asString();
		}

		return formatLine("id", enumNode.get("hash").asString()) +
				formatLine("qualifiedName", enumNode.get("fqn").asString()) +
				formatLine("name", enumNode.get("name").asString()) +
				formatLine("type", "Enum") +
				formatLine("belongsTo", belongsTo) +
				formatLine(dependsOn, dependsOn) +
				formatLine("filename", enumNode.get("fileName").asString());
	}

	private String toMetaDataEnumValue(Node enumValue) {
		String belongsTo = "";
		String dependsOn = "";
		StatementResult parent =  connector.executeRead(
				"MATCH (parent)-[:DECLARES]->(enumValue:EnumConstant) WHERE ID(enumValue) = " + enumValue.id() + " RETURN parent.hash");
		if(parent.hasNext()) {
			belongsTo = parent.single().get("parent.hash").asString();
		}
		StatementResult dependent =  connector.executeRead(
				"MATCH (enumValue:EnumConstant)-[:DEPENDS_ON]->(type) WHERE ID(enumValue) = " + enumValue.id() + " RETURN type.hash");
		if(dependent.hasNext()){
			dependsOn = dependent.single().get("type.hash").asString();
		}

		return formatLine("id", enumValue.get("hash").asString()) +
				formatLine("qualifiedName", enumValue.get("fqn").asString()) +
				formatLine("name", enumValue.get("name").asString()) +
				formatLine("type", "EnumValue") +
				formatLine("belongsTo", belongsTo) +
				formatLine(dependsOn, dependsOn) +
				formatLine("filename", enumValue.get("fileName").asString());
	}

	 private String getFunctionSignature(Node function){
		String signature = "";
		String returnType = "";
		StatementResult returnTypeNodes =  connector.executeRead(
				 "MATCH (function:Function)-[:RETURNS]->(node) WHERE ID(function) = " + function.id() + " RETURN node");
		if(returnTypeNodes.hasNext()) {
			returnType += returnTypeNodes.single().get("node").asNode().get("name").asString();
		}
		if(!returnType.endsWith("*")){
			returnType += " ";
		}
		String functionName = function.get("name").asString();
		List<Node> parameterList = new ArrayList<>();
		 StatementResult parameters =  connector.executeRead(
				 "MATCH (function:Function)-[:HAS]->(node) WHERE ID(function) = " + function.id() + " RETURN node ORDER BY node.index");
		 while (parameters.hasNext()) {
		 	parameterList.add(parameters.next().get("node").asNode());
		 }
		//var parameterList = function.getRelationships(Direction.OUTGOING, Rels.HAS).map[endNode]
		//sort parameters according to their index
		//parameterList = parameterList.sortBy[p|p.getProperty("index", 0) as Integer]
		String parameter = getFunctionParameters(parameterList);
		signature = returnType + functionName + "(" + parameter + ")";
		return signature;
	}

	private String getFunctionParameters(Iterable<Node> parameterList){
		String parameters = "";
		int counter = 0;
		for(Node parameter: parameterList){
			//add comma after parameter
			if(counter != 0){
				parameters += ", ";
			}
			String parameterTypeString = "";
			StatementResult parameterTypeList =  connector.executeRead(
					"MATCH (parameter:Parameter)-[:OF_TYPE]->(type:Type) WHERE ID(parameter) = " + parameter.id() + " RETURN type");
			while(parameterTypeList.hasNext()) {
				parameterTypeString += parameterTypeList.next().get("type").asNode().get("name").asString();
			}

			//put [] after the parameter name
			if(parameterTypeString.endsWith("]")){
				String[] parts = parameterTypeString.split("\\[", 2);
				log.info("string");
				log.info("parameterTypeString");
				log.info(parts[0]);
				parameters += parts[0] + parameter.get("name").asString() + "[" + parts[1];
			} else {
				parameters += parameterTypeString + " " + parameter.get("name").asString();
			}
			counter++;
		}

		return parameters;
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

	public String removeBrackets(List<String> list) {
		return removeBrackets(list.toString());
	}

	public String removeBrackets(String string) {
		return StringUtils.remove(StringUtils.remove(string, "["), "]");
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

	private String formatLine(String key, String value) {
		return "\"" + key +  "\":            \"" + value + "\",";
	}

	private String formatEndline(String key, String value) {
		return "\"" + key +  "\":            \"" + value + "\"";
	}

}