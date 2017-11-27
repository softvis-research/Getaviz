package org.svis.lib.repository.repo.api;

/**
 * A complex type to represent an author of commits in an versioning system.
 * 
 * @author Dan Häberlein
 *
 */
public interface Author {
	String getEmailAddress();
	String getName();
}
