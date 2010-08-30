/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

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

import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.AttributeDefinition.Builder;

public class MethodUtil {
    public static List<Method> getServiceMethods(Object service) {
        List<Method> result = new ArrayList<Method>();
        for (Class<?> serviceInterface : service.getClass().getInterfaces()) {
            result.addAll(Arrays.asList(serviceInterface.getMethods()));
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
                Builder builder = AttributeDefinition.builder();
                builder.name(propertyDescriptor.getDisplayName());
                builder.description(propertyDescriptor.getShortDescription());
                builder.id(propertyDescriptor.getName());
                attributes.add(builder.build());
            }
        } catch (IntrospectionException ex) {
            SendEventPage.log.error("building attribute list failed", ex);
        }
        return attributes;
    }
}
