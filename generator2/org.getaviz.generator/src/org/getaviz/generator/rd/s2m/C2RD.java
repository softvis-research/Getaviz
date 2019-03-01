package org.getaviz.generator.rd.s2m;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;
import org.getaviz.lib.database.Database;
import org.getaviz.lib.database.Labels;
import org.getaviz.lib.database.Rels;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class C2RD {

	private final SettingsConfiguration config = SettingsConfiguration.getInstance();
	private final GraphDatabaseService graph = Database.getInstance(config.getDatabaseName());
	private final Log log = LogFactory.getLog(C2RD.class);
	
	public C2RD() {
		log.info("C2RD started");
		Transaction tx = graph.beginTx();
		try {
			Result result = graph.execute("MATCH (n:TranslationUnit) RETURN n");
			Node root = graph.createNode(Labels.Model, Labels.RD);
			root.setProperty("date", new GregorianCalendar().getTime().toString());
			final Node configuration = graph.createNode(Labels.RD, Labels.Configuration);
			configuration.setProperty("method_type_mode", config.isMethodTypeMode());
			configuration.setProperty("method_disks", config.isMethodDisks());
			configuration.setProperty("data_disks", config.isDataDisks());
			root.createRelationshipTo(configuration, Rels.USED);
			List<Node> translationUnits = new ArrayList<>();
			while(result.hasNext()) {
				translationUnits.add((Node)result.next().get("n"));
			}
			translationUnits.forEach (unit -> 
				root.createRelationshipTo(translationUnitToDisk(unit), Rels.CONTAINS)
			);
			tx.success();
		} finally {
			tx.close();
		}
		log.info("C2RD finished");
	}
	
	private Node translationUnitToDisk(Node translationUnit) {
		final Node disk = graph.createNode(Labels.RD, Labels.Disk);
		disk.createRelationshipTo(translationUnit, Rels.VISUALIZES);
		disk.setProperty("ringWidth", config.getRDRingWidth());
		disk.setProperty("height", config.getRDHeight());
		disk.setProperty("transparency", config.getRDNamespaceTransparency());
		
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			disk.setProperty("color", config.getRDNamespaceColorHex());
		} else {
			disk.setProperty("color", config.getRDNamespaceColorAsPercentage());
		}
		List<Node> subElements = new ArrayList<>();
		for(Relationship relationship : translationUnit.getRelationships(Rels.DECLARES, Direction.OUTGOING)) {
			subElements.add(relationship.getEndNode());
		}
		List<Node> functions = new ArrayList<>();
		List<Node> variables = new ArrayList<>();
		List<Node> structs = new ArrayList<>();
		
		for(Node element : subElements) {
			if(element.hasLabel(Labels.Function)) {
				functions.add(element);
			}
			if(element.hasLabel(Labels.Variable)) {
				variables.add(element);
			}
			if(element.hasLabel(Labels.Struct)) {
				structs.add(element);
			}
		}

		if (config.isDataDisks()) {
			variables.forEach(variable -> disk.createRelationshipTo(variableToDisk(variable), Rels.CONTAINS));
		} else {
			variables.forEach(variable -> disk.createRelationshipTo(variableToDiskSegment(variable), Rels.CONTAINS));
		}
		if (config.isMethodDisks()) {
			functions.forEach(function -> disk.createRelationshipTo(functionToDisk(function), Rels.CONTAINS));
		} else {
			functions.forEach(function -> disk.createRelationshipTo(functionToDiskSegment(function), Rels.CONTAINS));
		}
		structs.forEach(struct -> disk.createRelationshipTo(structToDisk(struct), Rels.CONTAINS));
		
		return disk;
	}

	private Node functionToDisk(Node function) {
		final Node disk = graph.createNode(Labels.RD, Labels.Disk);
		disk.createRelationshipTo(function, Rels.VISUALIZES);
		disk.setProperty("ringWidth", config.getRDRingWidthMD());
		disk.setProperty("height", config.getRDHeight());
		disk.setProperty("transparency", config.getRDMethodTransparency());
		String color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0;
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getRDMethodColorHex();
		}
		disk.setProperty("color", color);
		return disk;
	}

	private Node functionToDiskSegment(Node function) {
		Node diskSegment = graph.createNode(Labels.RD, Labels.DiskSegment);
		diskSegment.createRelationshipTo(function, Rels.VISUALIZES);
		Double frequency = 0.0;
		Double luminance = 0.0;
		double height = config.getRDHeight();
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			diskSegment.setProperty("color", config.getRDMethodColorHex());
		} else {
			diskSegment.setProperty("color", config.getRDMethodColorAsPercentage());
		}
		diskSegment.setProperty("transparency", config.getRDMethodTransparency());
		diskSegment.setProperty("frequency", frequency);
		diskSegment.setProperty("luminance", luminance);
		diskSegment.setProperty("height", height);
		Integer numberOfStatements = 0;
		if (function.hasProperty("effectiveLineCount")) {
			numberOfStatements = (Integer)function.getProperty("effectiveLineCount");
		}
		if (numberOfStatements <= config.getRDMinArea()) {
			diskSegment.setProperty("size", config.getRDMinArea());
		} else {
			diskSegment.setProperty("size", numberOfStatements.doubleValue());
		}
		return diskSegment;
	}

	private Node variableToDisk(Node variable) {
		final Node disk = graph.createNode(Labels.RD, Labels.Disk);
		disk.createRelationshipTo(variable, Rels.VISUALIZES);
		disk.setProperty("ringWidth", config.getRDRingWidthAD());
		disk.setProperty("height", config.getRDHeight());
		disk.setProperty("transparency", config.getRDDataTransparency());
		String color = 153 / 255.0 + " " + 0 / 255.0 + " " + 0 / 255.0;
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getRDDataColorHex();
		}
		disk.setProperty("color", color);
		return disk;
	}

	private Node variableToDiskSegment(Node variable) {
		final Node diskSegment = graph.createNode(Labels.RD, Labels.DiskSegment);
		diskSegment.createRelationshipTo(variable, Rels.VISUALIZES);
		diskSegment.setProperty("size", 1.0);
		diskSegment.setProperty("height", config.getRDHeight());
		String color = config.getRDDataColorAsPercentage();
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getRDDataColorHex();
		}
		diskSegment.setProperty("color", color);
		diskSegment.setProperty("transparency", config.getRDDataTransparency());
		return diskSegment;
	}
	
	private Node structToDisk(Node struct) {
		final Node disk = graph.createNode(Labels.RD, Labels.Disk);
		disk.createRelationshipTo(struct, Rels.VISUALIZES);
		disk.setProperty("ringWidth", config.getRDRingWidth());
		disk.setProperty("height", config.getRDHeight());
		disk.setProperty("transparency", config.getRDClassTransparency());
		String color = config.getRDClassColorAsPercentage();
		if (config.getOutputFormat() == OutputFormat.AFrame) {
			color = config.getRDClassColorHex();
		}
		disk.setProperty("color", color);
		
		List<Node> variables = new ArrayList<>();
		for(Relationship relationship : struct.getRelationships(Rels.DECLARES, Direction.OUTGOING)) {
			variables.add(relationship.getEndNode());
		}
		
		if (config.isDataDisks()) {
			variables.forEach(variable -> disk.createRelationshipTo(variableToDisk(variable), Rels.CONTAINS));
		} else {
			variables.forEach(variable -> disk.createRelationshipTo(variableToDiskSegment(variable), Rels.CONTAINS));
		}
		
		return disk;
	}
}
