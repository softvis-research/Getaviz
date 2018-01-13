package org.svis.lib.repository.repo.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.svis.lib.repository.repo.api.Author;
import org.svis.lib.repository.repo.api.Commit;
import org.svis.lib.repository.repo.api.Diff;
import org.svis.lib.repository.repo.api.Id;
import org.svis.lib.repository.repo.api.VersionedFile;
import org.svis.lib.repository.repo.api.exception.DiffNotAvailableException;
import org.svis.lib.repository.repo.api.exception.FilesNotAvailableException;
import org.svis.lib.repository.repo.api.impl.ByteArrayVersionedFile;
import org.svis.lib.repository.repo.api.impl.DiffImplementation;
import org.svis.lib.repository.repo.util.IterablePatternMatcher;


public class GitCommit implements Commit {

	private static final int BUFFER_SIZE = 1024 * 100;
	private static final String DEFAULT_ENCODING = "utf-8"; // TODO transfer to constants
	private RevCommit commit;
	private Repository repository;

	public GitCommit(Repository repository, RevCommit commit) {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
		this.repository = repository;
		this.commit = commit;
	}
	
	public String getName() {
		return commit.name();
	}
	
	public RevCommit getCommit() {
		return commit;
	}

	@Override
	public String getDescription() {
		return commit.getFullMessage();
	}

	@Override
	public Author getAuthor() {
		return new GitAuthor(commit.getAuthorIdent());
	}

	@Override
	public Date getTimestamp() {
		Date date = commit.getCommitterIdent().getWhen();
		
		return date;
	}

	@Override
	public Id getCommitId() {
		return new GitId(commit.getId());
	}

	@Override
	public Diff getDiff() throws DiffNotAvailableException {
		DiffImplementation returnable = new DiffImplementation();
		if(commit.getParentCount() != 0)
		try {
			RevCommit[] parents = commit.getParents();
			for(RevCommit parent : parents){
				addDiff(returnable, parent);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return returnable;
	}

	private void addDiff(DiffImplementation returnable, RevCommit parent) throws IOException {
		RevWalk revWalk = new RevWalk(repository);
		parent = revWalk.parseCommit(parent.getId());
		revWalk.close();
		ByteArrayOutputStream put = new ByteArrayOutputStream(BUFFER_SIZE);
		DiffFormatter df = new DiffFormatter(put);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
		for(DiffEntry e : diffs){
			df.format(e);
			String diffText = put.toString(DEFAULT_ENCODING); //TODO make encoding insertable
			returnable.addOperation(e.getOldPath(), new GitOperation(diffText, e.getOldPath(), e.getNewPath(), e.getChangeType()));
			put.reset();
		}
		df.close();
	}

	@Override
	public Map<String, VersionedFile> getFiles() throws FilesNotAvailableException {
		return getFiles(Collections.<String> emptyList());
	}

	@Override
	public Map<String, VersionedFile> getFiles(Iterable<String> filters) throws FilesNotAvailableException {
		Map<String, VersionedFile> returnable = new HashMap<String, VersionedFile>();
		IterablePatternMatcher matcher = new IterablePatternMatcher();
		Iterable<Pattern> patterns = matcher.transformToPattern(filters);
		try {
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String currentFilePath = treeWalk.getPathString();
				if (matcher.isIncluded(patterns, currentFilePath)) {
					returnable.put(currentFilePath, readToByteArray(treeWalk));
				}
			}
		} catch (IOException e) {
			throw new FilesNotAvailableException(e);
		}
		return returnable;
	}

	private VersionedFile readToByteArray(TreeWalk treeWalk) throws IOException {
		ObjectId objectId = treeWalk.getObjectId(0);
		ObjectLoader loader = repository.open(objectId);
		return new ByteArrayVersionedFile(IOUtils.toByteArray(loader.openStream()));
	}

	@Override
	public String toString() {
		return String
				.format("GitCommit [commit=%s, getDescription()=%s, getAuthor()=%s, getTimestamp()=%s]",
						commit, getDescription(), getAuthor(), getTimestamp());
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof Commit) {
			Commit other = (Commit) obj;
			isEqual = getCommitId().equals(other.getCommitId());
		}
		return isEqual;
	}
	
	@Override
	public int hashCode() {
		return getCommitId().hashCode();
	}

	@Override
	public int compareTo(Commit o) {
		return this.getTimestamp().compareTo(o.getTimestamp()) * -1;
	}

}
