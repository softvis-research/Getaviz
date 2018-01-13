package org.svis.lib.repository.repo.api.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

public abstract class ExceptionWrapper extends Exception {

	private static final long serialVersionUID = 3085218184958719494L;
	
	protected Exception other;

	public ExceptionWrapper(Exception other) {
		this.other = other;
	}

	public int hashCode() {
		return other.hashCode();
	}

	public boolean equals(Object obj) {
		return other.equals(obj);
	}

	public String getMessage() {
		return other.getMessage();
	}

	public String getLocalizedMessage() {
		return other.getLocalizedMessage();
	}

	public Throwable getCause() {
		return other.getCause();
	}

	public Throwable initCause(Throwable cause) {
		return other.initCause(cause);
	}

	public String toString() {
		return other.toString();
	}

	public void printStackTrace() {
		other.printStackTrace();
	}

	public void printStackTrace(PrintStream s) {
		other.printStackTrace(s);
	}

	public void printStackTrace(PrintWriter s) {
		other.printStackTrace(s);
	}

	public StackTraceElement[] getStackTrace() {
		return other.getStackTrace();
	}

	public void setStackTrace(StackTraceElement[] stackTrace) {
		other.setStackTrace(stackTrace);
	}
	
}
