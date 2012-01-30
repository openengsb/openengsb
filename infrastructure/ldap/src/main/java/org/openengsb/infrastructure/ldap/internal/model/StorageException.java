package org.openengsb.infrastructure.ldap.internal.model;

public class StorageException extends Exception {

    private static final long serialVersionUID = -4954869104341194984L;

    public StorageException() {
        super();
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
