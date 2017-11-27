package org.svis.lib.repository.repo.svn;

import static org.svis.lib.repository.repo.api.Constants.FILE_SCHEME;
import static org.svis.lib.repository.repo.api.Constants.HTTPS_SCHEME;
import static org.svis.lib.repository.repo.api.Constants.HTTP_SCHEME;
import static org.svis.lib.repository.repo.api.Constants.SVN_SCHEME;
import static org.svis.lib.repository.repo.api.Constants.messageOutputStream;

import java.io.File;
import java.net.MalformedURLException;

import org.svis.lib.repository.repo.svn.util.ProgressBarReplicationHandler;
import org.svis.lib.repository.repo.util.TmpDirCreator;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.replicator.SVNRepositoryReplicator;

public class SubversionRepositoryInitializer {

	private static final String SCHEME_SEPARATOR = "://";
	private String url;

	public SubversionRepositoryInitializer(String url) {
		this.url = url;
	}

	public SVNRepository initRepository() throws SVNException, MalformedURLException {
		return initRepository(false);
	}
	
	public SVNRepository initRepository(boolean cached) throws SVNException, MalformedURLException {
		SVNRepository repo = null;
		SVNURL svnUrl = SVNURL.parseURIEncoded(url);
		if (url.startsWith(HTTP_SCHEME + SCHEME_SEPARATOR) || url.startsWith(HTTPS_SCHEME + SCHEME_SEPARATOR)) {
			DAVRepositoryFactory.setup();
			repo = DAVRepositoryFactory.create(svnUrl);
			repo.testConnection();
			if(cached) {
				TmpDirCreator tmpDirCreator = new TmpDirCreator(url);
				File tempDir = tmpDirCreator.getLocalTempDir();
				SVNURL cachedRepoPath = SVNURL.parseURIEncoded(FILE_SCHEME + SCHEME_SEPARATOR + tempDir);
				if(!tempDir.exists()){
					messageOutputStream.println("Caching subversion repository " + svnUrl + " This can take a while...");
					tempDir.mkdirs();
					cachedRepoPath = SVNRepositoryFactory.createLocalRepository(tempDir, true, true);
					tmpDirCreator.writeIdFileToTempDir();
					SVNRepository targetRepo = SVNRepositoryFactory.create(cachedRepoPath);
					SVNRepositoryReplicator replicator = SVNRepositoryReplicator.newInstance();
					replicator.setReplicationHandler(new ProgressBarReplicationHandler(repo.getLatestRevision()));
					replicator.replicateRepository(repo, targetRepo, -1, -1);
					messageOutputStream.println("\nCaching finished succesfully...");
				}
				svnUrl = cachedRepoPath;
				FSRepositoryFactory.setup();
				repo = FSRepositoryFactory.create(svnUrl);
			}
		} else if (url.startsWith(SVN_SCHEME + SCHEME_SEPARATOR)) {
			SVNRepositoryFactoryImpl.setup();
			repo = SVNRepositoryFactoryImpl.create(svnUrl);
		} else if (url.startsWith(FILE_SCHEME + SCHEME_SEPARATOR)) {
			FSRepositoryFactory.setup();
			repo = FSRepositoryFactory.create(svnUrl);
		} else
			throw new MalformedURLException(String.format("URL %s is not an supported SVN url!", url));
		repo.testConnection();
		return repo;
	}

}
