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

import java.util.UUID;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.ekb.api.EKBCommit;

/**
 * The EDBConverterUtils class provides some functionalities which are often needed for the transformations: EDBObjects
 * <-> Models
 */
public final class EDBConverterUtils {
    public static final String FILEWRAPPER_FILENAME_SUFFIX = ".filename";

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
    public static String createOID(OpenEngSBModel model, String domainId, String connectorId) {
        StringBuilder builder = new StringBuilder();
        builder.append(createOIDPrefix(domainId, connectorId));
        Object modelId = model.retrieveInternalModelId();
        if (modelId != null) {
            builder.append(modelId.toString());
        } else {
            builder.append(UUID.randomUUID().toString());
        }

        return builder.toString();
    }

    public static String createOID(Object model, String domainId, String connectorId) {
        if (!OpenEngSBModel.class.isAssignableFrom(model.getClass())) {
            throw new IllegalArgumentException("This function need to get a model passed.");
        }
        return createOID((OpenEngSBModel) model, domainId, connectorId);
    }

    /**
     * Creates the OID prefix for a model
     */
    private static String createOIDPrefix(String domainId, String connectorId) {
        StringBuilder builder = new StringBuilder();
        builder.append(domainId).append("/").append(connectorId).append("/");
        return builder.toString();
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
}
