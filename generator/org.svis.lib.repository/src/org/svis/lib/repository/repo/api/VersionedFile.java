package org.svis.lib.repository.repo.api;

import org.svis.lib.repository.repo.api.exception.FilesNotAvailableException;

public interface VersionedFile {

	byte[] getContent() throws FilesNotAvailableException;
}
