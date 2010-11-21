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


import java.util.List;

import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.usermanagement.exceptions.UserExistsException;
import org.openengsb.core.usermanagement.exceptions.UserNotFoundException;
import org.openengsb.core.usermanagement.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public class UserManagerImpl implements UserManager {

    private PersistenceService persistence;

    public UserManagerImpl() {
    }

    @Override
    public void createUser(User user) {
        if (userNameExists(user.getUsername())) {
            throw new UserExistsException("User with username: " + user.getUsername() + " already exists");
        }
        try {
            persistence.create(user);
        } catch (PersistenceException e) {
            //TODO: rethink
        }
    }

    @Override
    public void updateUser(User oldUser, User newUser) {
        if (!userNameExists(oldUser.getUsername())) {
            throw new UserNotFoundException("User with username: " + oldUser.getUsername() + " does not exists");
        }
        try {
            persistence.update(oldUser, oldUser);
        } catch (PersistenceException e) {
            //TODO: rethink
        }
    }

    @Override
    public void deleteUser(String username) {
        if (!userNameExists(username)) {
            throw new UserNotFoundException("User with username: " + username + " does not exists");
        }
        UserDetails toBeDeleted = new User(username, null);
        try {
            persistence.delete(toBeDeleted);
        } catch (PersistenceException e) {
            //TODO: rethink
        }
    }

    private boolean userNameExists(String username) {
        List<User> list = persistence.query(new User(username, null));
        return list.size() > 0;
    }


    @Override
    public User loadUserByUsername(String username) {
        List<User> list = persistence.query(new User(username, null));
        if (list.size() > 0) {
            return list.get(0);
        } else {
            throw new UserNotFoundException("user with name: " + username + " does not exist");
        }
    }

    public void setPersistence(PersistenceService persistence) {
        this.persistence = persistence;
    }
}
