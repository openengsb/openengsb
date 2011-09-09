package org.openengsb.core.security;

public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = -4110570969981216915L;

    public AccessDeniedException() {
        super();
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

}
