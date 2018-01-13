package org.svis.lib.repository.repo.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.svis.lib.repository.repo.api.Constants;
import org.svis.lib.repository.repo.api.Repository;
import org.svis.lib.repository.repo.api.exception.RepositoryNotAvailableException;
import org.svis.lib.repository.repo.git.GitRepository;
import org.svis.lib.repository.repo.svn.SubversionRepository;

public class RepositoryFactorys {

	public interface RepositoryFactory {
		Repository createRepoFrom(String url) throws RepositoryNotAvailableException;
	}
	
	public enum RepositoryType {
		GIT, 
		SUBVERSION;
	}
	
	private static Map<RepositoryType, RepositoryFactory> repoCreatorMap = new HashMap<RepositoryType, RepositoryFactory>();
	
	static {
		repoCreatorMap.put(RepositoryType.SUBVERSION, new RepositoryFactory() {
			
			@Override
			public Repository createRepoFrom(String url) throws RepositoryNotAvailableException {
				String separatorAsString = String.valueOf(Constants.SEPARATOR);
				if(url.contains(separatorAsString)){
					return new SubversionRepository(url.split(Pattern.quote(separatorAsString)));
				} else {
					return new SubversionRepository(url);
				}
			}
		});
		
		repoCreatorMap.put(RepositoryType.GIT, new RepositoryFactory() {
			
			@Override
			public Repository createRepoFrom(String url) throws RepositoryNotAvailableException {
				return new GitRepository(url);
			}
		});
	}
	
	public static Repository createRepoFromUrl(String url, RepositoryType type) throws RepositoryNotAvailableException{
		return repoCreatorMap.get(type).createRepoFrom(url);
	}
	
}
