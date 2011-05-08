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

package org.openengsb.core.edb;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

/**
 * DB-Objects handle an object that is ready to be put into a DB and give access to the metadata.
 */
@SuppressWarnings("serial")
@Entity
public class EDBObject extends HashMap<String, Object> {
    private long timestamp;
    private String uid;

    // necessary for jpa automatic enhancing
    protected EDBObject() {

    }

    /**
     * Create an EDBObject with a specified UID and timestamp.
     * 
     * @param uid The object's UID.
     * @param timestamp The timestamp, which must be equal to the Commit object it will be added to.
     */
    public EDBObject(String uid, long timestamp) {
        super();
        this.timestamp = timestamp;
        this.uid = uid;

        put("@uid", uid);
        put("@timestamp", new Long(timestamp));
    }

    /**
     * Convenience constructor to create an EDBObject using a Map of data. The UID and timestamp are stored after
     * loading the data Map, so any already existing values with the special key representing the UID or the Timestamp
     * will be overwritten by the provided parameters.
     * 
     * @param uid The object's UID.
     * @param timestamp The timestamp, which must be equal to the Commit object it will be added to.
     * @param data The data this object has to contain.
     */
    public EDBObject(String uid, long timestamp, Map<String, Object> data) {
        super(data);
        this.timestamp = timestamp;
        this.uid = uid;

        put("@uid", uid);
        put("@timestamp", new Long(timestamp));
    }

    /**
     * Usually used by a Database query function to create an EDBObject out of raw database-data. This will extract the
     * metadata from the raw data Map.
     * 
     * @param rawData The raw data, which has to contain all the required metadata as well!
     */
    public EDBObject(Map<String, Object> rawData) {
        super(rawData);
        this.timestamp = (Long) rawData.get("@timestamp");
        this.uid = (String) rawData.get("@uid");
    }

    /**
     * Retrieve the timestamp for this object.
     * 
     * @return The timestamp.
     */
    public final long getTimestamp() {
        return timestamp;
        // return (Long)get("@timestamp");
    }

    public final long getTimestampDEBUG() {
        return timestamp;
    }

    /**
     * This function updates the timestamp for this object. It is useful if you want to add the object to a Commit which
     * already uses a different timestamp for some reason.
     * 
     * @param timestamp The new timestamp.
     */
    public void updateTimestamp(long timestamp) {
        this.timestamp = timestamp;
        put("@timestamp", new Long(timestamp));
    }

    /**
     * Retrieve the UID for this object.
     * 
     * @return The object's UID.
     */
    public final String getUID() {
        return uid;
        // return (String)get("@uid");
    }

    /** Change the UID */
    public void setUID(String uid) {
        this.uid = uid;
        put("@uid", uid);
    }

    /**
     * Convenience function to retrieve a value as String.
     * 
     * @param key The key to query for.
     * @return The String representation of the value.
     */
    public final String getString(String key) {
        return (String) get(key);
    }

    /**
     * Convenience function to retrieve a value as long.
     * 
     * @param key The key to query for.
     * @return The value as Long.
     */
    public final long getLong(String key) {
        return (Long) get(key);
    }

    /**
     * Test if this object is a "deletion" entry in a history.
     * 
     * @return True if this object represents the deletion of this object, false otherwise.
     */
    public final boolean isDeleted() {
        Object id = get("@isDeleted");
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
