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

package org.openengsb.core.api.edb;

import java.util.List;


/**
 * A Commit object represents a change to the dataset. It can either reflect a change that already happened in the past,
 * in which it will contain a list of UIDs which have been changed by this commit. Or a new change that is going to be
 * committed to the database. The commit object needs a timestamp, representing the time at which the change happened.
 * Then the commit is filled with objects, or deletions. When everything is prepared, the commit() function can be
 * executed to send the change over to the database.
 */
public interface EDBCommit {

    void setCommitted(boolean c);

    boolean getCommitted();

    /**
     * For a query-commit: Retrieve a list of UIDs representing the objects which have been changed by this commit.
     */
    List<String> getUIDs();

    /**
     * For a created commit: retrieve the list of all objects that have been add()-ed to this commit.
    */
    List<EDBObject> getObjects();

    /**
     * For both, a created, or a queried commit: Retrieve a list of deleted UIDs.
     */
    List<String> getDeletions();

    /** Get the committer's name. */
    String getCommitter();

    /** Get the commit's timestamp. */
    Long getTimestamp();

    /** Get the commit's role. */
    String getRole();

    /**
     * Add an object to be committed (updated or created). The object's timestamp must match the commit's timestamp.
     */
    void add(EDBObject obj) throws EDBException;

    /**
     * Delete an object that already exists.
     */
    void delete(String uid) throws EDBException;

    /** Used by the database to "finalize" the commit */
    void finalize() throws EDBException;
}
