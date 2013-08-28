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

package org.openengsb.core.api.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * The QueryRequest object encapsulates a query request for data against the Engineering Database. It contains
 * parameters which represent properties of models and allows to define meta data for the query. This meta data is:
 * 
 * timestamp = The timestamp defines for which point in time the query is performed (only the objects which were active
 * at the given point in time and which are fitting the parameters are returned). The default value is
 * System.currentTimeMillis().
 * 
 * wildcardAware = Defines if the values of the parameters are aware of wildcards. Wildcards are % for a generic
 * sequence of characters and _ for exactly one unknown character. The default value is true.
 * 
 * caseSensitive = Defines if the values of the parameters are case sensitive in the query. The default value is true.
 */
public final class QueryRequest {
    private Map<String, Object> parameters;
    private long timestamp;
    private boolean wildcardAware;
    private boolean caseSensitive;

    private QueryRequest() {
        parameters = new HashMap<String, Object>();
        timestamp = System.currentTimeMillis();
        wildcardAware = true;
        caseSensitive = true;
    }

    /**
     * Creates a new QueryRequest object with no parameters.
     */
    public static QueryRequest create() {
        return new QueryRequest();
    }

    /**
     * Creates a new QueryRequest object and adds the given first parameter.
     */
    public static QueryRequest query(String key, Object value) {
        return QueryRequest.create().addParameter(key, value);
    }

    /**
     * Adds a parameter to this request.
     */
    public QueryRequest addParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    /**
     * Removes a parameter from this request.
     */
    public QueryRequest removeParameter(String key) {
        parameters.remove(key);
        return this;
    }

    /**
     * Returns the value for the parameter with the given key in the request.
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * Returns the map of parameters for this request.
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Returns the timestamp which was choosen for this request.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp for which this request should be performed.
     */
    public QueryRequest setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Returns true if this request is wildcard aware and false if it is not.
     */
    public boolean isWildcardAware() {
        return wildcardAware;
    }

    /**
     * Sets this request wildcard aware.
     */
    public QueryRequest wildcardAware() {
        this.wildcardAware = true;
        return this;
    }

    /**
     * Sets this request wildcard unaware.
     */
    public QueryRequest wildcardUnaware() {
        this.wildcardAware = false;
        return this;
    }

    /**
     * Returns true if this request is case sensitive and false if it is not.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Sets this request case sensitive.
     */
    public QueryRequest caseSensitive() {
        this.caseSensitive = true;
        return this;
    }

    /**
     * Sets this request case insensitive.
     */
    public QueryRequest caseInsensitive() {
        this.caseSensitive = false;
        return this;
    }
    
    @Override
    public String toString() {
        ToStringHelper helper = Objects.toStringHelper(getClass()).add("timestamp", timestamp);
        helper.addValue(wildcardAware ? "wildcard aware" : "wildcard unaware");
        helper.addValue(caseSensitive ? "case sensitive" : "case insensitive");
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            helper.add(entry.getKey(), entry.getValue());
        }
        return helper.toString();
    }
}
