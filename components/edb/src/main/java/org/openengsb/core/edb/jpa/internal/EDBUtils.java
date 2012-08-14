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

    public static EDBObjectEntry convertJPAEntryToEDBObjectEntry(JPAEntry entry) {
        EDBObjectEntry result = new EDBObjectEntry();
        result.setKey(entry.getKey());
        result.setType(entry.getType());
        result.setValue(getEntryValue(entry));
        return result;
    }

    public static Object getEntryValue(JPAEntry entry) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return entry.getType();
    }

    private static Class<?> loadClass(String className) {
        try {
            return EDBUtils.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Class {} can not be found by the EDB. This object type is not supported by the EDB",
                className);
        }
        return null;
    }

    private static Object invokeValueOf(Class<?> clazz, String value) throws IllegalAccessException,
        InvocationTargetException {
        try {
            return MethodUtils.invokeExactStaticMethod(clazz, "valueOf", value);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static JPAEntry convertEDBObjectEntryToJPAEntry(EDBObjectEntry entry) {
        return new JPAEntry(entry.getKey(), entry.getValue().toString(), entry.getType());
    }

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
}
