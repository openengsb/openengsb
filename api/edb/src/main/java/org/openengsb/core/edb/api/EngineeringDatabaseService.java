/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The AASTI licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.openengsb.core.edb.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;

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

    EDBObject getObject(String oid, String sid) throws EDBException;

    /**
     * Retrieve the current state of the object with the specified OID for the given timestamp.
     */
    EDBObject getObject(String oid, Long timestamp) throws EDBException;

    /**
     * Retrieve the current state of the objects with the specified OIDs.
     */
    List<EDBObject> getObjects(List<String> oids) throws EDBException;

    List<EDBObject> getObjects(List<String> oids, String sid) throws EDBException;

    /**
     * Retrieve the current state - a list of all EDBObjects currently available.
     */
    List<EDBObject> getHead() throws EDBException;

    List<EDBObject> getHead(String sid) throws EDBException;

    /**
     * Retrieve the history of an object with a specified OID.
     */
    List<EDBObject> getHistory(String oid) throws EDBException;

    List<EDBObject> getHistory(String oid, String sid) throws EDBException;

    /**
     * Retrieve the history of an object with a specified OID between a specified range of timestamps (inclusive).
     */
    List<EDBObject> getHistoryForTimeRange(String oid, Long from, Long to) throws EDBException;

    List<EDBObject> getHistoryForTimeRange(String oid, Long from, Long to, String sid) throws EDBException;

    /**
     * Get the Log for an object between two timestamps (inclusive).
     */
    List<EDBLogEntry> getLog(String oid, Long from, Long to) throws EDBException;

    List<EDBLogEntry> getLog(String oid, Long from, Long to, String sid) throws EDBException;

    /**
     * Retrieve the full state for a provided timestamp. Note, there need not exist a commit for this exact timestamp.
     * It will be equivalent retrieving the head from the latest commit before or at the exact time provided.
     */
    List<EDBObject> getHead(long timestamp) throws EDBException;

    List<EDBObject> getHead(long timestamp, String sid) throws EDBException;

    /**
     * Queries for EDBObject based on the given query request object
     */
    List<EDBObject> query(QueryRequest request) throws EDBException;

    List<EDBObject> query(QueryRequest request, String sid) throws EDBException;

    /**
     * Convenience function to query for a commit with a single matching key-value pair.
     */
    List<EDBCommit> getCommitsByKeyValue(String key, Object value) throws EDBException;

    List<EDBCommit> getCommitsByKeyValue(String key, Object value, String sid) throws EDBException;

    /**
     * More general query for a commit, with AND-connected key-value pairs to match.
     */
    List<EDBCommit> getCommits(Map<String, Object> query) throws EDBException;

    List<EDBCommit> getCommits(Map<String, Object> query, String sid) throws EDBException;

    /**
     * Returns a list of commit meta information of all commits which are matching the given request.
     */
    List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request) throws EDBException;

    List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request, String sid) throws EDBException;

    /**
     * Convenience function to get a commit for a timestamp. In this case, if the timestamp doesn't exist, null is
     * returned. Exceptions are only thrown for database errors.
     */
    EDBCommit getCommit(Long from) throws EDBException;

    EDBCommit getCommit(Long from, String sid) throws EDBException;

    /**
     * Convenience function to get a commit for a given revision string. If there is no commit for the given revision
     * string or if a database error occurs, an EDBException is thrown.
     */
    EDBCommit getCommitByRevision(String revision) throws EDBException;

    EDBCommit getCommitByRevision(String revision, String sid) throws EDBException;

    /**
     * Convenience function to query for a commit with a single matching key-value pair.
     */
    EDBCommit getLastCommitByKeyValue(String key, Object value) throws EDBException;

    EDBCommit getLastCommitByKeyValue(String key, Object value, String sid) throws EDBException;

    /**
     * More general query for the last commit, with AND-connected key-value pairs to match.
     */
    EDBCommit getLastCommit(Map<String, Object> query) throws EDBException;

    EDBCommit getLastCommit(Map<String, Object> queryMap, String sid) throws EDBException;

    /**
     * Compare two states and show the differences.
     */
    EDBDiff getDiff(Long firstTimestamp, Long secondTimestamp) throws EDBException;

    /*
     * Compare two states and show the differences. Further it is possible to make cross stage comparissons. 
     * If you enter two times the same stage ID it will compare on the same stage.
     */
    EDBDiff getDiff(Long firstTimestamp, Long secondTimestamp, String sid1, String sid2) throws EDBException;

    /**
     * Find all OIDs which have been "resurrected" (deleted and recreated)
     */
    List<String> getResurrectedOIDs() throws EDBException;

    List<String> getResurrectedOIDs(String sid) throws EDBException;

    /**
     * Fixed-Complex-Query - Get all objects at the state of last commit which matches the provided query.
     */
    List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> query) throws EDBException;

    List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> queryMap, String sid) throws EDBException;

    /**
     * Convenience function, see getStateofLastCommitMatching(Map<String, Object> query)
     */
    List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String key, Object value) throws EDBException;

    List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String key, Object value, String sid) throws EDBException;

    /**
     * Creates an EDBCommit object out of the given EDBObject lists
     */
    EDBCommit createEDBCommit(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes)
        throws EDBException;

    EDBCommit createEDBCommit(EDBStage stage, List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes)
        throws EDBException;

    /**
     * Returns the revision of the current state of the EDB.
     */
    UUID getCurrentRevisionNumber() throws EDBException;

    UUID getCurrentRevisionNumber(EDBStage stage) throws EDBException;

    /**
     * Returns the revision of the last commit performed in the EDB under the given contextId.
     */
    UUID getLastRevisionNumberOfContext(String contextId) throws EDBException;

    UUID getLastRevisionNumberOfContext(String contextId, String sid) throws EDBException;
}
