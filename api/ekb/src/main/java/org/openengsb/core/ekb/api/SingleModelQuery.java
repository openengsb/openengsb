package org.openengsb.core.ekb.api;


import java.util.ArrayList;
import java.util.List;

/**
 * Replacement for the old (prior than June 2014) EKBInterface Querying method.
 * Only worked with one model (class), with various ways of querying to retrieve
 * result from this model.
 * 
 */
public class SingleModelQuery implements Query {
    Class<?> model;
    QueryFilter queryFilter;
    QueryProjection queryProjection;

    public SingleModelQuery(Class<?> model) {
        this.model = model;
        this.queryProjection = new QueryProjection(model);
        this.queryFilter = null;
    }

    public SingleModelQuery(Class<?> model, QueryFilter queryFilter, QueryProjection queryProjection) {
        this.model = model;
        this.queryFilter = queryFilter;
        if (queryProjection != null) {
            this.queryProjection = queryProjection;
        } else {
            this.queryProjection = new QueryProjection(model);
        }
    }

    @Override
    public List<Class<?>> getJoinClasses() {
        List<Class<?>> modelList = new ArrayList<Class<?>>();
        modelList.add(model);

        return modelList;
    }

    @Override
    public QueryFilter getFilter() {
        return queryFilter;
    }

    @Override
    public List<QueryProjection> getProjection() {

        List<QueryProjection> projectionList = new ArrayList<QueryProjection>();
        projectionList.add(queryProjection);

        return projectionList;
    }

}
