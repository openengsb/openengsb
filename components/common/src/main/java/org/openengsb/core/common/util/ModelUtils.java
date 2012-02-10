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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.common.model.ModelProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * This static util class contains all necessary functions to deal with OpenEngSBModels (creating, converting).
 */
public final class ModelUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

    private ModelUtils() {
    }

    /**
     * Creates a proxy for the model interface which simulates an implementation of the interface.
     */
    @SuppressWarnings("unchecked")
    public static <T extends OpenEngSBModel> T createEmptyModelObject(Class<T> model, OpenEngSBModelEntry... entries) {
        LOGGER.debug("createEmpytModelObject for model interface {} called", model.getName());
        return (T) createModelObject(model, entries);
    }

    /**
     * Creates a proxy for the model interface which simulates an implementation of the interface. Only useable with
     * OpenEngSBModel interfaces. If the model parameter is not an OpenEngSBModel interface, an exception is thrown.
     */
    public static Object createModelObject(Class<?> model, OpenEngSBModelEntry... entries) {
        if (!OpenEngSBModel.class.isAssignableFrom(model)) {
            throw new IllegalArgumentException("OpenEngSBModel has to be deriveable from model parameter");
        }
        ClassLoader classLoader = model.getClassLoader();
        Class<?>[] classes = new Class<?>[]{ OpenEngSBModel.class, model };
        InvocationHandler handler = makeHandler(model, entries);

        return Proxy.newProxyInstance(classLoader, classes, handler);
    }

    private static ModelProxyHandler makeHandler(Class<?> model, OpenEngSBModelEntry[] entries) {
        ModelProxyHandler handler = new ModelProxyHandler(model, entries);
        return handler;
    }

    /**
     * Generates an OpenEngSBModelWrapper out of a model. This construct is needed so that the sending of
     * OpenEngSBModels over remote can work properly
     */
    public static OpenEngSBModelWrapper generateWrapperOutOfModel(OpenEngSBModel model) {
        OpenEngSBModelWrapper wrapper = new OpenEngSBModelWrapper();
        Class<?> clazz = ModelUtils.getModelClassOfOpenEngSBModelObject(model.getClass());
        wrapper.setEntries(model.getOpenEngSBModelEntries());
        wrapper.setModelClass(clazz.getName());
        return wrapper;
    }

    /**
     * Tries to generate a model of the given class out of a wrapper. If something went wrong (e.g. the model contained
     * in the wrapper is not of the class which is given as parameter) an illegal argument exception is thrown.
     */
    @SuppressWarnings("unchecked")
    public static <T extends OpenEngSBModel> T generateModelOutOfWrapper(OpenEngSBModelWrapper wrapper,
            Class<T> clazz) {
        Object modelObject = ModelUtils.generateModelOutOfWrapper(wrapper);
        if (modelObject != null) {
            try {
                return (T) modelObject;
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("Wrapper doesn't contain a model of class" + clazz.getName(), ex);
            }
        }
        throw new IllegalArgumentException("Wrapper doesn't contain a model of class" + clazz.getName());
    }

    /**
     * Tries to generate a model out of an OpenEngSB model wrapper. Without casting, only generating. If the model class
     * can't be found, an illegal argument exception is thrown.
     */
    public static Object generateModelOutOfWrapper(OpenEngSBModelWrapper wrapper) {
        return generateModelOutOfWrapper(wrapper, ModelUtils.class.getClassLoader());
    }

    /**
     * Tries to generate a model out of an OpenEngSB model wrapper. Uses the given class loader to load the model class.
     * Needed by sub models which aren't exported (like in the ITests).
     */
    public static Object generateModelOutOfWrapper(OpenEngSBModelWrapper wrapper, ClassLoader parentClassLoader) {
        OpenEngSBModelEntry[] entries = wrapper.getEntries().toArray(new OpenEngSBModelEntry[0]);
        Class<?> clazz;
        try {
            clazz = parentClassLoader.loadClass(wrapper.getModelClass());
            return ModelUtils.createModelObject(clazz, entries);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("The class of the model can't be found", ex);
        }
    }

    /**
     * Returns the real model class of a model (instead of "$ProxyX"). If the given class is not a model, an illegal
     * argument exception is thrown.
     */
    public static Class<?> getModelClassOfOpenEngSBModelObject(Class<?> clazz) {
        Class<?> result = null;
        for (Class<?> inter : clazz.getInterfaces()) {
            if (OpenEngSBModel.class.isAssignableFrom(inter) && !inter.equals(OpenEngSBModel.class)) {
                result = inter;
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("class " + clazz.getName() + " doesn't implement domain model object");
        }
        return result;
    }

    /**
     * Returns all property descriptors for a given class.
     */
    public static List<PropertyDescriptor> getPropertyDescriptorsForClass(Class<?> clasz) {
        List<PropertyDescriptor> properties = Lists.newArrayList();
        for (Class<?> parent : clasz.getInterfaces()) {
            if (!parent.equals(OpenEngSBModel.class)) {
                properties.addAll(getPropertyDescriptorsForClass(parent));
            }
        }
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

    /**
     * Tries to invoke a setter method of a given instance, with the given parameter object.
     */
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

    /**
     * Tries to invoke a getter method of the given instance.
     */
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

    /**
     * Converts the property name out of a method(e.g. getId -> id).
     */
    public static String getPropertyName(Method propertyMethod) {
        String propertyName = propertyMethod.getName().substring(3);
        char firstChar = propertyName.charAt(0);
        char newFirstChar = Character.toLowerCase(firstChar);
        return propertyName.replaceFirst("" + firstChar, "" + newFirstChar);
    }
}
