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

package org.openengsb.core.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * This static util class contains all necessary functions to deal with OpenEngSBModels.
 */
public final class ModelUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);
    public static final String MODEL_TAIL_FIELD_NAME = "openEngSBModelTail";

    private ModelUtils() {
    }

    /**
     * Performs the getOpenEngSBModelEntries function on a model object. Throws an IllegalArgumentException if the
     * passed object is not an OpenEngSBModel instance.
     */
    public static List<OpenEngSBModelEntry> getOpenEngSBModelEntries(Object model) {
        checkIfObjectIsModel(model);
        return ((OpenEngSBModel) model).toOpenEngSBModelEntries();
    }

    /**
     * Performs the addOpenEngSBModelEntry function on a model object. Throws an IllegalArgumentException if the passed
     * object is not an OpenEngSBModel instance.
     */
    public static void addOpenEngSBModelEntry(Object model, OpenEngSBModelEntry entry) {
        checkIfObjectIsModel(model);
        ((OpenEngSBModel) model).addOpenEngSBModelEntry(entry);
    }

    /**
     * Performs the removeOpenEngSBModelEntry function on a model object. Throws an IllegalArgumentException if the
     * passed object is not an OpenEngSBModel instance.
     */
    public static void removeOpenEngSBModelEntry(Object model, String key) {
        checkIfObjectIsModel(model);
        ((OpenEngSBModel) model).removeOpenEngSBModelEntry(key);
    }

    /**
     * Performs the retrieveInternalModelId function on a model object. Throws an IllegalArgumentException if the passed
     * object is not an OpenEngSBModel instance.
     */
    public static Object getInternalModelId(Object model) {
        checkIfObjectIsModel(model);
        return ((OpenEngSBModel) model).retrieveInternalModelId();
    }
    
    /**
     * Performs the retrieveInternalModelTimestamp function on a model object. Throws an IllegalArgumentException if the
     * passed object is not an OpenEngSBModel instance.
     */
    public static Long retrieveInternalModelTimestamp(Object model) {
        checkIfObjectIsModel(model);
        return ((OpenEngSBModel) model).retrieveInternalModelTimestamp();
    }
    
    /**
     * Performs the retrieveInternalModelVersion function on a model object. Throws an IllegalArgumentException if the
     * passed object is not an OpenEngSBModel instance.
     */
    public static Integer retrieveInternalModelVersion(Object model) {
        checkIfObjectIsModel(model);
        return ((OpenEngSBModel) model).retrieveInternalModelVersion();
    }

    /**
     * Performs the retrieveModelName function on a model object. Throws an IllegalArgumentException if the passed
     * object is not an OpenEngSBModel instance.
     */
    public static String retrieveModelName(Object model) {
        checkIfObjectIsModel(model);
        return ((OpenEngSBModel) model).retrieveModelName();
    }
    
    /**
     * Performs the retrieveModelVersion function on a model object. Throws an IllegalArgumentException if the passed
     * object is not an OpenEngSBModel instance.
     */
    public static String retrieveModelVersion(Object model) {
        checkIfObjectIsModel(model);
        return ((OpenEngSBModel) model).retrieveModelVersion();
    }

    /**
     * Performs the getOpenEngSBModelTail function on a model object. Throws an IllegalArgumentException if the passed
     * object is not an OpenEngSBModel instance.
     */
    public static List<OpenEngSBModelEntry> getOpenEngSBModelTail(Object model) {
        checkIfObjectIsModel(model);
        return ((OpenEngSBModel) model).getOpenEngSBModelTail();
    }

    /**
     * Performs the setOpenEngSBModelTail function on a model object. Throws an IllegalArgumentException if the passed
     * object is not an OpenEngSBModel instance.
     */
    public static void setOpenEngSBModelTail(Object model, List<OpenEngSBModelEntry> entries) {
        checkIfObjectIsModel(model);
        ((OpenEngSBModel) model).setOpenEngSBModelTail(entries);
    }

    /**
     * Creates a model of the given type and uses the list of OpenEngSBModelEntries as initialization data.
     */
    public static <T> T createModel(Class<T> model, List<OpenEngSBModelEntry> entries) {
        checkIfClassIsModel(model);
        try {
            T instance = model.newInstance();
            for (OpenEngSBModelEntry entry : entries) {
                if (tryToSetValueThroughField(entry, instance)) {
                    continue;
                }
                if (tryToSetValueThroughSetter(entry, instance)) {
                    continue;
                }
                ((OpenEngSBModel) instance).addOpenEngSBModelEntry(entry);
            }
            return instance;
        } catch (InstantiationException e) {
            LOGGER.error("InstantiationException while creating a new model instance.", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("IllegalAccessException while creating a new model instance.", e);
        } catch (SecurityException e) {
            LOGGER.error("SecurityException while creating a new model instance.", e);
        }
        return null;
    }

    /**
     * Tries to set the value of an OpenEngSBModelEntry to its corresponding field of the model. Returns true if the
     * field can be set, returns false if not.
     */
    private static boolean tryToSetValueThroughField(OpenEngSBModelEntry entry, Object instance)
        throws IllegalAccessException {
        try {
            Field field = instance.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(instance, entry.getValue());
            field.setAccessible(false);
            return true;
        } catch (NoSuchFieldException e) {
            // if no field with this name exist, try to use the corresponding setter
        } catch (SecurityException e) {
            // if a security manager is installed which don't allow this change of a field value, try
            // to use the corresponding setter
        }
        return false;
    }

    /**
     * Tries to set the value of an OpenEngSBModelEntry to its corresponding setter of the model. Returns true if the
     * setter can be called, returns false if not.
     */
    private static boolean tryToSetValueThroughSetter(OpenEngSBModelEntry entry, Object instance)
        throws IllegalAccessException {
        try {
            String setterName = getSetterName(entry.getKey());
            Method method = instance.getClass().getMethod(setterName, entry.getType());
            method.invoke(instance, entry.getValue());
            return true;
        } catch (NoSuchMethodException e) {
            // if there exist no such method, then it is an entry meant for the model tail
        } catch (IllegalArgumentException e) {
            LOGGER.error("IllegalArgumentException while trying to set values for the new model.", e);
        } catch (InvocationTargetException e) {
            LOGGER.error("InvocationTargetException while trying to set values for the new model.", e);
        }
        return false;
    }

    /**
     * Checks if the given object is an OpenEngSBModel. Throws an IllegalArgumentException if not.
     */
    public static void checkIfObjectIsModel(Object model) {
        if (!OpenEngSBModel.class.isAssignableFrom(model.getClass())) {
            throw new IllegalArgumentException("The given object is no model");
        }
    }

    /**
     * Returns true if the given object is an OpenEngSBModel, returns false if not.
     */
    public static boolean isObjectModel(Object model) {
        return OpenEngSBModel.class.isAssignableFrom(model.getClass());
    }

    /**
     * Returns true if the given object is an OpenEngSBModel and is also an Engineering Object, which means that there
     * is at least one OpenEngSBForeignKey field.
     */
    public static boolean isEngineeringObject(Object model) {
        if (!isObjectModel(model)) {
            return false;
        }
        for (Field field : model.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(OpenEngSBForeignKey.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given class is an OpenEngSBModel. Throws an IllegalArgumentException if not.
     */
    public static void checkIfClassIsModel(Class<?> clazz) {
        if (!OpenEngSBModel.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("The given class is no model");
        }
    }

    /**
     * Returns true if the given class is an OpenEngSBModel, returns false if not.
     */
    public static boolean isClassModel(Class<?> clazz) {
        return OpenEngSBModel.class.isAssignableFrom(clazz);
    }

    /**
     * Returns all property descriptors for a given class.
     */
    public static List<PropertyDescriptor> getPropertyDescriptorsForClass(Class<?> clasz) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clasz);
            return Arrays.asList(beanInfo.getPropertyDescriptors());
        } catch (IntrospectionException e) {
            LOGGER.error("instantiation exception while trying to create instance of class {}", clasz.getName());
        }
        return Lists.newArrayList();
    }

    private static String getSetterName(String propertyName) {
        return String.format("%s%s%s", "set", (propertyName.charAt(0) + "").toUpperCase(), propertyName.substring(1));
    }
}
