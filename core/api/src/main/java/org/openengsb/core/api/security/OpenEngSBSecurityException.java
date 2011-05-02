package org.openengsb.core.api.security;

public abstract class OpenEngSBSecurityException extends Exception {

    private static final long serialVersionUID = 1L;

    public OpenEngSBSecurityException() {
    }

    public OpenEngSBSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenEngSBSecurityException(String message) {
        super(message);
    }

    public OpenEngSBSecurityException(Throwable cause) {
        super(cause);
    }

}
