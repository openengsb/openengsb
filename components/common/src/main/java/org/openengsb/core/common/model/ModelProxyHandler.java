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

package org.openengsb.core.common.model;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.edb.EDBConstants;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelId;
import org.openengsb.core.common.AbstractOpenEngSBInvocationHandler;
import org.openengsb.core.common.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates an implementation for a model interface. This class is only able to handle getters and setters, toString
 * and getOpenEngSBModelEntries of domain models.
 */
public class ModelProxyHandler extends AbstractOpenEngSBInvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelProxyHandler.class);
    private Map<String, OpenEngSBModelEntry> objects;
    private List<ModelEntryConverterStep> steps;

    public ModelProxyHandler(Class<?> model, OpenEngSBModelEntry... entries) {
        objects = new HashMap<String, OpenEngSBModelEntry>();
        fillModelWithNullValues(model);
        for (OpenEngSBModelEntry entry : entries) {
            if (objects.containsKey(entry.getKey()) || entry.getKey().equals(EDBConstants.MODEL_OID)
                    || entry.getKey().equals(EDBConstants.MODEL_VERSION)) {
                objects.put(entry.getKey(), entry);
            } else {
                LOGGER.error("entry \"{}\" can not be set because the interface doesn't contain this field!",
                    entry.getKey());
            }
        }
        initializeModelConverterSteps();
    }

    private void fillModelWithNullValues(Class<?> model) {
        for (PropertyDescriptor propertyDescriptor : ModelUtils.getPropertyDescriptorsForClass(model)) {
            String propertyName = propertyDescriptor.getName();
            Class<?> clasz = propertyDescriptor.getPropertyType();
            objects.put(propertyName, new OpenEngSBModelEntry(propertyName, null, clasz));
        }
    }

    @Override
    protected Object handleInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("getOpenEngSBModelEntries")) {
            LOGGER.debug("getOpenEngSBModelEntries was called");
            return handleGetOpenEngSBModelEntries();
        } else if (methodName.startsWith("set")) {
            LOGGER.debug("setter method \"{}\" was called with parameter {}", methodName, args[0]);
            handleSetMethod(method, args);
            return null;
        } else if (methodName.startsWith("get")) {
            LOGGER.debug("called getter method \"{}\" was called", methodName);
            return handleGetMethod(method);
        } else if (methodName.equals("toString")) {
            LOGGER.debug("toString() was called");
            return handleToString();
        } else if (methodName.equals("addOpenEngSBModelEntry")) {
            OpenEngSBModelEntry entry = (OpenEngSBModelEntry) args[0];
            LOGGER.debug("addOpenEngSBModelEntry was called with entry {} with the value {}", entry.getKey(),
                entry.getValue());
            handleAddEntry(entry);
            return null;
        } else if (methodName.equals("removeOpenEngSBModelEntry")) {
            LOGGER.debug("removeOpenEngSBModelEntry was called with key {} ", args[0]);
            handleRemoveEntry((String) args[0]);
            return null;
        } else if (methodName.equals("equals")) {
            LOGGER.debug("equals was called");
            if (args[0] == null || !OpenEngSBModel.class.isAssignableFrom(args[0].getClass())) {
                return false;
            }
            OpenEngSBModel otherModel = (OpenEngSBModel) args[0];
            List<OpenEngSBModelEntry> thisEntries = handleGetOpenEngSBModelEntries();
            List<OpenEngSBModelEntry> otherEntries = otherModel.getOpenEngSBModelEntries();
            return thisEntries.equals(otherEntries);
        } else if (methodName.equals("hashCode")) {
            LOGGER.debug("hashCode was called");
            return objects.hashCode();
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Unknown command \"").append(methodName).append(".");
            builder.append(this.getClass().getSimpleName()).append(" is able to handle ");
            builder.append("getters/setters/toString/equals/hashCode and the OpenEngSBModel functions ");
            LOGGER.error(builder.toString());
            throw new IllegalArgumentException(builder.toString());
        }
    }

    /**
     * Handle call of "addOpenEngSBModelEntry"
     */
    private void handleAddEntry(OpenEngSBModelEntry entry) {
        objects.put(entry.getKey(), entry);
    }

    /**
     * Handle call of "removeOpenEngSBModelEntry"
     */
    private void handleRemoveEntry(String key) {
        objects.remove(key);
    }

    /**
     * Handle call of "toString"
     */
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

    /**
     * Handle call of a setter method
     */
    private void handleSetMethod(Method method, Object[] args) throws Throwable {
        String propertyName = ModelUtils.getPropertyName(method);
        if (method.isAnnotationPresent(OpenEngSBModelId.class) && args[0] != null) {
            OpenEngSBModelEntry entry =
                new OpenEngSBModelEntry(EDBConstants.MODEL_OID, args[0].toString(), String.class);
            objects.put(EDBConstants.MODEL_OID, entry);
        }
        Class<?> clasz = method.getParameterTypes()[0];
        objects.put(propertyName, new OpenEngSBModelEntry(propertyName, args[0], clasz));
    }

    /**
     * Handle call of a getter method
     */
    private Object handleGetMethod(Method method) throws Throwable {
        String propertyName = ModelUtils.getPropertyName(method);
        checkForGetterResultConversion(propertyName);
        return objects.get(propertyName).getValue();
    }

    /**
     * Does the conversion of model entries before they are returned by the getter if needed.
     */
    private void checkForGetterResultConversion(String propertyName) {
        OpenEngSBModelEntry entry = objects.get(propertyName);
        if (List.class.isAssignableFrom(entry.getType())) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) entry.getValue();
            if (list != null && list.size() != 0) {
                List<Object> temp = doListConversion(list, true);
                objects.put(propertyName, new OpenEngSBModelEntry(propertyName, temp, entry.getType()));
            }
        } else if (Map.class.isAssignableFrom(entry.getType())) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) entry.getValue();
            if (map != null && map.size() != 0) {
                Map<Object, Object> temp = doMapConversion(map, true);
                objects.put(propertyName, new OpenEngSBModelEntry(propertyName, temp, entry.getType()));
            }
        } else {
            Object obj = doObjectConversion(entry.getValue(), true);
            Class<?> clazz = obj != null ? obj.getClass() : entry.getType();
            objects.put(propertyName, new OpenEngSBModelEntry(propertyName, obj, clazz));
        }
    }

    /**
     * Handle call of "getOpenEngSBModelEntries"
     */
    private List<OpenEngSBModelEntry> handleGetOpenEngSBModelEntries() throws Throwable {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        for (OpenEngSBModelEntry entry : objects.values()) {
            if (entry.getValue() == null) {
                entries.add(entry);
                continue;
            }
            entries.addAll(convertEntry(entry));
        }
        return entries;
    }

    /**
     * Does the conversion of model entries before they are added to the list of model entries if needed.
     */
    private List<OpenEngSBModelEntry> convertEntry(OpenEngSBModelEntry entry) {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        if (List.class.isAssignableFrom(entry.getType())) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) entry.getValue();
            if (list.size() == 0) {
                entries.add(entry);
            } else {
                List<Object> temp = doListConversion(list, false);
                entries.add(new OpenEngSBModelEntry(entry.getKey(), temp, entry.getType()));
            }
        } else if (Map.class.isAssignableFrom(entry.getType())) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) entry.getValue();
            if (map.size() == 0) {
                entries.add(entry);
            } else {
                Map<Object, Object> temp = doMapConversion(map, false);
                entries.add(new OpenEngSBModelEntry(entry.getKey(), temp, entry.getType()));
            }
        } else {
            Object obj = doObjectConversion(entry.getValue(), false);
            entries.add(new OpenEngSBModelEntry(entry.getKey(), obj, obj.getClass()));
        }
        return entries;
    }

    /**
     * Checks if an object needs to be converted and does the converting work
     */
    private Object doObjectConversion(Object value, boolean forGetter) {
        return doListConversion(Arrays.asList(value), forGetter).get(0);
    }

    /**
     * Checks if a list of objects needs to be converted and does the converting work
     */
    private List<Object> doListConversion(List<Object> values, boolean forGetter) {
        List<Object> temp = new ArrayList<Object>();
        for (ModelEntryConverterStep step : steps) {
            Object value = values.get(0);
            if (forGetter ? step.matchForGetter(value) : step.matchForGetModelEntries(value)) {
                for (Object val : values) {
                    Object obj = forGetter ? step.convertForGetter(val) : step.convertForGetModelEntries(val);
                    temp.add(obj);
                }
                return temp;
            }
        }
        return values;
    }

    /**
     * Checks if a map of objects needs to be converted and does the converting work
     */
    private Map<Object, Object> doMapConversion(Map<Object, Object> values, boolean forGetter) {
        Map<Object, Object> temp = new HashMap<Object, Object>();
        for (ModelEntryConverterStep step : steps) {
            Object key = values.keySet().iterator().next();
            Object value = values.values().iterator().next();
            if (forGetter ? step.matchForGetter(value) : step.matchForGetModelEntries(value)
                    || forGetter ? step.matchForGetter(key) : step.matchForGetModelEntries(key)) {
                for (Map.Entry<Object, Object> entry : values.entrySet()) {
                    Object tempKey = entry.getKey();
                    Object k = forGetter ? step.convertForGetter(tempKey) : step.convertForGetModelEntries(tempKey);
                    Object val = entry.getValue();
                    Object obj = forGetter ? step.convertForGetter(val) : step.convertForGetModelEntries(val);
                    temp.put(k, obj);
                }
                return temp;
            }
        }
        return values;
    }

    /**
     * Defines the list of conversion steps which should be checked every time a converting task has to be done. The
     * order of the steps is important and the default converter step have always to be the last one (because this
     * converter just forwards the old value).
     */
    private void initializeModelConverterSteps() {
        steps = new ArrayList<ModelEntryConverterStep>();
        steps.add(ModelConverterStep.getInstance());
        steps.add(FileConverterStep.getInstance());
        steps.add(DefaultConverterStep.getInstance());
    }
}
