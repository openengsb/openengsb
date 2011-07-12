package org.openengsb.core.api.security;

import java.util.List;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.Role;
import org.springframework.security.core.GrantedAuthority;

public interface RoleManager {

    List<Role> findAllRoles();

    List<Role> findAllGlobalRoles();

    List<String> findAllUsersWithRole(String roleName);

    void createRole(String name, Permission... permissions);

    void deleteRole(String name);

    void addRoleToUser(String username, String rolename);

    void removeRoleFromuser(String username, String rolename);

    List<Permission> getAllPermissions(String rolename);

    void addPermissionsToRole(String rolename, Permission... permission);

    void addPermissionToUser(String username, Permission... permission);

    void removePermissionsFromRole(String rolename, Permission... permissions);

    void removePermissionsFromUser(String username, Permission... permissions);

    GrantedAuthority createRoleAuthority(String rolename);

}
