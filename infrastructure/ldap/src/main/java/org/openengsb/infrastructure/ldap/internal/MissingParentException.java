package org.openengsb.infrastructure.ldap.internal;

import org.apache.directory.shared.ldap.model.name.Dn;

public class MissingParentException extends Exception {

    private static final long serialVersionUID = 8263832471990241311L;

    private Dn lowestMatchedDn;
    
    public MissingParentException() {
        super();
    }
    
    /**
     * @param lowestMatchedDn the last existing Dn in the hierarchy.
     */
    public MissingParentException(Dn lowestMatchedDn) {
        this.lowestMatchedDn = lowestMatchedDn;
    }

    public MissingParentException(String message) {
        super(message);
    }

    public MissingParentException(Throwable cause) {
        super(cause);
    }

    public MissingParentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return the last existing Dn in the hierarchy.
     */
    public Dn getLowestMatchedDn() {
        return lowestMatchedDn;
    }

    /**
     * @param lowestMatchedDn the last existing Dn in the hierarchy.
     */
    public void setLowestMatchedDn(Dn dn) {
        this.lowestMatchedDn = dn;
    }
    
}
