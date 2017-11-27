package org.svis.lib.repository.repo.svn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.svis.lib.repository.repo.api.Branch;
import org.svis.lib.repository.repo.api.Commit;
import org.svis.lib.repository.repo.api.exception.BranchNotAvailableException;
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;

public class SubversionBranch implements Branch {

    private SVNRepository repo;
	private String name;
	
	public SubversionBranch(String name, String pathToRepo) throws BranchNotAvailableException {
        this.name = name;
		try {
			this.repo = new SubversionRepositoryInitializer(pathToRepo).initRepository();
		} catch (Exception e) {
			throw new BranchNotAvailableException(e);
		}
    }
    
    @Override
    public List<Commit> getCommits() throws CommitsNotAvailableException {
    	return getCommits(0);
    }
   
    public List<Commit> getCommits(long startRevision) throws CommitsNotAvailableException {
    	List<Commit> result = new ArrayList<Commit>();
    	try {
			Collection<?> col = repo.log( new String[] { "" } , null , startRevision, repo.getLatestRevision() , true , true );
			Iterator<?> colIt = col.iterator();
			while(colIt.hasNext()){
				SVNLogEntry entry = (SVNLogEntry) colIt.next();
				result.add(new SubversionCommit(entry, repo, name));
			}
		} catch (SVNException e) {
			throw new CommitsNotAvailableException(e);
		}
        Collections.sort(result);
        return result;
    }


    @Override
    public Commit getLastCommit() throws CommitsNotAvailableException {
        try {
			return getCommits(getCurrentRepositoryVersion()).get(0);
		} catch (SVNException e) {
			throw new CommitsNotAvailableException(e);
		}
    }

	private long getCurrentRepositoryVersion() throws SVNException {
		return repo.info("", repo.getLatestRevision()).getRevision();
	}
 
    @Override
    public String getName() {
        return name;
    }

	@Override
	public Commit getCommit(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Commit> getCommits(Date startDate, Date endDate) throws CommitsNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Commit> getCommits(int numberOfCommits) throws CommitsNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}
}
