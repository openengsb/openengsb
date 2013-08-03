/*
 * Copyright 2013 vauve_000.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.openengsb.core.edb.api.EDBBaseObject;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.jpa.internal.JPABaseObject;
import org.openengsb.core.edb.jpa.internal.JPAHead;
import org.openengsb.core.edb.jpa.internal.JPAObject;
import org.openengsb.core.edb.jpa.internal.JPAStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vauve_000
 */
public abstract class AbstractJPADao
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultJPADao.class);
    protected EntityManager entityManager;
	
	private Predicate checkSid(CriteriaBuilder criteriaBuilder, Root from, String sid, Predicate predicate)
	{
		if(sid != null)
		{
			Join join = from.join("stage");
			Predicate predicateSid = criteriaBuilder.equal(join.get("stageId"), sid);
			return predicateSid;
			//return criteriaBuilder.and(predicateSid);
		}
		
		return predicate;
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
	
	/**
     * Converts a parameter map for a query operation into a list of predicates which need to be added to the criteria
     * query.
     */
    private List<Predicate> convertQueryMapIntoPredicateList(Map<String, Object> map, Root<?> from,
            CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (Map.Entry<String, Object> value : map.entrySet()) {
            Join<?, ?> join = from.join("entries");

            Predicate predicate1 = builder.like(join.<String> get("key"), value.getKey());
            Predicate predicate2 = builder.like(join.<String> get("value"), value.getValue().toString());
            predicates.add(builder.and(predicate1, predicate2));
        }
        return predicates;
    }
	
	protected <T> T getJPAObject(Class<T> type, String oid, String sid, long timestamp) throws EDBException {
		synchronized (entityManager) {
            LOGGER.debug("Loading object {} for the time {}", oid, timestamp);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root from = query.from(type);

            query.select(from);

            Predicate predicate1 = criteriaBuilder.equal(from.get("oid"), oid);
            Predicate predicate2 = criteriaBuilder.le(from.get("timestamp"), timestamp);
			
			query.where(checkSid(criteriaBuilder, from, sid, criteriaBuilder.and(predicate1, predicate2)));
            
            query.orderBy(criteriaBuilder.desc(from.get("timestamp")));

            TypedQuery typedQuery = entityManager.createQuery(query).setMaxResults(1);
            List resultList = typedQuery.getResultList();

            if (resultList.size() < 1) {
                throw new EDBException("Failed to query existing object");
            } else if (resultList.size() > 1) {
                throw new EDBException("Received more than 1 object which should not be possible!");
            }

            return type.cast(resultList.get(0));
        }
	}
	
	protected <J extends JPABaseObject, E extends EDBBaseObject> JPAHead getJPAHead(Class<J> type, String sid, long timestamp) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading head for timestamp {}", timestamp);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<J> query = criteriaBuilder.createQuery(type);
            Root<J> from = query.from(type);

            query.select(from);

            Subquery<Number> subquery = query.subquery(Number.class);
            Root maxTime = subquery.from(type);
            subquery.select(criteriaBuilder.max(maxTime.get("timestamp")));
            Predicate subPredicate1 = criteriaBuilder.le(maxTime.get("timestamp"), timestamp);
            Predicate subPredicate2 = criteriaBuilder.equal(maxTime.get("oid"), from.get("oid"));
            subquery.where(checkSid(criteriaBuilder, maxTime, sid, criteriaBuilder.and(subPredicate1, subPredicate2)));

            Predicate predicate1 = criteriaBuilder.equal(from.get("timestamp"), subquery);
            Predicate predicate2 = criteriaBuilder.notEqual(from.get("isDeleted"), Boolean.TRUE);
            query.where(checkSid(criteriaBuilder, from, sid, criteriaBuilder.and(predicate1, predicate2)));

            TypedQuery<J> typedQuery = entityManager.createQuery(query);
            List<J> resultList = typedQuery.getResultList();

            JPAHead<J, E> head = new JPAHead();
            head.setJPAObjects(resultList);
            head.setTimestamp(timestamp);
            return head;
        }
    }
	
    protected <T> List<T> getJPAObjectHistory(Class<T> type, String oid, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading the history for the object {}", oid);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root from = query.from(type);
            query.select(from);
            query.where(checkSid(criteriaBuilder, from, sid, criteriaBuilder.equal(from.get("oid"), oid)));
            query.orderBy(criteriaBuilder.asc(from.get("timestamp")));

            TypedQuery<T> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }
	
	protected <T> List<T> getJPAObjectHistory(Class<T> type, String oid, String sid, long from, long to) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading the history for the object {} from {} to {}", new Object[]{ oid, from, to });
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root f = query.from(type);
            query.select(f);

            Predicate predicate1 = criteriaBuilder.equal(f.get("oid"), oid);
            Predicate predicate2 = criteriaBuilder.between(f.get("timestamp"), from, to);
            query.where(checkSid(criteriaBuilder, f, sid, criteriaBuilder.and(predicate1, predicate2)));
            query.orderBy(criteriaBuilder.asc(f.get("timestamp")));

            TypedQuery<T> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }
	
	protected <T> List<T> getJPAObjects(Class<T> type, List<String> oid, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading newest object {}", oid);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root<T> from = query.from(type);

            query.select(from);

            Subquery<Number> subquery = query.subquery(Number.class);
            Root maxTime = subquery.from(type);
            subquery.select(criteriaBuilder.max(maxTime.get("timestamp")));
            subquery.where(checkSid(criteriaBuilder, maxTime, sid, criteriaBuilder.equal(from.get("oid"), maxTime.get("oid"))));

            Predicate predicate1 = criteriaBuilder.in(from.get("oid")).value(oid);
            Predicate predicate2 = criteriaBuilder.equal(from.get("timestamp"), subquery);

            query.where(checkSid(criteriaBuilder, from, sid, criteriaBuilder.and(predicate1, predicate2)));

            TypedQuery<T> typedQuery = entityManager.createQuery(query);
            List<T> resultList = typedQuery.getResultList();
            return resultList;
        }
    }
	
	protected <C, O> List<C> getJPACommit(Class<C> commitType, Class<O> objectType, String oid, String sid, long from, long to) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading all commits which involve object {} from {} to {}", new Object[]{ oid, from, to });
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<C> query = criteriaBuilder.createQuery(commitType);
            Root<C> f = query.from(commitType);
            query.select(f);

            Subquery<O> subquery = query.subquery(objectType);
            Root fromJPAObject = subquery.from(objectType);
            subquery.select(fromJPAObject.get("timestamp"));
			
            Predicate predicate1 = criteriaBuilder.equal(fromJPAObject.get("oid"), oid);
            Predicate predicate2 = criteriaBuilder.between(fromJPAObject.get("timestamp"), from, to);
            subquery.where(checkSid(criteriaBuilder, fromJPAObject, sid, criteriaBuilder.and(predicate1, predicate2)));

            query.where(checkSid(criteriaBuilder, f, sid, criteriaBuilder.in(f.get("timestamp")).value(subquery)));
            query.orderBy(criteriaBuilder.asc(f.get("timestamp")));

            TypedQuery<C> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }
	
	protected <T> List<String> getResurrectedOIDs(Class<T> type, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("get resurrected JPA objects");

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
            Root from = query.from(type);
            query.select(from.get("oid"));

            Subquery<T> sub = query.subquery(type);
            Root f = sub.from(type);
            sub.select(f);
            Predicate subPredicate1 = criteriaBuilder.equal(from.get("oid"), f.get("oid"));
            Predicate subPredicate2 = criteriaBuilder.equal(f.get("isDeleted"), Boolean.TRUE);
            Predicate subPredicate3 = criteriaBuilder.gt(from.get("timestamp"), f.get("timestamp"));
            sub.where(checkSid(criteriaBuilder, f, sid, criteriaBuilder.and(subPredicate1, subPredicate2, subPredicate3)));

            Predicate predicate1 = checkSid(criteriaBuilder, from, sid, criteriaBuilder.notEqual(from.get("isDeleted"), Boolean.TRUE));
            Predicate predicate2 = criteriaBuilder.exists(sub);
            query.where(predicate1, predicate2);

            TypedQuery<String> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }
	
	protected <T> List<T> getJPACommit(Class<T> type, long timestamp, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Load the commit for the timestamp {}", timestamp);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root<T> from = query.from(type);
            query.select(from);

            Subquery<Number> subquery = query.subquery(Number.class);
            Root maxTime = subquery.from(type);
            subquery.select(criteriaBuilder.max(maxTime.get("timestamp")));
            subquery.where(criteriaBuilder.le(maxTime.get("timestamp"), timestamp));
			
			Predicate stagePred = checkSid(criteriaBuilder, maxTime, sid, null);
			
			if(stagePred != null)
			{
				subquery.where(stagePred);
			}
			
			query.where(criteriaBuilder.equal(from.get("timestamp"), subquery));
			
            TypedQuery<T> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }
	
	protected <T> List<T> getCommits(Class<T> type, Map<String, Object> param, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Get commits which are given to a param map with {} elements", param.size());
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root<T> from = query.from(type);

            query.select(from);
            Predicate[] predicates = analyzeParamMap(criteriaBuilder, from, param);
            query.where(checkSid(criteriaBuilder, from, sid, criteriaBuilder.and(predicates)));

            TypedQuery<T> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }
	
	protected <T> T getLastCommit(Class<T> type, Map<String, Object> param, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Get last commit which are given to a param map with {} elements", param.size());
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root<T> from = query.from(type);

            query.select(from);
            Predicate[] predicates = analyzeParamMap(criteriaBuilder, from, param);
            query.where(checkSid(criteriaBuilder, from, sid, criteriaBuilder.and(predicates)));
            query.orderBy(criteriaBuilder.desc(from.get("timestamp")));

            TypedQuery<T> typedQuery = entityManager.createQuery(query).setMaxResults(1);
            try {
                return typedQuery.getSingleResult();
            } catch (NoResultException ex) {
                throw new EDBException("there was no Object found with the given query parameters", ex);
            }
        }
    }
	
	protected <T> List<T> query(Class<T> type, Map<String, Object> values, String sid) throws EDBException {
        synchronized (entityManager) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            Root<T> from = query.from(type);

            List<Predicate> predicates = convertQueryMapIntoPredicateList(values, from, criteriaBuilder);
			
			Predicate[] ps = predicates.toArray(new Predicate[1]);
			ps[0] = checkSid(criteriaBuilder, from, sid, ps[0]); 
            query.where(ps);
			
            query.orderBy(criteriaBuilder.desc(from.get("timestamp")));
            TypedQuery<T> typedQuery = entityManager.createQuery(query);
            List<T> result = typedQuery.getResultList();
            return result;
        }
    }
	
	protected <T> Integer getVersionOfOid(Class<T> type, String oid, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("loading version of model under the oid {}", oid);

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<T> from = query.from(type);
            Expression<Long> maxExpression = criteriaBuilder.count(from.get("oid"));
            query.select(maxExpression);
            query.where(checkSid(criteriaBuilder, from, sid, criteriaBuilder.equal(from.get("oid"), oid)));

            TypedQuery<Long> typedQuery = entityManager.createQuery(query);
            try {
                return (int) typedQuery.getSingleResult().longValue();
            } catch (NoResultException ex) {
                LOGGER.debug("no model under the oid {}. Returning 0", oid);
                return 0;
            }
        }
    }
	
	protected <T> List<T> query(Class<T> type, Map<String, Object> values, Long timestamp, String sid) throws EDBException {
        synchronized (entityManager) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);

            Root<T> from = criteriaQuery.from(type);
            List<Predicate> predicates = convertQueryMapIntoPredicateList(values, from, criteriaBuilder);
            predicates.add(criteriaBuilder.notEqual(from.get("isDeleted"), Boolean.TRUE));

            Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
            Root subFrom = subquery.from(type);
            Expression<Long> maxExpression = criteriaBuilder.max(subFrom.get("timestamp"));
            subquery.select(maxExpression);
            Predicate p1 = criteriaBuilder.equal(subFrom.get("oid"), from.get("oid"));
            Predicate p2 = criteriaBuilder.le(subFrom.get("timestamp"), timestamp);
            subquery.where(checkSid(criteriaBuilder, subFrom, sid, criteriaBuilder.and(p1, p2)));
			
            predicates.add(checkSid(criteriaBuilder, from, sid, criteriaBuilder.equal(from.get("timestamp"), subquery)));
            criteriaQuery.where(predicates.toArray(new Predicate[0]));

            TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
            return typedQuery.getResultList();
        }
    }
}
