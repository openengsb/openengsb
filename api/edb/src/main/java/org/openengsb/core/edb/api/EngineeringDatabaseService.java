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

package org.openengsb.core.edb.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Defines the connection to the engineering database.
 */
public interface EngineeringDatabaseService {

    /**
     * Commit the provided commit object and returns the corresponding time stamp for the commit.
     */
    Long commit(EDBCommit obj) throws EDBException;

    /**
     * Retrieve the current state of the object with the specified OID.
     */
    EDBObject getObject(String oid) throws EDBException;

    /**
     * Retrieve the current state of the objects with the specified OIDs.
     */
    List<EDBObject> getObjects(List<String> oids) throws EDBException;

    /**
     * Retrieve the current state - a list of all EDBObjects currently available.
     */
    List<EDBObject> getHead() throws EDBException;

    /**
     * Retrieve the history of an object with a specified OID.
     */
    List<EDBObject> getHistory(String oid) throws EDBException;

    /**
     * Retrieve the history of an object with a specified OID between a specified range of timestamps (inclusive).
     */
    List<EDBObject> getHistoryForTimeRange(String oid, Long from, Long to) throws EDBException;

    /**
     * Get the Log for an object between two timestamps (inclusive).
     */
    List<EDBLogEntry> getLog(String oid, Long from, Long to) throws EDBException;

    /**
     * Retrieve the full state for a provided timestamp. Note, there need not exist a commit for this exact timestamp.
     * It will be equivalent retrieving the head from the latest commit before or at the exact time provided.
     */
    List<EDBObject> getHead(long timestamp) throws EDBException;

    /**
     * Convenience function to query for a single key-value pair in the current state.
     */
    List<EDBObject> queryByKeyValue(String key, Object value) throws EDBException;

    /**
     * More general query for an object in the current state with the provided key-value pairs.
     */
    List<EDBObject> queryByMap(Map<String, Object> query) throws EDBException;

    /**
     * Returns a list of JPAObjects which have all JPAEntries with the given keys and values at a specific timestamp
     * (similar to getHead)
     * */
    List<EDBObject> query(Map<String, Object> query, Long timestamp) throws EDBException;

    /**
     * Convenience function to query for a commit with a single matching key-value pair.
     */
    List<EDBCommit> getCommitsByKeyValue(String key, Object value) throws EDBException;

    /**
     * More general query for a commit, with AND-connected key-value pairs to match.
     */
    List<EDBCommit> getCommits(Map<String, Object> query) throws EDBException;

    /**
     * Convenience function to get a commit for a timestamp. In this case, if the timestamp doesn't exist, null is
     * returned. Exceptions are only thrown for database errors.
     */
    EDBCommit getCommit(Long from) throws EDBException;

    /**
     * Convenience function to query for a commit with a single matching key-value pair.
     */
    EDBCommit getLastCommitByKeyValue(String key, Object value) throws EDBException;

    /**
     * More general query for the last commit, with AND-connected key-value pairs to match.
     */
    EDBCommit getLastCommit(Map<String, Object> query) throws EDBException;

    /**
     * Compare two states and show the differences.
     */
    EDBDiff getDiff(Long firstTimestamp, Long secondTimestamp) throws EDBException;

    /**
     * Find all OIDs which have been "resurrected" (deleted and recreated)
     */
    List<String> getResurrectedOIDs() throws EDBException;

    /**
     * Fixed-Complex-Query - Get all objects at the state of last commit which matches the provided query.
     */
    List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> query) throws EDBException;

    /**
     * Convenience function, see getStateofLastCommitMatching(Map<String, Object> query)
     */
    List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String key, Object value) throws EDBException;
    
    /**
     * Creates an EDBCommit object out of the given EDBObject lists 
     */
    EDBCommit createEDBCommit(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes)
        throws EDBException;
    
    /**
     * Returns the revision of the current state of the EDB.
     */
    UUID getCurrentRevisionNumber() throws EDBException;
}
