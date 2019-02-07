package org.getaviz.generator;

import org.getaviz.generator.city.m2m.City2City;
import org.getaviz.generator.city.m2t.City2AFrame;
import org.getaviz.generator.city.m2t.City2X3D;
import org.getaviz.generator.jqa.JQA2JSON;
import org.getaviz.generator.jqa.JQACEnhancement;
import org.getaviz.generator.jqa.JQAJavaEnhancement;
import org.getaviz.generator.rd.m2m.RD2RD;
import org.getaviz.generator.rd.m2t.RD2AFrame;
import org.getaviz.generator.rd.m2t.RD2X3D;
import org.getaviz.generator.city.s2m.JQA2City;
import org.getaviz.generator.rd.s2m.C2RD;
import org.getaviz.generator.rd.s2m.Java2RD;
import org.getaviz.lib.database.Database;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class Generator {

	public static void main(String[] args) {
		SettingsConfiguration config = SettingsConfiguration.getInstance();
		GraphDatabaseService graph = Database.getInstance(config.getDatabaseName());
		ProgrammingLanguage sourceCodeLanguage = getLanguage(graph);
		
		if(sourceCodeLanguage == ProgrammingLanguage.C) {
			new JQACEnhancement();
		} else if(sourceCodeLanguage == ProgrammingLanguage.JAVA) {
			new JQAJavaEnhancement();
		}
		
		switch (config.getMetaphor()) {
		case CITY: {
			new JQA2City();
			new JQA2JSON();
			new City2City();
			switch(config.getOutputFormat()) {
			case X3D: new City2X3D();
			case AFrame: new City2AFrame();
			}
			break;
		}
		case RD: {
			if(sourceCodeLanguage == ProgrammingLanguage.C) {
				new C2RD();
			} else if(sourceCodeLanguage == ProgrammingLanguage.JAVA){
				new Java2RD();
			}
			new JQA2JSON();
			new RD2RD();
			switch(config.getOutputFormat()) {
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
	}

	/**
	 * Checks source code language in graph database and returns language of type ProgrammingLanguage.
	 * @param graph of type GraphDatabaseService
	 * @return element of enum ProgrammingLanguage
	 */
	private static ProgrammingLanguage getLanguage(GraphDatabaseService graph) {
		Transaction tx = graph.beginTx();
		Result queryResult = null;
		try {
			queryResult = graph.execute("MATCH (n:C) RETURN n LIMIT 10");
		} finally {
			tx.close();
		}
		
		if(queryResult.hasNext()) {
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
