package org.svis.lib.repository.repo.git;

import static org.svis.lib.repository.repo.api.Constants.CACHED;
import static org.svis.lib.repository.repo.api.Constants.GIT_SCHEME;
import static org.svis.lib.repository.repo.api.Constants.HTTP_SCHEME;
import static org.svis.lib.repository.repo.api.Constants.messageOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevWalk;
import org.svis.lib.repository.repo.api.Branch;
import org.svis.lib.repository.repo.api.Commit;
import org.svis.lib.repository.repo.api.Repository;
import org.svis.lib.repository.repo.api.Tag;
import org.svis.lib.repository.repo.api.exception.BranchNotAvailableException;
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;
import org.svis.lib.repository.repo.api.exception.RepositoryNotAvailableException;
import org.svis.lib.repository.repo.api.exception.TagsNotAvailableException;
import org.svis.lib.repository.repo.git.util.GitProgressMonitor;
import org.svis.lib.repository.repo.util.TmpDirCreator;
import org.svis.lib.repository.repo.git.GitBranch;

public class GitRepository implements Repository {

	private org.eclipse.jgit.lib.Repository delegateRepository;
	private Git git;

	public GitRepository(String dirPath) throws RepositoryNotAvailableException {
		try {
			File localDir = null;
			if(dirPath.startsWith(GIT_SCHEME)){
				throw new IllegalArgumentException("There is no support for authenticated repositories in the moment.");
			} else if(dirPath.startsWith(HTTP_SCHEME)){
				if(!CACHED)
					throw new IllegalArgumentException("Remote git repositories must be cached due to be able to read from them!");
				localDir = cloneToTmpDir(dirPath);
			} else {
				localDir = new File(dirPath);
			}
			delegateRepository = new RepositoryBuilder().findGitDir(localDir).build();
			git = new Git(delegateRepository);
		} catch (Exception e) {
			throw new RepositoryNotAvailableException(e);
		}
	}
	
	public boolean checkout(Commit commit) {
		CheckoutCommand checkout = git.checkout();
		checkout.setStartPoint(commit.getCommit());
		checkout.setName(commit.getCommitId().getIdRepresentation());
		checkout.setCreateBranch(false);
		try {
			checkout.call();
			return true;
		} catch (Exception e){
			return false;
		}
	}
	
	public void reset() throws Exception {
		CheckoutCommand checkout = git.checkout();
		checkout.setName(getDefaultBranch().getName());
		checkout.setCreateBranch(false);
		checkout.call();
	}

	private File cloneToTmpDir(String pathToRemoteGitRepository) throws GitAPIException, InvalidRemoteException, TransportException {
		File localDir;
		TmpDirCreator tmpDirCreator = new TmpDirCreator(pathToRemoteGitRepository);
		localDir = tmpDirCreator.getLocalTempDir();
		if(!localDir.exists()){
			messageOutputStream.println("Caching git repository " + pathToRemoteGitRepository + "...");
			Git.cloneRepository().setURI(pathToRemoteGitRepository)
								 .setDirectory(localDir)
								 .setProgressMonitor(new GitProgressMonitor())
								 .call();
			localDir.mkdirs();
			messageOutputStream.println("\nCaching finished succesfully...");
			tmpDirCreator.writeIdFileToTempDir();
		}
		return localDir;
	}
	
	@Override
	public String getPath() {
		return delegateRepository.getWorkTree().getAbsolutePath();
	}

	@Override
	public Collection<Branch> getBranches() throws BranchNotAvailableException {
		List<Branch> result = new ArrayList<Branch>();
		try {
			Map<String, Ref> refs = delegateRepository.getRefDatabase().getRefs(Constants.R_HEADS);
			for (Entry<String, Ref> entry : refs.entrySet()){
				String name = entry.getKey();
				Ref value = entry.getValue();
				result.add(createBranch(name, value));
			}
			return result;
		} catch (IOException e) {
			throw new BranchNotAvailableException(e);
		}
	}

	private GitBranch createBranch(String name, Ref value) {
		return new GitBranch(this, value, name);
	}
	
	@Override
	public Branch getBranch(String name) throws BranchNotAvailableException {
		try {
			Ref ref = delegateRepository.getRef(Constants.R_HEADS + name);
			return createBranch(name, ref);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BranchNotAvailableException(e);
		}
	}

	@Override
	public Collection<Tag> getTags() throws TagsNotAvailableException {
		List<Tag> result = new ArrayList<Tag>();
		try {
			Git del = new Git(delegateRepository);
			List<Ref> tags = del.tagList().call();
			del.close();
			RevWalk walk = new RevWalk(delegateRepository);
			for (Ref tag : tags){
				GitCommit commit = new GitCommit(delegateRepository, walk.parseCommit(tag.getObjectId()));
				GitTag wrapperTag = new GitTag(commit, createTagName(tag));
				result.add(wrapperTag);
				walk.close();
			}
			return result;
		} catch (Exception e) {
			throw new TagsNotAvailableException(e);
		}
	}

    private String createTagName(Ref tag) {
		String[] split = tag.getName().split("/");
		String tagName = split[split.length - 1];
		return tagName;
	}
    
	public org.eclipse.jgit.lib.Repository getDelegateRepository() {
		return delegateRepository;
	}
	
	@Override
    public Branch getDefaultBranch() throws BranchNotAvailableException {
        try {
			return getBranch(delegateRepository.getBranch());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public Commit getCommitById(String id) throws CommitsNotAvailableException {
		try {
			for(Branch b: getBranches()){
				Commit c = b.getCommit(id);
				return c;
			}
		} catch (BranchNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName() {
		
		return delegateRepository.getRemoteNames().iterator().next();
	}
	
	public org.eclipse.jgit.lib.Repository getRepository() {
		return delegateRepository;
	}
	
}
