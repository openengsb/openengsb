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

// DOCU: 
public final class QueryRequest {
    private Map<String, Object> parameters;
    private long timestamp;

    private QueryRequest() {
        parameters = new HashMap<String, Object>();
        timestamp = System.currentTimeMillis();
    }
    
    public static QueryRequest query() {
        return new QueryRequest();
    }
    
    public static QueryRequest query(String key, Object value) {
        return new QueryRequest().addParameter(key, value);
    }
    
    public QueryRequest addParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public QueryRequest setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public QueryRequest setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}
