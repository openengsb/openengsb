package org.openengsb.core.security.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

@Entity
public class PermissionSetData extends BeanData {

    @ManyToMany
    private Collection<PermissionData> permissions;

    @ManyToMany
    private Collection<PermissionSetData> permissionSets;

    public Collection<PermissionData> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<PermissionData> permissions) {
        this.permissions = permissions;
    }

    public Collection<PermissionSetData> getPermissionSets() {
        return permissionSets;
    }

    public void setPermissionSets(Collection<PermissionSetData> permissionSets) {
        this.permissionSets = permissionSets;
    }

}
