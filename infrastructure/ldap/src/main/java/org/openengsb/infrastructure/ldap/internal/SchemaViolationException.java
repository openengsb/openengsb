package org.openengsb.infrastructure.ldap.internal;

/**
 * Indicates inconsistencies concerning the schema.
 * */
public class SchemaViolationException extends Exception {

    private static final long serialVersionUID = -3946360633686495868L;

    public SchemaViolationException() {
        super();
    }

    public SchemaViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaViolationException(String message) {
        super(message);
    }

    public SchemaViolationException(Throwable cause) {
        super(cause);
    }

}
