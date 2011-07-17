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

package org.openengsb.core.ekb.internal;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class EKBUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EKBUtils.class);

    private EKBUtils() {
    }

    public static List<PropertyDescriptor> getPropertyDescriptorsForClass(Class<?> clasz) {
        List<PropertyDescriptor> properties = Lists.newArrayList();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clasz);
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                properties.add(descriptor);
            }
        } catch (IntrospectionException e) {
            LOGGER.error("instantiation exception while trying to create instance of class {}", clasz.getName());
        }
        return properties;
    }

    public static void invokeSetterMethod(Method setterMethod, Object instance, Object parameter) {
        try {
            setterMethod.invoke(instance, parameter);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("illegal argument exception when invoking {} with argument {}",
                setterMethod.getName(), parameter);
        } catch (IllegalAccessException ex) {
            LOGGER.error("illegal access exception when invoking {} with argument {}",
                setterMethod.getName(), parameter);
        } catch (InvocationTargetException ex) {
            LOGGER.error("invocatin target exception when invoking {} with argument {}",
                setterMethod.getName(), parameter);
        }
    }

    public static Object invokeGetterMethod(Method getterMethod, Object instance) {
        try {
            return getterMethod.invoke(instance);
        } catch (IllegalArgumentException e) {
            LOGGER.error("IllegalArgumentException while loading the value for property {}",
                getPropertyName(getterMethod));
        } catch (IllegalAccessException e) {
            LOGGER.error("IllegalAccessException while loading the value for property {}",
                getPropertyName(getterMethod));
        } catch (InvocationTargetException e) {
            LOGGER.error("InvocationTargetException while loading the value for property {}",
                getPropertyName(getterMethod));
        }
        return null;
    }

    public static String getPropertyName(Method propertyMethod) {
        String propertyName = propertyMethod.getName().substring(3);
        char firstChar = propertyName.charAt(0);
        char newFirstChar = Character.toLowerCase(firstChar);
        return propertyName.replaceFirst("" + firstChar, "" + newFirstChar);
    }

}
