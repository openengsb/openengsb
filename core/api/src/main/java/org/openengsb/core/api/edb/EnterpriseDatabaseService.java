package org.openengsb.core.api.edb;

import java.util.List;
import java.util.Map;

public interface EnterpriseDatabaseService {
    /** Open the database with the name chosen by the previous setDatabase() call. */
    void open() throws EDBException;

    /** Close the database and synchronize all changes to the HD. */
    void close();

    /** Choose the database which is to be opened with the next open() call. */
    void setDatabase(String databaseName, EDBDatabaseType databaseType);

    /*
     * Authenticate with user and password.
     * 
     * @return true if the authentication was successful, false otherwise.
     */
    // public boolean authenticate(String user, String pass) throws EDBException;

    /**
     * Create a commit which is ready to be filled with updates. Note that the provided timestamp must be valid. If
     * there is a commit with a later timestamp in the database, then the commit() call will fail!
     * 
     * @param committer The name of the committer.
     * @param role The role for this commit.
     * @param timestamp The timestamp for this commit. If the timestamp is invalid (see above), the final commit() call
     *        will fail!
     * @return A commit object, ready to be filled with updates and committed.
     */
    EDBCommit createCommit(String committer, String role, long timestamp);

    /**
     * Commit the provided commit object. When you call .commit() on a commit object, this is what happens. Note that it
     * will throw an exception in case the timestamp is invalid, @see createCommit()
     * 
     * @param obj The commit object to commit.
     */
    void commit(EDBCommit obj) throws EDBException;

    /**
     * Retrieve the current state of the object with the specified UID.
     * 
     * @param uid The desired object's UID
     * @return The matching EDBObject, or null if it doesn't exist or was deleted in a previous commit.
     */
    EDBObject getObject(String uid) throws EDBException;

    /**
     * Retrieve the current state - a list of all EDBObjects currently available.
     * 
     * @return A List of EDBObjects.
     */
    List<EDBObject> getHead() throws EDBException;

    /**
     * Retrieve the history of an object with a specified UID.
     * 
     * @param uid The desired object's UID.
     * @return A list of the Object with the provided UID, ordered by their timestamps.
     */
    List<EDBObject> getHistory(String uid) throws EDBException;

    /**
     * Retrieve the history of an object with a specified UID between a specified range of timestamps (inclusive).
     * 
     * @param uid The desired object's UID.
     * @param from The earliest timestamp.
     * @param to The latest timestamp.
     * @return A list of the Object with the provided UID, ordered by their timestamps.
     */
    List<EDBObject> getHistory(String uid, long from, long to) throws EDBException;

    /**
     * Get the Log for an object between two timestamps (inclusive).
     * 
     * @param uid The desired object's UID.
     * @param from The earliest timestamp.
     * @param to The latest timestamp.
     * @return A list of LogEntries for the provided UID, ordered by their timestamps.
     */
    List<EDBLogEntry> getLog(String uid, long from, long to) throws EDBException;

    /**
     * Retrieve the full state for a provided timestamp. Note, there need not exist a commit for this exact timestamp.
     * It will be equivalent retrieving the head from the latest commit before or at the exact time provided.
     * 
     * @param timestamp The timestamp for the desired state.
     * @return The full list of objects existing at the provided time.
     */
    List<EDBObject> getHead(long timestamp) throws EDBException;

    /** Convenience function to query for a single key-value pair in the current state. */
    List<EDBObject> query(String key, Object value) throws EDBException;

    /**
     * More general query for an object in the current state with the provided key-value pairs.
     * 
     * @param query A map of AND-connected key-value pairs to match.
     * @return A list of EDBObjects matching the query.
     */
    List<EDBObject> query(Map<String, Object> query) throws EDBException;

    /** Convenience function to query for a commit with a single matching key-value pair. */
    List<EDBCommit> getCommits(String key, Object value) throws EDBException;

    /**
     * More general query for a commit, with AND-connected key-value pairs to match.
     * 
     * @param query A map of AND-connected key-value pairs to match.
     * @return A list of Commit objects matching the query.
     */
    List<EDBCommit> getCommits(Map<String, Object> query) throws EDBException;

    /**
     * Convenience function to get a commit for a timestamp. In this case, if the timestamp doesn't exist, null is
     * returned. Exceptions are only thrown for database errors.
     * 
     * @param from The commit's timestamp
     * @return The commit, or null if it doesn't exist.
     */
    EDBCommit getCommit(long from) throws EDBException;

    /** Convenience function to query for a commit with a single matching key-value pair. */
    EDBCommit getLastCommit(String key, Object value) throws EDBException;

    /**
     * More general query for the last commit, with AND-connected key-value pairs to match.
     * 
     * @param query A map of AND-connected key-value pairs to match.
     * @return The latest Commit matching the query.
     */
    EDBCommit getLastCommit(Map<String, Object> query) throws EDBException;

    /**
     * Compare two states and show the differences.
     * 
     * @param min Timestamp representing the earliest state to compare.
     * @param max Timestamp representing the latest state to compare.
     * @return A Diff object containing the comparison between the two states.
     */
    EDBDiff getDiff(long min, long max) throws EDBException;

    /**
     * Find all UIDs which have been "resurrected" (deleted and recreated)
     * 
     * @return A list of resurrected UID Strings.
     */
    List<String> getResurrectedUIDs() throws EDBException;

    /**
     * Fixed-Complex-Query - Get all objects at the state of last commit which matches the provided query.
     * 
     * @param query The query to use to look for a matching commit.
     * @return A list of objects in the commit's state, or null if no commit was found.
     */
    List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> query) throws EDBException;

    /**
     * Convenience function, @see getStateofLastCommitMatching(Map<String, Object> query)
     * 
     * @param key A key to look for in the commit.
     * @param value The value for 'key' to check.
     * @return A list of objects in the commit's state, or null if no commit was found.
     */
    List<EDBObject> getStateOfLastCommitMatching(String key, Object value) throws EDBException;

    /**
     * Get a Transaction object
     * for jpa no implementation for this so far.
     */
//    UserTransaction getUserTransaction() throws EDBException;
}
