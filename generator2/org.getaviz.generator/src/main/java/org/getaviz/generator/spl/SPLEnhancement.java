package org.getaviz.generator.spl;

import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;

public class SPLEnhancement implements Step {

	private DatabaseConnector connector = DatabaseConnector.getInstance();
	//private 
	
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
		BenchmarkFileReader reader = new BenchmarkFileReader();
		reader.read();
	}

}
