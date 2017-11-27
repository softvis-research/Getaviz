package org.svis.lib.repository.repo.git;

import java.util.HashMap;
import java.util.Map;

import org.svis.lib.repository.repo.api.Diff;
import org.svis.lib.repository.repo.api.Operation;

public class GitDiff implements Diff {

	private Map<String, Operation> diffMap = new HashMap<String, Operation>();
	
	@Override
	public Map<String, Operation> getDiffs() {
		return diffMap;
	}
	
	public void addOperation(String filepath, GitOperation op) {
		diffMap.put(filepath, op);
	}
	
	@Override
	public String toString() {
		return diffMap.toString();
	}

}
