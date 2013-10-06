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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.jpa.internal.JPACommit;
import org.openengsb.core.edb.jpa.internal.JPAEntry;
import org.openengsb.core.edb.jpa.internal.JPAHead;
import org.openengsb.core.edb.jpa.internal.JPAObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

public class DefaultJPADao extends AbstractJPADao implements JPADao {
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
        return super.getJPAHead(null, timestamp);
    }
	
	@Override
	public JPAHead getJPAHead(long timestamp, String sid) throws EDBException
	{
		return super.getJPAHead(sid, timestamp);
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid) throws EDBException {
        return super.getJPAObjectHistory(oid, null);
    }
	
	@Override
	public List<JPAObject> getJPAObjectHistory(String oid, String sid) throws EDBException
	{
		return super.getJPAObjectHistory(oid, sid);
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException {
        return super.getJPAObjectHistory(oid, null, from, to);
    }
	
	@Override
	public List<JPAObject> getJPAObjectHistory(String oid, String sid, long from, long to) throws EDBException
	{
		return super.getJPAObjectHistory(oid, sid, from, to);
	}
	
	

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JPAObject getJPAObject(String oid, long timestamp) throws EDBException {
        return this.getJPAObject(oid, null, timestamp);
    }
	
	@Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JPAObject getJPAObject(String oid, String sid, long timestamp) throws EDBException {
        return super.getJPAObject(oid, sid, timestamp);
    }

    @Override
    public JPAObject getJPAObject(String oid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading newest object {}", oid);
            return this.getJPAObject(oid, System.currentTimeMillis());
        }
    }
	
	@Override
    public JPAObject getJPAObject(String oid, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading newest object {} from stage {}", oid, sid);
            return this.getJPAObject(oid, sid, System.currentTimeMillis());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<JPAObject> getJPAObjects(List<String> oids) throws EDBException {
        return super.getJPAObjects(oids, null);
    }
	
	@Override
	public List<JPAObject> getJPAObjects(List<String> oids, String sid) throws EDBException
	{
		return super.getJPAObjects(oids, sid);
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException {
        return super.getJPACommit(oid, null, from, to);
    }
	
	@Override
	public List<JPACommit> getJPACommit(String oid, String sid, long from, long to) throws EDBException
	{
		return super.getJPACommit(oid, sid, from, to);
	}
	
	

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        return super.getResurrectedOIDs(null);
    }
	
	@Override
	public List<String> getResurrectedOIDs(String sid) throws EDBException
	{
		return super.getResurrectedOIDs(sid);
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<JPACommit> getJPACommit(long timestamp) throws EDBException {
        //return (List<JPACommit>)(List<?>)super.getJPACommit(JPABaseCommit.class, timestamp, null);
		return super.getJPACommit(timestamp, null);
    }
	
	@Override
	public List<JPACommit> getJPACommit(long timestamp, String sid) throws EDBException
	{
		return super.getJPACommit(timestamp, sid);
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
        return super.getCommits(param, null);
    }
	
	@Override
	public List<JPACommit> getCommits(Map<String, Object> param, String sid) throws EDBException
	{
		return super.getCommits(param, sid);
	}

    @Override
    public JPACommit getLastCommit(Map<String, Object> param) throws EDBException {
        return super.getLastCommit(param, null);
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
        return Iterables.toArray(predicates, Predicate.class);
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
	public JPACommit getLastCommit(Map<String, Object> param, String sid) throws EDBException
	{
		return super.getLastCommit(param, sid);
	}



    @Override
    public Integer getVersionOfOid(String oid) throws EDBException {
        return super.getVersionOfOid(oid, null);
    }
	
	@Override
	public Integer getVersionOfOid(String oid, String sid) throws EDBException
	{
		return super.getVersionOfOid(oid, sid);
	}

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<JPAObject> query(QueryRequest request) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Perform query with the query object: {}", request);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<JPAObject> criteriaQuery = criteriaBuilder.createQuery(JPAObject.class);
            criteriaQuery.distinct(!request.isAndJoined());
            Root<JPAObject> from = criteriaQuery.from(JPAObject.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.notEqual(from.get("isDeleted"), Boolean.TRUE));

            Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
            Root subFrom = subquery.from(JPAObject.class);
            Expression<Long> maxExpression = criteriaBuilder.max(subFrom.get("timestamp"));
            subquery.select(maxExpression);
            Predicate p1 = criteriaBuilder.equal(subFrom.get("oid"), from.get("oid"));
            Predicate p2 = criteriaBuilder.le(subFrom.get("timestamp"), request.getTimestamp());
            subquery.where(criteriaBuilder.and(p1, p2));
            
            predicates.add(criteriaBuilder.equal(from.get("timestamp"), subquery));
            predicates.add(convertParametersToPredicateNew(request, from, criteriaBuilder, criteriaQuery));
            criteriaQuery.where(Iterables.toArray(predicates, Predicate.class));

            TypedQuery<JPAObject> typedQuery = entityManager.createQuery(criteriaQuery);
            return typedQuery.getResultList();
        }
    }
    
    /**
     * Converts a query request parameter map for a query operation into a list of predicates which need to be added to
     * the criteria query.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Predicate convertParametersToPredicateNew(QueryRequest request, Root<?> from,
            CriteriaBuilder builder, CriteriaQuery<?> query) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> value : request.getParameters().entrySet()) {
            Subquery<JPAEntry> subquery = query.subquery(JPAEntry.class);
            Root subFrom = subquery.from(JPAEntry.class);
            subquery.select(subFrom);
            Expression<String> expression = subFrom.get("value");
            String val = value.getValue().toString();
            if (!request.isCaseSensitive()) {
                expression = builder.lower(expression);
                val = val.toLowerCase();
            }
            
            Predicate predicate1 = builder.equal(from, subFrom.get("owner"));
            Predicate predicate2 = builder.like(subFrom.get("key"), value.getKey());
            Predicate predicate3 = request.isWildcardAware()
                    ? builder.like(expression, val) : builder.equal(expression, val);
            subquery.where(builder.and(predicate1, predicate2, predicate3));
                    
            predicates.add(builder.exists(subquery));
        }
        if (request.isAndJoined()) {
            return builder.and(Iterables.toArray(predicates, Predicate.class));
        } else {
            return builder.or(Iterables.toArray(predicates, Predicate.class));
        }
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}