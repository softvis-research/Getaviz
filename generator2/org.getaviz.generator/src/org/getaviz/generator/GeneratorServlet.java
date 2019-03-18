package org.getaviz.generator;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.getaviz.generator.SettingsConfiguration.OutputFormat;

public class GeneratorServlet extends HttpServlet {
	private static final long serialVersionUID = -5343549433924172589L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		SettingsConfiguration config = SettingsConfiguration.getInstance("/opt/config/settings.properties");

		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		PrintWriter out = response.getWriter();
		out.println("<h1>Getaviz</h1>");
		out.println("<h3>Visualization is generated, please wait. You will be redirected automatically.</h3>");
		Generator.run();
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		
		/**
		 * The following code should be replaced once the ui is connected to the backend
		 */
		String url = "http://localhost:8082/ui/index.php";
		String name = config.getName();
		boolean aframe = config.getOutputFormat() == OutputFormat.AFrame;
		
		response.setHeader("Location", url + "?aframe=" + Boolean.toString(aframe) + "&model=" + name + "&setup=web_a-frame/City%20bank&srcDir=data-gen");
	}
}
