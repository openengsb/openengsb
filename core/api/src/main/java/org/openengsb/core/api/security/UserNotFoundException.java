package org.openengsb.core.api.security;

public class UserNotFoundException extends OpenEngSBSecurityException {

    private static final long serialVersionUID = 6901506350918489164L;

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message, Throwable cause) {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

}
