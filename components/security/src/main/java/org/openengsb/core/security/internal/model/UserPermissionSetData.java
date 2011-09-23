package org.openengsb.core.security.internal.model;

import javax.persistence.Entity;

@Entity
public class UserPermissionSetData extends PermissionSetData {

    public UserPermissionSetData() {
    }

    public UserPermissionSetData(String id) {
        super(id);
    }

}
