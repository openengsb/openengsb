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
package org.openengsb.core.edbi.jdbc.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Introspector
 */
public final class Introspector {

    private static final Logger LOG = LoggerFactory.getLogger(Introspector.class);

    private Introspector() {
        // static class
    }

    /**
     * Returns a create of all property names and their respective type.
     * 
     * @param clazz the class to introspect
     * @return a mapping between property names and their type.
     */
    public static Map<String, Class<?>> getPropertyTypeMap(Class<?> clazz) {
        return getPropertyTypeMap(clazz, "class");
    }

    /**
     * Returns a create of all property names and their respective type. Any property passed via exclude are removed
     * from the create before hand.
     * 
     * @param clazz the class to introspect
     * @param exclude the properties to exclude
     * @return a mapping between property names and their type.
     */
    public static Map<String, Class<?>> getPropertyTypeMap(Class<?> clazz, String... exclude) {
        PropertyDescriptor[] descriptors;

        try {
            descriptors = getPropertyDescriptors(clazz);
        } catch (IntrospectionException e) {
            LOG.error("Failed to introspect " + clazz, e);
            return Collections.emptyMap();
        }

        HashMap<String, Class<?>> map = new HashMap<>(descriptors.length);

        for (PropertyDescriptor pd : descriptors) {
            map.put(pd.getName(), pd.getPropertyType());
        }

        for (String property : exclude) {
            map.remove(property);
        }

        return map;
    }

    /**
     * Returns a create of all property names and their values contained in the given object.
     * 
     * @param object the object to visit out of
     * @return the extracted value create
     */
    public static Map<String, Object> read(Object object) {
        PropertyDescriptor[] descriptors;
        Class<?> clazz = object.getClass();

        try {
            descriptors = getPropertyDescriptors(clazz);
        } catch (IntrospectionException e) {
            LOG.error("Failed to introspect " + clazz, e);
            return Collections.emptyMap();
        }

        HashMap<String, Object> map = new HashMap<>(descriptors.length);

        for (PropertyDescriptor pd : descriptors) {
            try {
                Object value = pd.getReadMethod().invoke(object);
                map.put(pd.getName(), value);
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                LOG.error("Failed to visit property " + pd.getName(), e);
            }
        }

        map.remove("class");

        return map;
    }

    /**
     * Returns all {@link java.lang.reflect.Field}s declared in the given {@link Class} that are annotated with the
     * given {@link java.lang.annotation.Annotation}.
     * 
     * @param clazz the class to introspect
     * @param annotation the annotation to look for
     * @return a collection of {@link java.lang.reflect.Field}s that are annotated with the given annotation
     */
    public static List<Field> getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> annotatedFields = new ArrayList<>(fields.length);

        for (Field f : fields) {
            if (f.isAnnotationPresent(annotation)) {
                annotatedFields.add(f);
            }
        }

        return annotatedFields;
    }

    /**
     * Uses {@link #getAnnotatedFields(Class, Class)} to look for a field annotated with the {@code OpenEngSBModelId}
     * annotation, and if present, returns its name.
     * 
     * @param modelClass the class to introspect
     * @return the name of the {@link java.lang.reflect.Field} annotated with {@code OpenEngSBModelId}, or null
     */
    public static String getOpenEngSBModelIdProperty(Class<?> modelClass) {
        List<Field> fields = getAnnotatedFields(modelClass, OpenEngSBModelId.class);

        if (fields.isEmpty()) {
            return null;
        }

        return fields.get(0).getName();
    }

    /**
     * Checks whether the given object is a OpenEngSBModel (i.e. is either assignable by OpenEngSBModel or has the Model
     * annotation.
     * 
     * @param object the object to check
     * @return true if the object is a model
     */
    public static boolean isModel(Object object) {
        return isModelClass(object.getClass());
    }

    /**
     * Checks whether the given class is a OpenEngSBModel class (i.e. is either assignable by OpenEngSBModel or has the
     * Model annotation.
     * 
     * @param clazz the class to check
     * @return true if the class is a model
     */
    public static boolean isModelClass(Class<?> clazz) {
        return OpenEngSBModel.class.isAssignableFrom(clazz) || clazz.isAnnotationPresent(Model.class);
    }

    /**
     * Returns descriptors for all properties of the bean. May return {@code null} if the information should be obtained
     * by automatic analysis.
     * 
     * @param beanClass the class to introspect
     * @return an array of {@code PropertyDescriptor}s describing all properties supported by the bean or {@code null}
     */
    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> beanClass) throws IntrospectionException {
        BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(beanClass);

        return beanInfo.getPropertyDescriptors();
    }

}
