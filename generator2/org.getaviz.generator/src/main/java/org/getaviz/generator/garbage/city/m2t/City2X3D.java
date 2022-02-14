package org.getaviz.generator.garbage.city.m2t;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.garbage.Step;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.garbage.Labels;
import org.getaviz.generator.garbage.output.X3D;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class City2X3D implements Step {
	private BuildingType buildingType;
	private String outputPath;
	private String buildingTypeAsString;
	private boolean showAttributesAsCylinders;
	private double panelSeparatorHeight;
	private String colorAsPercentage;
	private Log log = LogFactory.getLog(this.getClass());
	private X3D outputFormat;
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private SettingsConfiguration.OutputFormat format;


	public City2X3D(SettingsConfiguration config) {
		this.buildingType = config.getBuildingType();
		this.outputPath = config.getOutputPath();
		this.buildingTypeAsString = config.getBuildingTypeAsString();
		this.showAttributesAsCylinders = config.isShowAttributesAsCylinders();
		this.panelSeparatorHeight = config.getPanelSeparatorHeight();
		this.colorAsPercentage = config.getCityColor("black");
		this.outputFormat = new X3D(config);
		this.format = config.getOutputFormat();

	}

	public void run() {
		log.info("CityOutput started");
		FileWriter fw = null;
		String head = outputFormat.head();
		String body = outputFormat.viewports() + toX3DModel();
		String tail = outputFormat.tail();
		String fileName = "model.x3d";
		try {
			fw = new FileWriter(outputPath + fileName);
			switch (buildingType) {
				case CITY_ORIGINAL:
					fw.write(head + body + tail);
					break;
				case CITY_PANELS:
				case CITY_FLOOR:
				case CITY_BRICKS:
					fw.write(head + outputFormat.settingsInfo() + body + tail);
			}
		} catch (IOException e) {
			log.error(e);
			log.error("Could not create file");
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		log.info("CityOutput finished");
	}

	@Override
	public boolean checkRequirements() {
		return format.equals(SettingsConfiguration.OutputFormat.X3D);
	}

	private String toX3DModel() {
		StringBuilder districts = new StringBuilder();
		StringBuilder buildings = new StringBuilder();
		StringBuilder segments = new StringBuilder();
		connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(d:District)-[:VISUALIZES]->(e) WHERE n.building_type = '"
				+ buildingTypeAsString + "' RETURN d,e").forEachRemaining((result) -> {
					districts.append(toDistrict(result.get("d").asNode(), result.get("e").asNode()));
				});
		connector.executeRead("MATCH (n:Model)-[:CONTAINS*]->(b:Building)-[:VISUALIZES]->(e) WHERE n.building_type = '"
				+  buildingTypeAsString + "' RETURN b,e").forEachRemaining((result) -> {
					buildings.append(toBuilding(result.get("b").asNode(), result.get("e").asNode()));
				});
		if (!(buildingType == BuildingType.CITY_ORIGINAL)) {
			connector.executeRead(
					"MATCH (n:Model)-[:CONTAINS*]->(bs:BuildingSegment)-[:VISUALIZES]->(e) WHERE n.building_type = '"
							+ buildingTypeAsString + "' RETURN bs, e")
					.forEachRemaining((result) -> {
						Node segment = result.get("bs").asNode();
						if (segment.hasLabel(Labels.Floor.name())) {
							segments.append(toFloor(segment, result.get("e").asNode()));
						} else if (segment.hasLabel(Labels.Chimney.name())) {
							segments.append(toChimney(segment, result.get("e").asNode()));
						} else {
							segments.append(toBuildingSegment(segment, result.get("e").asNode()));
						}
					});
		}
		return districts.toString() + buildings + segments;
	}

	private String toDistrict(Node district, Node entity) {
		Node position = connector.getPosition(district.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<Group DEF='" + entity.get("hash").asString() + "'>");
		builder.append("\n");
		builder.append("\t <Transform translation='" + position.get("x").asDouble() + " "
				+ position.get("y").asDouble() + " " + position.get("z").asDouble() + "'>");
		builder.append("\n");
		builder.append("\t\t <Shape>");
		builder.append("\n");
		builder.append("\t\t\t <Box size='" + district.get("width").asDouble() + " "
				+ district.get("height").asDouble() + " " + district.get("length").asDouble() + "'></Box>");
		builder.append("\n");
		builder.append("\t\t\t <Appearance>");
		builder.append("\n");
		builder.append("\t\t\t\t <Material diffuseColor='" + district.get("color").asString() + "'></Material>");
		builder.append("\n");
		builder.append("\t\t\t </Appearance>");
		builder.append("\n");
		builder.append("\t\t </Shape>");
		builder.append("\n");
		builder.append("\t </Transform>");
		builder.append("\n");
		builder.append("</Group>\t\t");
		builder.append("\n");
		return builder.toString();
	}

	private String toBuilding(Node building, Node entity) {
		Node position = connector.getPosition(building.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<Group DEF='" + entity.get("hash").asString() + "'>");
		builder.append("\n");
		builder.append("\t <Transform translation='" + position.get("x").asDouble() + " "
				+ position.get("y").asDouble() + " " + position.get("z").asDouble() + "'>");
		builder.append("\n");
		builder.append("\t\t <Shape>");
		builder.append("\n");
		builder.append("\t\t\t <Box size='" + building.get("width").asDouble() + " " + building.get("height").asDouble()
				+ " " + building.get("length").asDouble() + "'></Box>");
		builder.append("\n");
		builder.append("\t\t\t <Appearance>");
		builder.append("\n");
		builder.append("\t\t\t\t <Material diffuseColor='" + building.get("color").asString() + "'></Material>");
		builder.append("\n");
		builder.append("\t\t\t </Appearance>");
		builder.append("\n");
		builder.append("\t\t </Shape>");
		builder.append("\n");
		builder.append("\t </Transform>");
		builder.append("\n");
		builder.append("</Group>\t\t");
		builder.append("\n");
		return builder.toString();
	}

	private String toBuildingSegment(Node segment, Node entity) {
		Node position = connector.getPosition(segment.id());
		List<Record> separators = new ArrayList<>();
		connector.executeRead("MATCH (n)-[:HAS]->(ps:PanelSeparator)-[:HAS]->(p:Position) WHERE ID(n) = " + segment.id()
				+ " RETURN ps,p").forEachRemaining(separators::add);
		double x = position.get("x").asDouble();
		double y = position.get("y").asDouble();
		double z = position.get("z").asDouble();
		double width = segment.get("width").asDouble();
		double height = segment.get("height").asDouble();
		double length = segment.get("length").asDouble();
		StringBuilder builder = new StringBuilder();
		builder.append("<Group DEF='" + entity.get("hash").asString() + "'>");
		builder.append("\n");
		builder.append("\t");
		builder.append("<Transform translation='" + x + " " + y + " " + z + "'>");
		builder.append("\n");
		builder.append("\t\t <Shape>");
		builder.append("\n");
		if (buildingType == BuildingType.CITY_PANELS && entity.hasLabel(Labels.Field.name())
				&& showAttributesAsCylinders) {
			builder.append("\t\t <Cylinder radius='" + width / 2);
			builder.append("' height='" + height + "'></Cylinder>");
			builder.append("\n");
		} else {
			builder.append("\t\t <Box size='" + width + " " + height + " " + length + "'></Box>");
			builder.append("\n");
		}
		builder.append("\t\t\t <Appearance>");
		builder.append("\n");
		builder.append("\t\t\t\t <Material diffuseColor='" + segment.get("color").asString() + "'></Material>");
		builder.append("\n");
		builder.append("\t\t\t </Appearance>");
		builder.append("\n");
		builder.append("\t\t </Shape>");
		builder.append("\n");
		builder.append("\t </Transform>");
		builder.append("\n");
		for (final Record record : separators) {
			builder.append("\t");
			final Node separator = record.get("ps").asNode();
			builder.append("\n");
			builder.append("\t");
			final Node pos = record.get("p").asNode();
			builder.append("\n");
			builder.append("\t <Transform translation='" + pos.get("x").asDouble() + " " + pos.get("y").asDouble()
					+ " " + pos.get("z").asDouble() + "'>");
			builder.append("\n");
			builder.append("\t\t<Shape>");
			builder.append("\n");
			if (separator.hasLabel(Labels.Cylinder.name())) {
				builder.append("\t\t <Cylinder radius='" + separator.get("radius").asDouble() + "' height='"
						+ "\'></Cylinder>");
				builder.append("\n");
			} else {
				builder.append("\t\t <Box size='" + separator.get("width").asDouble() + " "
						+ panelSeparatorHeight + " " + separator.get("length").asDouble() + "'></Box>");
				builder.append("\n");
			}
			builder.append("\t\t\t <Appearance>");
			builder.append("\n");
			builder.append(
					"\t\t\t\t <Material diffuseColor='" + colorAsPercentage + "'></Material>");
			builder.append("\n");
			builder.append("\t\t\t </Appearance>");
			builder.append("\n");
			builder.append("\t\t </Shape>");
			builder.append("\n");
			builder.append("\t </Transform>");
			builder.append("\n");
		}
		builder.append("</Group>");
		builder.append("\n");
		return builder.toString();
	}

	private String toFloor(Node floor, Node entity) {
		Node position = connector.getPosition(floor.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<Group DEF='" + entity.get("hash").asString() + "'>");
		builder.append("\n");
		builder.append("\t <Transform translation='" + position.get("x").asDouble() + " "
				+ position.get("y").asDouble() + " " + position.get("z").asDouble() + "'>");
		builder.append("\n");
		builder.append("\t\t <Shape>");
		builder.append("\n");
		builder.append("\t\t\t <Box size='" + floor.get("width").asDouble() + " " + floor.get("height").asDouble()
				+ " " + floor.get("length").asDouble() + "'></Box>");
		builder.append("\n");
		builder.append("\t\t\t <Appearance>");
		builder.append("\n");
		builder.append("\t\t\t\t <Material diffuseColor='" + floor.get("color").asString() + "'></Material>");
		builder.append("\n");
		builder.append("\t\t\t </Appearance>");
		builder.append("\n");
		builder.append("\t\t </Shape>");
		builder.append("\n");
		builder.append("\t </Transform>");
		builder.append("\n");
		builder.append("</Group>\t\t");
		builder.append("\n");
		return builder.toString();
	}

	private String toChimney(Node chimney, Node entity) {
		final Node position = connector.getPosition(chimney.id());
	    StringBuilder builder = new StringBuilder();
	    builder.append("<Group DEF='" + entity.get("hash").asString() + "'>");
	    builder.append("\n");
	    builder.append("\t <Transform translation='" + position.get("x").asDouble() + " " + position.get("y").asDouble() + " " + position.get("z").asDouble() + "'>");
	    builder.append("\n");
	    builder.append("\t\t <Shape>");
	    builder.append("\n");
	    builder.append("\t\t\t <Cylinder height='" + chimney.get("height").asDouble() + "' radius='" + chimney.get("width").asDouble() + "'></Cylinder>");
	    builder.append("\n");
	    builder.append("\t\t\t <Appearance>");
	    builder.append("\n");
	    builder.append("\t\t\t\t <Material diffuseColor='" + chimney.get("color").asString() + "'></Material>");
	    builder.append("\n");
	    builder.append("\t\t\t </Appearance>");
	    builder.append("\n");
	    builder.append("\t\t </Shape>");
	    builder.append("\n");
	    builder.append("\t </Transform>");
	    builder.append("\n");
	    builder.append("</Group>\t\t");
	    builder.append("\n");
	    return builder.toString();
	}
}