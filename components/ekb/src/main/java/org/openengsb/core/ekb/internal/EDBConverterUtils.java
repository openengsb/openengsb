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

import java.util.UUID;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

/**
 * The EDBConverterUtils class provides some functionalities which are often needed for the transformations: 
 * EDBObjects <-> Models
 */
public final class EDBConverterUtils {

    private EDBConverterUtils() {
    }

    public static final String MODELVERSION = "edbVersion";
    public static final String MODELID = "edbId";

    /**
     * Loads the entry with the given key of a given model.
     */
    private static Object getOpenEngSBModelEntryValue(OpenEngSBModel model, String key) {
        for (OpenEngSBModelEntry entry : model.getOpenEngSBModelEntries()) {
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
        return (Integer) getOpenEngSBModelEntryValue(model, MODELVERSION);
    }

    /**
     * Loads the model id of a model if existing
     */
    private static String getOpenEngSBModelId(OpenEngSBModel model) {
        return (String) getOpenEngSBModelEntryValue(model, MODELID);
    }

    /**
     * Creates the OID for a model
     */
    public static String createOID(OpenEngSBModel model, String domainId, String connectorId) {
        StringBuilder builder = new StringBuilder();
        builder.append(createOIDPrefix(domainId, connectorId));
        String modelId = getOpenEngSBModelId(model);
        if (modelId != null) {
            builder.append(modelId);
        } else {
            builder.append(UUID.randomUUID().toString());
        }

        return builder.toString();
    }

    /**
     * Creates the OID prefix for a model
     */
    private static String createOIDPrefix(String domainId, String connectorId) {
        StringBuilder builder = new StringBuilder();
        builder.append(domainId).append("/").append(connectorId).append("/");
        return builder.toString();
    }

}
