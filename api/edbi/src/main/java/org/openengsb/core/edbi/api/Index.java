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

package org.openengsb.core.edbi.api;

import java.util.Collection;

/**
 * Represents the Index of a Model. It acts as an adapter for the lost typesafety when storing class meta-data. It also
 * contains meta-data of the underlying database schema, that is, the table names that store the data for the given
 * model type.
 * 
 * @param <T> the type of the model
 */
public interface Index<T> {
    /**
     * The index name is essentially the canonical class name of the model type.
     * 
     * @return the name of the index.
     */
    String getName();

    /**
     * Returns the class of the model that is represented by this index. The type information might however not always
     * be available. If the Index was loaded from meta-data, an attempt is made to load the class from the index name.
     * 
     * @return the model type.
     * @throws UnavailableTypeInformationException if the type can not be retrieved
     */
    Class<T> getModelClass() throws UnavailableTypeInformationException;

    /**
     * Returns the name of the table from the underlying data structure that stores the latest version of model
     * instances.
     * 
     * @return an identifier unique to this model index
     */
    String getHeadTableName();

    /**
     * Returns the name of the table from the underlying data structure that stores the change history of model
     * instances.
     * 
     * @return an identifier unique to this model index
     */
    String getHistoryTableName();

    /**
     * Returns the fields of this index.
     * 
     * @return a collection of fields
     */
    Collection<IndexField<?>> getFields();

    /**
     * Allows you to specify a ClassLoader that is used when restoring type information when calling
     * {@link #getModelClass()} on a sparsely loaded Index.
     * 
     * @param classLoader the class loader to use
     */
    void setClassLoader(ClassLoader classLoader);
}
