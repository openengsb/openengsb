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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.PermissionSet;
import org.openengsb.core.common.util.BeanUtils2;
import org.openengsb.core.common.util.CollectionUtils2;
import org.openengsb.core.security.model.BeanData;
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
import com.google.common.collect.ComputationException;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class UserDataManagerImpl implements UserDataManager {

    private final class EntryElementParserFunction implements Function<EntryElement, Object> {
        @Override
        public Object apply(EntryElement input) {
            Class<?> elementType = getElementType(input);
            Constructor<?> constructor = getStringConstructor(elementType);
            return invokeStringConstructor(input, constructor);
        }

        private Class<?> getElementType(EntryElement input) {
            Class<?> elementType;
            try {
                elementType = Class.forName(input.getType());
            } catch (ClassNotFoundException e) {
                throw new ComputationException(e);
            }
            return elementType;
        }

        private Constructor<?> getStringConstructor(Class<?> elementType) {
            Constructor<?> constructor;
            try {
                constructor = elementType.getConstructor(String.class);
            } catch (NoSuchMethodException e) {
                throw new ComputationException(e);
            }
            return constructor;
        }

        private Object invokeStringConstructor(EntryElement input, Constructor<?> constructor) {
            try {
                return constructor.newInstance(input.getValue());
            } catch (InstantiationException e) {
                throw new ComputationException(e);
            } catch (IllegalAccessException e) {
                throw new ComputationException(e);
            } catch (InvocationTargetException e) {
                throw new ComputationException(e.getCause());
            }
        }
    }

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
    public void deleteUser(String username) throws UserNotFoundException {
        UserData found = doFindUser(username);
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
        return Lists.transform(value, new EntryElementParserFunction());
    }

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException {
        UserData user = doFindUser(username);
        EntryValue entryValue = new EntryValue();
        entryValue.setKey(attributename);
        List<EntryElement> entryElementList = makeEntryElementList(value);
        entryValue.setValue(entryElementList);
        user.getAttributes().put(attributename, entryValue);
        entityManager.merge(user);
    }

    private List<EntryElement> makeEntryElementList(Object... value) {
        List<EntryElement> valueElements = new ArrayList<EntryElement>();
        for (Object o : value) {
            Class<?> type = ClassUtils.primitiveToWrapper(o.getClass());
            EntryElement entryElement = new EntryElement(type.getName(), o.toString());
            valueElements.add(entryElement);
        }
        return valueElements;
    }

    @Override
    public void removeUserAttribute(String username, String attributename) throws UserNotFoundException {
        UserData user = doFindUser(username);
        EntryValue entryValue = user.getAttributes().get(attributename);
        if (entryValue == null) {
            // silently fail if attribute is not present
            return;
        }
        user.getAttributes().remove(attributename);
        entityManager.merge(user);
    }

    @Override
    public Collection<Permission> getUserPermissions(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionData> data = user.getPermissions();
        return parseBeanData(data);
    }

    private <T> Collection<T> parseBeanData(Collection<? extends BeanData> data) {
        return Collections2.transform(data, new Function<BeanData, T>() {
            @SuppressWarnings("unchecked")
            public T apply(BeanData input) {
                Class<?> permType;
                try {
                    permType = Class.forName(input.getType());
                } catch (ClassNotFoundException e) {
                    throw new ComputationException(e);
                }
                Map<String, Object> attributeValues = parseEntryMap(input.getAttributes());
                Object instance;
                try {
                    instance = permType.newInstance();
                    BeanUtils.populate(instance, attributeValues);
                } catch (InstantiationException e) {
                    throw new ComputationException(e);
                } catch (IllegalAccessException e) {
                    throw new ComputationException(e);
                } catch (InvocationTargetException e) {
                    throw new ComputationException(e);
                }
                return (T) instance;

            };
        });
    }

    @Override
    public <T extends Permission> Collection<T> getUserPermissions(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance with proper query.
        return CollectionUtils2.filterCollectionByClass(getUserPermissions(username), type);
    }

    private static EntryElement transformObjectToEntryElement(Object o) {
        return new EntryElement(o.getClass().getName(), o.toString());
    }

    private static List<EntryElement> transformAllObjects(Collection<Object> collection) {
        List<EntryElement> result = new ArrayList<EntryElement>();
        for (Object o : collection) {
            result.add(transformObjectToEntryElement(o));
        }
        return result;
    }

    private static List<EntryElement> transformAllObjects(Object[] array) {
        List<EntryElement> result = new ArrayList<EntryElement>();
        for (Object o : array) {
            result.add(transformObjectToEntryElement(o));
        }
        return result;
    }

    @Override
    public void storeUserPermission(String username, Permission permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        PermissionData permissionData = new PermissionData();
        String type = permission.getClass().getName();
        permissionData.setType(type);
        Map<String, EntryValue> entryMap = convertBeanToEntryMap(permission);

        // copy the map, because JPA does not like the transformed map for some reason
        entryMap = Maps.newHashMap(entryMap);
        permissionData.setAttributes(entryMap);
        Collection<PermissionData> permissions = user.getPermissions();
        if (permissions == null) {
            permissions = Sets.newHashSet();
            user.setPermissions(permissions);
        }
        permissions.add(permissionData);
        entityManager.merge(permissionData);
        entityManager.merge(user);
    }

    static Map<String, EntryValue> convertBeanToEntryMap(Permission permission) {
        Map<String, Object> buildAttributeValueMap = BeanUtils2.buildAttributeValueMap(permission);
        Map<String, EntryValue> entryMap =
            Maps.transformEntries(buildAttributeValueMap, new Maps.EntryTransformer<String, Object, EntryValue>() {
                @SuppressWarnings("unchecked")
                @Override
                public EntryValue transformEntry(String key, Object value) {
                    if (value instanceof Collection) {
                        return new EntryValue(key, transformAllObjects((Collection<Object>) value));
                    }
                    if (value.getClass().isArray()) {
                        return new EntryValue(key, transformAllObjects((Object[]) value));
                    }
                    return new EntryValue(key, Arrays.asList(transformObjectToEntryElement(value)));
                }
            });
        return entryMap;
    }

    private Map<String, Object> parseEntryMap(Map<String, EntryValue> entryMap) {
        return Maps.transformEntries(entryMap, new Maps.EntryTransformer<String, EntryValue, Object>() {
            @Override
            public Object transformEntry(String key, EntryValue value) {
                List<Object> objects = Lists.transform(value.getValue(), new EntryElementParserFunction());
                if (objects.isEmpty()) {
                    return null;
                }
                if (objects.size() == 1) {
                    return objects.get(0);
                }
                return objects;
            }
        });
    }

    @Override
    public void removeUserPermission(String username, final Permission permission) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionData> permissions = user.getPermissions();
        final Map<String, EntryValue> entryMap = convertBeanToEntryMap(permission);
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
    public Collection<PermissionSet> getUserPermissionSets(String username) throws UserNotFoundException {
        UserData user = doFindUser(username);
        Collection<PermissionSetData> permissionSets = user.getPermissionSets();
        return parseBeanData(permissionSets);
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