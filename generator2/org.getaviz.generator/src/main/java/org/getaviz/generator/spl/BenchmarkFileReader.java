package org.getaviz.generator.spl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class BenchmarkFileReader {
	
	private String path = "C:\\Users\\Janik\\Documents\\Bachelorarbeit\\argouml-spl-benchmark-solution\\ArgoUMLSPLBenchmark\\yourResults\\ScenarioTraditionalVariants";
	
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
		boolean isRefinement = false;
		ArrayList<String> parts = new ArrayList<String>(Arrays.asList(line.split(" ")));
		if (parts.get(parts.size() - 1).equalsIgnoreCase("Refinement")) {
			isRefinement = true;
			parts.remove(parts.size() - 1);
		}
		System.out.println(parts.size());
		System.out.println(parts.get(parts.size() - 1));
		if (parts.size() == 2 && parts.get(1).matches("[^\\(]*\\(.*\\)")) {
			trace.traceType = "Method";
			parts.add(1, parts.get(1).replaceAll("\\(.*\\)", ""));
			trace.name = parts.get(0) + "." + parts.get(1);
		} else if (parts.size() == 1) {
			trace.traceType = "Class";
			trace.name = parts.get(0);
		} else {
			// Werfe Exception
		}
		if (isRefinement) {
			trace.traceType += " Refinement";
		}
		return trace;
	}
	
	public static void main(String[] args) {
		BenchmarkFileReader reader = new BenchmarkFileReader();
		reader.read();
	}
}