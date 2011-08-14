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

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class UserManagerStub implements UserDataManager {

    private Map<String, Map<String, Object>> credentialsData = Maps.newHashMap();
    private Map<String, Map<String, Collection<Map<String, String>>>> permissionData = Maps.newHashMap();

    @Override
    public void createUser(String username) {
        credentialsData.put(username, new HashMap<String, Object>());
        permissionData.put(username, makePermissionData());
    }

    @Override
    public Object getUserCredentials(String username, String key) {
        return credentialsData.get(username).get(key);
    }

    @Override
    public void setUserCredentials(String username, String key, Object value) {
        credentialsData.get(username).put(key, value);
    }

    @Override
    public Collection<Map<String, String>> getUserPermissions(String username, String type) {
        return permissionData.get(username).get(type);
    }

    @Override
    public void storeUserPermission(String username, String type, Map<String, String> permission) {
        permissionData.get(username).get(type).add(permission);
    }

    private <T> Map<String, Collection<Map<String, String>>> makePermissionData() {
        return new MapMaker().makeComputingMap(new Function<String, Collection<Map<String, String>>>() {
            @Override
            public Collection<Map<String, String>> apply(String input) {
                return Sets.newHashSet();
            }
        });
    }
}
