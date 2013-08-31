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

package org.openengsb.core.ekb.persistence.query.edb.internal;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.QueryParser;

/**
 * The default QueryParser implementation of the OpenEngSB project. It allows and-joined and or-joined queries in the
 * format: 
 * 
 * key:"value" and key:"value" and ... 
 * key:"value" or key:"value" or ...
 */
public class DefaultQueryParser implements QueryParser {
    private static final Pattern AND_JOINED_STRING_QUERY_PATTERN = Pattern
        .compile("(\\w+\\:\\\"[^\\\"]*\\\"(\\s(and)\\s\\w+\\:\\\"[^\\\"]*\\\")*)?");
    private static final Pattern OR_JOINED_STRING_QUERY_PATTERN = Pattern
        .compile("(\\w+\\:\\\"[^\\\"]*\\\"(\\s(or)\\s\\w+\\:\\\"[^\\\"]*\\\")*)?");

    @Override
    public boolean isParsingPossible(String query) {
        return matchesAndJoinedQuery(query) || matchesOrJoinedQuery(query);
    }

    @Override
    public QueryRequest parseQueryString(String query) throws EKBException {
        if (query.isEmpty()) {
            return QueryRequest.create();
        } else if (matchesAndJoinedQuery(query)) {
            return createAndJoinedQueryRequest(query);
        } else if (matchesOrJoinedQuery(query)) {
            return createOrJoinedQueryRequest(query);
        } else {
            throw new EKBException(
                String.format("The given query %s matches neither and joined nor or joined format", query));
        }
    }

    /**
     * Creates an and-joined query request object out of the query string.
     */
    private QueryRequest createAndJoinedQueryRequest(String query) {
        return createQueryRequest(query.split(" and ")).andJoined();
    }

    /**
     * Creates an or-joined query request object out of the query string.
     */
    private QueryRequest createOrJoinedQueryRequest(String query) {
        return createQueryRequest(query.split(" or ")).orJoined();
    }

    /**
     * Parses the previously split query string into a query request and fills the parameters of the object
     * with the data.
     */
    private QueryRequest createQueryRequest(String[] elements) {
        QueryRequest request = QueryRequest.create();
        for (String element : elements) {
            String[] parts = StringUtils.split(element, ":", 2);
            parts[0] = parts[0].replace("\\", "\\\\");
            parts[1] = parts[1].replace("\\", "\\\\");
            request.addParameter(parts[0], parts[1].substring(1, parts[1].length() - 1));
        }
        return request;
    }

    /**
     * Returns true if the given query string matches the and-joined format and false otherwise.
     */
    private boolean matchesAndJoinedQuery(String query) {
        return AND_JOINED_STRING_QUERY_PATTERN.matcher(query).matches();
    }

    /**
     * Returns true if the given query string matches the or-joined format and false otherwise.
     */
    private boolean matchesOrJoinedQuery(String query) {
        return OR_JOINED_STRING_QUERY_PATTERN.matcher(query).matches();
    }
}
