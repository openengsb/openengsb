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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelId;
import org.openengsb.core.common.AbstractOpenEngSBInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates an implementation for a model interface. This class is only able to handle getters and setters, toString
 * and getOpenEngSBModelEntries of domain models.
 */
public class EKBProxyHandler extends AbstractOpenEngSBInvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EKBProxyHandler.class);
    private Map<String, OpenEngSBModelEntry> objects;

    public EKBProxyHandler(Class<?> model, OpenEngSBModelEntry... entries) {
        objects = new HashMap<String, OpenEngSBModelEntry>();
        fillModelWithNullValues(model);
        for (OpenEngSBModelEntry entry : entries) {
            if (objects.containsKey(entry.getKey())) {
                objects.put(entry.getKey(), entry);
            } else {
                LOGGER.error("entry \"{}\" can not be set because the interface doesn't contain this field!",
                    entry.getKey());
            }
        }
    }

    private void fillModelWithNullValues(Class<?> model) {
        for (PropertyDescriptor propertyDescriptor : EKBUtils.getPropertyDescriptorsForClass(model)) {
            String propertyName = propertyDescriptor.getName();
            Class<?> clasz = propertyDescriptor.getPropertyType();
            objects.put(propertyName, new OpenEngSBModelEntry(propertyName, null, clasz));
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
        } else if (method.getName().equals("addOpenEngSBModelEntry")) {
            OpenEngSBModelEntry entry = (OpenEngSBModelEntry) args[0];
            LOGGER.debug("addOpenEngSBModelEntry was called with entry {} with the value {}", entry.getKey(),
                entry.getValue());
            handleAddEntry(entry);
            return null;
        } else if (method.getName().equals("removeOpenEngSBModelEntry")) {
            LOGGER.debug("removeOpenEngSBModelEntry was called with key {} ", args[0]);
            handleRemoveEntry((String) args[0]);
            return null;
        } else {
            LOGGER.error("EKBProxyHandler is only able to handle getters and setters");
            throw new IllegalArgumentException("EKBProxyHandler is only able to handle getters and setters");
        }
    }

    private void handleAddEntry(OpenEngSBModelEntry entry) {
        objects.put(entry.getKey(), entry);
    }

    private void handleRemoveEntry(String key) {
        objects.remove(key);
    }

    private void handleSetMethod(Method method, Object[] args) throws Throwable {
        String propertyName = EKBUtils.getPropertyName(method);
        if (method.isAnnotationPresent(OpenEngSBModelId.class) && args[0] != null) {
            objects.put("edbId", new OpenEngSBModelEntry("edbId", args[0].toString(), String.class));
        }
        Class<?> clasz = method.getParameterTypes()[0];
        objects.put(propertyName, new OpenEngSBModelEntry(propertyName, args[0], clasz));
    }

    private Object handleGetMethod(Method method) throws Throwable {
        String propertyName = EKBUtils.getPropertyName(method);
        return objects.get(propertyName).getValue();
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

    private Object handleGetOpenEngSBModelEntries() throws Throwable {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        for (OpenEngSBModelEntry entry : objects.values()) {
            Class<?> clasz = entry.getType();
            if (clasz.isEnum() && entry.getValue() != null) {
                entries.add(new OpenEngSBModelEntry(entry.getKey(), entry.getValue().toString(), String.class));
            } else if (List.class.isAssignableFrom(clasz)) {
                entries.addAll(createListElements((List<?>) entry.getValue(), entry.getKey()));
            } else if (clasz.isInterface() && entry.getValue() != null) {
                entries.addAll(createSubmodelElements(clasz, entry.getValue(), entry.getKey() + "."));
            } else {
                entries.add(entry);
            }
        }
        return entries;
    }

    private List<OpenEngSBModelEntry> createListElements(List<?> list, String propertyName) {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        if (list == null) {
            return entries;
        }
        Class<?> clazz = list.get(0).getClass();
        for (int i = 0; i < list.size(); i++) {
            if (clazz.isInterface() || clazz.getName().contains("$Proxy")) {
                entries.addAll(createSubmodelElements(clazz, list.get(i), propertyName + i + "."));
            } else {
                entries.add(new OpenEngSBModelEntry(propertyName + i, list.get(i), list.get(i).getClass()));
            }
        }
        return entries;
    }

    private List<OpenEngSBModelEntry> createSubmodelElements(Class<?> clazz, Object object, String propertyPrefix) {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        for (PropertyDescriptor propertyDescriptor : EKBUtils.getPropertyDescriptorsForClass(clazz)) {
            String propertyName = propertyPrefix + propertyDescriptor.getName();
            Object obj = EKBUtils.invokeGetterMethod(propertyDescriptor.getReadMethod(), object);
            entries.add(new OpenEngSBModelEntry(propertyName, obj, obj.getClass()));
        }
        return entries;
    }
}
