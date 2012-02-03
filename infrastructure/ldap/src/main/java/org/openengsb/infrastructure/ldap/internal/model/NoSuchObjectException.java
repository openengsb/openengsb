package org.openengsb.infrastructure.ldap.internal.model;

import org.apache.directory.shared.ldap.model.name.Dn;

public class NoSuchObjectException extends Exception {

    private static final long serialVersionUID = 4929321966265341536L;
    
    private Dn lowestMatchedDn;

    public NoSuchObjectException() {
        super();
    }
    
    public NoSuchObjectException(Dn dn) {
        super();
        this.lowestMatchedDn = dn;
    }

    public NoSuchObjectException(String message) {
        super(message);
    }

    public NoSuchObjectException(Throwable cause) {
        super(cause);
    }

    public NoSuchObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public Dn getLowestMatchedDn() {
        return lowestMatchedDn;
    }

    public void setLowestMatchedDn(Dn dn) {
        this.lowestMatchedDn = dn;
    }

}
