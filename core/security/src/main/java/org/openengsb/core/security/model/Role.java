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

package org.openengsb.core.security.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Role implements Serializable {

    private static final long serialVersionUID = 807120463662044757L;

    private String name;
    private Collection<Role> roles;
    private Collection<Permission> permissions;

    public Role() {
    }

    public Role(String name, Collection<Permission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public Role(String name, Collection<Role> roles, Collection<Permission> permissions) {
        this.name = name;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<Permission> permissions) {
        this.permissions = permissions;
    }

    public Collection<Permission> getAllPermissions() {
        Collection<Permission> result = new ArrayList<Permission>();
        if (permissions != null) {
            result.addAll(permissions);
        }
        for (Role role : roles) {
            if (role == this) {
                throw new IllegalStateException("cyclic dependency detected");
            }
            result.addAll(role.getAllPermissions());
        }
        return result;
    }

}
