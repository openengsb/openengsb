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

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Table(name = "SIMPLEUSER")
@Entity
public class SimpleUser {

    @Id
    private String username;
    private String password;
    @ManyToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
    private Collection<RoleImpl> roles = new HashSet<RoleImpl>();
    @OneToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
    private Collection<AbstractPermission> permissions = new HashSet<AbstractPermission>();

    public SimpleUser(String username) {
        this.username = username;
    }

    public SimpleUser(String username, String password) {
        this(username);
        this.password = password;
    }

    public SimpleUser() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Collection<RoleImpl> getRoles() {
        return roles;
    }

    public void setRoles(Collection<RoleImpl> roles) {
        this.roles = roles;
        Iterable<RoleImpl> outdatedRoles = Iterables.filter(roles, new Predicate<RoleImpl>() {
            @Override
            public boolean apply(RoleImpl input) {
                return !input.getMembers().contains(this);
            }
        });
        for (RoleImpl r : outdatedRoles) {
            r.addMember(this);
        }
    }

    public Collection<AbstractPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<AbstractPermission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return username + " [password hidden] " + roles;
    }

    public void addRole(RoleImpl role) {
        roles.add(role);
        if (!role.getMembers().contains(this)) {
            role.addMember(this);
        }
    }

    public void removeRole(RoleImpl role) {
        roles.remove(role);
        if (role.getMembers().contains(this)) {
            role.removeMember(this);
        }
    }

}
