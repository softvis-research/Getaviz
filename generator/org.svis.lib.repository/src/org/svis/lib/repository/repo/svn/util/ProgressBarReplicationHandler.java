package org.svis.lib.repository.repo.svn.util;

import static org.svis.lib.repository.repo.api.Constants.ABORT_THRESHOLD;

import java.util.concurrent.atomic.AtomicLong;

import org.svis.lib.repository.repo.util.ProgressBarPrinter;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.replicator.ISVNReplicationHandler;
import org.tmatesoft.svn.core.replicator.SVNRepositoryReplicator;

public class ProgressBarReplicationHandler implements ISVNReplicationHandler {

	private AtomicLong lastReplicatedTime = new AtomicLong(Long.MAX_VALUE);
	private ProgressBarPrinter printer;
	
	public ProgressBarReplicationHandler(long latestRevision) {
		printer = new ProgressBarPrinter(latestRevision);
	}

	@Override
	public void revisionReplicating(SVNRepositoryReplicator source, SVNLogEntry logEntry) throws SVNException {}

	@Override
	public void revisionReplicated(SVNRepositoryReplicator source, SVNCommitInfo commitInfo) throws SVNException {
		printer.printProgressBar(commitInfo.getNewRevision());
	}
	
	@Override
	public void checkCancelled() throws SVNCancelException {
		long timeDifferenceInMillsec = System.currentTimeMillis() - lastReplicatedTime.get();
		if(timeDifferenceInMillsec > ABORT_THRESHOLD){
			throw new SVNCancelException();
		} else {
			lastReplicatedTime.set(Long.MAX_VALUE);
		}
	}
}
