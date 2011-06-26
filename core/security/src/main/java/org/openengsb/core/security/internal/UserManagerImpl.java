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
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.User;
import org.openengsb.core.security.model.SimpleUser;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class UserManagerImpl implements UserManager {

    private EntityManager entityManager;

    public UserManagerImpl() {
    }

    @Override
    public void createUser(User user) {
        if (userNameExists(user.getUsername())) {
            throw new UserExistsException("User with username: " + user.getUsername() + " already exists");
        }
        entityManager.persist(new SimpleUser(user));
    }

    @Override
    public void updateUser(User user) {
        if (!userNameExists(user.getUsername())) {
            throw new UserNotFoundException("User with username: " + user.getUsername() + " does not exists");
        }
        SimpleUser simpleUser = entityManager.find(SimpleUser.class, user.getUsername());
        simpleUser.setPassword(user.getPassword());
        simpleUser.setRoles(SimpleUser.convertAuthorityList(user.getAuthorities()));
        entityManager.merge(simpleUser);
    }

    @Override
    public void deleteUser(String username) {
        SimpleUser user = entityManager.find(SimpleUser.class, username);
        if (user == null) {
            throw new UserNotFoundException("User with username: " + username + " does not exists");
        }
        entityManager.remove(user);
    }

    private boolean userNameExists(String username) {
        return entityManager.find(SimpleUser.class, username) != null;
    }

    @Override
    public User loadUserByUsername(String username) {
        SimpleUser user = entityManager.find(SimpleUser.class, username);
        if (user == null) {
            throw new UsernameNotFoundException("user with name: " + username + " does not exist");
        }
        return user.toSpringUser();
    }

    @Override
    public List<User> getAllUser() {
        TypedQuery<SimpleUser> createQuery = entityManager.createQuery("SELECT u FROM SimpleUser u", SimpleUser.class);
        List<SimpleUser> resultList = createQuery.getResultList();
        return Lists.transform(resultList, new Function<SimpleUser, User>() {
            @Override
            public User apply(SimpleUser input) {
                return input.toSpringUser();
            }
        });
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
