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
		return config.getString("structure.database_name", "../databases/graph.db");
	}

	public boolean isHidePrivateElements() {
		return config.getBoolean("structure.hide_private_elements", false);
	}

	public String parser() {
		return config.getString("structure.parser", "verveinej");
	}

	public boolean isAttributeSortSize() {
		return config.getBoolean("structure.attribute_sort_size", false);
	}

	public boolean isMasterRoot() {
		return config.getBoolean("structure.master_root", false);
	}

	public boolean isMergePackages() {
		return config.getBoolean("structure.merge_packages", false);
	}

	public String getOutputFormat() {
		return config.getString("city.output_format", "x3d");
	}

	public String getBuildingType() {
		return config.getString("city.building_type", "city_original");
	}

	public String getScheme() {
		return config.getString("city.scheme", "types");
	}

	public String getClassElementsMode() {
		return config.getString("city.class_elements_mode", "methods_and_attributes");
	}

	public String getClassElementsSortModeCoarse() {
		return config.getString("city.class_elements_sort_mode_coarse", "methods_first");
	}

	public String getElementsSortModeFide() {
		return config.getString("city.elements_sort_mode_fide", "scheme");
	}

	public boolean isClassElementsSortModeFineDirectionReversed() {
		return config.getBoolean("city.class_elements_sort_mode_fine_direction_reversed", false);
	}

	public boolean isShowBuildingBase() {
		return config.getBoolean("city.building_base", false);
	}

	public String getBrickLayout() {
		return config.getString("city.brick.layout", "progressive");
	}

	public int getBrickSize() {
		return config.getInt("city.brick.size", 1);
	}

	public double getBrickHorizontalMargin() {
		return config.getDouble("city.brick.horizontal_margin", 0.5);
	}

	public double getBrickHorizontalGap() {
		return config.getDouble("city.brick.horizontal_gap", 0.2);
	}

	public double getBrickVerticalMargin() {
		return config.getDouble("city.brick.vertical_margin", 0.2);
	}

	public double getBrickVerticalGap() {
		return config.getDouble("city.brick.vertical_gap", 0.2);
	}

	public boolean isShowAttributesAsCylinders() {
		return config.getBoolean("city.show_attributes_as_cylinders", true);
	}

	public String getPanelSeparatorMode() {
		return config.getString("city.panel_separator_mode", "separator");
	}

	public int[] getPanelHeightTresholdNos() {
		int[] defaultValue = { 3, 6, 12, 24, 48, 96, 144, 192, 240 };
		String[] result = config.getStringArray("city.panel.height_treshold_nos");
		if (result.length == 0) {
			return defaultValue;
		} else {
			int[] value = new int[result.length];
			for(int i = 0; i < result.length; i++) {
				value[i] = Integer.parseInt(result[i]);
			}
			return value;
		}
	}

	public double getPanelHeightUnit() {
		return config.getDouble("city.panel.height_unit", 0.5);
	}

	public double getPanelHorizontalMargin() {
		return config.getDouble("city.panel.horizontal_margin", 0.5);
	}

	public double getPanelVerticalMargin() {
		return config.getDouble("city.panel.vertical_margin", 0.25);
	}

	public double getPanelVerticalGap() {
		return config.getDouble("city.panel.vertical_gap", 0.125);
	}

	public double getPanelSeparatorHeight() {
		return config.getDouble("city.panel.separator_height", 0.125);
	}

	public String getOriginalBuildingMetric() {
		return config.getString("city.original_building_metric", "none");
	}

	public int getWidthMin() {
		return config.getInt("city.width_min", 1);
	}

	public int getHeightMin() {
		return config.getInt("city.height_min", 1);
	}

	public double getBLDGHorizontalMargin() {
		return config.getDouble("city.BLDG_horizontal_margin", 3.0);
	}

	public double getBLDGHorizontalGap() {
		return config.getDouble("city.BLDG_horizontal_gap", 3.0);
	}

	public double getBLDGVerticalMargin() {
		return config.getDouble("city.BLDG_vertical_margin", 3.0);
	}

	public Color getPCKGColorStart() {
		return getColor(config.getString("city.PCKG_color_start", "#969696"));
	}

	public Color getPCKGColorEnd() {
		return getColor(config.getString("city.PCKG_color_end", "#f0f0f0"));
	}

	public Color getCLSSColorStart() {
		return getColor(config.getString("city.CLSS_color_start", "#131615"));
	}

	public Color getCLSSColorEnd() {
		return getColor(config.getString("city.CLSS_color_end", "#00ff00"));
	}

	public Color getCLSSColor() {
		return getColor(config.getString("city.CLSS_color", "#353559"));
	}

	public Color getDynamicCLSSColorStart() {
		return getColor(config.getString("city.dynamic_CLSS_color_start", "#fa965c"));
	}

	public Color getDynamicCLSSColorEnd() {
		return getColor(config.getString("dynamic_CLSS_color_end", "#feb280"));
	}

	public Color getDynamicMethod() {
		return getColor(config.getString("city.dynamic_method", "#735eb9"));
	}

	public Color getDynamicPCKGColorStart() {
		return getColor(config.getString("city.dynamic_PCKG_color_start", "#23862c"));
	}

	public Color getDynamicPCKGColorEnd() {
		return getColor(config.getString("city.dynamic_PCKG_color_end", "#7bcd8d"));
	}

	public Color getCityColor(String name) {
		String color = name.toLowerCase();
		return getColor(config.getString("city.color." + color));
	}

	private Color getColor(String hex) {
		return Color.decode(hex);
	}
}
