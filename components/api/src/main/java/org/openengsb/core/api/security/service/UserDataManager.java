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
package org.openengsb.core.api.security.service;

import java.util.Collection;
import java.util.List;

import org.openengsb.core.api.security.model.Permission;

/**
 * Serves as a centralized store for User related data. It manages existing users and their associated credentials,
 * permissions and other metadata.
 *
 * There is also support for hierarchical PermissionSets (e.g. Roles).
 *
 */
public interface UserDataManager {

    /**
     * returns a list of names of all users available
     */
    Collection<String> getUserList();

    /**
     * creates a new user with the specified name
     *
     * @throws UserExistsException if the user already exists
     */
    void createUser(String username) throws UserExistsException;

    /**
     * Deletes the user with the specified name.
     *
     * If the user does not exist, this method does nothing
     */
    void deleteUser(String username);

    /**
     * returns the value of the credential of the specified type.
     *
     * @throws UserNotFoundException if the user does not exist
     * @throws NoSuchCredentialsException if the user has no credentials of this type
     */
    String getUserCredentials(String username, String key) throws UserNotFoundException, NoSuchCredentialsException;

    /**
     * Sets the value of the credential of the specified type.
     *
     * Previous values are overwritten.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    void setUserCredentials(String username, String type, String value) throws UserNotFoundException;

    /**
     * Removes the credentials of the specified type from the user. Future invocations of
     * {@link UserDataManager#getUserCredentials(String, String)} with this type will result in a
     * {@link NoSuchCredentialsException}.
     *
     * If no credentials of this type were associated with the user in the first place, this method does nothing.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    void removeUserCredentials(String username, String type) throws UserNotFoundException;

    /**
     * Retrieves the value of the given attribute.
     *
     * The types of the values are restricted to:
     * <ul>
     * <li>primitive types (e.g. int)</li>
     * <li>wrapped primitive types (e.g. Integer)</li>
     * <li>Strings</li>
     * <li>Types having a constructor with exactly one argument of type String</li>
     * </ul>
     *
     * If the attribute only has a single value, the list will only contain the one value.
     *
     * The values are already of the correct type. So if a Long was stored as an attribute, the object in the resulting
     * list will be of type Long too.
     *
     * @throws UserNotFoundException if the user does not exist
     * @throws NoSuchAttributeException if there is no attribute of that name associated with the user
     */
    List<Object> getUserAttribute(String username, String attributename) throws UserNotFoundException,
        NoSuchAttributeException;

    /**
     * Sets the value of the given attribute. An attribute may contain multiple values (as a list).
     *
     * However the types of the values are restricted to:
     * <ul>
     * <li>primitive types (e.g. int)</li>
     * <li>wrapped primitive types (e.g. Integer)</li>
     * <li>Strings</li>
     * <li>Types having a constructor with exactly one argument of type String</li>
     * </ul>
     * the resulting list may contain any primitive type in its wrapped form or strings.
     *
     * If the attribute only has a single value, the list will only contain the one value.
     *
     * @throws UserNotFoundException if the user does not exist
     * @throws NoSuchAttributeException if there is no attribute of that name associated with the user
     */
    void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException;

    /**
     * Deletes the value of the attribute with the given name.
     *
     * If no such attribute was associated with the user, this method does nothing.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    void removeUserAttribute(String username, String attributename) throws UserNotFoundException;

    /**
     * Retrieves all permissions directly associated with the user. This does not include permissions that are implied
     * by associated permissionSets.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    Collection<Permission> getPermissionsForUser(String username) throws UserNotFoundException;

    /**
     * Retrieves all permissions the user is granted. This includes permissions granted by associated PermissionSets.
     * The result must also include all permissions granted by recursively all PermissionSets.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    Collection<Permission> getAllPermissionsForUser(String username) throws UserNotFoundException;

    /**
     * Retrieves all permissions of the given type directly associated with the user. This does not include permissions
     * that are implied by associated permissionSets.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    <T extends Permission> Collection<T> getPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException;

    /**
     * Retrieves all permissions of the given type the user is granted. This includes permissions granted by associated
     * PermissionSets. The result must also include all permissions granted by recursively all PermissionSets.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    <T extends Permission> Collection<T> getAllPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException;

    /**
     * Adds the given permissions to the given user.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    void addPermissionToUser(String username, Permission... permission) throws UserNotFoundException;

    /**
     * Removes the given permissions to the given user.
     *
     * If the user did not have a permission in the first place, nothing happens.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    void removePermissionFromUser(String username, Permission... permission) throws UserNotFoundException;

    /**
     * Creates a permissionSet granting the given Permissions
     *
     * @throws PermissionSetAlreadyExistsException if a permissionSet with that name already exists
     */
    void createPermissionSet(String permissionSet, Permission... permission) throws PermissionSetAlreadyExistsException;

