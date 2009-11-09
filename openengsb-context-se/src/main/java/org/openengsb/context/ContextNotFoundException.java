package org.openengsb.context;

public class ContextNotFoundException extends RuntimeException {

	public ContextNotFoundException() {
	}

	public ContextNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContextNotFoundException(String message) {
		super(message);
	}

	public ContextNotFoundException(Throwable cause) {
		super(cause);
	}
}
