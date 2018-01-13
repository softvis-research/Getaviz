package org.svis.lib.repository.repo.svn;

import static org.svis.lib.repository.repo.api.Operation.ChangeOperation.ADD;
import static org.svis.lib.repository.repo.api.Operation.ChangeOperation.DELETE;
import static org.svis.lib.repository.repo.api.Operation.ChangeOperation.MODIFY;
import static org.svis.lib.repository.repo.api.Operation.ChangeOperation.RENAME;
import static org.svis.lib.repository.repo.api.Operation.FileType.*;

import java.util.HashMap;
import java.util.Map;

import org.svis.lib.repository.repo.api.Operation;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;

public class SubversionOperation implements Operation {

	private static Map<Character, ChangeOperation> svnOperationToChangeOp = new HashMap<Character, ChangeOperation>();
	private static Map<SVNNodeKind, FileType> svnFileTypesMapping = new HashMap<SVNNodeKind, FileType>();
	
	static {
		svnOperationToChangeOp.put(SVNLogEntryPath.TYPE_ADDED, ADD);
		svnOperationToChangeOp.put(SVNLogEntryPath.TYPE_DELETED, DELETE);
		svnOperationToChangeOp.put(SVNLogEntryPath.TYPE_MODIFIED, MODIFY);
		svnOperationToChangeOp.put(SVNLogEntryPath.TYPE_REPLACED, RENAME);
		svnFileTypesMapping.put(SVNNodeKind.DIR, DIR);
		svnFileTypesMapping.put(SVNNodeKind.FILE, FILE);
		svnFileTypesMapping.put(SVNNodeKind.NONE, OTHER);
		svnFileTypesMapping.put(SVNNodeKind.UNKNOWN, OTHER);
	}
	
	private String oldPath;
	private String newPath;
	private String description;
	private ChangeOperation op;
	private FileType fileType;
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String oldPath() {
		return oldPath;
	}

	@Override
	public String newPath() {
		return newPath;
	}

	@Override
	public ChangeOperation typeOfOperation() {
		return op;
	}
	
	@Override
	public FileType getFileType() {
		return fileType;
	}

	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setOp(char c) {
		this.op = svnOperationToChangeOp.get(c);
	}
	
	public void setFileType(SVNNodeKind svnFileType) {
		this.fileType = svnFileTypesMapping.get(svnFileType);
	}

}
