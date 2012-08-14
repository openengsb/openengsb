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

package org.openengsb.core.edb.jpa.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

/**
 * The EDBUtils class contains functions needed in the whole EDB implementation.
 */
public class EDBUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EDBUtils.class);

    private EDBUtils() {
    }

    /**
     * Converts a JPAEntry object into an EDBObjectEntry element. If there is a problem with the instantiation of the
     * type of the JPAEntry, the simple string object will be written in the resulting element. To instantiate the type
     * first the static method "valueOf" of the type will be tried. If that didn't work, then the constructor of the
     * object with a string parameter is used. If that didn't work either, the simple string will be set in the entry.
     */
    public static EDBObjectEntry convertJPAEntryToEDBObjectEntry(JPAEntry entry) {
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
    public static Object getEntryValue(JPAEntry entry) {
        if (entry.getType().equals(String.class.getName())) {
            return entry.getValue();
        }
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
    private static Class<?> loadClass(String className) {
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
    private static Object invokeValueOf(Class<?> clazz, String value) throws IllegalAccessException,
        InvocationTargetException {
        try {
            return MethodUtils.invokeExactStaticMethod(clazz, "valueOf", value);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Converts a JPAEntry object into an EDBObjectEntry.
     */
    public static JPAEntry convertEDBObjectEntryToJPAEntry(EDBObjectEntry entry) {
        return new JPAEntry(entry.getKey(), entry.getValue().toString(), entry.getType());
    }

    /**
     * Converts a JPAObject object into an EDBObject.
     */
    public static EDBObject convertJPAObjectToEDBObject(JPAObject object) {
        EDBObject result = new EDBObject(object.getOID());
        for (JPAEntry kvp : object.getEntries()) {
            EDBObjectEntry entry = convertJPAEntryToEDBObjectEntry(kvp);
            result.put(entry.getKey(), entry);
        }
        result.setDeleted(object.isDeleted());
        result.updateTimestamp(object.getTimestamp());
        return result;
    }

    /**
     * Converts an EDBObject object into a JPAObject object.
     */
    public static JPAObject convertEDBObjectToJPAObject(EDBObject object) {
        JPAObject result = new JPAObject();
        result.setTimestamp(object.getTimestamp());
        result.setOID(object.getOID());
        result.setDeleted(object.isDeleted());
        List<JPAEntry> entries = new ArrayList<JPAEntry>();
        for (EDBObjectEntry entry : object.values()) {
            entries.add(convertEDBObjectEntryToJPAEntry(entry));
        }
        result.setEntries(entries);
        return result;
    }

    /**
     * Converts a list of EDBObjects into a list of JPAObjects
     */
    public static List<JPAObject> convertEDBObjectsToJPAObjects(List<EDBObject> objects) {
        List<JPAObject> result = new ArrayList<JPAObject>();
        for (EDBObject object : objects) {
            result.add(convertEDBObjectToJPAObject(object));
        }
        return result;
    }

    /**
     * Converts a list of JPAObjects into a list of EDBObjects
     */
    public static List<EDBObject> convertJPAObjectsToEDBObjects(List<JPAObject> objects) {
        List<EDBObject> result = new ArrayList<EDBObject>();
        for (JPAObject object : objects) {
            result.add(convertJPAObjectToEDBObject(object));
        }
        return result;
    }
}
