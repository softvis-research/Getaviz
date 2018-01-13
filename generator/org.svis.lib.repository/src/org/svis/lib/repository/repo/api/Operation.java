package org.svis.lib.repository.repo.api;

/**
 * Operations represent changes on file level from one commit to another.
 * Contains a typical diff representation for a specific file in 
 * addition to the change type of the performed operation. 
 * 
 * @author Dan Häberlein 
 *
 */
public interface Operation {

	String getDescription();
	String oldPath();
	String newPath();
	ChangeOperation typeOfOperation();
	FileType getFileType();
	
	public static enum FileType {
		FILE,
		DIR, 
		OTHER;
	}
	
	public static enum ChangeOperation {
		ADD,
		MODIFY,
		DELETE,
		RENAME,
		COPY;
	}
}
