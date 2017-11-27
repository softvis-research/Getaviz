package org.svis.lib.repository.repo.api.exception;

public class CommitsNotAvailableException extends ExceptionWrapper {
	private static final long serialVersionUID = 1L;

	public CommitsNotAvailableException(Exception other) {
		super(other);
	}

}
