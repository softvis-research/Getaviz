package org.svis.lib.repository.repo.git;

import org.svis.lib.repository.repo.api.Commit;
import org.svis.lib.repository.repo.api.Tag;
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;

public class GitTag implements Tag {

	private Commit commit;
	private String name;

	public GitTag(Commit commit, String name) {
		this.commit = commit;
		this.name = name;
	}
	
	@Override
	public Commit getCommit() {
		return commit;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if(obj instanceof Tag){
			Tag other = (Tag) obj;
			try {
				isEqual = name.equals(other.getName()) && other.getCommit().getCommitId().equals(commit.getCommitId());
			} catch (CommitsNotAvailableException e) {
				e.printStackTrace();
			}
		}
		return isEqual;
	}
	
	@Override
	public int hashCode() {
		return commit.hashCode();
	}

}
