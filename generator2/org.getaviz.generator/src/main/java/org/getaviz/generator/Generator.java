package org.getaviz.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.jqa.CEnhancement;
import org.getaviz.generator.jqa.JavaEnhancement;
import java.util.List;

public class Generator {
	private static Log log = LogFactory.getLog(Generator.class);
	private Metaphor metaphor;
	private SettingsConfiguration config;
	private List<ProgrammingLanguage> languages;

	public Generator(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
		this.config = config;
		metaphor = MetaphorFactory.createMetaphor(config, languages);
		this.languages = languages;
	}

	public void run() {
		log.info("Generator started");
		JavaEnhancement java = new JavaEnhancement(config);
		java.run();
		CEnhancement c = new CEnhancement(config,languages);
		c.run();
		metaphor.generate();
		log.info("Generator finished");
	}
}
