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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
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
        permissionData.put(username, new HashMap<Class<?>, Collection<Permission>>());
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
    public <T extends Permission> Collection<T> getPermissionsForUser(String username, Class<T> type) {
        return (Collection<T>) getPermissionsForUsername(username).get(type);
    }

    private Map<Class<?>, Collection<Permission>> getPermissionsForUsername(String username) {
        Map<Class<?>, Collection<Permission>> result = permissionData.get(username);
        if (result == null) {
            result = Maps.newHashMap();
            permissionData.put(username, result);
        }
        return result;
    }

    @Override
    public void addPermissionToUser(String username, Permission... permissions) {
        for (Permission permission : permissions) {
            Map<Class<?>, Collection<Permission>> permissionsForUsername = getPermissionsForUsername(username);
            Collection<Permission> p = permissionsForUsername.get(permission.getClass());
            if (p == null) {
                p = Sets.newHashSet();
                permissionsForUsername.put(permission.getClass(), p);
            }
            p.add(permission);
        }
    }

    @Override
    public void removePermissionFromUser(String username, Permission... permissions) {
        for (Permission permission : permissions) {
            getPermissionsForUsername(username).get(permission.getClass()).remove(permission);
        }
    }

    @Override
    public Collection<String> getUserList() {
        return credentialsData.keySet();
    }

    @Override
    public Collection<Permission> getPermissionsForUser(String username) throws UserNotFoundException {
        Collection<Collection<Permission>> values = getPermissionsForUsername(username).values();
        Collection<Permission> result = new ArrayList<Permission>();
        for (Collection<Permission> c : values) {
            result.addAll(c);
        }
        return result;
    }

    @Override
    public Collection<Permission> getAllPermissionsForUser(String username) throws UserNotFoundException {
        return getPermissionsForUser(username);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException {
        return (Collection<T>) Collections2.filter(getAllPermissionsForUser(username), Predicates.instanceOf(type));
    }

    @Override
    public void addPermissionSetToUser(String username, String... permission) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionSetFromUser(String username, String... permission) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createPermissionSet(String permissionSet, Permission... permission) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPermissionSetToPermissionSet(String permissionSetParent, String... permissionSet) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionSetFromPermissionSet(String permissionSetParent, String... permissionSet) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<String> getPermissionSetsFromPermissionSet(String permissionSet) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getPermissionsFromPermissionSet(String permissionSet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getAllPermissionsFromPermissionSet(String permissionSet) {
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
    public Collection<String> getPermissionSetsFromUser(String username) throws UserNotFoundException {
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

    @Override
    public Collection<String> getPermissionSetList() {
        // TODO Auto-generated method stub
        return null;
    }

}
