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

    protected JPADatabase db;
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

    public JPACommit(String committer, String role, Long timestamp, JPADatabase db) {
        this.db = db;
        this.timestamp = timestamp;
        this.committer = committer;
        this.role = role;

        uids = null;
        objects = new ArrayList<EDBObject>();
        deletions = new ArrayList<String>();
    }

    public void setCommitted(boolean c) {
        committed = c;
    }

    public boolean getCommitted() {
        return committed;
    }

    /**
     * For a query-commit: Retrieve a list of UIDs representing the objects which have been changed by this commit.
     */
    public List<String> getUIDs() {
        return uids;
    }

    /**
     * For a created commit: retrieve the list of all objects that have been add()-ed to this commit.
     */
    public final List<EDBObject> getObjects() {
        return objects;
    }

    /**
     * For both, a created, or a queried commit: Retrieve a list of deleted UIDs.
     */
    public final List<String> getDeletions() {
        return deletions;
    }

    /** Get the committer's name. */
    public final String getCommitter() {
        return committer;
    }

    /** Get the commit's timestamp. */
    public final long getTimestamp() {
        return timestamp;
    }

    /** Get the commit's role. */
    public final String getRole() {
        return role;
    }

    /**
     * Add an object to be committed (updated or created). The object's timestamp must match the commit's timestamp.
     */
    public void add(EDBObject obj) throws EDBException {
        if (obj.getTimestamp() != timestamp) {
            throw new EDBException("Object's timestamp doesn't match commit's timestamp!");
        }
        if (!objects.contains(obj)) {
            objects.add(obj);
        }
    }

    /**
     * Delete an object that already exists.
     */
    public void delete(String uid) throws EDBException {
        if (deletions.contains(uid)) {
            return;
        }
        deletions.add(uid);
    }

    /**
     * Commit the change to the database. This essentially calls the database's commit() function.
     */
    public void commit() throws EDBException {
        if (committed) {
            throw new EDBException("this commit class is already committed");
        }
        // Let's keep the implementation of transactions inside the Database
        db.commit(this);
    }

    @Override
    public void finalize() throws EDBException {
        if (getCommitted()) {
            throw new EDBException("Commit already finalized, probably already committed.");
        }
        fillUIDs();
        jpaObjects = new ArrayList<JPAObject>();
        for (EDBObject o : getObjects()) {
            jpaObjects.add(new JPAObject(o));
        }
    }

    @Override
    public void loadCommit() throws EDBException {
        for (JPAObject o : jpaObjects) {
            getObjects().add(o.getObject());
        }
        jpaObjects.clear();
    }

    public void fillUIDs() {
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
