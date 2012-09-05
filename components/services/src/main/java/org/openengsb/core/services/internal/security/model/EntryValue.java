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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * represents the value of a property in a bean. Multiple values in the form of Arrays and Collections are supported. If
 * a property has a single value, it is saved as a singleton-list.
 */
@SuppressWarnings("serial")
@Table(name = "ENTRY_VALUE")
@Entity
public class EntryValue {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "KEY")
    private String key;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "ENTRY_VALUES")
    private List<EntryElement> value;

    public EntryValue() {
    }

    public EntryValue(String key, List<EntryElement> value) {
        this.key = key;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<EntryElement> getValue() {
        return value;
    }

    public void setValue(List<EntryElement> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + ": " + value.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntryValue)) {
            return false;
        }
        final EntryValue other = (EntryValue) obj;
        return Objects.equal(key, other.key) && Objects.equal(value, other.value);
    }

}
