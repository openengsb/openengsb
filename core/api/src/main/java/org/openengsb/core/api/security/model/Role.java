package org.openengsb.core.api.security.model;

import java.util.Collection;

public interface Role {

    String getName();

    Collection<? extends Role> getNestedRoles();

    Collection<? extends Permission> getAllPermissions();

}
