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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.openengsb.core.edb.exceptions.EDBException;

public class JPAQueryBuilder {

    private StringBuilder builder;
    private List<Object> param;
    private int paramId;

    public JPAQueryBuilder(Map<String, Object> query) throws EDBException {
        paramId = 0;
        builder = new StringBuilder();
        param = new ArrayList<Object>();
        analyzeQuery(query);
    }

    @SuppressWarnings("unchecked")
    private void analyzeQuery(Map<String, Object> query) throws EDBException {
        for (Entry<String, Object> entry : query.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals("$and") || key.equals("$or")) {
                Map<String, Object> subQuery = (Map<String, Object>) value;
                handleSubQuery(subQuery, key);
            } else {
                final boolean brackets = builder.length() != 0;
                if (brackets) {
                    builder.append(" and ");
                }
                builder.append("(exists (select v from o.values v where v.key = :param" + paramId + " and v.value ");
                param.add(key);
                paramId++;
                if (value instanceof Pattern) {
                    builder.append(" REGEX :param" + paramId);
                } else {
                    builder.append(" = :param" + paramId);
                }
                builder.append(")) ");
                param.add(value);
                paramId++;
            }
        }
    }

    private void handleSubQuery(Map<String, Object> subQuery, String key) throws EDBException {
        if (subQuery == null) {
            throw new EDBException("Invalid $and query: value is not a sub-query.");
        }
        final boolean brackets = builder.length() != 0;
        if (brackets && key.equals("$and")) {
            builder.append(" and (");
        } else if (brackets && key.equals("$or")) {
            builder.append(" or (");
        }
        analyzeQuery(subQuery);
        if (brackets) {
            builder.append(")");
        }
    }

    public List<Object> getParams() {
        return param;
    }

    public String getQuery() {
        return builder.toString();
    }
}
