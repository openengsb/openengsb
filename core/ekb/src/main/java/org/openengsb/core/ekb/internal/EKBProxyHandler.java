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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.AbstractOpenEngSBInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates an implementation for a model interface. This class is only able to handle getters and setters of models.
 */
public class EKBProxyHandler extends AbstractOpenEngSBInvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EKBProxyHandler.class);
    private Map<String, OpenEngSBModelEntry> objects;

    @SuppressWarnings("rawtypes")
    public EKBProxyHandler(Method[] methods, OpenEngSBModelEntry... entries) {
        objects = new HashMap<String, OpenEngSBModelEntry>();
        for (Method method : methods) {
            if (method.getName().startsWith("set")) {
                String propertyName = getPropertyName(method.getName());
                Class clasz = method.getParameterTypes()[0];
                objects.put(propertyName, new OpenEngSBModelEntry(propertyName, null, clasz));
            }
        }
        for (OpenEngSBModelEntry entry : entries) {
            if (objects.containsKey(entry.getKey())) {
                objects.put(entry.getKey(), entry);
            } else {
                LOGGER.error("entry \"{}\" can not be set because the interface doesn't contain this field!",
                    entry.getKey());
            }
        }
    }

    @Override
    protected Object handleInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getOpenEngSBModelEntries")) {
            LOGGER.debug("getOpenEngSBModelEntries was called");
            return handleGetOpenEngSBModelEntries();
        } else if (method.getName().startsWith("set")) {
            LOGGER.debug("setter method \"{}\" was called with parameter {}", method.getName(), args[0]);
            handleSetMethod(method, args);
            return null;
        } else if (method.getName().startsWith("get")) {
            LOGGER.debug("called getter method \"{}\" was called", method.getName());
            return handleGetMethod(method);
        } else if (method.getName().equals("toString")) {
            LOGGER.debug("toString() was called");
            return handleToString();
        } else {
            LOGGER.error("EKBProxyHandler is only able to handle getters and setters");
            throw new IllegalArgumentException("EKBProxyHandler is only able to handle getters and setters");
        }
    }

    private void handleSetMethod(Method method, Object[] args) throws Throwable {
        String propertyName = getPropertyName(method.getName());
        Class<?> clasz = method.getParameterTypes()[0];

        objects.put(propertyName, new OpenEngSBModelEntry(propertyName, args[0], clasz));
    }

    private List<OpenEngSBModelEntry> createSubmodelElements(Class<?> clazz, Object object, String propertyPrefix) {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String propertyName = propertyPrefix + propertyDescriptor.getName();
                try {
                    Object obj = propertyDescriptor.getReadMethod().invoke(object);
                    entries.add(new OpenEngSBModelEntry(propertyName, obj, obj.getClass()));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("IllegalArgumentException while loading the value for property {}",
                        propertyDescriptor.getName());
                } catch (IllegalAccessException e) {
                    LOGGER.warn("IllegalAccessException while loading the value for property {}",
                        propertyDescriptor.getName());
                } catch (InvocationTargetException e) {
                    LOGGER.warn("InvocationTargetException while loading the value for property {}",
                        propertyDescriptor.getName());
                }
            }
        } catch (IntrospectionException ex) {
            LOGGER.error("instantiation exception while trying to create instance of class {}", clazz.getName());
        }
        return entries;
    }

    private Object handleGetMethod(Method method) throws Throwable {
        String propertyName = getPropertyName(method.getName());
        return objects.get(propertyName).getValue();
    }

    private String getPropertyName(String methodName) {
        String propertyName = methodName.substring(3);
        char firstChar = propertyName.charAt(0);
        char newFirstChar = Character.toLowerCase(firstChar);
        return propertyName.replaceFirst("" + firstChar, "" + newFirstChar);
    }

    private Object handleGetOpenEngSBModelEntries() throws Throwable {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        for (OpenEngSBModelEntry entry : objects.values()) {
            Class<?> clasz = entry.getType();
            if (List.class.isAssignableFrom(clasz)) {
                List<?> list = (List<?>) entry.getValue();
                if (list == null) {
                    continue;
                }
                Class<?> clazz = list.get(0).getClass();
                for (int i = 0; i < list.size(); i++) {
                    if (clazz.isInterface() || clazz.getName().contains("$Proxy")) {
                        entries.addAll(createSubmodelElements(clazz, list.get(i), entry.getKey() + i + "."));
                    } else {
                        entries.add(new OpenEngSBModelEntry(entry.getKey() + i, list.get(i), list.get(i).getClass()));
                    }
                }
            } else if (clasz.isInterface() && entry.getValue() != null) {
                entries.addAll(createSubmodelElements(clasz, entry.getValue(), entry.getKey() + "."));
            } else {
                entries.add(entry);
            }
        }
        return entries;
    }

    private String handleToString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        builder.append("{ ");
        for (OpenEngSBModelEntry entry : objects.values()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append(" == ").append(entry.getValue());
            first = false;
        }
        builder.append(" }");
        return builder.toString();
    }
}
