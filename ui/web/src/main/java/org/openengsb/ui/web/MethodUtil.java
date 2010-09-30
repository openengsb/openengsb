/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.l10n.PassThroughStringLocalizer;

public final class MethodUtil {
    private static Log log = LogFactory.getLog(MethodUtil.class);

    public static List<Method> getServiceMethods(Object service) {
        List<Method> result = new ArrayList<Method>();
        for (Class<?> serviceInterface : service.getClass().getInterfaces()) {
            if (Domain.class.isAssignableFrom(serviceInterface)) {
                result.addAll(Arrays.asList(serviceInterface.getDeclaredMethods()));
            }
        }
        return result;
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
                AttributeDefinition a = AttributeDefinition.builder(new PassThroughStringLocalizer())
                        .id(propertyDescriptor.getName())
                        .name(propertyDescriptor.getDisplayName())
                        .description(propertyDescriptor.getShortDescription()).build();
                attributes.add(a);
            }
        } catch (IntrospectionException ex) {
            log.error("building attribute list failed", ex);
        }
        return attributes;
    }

    public static Object buildBean(Class<?> beanClass, Map<String, String> values) {
        try {
            Object obj = beanClass.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getWriteMethod() == null
                        || !Modifier.isPublic(propertyDescriptor.getWriteMethod().getModifiers())) {
                    continue;
                }
                propertyDescriptor.getWriteMethod().invoke(obj, values.get(propertyDescriptor.getName()));
            }
            return obj;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private MethodUtil() {
    }
}
