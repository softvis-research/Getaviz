package org.svis.lib.repository.repo.git

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.svis.lib.repository.repo.api.Branch
import org.svis.lib.repository.repo.api.Commit
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException
import org.svis.lib.repository.repo.util.UniqueArrayList
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.Date
import org.eclipse.jgit.api.Git

class GitBranch implements Branch {

	@Accessors(PUBLIC_GETTER) String name
	var GitRepository repo
	var Ref branchRef
	val _allCommits = new HashMap<String,Commit>

	new(GitRepository repo, Ref branchRef, String branchName) {
		this.repo = repo
		this.branchRef = branchRef
		this.name = branchName
	}
	
	override String getName() {
		return this.name
	}
	
	override List<Commit> getCommits(int numberOfCommits) {
		val git = new Git(repo.repository)
		val commits = git.log.call
		
		val List<Commit> result = newLinkedList
		val size = git.log.call.size
		val diff = size / (numberOfCommits - 1)
		commits.forEach[commit, i|
			if (size < numberOfCommits - 1 || i == 0 || i == size || (i % diff == 0)) {
				val gitCommit = new GitCommit(repo.delegateRepository, commit)
				result += gitCommit
			}
		]
		return result.reverse
	}
	
	override List<Commit> getCommits(Date startDate, Date endDate) throws CommitsNotAvailableException {
		var result = new UniqueArrayList<Commit>()
		try {
			val repository = repo.delegateRepository
			val commit = repository.lastCommit
			
			result  += new GitCommit(repository, commit)
			commit.addParentCommits(startDate, endDate)
			result.addAll(_allCommits.values)
			Collections::sort(result)
		} catch (IOException e) {
			throw new CommitsNotAvailableException(e)
		}
		return result
	}

	override List<Commit> getCommits() throws CommitsNotAvailableException {
		var result = new UniqueArrayList<Commit>();
		try {
			val repository = repo.delegateRepository
			val commit = repository.lastCommit
		
			val e = new GitCommit(repository, commit)
			result += e
//			result.addAll(addParentCommits(commit));
			commit.addParentCommits
			result.addAll(_allCommits.values)
			Collections::sort(result)
		} catch (IOException e) {
			throw new CommitsNotAvailableException(e)
		}
		return result
	}
	
	override Commit getCommit(String id) {
		var result = new ArrayList<Commit>()
		try {
			var repository = repo.delegateRepository
			var walk = new RevWalk(repository)
			var commit = walk.parseCommit(ObjectId.fromString(id))
			walk.close
			val e = new GitCommit(repository, commit);
			result += e
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return result.get(0)
	}

	def getLastCommit(Repository repository) throws IOException {
		val walk = new RevWalk(repository)
		val commit = walk.parseCommit(branchRef.objectId)
		walk.close
		return commit
	}
	
	def List<Commit> addParentCommits(RevCommit commit, Date startDate, Date endDate) throws IOException {
//		List<Commit> result = new ArrayList<Commit>();	
		println("commit time :" + commit.commitTime)
		println("when: " + commit.committerIdent.when)
			
		val parents = commit.parents
		if (parents !== null) {
			for (parent : parents) {	
				var parentCommit = parent
				if(!_allCommits.containsKey(parentCommit.getName())){
//					System.out.println(i+" _ " + parentCommit.getName()+ " hinzugefügt.");
					var revWalk = new RevWalk(repo.delegateRepository)
					parentCommit = revWalk.parseCommit(parent.id)
					revWalk.close				
//					result.add(new GitCommit(repo.getDelegateRepository(), parentCommit));
				
					_allCommits.put(parentCommit.getName(),new GitCommit(repo.delegateRepository, parentCommit))
//					System.out.println("ParentCommits: "+parentCommit.getParentCount());
					if (parentCommit.committerIdent.when.after(startDate) && parentCommit.committerIdent.when.before(endDate)){
						parentCommit.addParentCommits(startDate, endDate)
					}
				}
//				result.addAll(addParentCommits(parentCommit));
			}
		}
		return new ArrayList<Commit>(_allCommits.values)
	}
	
	def List<Commit> addParentCommits(RevCommit commit) throws IOException {
//		List<Commit> result = new ArrayList<Commit>();	
			
		val parents = commit.parents
		if (parents !== null) {
			for (parent : parents) {	
				var parentCommit = parent
				if(!_allCommits.containsKey(parentCommit.getName())){
//					System.out.println(i+" _ " + parentCommit.getName()+ " hinzugefügt.");
					var revWalk = new RevWalk(repo.delegateRepository)
					parentCommit = revWalk.parseCommit(parent.id)
					revWalk.close				
//					result.add(new GitCommit(repo.getDelegateRepository(), parentCommit));
				
					_allCommits.put(parentCommit.getName(),new GitCommit(repo.delegateRepository, parentCommit))
//					System.out.println("ParentCommits: "+parentCommit.getParentCount());
					parentCommit.addParentCommits
				}
//				result.addAll(addParentCommits(parentCommit));
			}
		}
		return new ArrayList<Commit>(_allCommits.values)
	}

	override getLastCommit() throws CommitsNotAvailableException {
		val repository = repo.delegateRepository
		try {
			return new GitCommit(repository, getLastCommit(repository))
		} catch (IOException e) {
			throw new CommitsNotAvailableException(e)
		}
	}

	override toString() {
		return "Git-Branch named: " + name
	}
}
