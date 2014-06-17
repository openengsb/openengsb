package org.openengsb.core.ekb.api;


import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.model.QueryRequest;

public class EDBQueryFilter implements QueryFilter {
    protected QueryRequest queryRequest;
    protected List<QueryParser> queryParsers = new ArrayList<QueryParser>();

    public EDBQueryFilter() {
        queryRequest = null;
    }

    public EDBQueryFilter(QueryRequest queryRequest) {
        this.queryRequest = queryRequest;
    }

    public EDBQueryFilter(String queryRequest) {
        this.queryRequest = parseQueryString(queryRequest);
    }

    @Override
    public boolean filter(Object... objects) {
        return true;
    }

    public List<QueryParser> getQueryParsers() {
        return queryParsers;
    }

    public void setQueryParsers(List<QueryParser> queryParsers) {
        this.queryParsers = queryParsers;
    }

    public QueryRequest getQueryRequest() {
        return queryRequest;
    }

    public void setQueryRequest(QueryRequest queryRequest) {
        this.queryRequest = queryRequest;
    }

    public QueryRequest parseQueryString(String query) throws EKBException {
        if (query.isEmpty()) {
            return QueryRequest.create();
        }
        for (QueryParser parser : queryParsers) {
            if (parser.isParsingPossible(query)) {
                return parser.parseQueryString(query);
            }
        }
        throw new EKBException("There is no active parser which is able to parse the query string " + query);
    }
}
