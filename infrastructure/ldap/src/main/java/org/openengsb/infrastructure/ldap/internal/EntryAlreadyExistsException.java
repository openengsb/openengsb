package org.openengsb.infrastructure.ldap.internal;

import org.apache.directory.shared.ldap.model.entry.Entry;

public class EntryAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -3355338506299992562L;

    private Entry entry;
    
    public EntryAlreadyExistsException() {
        super();
    }
    
    public EntryAlreadyExistsException(Entry entry) {
        super();
        this.entry = entry;
    }

    public EntryAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntryAlreadyExistsException(String message) {
        super(message);
    }

    public EntryAlreadyExistsException(Throwable cause) {
        super(cause);
    }
    
    public void setEntry(Entry entry){
        this.entry = entry;
    }
    
    public Entry getEntry(){
        return entry;
    }

}
