package org.openengsb.core.security.internal;

import org.openengsb.core.api.PermissionProvider;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.security.model.RootPermission;

public class RootPermissionProvider implements PermissionProvider {
    @Override
    public Class<? extends Permission> getPermissionClass(String className) {
        if (RootPermission.class.getName().equals(className)) {
            return RootPermission.class;
        }
        return null;
    }

    @Override
    public Class<?>[] getSupportedPermissionClasses() {
        return new Class<?>[]{ RootPermission.class };
    }
}
