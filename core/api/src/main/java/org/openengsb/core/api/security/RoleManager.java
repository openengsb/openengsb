package org.openengsb.core.api.security;

import java.util.List;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.Role;
import org.springframework.security.core.GrantedAuthority;

public interface RoleManager {

    List<Role> findAllRoles();

    List<Role> findAllGlobalRoles();

    List<Role> findAllRoles(String context);

    List<String> findAllUsersWithRole(String roleName);

    List<String> findAllUsersWithRole(String roleName, String context);

    void createRole(String name, Permission... permissions);

    void createRole(String name, String context, Permission... permissions);

    void deleteRole(String name);

    void deleteRole(String name, String context);

    void addRoleToUser(String username, String rolename);

    void addRoleToUser(String username, String rolename, String context);

    void removeRoleFromuser(String username, String rolename);

    void removeRoleFromuser(String username, String rolename, String context);

    List<Permission> getAllPermissions(String rolename);

    List<Permission> getAllPermissions(String rolename, String context);

    void addPermissionsToRole(String rolename, Permission... permission);

    void addPermissionToUser(String username, Permission... permission);

    void removePermissionsFromRole(String rolename, Permission... permissions);

    void removePermissionsFromUser(String username, Permission... permissions);

    GrantedAuthority createRoleAuthority(String rolename);

}
