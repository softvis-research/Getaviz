package org.svis.lib.repository.repo.git;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.svis.lib.repository.repo.api.Id;

public class GitId implements Id {

	private AnyObjectId src;
	private String id;
		
	protected GitId(AnyObjectId src) {
		this.src = src;
		id = src.name();
	}
	
	@Override
	public String getIdRepresentation() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if(obj instanceof Id){
			Id other = (Id) obj;
			isEqual = id.equals(other.getIdRepresentation());
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public AnyObjectId getSrc() {
		return src;
	}

	//TODO: Kann entfernt werden, sobald FAMIX-Modelle direkt über den AST von VerveineJ erstellt werden können
	@Override
	public ObjectId getObjectId() {
		
		return src.toObjectId();
	}

}
