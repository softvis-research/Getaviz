package org.svis.lib.repository.repo.svn;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.svis.lib.repository.repo.api.VersionedFile;
import org.svis.lib.repository.repo.api.exception.FilesNotAvailableException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

public class LazySvnVersionedFile implements VersionedFile {

	private static final int ONE_MB = 1024 * 1014;
	
	private SVNRepository repo;
	private String currentFilePath;
	private long revision;

	public LazySvnVersionedFile(SVNRepository repo, String currentFilePath, long revision) {
		this.repo = repo;
		this.currentFilePath = currentFilePath;
		this.revision = revision;
	}

	@Override
	public byte[] getContent() throws FilesNotAvailableException {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream(ONE_MB);
			repo.getFile(currentFilePath, revision, null, output);
			return output.toByteArray();
		} catch (SVNException e) {
			throw new FilesNotAvailableException(e);
		}
	}

}
