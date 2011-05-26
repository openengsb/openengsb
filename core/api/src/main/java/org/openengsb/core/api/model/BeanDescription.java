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

package org.openengsb.core.api.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Container class that is intended to ease serialization. It is especially useful when the Runtime-type of the class is
 * not known, or when doing Serialization on Object-hierarchies.
 *
 * Example: A {@link org.openengsb.core.api.security.model.SecureRequest} contains a field for
 * {@link org.openengsb.core.api.security.model.AuthenticationInfo}. There may exist many implementations of
 * {@link org.openengsb.core.api.security.model.AuthenticationInfo} which can be difficult to handle with some message
 * formats
 *
 * However this class has some limits. It only supports String and byte[] properties. If any other type is encountered
 * the "toString()"-method is invoked to transform it into the beandescription. To transform it back, the type is
 * searched for a constructor that only takes one String as argument.
 */
public class BeanDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private String className;
    private Map<String, String> data;
    private Map<String, byte[]> binaryData;

    protected BeanDescription(String className) {
        this.className = className;
        data = new HashMap<String, String>();
        binaryData = new HashMap<String, byte[]>();
    }

    public BeanDescription(String className, Map<String, String> data) {
        this.className = className;
        this.data = data;
    }

    public BeanDescription(String className, Map<String, String> data, Map<String, byte[]> binaryData) {
        this.className = className;
        this.data = data;
        this.binaryData = binaryData;
    }

    public static BeanDescription fromObject(Object bean) {
        String className = bean.getClass().getCanonicalName();
        BeanDescription desc = new BeanDescription(className);
        populateData(desc, bean);
        return desc;
    }

    public BeanDescription() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * reconstructs the original object the {@link BeanDescription} is representing.
     */
    public Object toObject() {
        Class<?> beanType = getBeanType();
        Collection<PropertyDescriptor> accessibleProperties = getAccessiblePropertiesFromBean(beanType);
        Object bean;
        try {
            bean = beanType.newInstance();
            for (PropertyDescriptor d : accessibleProperties) {
                doSetPropertyOnBean(bean, d);
            }
            return bean;
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private void doSetPropertyOnBean(Object bean, PropertyDescriptor d) throws IllegalAccessException,
        InvocationTargetException, InstantiationException {
        Class<?> propertyType = d.getPropertyType();
        if (byte[].class.isAssignableFrom(propertyType)) {
            d.getWriteMethod().invoke(bean, binaryData.get(d.getName()));
        } else {
            Object value = getPropertyValueFromString(d, propertyType);
            d.getWriteMethod().invoke(bean, value);
        }
    }

    private Object getPropertyValueFromString(PropertyDescriptor d, Class<?> propertyType)
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        String string = data.get(d.getName());
        if (propertyType.equals(String.class)) {
            return string;
        } else {
            Constructor<?> constructor;
            try {
                constructor = propertyType.getConstructor(String.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
            return constructor.newInstance(string);
        }
    }

    private Class<?> getBeanType() {
        ClassLoader loader = this.getClass().getClassLoader();
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * reconstructs the original object the {@link BeanDescription} is representing.
     */
    @SuppressWarnings("unchecked")
    public <T> T toObject(Class<T> type) {
        Class<?> beanType = getBeanType();
        Preconditions
            .checkArgument(type.isAssignableFrom(beanType), "types are not compatible (%s,%s)", type, beanType);
        return (T) toObject();
    }

    private static BeanDescription populateData(BeanDescription desc, Object bean) {
        Collection<PropertyDescriptor> relevantPropertyDescriptors = getAccessiblePropertiesFromBean(bean.getClass());
        for (PropertyDescriptor propertyDescriptor : relevantPropertyDescriptors) {
            if (byte[].class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                desc.binaryData.put(propertyDescriptor.getName(), (byte[]) getPropertyValue(bean, propertyDescriptor));
            } else if (!propertyDescriptor.getPropertyType().isArray()) {
                desc.data.put(propertyDescriptor.getName(), getPropertyValue(bean, propertyDescriptor).toString());
            }
        }
        return desc;
    }

    private static Collection<PropertyDescriptor> getAccessiblePropertiesFromBean(Class<?> beanClass) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException(e);
        }
        List<PropertyDescriptor> allPropertyDescriptors = Arrays.asList(beanInfo.getPropertyDescriptors());
        Collection<PropertyDescriptor> relevantPropertyDescriptors =
            Collections2.filter(allPropertyDescriptors, new Predicate<PropertyDescriptor>() {
                @Override
                public boolean apply(PropertyDescriptor input) {
                    Method writeMethod = input.getWriteMethod();
                    return writeMethod != null && Modifier.isPublic(writeMethod.getModifiers());
                }
            });
        return relevantPropertyDescriptors;
    }

    private static Object getPropertyValue(Object bean, PropertyDescriptor propertyDescriptor) {
        try {
            return propertyDescriptor.getReadMethod().invoke(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Map<String, byte[]> getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(Map<String, byte[]> binaryData) {
        this.binaryData = binaryData;
    }

}
