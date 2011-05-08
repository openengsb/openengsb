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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class JPAQueryBuilderNew {

    private EntityManager em;
    private Set<JPAObject> result;

    public JPAQueryBuilderNew(EntityManager em, Map<String, Object> query) {
        this.em = em;
        result = new HashSet<JPAObject>();
        analyzeQuery(query);
    }

    @SuppressWarnings("unchecked")
    private void analyzeQuery(Map<String, Object> query) {
        for (Entry<String, Object> entry : query.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            StringBuilder builder = new StringBuilder();
            builder.append("select o from JPAObject o where exists ");
            builder.append("(select v from o.values v where v.key = :key and v.value ");
            // if (value instanceof Pattern) {
            // builder.append(" REGEXP [:value]");
            // } else {
            builder.append(" = :value");
            // }
            builder.append(")");

            Query q = em.createQuery(builder.toString());
            q.setParameter("key", key);
            q.setParameter("value", value);
            List<JPAObject> temp = q.getResultList();
            if (temp.size() == 0) {
                result = new HashSet<JPAObject>();
                break;
            }
            if (result.size() == 0) {
                result.addAll(temp);
            } else {
                result.retainAll(temp);
            }

            // if after all actions the result size ever get zero, we have the situation
            // that not all JPAObjects have all data in common -> empty result
            if (result.size() == 0) {
                return;
            }
        }
    }

    public List<JPAObject> getResults() {
        return new ArrayList<JPAObject>(result);
    }
}
