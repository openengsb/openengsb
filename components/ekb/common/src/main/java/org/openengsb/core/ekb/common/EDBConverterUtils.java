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

package org.openengsb.core.ekb.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.reflect.FieldUtils;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.ekb.api.EKBCommit;

/**
 * The EDBConverterUtils class provides some functionalities which are often needed for the transformations: EDBObjects
 * <-> Models
 */
// TODO: OPENENGSB-3361, there are many functions located here which would better fit in other models like to fit
// into the object oriented programming design. This class should be minimized as much as possible. 
public final class EDBConverterUtils {
    public static final String FILEWRAPPER_FILENAME_SUFFIX = ".filename";
    public static final String REFERENCE_PREFIX = "refersTo_";

    private EDBConverterUtils() {
    }

    /**
     * Loads the entry with the given key of a given model.
     */
    private static Object getOpenEngSBModelEntryValue(OpenEngSBModel model, String key) {
        for (OpenEngSBModelEntry entry : model.toOpenEngSBModelEntries()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Loads the version of a model if existing
     */
    public static Integer getModelVersion(OpenEngSBModel model) {
        return (Integer) getOpenEngSBModelEntryValue(model, EDBConstants.MODEL_VERSION);
    }

    /**
     * Creates the OID for a model
     */
    public static String createOID(OpenEngSBModel model, String contextId) {
        StringBuilder builder = new StringBuilder();
        builder.append(contextId).append("/");
        Object modelId = model.retrieveInternalModelId();
        if (modelId != null) {
            builder.append(modelId.toString());
        } else {
            builder.append(UUID.randomUUID().toString());
        }

        return builder.toString();
    }

    public static String createOID(Object model, String contextId) {
        if (!OpenEngSBModel.class.isAssignableFrom(model.getClass())) {
            throw new IllegalArgumentException("This function need to get a model passed.");
        }
        return createOID((OpenEngSBModel) model, contextId);
    }

    /**
     * Gets the information about domain, connector and instance of an EKBCommit object and returns the corresponding
     * ConnectorInformation object.
     */
    public static ConnectorInformation getConnectorInformationOfEKBCommit(EKBCommit commit) {
        String domainId = commit.getDomainId();
        String connectorId = commit.getConnectorId();
        String instanceId = commit.getInstanceId();
        return new ConnectorInformation(domainId, connectorId, instanceId);
    }

    /**
     * Returns the entry name for a map key in the EDB format. E.g. the map key for the property "map" with the index 0
     * would be "map.0.key".
     */
    public static String getEntryNameForMapKey(String property, Integer index) {
        return getEntryNameForMap(property, true, index);
    }

    /**
     * Returns the entry name for a map value in the EDB format. E.g. the map value for the property "map" with the
     * index 0 would be "map.0.value".
     */
    public static String getEntryNameForMapValue(String property, Integer index) {
        return getEntryNameForMap(property, false, index);
    }

    /**
     * Returns the entry name for a map element in the EDB format. The key parameter defines if the entry name should be
     * generated for the key or the value of the map. E.g. the map key for the property "map" with the index 0 would be
     * "map.0.key".
     */
    private static String getEntryNameForMap(String property, Boolean key, Integer index) {
        return String.format("%s.%d.%s", property, index, key ? "key" : "value");
    }

    /**
     * Returns the entry name for a list element in the EDB format. E.g. the list element for the property "list" with
     * the index 0 would be "list.0".
     */
    public static String getEntryNameForList(String property, Integer index) {
        return String.format("%s.%d", property, index);
    }

    /**
     * Adds to the EDBObject special entries which mark that a model is referring to other models through
     * OpenEngSBForeignKey annotations
     */
    public static void fillEDBObjectWithEngineeringObjectInformation(EDBObject object, OpenEngSBModel model)
        throws IllegalAccessException {
        if (!new SimpleModelWrapper(model).isEngineeringObject()) {
            return;
        }
        for (Field field : model.getClass().getDeclaredFields()) {
            OpenEngSBForeignKey annotation = field.getAnnotation(OpenEngSBForeignKey.class);
            if (annotation == null) {
                continue;
            }
            String value = (String) FieldUtils.readField(field, model, true);
            if (value == null) {
                continue;
            }
            String key = getEOReferenceStringFromAnnotation(annotation);
            object.put(key, new EDBObjectEntry(key, value, String.class));
        }
    }
    
    /**
     * Converts an OpenEngSBForeignKey annotation to the fitting format which will be added to an EDBObject.
     */
    public static String getEOReferenceStringFromAnnotation(OpenEngSBForeignKey key) {
        return String.format("%s%s:%s", REFERENCE_PREFIX, key.modelType(),
            key.modelVersion().toString());
    }
    
    /**
     * Filters the reference prefix values added in the model to EDBObject conversion out of the EDBObject
     */
    public static void filterEngineeringObjectInformation(EDBObject object, Class<?> model) {
        if (!SimpleModelWrapper.isEngineeringObjectClass(model)) {
            return;
        }
        Set<String> keySet = object.keySet();
        List<String> deletes = new ArrayList<String>();
        for (String key : keySet) {
            if (key.startsWith(REFERENCE_PREFIX)) {
                deletes.add(key);
            }
        }
        for (String delete : deletes) {
            object.remove(delete);
        }
    }
}
