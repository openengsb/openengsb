package org.openengsb.infrastructure.ldap.internal;

import org.apache.directory.shared.ldap.model.name.Dn;

public class NoSuchNodeException extends Exception {

    private static final long serialVersionUID = 4929321966265341536L;
    
    private Dn dn;

    public NoSuchNodeException() {
        super();
    }
    
    public NoSuchNodeException(Dn dn) {
        super();
        this.dn = dn;
    }

    public NoSuchNodeException(String message) {
        super(message);
    }

    public NoSuchNodeException(Throwable cause) {
        super(cause);
    }

    public NoSuchNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public Dn getDn() {
        return dn;
    }

    public void setDn(Dn dn) {
        this.dn = dn;
    }

}
