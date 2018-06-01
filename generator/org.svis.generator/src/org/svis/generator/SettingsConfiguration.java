package org.svis.generator;

import java.io.File;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class SettingsConfiguration {
	private static File file;
	private static PropertiesConfiguration config;
	static {
		file = new File("../org.svis.generator/src/org/svis/generator/settings.properties");
		try {
			Configurations configs = new Configurations();
			config = configs.properties(file);
		} catch (ConfigurationException cex) {
			System.out.println(cex);
		}
		
	}
	
	public static PropertiesConfiguration getConfig() {
		return config;
	}
}
