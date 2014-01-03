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

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edb.api.EDBCommit;

/**
 * An IndexEngine maintains Index data of OpenEngSBModel classes and allows to merge data into the index structures.
 */
public interface IndexEngine {

    /**
     * Creates a new Index for the given model type.
     * 
     * @param model the model type
     * @return a new Index instance
     * @throws IndexExistsException if the index for this model already exists
     */
    <T extends OpenEngSBModel> Index<T> createIndex(Class<T> model) throws IndexExistsException;

    /**
     * Checks whether an Index for the given model type exists.
     * 
     * @param model the model type to check for
     * @return true if it exists, false otherwise
     */
    boolean indexExists(Class<? extends OpenEngSBModel> model);

    /**
     * Checks whether an Index with the given name exists.
     * 
     * @param name the name of the index to check for
     * @return true if it exists, false otherwise
     */
    boolean indexExists(String name);

    /**
     * Retrieves the Index for the given model type.
     * 
     * @param model the model type
     * @return the Index instance for this model
     * @throws IndexNotFoundException if an index for the given model can not be found
     */
    <T extends OpenEngSBModel> Index<T> getIndex(Class<T> model) throws IndexNotFoundException;

    /**
     * Retrieves the Index by the given name. When an index is loaded this way, it may not contain typesafe type
     * information (i.e. the Class may not be loadable by the class loader)
     * 
     * @param name the name of the index
     * @return the Index instance of the given name
     * @throws IndexNotFoundException if an index with the given name can not be found
     */
    Index<?> getIndex(String name) throws IndexNotFoundException;

    /**
     * Allows you to specify a ClassLoader that is used when restoring type information on a sparsely loaded Index.
     * 
     * @param classLoader the class loader to use
     */
    void setClassLoader(ClassLoader classLoader);

    /**
     * Merges the given EDBCommit into the underlying data structure.
     * 
     * @param commit the EDBCommit to merge
     * @throws EDBIndexException propagated underlying non-runtime exceptions
     */
    void merge(EDBCommit commit) throws EDBIndexException;
}
