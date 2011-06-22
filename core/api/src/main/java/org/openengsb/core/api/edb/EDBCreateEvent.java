package org.openengsb.core.api.edb;

import org.openengsb.core.api.Event;
import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * Represents a create Event. Try to insert an OpenEngSBModel object into the EDB. The saving name defines under which
 * name the model should be saved. Because this name should be unique, it is recommended to use a connector name + id
 * for the name.
 */
public class EDBCreateEvent extends Event {

    private OpenEngSBModel model;
    private String savingName;
    private String committer;
    private String role;

    public EDBCreateEvent(OpenEngSBModel model, String savingName, String committer, String role) {
        this.model = model;
        this.savingName = savingName;
        this.committer = committer;
        this.role = role;
    }

    public OpenEngSBModel getModel() {
        return model;
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
