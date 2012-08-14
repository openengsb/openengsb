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
public class EDBObject extends HashMap<String, EDBObjectEntry> {
    private static final String OID_CONST = "oid";
    private static final String TIMESTAMP_CONST = "timestamp";
    private static final String DELETED_CONST = "isDeleted";

    private Long timestamp;
    private String oid;

    /**
     * Create an EDBObject with a specified OID.
     */
    public EDBObject(String oid) {
        super();
        setOID(oid);
    }

    /**
     * Create an EDBObject using a Map of data. The OID is stored after loading the data Map, so any already existing
     * values with the special key representing the OID will be overwritten by the provided parameters.
     */
    public EDBObject(String oid, Map<String, EDBObjectEntry> data) {
        super(data);
        setOID(oid);
    }

    /**
     * Retrieve the timestamp for this object.
     */
    public final Long getTimestamp() {
        return timestamp;
    }

    /**
     * This function updates the timestamp for this object. This is necessary if you want to commit the object to the
     * database. Should be set by the EnterpriseDatabaseService in the commit procedure.
     */
    public void updateTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        putEDBObjectEntry(TIMESTAMP_CONST, timestamp, Long.class);
    }

    /**
     * Retrieve the OID for this object.
     */
    public String getOID() {
        if (oid != null) {
            return oid;
        } else {
            oid = (String) get(OID_CONST).getValue();
            return oid;
        }
    }

    /**
     * Sets the OID
     */
    public void setOID(String oid) {
        this.oid = oid;
        putEDBObjectEntry(OID_CONST, oid, String.class);
    }

    /**
     * Returns the value of the EDBObjectEntry for the given key, casted as String. Returns null if there is no element
     * for the given key, or the value for the given key is null.
     */
    public String getString(String key) {
        EDBObjectEntry entry = get(key);
        return entry == null ? null : (String) entry.getValue();
    }

    /**
     * Returns the value of the EDBObjectEntry for the given key, casted as Long. Returns null if there is no element
     * for the given key, or the value for the given key is null.
     */
    public Long getLong(String key) {
        EDBObjectEntry entry = get(key);
        return entry == null ? null : (Long) entry.getValue();
    }

    /**
     * Returns the value of the EDBObjectEntry for the given key. Returns null if there is no element for the given key,
     * or the value for the given key is null.
     */
    public Object getObject(String key) {
        EDBObjectEntry entry = get(key);
        return entry == null ? null : entry.getValue();
    }

    /**
     * Returns the value of the EDBObjectEntry for the given key, casted as the given class. Returns null if there is no
     * element for the given key, or the value for the given key is null.
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> clazz) {
        EDBObjectEntry entry = get(key);
        return entry == null ? null : (T) entry.getValue();
    }

    /**
     * Returns true if the object is deleted.
     */
    public final Boolean isDeleted() {
        EDBObjectEntry deleted = get(DELETED_CONST);
        if (deleted == null) {
            return false;
        }
        return (Boolean) deleted.getValue();
    }

    public void setDeleted(Boolean deleted) {
        put(DELETED_CONST, new EDBObjectEntry(DELETED_CONST, deleted, Boolean.class));
    }

    /**
     * Adds an EDBObjectEntry to this EDBObject
     */
    public void putEDBObjectEntry(String key, Object value, String type) {
        put(key, new EDBObjectEntry(key, value, type));
    }

    /**
     * Adds an EDBObjectEntry to this EDBObject
     */
    public void putEDBObjectEntry(String key, Object value, Class<?> type) {
        putEDBObjectEntry(key, value, type.getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        for (Map.Entry<String, EDBObjectEntry> entry : this.entrySet()) {
            appendEntry(entry, builder);
        }
        builder.append("}");

        return builder.toString();
    }

    /**
     * Analyzes the entry and write the specific information into the StringBuilder.
     */
    private void appendEntry(Map.Entry<String, EDBObjectEntry> entry, StringBuilder builder) {
        if (builder.length() > 2) {
            builder.append(",");
        }
        builder.append(" \"").append(entry.getKey()).append("\"");
        builder.append(" : ").append(entry.getValue());
    }
}
