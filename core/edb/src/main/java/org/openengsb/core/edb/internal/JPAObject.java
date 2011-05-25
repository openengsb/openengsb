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

package org.openengsb.core.edb.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.openengsb.core.api.edb.EDBObject;

@Entity
/**
 * this defines a jpa object in the database. The correlation to the EDBObject is that
 * the JPAObject can be converted to an EDBObject.
 */
public class JPAObject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "jpaobject_id")
    private Long id;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<JPAEntry> values;
    
    private Long timestamp;
    private Boolean isDeleted;
    private String uid;

    public JPAObject() {
        isDeleted = false;
    }

    public JPAObject(EDBObject o) {
        timestamp = o.getTimestamp().longValue();
        uid = o.getUID();
        isDeleted = o.isDeleted();

        loadValues(o);
    }

    private void loadValues(EDBObject o) {
        values = new ArrayList<JPAEntry>();
        for (Map.Entry<String, Object> entry : o.entrySet()) {
            values.add(new JPAEntry(entry.getKey(), entry.getValue()));
        }
    }

    public EDBObject getObject() {
        Map<String, Object> data = new HashMap<String, Object>();
        for (JPAEntry kvp : values) {
            data.put(kvp.getKey(), kvp.getValue());
        }
        String s = (String) data.get("isDeleted");
        if (s != null) {
            data.put("isDeleted", s.equals("true"));
        }
        return new EDBObject(uid, timestamp, data);
    }

    public Boolean isDeleted() {
        return isDeleted;
    }

    /**
     * returns the internal Id the JPAObject has.
     */
    public Long getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getUID() {
        return uid;
    }
    
    public List<JPAEntry> getPairs() {
        return values;
    }

}
