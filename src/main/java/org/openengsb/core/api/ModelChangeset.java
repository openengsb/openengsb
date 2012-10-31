package org.openengsb.core.api;

import org.openengsb.core.api.model.OpenEngSBModel;

public class ModelChangeset {
    private final OpenEngSBModel[] created;
    private final OpenEngSBModel[] updated;
    private final OpenEngSBModel[] deleted;

    /**
     */
    public ModelChangeset(OpenEngSBModel[] created, OpenEngSBModel[] updated, OpenEngSBModel[] deleted) {
        this.created = created;
        this.updated = updated;
        this.deleted = deleted;
    }

    public OpenEngSBModel[] getCreated() {
        return created;
    }

    public OpenEngSBModel[] getUpdated() {
        return updated;
    }

    public OpenEngSBModel[] getDeleted() {
        return deleted;
    }
}
