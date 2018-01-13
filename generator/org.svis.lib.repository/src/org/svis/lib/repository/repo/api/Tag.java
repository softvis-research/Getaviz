package org.svis.lib.repository.repo.api;

import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;

/**
 * Simple interface representing tags of an versioning system.
 * Tags are usually just named commits. 
 * This interface provides read only access to the tagged commit and tag name.
 * 
 * @author Dan Häberlein
 *
 */
public interface Tag {
	Commit getCommit() throws CommitsNotAvailableException;
	String getName();
}
