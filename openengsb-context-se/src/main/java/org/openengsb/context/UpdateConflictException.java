package org.openengsb.context;

public class UpdateConflictException extends RuntimeException {

	public UpdateConflictException() {
	}

	public UpdateConflictException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpdateConflictException(String message) {
		super(message);
	}

	public UpdateConflictException(Throwable cause) {
		super(cause);
	}
}
