package org.openengsb.core.edb.internal.dao;

import java.util.List;
import java.util.Map;

import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.edb.internal.JPACommit;
import org.openengsb.core.edb.internal.JPAHead;
import org.openengsb.core.edb.internal.JPAObject;

/**
 * Represents a dao interface for connection to the JPA Database
 */
public interface JPADao {

    /**
     * Returns the most actual JPAHead Number.
     */
    Number getNewestJPAHeadNumber() throws EDBException;

    /**
     * Loads the JPAHead with the given timestamp.
     */
    JPAHead getJPAHead(long timestamp) throws EDBException;

    /**
     * Returns the most actual JPAObject timestamp.
     */
    Number getNewestJPAObjectTimestamp(String oid) throws EDBException;

    /**
     * Returns the history (all objects) of a given object.
     */
    List<JPAObject> getJPAObjectHistory(String oid) throws EDBException;

    /**
     * Returns the history (between from and to) of a given object.
     */
    List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException;

    /**
     * Returns a JPAObject with the given timestamp
     */
    JPAObject getJPAObject(String oid, long timestamp) throws EDBException;

    /**
     * Returns all commits which are involved with the given oid which are between from and to
     */
    List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException;

    /**
     * Returns a list with all ever deleted JPAObjects
     */
    List<JPAObject> getDeletedJPAObjects() throws EDBException;

    /**
     * Returns all JPAObjects with the given id which are younger than the given timestamp
     */
    List<JPAObject> getJPAObjectVersionsYoungerThanTimestamp(String oid, long timestamp) throws EDBException;

    /**
     * Loads a JPACommit with the given timestamp
     */
    List<JPACommit> getJPACommit(long timestamp) throws EDBException;

    /**
     * Get all commits which are given with the param map. In the map there are values like commiter, role, etc.
     */
    List<JPACommit> getCommits(Map<String, Object> param) throws EDBException;

    /**
     * like getCommits, but it returns only the newest commit
     */
    JPACommit getLastCommit(Map<String, Object> param) throws EDBException;

    /**
     * Returns a list of JPAObjects which have all a JPAEntry with the given key and value.
     */
    List<JPAObject> query(String key, Object value) throws EDBException;
}
