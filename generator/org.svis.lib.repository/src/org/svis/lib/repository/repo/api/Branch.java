package org.svis.lib.repository.repo.api;

import java.util.Date;
import java.util.List;

import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;

/**
 * A branch of a versioning system. 
 * Enables read access to the commits and the branchname. 
 * 
 * @author Dan Häberlein
 *
 */
public interface Branch {
	List<Commit> getCommits(Date startDate, Date endDate) throws CommitsNotAvailableException;
	List<Commit> getCommits(int numberOfCommits) throws CommitsNotAvailableException;
	List<Commit> getCommits() throws CommitsNotAvailableException;
	Commit getLastCommit() throws CommitsNotAvailableException;
	String getName();
	Commit getCommit(String id);
}
