package org.getaviz.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.city.m2m.City2City;
import org.getaviz.generator.city.m2t.City2AFrame;
import org.getaviz.generator.city.m2t.City2X3D;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.jqa.C2JSON;
import org.getaviz.generator.jqa.DatabaseBuilder;
import org.getaviz.generator.jqa.JQA2JSON;
import org.getaviz.generator.jqa.JQACEnhancement;
import org.getaviz.generator.rd.m2m.RD2RD;
import org.getaviz.generator.rd.m2t.RD2AFrame;
import org.getaviz.generator.rd.m2t.RD2X3D;
import org.getaviz.generator.city.s2m.JQA2City;
import org.getaviz.generator.rd.s2m.C2RD;
import org.getaviz.generator.rd.s2m.JQA2RD;
import org.neo4j.driver.v1.StatementResult;

import javax.xml.crypto.Data;

public class Generator {
	private static org.getaviz.generator.SettingsConfiguration config = org.getaviz.generator.SettingsConfiguration.getInstance();
	private static Log log = LogFactory.getLog(Generator.class);
	private static DatabaseConnector connector = DatabaseConnector.getInstance();

	public static void main(String[] args) {
		run();
	}

	static void run() {
		log.info("Generator started");
		ProgrammingLanguage sourceCodeLanguage = getLanguage();
		try {
			if(!config.isSkipScan()) {
				if(sourceCodeLanguage == ProgrammingLanguage.C) {
					new JQACEnhancement();
				} else if(sourceCodeLanguage == ProgrammingLanguage.JAVA) {
					new DatabaseBuilder();
				}
			}
			switch (config.getMetaphor()) {
			case CITY: {
				new JQA2City();
				new JQA2JSON();
				new City2City();
				switch (config.getOutputFormat()) {
				case X3D:
					new City2X3D(); break;
				case AFrame:
					new City2AFrame(); break;
				}
				break;
			}
			case RD: {
				if(sourceCodeLanguage == ProgrammingLanguage.JAVA) {
					new JQA2RD();
					new JQA2JSON();

				} else {
					new C2RD();
					new C2JSON();
				}
				new RD2RD();
				switch (config.getOutputFormat()) {
				case X3D: {
					new RD2X3D();
					break;
				}
				case AFrame: {
					new RD2AFrame();
					break;
				}
				}
				break;
			}
			}

		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * Checks source code language in graph database and returns language of type ProgrammingLanguage.
	 * @return element of enum ProgrammingLanguage
	 */
	private static ProgrammingLanguage getLanguage() {
		StatementResult result = connector.executeRead("MATCH (n:C) RETURN n LIMIT 10");
		if(result.hasNext()) {
			return ProgrammingLanguage.C;
		} else {
			return ProgrammingLanguage.JAVA;
		}
	}

	public enum ProgrammingLanguage{
		JAVA,
		C
	}
}
