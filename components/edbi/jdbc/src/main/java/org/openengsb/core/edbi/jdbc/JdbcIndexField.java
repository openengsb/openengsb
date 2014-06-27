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

import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.UnavailableTypeInformationException;
import org.openengsb.core.edbi.jdbc.sql.DataType;

/**
 * JdbcIndexField
 */
public class JdbcIndexField<T> implements IndexField<T> {

    private JdbcIndex<?> index;
    private String name;
    private Class<T> type;
    private String typeName;
    private String mappedName;
    private DataType mappedType;
    private ClassLoader classLoader;

    JdbcIndexField() {

    }

    JdbcIndexField(JdbcIndex<?> index) {
        this.index = index;
    }

    @Override
    public JdbcIndex<?> getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getType() throws UnavailableTypeInformationException {
        if (type == null) {
            try {
                ClassLoader cl = (classLoader != null) ? classLoader : getClass().getClassLoader();
                type = (Class<T>) Class.forName(typeName, true, cl);
            } catch (ClassNotFoundException e) {
                throw new UnavailableTypeInformationException("Can't load class " + typeName, e);
            }
        }

        return type;
    }

    @Override
    public String getMappedName() {
        return mappedName;
    }

    @Override
    public DataType getMappedType() {
        return mappedType;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getTypeName() {
        return typeName;
    }

    void setIndex(JdbcIndex<?> index) {
        this.index = index;
    }

    void setName(String name) {
        this.name = name;
    }

    void setType(Class<T> type) {
        this.type = type;
    }

    void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    void setMappedType(DataType mappedType) {
        this.mappedType = mappedType;
    }

    void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
