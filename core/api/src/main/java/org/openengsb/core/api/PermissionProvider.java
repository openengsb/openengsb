package org.openengsb.core.api;

import org.openengsb.core.api.security.model.Permission;

public interface PermissionProvider {

    Class<? extends Permission> getPermissionClass(String className);

    Class<?>[] getSupportedPermissionClasses();

}
