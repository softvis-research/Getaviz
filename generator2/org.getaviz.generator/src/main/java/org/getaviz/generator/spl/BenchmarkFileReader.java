package org.getaviz.generator.spl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BenchmarkFileReader {
	private Log log = LogFactory.getLog(this.getClass());
	private String path = "/var/lib/jetty/benchmark-files";

	public BenchmarkFileReader(SettingsConfiguration config) {
		String zipFileUrl = config.getSPLBenchmarkFileLocation();
		try {
			unzip(zipFileUrl);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void unzip(String zipFileUrl) throws Exception {
		File destinationDirectory = new File(path);
		if (!destinationDirectory.exists()) {
			destinationDirectory.mkdir();
		}
		ZipInputStream zis;
		try {
			zis = new ZipInputStream(new URL(zipFileUrl).openStream());
		} catch (Exception e) {
			throw new Exception("No .zip file found at: " + zipFileUrl);
		}
		ZipEntry zipEntry = zis.getNextEntry();
		byte[] buffer = new byte[2048];
		while (zipEntry != null) {
			File currentFile = new File(destinationDirectory, new File(zipEntry.getName()).getName());
			currentFile.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(currentFile);
			int length;
			while ((length = zis.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			outputStream.close();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}
	
	public ArrayList<FeatureTrace> read() {
		File folder = new File(path);
		File[] files = folder.listFiles();
		ArrayList<FeatureTrace> featureTraces = new ArrayList<FeatureTrace>();
		
		for (File file: files) {
			String featureConfiguration = file.getName().replace(".txt", "");
			try {
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					FeatureTrace trace = buildFeatureTrace(line);
					trace.featureAffiliation = featureConfiguration;
					featureTraces.add(trace);
				}
				scanner.close();
			}
			catch (Exception e) {
				
			}
		}
		return featureTraces;
	}
	
	private FeatureTrace buildFeatureTrace(String line) {
		FeatureTrace trace = new FeatureTrace();
		ArrayList<String> parts = new ArrayList<String>(Arrays.asList(line.split(" ")));
		if (parts.get(parts.size() - 1).equalsIgnoreCase("Refinement")) {
			trace.isRefinement = true;
			parts.remove(parts.size() - 1);
		}
		if (parts.size() == 2 && parts.get(1).matches("[^\\(]*\\(.*\\)")) {
			trace.traceType = "Method";
			parts.add(1, parts.get(1).replaceAll("\\(.*\\)", ""));
			trace.name = parts.get(0) + "." + parts.get(1);
		} else if (parts.size() == 1) {
			trace.traceType = "Class";
			trace.name = parts.get(0);
		} else {
			// TODO Werfe Exception
		}
		if (trace.isRefinement) {
			trace.traceType += " Refinement";
		}
		return trace;
	}

	public static void main(String[] args) {
		SettingsConfiguration config = SettingsConfiguration.getInstance();
		BenchmarkFileReader benchmarkFileReader = new BenchmarkFileReader(config);
	}
}