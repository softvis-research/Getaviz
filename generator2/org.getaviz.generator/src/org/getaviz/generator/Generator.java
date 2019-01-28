package org.getaviz.generator;

import org.getaviz.generator.city.m2m.City2City;
import org.getaviz.generator.city.m2t.City2AFrame;
import org.getaviz.generator.city.m2t.City2X3D;
import org.getaviz.generator.jqa.JQA2JSON;
import org.getaviz.generator.jqa.JQAEnhancement;
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
		boolean isCSourceCode = isCSourceCode(graph);
		
		new JQAEnhancement(isCSourceCode);
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
			if(isCSourceCode) {
				new C2RD();
			} else {
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

	private static boolean isCSourceCode(GraphDatabaseService graph) {
		Transaction tx = graph.beginTx();
		Result queryResult = null;
		try {
			queryResult = graph.execute("MATCH (n:C) RETURN n LIMIT 10");
		} finally {
			tx.close();
		}
		
		if(queryResult.hasNext()) {
			return true;
		} else {
			return false;
		}
	}
}
