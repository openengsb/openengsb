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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.NotSupportedException;
import javax.transaction.UserTransaction;

import org.openengsb.core.edb.Commit;
import org.openengsb.core.edb.Diff;
import org.openengsb.core.edb.EDBObject;
import org.openengsb.core.edb.LogEntry;
import org.openengsb.core.edb.exceptions.EDBException;
import org.openengsb.core.edb.exceptions.NoDatabaseSelectedException;
import org.openengsb.core.edb.exceptions.NotImplementedException;

public class JPADatabase implements org.openengsb.core.edb.Database {
    @PersistenceContext
    EntityManagerFactory emf;
    EntityManager em;
    @Resource
    EntityTransaction utx;

    private String dbname;
    private JPAHead head;
    private JPADatabaseType type;

    public JPADatabase() {
        dbname = null;
        head = null;
    }

    public JPADatabase(String databaseName) {
        dbname = databaseName;
        head = null;
    }

    @Override
    public void open() throws EDBException {
        if (dbname == null) {
            throw new NoDatabaseSelectedException(
                "There is no database name defined. Unable to connect to unknown database");
        }
        if (type == null) {
            throw new EDBException("The database type have to be set for a jpa connection");
        }
        Properties props = null;
        try {
            props = type.getPropertiesForDatabaseType();
            String connectionUrl = type.getConnectionPrefix() + dbname;
            props.setProperty("openjpa.ConnectionURL", connectionUrl);
        } catch (NotSupportedException ex) {
            throw new NotImplementedException("this type of jpa connection isn't implemented", ex);
        }
        emf = Persistence.createEntityManagerFactory("openjpa", props);
        em = emf.createEntityManager();
        utx = em.getTransaction();
        Query q = em.createQuery("SELECT max(h.timestamp) FROM JPA2Head h");
        Number max = (Number) q.getSingleResult();
        if (max != null && max.longValue() > 0) {
            head = loadHead(max.longValue());
            // System.out.println("HEAD WITH " + _head.count());
        }
    }

    @Override
    public void close() {
        em.close();
        utx = null;
        em = null;
        emf = null;
    }

    @Override
    public void setDatabase(String databaseName) {
        dbname = databaseName;
    }

    @Override
    public void setDatabaseType(JPADatabaseType databaseType) {
        type = databaseType;
    }

    @Override
    public Commit createCommit(String committer, String role, long timestamp) {
        return new JPACommit(committer, role, timestamp, this);
    }

