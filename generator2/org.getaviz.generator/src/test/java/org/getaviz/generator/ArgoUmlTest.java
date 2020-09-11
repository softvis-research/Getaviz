package org.getaviz.generator;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.ArgoUml;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArgoUmlTest {

	private static Log log = LogFactory.getLog(ArgoUmlTest.class);
	private static ArgoUml mockup = new ArgoUml();
	private static DatabaseConnector connector;

	@BeforeAll
	static void setup() {
		initializeDatabase();
		extractAndGenerate();
	}
	
	@AfterAll
	static void close() {
		mockup.close();
	}
	
	static void initializeDatabase() {
		// mockup.setupDatabase("./test/databases/ArgoUmlTest.db");
		mockup.loadProperties("ArgoUmlTest.properties");
		// connector = mockup.getConnector(); 
		connector = DatabaseConnector.getInstance("bolt://localhost:7688");
	}

	
	static void extractAndGenerate() {
		Locale.setDefault(Locale.US);
		SettingsConfiguration config = SettingsConfiguration.getInstance();
		ArrayList<ProgrammingLanguage> languages = new ArrayList<ProgrammingLanguage>();
		languages.add(ProgrammingLanguage.JAVA);
		Generator generator = new Generator(config, languages);
		generator.run();
	}

	@Test
	void doNothing() {
		log.info("done");
	}
}