    /**
     * adds the given Permissions to the permissionSet
     *
     * @throws PermissionSetNotFoundException if the permissionSet does not exist
     */
    void addPermissionToSet(String permissionSet, Permission... permission) throws PermissionSetNotFoundException;

    /**
     * removes the given Permissions to the permissionSet
     *
     * if the permissionSet did not contain a permission in the first place, nothing happens.
     *
     * @throws PermissionSetNotFoundException if the permissionSet does not exist
     */
    void removePermissionFromSet(String permissionSet, Permission... permission) throws PermissionSetNotFoundException;

    /**
     * Returns a list the names of all permissionSets associated with the user
     *
     * @throws UserNotFoundException if the user does not exist
     */
    Collection<String> getPermissionSetsFromUser(String username) throws UserNotFoundException;

    /**
     * Adds the given permissionSets to the given user.
     *
     * @throws UserNotFoundException if the user does not exist
     * @throws PermissionSetNotFoundException if one of the permissionSets does not exist
     */
    void addPermissionSetToUser(String username, String... permissionSet) throws UserNotFoundException,
        PermissionSetNotFoundException;

    /**
     * Removes the permissionSet from the user
     *
     * If the user did not have such a permissionSet, or no such permissionSet exists, nothing happens.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    void removePermissionSetFromUser(String username, String... permissionSet) throws UserNotFoundException;

    /**
     * Adds a child permissionSet to a parent permissionSet
     *
     * @throws PermissionSetNotFoundException if one of the permissionSets does not exist
     */
    void addPermissionSetToPermissionSet(String permissionSetParent, String... permissionSet)
        throws PermissionSetNotFoundException;

    /**
     * Adds a child permissionSet to a parent permissionSet
     *
     * @throws PermissionSetNotFoundException if the parent permissionSet does not exist
     */
    void removePermissionSetFromPermissionSet(String permissionSetParent, String... permissionSet)
        throws PermissionSetNotFoundException;

    /**
     * Returns a list the names of all permissionSets directly associated with the parent permissionSet.
     *
     * @throws PermissionSetNotFoundException if the parent permissionSet does not exist
     */
    Collection<String> getPermissionSetsFromPermissionSet(String permissionSet) throws PermissionSetNotFoundException;

    /**
     * Retrieves all permissions directly associated with the parent permissionSet. This does not include permissions
     * that are implied by child permissionSets.
     *
     * @throws PermissionSetNotFoundException if the parent permissionSet does not exist
     */
    Collection<Permission> getPermissionsFromPermissionSet(String permissionSet) throws PermissionSetNotFoundException;

    /**
     * Retrieves all permissions the user is granted. This includes permissions recursively granted by associated
     * PermissionSets. The result must also include all permissions granted by recursively all PermissionSets.
     *
     * @throws PermissionSetNotFoundException if the parent permissionSet does not exist
     */
    Collection<Permission> getAllPermissionsFromPermissionSet(String permissionSet)
        throws PermissionSetNotFoundException;

    /**
     * Sets the value of the given attribute.
     *
     * Previous values of the attribute are overwritten.
     * 
     */
    void setPermissionSetAttribute(String permissionSet, String attributename, String value)
        throws PermissionSetNotFoundException;

    /**
     * returns the value of the given attribute.
     *
     * @throws PermissionSetNotFoundException if the permissionSet does not exist
     * @throws NoSuchAttributeException if there is no attribute of that name associated with the user
     */
    String getPermissionSetAttribute(String permissionSet, String attributename) throws PermissionSetNotFoundException,
        NoSuchAttributeException;

}
