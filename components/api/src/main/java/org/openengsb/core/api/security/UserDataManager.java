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

public interface UserDataManager {

    Collection<String> getUserList();

    void createUser(String username) throws UserExistsException;

    void deleteUser(String username);

    String getUserCredentials(String username, String key) throws UserNotFoundException, NoSuchCredentialsException;

    void setUserCredentials(String username, String type, String value) throws UserNotFoundException;

    void removeUserCredentials(String username, String type) throws UserNotFoundException;

    List<Object> getUserAttribute(String username, String attributename) throws UserNotFoundException,
        NoSuchAttributeException;

    void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException;

    void removeUserAttribute(String username, String attributename) throws UserNotFoundException;

    Collection<Permission> getPermissionsForUser(String username) throws UserNotFoundException;

    Collection<Permission> getAllPermissionsForUser(String username) throws UserNotFoundException;

    <T extends Permission> Collection<T> getPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException;

    <T extends Permission> Collection<T> getAllPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException;

    void addPermissionToUser(String username, Permission... permission) throws UserNotFoundException;

    void removePermissionFromUser(String username, Permission... permission) throws UserNotFoundException;

    /*
     * 
     */
    void createPermissionSet(String permissionSet, Permission... permission) throws PermissionSetAlreadyExistsException;

    void addPermissionToSet(String permissionSet, Permission... permission) throws PermissionSetNotFoundException;

    void removePermissionFromSet(String permissionSet, Permission... permission) throws PermissionSetNotFoundException;

    Collection<String> getPermissionSetsFromUser(String username) throws UserNotFoundException;

    void addPermissionSetToUser(String username, String... permissionSet) throws UserNotFoundException,
        PermissionSetNotFoundException;

    void removePermissionSetFromUser(String username, String... permissionSet) throws UserNotFoundException;

    void addPermissionSetToPermissionSet(String permissionSetParent, String... permissionSet)
        throws PermissionSetNotFoundException;

    void removePermissionSetFromPermissionSet(String permissionSetParent, String... permissionSet)
        throws PermissionSetNotFoundException;

    Collection<String> getPermissionSetsFromPermissionSet(String permissionSet) throws UserNotFoundException;

    Collection<Permission> getPermissionsFromSet(String permissionSet) throws PermissionSetNotFoundException;

    Collection<Permission> getAllPermissionsFromSet(String permissionSet) throws PermissionSetNotFoundException;

    void setPermissionSetAttribute(String permissionSet, String attributename, String value)
        throws PermissionSetNotFoundException;

    String getPermissionSetAttribute(String permissionSet, String attributename) throws PermissionSetNotFoundException,
        NoSuchAttributeException;

}
