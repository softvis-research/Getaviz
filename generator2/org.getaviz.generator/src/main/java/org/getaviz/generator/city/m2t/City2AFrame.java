package org.getaviz.generator.city.m2t;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.xtend2.lib.StringConcatenation;
import java.io.IOException;
import java.io.FileWriter;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.types.Node;
import java.util.ArrayList;
import java.util.List;

import org.getaviz.generator.OutputFormatHelper;

public class City2AFrame {
	SettingsConfiguration config = SettingsConfiguration.getInstance();
	DatabaseConnector connector = DatabaseConnector.getInstance();
	Log log = LogFactory.getLog(this.getClass());

	public City2AFrame() {
		log.info("City2AFrame has started");
		FileWriter fw = null;
		String fileName = "model2.html";

		try {
			fw = new FileWriter(config.getOutputPath() + fileName);
			fw.write(OutputFormatHelper.AFrameHead() + toAFrameModel() + OutputFormatHelper.AFrameTail());
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

	private String toAFrameModel() {
		StringBuilder districts = new StringBuilder();
		StringBuilder buildings = new StringBuilder();
		StringBuilder segments = new StringBuilder();
		connector.executeRead(
				"MATCH (n:Model)-[:CONTAINS*]->(d:District)-[:HAS]->(p:Position) WHERE n.building_type = \'"
						+ config.getBuildingTypeAsString() + "\' RETURN d,p")
				.forEachRemaining((record) -> {
					districts.append(toDistrict(record.get("d").asNode(), record.get("p").asNode()));
				});
		if (config.getBuildingType() == BuildingType.CITY_ORIGINAL || config.isShowBuildingBase()) {
			connector.executeRead(
					"MATCH (n:Model)-[:CONTAINS*]->(b:Building)-[:HAS]->(p:Position) WHERE n.building_type = \'"
							+ config.getBuildingTypeAsString() + "\' RETURN b,p")
					.forEachRemaining((record) -> {
						buildings.append(toBuilding(record.get("b").asNode(), record.get("p").asNode()));
					});
		}

		if (!(config.getBuildingType() == BuildingType.CITY_ORIGINAL)) {
			connector.executeRead(
					"MATCH (n:Model)-[:CONTAINS*]->(bs:BuildingSegment)-[:HAS]->(p:Position) WHERE n.building_type = \'"
							+ config.getBuildingTypeAsString() + "\' RETURN bs,p")
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
		StringConcatenation builder = new StringConcatenation();
		builder.append("<a-box id=\"" + entity.get("hash").asString() + "\"");
		builder.newLine();
		builder.append("\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"");
		builder.newLine();
		builder.append("\t width=\"" + district.get("width") + "\"");
		builder.newLine();
		builder.append("\t height=\"" + district.get("height") + "\"");
		builder.newLine();
		builder.append("\t depth=\"" + district.get("length") + "\"");
		builder.newLine();
		builder.append("\t color=\"" + district.get("color").asString() + "\"");
		builder.newLine();
		builder.append("\t shader=\"flat\"");
		builder.newLine();
		builder.append("\t flat-shading=\"true\">");
		builder.newLine();
		builder.append("</a-box>");
		builder.newLine();
		return builder.toString();
	}

	private String toBuilding(Node building, Node position) {
		Node entity = connector.getVisualizedEntity(building.id());
		StringConcatenation _builder = new StringConcatenation();
		_builder.append("<a-box id=\"" + entity.get("hash").asString() + "\"");
		_builder.newLine();
		_builder.append(
				"\t\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"");
		_builder.newLine();
		_builder.append("\t\t width=\"" + building.get("width") + "\"");
		_builder.newLine();
		_builder.append("\t\t height=\"" + building.get("height") + "\"");
		_builder.newLine();
		_builder.append("\t\t depth=\"" + building.get("length") + "\"");
		_builder.newLine();
		_builder.append("\t\t color=\"" + building.get("color").asString() + "\"");
		_builder.newLine();
		_builder.append("\t\t shader=\"flat\"");
		_builder.newLine();
		_builder.append("\t\t flat-shading=\"true\">");
		_builder.newLine();
		_builder.append("</a-box>");
		_builder.newLine();
		return _builder.toString();
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
		connector.executeRead("MATCH (n)-[:HAS]->(ps:PanelSeparator) RETURN ps").forEachRemaining((record) -> {
			separators.add(record.get("ps").asNode());
		});
		double width = segment.get("width").asDouble();
		double height = segment.get("height").asDouble();
		double length = segment.get("length").asDouble();
		StringConcatenation builder = new StringConcatenation();
		if (config.getBuildingType() == BuildingType.CITY_PANELS && entity.hasLabel(Labels.Field.name())
				&& config.isShowAttributesAsCylinders()) {
			builder.append("<a-cylinder id=\"" + entity.get("hash").asString() + "\"");
			builder.newLine();
			builder.append(buildPosition(position));
			builder.newLine();
			builder.append("\t radius=\"" + width / 2 + "\"");
			builder.newLine();
			builder.append("\t height=\"" + "\" ");
			builder.newLine();
			builder.append("\t color=\"" + segment.get("color").asString() + "\"");
			builder.newLineIfNotEmpty();
			builder.append("\t shader=\"flat\"");
			builder.newLine();
			builder.append("\t flat-shading=\"true\"");
			builder.newLine();
			builder.append("\t segments-height=\"2\"");
			builder.newLine();
			builder.append("\t segments-radial=\"20\">");
			builder.newLine();
			builder.append("</a-cylinder>");
			builder.newLine();
		} else {
			builder.append("<a-box id=\"" + entity.get("hash").asString() + "\"");
			builder.newLine();
			builder.append(buildPosition(position));
			builder.newLine();
			builder.append("\t width=\"" + width + "\"");
			builder.newLine();
			builder.append("\t height=\"" + height + "\"");
			builder.newLine();
			builder.append("\t depth=\"" + length + "\"");
			builder.newLine();
			builder.append(buildColor(segment));
			builder.newLine();
			builder.append("\t shader=\"flat\"");
			builder.newLine();
			builder.append("\t flat-shading=\"true\">");
			builder.newLine();
			builder.append("</a-box>");
			builder.newLine();
		}
		for (final Node separator : separators) {
			final Node pos = connector.getPosition(separator.id());
			builder.newLineIfNotEmpty();
			if (separator.hasLabel(Labels.Cylinder.name())) {
				builder.append("<a-cylinder  id=\"" + entity.get("hash").asString() + "\"");
				builder.newLine();
				builder.append(buildPosition(pos));
				builder.newLine();
				builder.append("\t radius=\"" + separator.get("radius") + "\" ");
				builder.newLine();
				builder.append("\t height=\"" + config.getPanelSeparatorHeight() + "\" ");
				builder.newLine();
				builder.append("\t color=\"" + config.getCityColorHex("black") + "\"");
				builder.newLine();
				builder.append("\t shader=\"flat\"");
				builder.newLine();
				builder.append("\t flat-shading=\"true\"");
				builder.newLine();
				builder.append("\t segments-height=\"2\"");
				builder.newLine();
				builder.append("\t segments-radial=\"20\">");
				builder.newLine();
				builder.append("</a-cylinder>");
				builder.newLine();
			} else {
				builder.append("<a-box id=\"" + entity.get("hash").asString() + "\"");
				builder.newLineIfNotEmpty();
				builder.append(buildPosition(pos));
				builder.newLine();
				builder.append("\t width=\"" + separator.get("width") + "\"");
				builder.newLine();
				builder.append("\t height=\"" + config.getPanelSeparatorHeight() + "\"");
				builder.newLine();
				builder.append("\t depth=\"" + separator.get("length") + "\"");
				builder.newLine();
				builder.append("\t color=\"" + config.getCityColorHex("black") + "\"");
				builder.newLine();
				builder.append("\t shader=\"flat\"");
				builder.newLine();
				builder.append("\t flat-shading=\"true\">");
				builder.newLine();
				builder.append("</a-box>");
				builder.newLine();
			}
		}
		return builder.toString();
	}

	private String toFloor(Node floor, Node position) {
		Node entity = connector.getVisualizedEntity(floor.id());
		StringConcatenation builder = new StringConcatenation();
		builder.append("<a-box id=\"" + entity.get("hash").asString() + "\"");
		builder.newLine();
		builder.append(buildPosition(position));
		builder.newLine();
		builder.append("\t width=\"" + floor.get("width") + "\"");
		builder.newLine();
		builder.append("\t height=\"" + floor.get("height") + "\"");
		builder.newLine();
		builder.append("\t depth=\"" + floor.get("length") + "\"");
		builder.newLine();
		builder.append("\t color=\"" + floor.get("color").asString() + "\"");
		builder.newLine();
		builder.append("\t shader=\"flat\"");
		builder.newLine();
		builder.append("\t flat-shading=\"true\">");
		builder.newLine();
		builder.append("</a-box>");
		builder.newLine();
		return builder.toString();
	}

	private String toChimney(Node chimney, Node position) {
		Node entity = connector.getVisualizedEntity(chimney.id());		
		StringConcatenation builder = new StringConcatenation();
	    builder.append("<a-box id=\"" + entity.get("hash").asString() + "\"");
	    builder.newLine();
	    builder.append(buildPosition(position));
	    builder.newLine();
	    builder.append("\t width=\"" + chimney.get("width") + "\"");
	    builder.newLine();
	    builder.append("\t height=\"" + chimney.get("height") + "\"");
	    builder.newLine();
	    builder.append("\t depth=\"" + chimney.get("length") + "\"");
	    builder.newLine();
	    builder.append("\t color=\"" + chimney.get("color").asString() + "\"");
	    builder.newLine();
	    builder.append("\t shader=\"flat\"");
	    builder.newLine();
	    builder.append("\t flat-shading=\"true\">");
	    builder.newLine();
	    builder.append("</a-box>");
	    builder.newLine();
	    return builder.toString();
	}
}