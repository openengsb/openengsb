package org.openengsb.core.usersync.exception;

public class SynchronizationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SynchronizationException() {
        super();
    }

    public SynchronizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SynchronizationException(String message) {
        super(message);
    }

    public SynchronizationException(Throwable cause) {
        super(cause);
    }

}
