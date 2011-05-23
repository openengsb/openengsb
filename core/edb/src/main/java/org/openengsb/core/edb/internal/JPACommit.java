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

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBObject;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JPACommit implements EDBCommit {
    private List<JPAObject> jpaObjects;
    @Basic
    protected String committer;
    @Basic
    protected Long timestamp;
    @Basic
    protected String role;

    protected List<EDBObject> objects; // For built commit objects

    @ElementCollection
    protected List<String> deletions;

    @ElementCollection
    protected List<String> uids; // For queried commit objects

    // / The object has been committed, you must not commit it twice.
    protected boolean committed = false;

    public JPACommit() {
    }

    public JPACommit(String committer, String role, Long timestamp) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.role = role;

        uids = new ArrayList<String>();
        objects = new ArrayList<EDBObject>();
        deletions = new ArrayList<String>();
    }

    @Override
    public void setCommitted(boolean c) {
        committed = c;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public List<String> getUIDs() {
        return uids;
    }

    @Override
    public final List<EDBObject> getObjects() {
        return objects;
    }

    @Override
    public final List<String> getDeletions() {
        return deletions;
    }

    @Override
    public final String getCommitter() {
        return committer;
    }

    @Override
    public final Long getTimestamp() {
        return timestamp;
    }

    @Override
    public final String getRole() {
        return role;
    }

    @Override
    public void add(EDBObject obj) throws EDBException {
        if (obj.getTimestamp().longValue() != timestamp) {
            throw new EDBException("Object's timestamp doesn't match commit's timestamp!");
        }
        if (!objects.contains(obj)) {
            objects.add(obj);
        }
    }

    @Override
    public void delete(String uid) throws EDBException {
        if (deletions.contains(uid)) {
            return;
        }
        deletions.add(uid);
    }

    @Override
    public void finalize() throws EDBException {
        if (isCommitted()) {
            throw new EDBException("Commit already finalized, probably already committed.");
        }
        fillUIDs();
        jpaObjects = new ArrayList<JPAObject>();
        for (EDBObject o : getObjects()) {
            jpaObjects.add(new JPAObject(o));
        }
    }

    private void fillUIDs() {
        if (uids == null) {
            uids = new ArrayList<String>();
        } else {
            uids.clear();
        }
        for (EDBObject o : objects) {
            uids.add(o.getUID());
        }
    }
}
