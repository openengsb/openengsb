/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.api.security;

import java.util.Collection;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.Role;
import org.springframework.security.core.GrantedAuthority;

/**
 * Service for managing roles for security purposes.
 * 
 * A role is used to create a collection of {@link Permission}s
 */
public interface RoleManager {

    /**
     * return a list of all existing roles
     */
    Collection<Role> findAllRoles();

    /**
     * find all users with the specified role and return a list of the usernames
     */
    Collection<String> findAllUsersWithRole(String roleName);

    /**
     * return a list of all permissions a role with that name grants. The result must also include permissions implied
     * by nested roles.
     */
    Collection<Permission> getAllPermissions(String rolename);

    /**
     * create a new role with the given permissions
     */
    void createRole(String name, Permission... permissions);

    /**
     * delete a role with the given name
     */
    void deleteRole(String name);

    /**
     * assign a role to a user
     */
    void addRoleToUser(String username, String rolename);

    /**
     * remove a role from a user
     */
    void removeRoleFromuser(String username, String rolename);

    /**
     * grants the role with the given name the desired Permissions
     */
    void addPermissionsToRole(String rolename, Permission... permission);

    /**
     * grants the user with the given name the desired Permissions
     */
    void addPermissionToUser(String username, Permission... permission);

    /**
     * removes permissions from the role with the given name
     */
    void removePermissionsFromRole(String rolename, Permission... permissions);

    /**
     * removes permissions from the user with the given name
     */
    void removePermissionsFromUser(String username, Permission... permissions);

    /**
     * converts the specific Role-implementation to a spring-security-compatible version
     */
    GrantedAuthority createRoleAuthority(String rolename);

}
