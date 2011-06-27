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
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@NamedQueries({ @NamedQuery(name = "listAllRoles", query = "SELECT r.name FROM Role r") })
@Entity
public class Role implements Serializable {

    private static final long serialVersionUID = 807120463662044757L;

    @Id
    private String name;
    @ManyToMany(cascade = { CascadeType.PERSIST })
    private Collection<Role> roles = new HashSet<Role>();
    @ManyToMany(cascade = { CascadeType.PERSIST })
    private Collection<Permission> permissions = new HashSet<Permission>();
    @ManyToMany(mappedBy = "roles")
    private Collection<SimpleUser> members = new HashSet<SimpleUser>();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Role(String name, Collection<Permission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public Role(String name, Collection<Role> roles, Collection<Permission> permissions) {
        this.name = name;
        this.roles = roles;
        updateUserRoleRelation(roles);
        this.permissions = permissions;
    }

    private void updateUserRoleRelation(Collection<Role> roles) {
        Iterable<SimpleUser> outdatedMembers = Iterables.filter(members, new Predicate<SimpleUser>() {
            @Override
            public boolean apply(SimpleUser input) {
                return !input.getRoles().contains(this);
            }
        });
        for (SimpleUser r : outdatedMembers) {
            r.addRole(this);
        }
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

    public Collection<SimpleUser> getMembers() {
        return members;
    }

    public void setMembers(Collection<SimpleUser> members) {
        this.members = members;
    }

    public Collection<Permission> getAllPermissions() {
        Collection<Permission> result = new ArrayList<Permission>();
        for (Role role : roles) {
            if (role == this) {
                throw new IllegalStateException("cyclic dependency detected");
            }
            result.addAll(role.getAllPermissions());
        }
        return result;
    }

    public void addMember(SimpleUser member) {
        members.add(member);
        if (!member.getRoles().contains(this)) {
            member.addRole(this);
        }
    }

    public void removeMember(SimpleUser member) {
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
        result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
        result = prime * result + ((roles == null) ? 0 : roles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Role other = (Role) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (permissions == null) {
            if (other.permissions != null)
                return false;
        } else if (!permissions.equals(other.permissions))
            return false;
        if (roles == null) {
            if (other.roles != null)
                return false;
        } else if (!roles.equals(other.roles))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Role: " + name;
    }

}
