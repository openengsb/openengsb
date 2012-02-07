package org.openengsb.infrastructure.ldap.internal;

public class MissingOrderException extends Exception {

    private static final long serialVersionUID = 5956010048635023889L;

    public MissingOrderException() {
        super();
    }

    public MissingOrderException(String message) {
        super(message);
    }

    public MissingOrderException(Throwable cause) {
        super(cause);
    }

    public MissingOrderException(String message, Throwable cause) {
        super(message, cause);
    }

}
