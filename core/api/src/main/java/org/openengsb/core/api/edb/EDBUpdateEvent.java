package org.openengsb.core.api.edb;

import org.openengsb.core.api.Event;
import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * Represents a update Event. Try to update an OpenEngSBModel object in the EDB under the given name.
 */
public class EDBUpdateEvent extends Event {

    private OpenEngSBModel model;
    private String savingName;
    private String committer;
    private String role;

    public EDBUpdateEvent(OpenEngSBModel model, String savingName, String committer, String role) {
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
