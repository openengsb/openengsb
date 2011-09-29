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
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.PermissionSetNotFoundException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.common.util.CollectionUtilsExtended;
import org.openengsb.core.security.internal.model.EntryElement;
import org.openengsb.core.security.internal.model.EntryValue;
import org.openengsb.core.security.internal.model.PermissionData;
import org.openengsb.core.security.internal.model.PermissionSetData;
import org.openengsb.core.security.internal.model.UserData;
import org.openengsb.core.security.internal.model.UserPermissionSetData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
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
        UserPermissionSetData permissionSetData = new UserPermissionSetData(username);
        newUser.setPermissionSet(permissionSetData);
        entityManager.persist(newUser);
    }

    @Override
    public void deleteUser(String username) {
        UserData found;
        try {
            found = doFindUser(username);
        } catch (UserNotFoundException e) {
            LOGGER.warn("user {} was to be deleted, but not found", username);
            return;
        }
        entityManager.remove(found);
    }

    @Override
    public String getUserCredentials(String username, final String key) throws UserNotFoundException {
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
    public List<Object> getUserAttribute(String username, String attributename) throws UserNotFoundException {
        UserData user = doFindUser(username);
        EntryValue entryValue = user.getAttributes().get(attributename);
        if (entryValue == null) {
            return null;
        }
        List<EntryElement> value = entryValue.getValue();
        return EntryUtils.convertAllEntryElementsToObject(value);
    }

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException {
        UserData user = doFindUser(username);
        EntryValue entryValue = new EntryValue();
        entryValue.setKey(attributename);
        List<EntryElement> entryElementList = EntryUtils.makeEntryElementList(value);
        entryValue.setValue(entryElementList);
        user.getAttributes().put(attributename, entryValue);
        entityManager.merge(user);
    }

    @Override
    public void removeUserAttribute(String username, String attributename) throws UserNotFoundException {
        UserData user = doFindUser(username);
        EntryValue entryValue = user.getAttributes().get(attributename);
        if (entryValue == null) {
            // silently fail if attribute is not present
            LOGGER.warn("user does not have attribute, " + attributename);
            return;
        }
        user.getAttributes().remove(attributename);
        entityManager.merge(user);
    }

    @Override
    public Collection<Permission> getPermissionsForUser(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        return getPermissionsFromSetData(user.getPermissionSet());
    }

    @Override
    public Collection<Permission> getAllPermissionsForUser(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        return getAllPermissionsFromSetData(user.getPermissionSet());
    }

    @Override
    public <T extends Permission> Collection<T> getPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance with proper query.
        return CollectionUtilsExtended.filterCollectionByClass(getPermissionsForUser(username), type);
    }

    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance with proper query.
        return CollectionUtilsExtended.filterCollectionByClass(getAllPermissionsForUser(username), type);
    }

    @Override
    public void addPermissionToUser(String username, Permission... permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        addPermissionsToSet(user.getPermissionSet(), permission);
    }

    private static PermissionData convertPermissionToPermissionData(Permission permission) {
        PermissionData permissionData = new PermissionData();
        String type = permission.getClass().getName();
        permissionData.setType(type);
        Map<String, EntryValue> entryMap = EntryUtils.convertBeanToEntryMap(permission);

        // copy the map, because JPA does not like the transformed map for some reason
        entryMap = Maps.newHashMap(entryMap);
        permissionData.setAttributes(entryMap);
        return permissionData;
    }

    @Override
    public void removePermissionFromUser(String username, final Permission... permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        doRemovePermissionsFromSet(user.getPermissionSet(), permission);
    }

    @Override
    public Collection<String> getPermissionSetList() {
        TypedQuery<String> query = entityManager.createQuery("SELECT s.id from PermissionSetData s", String.class);
        return query.getResultList();
    }

    @Override
    public void createPermissionSet(String permissionSet, Permission... permission) {
        PermissionSetData data = new PermissionSetData(permissionSet);
        if (permission != null) {
            Collection<PermissionData> permissions = data.getPermissions();
            for (Permission p : permission) {
                permissions.add(convertPermissionToPermissionData(p));
            }
        }
        entityManager.persist(data);
    }

    @Override
    public Collection<String> getPermissionSetsFromUser(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionSetData> permissionSets = user.getPermissionSet().getPermissionSets();
        return Collections2.transform(permissionSets, new Function<PermissionSetData, String>() {
            @Override
            public String apply(PermissionSetData input) {
                return input.getId();
            }
        });
    }

    @Override
    public void addPermissionSetToUser(String username, String... permissionSet) throws UserNotFoundException {
        UserData user = doFindUser(username);
        doAddPermissionSetToSet(user.getPermissionSet(), permissionSet);
    }

    @Override
    public void removePermissionSetFromUser(String username, String... permissionSet) throws UserNotFoundException {
        UserData user = doFindUser(username);
        doRemovePermissionSetFromSet(user.getPermissionSet(), permissionSet);
    }

    @Override
    public void addPermissionSetToPermissionSet(String permissionSetParent, String... permissionSet) {
        PermissionSetData parent = doFindPermissionSet(permissionSetParent);
        doAddPermissionSetToSet(parent, permissionSet);
    }

    @Override
    public Collection<String> getPermissionSetsFromPermissionSet(String permissionSet) throws UserNotFoundException {
        PermissionSetData parent = doFindPermissionSet(permissionSet);
        return Collections2.transform(parent.getPermissionSets(), new Function<PermissionSetData, String>() {
            @Override
            public String apply(PermissionSetData input) {
                return input.getId();
            }
        });
    }

    @Override
    public void removePermissionSetFromPermissionSet(String permissionSetParent, String... permissionSet) {
        PermissionSetData parent = doFindPermissionSet(permissionSetParent);
        doRemovePermissionSetFromSet(parent, permissionSet);
    }

    @Override
    public Collection<Permission> getPermissionsFromPermissionSet(String permissionSet) {
        PermissionSetData set = doFindPermissionSet(permissionSet);
        return getPermissionsFromSetData(set);
    }

    @Override
    public Collection<Permission> getAllPermissionsFromPermissionSet(String permissionSet) {
        PermissionSetData set = doFindPermissionSet(permissionSet);
        return getAllPermissionsFromSetData(set);
    }

    @Override
    public void addPermissionToSet(String permissionSet, Permission... permission) {
        PermissionSetData set = doFindPermissionSet(permissionSet);
        addPermissionsToSet(set, permission);
    }

    private void addPermissionsToSet(PermissionSetData set, Permission... permission) {
        for (Permission p : permission) {
            PermissionData data = convertPermissionToPermissionData(p);
            set.getPermissions().add(data);
        }
        entityManager.merge(set);
    }

    @Override
    public void removePermissionFromSet(String permissionSet, Permission... permission) {
        PermissionSetData set = doFindPermissionSet(permissionSet);
        doRemovePermissionsFromSet(set, permission);
    }

    @Override
    public String getPermissionSetAttribute(String permissionSet, String attributename) {
        return doFindPermissionSet(permissionSet).getMetadata().get(attributename);
    }

    @Override
    public void setPermissionSetAttribute(String permissionSet, String attributename, String value) {
        doFindPermissionSet(permissionSet).getMetadata().put(attributename, value);
    }

    private void doAddPermissionSetToSet(PermissionSetData parent, String... permissionSet) {
        for (String p : permissionSet) {
            PermissionSetData child = doFindPermissionSet(p);
            parent.getPermissionSets().add(child);
        }
        entityManager.merge(parent);
    }

    private void doRemovePermissionSetFromSet(PermissionSetData parent, String... permissionSet) {
        for (String p : permissionSet) {
            PermissionSetData child = doFindPermissionSet(p);
            parent.getPermissionSets().remove(child);
        }
        entityManager.merge(parent);
    }

    private Collection<Permission> getPermissionsFromSetData(PermissionSetData set) {
        Collection<PermissionData> data = set.getPermissions();
        return EntryUtils.convertAllBeanDataToObjects(data);
    }

    private Collection<Permission> getAllPermissionsFromSetData(PermissionSetData set) {
        Collection<Permission> result = Sets.newHashSet(getPermissionsFromSetData(set));
        for (PermissionSetData child : set.getPermissionSets()) {
            result.addAll(getAllPermissionsFromPermissionSet(child.getId()));
        }
        return result;
    }

    private void doRemovePermissionsFromSet(PermissionSetData set, Permission... permission) {
        for (Permission p : permission) {
            PermissionData data = convertPermissionToPermissionData(p);
            set.getPermissions().remove(data);
        }
        entityManager.merge(set);
    }

    private UserData doFindUser(String username) throws UserNotFoundException {
        UserData found = entityManager.find(UserData.class, username);
        if (found == null) {
            throw new UserNotFoundException("User with name " + username + " not found");
        }
        return found;
    }

    private PermissionSetData doFindPermissionSet(String permissionSet) throws PermissionSetNotFoundException {
        PermissionSetData set = entityManager.find(PermissionSetData.class, permissionSet);
        if (set == null) {
            throw new PermissionSetNotFoundException("permissionSet " + permissionSet + " not found");
        }
        return set;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
