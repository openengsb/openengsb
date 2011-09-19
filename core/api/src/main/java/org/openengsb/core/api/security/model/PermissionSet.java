package org.openengsb.core.api.security.model;

import java.util.Collection;

public interface PermissionSet {

    Collection<Permission> getPermissions();

    Collection<PermissionSet> getPermissionSets();

    void setPermissions(Collection<Permission> permissions);

    void setPermissionSets(Collection<PermissionSet> permissionSets);

}
