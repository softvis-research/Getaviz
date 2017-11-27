package org.svis.lib.repository.repo.svn;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;
import org.svis.lib.repository.repo.api.Author;
import org.svis.lib.repository.repo.api.Commit;
import org.svis.lib.repository.repo.api.Diff;
import org.svis.lib.repository.repo.api.Id;
import org.svis.lib.repository.repo.api.VersionedFile;
import org.svis.lib.repository.repo.api.exception.DiffNotAvailableException;
import org.svis.lib.repository.repo.api.exception.FilesNotAvailableException;
import org.svis.lib.repository.repo.api.impl.DiffImplementation;
import org.svis.lib.repository.repo.util.IterablePatternMatcher;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc2.ng.SvnDiffGenerator;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnDiff;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public class SubversionCommit implements Commit {

	private SVNLogEntry entry;
	private SVNRepository repo;
	private String fromBranch;

	public SubversionCommit(SVNLogEntry entry, SVNRepository repo, String fromBranch) {
		this.entry = entry;
		this.repo = repo;
		this.fromBranch = fromBranch;
	}
	
	public String getName() {
		return "";
	}
	
	public RevCommit getCommit() {
		return null;
	}

	@Override
	public int compareTo(Commit o) {
		return getTimestamp().compareTo(o.getTimestamp()) * -1;
	}

	@Override
	public String getDescription() {
		return entry.getMessage();
	}

	@Override
	public Author getAuthor() {
		return new SubversionAuthor(entry.getAuthor());
	}

	@Override
	public Date getTimestamp() {
		return entry.getDate();
	}

	@Override
	public Id getCommitId() {
		return new SubversionRevisionNumber(entry.getRevision());
	}

	@Override
	public Diff getDiff() throws DiffNotAvailableException {
		DiffImplementation returnable = new DiffImplementation();
		try {
			Map<String, SVNLogEntryPath> changedPaths = entry.getChangedPaths();
			long revision = entry.getRevision();
			for (Entry<String, SVNLogEntryPath> e : changedPaths.entrySet()) {
				String cleanedFilePath = removeBranchName(e.getKey());
				SubversionOperation op = new SubversionOperation();
				SVNLogEntryPath currentEntry = e.getValue();
				char type = currentEntry.getType();
				op.setOp(type);
				SVNNodeKind currentEntryKind = currentEntry.getKind();
				op.setFileType(currentEntryKind);
				switch (type) {
				case SVNLogEntryPath.TYPE_ADDED:
					op.setNewPath(cleanedFilePath);
					if (SVNNodeKind.FILE.equals(currentEntryKind)) {
						op.setDescription(createDiffDescription(revision, cleanedFilePath));
					}
					break;
				case SVNLogEntryPath.TYPE_DELETED:
					op.setOldPath(cleanedFilePath);
					break;
				case SVNLogEntryPath.TYPE_MODIFIED:
					op.setNewPath(cleanedFilePath);
					op.setOldPath(cleanedFilePath);
					if (SVNNodeKind.FILE.equals(currentEntryKind)) {
						op.setDescription(createDiffDescription(revision, cleanedFilePath));
					}
					break;
				case SVNLogEntryPath.TYPE_REPLACED:
					op.setNewPath(cleanedFilePath);
					System.out.println("ahja...");
					break;
				}
				returnable.addOperation(cleanedFilePath, op);
			}
		} catch (SVNException e) {
			throw new DiffNotAvailableException(e);
		}
		return returnable;
	}

	private String createDiffDescription(long revision, String cleanedFilePath) throws SVNException {
		String returnable = null;
		final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			final SvnDiffGenerator diffGenerator = new SvnDiffGenerator();
			final SvnDiff diff = svnOperationFactory.createDiff();
			SVNURL currentRepoFilePath = SVNURL.parseURIEncoded(repo.getLocation().toString() + "/" + cleanedFilePath);
			List<SVNFileRevision> revisions = new ArrayList<SVNFileRevision>();
			repo.getFileRevisions(cleanedFilePath, revisions, 0, entry.getRevision());
			diff.setSources(SvnTarget.fromURL(currentRepoFilePath, SVNRevision.create(getLastCommitWhenChanged(revision, revisions))),
					SvnTarget.fromURL(currentRepoFilePath, SVNRevision.create(revision)));
			diff.setUseGitDiffFormat(true);
			diff.setDiffGenerator(diffGenerator);
			diff.setOutput(byteArrayOutputStream);
			diff.setDepth(SVNDepth.EMPTY);
			diff.run();
			returnable = new String(byteArrayOutputStream.toByteArray());
		} finally {
			svnOperationFactory.dispose();
		}
		return returnable;
	}

	long getLastCommitWhenChanged(long revision, List<SVNFileRevision> revisions) {
		long min = Long.MAX_VALUE;
		long returnable = 0;
		for (SVNFileRevision r : revisions) {
			long curRev = r.getRevision();
			if (curRev < revision) {
				long curmin = Math.abs(revision - curRev);
				if (curmin < min) {
					min = curmin;
					returnable = curRev;
				}
			}
		}
		return returnable;
	}

	private String removeBranchName(String filepathWithBranch) {
		return filepathWithBranch.substring(1).replace(fromBranch + "/", "");
	}

	@Override
	public Map<String, VersionedFile> getFiles() throws FilesNotAvailableException {
		return getFiles(Collections.<String> emptySet());
	}

	private Map<String, VersionedFile> getFiles(String path, Iterable<Pattern> filters) throws FilesNotAvailableException {
		IterablePatternMatcher matcher = new IterablePatternMatcher();
		try {
			Map<String, VersionedFile> returnable = new HashMap<String, VersionedFile>();
			List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
			repo.getDir(path, entry.getRevision(), false, entries);
			for (SVNDirEntry dirEntry : entries) {
				SVNNodeKind kind = dirEntry.getKind();
				String currentFilePath = path + dirEntry.getName();
				if (SVNNodeKind.FILE.equals(kind)) {
					if (matcher.isIncluded(filters, currentFilePath)) {
						returnable.put(currentFilePath, new LazySvnVersionedFile(repo, currentFilePath, this.entry.getRevision()));
					}
				} else if (SVNNodeKind.DIR.equals(kind)) {
					returnable.putAll(getFiles(currentFilePath + "/", filters));
				} else {
					System.err.println("skipping unknown versioned item " + currentFilePath);
				}
			}
			return returnable;
		} catch (SVNException e) {
			throw new FilesNotAvailableException(e);
		}
	}

	@Override
	public Map<String, VersionedFile> getFiles(Iterable<String> filters) throws FilesNotAvailableException {
		return getFiles("", new IterablePatternMatcher().transformToPattern(filters));
	}

	@Override
	public String toString() {
		return String.format("SubversionCommit [getDescription()=%s, getTimestamp()=%s, getCommitId()=%s]", getDescription(), getTimestamp(), getCommitId());
	}

}
