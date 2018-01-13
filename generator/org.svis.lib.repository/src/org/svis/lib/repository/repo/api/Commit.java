package org.svis.lib.repository.repo.api;

import java.util.Date;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;
import org.svis.lib.repository.repo.api.exception.DiffNotAvailableException;
import org.svis.lib.repository.repo.api.exception.FilesNotAvailableException;

/**
 * Representation of a commit of an arbitrary versioning system. 
 * Grants read access to all components of an commit including files, 
 * difference to the last commit and other meta information. 
 * 
 * @author Dan Häberlein
 *
 */
public interface Commit extends Comparable<Commit> {
	String getDescription();
	String getName();
	RevCommit getCommit();
	Author getAuthor();
	Date getTimestamp();
	Id getCommitId();	
	Diff getDiff() throws DiffNotAvailableException;
	Map<String, VersionedFile> getFiles() throws FilesNotAvailableException;
	Map<String, VersionedFile> getFiles(Iterable<String> filters) throws FilesNotAvailableException;
}
