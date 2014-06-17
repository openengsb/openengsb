package org.openengsb.core.ekb.api;


import java.util.List;

public interface Query {

    /**
     * Select part of the query. Classes that will be checked against
     * QueryFilters.
     * 
     * @return List of Joined Classes
     */
    List<Class<?>> getJoinClasses();

    /**
     * QueryFilter used within the query.
     * 
     * @return QueryFilter
     */
    QueryFilter getFilter();

    /**
     * Result projection from the query execution.
     * 
     * @return List of QueryProjection
     */
    List<QueryProjection> getProjection();
}
