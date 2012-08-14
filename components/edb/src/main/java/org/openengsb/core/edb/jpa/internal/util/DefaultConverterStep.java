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

package org.openengsb.core.edb.jpa.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.jpa.internal.JPAEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

/**
 * The DefaultConverterStep is the step which shall be used if there was no other match in the converter steps before.
 * This class simply transforms objects into strings in the one way and tries to reverse the string creation in the
 * other way.
 */
public class DefaultConverterStep implements EDBConverterStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConverterStep.class);

    @Override
    public Boolean doesStepFit(String classname) {
        return true;
    }

    @Override
    public JPAEntry convertToJPAEntry(EDBObjectEntry entry) {
        return new JPAEntry(entry.getKey(), entry.getValue().toString(), entry.getType());
    }

    @Override
    public EDBObjectEntry convertToEDBObjectEntry(JPAEntry entry) {
        EDBObjectEntry result = new EDBObjectEntry();
        result.setKey(entry.getKey());
        result.setType(entry.getType());
        result.setValue(getEntryValue(entry));
        return result;
    }

    /**
     * Tries to get the object value for a given JPAEntry. To instantiate the type first the static method "valueOf" of
     * the type will be tried. If that didn't work, then the constructor of the object with a string parameter is used.
     * If that didn't work either, the simple string will be set in the entry.
     */
    public Object getEntryValue(JPAEntry entry) {
        try {
            Class<?> typeClass = loadClass(entry.getType());
            if (typeClass == null) {
                return entry.getValue();
            }
            Object result = invokeValueOf(typeClass, entry.getValue());
            if (result != null) {
                return result;
            }
            Constructor<?> constructor = ClassUtils.getConstructorIfAvailable(typeClass, String.class);
            return constructor.newInstance(entry.getValue());
        } catch (IllegalAccessException e) {
            LOGGER.error("IllegalAccessException when trying to create object of type {}", entry.getType(), e);
        } catch (InvocationTargetException e) {
            LOGGER.error("InvocationTargetException when trying to create object of type {}", entry.getType(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("IllegalArgumentException when trying to create object of type {}", entry.getType(), e);
        } catch (InstantiationException e) {
            LOGGER.error("InstantiationException when trying to create object of type {}", entry.getType(), e);
        }
        return entry.getType();
    }

    /**
     * Tries to load the class with the given name. Returns the class object if the class can be loaded. Returns null if
     * the class could not be loaded.
     */
    private Class<?> loadClass(String className) {
        try {
            return EDBUtils.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Class {} can not be found by the EDB. This object type is not supported by the EDB",
                className);
        }
        return null;
    }

    /**
     * Tries to invoke the method valueOf of the given class object. If this method can be called, the result will be
     * given back based on the given value which is used as parameter for the method. If this method can't be called
     * null will be given back.
     */
    private Object invokeValueOf(Class<?> clazz, String value) throws IllegalAccessException,
        InvocationTargetException {
        try {
            return MethodUtils.invokeExactStaticMethod(clazz, "valueOf", value);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
