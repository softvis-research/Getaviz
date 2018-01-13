package org.svis.lib.repository.repo.api;

import org.eclipse.jgit.lib.ObjectId;

/**
 * Interface for a class which implements the ID of a commit.
 * 
 * @author Dan Häberlein
 *
 */
public interface Id {
	String getIdRepresentation();
	ObjectId getObjectId();
}


