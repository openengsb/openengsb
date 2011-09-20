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
package org.openengsb.core.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class UserManagerStub implements UserDataManager {

    private Map<String, Map<String, String>> credentialsData = Maps.newHashMap();
    private Map<String, Map<Class<?>, Collection<Permission>>> permissionData = Maps.newHashMap();

    @Override
    public void createUser(String username) throws UserExistsException {
        if (credentialsData.containsKey(username)) {
            throw new UserExistsException("user exists");
        }
        credentialsData.put(username, new HashMap<String, String>());
        permissionData.put(username, makePermissionData());
    }

    @Override
    public void deleteUser(String username) {
        credentialsData.remove(username);
        permissionData.remove(username);
    }

    @Override
    public String getUserCredentials(String username, String key) {
        return credentialsData.get(username).get(key);
    }

    private <T> Map<Class<?>, Collection<Permission>> makePermissionData() {
        return new MapMaker().makeComputingMap(new Function<Class<?>, Collection<Permission>>() {
            @Override
            public Collection<Permission> apply(Class<?> input) {
                return Sets.newHashSet();
            }
        });
    }

    @Override
    public void setUserCredentials(String username, String type, String value) {
        credentialsData.get(username).put(type, value);
    }

    @Override
    public void removeUserCredentials(String username, String type) {
        credentialsData.get(username).remove(type);
    }

    @Override
    public List<Object> getUserAttribute(String username, String attributename) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeUserAttribute(String username, String attributename) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Permission> Collection<T> getUserPermissions(String username, Class<T> type) {
        return (Collection<T>) permissionData.get(username).get(type);
    }

    @Override
    public void storeUserPermission(String username, Permission permission) {
        permissionData.get(username).get(permission.getClass()).add(permission);
    }

    @Override
    public void removeUserPermission(String username, Permission permission) {
        permissionData.get(username).get(permission.getClass()).remove(permission);

    }

    @Override
    public Collection<String> getUserList() {
        return credentialsData.keySet();
    }

    @Override
    public Collection<Permission> getUserPermissions(String username) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getAllUserPermissions(String username) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Permission> Collection<T> getAllUserPermissions(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void storeUserPermissionSet(String username, String permission) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeUserPermissionSet(String username, String permission) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createPermissionSet(String permissionSet, Permission... permission) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSetToPermissionSet(String permissionSetParent, String permissionSet) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeSetFromPermissionSet(String permissionSetParent, String permissionSet) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<String> getMemberPermissionSets(String permissionSet) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getPermissionsFromSet(String permissionSet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getAllPermissionsFromSet(String permissionSet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPermissionSetAttribute(String permissionSet, String attributename, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPermissionSetAttribute(String permissionSet, String attributename) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getUserPermissionSets(String username) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addPermissionToSet(String permissionSet, Permission... permission) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionFromSet(String permissionSet, Permission... permission) {
        // TODO Auto-generated method stub

    }

}
