package org.svis.lib.repository.repo.svn;

import org.svis.lib.repository.repo.api.Commit;
import org.svis.lib.repository.repo.api.Tag;
import org.svis.lib.repository.repo.api.exception.BranchNotAvailableException;
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;

public class SubversionTag implements Tag {

	private SubversionBranch tagBranch;

	public SubversionTag(String name, String tagUrl) throws BranchNotAvailableException {
		tagBranch = new SubversionBranch(name, tagUrl);
	}

	@Override
	public Commit getCommit() throws CommitsNotAvailableException {
		return tagBranch.getLastCommit();
	}

	@Override
	public String getName() {
		return tagBranch.getName();
	}

}
