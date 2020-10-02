package org.getaviz.generator.spl;

import org.getaviz.generator.Step;

import java.io.FileReader;
import java.io.FileWriter;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class SPLEnhancement implements Step {
	
	@Override
	public boolean checkRequirements() {
		// TODO Auto-generated method stub
		// check if metaData.json exists and if not, tell that it has to run before
		return false;
	}

	@Override
	public void run() {
		if (checkRequirements()) {
			enhanceClasses();
		}
	}

	private void enhanceClasses() {
		BenchmarkFileReader bfReader = new BenchmarkFileReader();
		ArrayList<FeatureTrace> featureTraces = bfReader.read();
 		String path = "/home/lyannen/Dokumente/Uni/Bachelor/Semester_6/ba/software/temp_files/metaData.json";
		JSONParser parser = new JSONParser();
		try { 
			FileReader reader = new FileReader(path);
			JSONArray arr = (JSONArray) parser.parse(reader);
			for (Object obj: arr) {
				JSONObject jsonObj = (JSONObject) obj;
				String qualifiedName = jsonObj.get("qualifiedName").toString();
				int index = qualifiedName.indexOf("(");
				if (index >= 0) {
					qualifiedName = qualifiedName.substring(0, index);
				}

				ArrayList<FeatureAffiliation> featureAffiliations = new ArrayList<>();
				for (FeatureTrace featureTrace: featureTraces) {
					if (qualifiedName.equals(featureTrace.name)) {
						FeatureAffiliation featureAffiliation = new FeatureAffiliation();
						featureAffiliation.feature = featureTrace.featureAffiliation;
						featureAffiliation.traceType = featureTrace.traceType;
						featureAffiliation.isRefinement = featureTrace.isRefinement;
						featureAffiliations.add(featureAffiliation);
					}
				}

				featureAffiliations = determineElementarySets(featureAffiliations);

				JSONArray featureAffiliationsJSON = new JSONArray();
				for (FeatureAffiliation featureAffiliation: featureAffiliations) {
					JSONObject featureAffiliationJSON = new JSONObject();
					featureAffiliationJSON.put("feature", featureAffiliation.feature);
					featureAffiliationJSON.put("traceType", featureAffiliation.traceType);
					featureAffiliationJSON.put("isRefinement", featureAffiliation.isRefinement);
					if (!featureAffiliation.isRefinement) {
						featureAffiliationJSON.put("elementarySet", featureAffiliation.elementarySet.toString());
					}
					featureAffiliationsJSON.add(featureAffiliationJSON);
				}

				if (!featureAffiliations.isEmpty()) {
					jsonObj.put("featureAffiliations", featureAffiliationsJSON);
				}
			}
			FileWriter file = new FileWriter("/home/lyannen/Dokumente/Uni/Bachelor/Semester_6/ba/software/temp_files/metaData_spl.json");
	        file.write(arr.toJSONString());
	        file.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	private ArrayList<FeatureAffiliation> determineElementarySets(ArrayList<FeatureAffiliation> featureAffiliations) {
		ArrayList<FeatureAffiliation> featureAffiliationsWithoutRefinements = new ArrayList<>();
		for (FeatureAffiliation featureAffiliation: featureAffiliations) {
			if (!featureAffiliation.isRefinement) {
				featureAffiliationsWithoutRefinements.add(featureAffiliation);
			}
		}
		if (!featureAffiliationsWithoutRefinements.isEmpty()) {
			if (featureAffiliationsWithoutRefinements.size() == 1) {
				FeatureAffiliation featureAffiliation = featureAffiliationsWithoutRefinements.get(0);
				if (featureAffiliation.feature.contains("_and_")) {
					featureAffiliation.elementarySet = FeatureAffiliation.ElementarySet.And;
				} else {
					featureAffiliation.elementarySet = FeatureAffiliation.ElementarySet.Pure;
				}
			} else {
				Boolean tooComplicated = false;
				for (FeatureAffiliation featureAffiliation: featureAffiliationsWithoutRefinements) {
					if (featureAffiliation.feature.contains("_and_")) {
						tooComplicated = true;
						break;
					}
				}
				if (!tooComplicated) {
					for (FeatureAffiliation featureAffiliation: featureAffiliationsWithoutRefinements) {
						featureAffiliation.elementarySet = FeatureAffiliation.ElementarySet.Or;
					}
				}
			}
		}
		return featureAffiliations;
	}
	
	public static void main(String[] args) {
		SPLEnhancement spl = new SPLEnhancement();
		spl.enhanceClasses();
	}

}
