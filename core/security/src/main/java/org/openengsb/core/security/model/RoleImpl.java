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
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.Role;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@NamedQueries({
    @NamedQuery(name = "listAllRoles", query = "SELECT r FROM RoleImpl r"),
    @NamedQuery(name = "listUsersWithRole",
        query = "SELECT u.username FROM RoleImpl r JOIN r.members u WHERE r.name = :groupname")})
@Entity
public class RoleImpl implements Role, Serializable {

    private static final long serialVersionUID = 807120463662044757L;

    @Id
    private String name;

    @ManyToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
    private Collection<RoleImpl> nestedRoles = new HashSet<RoleImpl>();
    @ManyToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
    private Collection<AbstractPermission> permissions = new HashSet<AbstractPermission>();
    @ManyToMany(cascade = { CascadeType.PERSIST }, mappedBy = "roles")
    private Collection<UserImpl> members = new HashSet<UserImpl>();

    public RoleImpl() {
    }

    public RoleImpl(String name) {
        this.name = name;
    }

    public RoleImpl(String name, Collection<AbstractPermission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public RoleImpl(String name, Collection<RoleImpl> roles, Collection<AbstractPermission> permissions) {
        this.name = name;
        nestedRoles = roles;
        updateUserRoleRelation(roles);
        this.permissions = permissions;
    }

    private void updateUserRoleRelation(Collection<? extends Role> roles) {
        Iterable<UserImpl> outdatedMembers = Iterables.filter(members, new Predicate<UserImpl>() {
            @Override
            public boolean apply(UserImpl input) {
                return !input.getRoles().contains(this);
            }
        });
        for (UserImpl r : outdatedMembers) {
            r.addRole(this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<AbstractPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<AbstractPermission> permissions) {
        this.permissions = permissions;
    }

    public Collection<UserImpl> getMembers() {
        return members;
    }

    public void setMembers(Collection<UserImpl> members) {
        this.members = members;
    }

    @Override
    public Collection<Permission> getAllPermissions() {
        Collection<Permission> result = new ArrayList<Permission>(permissions);
        for (Role role : nestedRoles) {
            if (role == this) {
                throw new IllegalStateException("cyclic dependency detected");
            }
            result.addAll(role.getAllPermissions());
        }
        return result;
    }

    public void addMember(UserImpl member) {
        members.add(member);
        if (!member.getRoles().contains(this)) {
            member.addRole(this);
        }
    }

    public void removeMember(UserImpl member) {
        members.add(member);
        if (member.getRoles().contains(this)) {
            member.removeRole(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RoleImpl other = (RoleImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Role: " + name;
    }

    @Override
    public Collection<? extends Role> getNestedRoles() {
        return nestedRoles;
    }

    @SuppressWarnings("unchecked")
    public void setNestedRoles(Collection<? extends Role> nestedRoles) {
        this.nestedRoles = (Collection<RoleImpl>) nestedRoles;
    }

}
