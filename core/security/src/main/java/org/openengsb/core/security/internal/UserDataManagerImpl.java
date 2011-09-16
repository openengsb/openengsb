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
import java.util.HashMap;
import java.util.HashSet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.util.BeanUtils2;
import org.openengsb.core.security.model.CredentialData;
import org.openengsb.core.security.model.PermissionData;
import org.openengsb.core.security.model.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComputationException;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

public class UserDataManagerImpl implements UserDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataManagerImpl.class);

    private EntityManager entityManager;

    public UserDataManagerImpl() {
    }

    @Override
    public Collection<String> getUserList() {
        TypedQuery<String> query = entityManager.createQuery("SELECT u.username from UserData u", String.class);
        return query.getResultList();
    }

    @Override
    public void createUser(String username) throws UserExistsException {
        UserData newUser = new UserData();
        newUser.setUsername(username);
        newUser.setAttributes(new HashMap<String, String>());
        newUser.setPermissions(new HashSet<PermissionData>());
        entityManager.persist(newUser);
    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException {
        UserData found = doFindUser(username);
        entityManager.remove(found);
    }

    @Override
    public String getUserCredentials(String username, final String key) throws UserNotFoundException {
        UserData found = doFindUser(username);
        return found.getCredentials().get(key).getValue();
    }

    @Override
    public void setUserCredentials(String username, String type, String value) throws UserNotFoundException {
        UserData found = doFindUser(username);
        found.getCredentials().put(type, new CredentialData(type, value));
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
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void setUserAttribute(String username, String attributename, String... value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void removeUserAttribute(String username, String attributename) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<Permission> getUserPermissions(String username, String type) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionData> data = user.getPermissions();
        return Collections2.transform(data, new Function<PermissionData, Permission>() {
            public Permission apply(PermissionData input) {
                try {
                    Class<?> permType = Class.forName(input.getType());
                    return (Permission) BeanUtils2.buildBeanFromAttributeMap(permType, input.getAttributes());
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
        permissionData.setType(type);
        permissionData.setAttributes(permission.toAttributes());
        Collection<PermissionData> permissions = user.getPermissions();
        if (permissions == null) {
            permissions = Sets.newHashSet();
            user.setPermissions(permissions);
        }
        permissions.add(permissionData);
        entityManager.merge(permissionData);
        entityManager.merge(user);
    }

    @Override
    public void removeUserPermission(String username, final Permission permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionData> permissions = user.getPermissions();
        PermissionData entry = Iterators.find(permissions.iterator(), new Predicate<PermissionData>() {
            @Override
            public boolean apply(PermissionData input) {
                return input.getAttributes().equals(permission.toAttributes());
            }
        });
        if (entry == null) {
            LOGGER.warn("user does not have permission, " + permission);
            return;
        }
        permissions.remove(entry);
        entityManager.remove(entry);
        entityManager.merge(user);
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
