package org.getaviz.generator;

import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.extract.Importer;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = -5343549433924172589L;
	private static final Log log = LogFactory.getLog(Servlet.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		log.info("POST request generator");
		SettingsConfiguration config = SettingsConfiguration.getInstance(request);
		List<ProgrammingLanguage> languages = importData(config);
		generateVisualization(config, languages);
		writePostResponse(response);
		log.info("POST request finished.");
	}

	private List<ProgrammingLanguage> importData(SettingsConfiguration config) {
		Importer importer = new Importer(config);
		importer.run();
		return importer.getImportedProgrammingLanguages();
	}

	private void generateVisualization(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
		Generator generator = new Generator(config, languages);
		generator.run();
	}
	
	private void writePostResponse(HttpServletResponse response) {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
