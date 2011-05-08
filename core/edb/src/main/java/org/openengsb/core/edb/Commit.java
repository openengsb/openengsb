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

package org.openengsb.core.edb;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.openengsb.core.edb.exceptions.AlreadyCommittedException;
import org.openengsb.core.edb.exceptions.EDBException;
import org.openengsb.core.edb.exceptions.InvalidTimestampException;

/**
 * A Commit object represents a change to the dataset. It can either reflect a change that already happened in the past,
 * in which it will contain a list of UIDs which have been changed by this commit. Or a new change that is going to be
 * committed to the database. The commit object needs a timestamp, representing the time at which the change happened.
 * Then the commit is filled with objects, or deletions. When everything is prepared, the commit() function can be
 * executed to send the change over to the database.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Commit {
    protected Database db;
    @Basic
    protected String committer;
    @Basic
    protected long timestamp;
    @Basic
    protected String role;

    protected List<EDBObject> objects; // For built commit objects

    // @Lob
    @ElementCollection
    protected List<String> deletions;
    // @Lob
    @ElementCollection
    protected List<String> uids; // For queried commit objects

    // / The object has been committed, you must not commit it twice.
    protected boolean committed = false;

    protected Commit() {
    }

    protected Commit(String committer, String role, long timestamp, Database db) {
        this.db = db;
        this.timestamp = timestamp;
        this.committer = committer;
        this.role = role;

        uids = null;
        objects = new ArrayList<EDBObject>();
        deletions = new ArrayList<String>();
    }

    protected final void setCommitted(boolean c) { // getCommit() sets this to avoid db.getCommit(x).commit()
        committed = c;
    }

    protected final boolean getCommitted() {
        return committed;
    }

    /**
     * For a query-commit: Retrieve a list of UIDs representing the objects which have been changed by this commit.
     * 
     * @return A list of UIDs.
     */
    public final List<String> getUIDs() {
        return uids;
    }

    /**
     * For a created commit: retrieve the list of all objects that have been add()-ed to this commit.
     * 
     * @return A list of EDBObjects.
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
     * 
     * @param obj An object suitable for committing in this commit.
     */
    public void add(EDBObject obj) throws InvalidTimestampException {
        if (obj.getTimestamp() != timestamp) {
            throw new InvalidTimestampException("Object's timestamp doesn't match commit's timestamp!");
        }
        if (!objects.contains(obj)) {
            objects.add(obj);
        }
    }

    /**
     * Delete an object that already exists.
     * 
     * @param uid The object's UID.
     */
    public void delete(String uid) throws EDBException {
        if (deletions.contains(uid)) {
            return;
        }
        deletions.add(uid);
    }

    /**
     * Commit the change to the database. This essentiall calls the database's commit() function.
     */
    public void commit() throws EDBException {
        if (committed) {
            throw new AlreadyCommittedException();
        }
        // Let's keep the implementation of transactions inside the Database
        db.commit(this);
    }

    /** Used by the database to "finalize" the commit */
    @Override
    public abstract void finalize() throws EDBException;

    /** Used by the database - mainly by JPA - to finalize the loading of the commit */
    public abstract void loadCommit() throws EDBException;

    /** For the JPA testing */
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
