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

package org.openengsb.core.edb.jpa.internal;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.openengsb.core.edb.api.EDBObjectEntry;

/**
 * A JPAEntry is assigned with JPAObjects. A JPAObject contains as many JPAEntries as it wants. So to say the JPAEntries
 * are concrete key/value pairs extending JPAObjects.
 */
@SuppressWarnings("serial")
@Entity
public class JPAEntry extends AbstractDataRow {
    @Column(name = "KEY")
    private String key;
    @Column(name = "VALUE")
    private String value;
    @Column(name = "TYPE")
    private String type;

    public JPAEntry() {
        key = "";
        value = null;
    }

    public JPAEntry(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }
    
    public JPAEntry(EDBObjectEntry entry) {
        this.key = entry.getKey();
        this.value = entry.getValue().toString();
        this.type = entry.getType();
    }

    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
