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

public class JPADatabase implements org.openengsb.core.api.edb.EnterpriseDatabaseService {
    @PersistenceContext
    private EntityManagerFactory emf;
    private EntityManager entityManager;

    @Resource
    private EntityTransaction utx;

    private JPAHead head;
    private JPACriteriaFunctions criteria;

    public JPADatabase() {
        head = null;
    }
    
    public void open() throws EDBException {
        Properties props = new Properties();
        emf = Persistence.createEntityManagerFactory("edb-test", props);
        entityManager = emf.createEntityManager();
        utx = entityManager.getTransaction();
        criteria = new JPACriteriaFunctions(entityManager);

        Number max = criteria.getNewestJPAHeadNumber();
        if (max != null && max.longValue() > 0) {
            head = loadHead(max.longValue());
        }
    }

    public void close() {
        entityManager.close();
        utx = null;
        entityManager = null;
        emf = null;
    }

    @Override
    public JPACommit createCommit(String committer, String role, long timestamp) {
        return new JPACommit(committer, role, timestamp);
    }

    @Override
    public void commit(EDBCommit obj) throws EDBException {
        if (obj.getCommitted()) {
            throw new EDBException("EDBCommit was already commitet!");
        }

        obj.finalize();
        // First prepare a second head... if this fails, we don't need to continue
        JPAHead nextHead;
        if (head != null) {
            nextHead = new JPAHead(head, obj.getTimestamp());
        } else {
            nextHead = new JPAHead(obj.getTimestamp());
        }

        for (String del : obj.getDeletions()) {
            nextHead.delete(del);
        }
        for (EDBObject update : obj.getObjects()) {
            String uid = update.getUID();
            nextHead.replace(uid, update);
        }

        try {
            if (utx != null) {
                utx.begin();
            }

            long timestamp = obj.getTimestamp();
            obj.setCommitted(true);
            entityManager.persist(obj);
            entityManager.persist(nextHead);

            for (JPAObject o : nextHead.getJPAObjects()) {
                entityManager.persist(o);
            }

            for (String id : obj.getDeletions()) {
                EDBObject o = new EDBObject(id, timestamp);
                o.put("@isDeleted", new Boolean(true));
                JPAObject j = new JPAObject(o);
                entityManager.persist(j);
            }
            if (utx != null) {
                utx.commit();
            }

            head = nextHead;
        } catch (Exception ex) {
            try {
                if (utx != null) {
                    utx.rollback();
                }
            } catch (Exception e) {
                throw new EDBException("Failed to rollback transaction to DB", e);
            }
            throw new EDBException("Failed to commit transaction to DB", ex);
        }
    }

    @Override
    public EDBObject getObject(String uid) throws EDBException {
        Number number = criteria.getNewestJPAObjectTimestamp(uid);
        if (number.longValue() <= 0) {
            throw new EDBException("the given uid was never commited to the database");
        }
        JPAObject temp = criteria.getJPAObject(uid, number.longValue());
        return temp.getObject();
    }

    @Override
    public List<EDBObject> getHistory(String uid) throws EDBException {
        List<JPAObject> jpa = criteria.getJPAObjectHistory(uid);
        return generateEDBObjectList(jpa);
    }

    @Override
    public List<EDBObject> getHistory(String uid, long from, long to) throws EDBException {
        List<JPAObject> jpa = criteria.getJPAObjectHistory(uid, from, to);
        return generateEDBObjectList(jpa);
    }

    private List<EDBObject> generateEDBObjectList(List<JPAObject> jpaObjects) {
        List<EDBObject> edb = new ArrayList<EDBObject>();
        for (JPAObject j : jpaObjects) {
            edb.add(j.getObject());
        }
        return edb;
    }

    @Override
    public List<EDBLogEntry> getLog(String uid, long from, long to) throws EDBException {
        List<EDBObject> hist = getHistory(uid, from, to);
        List<JPACommit> commits = criteria.getJPACommit(uid, from, to);
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

    private JPAHead loadHead(long timestamp) throws EDBException {
        return criteria.getJPAHead(timestamp);
    }

    @Override
    public List<EDBObject> getHead() throws EDBException {
        return head.get();
    }

    @Override
    public List<EDBObject> getHead(long timestamp) throws EDBException {
        JPAHead head = loadHead(timestamp);
        if (head != null) {
            return head.get();
        }
        throw new EDBException("Failed to get head for timestamp " + Long.toString(timestamp));
    }

    @Override
    public List<EDBObject> query(String key, Object value) throws EDBException {
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
                    return new ArrayList<EDBObject>();
                }

            }
            List<EDBObject> res = new ArrayList<EDBObject>();
            for (JPAObject obj : result) {
                res.add(obj.getObject());
            }
            return res;
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
    public List<String> getResurrectedUIDs() throws EDBException {
        List<JPAObject> objects = criteria.getDeletedJPAObjects();
        List<String> result = new ArrayList<String>();
        for (JPAObject o : objects) {
            List<JPAObject> temp = criteria.getJPAObjectVersionsYoungerThanTimestamp(o.getUID(), o.getTimestamp());
            if (temp.size() != 0) {
                result.add(o.getUID());
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
