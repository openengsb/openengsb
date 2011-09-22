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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.util.CollectionUtils2;
import org.openengsb.core.security.model.EntryElement;
import org.openengsb.core.security.model.EntryValue;
import org.openengsb.core.security.model.PermissionData;
import org.openengsb.core.security.model.PermissionSetData;
import org.openengsb.core.security.model.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
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
        newUser.setPermissions(new HashSet<PermissionData>());
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
    public Collection<Permission> getUserPermissions(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionData> data = user.getPermissions();
        return EntryUtils.convertAllBeanDataToObjects(data);
    }

    @Override
    public Collection<Permission> getAllUserPermissions(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<Permission> result = new HashSet<Permission>();
        result.addAll(getUserPermissions(username));
        for (PermissionSetData set : user.getPermissionSets()) {
            Collection<Permission> setPermissions = getAllPermissionsFromSet(set.getId());
            result.addAll(setPermissions);
        }
        return result;
    }

    @Override
    public <T extends Permission> Collection<T> getUserPermissions(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance with proper query.
        return CollectionUtils2.filterCollectionByClass(getUserPermissions(username), type);
    }

    @Override
    public <T extends Permission> Collection<T> getAllUserPermissions(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance with proper query.
        return CollectionUtils2.filterCollectionByClass(getAllUserPermissions(username), type);
    }

    @Override
    public void storeUserPermission(String username, Permission permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        PermissionData permissionData = convertPermissionToPermissionData(permission);
        Collection<PermissionData> permissions = user.getPermissions();
        if (permissions == null) {
            permissions = Sets.newHashSet();
            user.setPermissions(permissions);
        }
        permissions.add(permissionData);
        entityManager.merge(user);
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
    public void removeUserPermission(String username, final Permission permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionData> permissions = user.getPermissions();
        final Map<String, EntryValue> entryMap = EntryUtils.convertBeanToEntryMap(permission);
        PermissionData entry = Iterators.find(permissions.iterator(), new Predicate<PermissionData>() {
            @Override
            public boolean apply(PermissionData input) {
                return input.getAttributes().equals(entryMap);
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
    public Collection<String> getUserPermissionSets(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionSetData> permissionSets = user.getPermissionSets();
        return Collections2.transform(permissionSets, new Function<PermissionSetData, String>() {
            @Override
            public String apply(PermissionSetData input) {
                return input.getId();
            }
        });
    }

    @Override
    public void storeUserPermissionSet(String username, String permissionSet) throws UserNotFoundException {
        UserData user = doFindUser(username);
        PermissionSetData set = findPermissionSet(permissionSet);
        user.getPermissionSets().add(set);
        entityManager.merge(user);
    }

    @Override
    public void removeUserPermissionSet(String username, String permissionSet) throws UserNotFoundException {
        UserData user = doFindUser(username);
        PermissionSetData set = findPermissionSet(permissionSet);
        user.getPermissionSets().remove(set);
        entityManager.merge(user);
    }

    @Override
    public void addSetToPermissionSet(String permissionSetParent, String permissionSet) {
        PermissionSetData parent = findPermissionSet(permissionSetParent);
        PermissionSetData child = findPermissionSet(permissionSet);
        parent.getPermissionSets().add(child);
        entityManager.merge(parent);
    }

    @Override
    public Collection<String> getMemberPermissionSets(String permissionSet) throws UserNotFoundException {
        PermissionSetData parent = findPermissionSet(permissionSet);
        return Collections2.transform(parent.getPermissionSets(), new Function<PermissionSetData, String>() {
            @Override
            public String apply(PermissionSetData input) {
                return input.getId();
            }
        });
    }

    @Override
    public void removeSetFromPermissionSet(String permissionSetParent, String permissionSet) {
        PermissionSetData parent = findPermissionSet(permissionSetParent);
        PermissionSetData child = findPermissionSet(permissionSet);
        parent.getPermissionSets().remove(child);
        entityManager.merge(parent);
    }

    @Override
    public Collection<Permission> getPermissionsFromSet(String permissionSet) {
        PermissionSetData set = findPermissionSet(permissionSet);
        Collection<PermissionData> data = set.getPermissions();
        return EntryUtils.convertAllBeanDataToObjects(data);
    }

    @Override
    public Collection<Permission> getAllPermissionsFromSet(String permissionSet) {
        Collection<Permission> result = Sets.newHashSet(getPermissionsFromSet(permissionSet));
        PermissionSetData set = findPermissionSet(permissionSet);
        for (PermissionSetData child : set.getPermissionSets()) {
            result.addAll(getAllPermissionsFromSet(child.getId()));
        }
        return result;
    }

    @Override
    public void addPermissionToSet(String permissionSet, Permission... permission) {
        PermissionSetData set = findPermissionSet(permissionSet);
        for (Permission p : permission) {
            PermissionData data = convertPermissionToPermissionData(p);
            set.getPermissions().add(data);
        }
        entityManager.merge(set);
    }

    @Override
    public void removePermissionFromSet(String permissionSet, Permission... permission) {
        PermissionSetData set = findPermissionSet(permissionSet);
        for (Permission p : permission) {
            PermissionData data = convertPermissionToPermissionData(p);
            set.getPermissions().remove(data);
        }
        entityManager.merge(set);

    }

    @Override
    public String getPermissionSetAttribute(String permissionSet, String attributename) {
        return findPermissionSet(permissionSet).getMetadata().get(attributename);
    }

    @Override
    public void setPermissionSetAttribute(String permissionSet, String attributename, String value) {
        findPermissionSet(permissionSet).getMetadata().put(attributename, value);
    }

    //
    // private static PermissionSetData convertPermissionSetToData(PermissionSet permissionSet) {
    // PermissionSetData data = new PermissionSetData();
    // data.setId(permissionSet.getId());
    // Collection<PermissionData> permissions =
    // Collections2.transform(permissionSet.getPermissions(), new Function<Permission, PermissionData>() {
    // @Override
    // public PermissionData apply(Permission input) {
    // PermissionData permissionData = new PermissionData();
    // permissionData.setType(input.getClass().getName());
    // permissionData.setAttributes(EntryUtils.convertBeanToEntryMap(input));
    // return permissionData;
    // }
    // });
    // data.setPermissions(permissions);
    // Collection<PermissionSetData> permissionSets =
    // Collections2.transform(permissionSet.getPermissionSets(), new Function<PermissionSet, PermissionSetData>() {
    // @Override
    // public PermissionSetData apply(PermissionSet input) {
    // return convertPermissionSetToData(input);
    // }
    // });
    // data.setPermissionSets(permissionSets);
    // return data;
    // }

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

    private PermissionSetData findPermissionSet(String permissionSet) {
        PermissionSetData set = entityManager.find(PermissionSetData.class, permissionSet);
        return set;
    }

}
