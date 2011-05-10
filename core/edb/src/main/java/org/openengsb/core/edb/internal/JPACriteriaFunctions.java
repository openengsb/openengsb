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
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.openengsb.core.api.edb.EDBException;

/**
 * A support class where all calls to the database via jpa criteria are done
 */
public class JPACriteriaFunctions {

    private EntityManager em;

    public JPACriteriaFunctions(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Number getMostActualJPAHeadNumber() throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Number> query = criteriaBuilder.createQuery(Number.class);
        Root from = query.from(JPAHead.class);
        Expression<Number> maxExpression = criteriaBuilder.max(from.get("timestamp"));
        CriteriaQuery<Number> select = query.select(maxExpression);

        TypedQuery<Number> typedQuery = em.createQuery(select);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException ex) {
            throw new EDBException("there was no commit so far", ex);
        }
    }

    public JPAHead getJPAHead(long timestamp) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPAHead> query = criteriaBuilder.createQuery(JPAHead.class);
        Root<JPAHead> from = query.from(JPAHead.class);

        CriteriaQuery<JPAHead> select = query.select(from);

        Predicate predicate = criteriaBuilder.equal(from.get("timestamp"), timestamp);
        query.where(predicate);

        TypedQuery<JPAHead> typedQuery = em.createQuery(select);
        List<JPAHead> resultList = typedQuery.getResultList();

        if (resultList == null || resultList.size() != 1 || resultList.get(0) == null) {
            throw new EDBException("Head not found for timestamp " + timestamp);
        }

        return resultList.get(0);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Number getMostActualJPAObjectNumber(String uid) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Number> query = criteriaBuilder.createQuery(Number.class);
        Root from = query.from(JPAObject.class);

        Predicate predicate = criteriaBuilder.equal(from.get("uid"), uid);
        query.where(predicate);

        Expression<Number> maxExpression = criteriaBuilder.max(from.get("timestamp"));
        CriteriaQuery<Number> select = query.select(maxExpression);

        TypedQuery<Number> typedQuery = em.createQuery(select);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException e) {
            throw new EDBException("the given uid was never saved in the database", e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String uid) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root from = query.from(JPAObject.class);

        Predicate predicate = criteriaBuilder.equal(from.get("uid"), uid);
        query.where(predicate);

        CriteriaQuery<JPAObject> select = query.select(from);

        TypedQuery<JPAObject> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String uid, long from, long to) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root f = query.from(JPAObject.class);

        Predicate predicate1 = criteriaBuilder.equal(f.get("uid"), uid);
        Predicate predicate2 = criteriaBuilder.between(f.get("timestamp"), from, to);
        query.where(criteriaBuilder.and(predicate1, predicate2));

        CriteriaQuery<JPAObject> select = query.select(f);

        select.orderBy(criteriaBuilder.asc(f.get("timestamp")));

        TypedQuery<JPAObject> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    public JPAObject getJPAObject(String uid, long timestamp) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root<JPAObject> from = query.from(JPAObject.class);

        CriteriaQuery<JPAObject> select = query.select(from);

        Predicate predicate1 = criteriaBuilder.equal(from.get("timestamp"), timestamp);
        Predicate predicate2 = criteriaBuilder.equal(from.get("uid"), uid);
        query.where(criteriaBuilder.and(predicate1, predicate2));

        TypedQuery<JPAObject> typedQuery = em.createQuery(select);
        List<JPAObject> resultList = typedQuery.getResultList();

        if (resultList.size() < 1) {
            throw new EDBException("Failed to query existing object");
        } else if (resultList.size() > 1) {
            throw new EDBException("Received more than 1 object which should not be possible!");
        }

        return resultList.get(0);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPACommit> getJPACommit(String uid, long from, long to) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        Subquery<JPAObject> subquery = query.subquery(JPAObject.class);
        Root fromJPAObject = subquery.from(JPAObject.class);
        subquery.select(fromJPAObject.get("timestamp"));
        Predicate predicate1 = criteriaBuilder.equal(fromJPAObject.get("uid"), uid);
        Predicate predicate2 = criteriaBuilder.between(fromJPAObject.get("timestamp"), from, to);
        subquery.where(criteriaBuilder.and(predicate1, predicate2));
        select.where(criteriaBuilder.in(f.get("timestamp")).value(subquery));

        select.orderBy(criteriaBuilder.asc(f.get("timestamp")));

        TypedQuery<JPACommit> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    public List<JPAObject> getDeletedJPAObjects() throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root<JPAObject> f = query.from(JPAObject.class);

        CriteriaQuery<JPAObject> select = query.select(f);

        select.where(criteriaBuilder.equal(f.get("isDeleted"), Boolean.TRUE));

        TypedQuery<JPAObject> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<JPAObject> getJPAObjectVersionsYoungerThanTimestamp(String uid, long timestamp) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
        Root f = query.from(JPAObject.class);

        CriteriaQuery<JPAObject> select = query.select(f);

        Predicate predicate1 = criteriaBuilder.equal(f.get("uid"), uid);
        Predicate predicate2 = criteriaBuilder.gt(f.get("timestamp"), timestamp);

        select.where(criteriaBuilder.and(predicate1, predicate2));

        TypedQuery<JPAObject> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    public List<JPACommit> getJPACommit(long from) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        select.where(criteriaBuilder.equal(f.get("timestamp"), from));

        TypedQuery<JPACommit> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    public List<JPACommit> getCommits(Map<String, Object> param) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        Predicate[] predicates = analyzeParamMap(criteriaBuilder, f, param);

        select.where(criteriaBuilder.and(predicates));

        TypedQuery<JPACommit> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    public JPACommit getLastCommit(Map<String, Object> param) throws EDBException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
        Root<JPACommit> f = query.from(JPACommit.class);

        CriteriaQuery<JPACommit> select = query.select(f);

        Predicate[] predicates = analyzeParamMap(criteriaBuilder, f, param);

        select.where(criteriaBuilder.and(predicates));

        select.orderBy(criteriaBuilder.asc(f.get("timestamp")));

        TypedQuery<JPACommit> typedQuery = em.createQuery(select);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException ex) {
            throw new EDBException("there was no Object found with the given query parameters", ex);
        }
    }

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
}
