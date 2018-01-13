package org.svis.lib.repository.repo.api.exception;

public class RepositoryNotAvailableException extends ExceptionWrapper {
	private static final long serialVersionUID = 1L;

	public RepositoryNotAvailableException(Exception other) {
		super(other);
	}

}
