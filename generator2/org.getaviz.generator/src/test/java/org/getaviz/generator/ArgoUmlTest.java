package org.getaviz.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.extract.Importer;
import org.getaviz.generator.extract.ScanStep;
import org.getaviz.generator.mockups.ArgoUml;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		mockup.setupDatabase("./test/databases/ArgoUmlTest.db");
		mockup.loadProperties("ArgoUmlTest.properties");
		connector = mockup.getConnector();
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