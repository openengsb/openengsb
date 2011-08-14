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

package org.openengsb.core.common.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public final class BeanUtils2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils2.class);

    protected static Iterator<PropertyDescriptor> getRelevantProperties(final Object bean)
        throws IntrospectionException {
        Class<?> beanClass = bean.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Iterator<PropertyDescriptor> propertyIterator = Iterators.forArray(propertyDescriptors);
        return Iterators.filter(propertyIterator, new Predicate<PropertyDescriptor>() {
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
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> buildAttributeMap(Object bean) {
        try {
            return BeanUtils.describe(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T buildBeanFromAttributeMap(Class<T> beanClass, Map<String, String> values) {
        T bean;
        try {
            bean = beanClass.newInstance();
            BeanUtils.populate(bean, values);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
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

    private BeanUtils2() {
    }
}
