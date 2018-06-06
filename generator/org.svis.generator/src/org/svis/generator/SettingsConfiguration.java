package org.svis.generator;

import java.io.File;
import java.awt.Color;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class SettingsConfiguration {
	private String path = "../org.svis.generator/src/org/svis/generator/settings.properties";
	private PropertiesConfiguration config;
	
	public SettingsConfiguration() {
		File file = new File(path);
		try {
			Configurations configs = new Configurations();
			config = configs.properties(file);
		} catch (ConfigurationException cex) {
			System.out.println(cex);
		}
	}
	
	public String getDatabaseName() {
		return config.getString("structure.database_name");
	}
	
	public boolean isHidePrivateElements() {
		return config.getBoolean("structure.hide_private_elements");
	}
	
	public String parser() {
		return config.getString("structure.parser");
	}
	
	public boolean isAttributeSortSize() {
		return config.getBoolean("structure.attribute_sort_size");
	}
	
	public boolean isMasterRoot() {
		return config.getBoolean("structure.master_root");
	}
	
	public boolean isMergePackages() {
		return config.getBoolean("structure.merge_packages");
	}
	
	public String getOutputFormat() {
		return config.getString("city.output_format");
	}
	
	public String getBuildingType() {
		return config.getString("city.building_type");
	}
	
	public String getScheme() {
		return config.getString("city.scheme");
	}
	
	public String getClassElementsMode() {
		return config.getString("city.class_elements_mode");
	}
	
	public String getClassElementsSortModeCoarse() {
		return config.getString("city.class_elements_sort_mode_coarse");
	}
	
	public String getElementsSortModeFide() {
		return config.getString("city.elements_sort_mode_fide");
	}
	
	public boolean isBuildingBase() {
		return config.getBoolean("city.building_base");
	}
	
	public String getBrickLayout() {
		return config.getString("city.brick.layout");
	}
	
	public int getBrickSize() {
		return config.getInt("city.brick.size");
	}
	
	public double getBrickHorizontalMargin() {
		return config.getDouble("city.brick.horizontal_margin");
	}
	
	public double getBrickHorizontalGap() {
		return config.getDouble("city.brick.horizontal_gap");
	}
	
	public double getBrickVerticalMargin() {
		return config.getDouble("city.brick.vertical_margin");
	}
	
	public double getBrickVerticalGap() {
		return config.getDouble("city.brick.vertical_gap");
	}
	
	public boolean isShowAttributesAsCylinders() {
		return config.getBoolean("city.show_attributes_as_cylinders");
	}
	
	public String getPanelSeparatorMode() {
		return config.getString("city.panel_separator_mode");
	}
	
	public String[] getPanelHeightTresholdNos() {
		return config.getStringArray("city.panel.height_treshold_nos");
	}
	
	public double getPanelHeightUnit() {
		return config.getDouble("city.panel.height_unit");
	}
	
	public double getPanelHorizontalMargin() {
		return config.getDouble("city.panel.horizontal_margin");
	}
	
	public double getPanelVerticalMargin() {
		return config.getDouble("city.panel.vertical_margin");
	}
	
	public double getPanelVerticalGap() {
		return config.getDouble("city.panel.vertical_gap");
	}
	
	public double getPanelSeparatorHeight() {
		return config.getDouble("city.panel.separator_height");
	}
	
	public String getOriginalBuildingMetric() {
		return config.getString("city.original_building_metric");
	}
	
	public int getWidthMin() {
		return config.getInt("city.width_min");
	}
	
	public int getHeightMin() {
		return config.getInt("city.height_min");
	}
	
	public double getBLDGHorizontalMargin() {
		return config.getDouble("city.BLDG_horizontal_margin");
	}
	
	public double getBLDGHorizontalGap() {
		return config.getDouble("city.BLDG_horizontal_gap");
	}
	
	public double getBLDGVerticalMargin() {
		return config.getDouble("city.BLDG_vertical_margin");
	}
	
	public Color getPCKGColorStart() {	
		return getColor(config.getString("city.PCKG_color_start"));
	}
	
	public Color getPCKGColorEnd() {
		return getColor(config.getString("city.PCKG_color_end"));
	}
	
	public Color getCLSSColorStart() {
		return getColor(config.getString("city.CLSS_color_start"));
	}
	
	public Color getCLSSColorEnd() {
		return getColor(config.getString("city.CLSS_color_end"));
	}
	
	public Color getCLSSColor() {
		return getColor(config.getString("city.CLSS_color"));
	}
	
	public Color getDynamicCLSSColorStart() {
		return getColor(config.getString("city.dynamic_CLSS_color_start"));
	}
	
	public Color getDynamicCLSSColorEnd() {
		return getColor(config.getString("dynamic_CLSS_color_end"));
	}
	
	public Color getDynamicMethod() {
		return getColor(config.getString("city.dynamic_method"));
	}
	
	public Color getDynamicPCKGColorStart() {
		return getColor(config.getString("city.dynamic_PCKG_color_start"));
	}
	
	public Color getDynamicPCKGColorEnd() {
		return getColor(config.getString("city.dynamic_PCKG_color_end"));
	}
	
	public Color getCityColor(String name) {
		String color = name.toLowerCase();
		return getColor(config.getString("city.color." + color));
	}
	
	private Color getColor(String hex) {
		return Color.decode(hex);
	}
}
