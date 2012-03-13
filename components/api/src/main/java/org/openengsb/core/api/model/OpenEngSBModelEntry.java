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

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * Simple Model Entry class. Every model entry has three fields: key, value and type. Key defines the id, value is the
 * value for the key and type defines the type of the value.
 */
@XmlRootElement
public class OpenEngSBModelEntry {
    private String key;
    private Object value;
    private Class<?> type;

    public OpenEngSBModelEntry() {
    }

    public OpenEngSBModelEntry(String key, Object value, Class<?> type) {
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

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, type, value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OpenEngSBModelEntry)) {
            return false;
        }
        OpenEngSBModelEntry other = (OpenEngSBModelEntry) o;
        return Objects.equal(key, other.key) && Objects.equal(type, other.type)
                && Objects.equal(value, other.value);
    }
}
