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
import org.openengsb.core.edb.internal.QJPAObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPASubQuery;
import com.mysema.query.types.Predicate;

public class QueryDslJPADao implements JPADao {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDslJPADao.class);
    private EntityManager entityManager;

    public QueryDslJPADao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public JPAHead getJPAHead(long timestamp) throws EDBException {
        LOGGER.debug("Loading JPAHead for timestamp {}", timestamp);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        QJPAObject subObject = QJPAObject.jPAObject;

        JPASubQuery sub = new JPASubQuery().from(subObject)
            .where(subObject.oid.eq(object.oid).and(subObject.timestamp.loe(timestamp)));

        List<JPAObject> result =
            query.from(object)
                 .where(
                     object.isDeleted.isFalse().and(
                         object.timestamp.eq(sub
                             .unique(object.timestamp.max())))).list(object);

        JPAHead head = new JPAHead();
        head.setJPAObjects(result);
        head.setTimestamp(timestamp);
        return head;
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
        return query.from(object).where(object.oid.eq(oid).and(object.timestamp.between(from, to)))
            .orderBy(object.timestamp.asc()).list(object);
    }

    @Override
    public JPAObject getJPAObject(String oid) throws EDBException {
        LOGGER.debug("Loading newest object {}", oid);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        JPAObject result =
            query.from(object).where(object.oid.eq(oid)).orderBy(object.timestamp.desc()).limit(1).uniqueResult(object);
        if (result == null) {
            throw new EDBException("the given oid " + oid + " was never commited to the database");
        }
        return result;
    }

    @Override
    public List<JPAObject> getJPAObjects(List<String> oids) throws EDBException {
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        QJPAObject subObject = new QJPAObject("subObject");
        JPASubQuery sub = new JPASubQuery().from(subObject).where(subObject.oid.eq(object.oid));
        List<JPAObject> result =
            query
                .from(object)
                .where(
                    object.oid.in(oids).and(object.isDeleted.isFalse())
                        .and(object.timestamp.eq(sub.unique(object.timestamp.max())))).list(object);

        return result;
    }

    @Override
    public JPAObject getJPAObject(String oid, long timestamp) throws EDBException {
        LOGGER.debug("Loading object {} for the time {}", oid, timestamp);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        List<JPAObject> resultList =
            query.from(object).where(object.oid.eq(oid).and(object.timestamp.loe(timestamp)))
                .orderBy(object.timestamp.desc()).limit(1).list(object);
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
    public List<JPACommit> getJPACommit(long timestamp) throws EDBException {
        LOGGER.debug("Load the commit for the timestamp {}", timestamp);
        JPQLQuery query = new JPAQuery(entityManager);
        QJPACommit commit = QJPACommit.jPACommit;
        return query.from(commit).where(commit.timestamp.loe(timestamp)).limit(1).list(commit);
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
    public List<String> getResurrectedOIDs() throws EDBException {
        LOGGER.debug("get resurrected JPA objects");
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;
        QJPAObject subObject = new QJPAObject("subObject");

        JPASubQuery sub =
            new JPASubQuery().from(subObject).where(
                subObject.oid.eq(object.oid).and(
                    subObject.isDeleted.isTrue().and(object.timestamp.gt(subObject.timestamp))));

        List<String> resultList =
            query.from(object).where(object.isDeleted.isFalse().and(sub.exists())).list(object.oid);
        return resultList;
    }

    @Override
    public List<JPAObject> query(Map<String, Object> values) throws EDBException {
        JPQLQuery query = new JPAQuery(entityManager);
        QJPAObject object = QJPAObject.jPAObject;

        query.from(object);
        int i = 0;
        for (Map.Entry<String, Object> value : values.entrySet()) {
            QJPAEntry entry = new QJPAEntry("join" + i);
            query.join(object.entries, entry);
            query.where(entry.key.eq(value.getKey()).and(entry.value.eq(value.getValue().toString())));
            i++;
        }
        List<JPAObject> result = query.orderBy(object.timestamp.desc()).list(object);
        return result;
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
