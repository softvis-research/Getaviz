package org.svis.generator;

import java.io.File;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;


public class SettingsConfiguration {
	private String path = "../org.svis.generator/src/org/svis/generator/settings.properties";
	private PropertiesConfiguration config;
	private String sectionPrefix = "";
	
	public SettingsConfiguration() {
		File file = new File(path);
		try {
			Configurations configs = new Configurations();
			config = configs.properties(file);
		} catch (ConfigurationException cex) {
			System.out.println(cex);
		}
		sectionPrefix = "famix.";
	}
	
	public String getDatabaseName() {
		return config.getString(sectionPrefix + "database_name");
	}
	
	public boolean isHidePrivateElements() {
		return config.getBoolean(sectionPrefix + "famix.hide_private_elements");
	}
	
	public String parser() {
		return config.getString(sectionPrefix + "famix.parser");
	}
	
	public boolean isAttributeSortSize() {
		return config.getBoolean(sectionPrefix + "attribute_sort_size");
	}
	
	public boolean isMasterRoot() {
		return config.getBoolean(sectionPrefix + "master_root");
	}
	
	public boolean isMergePackages() {
		return config.getBoolean("merge_packages");
	}
}
