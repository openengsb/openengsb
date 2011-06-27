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

package org.openengsb.core.security.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.security.UserUtils;
import org.openengsb.core.security.model.Role;
import org.openengsb.core.security.model.RoleAuthority;
import org.openengsb.core.security.model.SimpleUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserManagerImpl implements UserManager {

    private EntityManager entityManager;

    public UserManagerImpl() {
    }

    @Override
    public void createUser(UserDetails user) {
        if (userExists(user.getUsername())) {
            throw new UserExistsException("User with username: " + user.getUsername() + " already exists");
        }
        SimpleUser simpleUser = UserUtils.toSimpleUser(user);
        entityManager.persist(simpleUser);
        Collection<RoleAuthority> roles = UserUtils.filterCollectionByType(user.getAuthorities(), RoleAuthority.class);
        for (RoleAuthority r : roles) {
            Role role = entityManager.find(Role.class, r.getRole().getName());
            Collection<SimpleUser> members = role.getMembers();
            if (members == null) {
                members = new HashSet<SimpleUser>();
                role.setMembers(members);
            }
            members.add(simpleUser);
        }
    }

    @Override
    public void updateUser(UserDetails user) {
        if (!userExists(user.getUsername())) {
            throw new UsernameNotFoundException("User with username: " + user.getUsername() + " does not exists");
        }
        SimpleUser simpleUser = entityManager.find(SimpleUser.class, user.getUsername());
        simpleUser.setPassword(user.getPassword());
        simpleUser.setRoles(UserUtils.getRolesFromSpringUser(user));
        simpleUser.setPermissions(UserUtils.getPermissionsFromSpringUser(user));
        entityManager.merge(simpleUser);
    }

    @Override
    public void deleteUser(String username) {
        SimpleUser user = entityManager.find(SimpleUser.class, username);
        if (user == null) {
            throw new UsernameNotFoundException("User with username: " + username + " does not exists");
        }
        entityManager.remove(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        SimpleUser user = entityManager.find(SimpleUser.class, username);
        if (user == null) {
            throw new UsernameNotFoundException("user with name: " + username + " does not exist");
        }
        return UserUtils.toSpringUser(user);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean userExists(String username) {
        return entityManager.find(SimpleUser.class, username) != null;
    }

    @Override
    public List<String> getUsernameList() {
        TypedQuery<String> createQuery = entityManager.createQuery("SELECT u.username FROM SimpleUser u", String.class);
        return createQuery.getResultList();
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
