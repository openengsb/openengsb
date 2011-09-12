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
import java.util.Map;

import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.model.Permission;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class UserManagerStub implements UserDataManager {

    private Map<String, Map<String, String>> credentialsData = Maps.newHashMap();
    private Map<String, Map<String, Collection<Permission>>> permissionData = Maps.newHashMap();

    @Override
    public void createUser(String username) {
        credentialsData.put(username, new HashMap<String, String>());
        permissionData.put(username, makePermissionData());
    }

    @Override
    public void deleteUser(String username) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getUserCredentials(String username, String key) {
        return credentialsData.get(username).get(key);
    }

    private <T> Map<String, Collection<Permission>> makePermissionData() {
        return new MapMaker().makeComputingMap(new Function<String, Collection<Permission>>() {
            @Override
            public Collection<Permission> apply(String input) {
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
    public String[] getUserAttribute(String username, String attributename) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setUserAttribute(String username, String attributename, String... value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeUserAttribute(String username, String attributename) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<Permission> getUserPermissions(String username, String type) {
        return permissionData.get(username).get(type);
    }

    @Override
    public void storeUserPermission(String username, Permission permission) {
        permissionData.get(username).get(permission.getClass().getName()).add(permission);
    }

    @Override
    public void removeUserPermissoin(String username, Permission permission) {
        permissionData.get(username).get(permission.getClass().getName()).remove(permission);

    }

}
