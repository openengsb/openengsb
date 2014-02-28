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

import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexFieldVisitor;
import org.openengsb.core.edbi.api.UnavailableTypeInformationException;

/**
 * Index implementation for the JDBC EBDI container.
 * 
 * @param <T> model type parameter
 */
public class JdbcIndex<T> implements Index<T> {

    private Class<T> modelClass;
    private String name;
    private String headTableName;
    private String historyTableName;
    private Collection<IndexField<?>> fields;

    private ClassLoader classLoader;

    JdbcIndex() {

    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getModelClass() throws UnavailableTypeInformationException {
        if (modelClass == null) {
            try {
                ClassLoader cl = (classLoader != null) ? classLoader : getClass().getClassLoader();
                modelClass = (Class<T>) Class.forName(name, true, cl);
            } catch (ClassNotFoundException e) {
                throw new UnavailableTypeInformationException("Can't load class " + name);
            }
        }

        return modelClass;
    }

    @Override
    public String getHeadTableName() {
        return headTableName;
    }

    @Override
    public String getHistoryTableName() {
        return historyTableName;
    }

    @Override
    public Collection<IndexField<?>> getFields() {
        return fields;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    boolean hasTypeInformation() {
        return modelClass != null;
    }

    void setModelClass(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    void setName(String name) {
        this.name = name;
    }

    void setHeadTableName(String headTableName) {
        this.headTableName = headTableName;
    }

    void setHistoryTableName(String historyTableName) {
        this.historyTableName = historyTableName;
    }

    void setFields(Collection<? extends IndexField<?>> fields) {
        this.fields = new ArrayList<>(fields.size());
        this.fields.addAll(fields);
    }

    void accept(IndexFieldVisitor visitor) {
        for (IndexField<?> field : getFields()) {
            visitor.visit(field);
        }
    }
}
