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

package org.openengsb.core.edb.internal.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.edb.internal.JPACommit;
import org.openengsb.core.edb.internal.JPAHead;
import org.openengsb.core.edb.internal.JPAObject;
import org.openengsb.core.edb.internal.QJPACommit;
import org.openengsb.core.edb.internal.QJPAEntry;
import org.openengsb.core.edb.internal.QJPAHead;
import org.openengsb.core.edb.internal.QJPAObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPASubQuery;
import com.mysema.query.types.Predicate;

public class QueryDslJPADao implements JPADao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJPADao.class);
    private EntityManager entityManager;

    public QueryDslJPADao() {
    }

    public QueryDslJPADao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Number getNewestJPAHeadNumber() throws EDBException {
        LOGGER.debug("getting newest jpa head timestamp");
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAHead head = QJPAHead.jPAHead;
        return query.from(head).uniqueResult(head.timestamp.max());
    }

    @Override
    public JPAHead getJPAHead(long timestamp) throws EDBException {
        LOGGER.debug("Loading JPAHead for timestamp {}", timestamp);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAHead head = QJPAHead.jPAHead;
        List<JPAHead> resultList = query.from(head).where(head.timestamp.eq(timestamp)).list(head);
        if (resultList == null || resultList.get(0) == null) {
            throw new EDBException("Head not found for timestamp " + timestamp);
        } else if (resultList.size() != 1) {
            throw new EDBException("Multiple heads found for the timestamp " + timestamp);
        }
        return resultList.get(0);
    }

    @Override
    public Number getNewestJPAObjectTimestamp(String oid) throws EDBException {
        LOGGER.debug("Loading newest Timestamp for object with the id {}", oid);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        return query.from(object).where(object.oid.eq(oid)).uniqueResult(object.timestamp.max());
    }

    @Override
    public List<JPAObject> getJPAObjectHistory(String oid) throws EDBException {
        LOGGER.debug("Loading the history for the object {}", oid);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        return query.from(object).where(object.oid.eq(oid)).list(object);
    }

    @Override
    public List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException {
        LOGGER.debug("Loading the history for the object {} from {} to {}", from, to);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        return query.from(object).where(object.oid.eq(oid).and(object.timestamp.between(from, to))).list(object);
    }

    @Override
    public JPAObject getJPAObject(String oid) throws EDBException {
        LOGGER.debug("Loading newest object {}", oid);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        JPAObject result =
            query
                .from(object)
                .where(
                    object.oid.eq(oid).and(
                        object.timestamp.eq(new JPASubQuery().from(object).where(object.oid.eq(oid))
                            .unique(object.timestamp.max())))).uniqueResult(object);
        if (result == null) {
            throw new EDBException("the given oid " + oid + " was never commited to the database");
        }
        return result;
    }

    @Override
    public JPAObject getJPAObject(String oid, long timestamp) throws EDBException {
        LOGGER.debug("Loading object {} for the time {}", oid, timestamp);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        JPASubQuery sub = new JPASubQuery().from(object)
            .where(object.oid.eq(oid).and(object.timestamp.loe(timestamp)));
        List<JPAObject> resultList =
            query
                .from(object)
                .where(
                    object.oid.eq(oid).and(
                        object.timestamp.eq(
                            sub.unique(object.timestamp.max())))).list(object);
        if (resultList.size() < 1) {
            throw new EDBException("Failed to query existing object");
        } else if (resultList.size() > 1) {
            throw new EDBException("Received more than 1 object which should not be possible!");
        }
        return resultList.get(0);
    }

    @Override
    public List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException {
        LOGGER.debug("Loading all commits which involve object {} from {} to {}", new Object[]{ oid, from, to });
        JPQLQuery query = new JPAQuery(entityManager);
        QJPACommit commit = QJPACommit.jPACommit;
        QJPAObject object = QJPAObject.jPAObject;
        JPASubQuery sub = new JPASubQuery().from(object)
            .where(object.oid.eq(oid).and(object.timestamp.between(from, to)));
        return query.from(commit).where(commit.timestamp.in(sub.list(object.timestamp))).
            orderBy(commit.timestamp.asc()).list(commit);
    }

    @Override
    public List<JPAObject> getDeletedJPAObjects() throws EDBException {
        LOGGER.debug("Load all deleted JPAObjects");
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        return query.from(object).where(object.isDeleted.eq(true)).list(object);
    }

    @Override
    public List<JPAObject> getJPAObjectVersionsYoungerThanTimestamp(String oid, long timestamp) throws EDBException {
        LOGGER.debug("Load all objects with the given oid {} which are younger than {}", oid, timestamp);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        return query.from(object).where(object.oid.eq(oid).and(object.timestamp.gt(timestamp))).list(object);
    }

    @Override
    public List<JPACommit> getJPACommit(long timestamp) throws EDBException {
        LOGGER.debug("Load the commit for the timestamp {}", timestamp);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPACommit commit = QJPACommit.jPACommit;
        return query.from(commit).where(commit.timestamp.eq(timestamp)).list(commit);
    }

    @Override
    public List<JPACommit> getCommits(Map<String, Object> param) throws EDBException {
        LOGGER.debug("Get commits which are given to a param map with {} elements", param.size());
        JPQLQuery query = new JPAQuery(entityManager);
        QJPACommit commit = QJPACommit.jPACommit;
        return query.from(commit).where(analyzeParamMap(commit, param)).list(commit);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> param) throws EDBException {
        LOGGER.debug("Get last commit which are given to a param map with {} elements", param.size());
        JPQLQuery query = new JPAQuery(entityManager);
        QJPACommit commit = QJPACommit.jPACommit;
        return query.from(commit).where(analyzeParamMap(commit, param)).orderBy(commit.timestamp.asc())
            .uniqueResult(commit);
    }

    /**
     * Analyzes the map and filters the values which are used for query
     */
    private Predicate[] analyzeParamMap(QJPACommit commit, Map<String, Object> param) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (Map.Entry<String, Object> q : param.entrySet()) {
            String key = q.getKey();
            Object value = q.getValue();
            if (key.equals("timestamp")) {
                predicates.add(commit.timestamp.eq((Long) value));
            } else if (key.equals("committer")) {
                predicates.add(commit.committer.eq(value.toString()));
            } else if (key.equals("context")) {
                predicates.add(commit.context.eq(value.toString()));
            }
        }
        Predicate[] temp = new Predicate[predicates.size()];
        for (int i = 0; i < predicates.size(); i++) {
            temp[i] = predicates.get(i);
        }
        return temp;
    }

    @Override
    public List<JPAObject> query(String key, Object value) throws EDBException {
        LOGGER.debug("Query for objects which have entries where key = {} and value = {}", key, value);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        QJPAEntry entry = QJPAEntry.jPAEntry;
        List<JPAObject> objects =
            query.from(object)
                .innerJoin(object.entries, entry)
                .where(entry.key.eq(key).and(entry.value.eq(value.toString()))).orderBy(object.timestamp.asc())
                .list(object);
        return objects;
    }

    @Override
    public Integer getVersionOfOid(String oid) throws EDBException {
        LOGGER.debug("loading version of model under the oid {}", oid);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        return (int) query.from(object).where(object.oid.eq(oid)).uniqueResult(object.oid.count()).longValue();
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
