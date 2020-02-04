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
			builder.append("<a-circle id=\"").append(entity.get("hash").asString()).append("\" ");
			builder.append("\n");
			builder.append("\t position=\"").append(position.get("x")).append(" ");
			builder.append(position.get("y")).append(" ");
			builder.append(position.get("z")).append("\"");
			builder.append("\n");
			builder.append("\t radius=\"").append(radius).append("\" ");
			builder.append("\n");
			builder.append("\t color=\"").append(disk.get("color").asString()).append("\"");
			builder.append("\n");
			builder.append("\t buffer=\"true\"");
			builder.append("\n");
			builder.append("\t depth-test=\"false\"");
			builder.append("\n");
			builder.append("\t depth-write=\"false\">");
			builder.append("\n");
			builder.append("\t").append(toSegment(segments));
			builder.append("\n");
			builder.append("</a-circle>");
			builder.append("\n");
		} else {
			builder.append("<a-ring id=\"").append(entity.get("hash").asString("NOHASH")).append("\"");
			builder.append("\n");
			builder.append("\t position=\"").append(position.get("x")).append(" ");
			builder.append(position.get("y")).append(" ");
			builder.append(position.get("z")).append("\"");
			builder.append("\n");
			builder.append("\t radius-inner=\"").append(radius - ringWidth).append("\"");
			builder.append("\n");
			builder.append("\t radius-outer=\"").append(radius).append("\" ");
			builder.append("\n");
			builder.append("\t color=\"").append(disk.get("color").asString()).append("\"");
			builder.append("\n");
			builder.append("\t buffer=\"true\"");
			builder.append("\n");
			builder.append("\t depth-test=\"false\"");
			builder.append("\n");
			builder.append("\t depth-write=\"false\"");
			builder.append("\n");
			builder.append("\t segments-phi=\"1\">");
			builder.append("\n");
			builder.append("\t").append(toSegment(segments));
			builder.append("\n");
			builder.append("</a-ring>");
			builder.append("\n");
		}
		return builder.toString();
	}

	private String toSegment(List<Node> segments) {
		StringBuilder builder = new StringBuilder();
		for (final Node segment : segments) {
			Node entity = connector.getVisualizedEntity(segment.id());
			if (segment.get("innerRadius").asDouble() == 0) {
				builder.append("<a-circle id=\"").append(entity.get("hash").asString("NOHAHS")).append("\"");
				builder.append("\n");
				builder.append("\t radius=\"").append(segment.get("outerRadius")).append("\" ");
				builder.append("\n");
				builder.append("\t color=\"").append(segment.get("color").asString()).append("\"");
				builder.append("\n");
				builder.append("\t theta-start=\"").append(segment.get("anglePosition")).append("\"");
				builder.append("\n");
				builder.append("\t theta-length=\"").append(segment.get("angle")).append("\"");
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
				builder.append("<a-ring id=\"").append(entity.get("hash").asString("NOHASH")).append("\"");
				builder.append("\n");
				builder.append("\t radius-inner=\"").append(segment.get("innerRadius")).append("\"");
				builder.append("\n");
				builder.append("\t radius-outer=\"").append(segment.get("outerRadius")).append("\" ");
				builder.append("\n");
				builder.append("\t color=\"").append(segment.get("color").asString()).append("\"");
				builder.append("\n");
				builder.append("\t buffer=\"true\"");
				builder.append("\n");
				builder.append("\t depth-test=\"false\"");
				builder.append("\n");
				builder.append("\t depth-write=\"false\"");
				builder.append("\n");
				builder.append("\t theta-start=\"").append(segment.get("anglePosition")).append("\"");
				builder.append("\n");
				builder.append("\t theta-length=\"").append(segment.get("angle")).append("\"");
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
