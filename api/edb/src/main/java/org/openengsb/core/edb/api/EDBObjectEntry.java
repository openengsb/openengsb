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

import com.google.common.base.Objects;

/**
 * The EDBObjectEntry class represents a key/value/type triple. An EDBObject has a number of these entries which
 * represent the actual information which is saved in the EDB.
 */
public class EDBObjectEntry {
    private String key;
    private Object value;
    private String type;

    public EDBObjectEntry() {
    }

    public EDBObjectEntry(String key, Object value, Class<?> type) {
        this(key, value, type.getName());
    }

    public EDBObjectEntry(String key, Object value, String type) {
		this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("key", key).add("value", value).add("type", type).toString();
    }
}