    @Override
    public void commit(Commit obj) throws EDBException {
        obj.finalize();
        // First prepare a second head... if this fails, we don't need to continue hehe
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

        utx.begin();
        try {
            long timestamp = obj.getTimestamp();
            em.persist(obj);
            em.persist(nextHead);

            for (JPAObject o : nextHead.getJPAObjects()) {
                em.persist(o);
            }

            for (String id : obj.getDeletions()) {
                EDBObject o = new EDBObject(id, timestamp);
                o.put("@isDeleted", new Boolean(true));
                // em.persist(o);
                JPAObject j = new JPAObject(o);
                em.persist(j);
            }
            utx.commit();
            head = nextHead;
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception e) {
                throw new EDBException("Failed to rollback transaction to DB " + e.toString());
            }
            throw new EDBException("Failed to commit transaction to DB " + ex.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public EDBObject getObject(String uid) throws EDBException {
        Query query = em.createQuery("select max(o.timestamp) from JPAObject o where o.uid = :uid");
        query.setParameter("uid", uid);
        Number number = (Number) query.getSingleResult();
        if (number.longValue() <= 0) {
            return null;
        }
        query = em.createQuery("select o from JPAObject o where o.uid = :uid AND o.timestamp = :time");
        query.setParameter("uid", uid);
        query.setParameter("time", number.longValue());
        List<JPAObject> obj = query.getResultList();
        if (obj.size() < 1) {
            throw new EDBException("Failed to query existing object");
        } else if (obj.size() > 1) {
            throw new EDBException("Received more than 1 object which should not be possible!");
        }
        return obj.get(0).getObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EDBObject> getHistory(String uid) throws EDBException {
        Query query = em.createQuery("select o from JPAObject o where o.uid = :uid");
        query.setParameter("uid", uid);
        List<JPAObject> jpa = query.getResultList();
        return generateEDBObjectList(jpa);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EDBObject> getHistory(String uid, long from, long to) throws EDBException {
        Query query =
            em.createQuery("select o from JPAObject o where o.uid = :uid AND o.timestamp"
                    + " BETWEEN :from AND :to order by o.timestamp");
        query.setParameter("uid", uid);
        query.setParameter("from", from);
        query.setParameter("to", to);
        List<JPAObject> jpa = query.getResultList();
        return generateEDBObjectList(jpa);
    }

    private List<EDBObject> generateEDBObjectList(List<JPAObject> jpaObjects) {
        List<EDBObject> edb = new ArrayList<EDBObject>();
        for (JPAObject j : jpaObjects) {
            edb.add(j.getObject());
        }
        return edb;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LogEntry> getLog(String uid, long from, long to) throws EDBException {
        List<EDBObject> hist = getHistory(uid, from, to);
        Query query =
            em.createQuery("select c from JPACommit c where c.timestamp in "
                    + "(select o.timestamp from JPAObject o where o.uid = :uid and "
                    + "o.timestamp between :from and :to) order by c.timestamp");
        query.setParameter("uid", uid);
        query.setParameter("from", from);
        query.setParameter("to", to);
        List<Commit> commits = query.getResultList();
        if (hist.size() != commits.size()) {
            throw new EDBException("inconsistent log " + Integer.toString(commits.size()) + " commits for "
                    + Integer.toString(hist.size()) + " history entries");
        }
        List<LogEntry> log = new ArrayList<LogEntry>();
        for (int i = 0; i < hist.size(); ++i) {
            log.add(new LogEntry(commits.get(i), hist.get(i)));
        }
        return log;
    }

    @SuppressWarnings("unchecked")
    public JPAHead loadHead(long timestamp) throws EDBException {
        Query query = em.createQuery("select h from JPA2Head h where h.timestamp = :time");
        query.setParameter("time", timestamp);
        List<JPAHead> list = query.getResultList();
        if (list == null || list.size() != 1 || list.get(0) == null) {
            throw new EDBException("Head not found for timestamp " + Long.toString(timestamp));
        }
        JPAHead head = list.get(0);
        return head;
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
        // actually loadhead should have covered this already
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
            JPAQueryBuilderNew builder = new JPAQueryBuilderNew(em, queryMap);
            return generateEDBObjectList(builder.getResults());

            // only temporary commented. JPAQueryBuilderNew is only a temporary solution
            // until a bug in JPA is eliminated.

            // JPAQueryBuilder builder = new JPAQueryBuilder(queryMap);
            //
            // String queryString = "select o from JPAObject o where " + builder.getQuery();
            // List<Object> params = builder.getParams();
            //
            // Query query = em.createQuery(queryString);
            // System.out.println(queryString);
            // for (int i = 0; i < params.size(); ++i) {
            // query.setParameter("param" + i, params.get(i));
            // }
            // List<JPAObject> list = query.getResultList();
            // if (list == null)
            // return null;
            // List<EDBObject> out = new ArrayList<EDBObject>();
            // for (JPAObject jpa : list)
            // out.add(jpa.getObject());
            // return out;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EDBException("fail", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public EDBObject queryExt(String k, String v) throws EDBException {
        try {
            Query query = em.createQuery("select o from JPAObject o where exists "
                            + "(select v from o.values v where "
                            + "(v.key = :key and v.value LIKE :value))");
            query.setParameter("key", k);
            query.setParameter("value", v);
            List<JPAObject> list = query.getResultList();
            if (list.size() == 0 || list.get(0) == null) {
                return null;
            }
            return list.get(0).getObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EDBException("fail");
        }
    }

    @Override
    public List<Commit> getCommits(String key, Object value) throws EDBException {
        Map<String, Object> q = new HashMap<String, Object>();
        q.put(key, value);
        return getCommits(q);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Commit> getCommits(Map<String, Object> query) throws EDBException {
        JPAQueryCommitBuilder builder = new JPAQueryCommitBuilder("select c from JPACommit c", query);
        Query jquery = em.createQuery(builder.getSQLCommand());
        for (Map.Entry<String, Object> p : builder.getParams().entrySet()) {
            jquery.setParameter(p.getKey(), p.getValue());
        }
        List<Commit> commits = jquery.getResultList();
        if (commits == null) {
            commits = new ArrayList<Commit>();
        }
        return commits;
    }

    @Override
    public Commit getLastCommit(String key, Object value) throws EDBException {
        Map<String, Object> q = new HashMap<String, Object>();
        q.put(key, value);
        return getLastCommit(q);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Commit getLastCommit(Map<String, Object> query) throws EDBException {
        JPAQueryCommitBuilder builder = new JPAQueryCommitBuilder("select max(c.timestamp) from JPACommit c", query);
        Query jquery = em.createQuery(builder.getSQLCommand());
        for (Map.Entry<String, Object> p : builder.getParams().entrySet()) {
            jquery.setParameter(p.getKey(), p.getValue());
        }
        List<Number> time = jquery.getResultList();
        if (time.size() == 0 || time.get(0).longValue() == 0) {
            return null;
        }
        jquery =
            em.createQuery("select c from JPACommit c " + builder.getWhereClause() + " and c.timestamp = :maxtime");
        for (Map.Entry<String, Object> p : builder.getParams().entrySet()) {
            jquery.setParameter(p.getKey(), p.getValue());
        }
        jquery.setParameter("maxtime", time.get(0).longValue());
        List<Commit> commits = jquery.getResultList();
        if (commits.size() != 1) {
            throw new EDBException("Found no commit for an existing timestamp!");
        }
        return commits.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Commit getCommit(long from) throws EDBException {
        Query query = em.createQuery("select c from JPACommit c where c.timestamp = :time");
        query.setParameter("time", from);
        List<Commit> commits = query.getResultList();
        if (commits == null || commits.size() != 1) {
            return null;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getResurrectedUIDs() throws EDBException {
        Query query = em.createQuery("select o from JPAObject o where o.isDeleted = true");
        List<JPAObject> objects = query.getResultList();

        List<String> result = new ArrayList<String>();
        for (JPAObject o : objects) {
            query = em.createQuery("select o from JPAObject o where o.uid = :uid and o.timestamp > :timestamp");
            query.setParameter("uid", o.getUID());
            query.setParameter("timestamp", o.getTimestamp());
            if (query.getResultList().size() != 0) {
                result.add(o.getUID());
            }
        }
        return result;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(
            Map<String, Object> query) throws EDBException {
        Commit ci = getLastCommit(query);
        if (ci == null) {
            return null;
        }
        return getHead(ci.getTimestamp());
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(String key, Object value) throws EDBException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(key, value);
        return getStateOfLastCommitMatching(query);
    }

    @Override
    public UserTransaction getUserTransaction() throws EDBException {
        throw new NotImplementedException();
    }
}
