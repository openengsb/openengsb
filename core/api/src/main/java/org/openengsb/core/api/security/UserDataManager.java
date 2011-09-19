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

import java.util.Collection;
import java.util.List;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.PermissionSet;

public interface UserDataManager {

    Collection<String> getUserList();

    void createUser(String username) throws UserExistsException;

    void deleteUser(String username) throws UserNotFoundException;

    String getUserCredentials(String username, String key) throws UserNotFoundException;

    void setUserCredentials(String username, String type, String value) throws UserNotFoundException;

    void removeUserCredentials(String username, String type) throws UserNotFoundException;

    List<Object> getUserAttribute(String username, String attributename) throws UserNotFoundException;

    void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException;

    void removeUserAttribute(String username, String attributename) throws UserNotFoundException;

    Collection<Permission> getUserPermissions(String username) throws UserNotFoundException;

    <T extends Permission> Collection<T> getUserPermissions(String username, Class<T> type)
        throws UserNotFoundException;

    void storeUserPermission(String username, Permission permission) throws UserNotFoundException;

    void removeUserPermission(String username, Permission permission) throws UserNotFoundException;

    Collection<PermissionSet> getUserPermissionSets(String username) throws UserNotFoundException;

    <T extends PermissionSet> Collection<T> getuserPermissionSets(String username, Class<T> type)
        throws UserNotFoundException;

    void storeUserPermissionSet(String username, PermissionSet permission) throws UserNotFoundException;

    void removeUserPermissionSet(String username, PermissionSet permission) throws UserNotFoundException;

}
