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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.jdbc.Index;

@SuppressWarnings("serial")
@Entity
/**
 * this defines a jpa object in the database. The correlation to the EDBObject
 * is that the JPAObject can be converted to an EDBObject through the EDBUtils
 * class.
 */
public class JPAObject extends VersionedEntity {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "owner")
    private List<JPAEntry> entries;
    @Column(name = "TIME")
    private Long timestamp;
    @Column(name = "ISDELETED")
    private Boolean isDeleted;
    @Index
    @Column(name = "OID")
    private String oid;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "STAGE", nullable = true)
    private JPAStage stage;

    public JPAObject() {
        entries = new ArrayList<>();
        isDeleted = false;
    }

    /**
     * Adds an new entry to the JPAEntry list of this object.
     */
    public void addEntry(JPAEntry entry) {
        entries.add(entry);
    }

    /**
     * Returns the entry of the JPAEntry list of this object with the given key.
     * Returns null in case there is no such entry.
     */
    public JPAEntry getEntry(String entryKey) {
        for (JPAEntry entry : entries) {
            if (entry.getKey().equals(entryKey)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Removes the entry from the JPAEntry list of this object with the given
     * key.
     */
    public void removeEntry(String entryKey) {
        Iterator<JPAEntry> iter = entries.iterator();
        while (iter.hasNext()) {
            if (iter.next().getKey().equals(entryKey)) {
                iter.remove();
                return;
            }
        }
    }

    public List<JPAEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<JPAEntry> entries) {
        this.entries = entries;
    }

    public void setJPAStage(JPAStage stage) {
        this.stage = stage;
    }

    public JPAStage getJPAStage() {
        return this.stage;
    }

    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        this.isDeleted = deleted;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOID() {
        return oid;
    }

    public void setOID(String oid) {
        this.oid = oid;
    }
}
