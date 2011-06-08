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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBLogEntry;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.edb.internal.dao.DefaultJPADao;
import org.openengsb.core.edb.internal.dao.JPADao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPADatabase implements org.openengsb.core.api.edb.EnterpriseDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JPADatabase.class);
    private EntityTransaction utx;
    @PersistenceContext(name = "openengsb-edb")
    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private JPAHead head;
    private JPADao dao;

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
        dao = new DefaultJPADao(entityManager);
        LOGGER.debug("starting of EDB successful");

        Number max = dao.getNewestJPAHeadNumber();
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
    public JPACommit createCommit(String committer, String role) {
        LOGGER.debug("creating commit for committer " + committer + " with role " + role);
        return new JPACommit(committer, role);
    }

    @Override
    public Long commit(EDBCommit commit) throws EDBException {
        if (commit.isCommitted()) {
            throw new EDBException("EDBCommit was already commitet!");
        }

        long timestamp = System.currentTimeMillis();
        commit.setTimestamp(timestamp);

        JPAHead nextHead;
        if (head != null) {
            LOGGER.debug("adding a new JPAHead");
            nextHead = new JPAHead(head, timestamp);
        } else {
            LOGGER.debug("creating the first JPAHead");
            nextHead = new JPAHead(timestamp);
        }

        for (String del : commit.getDeletions()) {
            nextHead.delete(del);
        }
        for (EDBObject update : commit.getObjects()) {
            String oid = update.getOID();
            update.updateTimestamp(timestamp);
            nextHead.replace(oid, update);
        }

        try {
            performUtxAction(UTXACTION.BEGIN);
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
                EDBObject o = new EDBObject(id);
                o.updateTimestamp(timestamp);
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

        return timestamp;
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
        Number number = dao.getNewestJPAObjectTimestamp(oid);
        if (number.longValue() <= 0) {
            throw new EDBException("the given oid " + oid + " was never commited to the database");
        }
        LOGGER.debug("loading JPAObject with the oid " + oid + " and the timestamp " + number.longValue());
        JPAObject temp = dao.getJPAObject(oid, number.longValue());
        return temp.getObject();
    }

    @Override
    public List<EDBObject> getHistory(String oid) throws EDBException {
        LOGGER.debug("loading history of JPAObject with the oid " + oid);
        List<JPAObject> jpa = dao.getJPAObjectHistory(oid);
        return generateEDBObjectList(jpa);
    }

    @Override
    public List<EDBObject> getHistory(String oid, Long from, Long to) throws EDBException {
        LOGGER.debug("loading JPAObject with the oid " + oid + " from"
                + " the timestamp " + from + " to the timestamp " + to);
        List<JPAObject> jpa = dao.getJPAObjectHistory(oid, from, to);
        return generateEDBObjectList(jpa);
    }

    /**
     * transforms a list of JPAObjects to a List of EDBObjects
     */
    private List<EDBObject> generateEDBObjectList(List<JPAObject> jpaObjects) {
        List<EDBObject> result = new ArrayList<EDBObject>();
        for (JPAObject j : jpaObjects) {
            result.add(j.getObject());
        }
        return result;
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to) throws EDBException {
        LOGGER.debug("loading the log of JPAObject with the oid " + oid + " from"
                + " the timestamp " + from + " to the timestamp " + to);
        List<EDBObject> history = getHistory(oid, from, to);
        List<JPACommit> commits = dao.getJPACommit(oid, from, to);
        if (history.size() != commits.size()) {
            throw new EDBException("inconsistent log " + Integer.toString(commits.size()) + " commits for "
                    + Integer.toString(history.size()) + " history entries");
        }
        List<EDBLogEntry> log = new ArrayList<EDBLogEntry>();
        for (int i = 0; i < history.size(); ++i) {
            log.add(new LogEntry(commits.get(i), history.get(i)));
        }
        return log;
    }

    /**
     * loads the JPAHead with the given timestamp
     */
    private JPAHead loadHead(long timestamp) throws EDBException {
        LOGGER.debug("load the JPAHead with the timestamp " + timestamp);
        return dao.getJPAHead(timestamp);
    }

    @Override
    public List<EDBObject> getHead() throws EDBException {
        return head.getEDBObjects();
    }

    @Override
    public List<EDBObject> getHead(long timestamp) throws EDBException {
        LOGGER.debug("load the elements of the JPAHead with the timestamp " + timestamp);
        JPAHead head = loadHead(timestamp);
        if (head != null) {
            return head.getEDBObjects();
        }
        throw new EDBException("Failed to get head for timestamp " + Long.toString(timestamp));
    }

    @Override
    public List<EDBObject> query(String key, Object value) throws EDBException {
        LOGGER.debug("query for objects with key = " + key + " and value = " + value);
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return query(queryMap);
    }

    @Override
    public List<EDBObject> query(Map<String, Object> queryMap) throws EDBException {
        try {
            Set<JPAObject> result = new HashSet<JPAObject>();

            for (Entry<String, Object> entry : queryMap.entrySet()) {
                analyzeEntry(entry, result);

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

    private void analyzeEntry(Entry<String, Object> entry, Set<JPAObject> set) {
        String key = entry.getKey();
        Object value = entry.getValue();

        List<JPAObject> temp = dao.query(key, value);

        if (temp.size() == 0) {
            set = new HashSet<JPAObject>();
            return;
        }
        if (set.size() == 0) {
            set.addAll(temp);
        } else {
            set.retainAll(temp);
        }
    }

    @Override
    public List<EDBCommit> getCommits(String key, Object value) throws EDBException {
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return getCommits(queryMap);
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> queryMap) throws EDBException {
        List<JPACommit> commits = dao.getCommits(queryMap);
        return new ArrayList<EDBCommit>(commits);
    }

    @Override
    public JPACommit getLastCommit(String key, Object value) throws EDBException {
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return getLastCommit(queryMap);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> queryMap) throws EDBException {
        JPACommit result = dao.getLastCommit(queryMap);
        return result;
    }

    @Override
    public JPACommit getCommit(Long from) throws EDBException {
        List<JPACommit> commits = dao.getJPACommit(from);
        if (commits == null) {
            throw new EDBException("there is no commit for this timestamp");
        } else if (commits.size() != 1) {
            throw new EDBException("there are more than one commit for one timestamp");
        }
        return commits.get(0);
    }

    @Override
    public Diff getDiff(Long firstTimestamp, Long secondTimestamp) throws EDBException {
        List<EDBObject> headA = getHead(firstTimestamp);
        List<EDBObject> headB = getHead(secondTimestamp);

        return new Diff(getCommit(firstTimestamp), getCommit(secondTimestamp), headA, headB);
    }

    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        List<JPAObject> objects = dao.getDeletedJPAObjects();
        List<String> result = new ArrayList<String>();
        for (JPAObject o : objects) {
            List<JPAObject> temp = dao.getJPAObjectVersionsYoungerThanTimestamp(o.getOID(), o.getTimestamp());
            if (temp.size() != 0) {
                result.add(o.getOID());
            }
        }
        return result;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(
            Map<String, Object> queryMap) throws EDBException {
        JPACommit ci = getLastCommit(queryMap);
        return getHead(ci.getTimestamp());
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(String key, Object value) throws EDBException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(key, value);
        return getStateOfLastCommitMatching(query);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        dao = new DefaultJPADao(entityManager);
    }
}
