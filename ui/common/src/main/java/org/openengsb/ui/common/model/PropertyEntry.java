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

package org.openengsb.ui.common.model;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
public class PropertyEntry implements Comparable<PropertyEntry>, Serializable {

    private String key;
    private Object value;

    public PropertyEntry(Map.Entry<String, Object> entry) {
        key = entry.getKey();
        value = entry.getValue();
    }

    public PropertyEntry() {
    }

    public PropertyEntry(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int compareTo(PropertyEntry o) {
        return key.compareTo(o.getKey());
    }

}
