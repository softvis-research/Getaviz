package org.getaviz.generator;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GeneratorServlet extends HttpServlet {
	private static final long serialVersionUID = -5343549433924172589L;
	private static Log log = LogFactory.getLog(GeneratorServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("GET request generator");
		Generator.run();
		writeGetResponse(response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		log.info("POST request generator");
		SettingsConfiguration config = SettingsConfiguration.getInstance(request);
		Generator.run();
		writePostResponse(response);
	}
	
	private void writePostResponse(HttpServletResponse response) {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	private void writeGetResponse(HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println("<h1>Getaviz</h1>");
		out.println("<h3>Visualization has been generated to the output directory.</h3>");
	}
}
