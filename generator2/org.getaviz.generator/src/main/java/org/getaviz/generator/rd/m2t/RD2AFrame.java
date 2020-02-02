package org.getaviz.generator.rd.m2t;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.Step;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.output.AFrame;
import org.neo4j.driver.v1.types.Node;
import java.util.ArrayList;
import java.util.List;

public class RD2AFrame implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private double ringWidth;
	private String outputPath;
	private AFrame outputFormat;
	private SettingsConfiguration.OutputFormat format;

	public RD2AFrame(SettingsConfiguration config) {
		this.ringWidth = config.getRDRingWidth();
		this.outputPath = config.getOutputPath();
		this.outputFormat = new AFrame();
		this.format = config.getOutputFormat();
	}

	@Override
	public boolean checkRequirements() {
		return format.equals(SettingsConfiguration.OutputFormat.AFrame);
	}

	public void run() {
		log.info("RD2AFrame has started");
		FileWriter fw = null;
		String fileName = "model.html";
		try {
			fw = new FileWriter(outputPath+ fileName);
			fw.write(outputFormat.head() + body() + outputFormat.tail());
		} catch (IOException e) {
			log.error("Could not create file");
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		log.info("RD2AFrame has finished");
	}

	private String body() {
		StringBuilder elements = new StringBuilder();
		try {
			connector.executeRead(
					"MATCH (element)<-[:VISUALIZES]-(d:MainDisk)-[:HAS]->(p:Position) RETURN d,p, element.hash ORDER BY element.hash")
					.forEachRemaining((result) -> {
						elements.append(toDisk(result.get("d").asNode(), result.get("p").asNode()));
					});
		} catch (Exception e) {
			log.error(e);
		}
		connector.executeRead(
				"MATCH (element)<-[:VISUALIZES]-(d:SubDisk)-[:HAS]->(p:Position) RETURN d,p, element.hash ORDER BY element.hash")
				.forEachRemaining((result) -> {
					elements.append(toDisk(result.get("d").asNode(), result.get("p").asNode()));
				});
		connector.executeRead(
				"MATCH (element)<-[:VISUALIZES]-(d:SubDisk)-[:HAS]->(p:Position) RETURN d,p, element.hash ORDER BY element.hash")
				.forEachRemaining((result) -> {
					elements.append(toDisk(result.get("d").asNode(), result.get("p").asNode()));
				});
		return elements.toString();
	}

	private String toDisk(Node disk, Node position) {
		double radius = disk.get("radius").asDouble(0);
		Node entity = connector.getVisualizedEntity(disk.id());
		ArrayList<Node> segments = new ArrayList<>();
		connector.executeRead("MATCH (n)-[:CONTAINS]->(ds:DiskSegment)-[:VISUALIZES]->(element) WHERE ID(n) = "
				+ disk.id() + " RETURN ds, element.hash ORDER BY element.hash").forEachRemaining((result) -> {
					segments.add(result.get("ds").asNode());
				});
		StringBuilder builder = new StringBuilder();
		if (radius - ringWidth == 0) {
			builder.append("<a-circle id=\"" + entity.get("hash").asString() + "\" ");
			builder.append("\n");
			builder.append("\t position=\"" + position.get("x") + " ");
			builder.append(position.get("y") + " ");
			builder.append(position.get("z") + "\"");
			builder.append("\n");
			builder.append("\t radius=\"" + radius + "\" ");
			builder.append("\n");
			builder.append("\t color=\"" + disk.get("color").asString() + "\"");
			builder.append("\n");
			builder.append("\t buffer=\"true\"");
			builder.append("\n");
			builder.append("\t depth-test=\"false\"");
			builder.append("\n");
			builder.append("\t depth-write=\"false\">");
			builder.append("\n");
			builder.append("\t" + toSegment(segments));
			builder.append("\n");
			builder.append("</a-circle>");
			builder.append("\n");
		} else {
			builder.append("<a-ring id=\"" + entity.get("hash").asString("NOHASH") + "\"");
			builder.append("\n");
			builder.append("\t position=\"" + position.get("x") + " ");
			builder.append(position.get("y") + " ");
			builder.append(position.get("z") + "\"");
			builder.append("\n");
			builder.append("\t radius-inner=\"" + (radius - ringWidth) + "\"");
			builder.append("\n");
			builder.append("\t radius-outer=\"" + radius + "\" ");
			builder.append("\n");
			builder.append("\t color=\"" + disk.get("color").asString() + "\"");
			builder.append("\n");
			builder.append("\t buffer=\"true\"");
			builder.append("\n");
			builder.append("\t depth-test=\"false\"");
			builder.append("\n");
			builder.append("\t depth-write=\"false\"");
			builder.append("\n");
			builder.append("\t segments-phi=\"1\">");
			builder.append("\n");
			builder.append("\t" + toSegment(segments));
			builder.append("\n");
			builder.append("</a-ring>");
			builder.append("\n");
		}
		String properties = builder.toString();
		return properties;
	}

	private String toSegment(List<Node> segments) {
		StringBuilder builder = new StringBuilder();
		for (final Node segment : segments) {
			Node entity = connector.getVisualizedEntity(segment.id());
			if (segment.get("innerRadius").asDouble() == 0) {
				builder.append("<a-circle id=\"" + entity.get("hash").asString("NOHAHS") + "\"");
				builder.append("\n");
				builder.append("\t radius=\"" + segment.get("outerRadius") + "\" ");
				builder.append("\n");
				builder.append("\t color=\"" + segment.get("color").asString() + "\"");
				builder.append("\n");
				builder.append("\t theta-start=\"" + segment.get("anglePosition") + "\"");
				builder.append("\n");
				builder.append("\t theta-length=\"" + segment.get("angle") + "\"");
				builder.append("\n");
				builder.append("\t buffer=\"true\"");
				builder.append("\n");
				builder.append("\t depth-test=\"false\"");
				builder.append("\n");
				builder.append("\t depth-write=\"false\">");
				builder.append("\n");
				builder.append("</a-circle>");
				builder.append("\n");
			} else {
				builder.append("<a-ring id=\"" + entity.get("hash").asString("NOHASH") + "\"");
				builder.append("\n");
				builder.append("\t radius-inner=\"" + segment.get("innerRadius") + "\"");
				builder.append("\n");
				builder.append("\t radius-outer=\"" + segment.get("outerRadius") + "\" ");
				builder.append("\n");
				builder.append("\t color=\"" + segment.get("color").asString() + "\"");
				builder.append("\n");
				builder.append("\t buffer=\"true\"");
				builder.append("\n");
				builder.append("\t depth-test=\"false\"");
				builder.append("\n");
				builder.append("\t depth-write=\"false\"");
				builder.append("\n");
				builder.append("\t theta-start=\"" + segment.get("anglePosition") + "\"");
				builder.append("\n");
				builder.append("\t theta-length=\"" + segment.get("angle") + "\"");
				builder.append("\n");
				builder.append("\t segments-phi=\"1\">");
				builder.append("\n");
				builder.append("</a-ring>");
				builder.append("\n");
			}
		}
		return builder.toString();
	}
}
