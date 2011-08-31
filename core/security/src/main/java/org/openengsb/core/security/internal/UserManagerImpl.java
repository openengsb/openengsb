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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.security.UserUtils;
import org.openengsb.core.security.model.UserImpl;
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
        UserImpl simpleUser = UserUtils.toSimpleUser(user);
        entityManager.persist(simpleUser);
    }

    @Override
    public void updateUser(UserDetails user) {
        if (!userExists(user.getUsername())) {
            throw new UsernameNotFoundException("User with username: " + user.getUsername() + " does not exists");
        }
        UserImpl simpleUser = entityManager.find(UserImpl.class, user.getUsername());
        simpleUser.setPassword(user.getPassword());
        simpleUser.setRoles(UserUtils.getRolesFromSpringUser(user.getAuthorities()));
        simpleUser.setPermissions(UserUtils.getPermissionsFromSpringUser(user.getAuthorities()));
        entityManager.merge(simpleUser);
    }

    @Override
    public void deleteUser(String username) {
        UserImpl user = entityManager.find(UserImpl.class, username);
        if (user == null) {
            throw new UsernameNotFoundException("User with username: " + username + " does not exists");
        }
        entityManager.remove(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserImpl user = entityManager.find(UserImpl.class, username);
        if (user == null) {
            throw new UsernameNotFoundException("user with name: " + username + " does not exist");
        }
        return UserUtils.toSpringUser(user);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // not implemented since we don't use the usermanager that way.
        // rethink this after OPENENGSB-200 is fixed
        throw new UnsupportedOperationException("Not implemented, use updateUser()");
    }

    @Override
    public boolean userExists(String username) {
        return entityManager.find(UserImpl.class, username) != null;
    }

    @Override
    public List<String> getUsernameList() {
        TypedQuery<String> createQuery = entityManager.createQuery("SELECT u.username FROM UserImpl u", String.class);
        return createQuery.getResultList();
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
