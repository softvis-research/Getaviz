package org.svis.lib.repository.repo.api.exception;

public class BranchNotAvailableException extends ExceptionWrapper {
	private static final long serialVersionUID = 1L;
	
	public BranchNotAvailableException(Exception other) {
		super(other);
	}
}
