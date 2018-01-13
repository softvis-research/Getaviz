package org.svis.lib.repository.repo.git;

import org.eclipse.jgit.lib.PersonIdent;
import org.svis.lib.repository.repo.api.Author;

public class GitAuthor implements Author {

	private PersonIdent authorIdent;

	public GitAuthor(PersonIdent authorIdent) {
		this.authorIdent = authorIdent;
	}

	@Override
	public String getEmailAddress() {
		return authorIdent.getEmailAddress();
	}

	@Override
	public String getName() {
		return authorIdent.getName();
	}

	@Override
	public String toString() {
		return String.format("GitAuthor [getEmailAddress()=%s, getName()=%s]", getEmailAddress(), getName());
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if(obj instanceof Author){
			Author other = (Author) obj;
			isEqual = getEmailAddress().equals(other.getEmailAddress()) && getName().equals(other.getName());
		}
		return isEqual;
	}
	
	
}
