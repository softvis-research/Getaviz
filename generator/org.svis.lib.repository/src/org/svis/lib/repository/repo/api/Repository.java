package org.svis.lib.repository.repo.api;

import java.util.Collection;

import org.svis.lib.repository.repo.api.exception.BranchNotAvailableException;
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;
import org.svis.lib.repository.repo.api.exception.TagsNotAvailableException;

/**
 * Root object of an arbitary versioning system.
 * A repository can point to a remote object (needed for central versioning systems like subversion),
 * so it could be that an implementation requires connection to the remote repository.
 * This is a "read only API". 
 * 
 * @author Dan Häberlein
 *
 */
public interface Repository { // TODO commit search function using IDs?
	String getPath();
	Collection<Branch> getBranches() throws BranchNotAvailableException;
	Branch getBranch(String name) throws BranchNotAvailableException;
	Collection<Tag> getTags() throws TagsNotAvailableException;
    Branch getDefaultBranch() throws BranchNotAvailableException;
    Commit getCommitById(String id) throws CommitsNotAvailableException;
    String getName();
    void reset() throws Exception;
    boolean checkout(Commit commit);
}
