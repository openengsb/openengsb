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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openengsb.core.edbi.api.ClassNameTranslator;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.jdbc.util.Introspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates new JdbcIndex instances for model Classes. It will only map the java aspects of the index, that is it will
 * not map schema names such as tables or column names for fields.
 */
public class IndexBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(IndexBuilder.class);

    public static final String[] EXCLUDED_PROPERTIES = {
        "class",
        "openEngSBModelTail"
    };

    /**
     * The translator is used to generate index names of the Class the Index is created for.
     */
    private ClassNameTranslator indexNameTranslator;

    public IndexBuilder(ClassNameTranslator indexNameTranslator) {
        this.indexNameTranslator = indexNameTranslator;
    }

    /**
     * Builds a JdbcIndex instance for the given model Class.
     * 
     * @param model the model class
     * @param <T> the type of the model class
     * @return a new Index
     */
    public <T> JdbcIndex<T> buildIndex(Class<T> model) {
        JdbcIndex<T> index = new JdbcIndex<>();

        index.setModelClass(model);
        index.setName(indexNameTranslator.translate(model));

        index.setFields(buildFields(index));

        return index;
    }

    @SuppressWarnings("unchecked")
    private Collection<IndexField<?>> buildFields(final JdbcIndex<?> index) {
        Map<String, Class<?>> properties;
        List<IndexField<?>> fields;

        properties = Introspector.getPropertyTypeMap(index.getModelClass(), getExcludedProperties());
        fields = new ArrayList<>(properties.size());

        for (Map.Entry<String, Class<?>> entry : properties.entrySet()) {
            JdbcIndexField field = new JdbcIndexField(index);
            field.setName(entry.getKey());
            field.setType(entry.getValue());

            fields.add(field);
        }

        return fields;
    }

    public String[] getExcludedProperties() {
        return EXCLUDED_PROPERTIES;
    }

    public ClassNameTranslator getIndexNameTranslator() {
        return indexNameTranslator;
    }

    public void setIndexNameTranslator(ClassNameTranslator indexNameTranslator) {
        this.indexNameTranslator = indexNameTranslator;
    }
}
