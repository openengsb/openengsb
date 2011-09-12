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

package org.openengsb.core.security.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.util.BeanUtils2;
import org.openengsb.core.security.model.PermissionData;
import org.openengsb.core.security.model.UserData;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComputationException;

public class UserDataManagerImpl implements UserDataManager {

    private EntityManager entityManager;

    public UserDataManagerImpl() {
    }

    @Override
    public void createUser(String username) throws UserExistsException {
        UserData newUser = new UserData();
        newUser.setUsername(username);
        entityManager.persist(newUser);
    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException {
        UserData found = doFindUser(username);
        entityManager.remove(found);
    }

    @Override
    public String getUserCredentials(String username, String key) throws UserNotFoundException {
        UserData found = doFindUser(username);
        return found.getCredentials().get(key);
    }

    @Override
    public void setUserCredentials(String username, String type, String value) throws UserNotFoundException {
        UserData found = doFindUser(username);
        found.getCredentials().put(type, value);
        entityManager.merge(found);
    }

    @Override
    public void removeUserCredentials(String username, String type) throws UserNotFoundException {
        UserData found = doFindUser(username);
        found.getCredentials().remove(type);
        entityManager.merge(found);
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
    public Collection<Permission> getUserPermissions(String username, String type) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Map<String, PermissionData> data = user.getPermissions();
        return Collections2.transform(data.entrySet(), new Function<Entry<String, PermissionData>, Permission>() {
            public Permission apply(Entry<String, PermissionData> input) {
                try {
                    Class<?> permType = Class.forName(input.getKey());
                    return (Permission) BeanUtils2
                        .buildBeanFromAttributeMap(permType, input.getValue().getAttributes());
                } catch (ClassNotFoundException e) {
                    throw new ComputationException(e);
                }
            };
        });
    }

    @Override
    public void storeUserPermission(String username, Permission permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        PermissionData permissionData = new PermissionData();
        String type = permission.getClass().getName();
        permissionData.setAttributes(permission.toAttributes());
        user.getPermissions().put(type, permissionData);
        entityManager.merge(permissionData);
        entityManager.merge(user);
    }

    @Override
    public void removeUserPermissoin(String username, Permission permission) {
        // TODO Auto-generated method stub

    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private UserData doFindUser(String username) throws UserNotFoundException {
        UserData found = entityManager.find(UserData.class, username);
        if (found == null) {
            throw new UserNotFoundException("User with name " + username + " not found");
        }
        return found;
    }
}
