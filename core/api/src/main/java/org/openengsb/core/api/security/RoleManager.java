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

public interface RoleManager {

    Collection<Role> findAllRoles();

    Collection<String> findAllUsersWithRole(String roleName);

    Collection<Permission> getAllPermissions(String rolename);

    void createRole(String name, Permission... permissions);

    void deleteRole(String name);

    void addRoleToUser(String username, String rolename);

    void removeRoleFromuser(String username, String rolename);

    void addPermissionsToRole(String rolename, Permission... permission);

    void addPermissionToUser(String username, Permission... permission);

    void removePermissionsFromRole(String rolename, Permission... permissions);

    void removePermissionsFromUser(String username, Permission... permissions);

    GrantedAuthority createRoleAuthority(String rolename);

}
