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

package org.openengsb.core.edb.internal;

import java.util.UUID;

import org.openengsb.core.api.edb.EDBConstants;
import org.openengsb.core.api.edb.EDBEvent;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

public final class ModelConverterUtils {
    
    private ModelConverterUtils() {
    }
    
    private static Object getOpenEngSBModelEntryValue(OpenEngSBModel model, String key) {
        for (OpenEngSBModelEntry entry : model.getOpenEngSBModelEntries()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public static Integer getModelVersion(OpenEngSBModel model) {
        return (Integer) getOpenEngSBModelEntryValue(model, EDBConstants.MODEL_VERSION);
    }
    
    private static String getOpenEngSBModelId(OpenEngSBModel model) {
        return (String) getOpenEngSBModelEntryValue(model, EDBConstants.MODEL_OID);
    }
    
    public static String createOID(OpenEngSBModel model, EDBEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append(createOIDPrefix(event));
        String modelId = getOpenEngSBModelId(model);
        if (modelId != null) {
            builder.append(modelId);
        } else {
            builder.append(UUID.randomUUID().toString());
        }

        return builder.toString();
    }
    
    private static String createOIDPrefix(EDBEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append(event.getDomainId()).append("/").append(event.getConnectorId()).append("/");
        return builder.toString();
    }

}
