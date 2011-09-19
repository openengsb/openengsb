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
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.PermissionSet;
import org.openengsb.core.common.util.BeanUtils2;
import org.openengsb.core.common.util.CollectionUtils2;
import org.openengsb.core.security.model.CredentialData;
import org.openengsb.core.security.model.PermissionData;
import org.openengsb.core.security.model.PermissionSetData;
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
    public Collection<Permission> getUserPermissions(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionData> data = user.getPermissions();
        return parsePermissionData(data);
    }

    private Collection<Permission> parsePermissionData(Collection<PermissionData> data) {
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
    public <T extends Permission> Collection<T> getUserPermissions(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance with proper query.
        return CollectionUtils2.filterCollectionByClass(getUserPermissions(username), type);
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

    @Override
    public Collection<PermissionSet> getUserPermissionSets(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionSetData> permissionSets = user.getPermissionSets();
        Collections2.transform(permissionSets, new Function<PermissionSetData, PermissionSet>() {
            @Override
            public PermissionSet apply(PermissionSetData input) {
                try {
                    Class<?> permType = Class.forName(input.getType());
                    PermissionSet result =
                        (PermissionSet) BeanUtils2.buildBeanFromAttributeMap(permType, input.getAttributes());
                    result.setPermissions(parsePermissionData(input.getPermissions()));
                    return result;
                } catch (ClassNotFoundException e) {
                    throw new ComputationException(e);
                }
            }
        });

        return null;
    }

    @Override
    public <T extends PermissionSet> Collection<T> getuserPermissionSets(String username, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void storeUserPermissionSet(String username, PermissionSet permission) throws UserNotFoundException {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeUserPermissionSet(String username, PermissionSet permission) throws UserNotFoundException {
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
