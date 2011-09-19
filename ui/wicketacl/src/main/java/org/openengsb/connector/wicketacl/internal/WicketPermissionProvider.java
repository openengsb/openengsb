package org.openengsb.connector.wicketacl.internal;

import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.core.api.PermissionProvider;
import org.openengsb.core.api.security.model.Permission;

public class WicketPermissionProvider implements PermissionProvider {

    @Override
    public Class<? extends Permission> getPermissionClass(String className) {
        if (WicketPermission.class.getName().equals(className)) {
            return WicketPermission.class;
        }
        return null;
    }

    @Override
    public Class<?>[] getSupportedPermissionClasses() {
        return new Class<?>[]{ WicketPermission.class };
    }
}
