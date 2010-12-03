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

package org.openengsb.core.common.usermanagement;

import java.util.List;

import org.openengsb.core.common.usermanagement.model.User;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserManager extends UserDetailsService, BundleContextAware {

    /**
     * create an user and save it to the persistence,
     */
    void createUser(User user);

    /**
     * update the specified user in the persistence
     */
    void updateUser(User user);

    /**
     * delete an user, specified by its username from the persistence
     */
    void deleteUser(String username);

    /**
     * get one single user from the persistence,
     */
    User loadUserByUsername(String username);

    /**
     * returns a list of all existing users
     */
    List<User> getAllUser();

}
