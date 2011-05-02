package org.openengsb.core.api.security;

public class MessageAuthenticationException extends Exception {
    private static final long serialVersionUID = 1L;

    public MessageAuthenticationException() {
    }

    public MessageAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageAuthenticationException(String message) {
        super(message);
    }

    public MessageAuthenticationException(Throwable cause) {
        super(cause);
    }

}
