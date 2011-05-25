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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBLogEntry;
import org.openengsb.core.api.edb.EDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPADatabase implements org.openengsb.core.api.edb.EnterpriseDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JPADatabase.class);
    @Resource
    private EntityTransaction utx;
    @PersistenceContext
    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private JPAHead head;
    private JPACriteriaFunctions criteria;

    public JPADatabase() {
        head = null;
    }

    /**
     * this is just for testing the JPADatabase. Should only be called in the corresponding test class.
     */
    public void open() throws EDBException {
        LOGGER.debug("starting to open EDB for testing via JPA");
        Properties props = new Properties();
        emf = Persistence.createEntityManagerFactory("edb-test", props);
        entityManager = emf.createEntityManager();
        utx = entityManager.getTransaction();
        criteria = new JPACriteriaFunctions(entityManager);
        LOGGER.debug("starting of EDB successful");

        Number max = criteria.getNewestJPAHeadNumber();
        if (max != null && max.longValue() > 0) {
            LOGGER.debug("loading JPA Head with timestamp " + max.longValue());
            head = loadHead(max.longValue());
        }
    }

    /**
     * this is just for testing the JPADatabase. Should only be called in the corresponding test class.
     */
    public void close() {
        entityManager.close();
        utx = null;
        entityManager = null;
        emf = null;
    }

    @Override
    public JPACommit createCommit(String committer, String role, long timestamp) {
        LOGGER.debug("creating commit for committer %s with role %s at timestamp %d",
            Arrays.asList(committer, role, timestamp + "").toArray());
        return new JPACommit(committer, role, timestamp);
    }

    @Override
    public void commit(EDBCommit commit) throws EDBException {
        if (commit.isCommitted()) {
            throw new EDBException("EDBCommit was already commitet!");
        }

        commit.finalize();
        JPAHead nextHead;
        if (head != null) {
            LOGGER.debug("adding a new JPAHead");
            nextHead = new JPAHead(head, commit.getTimestamp());
        } else {
            LOGGER.debug("creating the first JPAHead");
            nextHead = new JPAHead(commit.getTimestamp());
        }

        for (String del : commit.getDeletions()) {
            nextHead.delete(del);
        }
        for (EDBObject update : commit.getObjects()) {
            String oid = update.getOID();
            nextHead.replace(oid, update);
        }

        try {
            performUtxAction(UTXACTION.BEGIN);

            long timestamp = commit.getTimestamp();
            commit.setCommitted(true);
            LOGGER.debug("persisting JPACommit and the new JPAHead");
            entityManager.persist(commit);
            entityManager.persist(nextHead);

            LOGGER.debug("persisting the elements of the new JPAHead");
            for (JPAObject o : nextHead.getJPAObjects()) {
                entityManager.persist(o);
            }

            LOGGER.debug("setting the deleted elements as deleted");
            for (String id : commit.getDeletions()) {
                EDBObject o = new EDBObject(id, timestamp);
                o.put("isDeleted", new Boolean(true));
                JPAObject j = new JPAObject(o);
                entityManager.persist(j);
            }

            performUtxAction(UTXACTION.COMMIT);
            head = nextHead;
        } catch (Exception ex) {
            try {
                performUtxAction(UTXACTION.ROLLBACK);
            } catch (Exception e) {
                throw new EDBException("Failed to rollback transaction to DB", e);
            }
            throw new EDBException("Failed to commit transaction to DB", ex);
        }
    }

    /**
     * helper function that performs a UTXACTION if the utx is not null
     */
    private void performUtxAction(UTXACTION action) {
        if (utx == null) {
            return;
        }
        switch (action) {
            case BEGIN:
                utx.begin();
                break;
            case COMMIT:
                utx.commit();
                break;
            case ROLLBACK:
                utx.rollback();
                break;
            default:
                LOGGER.warn("unknown Transaction action: " + action.toString());
                break;
        }
    }

    /**
     * enumeration for categorizing the transaction actions.
     */
    private enum UTXACTION {
            BEGIN, COMMIT, ROLLBACK
    };

    @Override
    public EDBObject getObject(String oid) throws EDBException {
        Number number = criteria.getNewestJPAObjectTimestamp(oid);
        if (number.longValue() <= 0) {
            throw new EDBException("the given oid was never commited to the database");
        }
        LOGGER.debug("loading JPAObject with the oid %s and the timestamp %d", Arrays.asList(oid, number).toArray());
        JPAObject temp = criteria.getJPAObject(oid, number.longValue());
        return temp.getObject();
    }

    @Override
    public List<EDBObject> getHistory(String oid) throws EDBException {
        LOGGER.debug("loading history of JPAObject with the oid %s", oid);
        List<JPAObject> jpa = criteria.getJPAObjectHistory(oid);
        return generateEDBObjectList(jpa);
    }

    @Override
    public List<EDBObject> getHistory(String oid, long from, long to) throws EDBException {
        LOGGER.debug("loading JPAObject with the oid " + oid + " from"
                + " the timestamp " + from + " to the timestamp " + to);
        List<JPAObject> jpa = criteria.getJPAObjectHistory(oid, from, to);
        return generateEDBObjectList(jpa);
    }

    /**
     * transforms a list of JPAObjects to a List of EDBObjects
     */
    private List<EDBObject> generateEDBObjectList(List<JPAObject> jpaObjects) {
        List<EDBObject> edb = new ArrayList<EDBObject>();
        for (JPAObject j : jpaObjects) {
            edb.add(j.getObject());
        }
        return edb;
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, long from, long to) throws EDBException {
        LOGGER.debug("loading the log of JPAObject with the oid " + oid + " from"
                + " the timestamp " + from + " to the timestamp " + to);
        List<EDBObject> hist = getHistory(oid, from, to);
        List<JPACommit> commits = criteria.getJPACommit(oid, from, to);
        if (hist.size() != commits.size()) {
            throw new EDBException("inconsistent log " + Integer.toString(commits.size()) + " commits for "
                    + Integer.toString(hist.size()) + " history entries");
        }
        List<EDBLogEntry> log = new ArrayList<EDBLogEntry>();
        for (int i = 0; i < hist.size(); ++i) {
            log.add(new LogEntry(commits.get(i), hist.get(i)));
        }
        return log;
    }

    /**
     * loads the JPAHead with the given timestamp
     */
    private JPAHead loadHead(long timestamp) throws EDBException {
        LOGGER.debug("load the JPAHead with the timestamp %d", timestamp);
        return criteria.getJPAHead(timestamp);
    }

    @Override
    public List<EDBObject> getHead() throws EDBException {
        return head.getEDBObjects();
    }

    @Override
    public List<EDBObject> getHead(long timestamp) throws EDBException {
        LOGGER.debug("load the elements of the JPAHead with the timestamp %d", timestamp);
        JPAHead head = loadHead(timestamp);
        if (head != null) {
            return head.getEDBObjects();
        }
        throw new EDBException("Failed to get head for timestamp " + Long.toString(timestamp));
    }

    @Override
    public List<EDBObject> query(String key, Object value) throws EDBException {
        LOGGER.debug("query for objects with key = %s and value = %s", Arrays.asList(key, value).toArray());
        Map<String, Object> q = new HashMap<String, Object>();
        q.put(key, value);
        return query(q);
    }

    @Override
    public List<EDBObject> query(Map<String, Object> queryMap) throws EDBException {
        try {
            Set<JPAObject> result = new HashSet<JPAObject>();

            for (Entry<String, Object> entry : queryMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                List<JPAObject> temp = criteria.query(key, value);
                if (temp.size() == 0) {
                    return new ArrayList<EDBObject>();
                }
                if (result.size() == 0) {
                    result.addAll(temp);
                } else {
                    result.retainAll(temp);
                }

                // if the result size at this position ever get 0 we know that there is at least
                // one object that has at least one key/value pair that has at least one of the
                // others not.
                if (result.size() == 0) {
                    LOGGER.debug("there are no objects which have all values from the map");
                    return new ArrayList<EDBObject>();
                }
            }
            return generateEDBObjectList(new ArrayList<JPAObject>(result));
        } catch (Exception ex) {
            throw new EDBException("failed to query for objects with the given map", ex);
        }
    }

    @Override
    public List<EDBCommit> getCommits(String key, Object value) throws EDBException {
        Map<String, Object> q = new HashMap<String, Object>();
        q.put(key, value);
        return getCommits(q);
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> query) throws EDBException {
        List<JPACommit> commits = criteria.getCommits(query);
        return new ArrayList<EDBCommit>(commits);
    }

    @Override
    public JPACommit getLastCommit(String key, Object value) throws EDBException {
        Map<String, Object> q = new HashMap<String, Object>();
        q.put(key, value);
        return getLastCommit(q);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> query) throws EDBException {
        JPACommit result = criteria.getLastCommit(query);
        return result;
    }

    @Override
    public JPACommit getCommit(long from) throws EDBException {
        List<JPACommit> commits = criteria.getJPACommit(from);
        if (commits == null || commits.size() != 1) {
            throw new EDBException("there is no commit for this timestamp");
        }
        return commits.get(0);
    }

    @Override
    public Diff getDiff(long min, long max) throws EDBException {
        List<EDBObject> headA;
        List<EDBObject> headB;
        headA = getHead(min);
        headB = getHead(max);

        return new Diff(getCommit(min), getCommit(max), headA, headB);
    }

    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        List<JPAObject> objects = criteria.getDeletedJPAObjects();
        List<String> result = new ArrayList<String>();
        for (JPAObject o : objects) {
            List<JPAObject> temp = criteria.getJPAObjectVersionsYoungerThanTimestamp(o.getOID(), o.getTimestamp());
            if (temp.size() != 0) {
                result.add(o.getOID());
            }
        }
        return result;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(
            Map<String, Object> query) throws EDBException {
        JPACommit ci = getLastCommit(query);
        return getHead(ci.getTimestamp());
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(String key, Object value) throws EDBException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(key, value);
        return getStateOfLastCommitMatching(query);
    }

    public void setEntityManager(EntityManager em) {
        this.entityManager = em;
        criteria = new JPACriteriaFunctions(em);
    }
}
