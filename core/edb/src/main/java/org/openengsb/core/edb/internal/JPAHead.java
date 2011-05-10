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
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.openengsb.core.api.edb.EDBObject;

@Entity
public class JPAHead {

    @OneToMany(cascade = CascadeType.ALL)
    private List<JPAObject> head;
    private Long timestamp;
    private List<EDBObject> loaded;

    public JPAHead() {
    }

    public JPAHead(Long timestamp) {
        this.timestamp = timestamp;
        head = new ArrayList<JPAObject>();
    }

    public JPAHead(JPAHead cp, Long timestamp) {
        this.timestamp = timestamp;
        List<JPAObject> list = cp.getJPAObjects();

        if (list != null) {
            head = new ArrayList<JPAObject>(list);
        } else {
            head = new ArrayList<JPAObject>();
        }
    }

    public int count() {
        return head.size();
    }

    public List<EDBObject> get() {
        if (loaded == null) {
            loaded = new ArrayList<EDBObject>();
            for (JPAObject o : head) {
                loaded.add(o.getObject());
            }
        }
        return loaded;
    }

    public List<JPAObject> getJPAObjects() {
        return head;
    }

    public void delete(String uid) {
        loaded = null;
        for (int i = 0; i < head.size(); ++i) {
            JPAObject o = head.get(i);
            if (uid.equals(o.getUID())) {
                head.remove(i);
                return;
            }
        }
    }

    public void replace(String uid, JPAObject obj) {
        loaded = null;
        for (int i = 0; i < head.size(); ++i) {
            JPAObject o = head.get(i);
            if (uid.equals(o.getUID())) {
                head.remove(i);
                break;
            }
        }
        head.add(obj);
    }

    public void replace(String uid, EDBObject obj) {
        loaded = null;
        this.replace(uid, new JPAObject(obj));
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
