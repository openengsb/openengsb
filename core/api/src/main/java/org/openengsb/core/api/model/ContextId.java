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

package org.openengsb.core.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a unique identification of context instances.
 * 
 * A context instance is identified by a String-identifier
 * 
 */
public class ContextId {
    
    public static final String META_KEY_ID = "id";

    private String id;

    public ContextId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * returns a map-representation of the ID for use with
     * {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
     */
    public Map<String, String> toMetaData() {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put(META_KEY_ID, id);
        return metaData;
    }

    /**
     * parses a ContextId object from a Map-representation used in
     * {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
     */
    public static ContextId fromMetaData(Map<String, String> metaData) {
        return new ContextId(metaData.get(META_KEY_ID));
    }

    @Override
    public String toString() {
        return "Context: " + getId();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() == obj.getClass()) {
            ContextId other = (ContextId) obj;
            return equals(other);
        }
        if (Map.class.isInstance(obj)) {
            Map<?, ?> metadata = (Map<?, ?>) obj;
            return toMetaData().equals(metadata);
        }
        if (obj instanceof String) {
            return toString().equals(obj);
        }
        return false;
    }

    private boolean equals(ContextId other) {
        if (this.id == null && other.id != null) {
            return false;
        }
        if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public static Map<String, String> getContextWildCard() {
        return Collections.emptyMap();
    }

}
