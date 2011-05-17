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

package org.openengsb.core.api.edb;

import java.util.HashMap;
import java.util.Map;

/**
 * DB-Objects handle an object that is ready to be put into a DB and give access to the metadata.
 */
@SuppressWarnings("serial")
public class EDBObject extends HashMap<String, Object> {
    private Long timestamp;
    private String uid;

    private static final String UID_CONST = "@uid";
    private static final String TIMESTAMP_CONST = "@timestamp";
    private static final String DELETED_CONST = "@isDeleted";

    /**
     * Create an EDBObject with a specified UID and timestamp.
     */
    public EDBObject(String uid, Long timestamp) {
        super();
        this.timestamp = timestamp;
        this.uid = uid;

        put(UID_CONST, uid);
        put(TIMESTAMP_CONST, timestamp);
    }

    /**
     * Convenience constructor to create an EDBObject using a Map of data. The UID and timestamp are stored after
     * loading the data Map, so any already existing values with the special key representing the UID or the Timestamp
     * will be overwritten by the provided parameters.
     */
    public EDBObject(String uid, Long timestamp, Map<String, Object> data) {
        super(data);
        this.timestamp = timestamp;
        this.uid = uid;

        put(UID_CONST, uid);
        put(TIMESTAMP_CONST, timestamp);
    }

    /**
     * Usually used by a Database query function to create an EDBObject out of raw database-data. This will extract the
     * metadata from the raw data Map.
     */
    public EDBObject(Map<String, Object> rawData) {
        super(rawData);
        this.timestamp = (Long) rawData.get(TIMESTAMP_CONST);
        this.uid = (String) rawData.get(UID_CONST);
    }

    /**
     * Retrieve the timestamp for this object.
     */
    public final Long getTimestamp() {
        return timestamp;
    }

    /**
     * This function updates the timestamp for this object. It is useful if you want to add the object to a Commit which
     * already uses a different timestamp for some reason.
     */
    public void updateTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        put(TIMESTAMP_CONST, timestamp);
    }

    /**
     * Retrieve the UID for this object.
     */
    public final String getUID() {
        if (uid != null) {
            return uid;
        } else {
            uid = (String) get(UID_CONST);
            return uid;
        }
    }

    /** Change the UID */
    public void setUID(String uid) {
        this.uid = uid;
        put(UID_CONST, uid);
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

    /**
     * so far here was used a mongo object. Now there is a manual programming of the object toString method. It just
     * iterates through the key - value pairs and print them corresponding to their class. So far String and Long are
     * taken especially into consideration.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");

        for (Map.Entry<String, Object> entry : this.entrySet()) {
            if (builder.length() > 2) {
                builder.append(",");
            }
            String key = entry.getKey();
            Object value = entry.getValue();

            builder.append(" \"" + key + "\"");
            builder.append(" : ");

            if (value.getClass().equals(String.class)) {
                builder.append("\"" + (String) value + "\" ");
            } else if (value.getClass().equals(Long.class)) {
                builder.append((Long) value + " ");
            } else {
                builder.append(value.toString() + " ");
            }
        }

        builder.append("}");

        return builder.toString();
    }
}
