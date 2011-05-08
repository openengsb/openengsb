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

package org.openengsb.core.edb.internal;

import java.util.HashMap;
import java.util.Map;

public class JPAQueryCommitBuilder {
    private String command;
    private StringBuilder builder;
    private Map<String, Object> params;

    public JPAQueryCommitBuilder(String sqlCommand, Map<String, Object> params) {
        this.command = sqlCommand;
        this.builder = new StringBuilder();
        this.params = new HashMap<String, Object>();
        analyzeParams(params);
    }

    private void analyzeParams(Map<String, Object> param) {
        boolean andString = false;
        if (param.size() > 0) {
            for (Map.Entry<String, Object> q : param.entrySet()) {
                String key = q.getKey();
                Object value = q.getValue();
                String connector;
                if (andString) {
                    connector = " and ";
                } else {
                    connector = " where ";
                }

                if (key.equals("timestamp") && !params.containsKey("timestamp") && value instanceof Long) {
                    builder.append(connector);
                    builder.append(" c.timestamp = :timestamp");
                    params.put("timestamp", value);
                } else if (key.equals("committer") && !params.containsKey("committer") && value instanceof String) {
                    builder.append(connector);
                    builder.append(" c.committer = :committer");
                    params.put("committer", value);
                } else if (key.equals("role") && !params.containsKey("role") && value instanceof String) {
                    builder.append(connector);
                    builder.append(" c.role = :role");
                    params.put("role", value);
                }
                andString = true;
            }
        }
    }

    public String getSQLCommand() {
        return command + builder.toString();
    }

    public String getWhereClause() {
        return builder.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }

}
