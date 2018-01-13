package org.svis.lib.repository.repo.api;

import java.util.Map;

/**
 * Difference representation for textual changes in a versioning system.
 * 
 * @author Dan Häberlein
 *
 */
public interface Diff {
	Map<String, Operation> getDiffs();
}
