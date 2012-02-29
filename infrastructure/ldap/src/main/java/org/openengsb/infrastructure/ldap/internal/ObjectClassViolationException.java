package org.openengsb.infrastructure.ldap.internal;

public class ObjectClassViolationException extends RuntimeException{

    private static final long serialVersionUID = 3329558001005307519L;

    public ObjectClassViolationException() {
        super();
    }

    public ObjectClassViolationException(String message) {
        super(message);
    }

    public ObjectClassViolationException(Throwable cause) {
        super(cause);
    }

    public ObjectClassViolationException(String message, Throwable cause) {
        super(message, cause);
    }

}
