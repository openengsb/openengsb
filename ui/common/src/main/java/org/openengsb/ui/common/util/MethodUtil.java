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

package org.openengsb.ui.common.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.AttributeDefinition.Builder;
import org.openengsb.core.api.l10n.PassThroughStringLocalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public final class MethodUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodUtil.class);

    public static Class<?>[] getAllInterfaces(Object serviceObject) {
        List<Class<?>> interfaces = new ArrayList<Class<?>>();
        interfaces.addAll(Arrays.asList(serviceObject.getClass().getInterfaces()));
        Class<?> superClass = serviceObject.getClass().getSuperclass();
        while (superClass != null) {
            interfaces.addAll(Arrays.asList(superClass.getInterfaces()));
            superClass = superClass.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }

    public static List<AttributeDefinition> buildAttributesList(Class<?> theClass) {
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(theClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getWriteMethod() == null
                        || !Modifier.isPublic(propertyDescriptor.getWriteMethod().getModifiers())) {
                    continue;
                }
                Builder builder =
                    AttributeDefinition.builder(new PassThroughStringLocalizer()).id(propertyDescriptor.getName())
                        .name(propertyDescriptor.getDisplayName())
                        .description(propertyDescriptor.getShortDescription());
                addEnumValues(propertyDescriptor.getPropertyType(), builder);
                AttributeDefinition a = builder.build();
                attributes.add(a);
            }
        } catch (IntrospectionException ex) {
            LOGGER.error("building attribute list failed", ex);
        }
        return attributes;
    }

    public static void addEnumValues(Class<?> theClass, Builder builder) {
        if (theClass.isEnum()) {
            Object[] enumConstants = theClass.getEnumConstants();
            for (Object object : enumConstants) {
                builder.option(object.toString(), object.toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T buildBean(Class<T> beanClass, Map<String, String> values) {
        try {
            Object obj = beanClass.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getWriteMethod() == null
                        || !Modifier.isPublic(propertyDescriptor.getWriteMethod().getModifiers())) {
                    continue;
                }

                String value = values.get(propertyDescriptor.getName());
                Object ob = convertToCorrectClass(propertyDescriptor.getPropertyType(), value);
                propertyDescriptor.getWriteMethod().invoke(obj, ob);
            }
            return (T) obj;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Map<String, PropertyEditor> getRelevantProperties(final Object bean)
        throws IntrospectionException {
        Class<?> beanClass = bean.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Iterator<PropertyDescriptor> propertyIterator = Iterators.forArray(propertyDescriptors);
        Iterator<PropertyDescriptor> filtered = Iterators.filter(propertyIterator, new Predicate<PropertyDescriptor>() {
            @Override
            public boolean apply(PropertyDescriptor input) {
                if (!input.getPropertyType().equals(String.class)) {
                    return false;
                }
                Method writeMethod = input.getWriteMethod();
                if (writeMethod == null) {
                    return false;
                }
                return Modifier.isPublic(writeMethod.getModifiers());
            }
        });
        Map<String, PropertyEditor> result = Maps.newHashMap();
        while (filtered.hasNext()) {
            PropertyDescriptor descriptor = filtered.next();
            result.put(descriptor.getName(), descriptor.createPropertyEditor(bean));
        }
        return result;
    }

    public static Map<String, String> buildAttributeMap(Object bean) throws IntrospectionException {
        Map<String, PropertyEditor> relevantProperties = getRelevantProperties(bean);
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, PropertyEditor> entry : relevantProperties.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAsText());
        }
        return result;
    }

    public static <T> T buildBeanFromAttributeMap(Class<T> beanClass, Map<String, String> values) {
        T bean;
        try {
            bean = beanClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        Map<String, PropertyEditor> relevantProperties;
        try {
            relevantProperties = getRelevantProperties(bean);
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException(e);
        }
        for (Map.Entry<String, PropertyEditor> entry : relevantProperties.entrySet()) {
            PropertyEditor editor = entry.getValue();
            editor.setAsText(values.get(entry.getKey()));
        }
        return bean;
    }

    public enum TestEnum {
            a, b, c
    }

    public static Object convertToCorrectClass(Class<?> type, Object value) {
        if (type.isInstance(value)) {
            return value;
        }
        if (type.isEnum()) {
            type.getEnumConstants();
            for (Object object : type.getEnumConstants()) {
                if (object.toString().equals(value)) {
                    return object;
                }
            }
        }
        if (String.class.isInstance(value)) {
            Constructor<?> constructor = getStringOnlyConstructor(type);
            if (constructor != null) {
                try {
                    return constructor.newInstance(value);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public static Constructor<?> getStringOnlyConstructor(Class<?> type) {
        try {
            return type.getConstructor(String.class);
        } catch (SecurityException e) {
            LOGGER.error("unexpected security-exception occured", e);
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private MethodUtil() {
    }
}
