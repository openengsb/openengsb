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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.security.PermissionProvider;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.BeanUtilsExtended;
import org.openengsb.core.security.internal.model.BeanData;
import org.openengsb.core.security.internal.model.EntryElement;
import org.openengsb.core.security.internal.model.EntryValue;
import org.osgi.framework.Filter;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComputationException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class EntryUtils {

    static List<EntryElement> convertAllObjectsToEntryElements(Collection<? extends Object> collection) {
        List<EntryElement> result = new ArrayList<EntryElement>();
        for (Object o : collection) {
            result.add(transformObjectToEntryElement(o));
        }
        return result;
    }

    static List<EntryElement> convertAllObjectsToEntryElements(Object[] array) {
        List<EntryElement> result = new ArrayList<EntryElement>();
        for (Object o : array) {
            result.add(transformObjectToEntryElement(o));
        }
        return result;
    }

    static EntryElement transformObjectToEntryElement(Object o) {
        return new EntryElement(o.getClass().getName(), o.toString());
    }

    static List<Object> convertAllEntryElementsToObject(List<EntryElement> value) {
        return Lists.transform(value, new EntryElementParserFunction());
    }

    private static class EntryElementParserFunction implements Function<EntryElement, Object> {
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

    private static final class EntryValueToObjectTransformer implements
            Maps.EntryTransformer<String, EntryValue, Object> {
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
    }

    static List<EntryElement> makeEntryElementList(Object... value) {
        List<EntryElement> valueElements = new ArrayList<EntryElement>();
        for (Object o : value) {
            Class<?> type = ClassUtils.primitiveToWrapper(o.getClass());
            EntryElement entryElement = new EntryElement(type.getName(), o.toString());
            valueElements.add(entryElement);
        }
        return valueElements;
    }

    static Map<String, EntryValue> convertBeanToEntryMap(Permission permission) {
        Map<String, Object> buildAttributeValueMap = BeanUtilsExtended.buildObjectAttributeMap(permission);
        return Maps.transformEntries(buildAttributeValueMap, new ObjectToEntryValueTransformer());
    }

    private static final class ObjectToEntryValueTransformer implements
            Maps.EntryTransformer<String, Object, EntryValue> {
        @SuppressWarnings("unchecked")
        @Override
        public EntryValue transformEntry(String key, Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Collection) {
                return new EntryValue(key, convertAllObjectsToEntryElements((Collection<Object>) value));
            }
            if (value.getClass().isArray()) {
                return new EntryValue(key, convertAllObjectsToEntryElements((Object[]) value));
            }
            return new EntryValue(key, Arrays.asList(transformObjectToEntryElement(value)));
        }
    }

    static <T> Collection<T> convertAllBeanDataToObjects(Collection<? extends BeanData> data) {
        return Collections2.transform(data, new BeanDataToObjectFunction<T>());
    }

    @SuppressWarnings("unchecked")
    static <T> T convertBeanDataToObject(BeanData input) {
        Class<?> permType;
        try {
            permType = findPermissionClass(input.getType());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("permission-type could not be found " + input.getType(), e);
        }
        Map<String, Object> attributeValues = convertEntryMapToAttributeMap(input.getAttributes());
        return (T) BeanUtilsExtended.createBeanFromAttributeMap(permType, attributeValues);
    }

    static Map<String, Object> convertEntryMapToAttributeMap(Map<String, EntryValue> entryMap) {
        return Maps.transformEntries(entryMap, new EntryValueToObjectTransformer());
    }

    private static Class<? extends Permission> findPermissionClass(String name) throws ClassNotFoundException {
        OsgiUtilsService utilService = OpenEngSBCoreServices.getServiceUtilsService();
        Filter filter = utilService.makeFilter(PermissionProvider.class, String.format("(permissionClass=%s)", name));
        PermissionProvider provider =
            OpenEngSBCoreServices.getServiceUtilsService().getOsgiServiceProxy(filter, PermissionProvider.class);
        return provider.getPermissionClass(name);
    }

    private static final class BeanDataToObjectFunction<T> implements Function<BeanData, T> {
        @Override
        public T apply(BeanData input) {
            return convertBeanDataToObject(input);
        }
    }

    private EntryUtils() {
    }

}
