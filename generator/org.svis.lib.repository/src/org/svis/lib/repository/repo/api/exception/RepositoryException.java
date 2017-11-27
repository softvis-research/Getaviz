package org.svis.lib.repository.repo.api.exception;

public class RepositoryException extends ExceptionWrapper {
	private static final long serialVersionUID = 1L;

	public RepositoryException(Exception other) {
		super(other);
	}

}
