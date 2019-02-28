package org.getaviz.generator;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GeneratorServlet extends HttpServlet {
	private static final long serialVersionUID = -5343549433924172589L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		SettingsConfiguration.getInstance("/opt/config/settings.properties");

		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		PrintWriter out = response.getWriter();
		out.println("<h1>Hello, World!</h1>");
		Generator.run();
	}
}
