package org.openengsb.core.edb.jpa.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.jpa.internal.JPAEntry;
import org.openengsb.core.edb.jpa.internal.JPAObject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Converts a QueryRequest into a CriteriaQuery.
 * 
 */
public class QueryRequestCriteriaBuilder {

    private final CriteriaBuilder builder;
    private final QueryRequest request;

    public QueryRequestCriteriaBuilder(QueryRequest request, CriteriaBuilder builder) {
        this.builder = builder;
        this.request = request;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CriteriaQuery<JPAObject> buildQuery() {
        CriteriaQuery<JPAObject> criteriaQuery = builder.createQuery(JPAObject.class);
        criteriaQuery.distinct(!request.isAndJoined());
        Root from = criteriaQuery.from(JPAObject.class);

        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
        Root subFrom = subquery.from(JPAObject.class);
        Expression<Long> maxExpression = builder.max(subFrom.get("timestamp"));
        subquery.select(maxExpression);
        Predicate p1 = builder.equal(subFrom.get("oid"), from.get("oid"));
        Predicate p2 = builder.le(subFrom.get("timestamp"), request.getTimestamp());
        subquery.where(builder.and(p1, p2));

        List<Predicate> predicates = new ArrayList<>();
        if (request.getContextId() != null) {
            predicates.add(builder.like(from.get("oid"), request.getContextId() + "/%"));
        }
        predicates.add(builder.notEqual(from.get("isDeleted"), !request.isDeleted()));
        predicates.add(builder.equal(from.get("timestamp"), subquery));
        if (request.getModelClassName() != null) {
            Subquery<JPAEntry> subquery2 =
                buildJPAEntrySubquery(EDBConstants.MODEL_TYPE, request.getModelClassName(), from, criteriaQuery);
            predicates.add(builder.exists(subquery2));
        }
        predicates.add(convertParametersToPredicate(from, criteriaQuery));
        criteriaQuery.where(Iterables.toArray(predicates, Predicate.class));
        return criteriaQuery;
    }

    /**
     * Converts a query request parameter map for a query operation into a list of predicates which need to be added to
     * the criteria query.
     */
    @SuppressWarnings({ "unchecked" })
    private Predicate convertParametersToPredicate(Root<?> from, CriteriaQuery<?> query) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Set<Object>> value : request.getParameters().entrySet()) {
            Subquery<JPAEntry> subquery = buildJPAEntrySubquery(value.getKey(), value.getValue(), from, query);
            predicates.add(builder.exists(subquery));
        }
        if (request.isAndJoined()) {
            return builder.and(Iterables.toArray(predicates, Predicate.class));
        } else {
            return builder.or(Iterables.toArray(predicates, Predicate.class));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Subquery buildJPAEntrySubquery(String key, Object value, Root<?> from, CriteriaQuery<?> query) {
        Subquery<JPAEntry> subquery = query.subquery(JPAEntry.class);
        Root subFrom = subquery.from(JPAEntry.class);
        subquery.select(subFrom);
        Predicate ownerPredicate = builder.equal(from, subFrom.get("owner"));
        Predicate keyPredicate = builder.like(subFrom.get("key"), key);
        Predicate valuePredicate =
            value instanceof Set ? buildValuePredicate((Set<Object>) value, subFrom) : builder.equal(
                subFrom.get("value"),
                value);
        subquery.where(builder.and(ownerPredicate, keyPredicate, valuePredicate));
        return subquery;
    }

    private Predicate buildValuePredicate(Set<Object> values, Root<?> subFrom) {
        Expression<String> expression = subFrom.get("value");

        if (!request.isCaseSensitive()) {
            expression = builder.lower(expression);
        }

        List<Predicate> valuePredicates = Lists.newArrayList();
        for (Object obj : values) {
            String caseCheckedValue = getCaseCheckedValue(obj, request.isCaseSensitive());
            valuePredicates.add(request.isWildcardAware() ? builder.like(expression, caseCheckedValue) : builder
                .equal(expression, caseCheckedValue));
        }
        if (request.isAndJoined()) {
            return builder.and(Iterables.toArray(valuePredicates, Predicate.class));
        } else {
            return builder.or(Iterables.toArray(valuePredicates, Predicate.class));
        }
    }

    private String getCaseCheckedValue(Object object, boolean caseSensitive) {
        return caseSensitive ? object.toString() : object.toString().toLowerCase();
    }
}
