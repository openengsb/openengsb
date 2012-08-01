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

package org.openengsb.core.edb.api;

import java.util.HashMap;
import java.util.Map;

/**
 * DB-Objects handle an object that is ready to be put into a DB and give access to the metadata.
 */
@SuppressWarnings("serial")
public class EDBObject extends HashMap<String, Object> {
    private Long timestamp;
    private String oid;

    private static final String OID_CONST = "oid";
    private static final String TIMESTAMP_CONST = "timestamp";
    private static final String DELETED_CONST = "isDeleted";

    /**
     * Create an EDBObject with a specified OID.
     */
    public EDBObject(String oid) {
        super();
        this.oid = oid;

        put(OID_CONST, oid);
    }

    /**
     * Convenience constructor to create an EDBObject using a Map of data. The OID is stored after loading the data Map,
     * so any already existing values with the special key representing the OID will be overwritten by the provided
     * parameters.
     */
    public EDBObject(String oid, Map<String, Object> data) {
        super(data);
        this.oid = oid;

        put(OID_CONST, oid);
    }

    /**
     * Usually used by a Database query function to create an EDBObject out of raw database-data. This will extract the
     * metadata from the raw data Map.
     */
    public EDBObject(Map<String, Object> rawData) {
        super(rawData);
        this.timestamp = (Long) rawData.get(TIMESTAMP_CONST);
        this.oid = (String) rawData.get(OID_CONST);
    }

    /**
     * Retrieve the timestamp for this object.
     */
    public final Long getTimestamp() {
        return timestamp;
    }

    /**
     * This function updates the timestamp for this object. This is necessary if you want to commit the object to the
     * database! Should be set by the EnterpriseDatabaseService in the commit procedure.
     */
    public void updateTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        put(TIMESTAMP_CONST, timestamp);
    }

    /**
     * Retrieve the OID for this object.
     */
    public final String getOID() {
        if (oid != null) {
            return oid;
        } else {
            oid = (String) get(OID_CONST);
            return oid;
        }
    }

    /** Change the OID */
    public void setOID(String oid) {
        this.oid = oid;
        put(OID_CONST, oid);
    }

    /**
     * Convenience function to retrieve a value as String.
     */
    public final String getString(String key) {
        return (String) get(key);
    }

    /**
     * Convenience function to retrieve a value as long.
     */
    public final long getLong(String key) {
        return (Long) get(key);
    }

    /**
     * Test if this object is a "deletion" entry in a history.
     */
    public final boolean isDeleted() {
        Object id = get(DELETED_CONST);
        if (id == null) {
            return false;
        }
        return (Boolean) id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : this.entrySet()) {
            appendEntry(entry, builder);
        }
        builder.append("}");

        return builder.toString();
    }

    /**
     * analyzes the entry and write the specific information into the StringBuilder.
     */
    private void appendEntry(Map.Entry<String, Object> entry, StringBuilder builder) {
        if (builder.length() > 2) {
            builder.append(",");
        }
        String key = entry.getKey();
        Object value = entry.getValue();

        builder.append(" \"").append(key).append("\"");
        builder.append(" : ");

        if (value.getClass().equals(String.class)) {
            builder.append("\"").append((String) value).append("\" ");
        } else if (value.getClass().equals(Long.class)) {
            builder.append((Long) value).append(" ");
        } else {
            builder.append(value.toString()).append(" ");
        }
    }
}
