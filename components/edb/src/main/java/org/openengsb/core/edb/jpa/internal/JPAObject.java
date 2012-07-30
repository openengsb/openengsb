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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.openengsb.core.common.AbstractDataRow;
import org.openengsb.core.edb.api.EDBObject;

@SuppressWarnings("serial")
@Entity
/**
 * this defines a jpa object in the database. The correlation to the EDBObject is that
 * the JPAObject can be converted to an EDBObject.
 */
public class JPAObject extends AbstractDataRow {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JPAEntry> entries;
    @Column(name = "TIME")
    private Long timestamp;
    @Column(name = "ISDELETED")
    private Boolean isDeleted;
    @Column(name = "OID")
    private String oid;

    public JPAObject() {
        isDeleted = false;
    }

    public JPAObject(EDBObject o) {
        timestamp = o.getTimestamp().longValue();
        oid = o.getOID();
        isDeleted = o.isDeleted();

        loadValues(o);
    }

    private void loadValues(EDBObject o) {
        entries = new ArrayList<JPAEntry>();
        for (Map.Entry<String, Object> entry : o.entrySet()) {
            entries.add(new JPAEntry(entry.getKey(), entry.getValue()));
        }
    }

    public EDBObject getObject() {
        Map<String, Object> data = new HashMap<String, Object>();
        for (JPAEntry kvp : entries) {
            data.put(kvp.getKey(), kvp.getValue());
        }
        String s = (String) data.get("isDeleted");
        if (s != null) {
            data.put("isDeleted", s.equals("true"));
        }
        EDBObject object = new EDBObject(oid, data);
        object.updateTimestamp(timestamp);
        return object;
    }

    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getOID() {
        return oid;
    }

    public List<JPAEntry> getPairs() {
        return entries;
    }
}
