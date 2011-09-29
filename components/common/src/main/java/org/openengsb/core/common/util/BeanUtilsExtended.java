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
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Provides utility-methods for converting beans to something that can be stored (Map) and vice versa. It uses
 * {@link BeanUtils} to provide that functionality.
 */
public final class BeanUtilsExtended {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtilsExtended.class);

    /**
     * Analyzes the bean and returns a map containing the property-values. Works similar to
     * {@link BeanUtils#describe(Object)} but does not convert everything to strings.
     *
     * @throws IllegalArgumentException if the bean cannot be analyzed properly. Propably because some getter throw an
     *         Exception
     */
    public static Map<String, Object> buildObjectAttributeMap(Object bean) throws IllegalArgumentException {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
        } catch (IntrospectionException e1) {
            throw new IllegalArgumentException(e1);
        }
        Map<String, Object> result;
        result = Maps.newHashMap();
        for (PropertyDescriptor pDesc : beanInfo.getPropertyDescriptors()) {
            String name = pDesc.getName();
            Object propertyValue;
            try {
                propertyValue = pDesc.getReadMethod().invoke(bean);
            } catch (IllegalAccessException e) {
                // this should never happen since the Introspector only returns accessible read-methods
                LOGGER.error("WTF: got property descriptor with inaccessible read-method");
                throw new IllegalStateException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
            if (propertyValue != null) {
                result.put(name, propertyValue);
            }
        }
        return result;
    }

    /**
     * Creates a new instance of the beanType and populates it with the property-values from the map
     *
     * @throws IllegalArgumentException if the bean cannot be populated because of errors in the definition of the
     *         beantype
     */
    public static <BeanType> BeanType createBeanFromAttributeMap(Class<BeanType> beanType,
            Map<String, ? extends Object> attributeValues) {
        BeanType instance;
        try {
            instance = beanType.newInstance();
            BeanUtils.populate(instance, attributeValues);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
        return instance;
    }

    /**
     * returns the result of {@link BeanUtils#describe(Object)} converted to the proper generic map-type.
     *
     * Exceptions from Reflection are wrapped in {@link IllegalArgumentException}s
     *
     * @throws IllegalArgumentException if some property in the bean cannot be accessed
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> buildStringAttributeMap(Object bean) throws IllegalArgumentException {
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

    private BeanUtilsExtended() {
    }

}
