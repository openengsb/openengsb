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
package org.openengsb.core.services.internal.security.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Holds a single value of a property. The type must be a class with a String-only-constructor that can be used to
 * instantiate the original instance with a previous result of the type's toString-method.
 * <p/>
 * The value is saved as string.
 * <p/>
 * To support multiple values (Collections, Arrays) {@link EntryValue} is used as a wrapper
 */
@SuppressWarnings("serial")
@Table(name = "ENTRY_VALUE_ELEMENT")
@Entity
public class EntryElement {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "TYPE", nullable = false)
    private String type;
    @Column(name = "VALUE", nullable = false)
    private String value;

    public EntryElement(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public EntryElement() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", value, type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntryElement)) {
            return false;
        }
        final EntryElement other = (EntryElement) obj;
        return Objects.equal(type, other.type) && Objects.equal(value, other.value);
    }

}
