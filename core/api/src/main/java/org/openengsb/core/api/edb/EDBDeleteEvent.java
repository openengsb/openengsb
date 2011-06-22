package org.openengsb.core.api.edb;

import org.openengsb.core.api.Event;

/**
 * Represents a delete Event. Try to delete an OpenEngSBModel object in the EDB under the given name.
 */
public class EDBDeleteEvent extends Event {
    
    private String savingName;
    private String committer;
    private String role;

    public EDBDeleteEvent(String savingName, String committer, String role) {
        this.savingName = savingName;
        this.committer = committer;
        this.role = role;
    }

    public String getSavingName() {
        return savingName;
    }
    
    public String getCommitter() {
        return committer;
    }
    
    public String getRole() {
        return role;
    }

}
