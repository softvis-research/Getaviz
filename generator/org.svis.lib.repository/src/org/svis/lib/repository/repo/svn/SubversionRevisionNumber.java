package org.svis.lib.repository.repo.svn;

import org.eclipse.jgit.lib.ObjectId;
import org.svis.lib.repository.repo.api.Id;

public class SubversionRevisionNumber implements Id {

	private long revision; 
	
	public SubversionRevisionNumber(long revision) {
		this.revision = revision;
	}
	
	@Override
	public String getIdRepresentation() {
		return Long.valueOf(revision).toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (revision ^ (revision >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubversionRevisionNumber other = (SubversionRevisionNumber) obj;
		if (revision != other.revision)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("SubversionRevisionNumber [revision=%s]", revision);
	}

	@Override
	public ObjectId getObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
}
