/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.usermanagement;


import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.usermanagement.UserManager;
import org.openengsb.core.common.usermanagement.exceptions.UserExistsException;
import org.openengsb.core.common.usermanagement.exceptions.UserManagementException;
import org.openengsb.core.common.usermanagement.exceptions.UserNotFoundException;
import org.openengsb.core.common.usermanagement.model.User;
import org.osgi.framework.BundleContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

public class UserManagerImpl implements UserManager {

    private PersistenceService persistence;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContext;

    public UserManagerImpl() {
    }

    @Override
    public void createUser(User user) throws UserManagementException {
        if (userNameExists(user.getUsername())) {
            throw new UserExistsException("User with username: " + user.getUsername() + " already exists");
        }
        try {
            persistence.create(user);
        } catch (PersistenceException e) {
            throw new UserManagementException(e);
        }
    }

    @Override
    public void updateUser(User user) throws UserManagementException {
        User oldUser = new User(user.getUsername());
        if (!userNameExists(oldUser.getUsername())) {
            throw new UserNotFoundException("User with username: " + oldUser.getUsername() + " does not exists");
        }
        try {
            persistence.update(oldUser, user);
        } catch (PersistenceException e) {
            throw new UserManagementException(e);
        }
    }

    @Override
    public void deleteUser(String username) throws UserManagementException {
        if (!userNameExists(username)) {
            throw new UserNotFoundException("User with username: " + username + " does not exists");
        }
        UserDetails toBeDeleted = new User(username);
        try {
            persistence.delete(toBeDeleted);
        } catch (PersistenceException e) {
            throw new UserManagementException(e);
        }
    }

    private boolean userNameExists(String username) {
        List<User> list = persistence.query(new User(username));
        return list.size() > 0;
    }


    @Override
    public User loadUserByUsername(String username) {
        List<User> list = persistence.query(new User(username));
        if (list.size() > 0) {
            return list.get(0);
        } else {
            throw new UserNotFoundException("user with name: " + username + " does not exist");
        }
    }

    @Override
    public List<User> getAllUser() {
        return persistence.query(new User(null));
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void init() throws UserManagementException {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
        try {
            loadUserByUsername(null);
        } catch (UserNotFoundException ex) {
            //create dummy admin user
            List<GrantedAuthority> auth = new ArrayList<GrantedAuthority>();
            auth.add(new GrantedAuthorityImpl("ROLE_USER"));
            createUser(new User("admin", "password", auth));
        }
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
