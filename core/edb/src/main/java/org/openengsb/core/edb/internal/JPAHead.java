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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.openengsb.core.api.edb.EDBObject;

/**
 * A JPA Head contains all JPAObjects which are bound to a specific timestamp.
 */
@Entity
public class JPAHead {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JPAObject> objects;
    @Version
    private Integer versionNumber;
    @Column(name = "TIME")
    private Long timestamp;
    
    private List<EDBObject> loaded;

    /**
     * the empty constructor is only for the jpa enhancer. Do not use it in real code.
     */
    @Deprecated
    public JPAHead() {
    }

    public JPAHead(Long timestamp) {
        this.timestamp = timestamp;
        objects = new ArrayList<JPAObject>();
    }

    public JPAHead(JPAHead cp, Long timestamp) {
        this.timestamp = timestamp;
        List<JPAObject> list = cp.getJPAObjects();

        if (list != null) {
            objects = new ArrayList<JPAObject>(list);
        } else {
            objects = new ArrayList<JPAObject>();
        }
    }

    public int count() {
        return objects.size();
    }

    public List<EDBObject> getEDBObjects() {
        if (loaded == null) {
            loaded = new ArrayList<EDBObject>();
            for (JPAObject o : objects) {
                loaded.add(o.getObject());
            }
        }
        return loaded;
    }

    public List<JPAObject> getJPAObjects() {
        return objects;
    }

    public void delete(String oid) {
        loaded = null;
        for (int i = 0; i < objects.size(); ++i) {
            JPAObject o = objects.get(i);
            if (oid.equals(o.getOID())) {
                objects.remove(i);
                return;
            }
        }
    }

    public void replace(String oid, JPAObject obj) {
        loaded = null;
        for (int i = 0; i < objects.size(); ++i) {
            JPAObject o = objects.get(i);
            if (oid.equals(o.getOID())) {
                objects.remove(i);
                break;
            }
        }
        objects.add(obj);
    }

    public void replace(String oid, EDBObject obj) {
        loaded = null;
        this.replace(oid, new JPAObject(obj));
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public int getVersionNumber() {
        return versionNumber;
    }
}
