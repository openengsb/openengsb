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
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.edb.internal.JPACommit;
import org.openengsb.core.edb.internal.JPAEntry;
import org.openengsb.core.edb.internal.JPAHead;
import org.openengsb.core.edb.internal.JPAObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJPADao implements JPADao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJPADao.class);
    private EntityManager entityManager;

    public DefaultJPADao() {
    }

    public DefaultJPADao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Number getNewestJPAHeadNumber() throws EDBException {
        LOGGER.debug("getting newest jpa head timestamp");
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Number> query = criteriaBuilder.createQuery(Number.class);
        Root from = query.from(JPAHead.class);
        Expression<Number> maxExpression = criteriaBuilder.max(from.get("timestamp"));
        CriteriaQuery<Number> select = query.select(maxExpression);

        TypedQuery<Number> typedQuery = entityManager.createQuery(select);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException ex) {
            throw new EDBException("there was no commit so far", ex);
        }
    }

    @Override
    public JPAHead getJPAHead(long timestamp) throws EDBException {
        LOGGER.debug("Loading JPAHead for timestamp " + timestamp);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPAHead> query = criteriaBuilder.createQuery(JPAHead.class);
        Root<JPAHead> from = query.from(JPAHead.class);

        CriteriaQuery<JPAHead> select = query.select(from);

        Predicate predicate = criteriaBuilder.equal(from.get("timestamp"), timestamp);
        query.where(predicate);

        TypedQuery<JPAHead> typedQuery = entityManager.createQuery(select);
        List<JPAHead> resultList = typedQuery.getResultList();

        if (resultList == null || resultList.get(0) == null) {
            throw new EDBException("Head not found for timestamp " + timestamp);
        } else if (resultList.size() != 1) {
            throw new EDBException("Multiple heads found for the timestamp " + timestamp);
        }

        return resultList.get(0);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Number getNewestJPAObjectTimestamp(String oid) throws EDBException {
        LOGGER.debug("Loading newest Timestamp for object with the id " + oid);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Number> query = criteriaBuilder.createQuery(Number.class);
        Root from = query.from(JPAObject.class);

        Predicate predicate = criteriaBuilder.equal(from.get("oid"), oid);
        query.where(predicate);

        Expression<Number> maxExpression = criteriaBuilder.max(from.get("timestamp"));
        CriteriaQuery<Number> select = query.select(maxExpression);

        TypedQuery<Number> typedQuery = entityManager.createQuery(select);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException e) {
            throw new EDBException("the given oid was never saved in the database", e);
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid) throws EDBException {
        LOGGER.debug("Loading the history for the object " + oid);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root from = query.from(JPAObject.class);

        Predicate predicate = criteriaBuilder.equal(from.get("oid"), oid);
        query.where(predicate);

        CriteriaQuery<JPAObject> select = query.select(from);

        TypedQuery<JPAObject> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException {
        LOGGER.debug("Loading the history for the object " + oid + " from " + from + " to " + to);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root f = query.from(JPAObject.class);

        Predicate predicate1 = criteriaBuilder.equal(f.get("oid"), oid);
        Predicate predicate2 = criteriaBuilder.between(f.get("timestamp"), from, to);
        query.where(criteriaBuilder.and(predicate1, predicate2));

        CriteriaQuery<JPAObject> select = query.select(f);

        select.orderBy(criteriaBuilder.asc(f.get("timestamp")));

        TypedQuery<JPAObject> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Override
    public JPAObject getJPAObject(String oid, long timestamp) throws EDBException {
        LOGGER.debug("Loading object " + oid + " for the time " + timestamp);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root<JPAObject> from = query.from(JPAObject.class);

        CriteriaQuery<JPAObject> select = query.select(from);

        Predicate predicate1 = criteriaBuilder.equal(from.get("timestamp"), timestamp);
        Predicate predicate2 = criteriaBuilder.equal(from.get("oid"), oid);
        query.where(criteriaBuilder.and(predicate1, predicate2));

        TypedQuery<JPAObject> typedQuery = entityManager.createQuery(select);
        List<JPAObject> resultList = typedQuery.getResultList();

        if (resultList.size() < 1) {
            throw new EDBException("Failed to query existing object");
        } else if (resultList.size() > 1) {
            throw new EDBException("Received more than 1 object which should not be possible!");
        }

        return resultList.get(0);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException {
        LOGGER.debug("Loading all commits which involve object " + oid + " from " + from + " to " + to);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        Subquery<JPAObject> subquery = query.subquery(JPAObject.class);
        Root fromJPAObject = subquery.from(JPAObject.class);
        subquery.select(fromJPAObject.get("timestamp"));
        Predicate predicate1 = criteriaBuilder.equal(fromJPAObject.get("oid"), oid);
        Predicate predicate2 = criteriaBuilder.between(fromJPAObject.get("timestamp"), from, to);
        subquery.where(criteriaBuilder.and(predicate1, predicate2));
        select.where(criteriaBuilder.in(f.get("timestamp")).value(subquery));

        select.orderBy(criteriaBuilder.asc(f.get("timestamp")));

        TypedQuery<JPACommit> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Override
    public List<JPAObject> getDeletedJPAObjects() throws EDBException {
        LOGGER.debug("Load all deleted JPAObjects");
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root<JPAObject> f = query.from(JPAObject.class);

        CriteriaQuery<JPAObject> select = query.select(f);

        select.where(criteriaBuilder.equal(f.get("isDeleted"), Boolean.TRUE));

        TypedQuery<JPAObject> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<JPAObject> getJPAObjectVersionsYoungerThanTimestamp(String oid, long timestamp) throws EDBException {
        LOGGER.debug("Load all objects with the given oid " + oid + " which are younger than " + timestamp);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root f = query.from(JPAObject.class);

        CriteriaQuery<JPAObject> select = query.select(f);

        Predicate predicate1 = criteriaBuilder.equal(f.get("oid"), oid);
        Predicate predicate2 = criteriaBuilder.gt(f.get("timestamp"), timestamp);

        select.where(criteriaBuilder.and(predicate1, predicate2));

        TypedQuery<JPAObject> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Override
    public List<JPACommit> getJPACommit(long timestamp) throws EDBException {
        LOGGER.debug("Load the commit for the timestamp " + timestamp);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        select.where(criteriaBuilder.equal(f.get("timestamp"), timestamp));

        TypedQuery<JPACommit> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Override
    public List<JPACommit> getCommits(Map<String, Object> param) throws EDBException {
        LOGGER.debug("Get commits which are given to a param map with " + param.size() + " elements");
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        Predicate[] predicates = analyzeParamMap(criteriaBuilder, f, param);

        select.where(criteriaBuilder.and(predicates));

        TypedQuery<JPACommit> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> param) throws EDBException {
        LOGGER.debug("Get last commit which are given to a param map with " + param.size() + " elements");
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        Predicate[] predicates = analyzeParamMap(criteriaBuilder, f, param);

        select.where(criteriaBuilder.and(predicates));

        select.orderBy(criteriaBuilder.asc(f.get("timestamp")));

        TypedQuery<JPACommit> typedQuery = entityManager.createQuery(select);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException ex) {
            throw new EDBException("there was no Object found with the given query parameters", ex);
        }
    }

    /**
     * Analyzes the map and filters the values which are used for query
     */
    @SuppressWarnings("rawtypes")
    private Predicate[] analyzeParamMap(CriteriaBuilder criteriaBuilder, Root from, Map<String, Object> param) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (Map.Entry<String, Object> q : param.entrySet()) {
            String key = q.getKey();
            Object value = q.getValue();
            if (key.equals("timestamp")) {
                predicates.add(criteriaBuilder.equal(from.get("timestamp"), value));
            } else if (key.equals("committer")) {
                predicates.add(criteriaBuilder.equal(from.get("committer"), value));
            } else if (key.equals("role")) {
                predicates.add(criteriaBuilder.equal(from.get("role"), value));
            }
        }
        Predicate[] temp = new Predicate[predicates.size()];
        for (int i = 0; i < predicates.size(); i++) {
            temp[i] = predicates.get(i);
        }

        return temp;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> query(String key, Object value) throws EDBException {
        LOGGER.debug("Query for objects which have entries where key = " + key + " and value = " + value);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root<JPAObject> f = query.from(JPAObject.class);

        CriteriaQuery<JPAObject> select = query.select(f);

        Subquery subquery = query.subquery(JPAEntry.class);
        Root fromKeyValuePair = subquery.from(JPAEntry.class);
        subquery.select(fromKeyValuePair);

        Join j = f.join("entries");
        Path<Object> path = j.get("id");

        Predicate predicate1 = criteriaBuilder.equal(fromKeyValuePair.get("key"), key);
        Predicate predicate2 = criteriaBuilder.equal(fromKeyValuePair.get("value"), value);
        Predicate predicate3 = criteriaBuilder.in(fromKeyValuePair.get("id")).value(path);
        subquery.where(criteriaBuilder.and(predicate1, predicate2, predicate3));

        select.where(criteriaBuilder.exists(subquery));

        select.orderBy(criteriaBuilder.asc(f.get("timestamp")));

        TypedQuery<JPAObject> typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
