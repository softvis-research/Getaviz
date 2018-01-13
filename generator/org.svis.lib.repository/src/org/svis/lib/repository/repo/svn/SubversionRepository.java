package org.svis.lib.repository.repo.svn;

import static org.svis.lib.repository.repo.api.Constants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.svis.lib.repository.repo.api.Branch;
import org.svis.lib.repository.repo.api.Commit;
import org.svis.lib.repository.repo.api.Repository;
import org.svis.lib.repository.repo.api.Tag;
import org.svis.lib.repository.repo.api.exception.BranchNotAvailableException;
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException;
import org.svis.lib.repository.repo.api.exception.RepositoryNotAvailableException;
import org.svis.lib.repository.repo.api.exception.TagsNotAvailableException;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

public class SubversionRepository implements Repository {

//	@ValidateRegex(matches={"http://svn.apache.org/repos/asf/ant/test/even/long/path/trunk/", 
//							"http://svn.apache.org/repos/asf/ant/test/branches/feature_xyz",
//							"file:///svn.apache.org/repos/asf/ant/test/tags/1.0/",
//							"file:///home/dhaeb/dvl/J/iwi-3d-model-generator/generator/versioning/src/test/resources/test-repos/svn/trunk"})	
	private static final String PATTERN = "((?:" + SVN_SCHEME + "|" + HTTP_SCHEME + "|" + FILE_SCHEME + "):\\/\\/.+?\\/(:?.+?\\/)(" + TRUNK + "|" + BRANCHES + "|" + TAGS + ")\\/?(.*))";
	private static Pattern p = Pattern.compile(PATTERN);
	
	private String basePath;
	private Map<String, String> branchToPathMap = new HashMap<String, String>();
	private Map<String, String> tagToPathMap = new HashMap<String, String>();
	

	public SubversionRepository(String[] urls) throws RepositoryNotAvailableException {
		if(urls.length == 0) 
			throw new RepositoryNotAvailableException(new IllegalArgumentException("can't connect to an svn repository without knowing a path"));
		for(String url : urls){
			Matcher matcher = p.matcher(url);
			if(matcher.matches()){
				if(basePath == null) {
					basePath = url;
				} else {
					basePath = createNewBasePath(url);
				}
				switch(matcher.group(3)){
					case TRUNK:		
						branchToPathMap.put(TRUNK, url); 
						break;
					case BRANCHES:
						branchToPathMap.put(matcher.group(4), url); 
						break;
					case TAGS: 
						tagToPathMap.put(matcher.group(4), url);
				}
			} else {
				throw new RepositoryNotAvailableException(new IllegalArgumentException("can't include url " + url));
			}
		}
	}
	
	public boolean checkout(Commit commit) {
		return false;
	}
	
	public void reset() {
		
	}

	private String createNewBasePath(String url) {
		int capacity = Math.min(basePath.length(),url.length());
		StringBuilder builder = new StringBuilder(capacity);
		for(int i = 0; i < capacity; i++){
			char baseC = basePath.charAt(i);
			char urlC = url.charAt(i);
			if(baseC == urlC){
				builder.append(urlC);
			} else 
				break;
		}
		String newBasePath = builder.toString();
		return newBasePath;
	}
	
	public SubversionRepository(String basePath) throws RepositoryNotAvailableException {
		try {
			SVNRepository repository = new SubversionRepositoryInitializer(basePath).initRepository(CACHED);
			this.basePath = repository.getLocation().toDecodedString();
			long latestRevision = repository.getLatestRevision();
			branchToPathMap.put(TRUNK, basePath + "/" + TRUNK);
			searchEntriesFor(repository, latestRevision, branchToPathMap, BRANCHES);
			searchEntriesFor(repository, latestRevision, tagToPathMap, TAGS);
		} catch (Exception e) {
			throw new RepositoryNotAvailableException(e);
		}
	}

	private void searchEntriesFor(SVNRepository repository, long latestRevision, Map<String, String> pathmap, String searchable) throws SVNException {
		List<SVNDirEntry> currentEntries = new ArrayList<>();
		try {
			repository.getDir(searchable, latestRevision, false, currentEntries);
		} catch (SVNException e) {
			if(!SVNErrorCode.FS_NOT_FOUND.equals(e.getErrorMessage().getErrorCode())){
				throw e;
			}
		}
		addToMap(currentEntries, pathmap, basePath + "/" + searchable + "/");
	}
	
	private void addToMap(List<SVNDirEntry> currentEntries, Map<String, String> pathMap, String basePath) {
		for(SVNDirEntry entry : currentEntries){
			if(SVNNodeKind.DIR.equals(entry.getKind())){
				String entryName = entry.getName();
				pathMap.put(entryName, basePath + entryName);
			}
		}
	}

	@Override
	public String getPath() {
		return basePath;
	}
	
	@Override
	public Collection<Branch> getBranches() throws BranchNotAvailableException {
		List<Branch> resultList = new ArrayList<Branch>();
		for(String key : branchToPathMap.keySet()){
			resultList.add(getBranch(key));
		}
		return resultList;
	}

	@Override
	public Branch getBranch(String name) throws BranchNotAvailableException {
		String pathToRepo = branchToPathMap.get(name);
		if(pathToRepo == null){
			throw new BranchNotAvailableException(new IllegalArgumentException("there is no branch with name " + name));
		}
		return new SubversionBranch(name, pathToRepo);
	}

	@Override
	public Collection<Tag> getTags() throws TagsNotAvailableException {
		try {
			List<Tag> resultList = new ArrayList<Tag>();
			for(Entry<String, String> tag : tagToPathMap.entrySet()){
				resultList.add(new SubversionTag(tag.getKey(), tag.getValue()));
			}
			return resultList;
		} catch (BranchNotAvailableException e) {
			throw new TagsNotAvailableException(e);
		}
	}

	@Override
	public Branch getDefaultBranch() throws BranchNotAvailableException {
		return getBranch(TRUNK);
	}

	@Override
	public Commit getCommitById(String id) throws CommitsNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
