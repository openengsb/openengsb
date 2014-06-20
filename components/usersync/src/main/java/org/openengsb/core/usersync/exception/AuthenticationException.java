package org.openengsb.core.usersync.exception;

@SuppressWarnings("serial")
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String string) {
        super(string);
    }

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
