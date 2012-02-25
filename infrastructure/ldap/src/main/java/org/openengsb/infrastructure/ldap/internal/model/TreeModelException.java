package org.openengsb.infrastructure.ldap.internal.model;

public class TreeModelException extends RuntimeException {

    private static final long serialVersionUID = 344453518555807487L;

    public TreeModelException() {
        super();
    }

    public TreeModelException(String message) {
        super(message);
    }

    public TreeModelException(Throwable cause) {
        super(cause);
    }

    public TreeModelException(String message, Throwable cause) {
        super(message, cause);
    }

}
