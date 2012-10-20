package org.openengsb.infrastructure.ldap.internal;

public class InconsistentDITException extends Exception {

    private static final long serialVersionUID = -6522553249268292980L;

    public InconsistentDITException() {
        super();
    }

    public InconsistentDITException(String message, Throwable cause) {
        super(message, cause);
    }

    public InconsistentDITException(String message) {
        super(message);
    }

    public InconsistentDITException(Throwable cause) {
        super(cause);
    }

}
