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
package org.openengsb.core.edbi.jdbc;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.jdbc.JdbcIndex;
import org.openengsb.core.edbi.jdbc.util.Introspector;
import org.openengsb.core.edbi.jdbc.util.ModelUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Map;

/**
* IndexRecord
*/
public class IndexRecord extends MapSqlParameterSource implements SqlParameterSource {

    private JdbcIndex<?> index;

    public IndexRecord(JdbcIndex<?> index) {
        super();
        this.index = index;
    }

    public IndexRecord(JdbcIndex<?> index, OpenEngSBModel model) {
        this(index);
        addValues(model);
    }

    public void addValues(OpenEngSBModel model) {
        Map<String, OpenEngSBModelEntry> entries = ModelUtils.toEntryMap(model);

        for (IndexField<?> indexField : index.getFields()) {
            String paramName = indexField.getMappedName();
            Object value = extractValue(indexField, entries.get(indexField.getName()));

            addValue(paramName, value);
        }
    }

    protected Object extractValue(IndexField<?> field, OpenEngSBModelEntry entry) {
        Object value = entry.getValue();

        if (Introspector.isModel(value)) {
            return ((OpenEngSBModel) value).retrieveInternalModelId();
        }

        // TODO: value mapping

        return value;
    }

    @Override
    public boolean hasValue(String paramName) {
        return true;
    }

    @Override
    public Object getValue(String paramName) {
        if (super.hasValue(paramName)) {
            return super.getValue(paramName);
        } else {
            return null;
        }
    }

}
