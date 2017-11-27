package org.svis.lib.repository.repo.git;

import static org.svis.lib.repository.repo.api.Operation.ChangeOperation.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.svis.lib.repository.repo.api.Operation;

public class GitOperation implements Operation {

	private static Map<ChangeType, ChangeOperation> gitToApiOperationMapping = new HashMap<ChangeType, ChangeOperation>();
	
	static {
		gitToApiOperationMapping.put(ChangeType.ADD, ADD);
		gitToApiOperationMapping.put(ChangeType.COPY, COPY);
		gitToApiOperationMapping.put(ChangeType.DELETE, DELETE);
		gitToApiOperationMapping.put(ChangeType.MODIFY, MODIFY);
		gitToApiOperationMapping.put(ChangeType.RENAME, RENAME);
	}
	
	private String description;
	private ChangeOperation operationType;
	private String oldPath;
	private String newPath;

	public GitOperation(String description, String oldPath, String newPath, ChangeType operationType) {
		this.description = description;
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.operationType = gitToApiOperationMapping.get(operationType);
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ChangeOperation typeOfOperation() {
		return operationType;
	}

	@Override
	public String toString() {
		return String.format("GitOperation [operationType=%s, description=%s]", operationType, description);
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
	public FileType getFileType() {
		return FileType.FILE; //TODO this does not anticipate other types like links
	}
	
}