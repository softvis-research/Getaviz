package org.getaviz.generator.garbage.city.m2t;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.garbage.Step;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.garbage.Labels;
import org.getaviz.generator.garbage.output.AFrame;
import org.neo4j.driver.v1.types.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class 	City2AFrame implements Step {
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());
	private BuildingType buildingType;
	private String outputPath;
	private String buildingTypeAsString;
	private boolean showAttributesAsCylinders;
	private double panelSeparatorHeight;
	private String color;
	private boolean showBuildingBase;
	private AFrame outputFormat;
	private SettingsConfiguration.OutputFormat format;


	public City2AFrame(SettingsConfiguration config) {
		this.buildingType = config.getBuildingType();
		this.outputPath = config.getOutputPath();
		this.buildingTypeAsString = config.getBuildingTypeAsString();
		this.showAttributesAsCylinders = config.isShowAttributesAsCylinders();
		this.panelSeparatorHeight = config.getPanelSeparatorHeight();
		this.color = config.getCityColor("black");
		this.showBuildingBase = config.isShowBuildingBase();
		this.outputFormat = new AFrame();
		this.format = config.getOutputFormat();
	}

	public void run() {
		log.info("City2AFrame has started");
		FileWriter fw = null;
		String fileName = "model.html";

		try {
			fw = new FileWriter(outputPath + fileName);
			fw.write(outputFormat.head() + toAFrameModel() + outputFormat.tail());
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
		log.info("City2AFrame has finished");
	}

	@Override
	public boolean checkRequirements() {
		return format.equals(SettingsConfiguration.OutputFormat.AFrame);
	}

	private String toAFrameModel() {
		StringBuilder districts = new StringBuilder();
		StringBuilder buildings = new StringBuilder();
		StringBuilder segments = new StringBuilder();
		connector.executeRead(
				"MATCH (n:Model)-[:CONTAINS*]->(d:District)-[:HAS]->(p:Position) WHERE n.building_type = '"
						+ buildingTypeAsString + "' RETURN d,p")
				.forEachRemaining((record) -> districts.append(toDistrict(record.get("d").asNode(), record.get("p").asNode())));
		if (buildingType == BuildingType.CITY_ORIGINAL || showBuildingBase) {
			connector.executeRead(
					"MATCH (n:Model)-[:CONTAINS*]->(b:Building)-[:HAS]->(p:Position) WHERE n.building_type = '"
							+ buildingTypeAsString + "' RETURN b,p")
					.forEachRemaining((record) -> buildings.append(toBuilding(record.get("b").asNode(), record.get("p").asNode())));
		}

		if (!(buildingType == BuildingType.CITY_ORIGINAL)) {
			connector.executeRead(
					"MATCH (n:Model)-[:CONTAINS*]->(bs:BuildingSegment)-[:HAS]->(p:Position) WHERE n.building_type = '"
							+ buildingTypeAsString + "' RETURN bs,p")
					.forEachRemaining((record) -> {
						Node segment = record.get("bs").asNode();
						if (segment.hasLabel(Labels.Floor.name())) {
							segments.append(toFloor(segment, record.get("p").asNode()));
						} else if (segment.hasLabel(Labels.Chimney.name())) {
							segments.append(toChimney(segment, record.get("p").asNode()));
						} else {
							segments.append(toBuildingSegment(segment, record.get("p").asNode()));
						}
					});
		}
		return districts.toString() + buildings + segments;
	}

	private String toDistrict(Node district, Node position) {
		Node entity = connector.getVisualizedEntity(district.id());
		return "<a-box id=\"" + entity.get("hash").asString() + "\"" +
				"\n" +
				"\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"" +
				"\n" +
				"\t width=\"" + district.get("width") + "\"" +
				"\n" +
				"\t height=\"" + district.get("height") + "\"" +
				"\n" +
				"\t depth=\"" + district.get("length") + "\"" +
				"\n" +
				"\t color=\"" + district.get("color").asString() + "\">" +
				"\n" +
				"</a-box>" +
				"\n";
	}

	private String toBuilding(Node building, Node position) {
		Node entity = connector.getVisualizedEntity(building.id());
		return "<a-box id=\"" + entity.get("hash").asString() + "\"" +
				"\t\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"" +
				"\n" +
				"\t\t width=\"" + building.get("width") + "\"" +
				"\n" +
				"\t\t height=\"" + building.get("height") + "\"" +
				"\n" +
				"\t\t depth=\"" + building.get("length") + "\"" +
				"\n" +
				"\t\t color=\"" + building.get("color").asString() + "\">" +
				"\n" +
				"</a-box>" +
				"\n";
	}

	private String buildPosition(Node position) {
		return "\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"";
	}

	private String buildColor(Node segment) {
		return "\t color=\"" + segment.get("color").asString() + "\"";
	}

	private String toBuildingSegment(Node segment, Node position) {
		Node entity = connector.getVisualizedEntity(segment.id());
		List<Node> separators = new ArrayList<>();
		connector.executeRead("MATCH (n)-[:HAS]->(ps:PanelSeparator) RETURN ps").forEachRemaining((record) -> separators.add(record.get("ps").asNode()));
		double width = segment.get("width").asDouble();
		double height = segment.get("height").asDouble();
		double length = segment.get("length").asDouble();
		StringBuilder builder = new StringBuilder();
		if (buildingType == BuildingType.CITY_PANELS && entity.hasLabel(Labels.Field.name())
				&& showAttributesAsCylinders) {
			builder.append("<a-cylinder id=\"").append(entity.get("hash").asString()).append("\"");
			builder.append("\n");
			builder.append(buildPosition(position));
			builder.append("\n");
			builder.append("\t radius=\"").append(width / 2).append("\"");
			builder.append("\n");
			builder.append("\t height=\"" + "\" ");
			builder.append("\n");
			builder.append("\t color=\"").append(segment.get("color").asString()).append("\"");
			builder.append("\n");
			builder.append("\t segments-height=\"2\"");
			builder.append("\n");
			builder.append("\t segments-radial=\"20\">");
			builder.append("\n");
			builder.append("</a-cylinder>");
			builder.append("\n");
		} else {
			builder.append("<a-box id=\"").append(entity.get("hash").asString()).append("\"");
			builder.append("\n");
			builder.append(buildPosition(position));
			builder.append("\n");
			builder.append("\t width=\"").append(width).append("\"");
			builder.append("\n");
			builder.append("\t height=\"").append(height).append("\"");
			builder.append("\n");
			builder.append("\t depth=\"").append(length).append("\"");
			builder.append("\n");
			builder.append(buildColor(segment)).append(">");
			builder.append("\n");
			builder.append("</a-box>");
			builder.append("\n");
		}
		for (final Node separator : separators) {
			final Node pos = connector.getPosition(separator.id());
			builder.append("\n");
			if (separator.hasLabel(Labels.Cylinder.name())) {
				builder.append("<a-cylinder  id=\"").append(entity.get("hash").asString()).append("\"");
				builder.append("\n");
				builder.append(buildPosition(pos));
				builder.append("\n");
				builder.append("\t radius=\"").append(separator.get("radius")).append("\" ");
				builder.append("\n");
				builder.append("\t height=\"").append(panelSeparatorHeight).append("\" ");
				builder.append("\n");
				builder.append("\t color=\"").append(color).append("\"");
				builder.append("\n");
				builder.append("\t segments-height=\"2\"");
				builder.append("\n");
				builder.append("\t segments-radial=\"20\">");
				builder.append("\n");
				builder.append("</a-cylinder>");
				builder.append("\n");
			} else {
				builder.append("<a-box id=\"").append(entity.get("hash").asString()).append("\"");
				builder.append("\n");
				builder.append(buildPosition(pos));
				builder.append("\n");
				builder.append("\t width=\"").append(separator.get("width")).append("\"");
				builder.append("\n");
				builder.append("\t height=\"").append(panelSeparatorHeight).append("\"");
				builder.append("\n");
				builder.append("\t depth=\"").append(separator.get("length")).append("\"");
				builder.append("\n");
				builder.append("\t color=\"").append(color).append("\">");
				builder.append("\n");
				builder.append("</a-box>");
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	private String toFloor(Node floor, Node position) {
		Node entity = connector.getVisualizedEntity(floor.id());
		return "<a-box id=\"" + entity.get("hash").asString() + "\"" +
				"\n" +
				buildPosition(position) +
				"\n" +
				"\t width=\"" + floor.get("width") + "\"" +
				"\n" +
				"\t height=\"" + floor.get("height") + "\"" +
				"\n" +
				"\t depth=\"" + floor.get("length") + "\"" +
				"\n" +
				"\t color=\"" + floor.get("color").asString() + "\">" +
				"\n" +
				"</a-box>" +
				"\n";
	}

	private String toChimney(Node chimney, Node position) {
		Node entity = connector.getVisualizedEntity(chimney.id());
		return "<a-box id=\"" + entity.get("hash").asString() + "\"" +
				"\n" +
				buildPosition(position) +
				"\n" +
				"\t width=\"" + chimney.get("width") + "\"" +
				"\n" +
				"\t height=\"" + chimney.get("height") + "\"" +
				"\n" +
				"\t depth=\"" + chimney.get("length") + "\"" +
				"\n" +
				"\t color=\"" + chimney.get("color").asString() + "\">" +
				"\n" +
				"</a-box>" +
				"\n";
	}
}