package org.getaviz.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.city.m2m.City2City;
import org.getaviz.generator.city.m2t.City2AFrame;
import org.getaviz.generator.city.m2t.City2X3D;
import org.getaviz.generator.jqa.DatabaseBuilder;
import org.getaviz.generator.jqa.JQA2JSON;
import org.getaviz.generator.rd.m2m.RD2RD;
import org.getaviz.generator.rd.m2t.RD2AFrame;
import org.getaviz.generator.rd.m2t.RD2X3D;
import org.getaviz.generator.city.s2m.JQA2City;
import org.getaviz.generator.rd.s2m.JQA2RD;

public class Generator {
	private static SettingsConfiguration config = SettingsConfiguration.getInstance();
	private static Log log = LogFactory.getLog(Generator.class);

	public static void main(String[] args) {
		run();
	}

	public static void run() {
		log.info("Generator startet");
		// start generation process
		try {
			if(!config.isSkipScan()) {
				new DatabaseBuilder();
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
				new JQA2RD();
				new JQA2JSON();
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
}
