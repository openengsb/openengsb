package org.openengsb.core.api.edb;

import java.util.List;
import java.util.Map;

/**
 * Defines the connection to the enterprise database.
 */
public interface EnterpriseDatabaseService {
    /**
     * Create a commit which is ready to be filled with updates.
     */
    EDBCommit createCommit(String committer, String role);

    /**
     * Commit the provided commit object and returns the corresponding time stamp for the commit.
     */
    Long commit(EDBCommit obj) throws EDBException;

    /**
     * Retrieve the current state of the object with the specified OID.
     */
    EDBObject getObject(String oid) throws EDBException;

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
    List<EDBObject> getHistory(String oid, Long from, Long to) throws EDBException;

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
    List<EDBObject> query(String key, Object value) throws EDBException;

    /**
     * More general query for an object in the current state with the provided key-value pairs.
     */
    List<EDBObject> query(Map<String, Object> query) throws EDBException;

    /** 
     * Convenience function to query for a commit with a single matching key-value pair. 
     */
    List<EDBCommit> getCommits(String key, Object value) throws EDBException;

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
    EDBCommit getLastCommit(String key, Object value) throws EDBException;

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
    List<EDBObject> getStateOfLastCommitMatching(String key, Object value) throws EDBException;
}
