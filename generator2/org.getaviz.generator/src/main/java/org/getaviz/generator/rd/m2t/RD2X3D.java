package org.getaviz.generator.rd.m2t;

import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.Step;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.output.X3D;
import org.neo4j.driver.v1.types.Node;

public class RD2X3D implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private String outputPath;
	private X3D outputFormat;

	public RD2X3D(SettingsConfiguration config) {
		this.outputPath = config.getOutputPath();
		this.outputFormat = new X3D(config);
	}

	public void run() {
		log.info("RD2X3D has started");
		FileWriter fw = null;
		String fileName = "model.x3d";
		try {
			fw = new FileWriter(outputPath + fileName);
			fw.write(outputFormat.head() + toRD() + outputFormat.tail());
		} catch (IOException e) {
			log.error("Could not create file " + fileName);
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		log.info("RD2X3D has finished");
	}


	private String toRD() {
		StringBuilder disks = new StringBuilder();
		StringBuilder segments = new StringBuilder();
		connector.executeRead(
				"MATCH (n:Model:RD)-[:CONTAINS*]->(d:Disk)-[:HAS]->(p:Position),(d)-[:VISUALIZES]-(element) RETURN d,p ORDER BY element.hash")
				.forEachRemaining((result) -> {
					disks.append(toDisk(result.get("d").asNode(), result.get("p").asNode()));
				});

		connector.executeRead(
				"MATCH (n:Model:RD)-[:CONTAINS*]->(ds:DiskSegment)-[:VISUALIZES]->(element) RETURN ds ORDER BY element.hash")
				.forEachRemaining((result) -> {
					segments.append(toSegment(result.get("ds").asNode()));
				});
		return disks.toString() + segments.toString();
	}

	private String toDisk(Node disk, Node position) {
		Node entity = connector.getVisualizedEntity(disk.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<Transform translation=\'" + position.get("x") + " " + position.get("y") + " "
				+ position.get("z") + "\' ");
		builder.append("\n");
		builder.append("\t rotation=\'0 0 1 1.57\'");
		builder.append("\n");
		builder.append("\t scale=\'1 1 " + disk.get("height") + "\'>");
		builder.append("\n");
		builder.append("\t <Transform DEF=\'" + entity.get("hash").asString() + "\'>");
		builder.append("\n");
		builder.append("\t\t <Shape>");
		builder.append("\n");
		builder.append("\t\t\t <Extrusion");
		builder.append("\n");
		builder.append("\t\t\t\t convex=\'true\'");
		builder.append("\n");
		builder.append("\t\t\t\t solid=\'true\'");
		builder.append("\n");
		builder.append("\t\t\t\t crossSection=\'" + disk.get("crossSection").asString() + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t spine=\'" + disk.get("spine").asString() + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t creaseAngle=\'1\'");
		builder.append("\n");
		builder.append("\t\t\t\t beginCap=\'true\'");
		builder.append("\n");
		builder.append("\t\t\t\t endCap=\'true\'></Extrusion>");
		builder.append("\n");
		builder.append("\t\t\t <Appearance>");
		builder.append("\n");
		builder.append("\t\t\t\t\t <Material");
		builder.append("\n");
		builder.append("\t\t\t\t\t\t diffuseColor=\'" + outputFormat.printColor(disk.get("color").asString()) + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t\t\t transparency=\'" + disk.get("transparency") + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t\t ></Material>");
		builder.append("\n");
		builder.append("\t\t\t </Appearance>");
		builder.append("\n");
		builder.append("\t\t </Shape>");
		builder.append("\n");
		builder.append("\t </Transform>");
		builder.append("\n");
		builder.append("</Transform>");
		builder.append("\n");
		String properties = builder.toString();
		connector.executeWrite("MATCH disk WHERE ID(disk) = " + disk.id() + " SET disk.RD2X3D = " + properties);
		return builder.toString();
	}

	private String toSegment(Node segment) {
		Node position = connector.executeRead(
				"MATCH (n)<-[:CONTAINS]-(parent)-[:HAS]->(p:Position) WHERE ID(n) = " + segment.id() + " RETURN p")
				.single().get("p").asNode();
		Node entity = connector.getVisualizedEntity(segment.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<Transform  translation=\'" + position.get("x") + " " + position.get("y") + " "
				+ position.get("z") + "\' rotation=\'0 0 1 1.57\'>");
		builder.append("\n");
		builder.append("\t <Transform DEF=\'" + entity.get("hash").asString() + "\'>");
		builder.append("\n");
		builder.append("\t\t <Shape>");
		builder.append("\n");
		builder.append("\t\t\t <Extrusion");
		builder.append("\n");
		builder.append("\t\t\t\t convex=\'true\'");
		builder.append("\n");
		builder.append("\t\t\t\t solid=\'true\'");
		builder.append("\n");
		builder.append("\t\t\t\t crossSection=\'" + segment.get("crossSection").asString() + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t spine=\'" + segment.get("spine").asString() + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t creaseAngle=\'1\'");
		builder.append("\n");
		builder.append("\t\t\t\t beginCap=\'true\'");
		builder.append("\n");
		builder.append("\t\t\t\t endCap=\'true\'></Extrusion>");
		builder.append("\n");
		builder.append("\t\t\t <Appearance>");
		builder.append("\n");
		builder.append("\t\t\t\t\t <Material");
		builder.append("\n");
		builder.append("\t\t\t\t\t\t diffuseColor=\'" + outputFormat.printColor(segment.get("color").asString()) + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t\t\t transparency=\'" + segment.get("transparency") + "\'");
		builder.append("\n");
		builder.append("\t\t\t\t\t ></Material>");
		builder.append("\n");
		builder.append("\t\t\t </Appearance>");
		builder.append("\n");
		builder.append("\t\t </Shape>");
		builder.append("\n");
		builder.append("\t </Transform>");
		builder.append("\n");
		builder.append("</Transform>");
		builder.append("\n");
		return builder.toString();
	}
}