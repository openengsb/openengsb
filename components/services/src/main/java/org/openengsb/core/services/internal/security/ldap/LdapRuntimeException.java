package org.openengsb.core.services.internal.security.ldap;

public class LdapRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 6267165144631084590L;

    public LdapRuntimeException() {
        super();
    }

    public LdapRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LdapRuntimeException(String message) {
        super(message);
    }

    public LdapRuntimeException(Throwable cause) {
        super(cause);
    }

}
