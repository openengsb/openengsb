package org.openengsb.core.security.model;

import java.util.Collection;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.PermissionSet;

public class GenericPermissionSet implements PermissionSet {

    private Collection<Permission> permissions;

    private Collection<PermissionSet> permissionSets;

    @Override
    public Collection<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(Collection<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Collection<PermissionSet> getPermissionSets() {
        return permissionSets;
    }

    @Override
    public void setPermissionSets(Collection<PermissionSet> permissionSets) {
        this.permissionSets = permissionSets;
    }

}
