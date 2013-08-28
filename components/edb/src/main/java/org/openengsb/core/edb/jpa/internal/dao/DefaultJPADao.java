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

package org.openengsb.core.edb.jpa.internal.dao;

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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.jpa.internal.JPACommit;
import org.openengsb.core.edb.jpa.internal.JPAHead;
import org.openengsb.core.edb.jpa.internal.JPAObject;
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public JPAHead getJPAHead(long timestamp) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading head for timestamp {}", timestamp);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
            Root<JPAObject> from = query.from(JPAObject.class);

            query.select(from);

            Subquery<Number> subquery = query.subquery(Number.class);
            Root maxTime = subquery.from(JPAObject.class);
            subquery.select(criteriaBuilder.max(maxTime.get("timestamp")));
            Predicate subPredicate1 = criteriaBuilder.le(maxTime.get("timestamp"), timestamp);
            Predicate subPredicate2 = criteriaBuilder.equal(maxTime.get("oid"), from.get("oid"));
            subquery.where(criteriaBuilder.and(subPredicate1, subPredicate2));

            Predicate predicate1 = criteriaBuilder.equal(from.get("timestamp"), subquery);
            Predicate predicate2 = criteriaBuilder.notEqual(from.get("isDeleted"), Boolean.TRUE);
            query.where(criteriaBuilder.and(predicate1, predicate2));

            TypedQuery<JPAObject> typedQuery = entityManager.createQuery(query);
            List<JPAObject> resultList = typedQuery.getResultList();

            JPAHead head = new JPAHead();
            head.setJPAObjects(resultList);
            head.setTimestamp(timestamp);
            return head;
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading the history for the object {}", oid);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
            Root from = query.from(JPAObject.class);
            query.select(from);
            query.where(criteriaBuilder.equal(from.get("oid"), oid));
            query.orderBy(criteriaBuilder.asc(from.get("timestamp")));

            TypedQuery<JPAObject> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading the history for the object {} from {} to {}", new Object[]{ oid, from, to });
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
            Root f = query.from(JPAObject.class);
            query.select(f);

            Predicate predicate1 = criteriaBuilder.equal(f.get("oid"), oid);
            Predicate predicate2 = criteriaBuilder.between(f.get("timestamp"), from, to);
            query.where(criteriaBuilder.and(predicate1, predicate2));
            query.orderBy(criteriaBuilder.asc(f.get("timestamp")));

            TypedQuery<JPAObject> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JPAObject getJPAObject(String oid, long timestamp) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading object {} for the time {}", oid, timestamp);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
            Root from = query.from(JPAObject.class);

            query.select(from);

            Predicate predicate1 = criteriaBuilder.equal(from.get("oid"), oid);
            Predicate predicate2 = criteriaBuilder.le(from.get("timestamp"), timestamp);
            query.where(criteriaBuilder.and(predicate1, predicate2));
            query.orderBy(criteriaBuilder.desc(from.get("timestamp")));

            TypedQuery<JPAObject> typedQuery = entityManager.createQuery(query).setMaxResults(1);
            List<JPAObject> resultList = typedQuery.getResultList();

            if (resultList.size() < 1) {
                throw new EDBException("Failed to query existing object");
            } else if (resultList.size() > 1) {
                throw new EDBException("Received more than 1 object which should not be possible!");
            }

            return resultList.get(0);
        }
    }

    @Override
    public JPAObject getJPAObject(String oid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading newest object {}", oid);
            return getJPAObject(oid, System.currentTimeMillis());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<JPAObject> getJPAObjects(List<String> oid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading newest object {}", oid);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPAObject> query = criteriaBuilder.createQuery(JPAObject.class);
            Root<JPAObject> from = query.from(JPAObject.class);

            query.select(from);

            Subquery<Number> subquery = query.subquery(Number.class);
            Root maxTime = subquery.from(JPAObject.class);
            subquery.select(criteriaBuilder.max(maxTime.get("timestamp")));
            subquery.where(criteriaBuilder.equal(from.get("oid"), maxTime.get("oid")));

            Predicate predicate1 = criteriaBuilder.in(from.get("oid")).value(oid);
            Predicate predicate2 = criteriaBuilder.equal(from.get("timestamp"), subquery);

            query.where(criteriaBuilder.and(predicate1, predicate2));

            TypedQuery<JPAObject> typedQuery = entityManager.createQuery(query);
            List<JPAObject> resultList = typedQuery.getResultList();
            return resultList;
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading all commits which involve object {} from {} to {}", new Object[]{ oid, from, to });
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
            Root<JPACommit> f = query.from(JPACommit.class);
            query.select(f);

            Subquery<JPAObject> subquery = query.subquery(JPAObject.class);
            Root fromJPAObject = subquery.from(JPAObject.class);
            subquery.select(fromJPAObject.get("timestamp"));
            Predicate predicate1 = criteriaBuilder.equal(fromJPAObject.get("oid"), oid);
            Predicate predicate2 = criteriaBuilder.between(fromJPAObject.get("timestamp"), from, to);
            subquery.where(criteriaBuilder.and(predicate1, predicate2));

            query.where(criteriaBuilder.in(f.get("timestamp")).value(subquery));
            query.orderBy(criteriaBuilder.asc(f.get("timestamp")));

            TypedQuery<JPACommit> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("get resurrected JPA objects");

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
            Root from = query.from(JPAObject.class);
            query.select(from.get("oid"));

            Subquery<JPAObject> sub = query.subquery(JPAObject.class);
            Root f = sub.from(JPAObject.class);
            sub.select(f);
            Predicate subPredicate1 = criteriaBuilder.equal(from.get("oid"), f.get("oid"));
            Predicate subPredicate2 = criteriaBuilder.equal(f.get("isDeleted"), Boolean.TRUE);
            Predicate subPredicate3 = criteriaBuilder.gt(from.get("timestamp"), f.get("timestamp"));
            sub.where(criteriaBuilder.and(subPredicate1, subPredicate2, subPredicate3));

            Predicate predicate1 = criteriaBuilder.notEqual(from.get("isDeleted"), Boolean.TRUE);
            Predicate predicate2 = criteriaBuilder.exists(sub);
            query.where(predicate1, predicate2);

            TypedQuery<String> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<JPACommit> getJPACommit(long timestamp) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Load the commit for the timestamp {}", timestamp);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
            Root<JPACommit> from = query.from(JPACommit.class);
            query.select(from);

            Subquery<Number> subquery = query.subquery(Number.class);
            Root maxTime = subquery.from(JPACommit.class);
            subquery.select(criteriaBuilder.max(maxTime.get("timestamp")));
            subquery.where(criteriaBuilder.le(maxTime.get("timestamp"), timestamp));

            query.where(criteriaBuilder.equal(from.get("timestamp"), subquery));

            TypedQuery<JPACommit> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }

    @Override
    public JPACommit getJPACommit(String revision) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Get commit for the revision {}", revision);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
            Root<JPACommit> from = query.from(JPACommit.class);
            query.select(from).where(criteriaBuilder.equal(from.get("revision"), revision));
            TypedQuery<JPACommit> typedQuery = entityManager.createQuery(query);
            List<JPACommit> result = typedQuery.getResultList();
            switch (result.size()) {
                case 0:
                    throw new EDBException("There is no commit with the given revision " + revision);
                case 1:
                    return result.get(0);
                default:
                    throw new EDBException("More than one commit with the given revision found!");
            }
        }
    }

    @Override
    public List<JPACommit> getCommits(Map<String, Object> param) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Get commits which are given to a param map with {} elements", param.size());
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
            Root<JPACommit> from = query.from(JPACommit.class);

            query.select(from);
            Predicate[] predicates = analyzeParamMap(criteriaBuilder, from, param);
            query.where(criteriaBuilder.and(predicates));

            TypedQuery<JPACommit> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> param) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Get last commit which are given to a param map with {} elements", param.size());
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPACommit> query = criteriaBuilder.createQuery(JPACommit.class);
            Root<JPACommit> from = query.from(JPACommit.class);

            query.select(from);
            Predicate[] predicates = analyzeParamMap(criteriaBuilder, from, param);
            query.where(criteriaBuilder.and(predicates));
            query.orderBy(criteriaBuilder.desc(from.get("timestamp")));

            TypedQuery<JPACommit> typedQuery = entityManager.createQuery(query).setMaxResults(1);
            try {
                return typedQuery.getSingleResult();
            } catch (NoResultException ex) {
                throw new EDBException("there was no Object found with the given query parameters", ex);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Get matching revisions for the request {}", request);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery query = criteriaBuilder.createQuery();
            Root<JPACommit> from = query.from(JPACommit.class);
            query.multiselect(from.get("committer"), from.get("timestamp"), from.get("context"), from.get("comment")
                , from.get("revision"), from.get("parent"), from.get("domainId"), from.get("connectorId")
                , from.get("instanceId"));

            Predicate[] predicates = convertCommitRequestToPredicates(criteriaBuilder, from, request);
            query.where(criteriaBuilder.and(predicates));
            query.orderBy(criteriaBuilder.asc(from.get("timestamp")));
            TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
            List<CommitMetaInfo> infos = new ArrayList<>();
            for (Object[] row : typedQuery.getResultList()) {
                CommitMetaInfo info = new CommitMetaInfo();
                info.setCommitter(row[0] != null ? row[0].toString() : null);
                info.setTimestamp(row[1] != null ? Long.valueOf(row[1].toString()) : null);
                info.setContext(row[2] != null ? row[2].toString() : null);
                info.setComment(row[3] != null ? row[3].toString() : null);
                info.setRevision(row[4] != null ? row[4].toString() : null);
                info.setParent(row[5] != null ? row[5].toString() : null);
                info.setDomainId(row[6] != null ? row[6].toString() : null);
                info.setConnectorId(row[7] != null ? row[7].toString() : null);
                info.setInstanceId(row[8] != null ? row[8].toString() : null);
                infos.add(info);
            }
            return infos;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Predicate[] convertCommitRequestToPredicates(CriteriaBuilder builder, Root from,
            CommitQueryRequest request) {
        List<Predicate> predicates = new ArrayList<>();
        if (request.getCommitter() != null) {
            predicates.add(builder.equal(from.get("committer"), request.getCommitter()));
        }
        if (request.getContext() != null) {
            predicates.add(builder.equal(from.get("context"), request.getContext()));
        }
        predicates.add(builder.between(from.get("timestamp"), request.getStartTimestamp(),
            request.getEndTimestamp()));
        return predicates.toArray(new Predicate[predicates.size()]);
    }

    /**
     * Analyzes the map and filters the values which are used for query
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Predicate[] analyzeParamMap(CriteriaBuilder criteriaBuilder, Root from, Map<String, Object> param) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals("timestamp")) {
                predicates.add(criteriaBuilder.le(from.get("timestamp"), (Long) value));
            } else if (key.equals("committer")) {
                predicates.add(criteriaBuilder.equal(from.get("committer"), value));
            } else if (key.equals("context")) {
                predicates.add(criteriaBuilder.equal(from.get("context"), value));
            }
        }
        Predicate[] temp = new Predicate[predicates.size()];
        for (int i = 0; i < predicates.size(); i++) {
            temp[i] = predicates.get(i);
        }

        return temp;
    }

    @Override
    public Integer getVersionOfOid(String oid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("loading version of model under the oid {}", oid);

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<JPAObject> from = query.from(JPAObject.class);
            Expression<Long> maxExpression = criteriaBuilder.count(from.get("oid"));
            query.select(maxExpression);
            query.where(criteriaBuilder.equal(from.get("oid"), oid));

            TypedQuery<Long> typedQuery = entityManager.createQuery(query);
            try {
                return (int) typedQuery.getSingleResult().longValue();
            } catch (NoResultException ex) {
                LOGGER.debug("no model under the oid {}. Returning 0", oid);
                return 0;
            }
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<JPAObject> query(QueryRequest request) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Perform query with the query object: {}", request);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPAObject> criteriaQuery = criteriaBuilder.createQuery(JPAObject.class);

            Root<JPAObject> from = criteriaQuery.from(JPAObject.class);
            List<Predicate> predicates = convertRequestToPredicateList(request, from, criteriaBuilder);
            predicates.add(criteriaBuilder.notEqual(from.get("isDeleted"), Boolean.TRUE));

            Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
            Root subFrom = subquery.from(JPAObject.class);
            Expression<Long> maxExpression = criteriaBuilder.max(subFrom.get("timestamp"));
            subquery.select(maxExpression);
            Predicate p1 = criteriaBuilder.equal(subFrom.get("oid"), from.get("oid"));
            Predicate p2 = criteriaBuilder.le(subFrom.get("timestamp"), request.getTimestamp());
            subquery.where(criteriaBuilder.and(p1, p2));
            
            predicates.add(criteriaBuilder.equal(from.get("timestamp"), subquery));
            criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            TypedQuery<JPAObject> typedQuery = entityManager.createQuery(criteriaQuery);
            return typedQuery.getResultList();
        }
    }

    /**
     * Converts a query request parameter map for a query operation into a list of predicates which need to be added to
     * the criteria query.
     */
    private List<Predicate> convertRequestToPredicateList(QueryRequest request, Root<?> from,
            CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> value : request.getParameters().entrySet()) {
            Join<?, ?> join = from.join("entries");
            Expression<String> expression = join.<String> get("value");
            String val = value.getValue().toString();
            if (!request.isCaseSensitive()) {
                expression = builder.lower(expression);
                val = val.toLowerCase();
            }
            Predicate predicate1 = builder.like(join.<String> get("key"), value.getKey());
            Predicate predicate2 = request.isWildcardAware()
                    ? builder.like(expression, val) : builder.equal(expression, val);

            predicates.add(builder.and(predicate1, predicate2));
        }
        return predicates;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
