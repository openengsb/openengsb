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

package org.openengsb.core.api.security;

import java.util.List;

import org.openengsb.core.api.security.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserManager extends UserDetailsService {

    /**
     * create a user and save it to the persistence,
     * @throws UserManagementException if a persistence error occurred
     */
    void createUser(User user);

    /**
     * update the specified user in the persistence,
     * the user is identified by its name, so the name can not be changed
     * @throws UserManagementException if a persistence error occurred
     */
    void updateUser(User user);

    /**
     * delete a user, specified by its username from the persistence
     * @throws UserManagementException if a persistence error occurred
     */
    void deleteUser(String username);

    /**
     * get one single user from the persistence,
     */
    User loadUserByUsername(String username);

    /**
     * returns a list of all existing users
     * @throws UserManagementException if a persistence error occurred
     */
    List<User> getAllUser();

}
