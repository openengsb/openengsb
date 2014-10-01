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

import java.util.List;

/**
 * An IndexEngine maintains Index data of model classes and allows to merge data into the index structures.
 */
public interface IndexEngine {

    /**
     * Creates a new Index for the given model type.
     * 
     * @param model the model type
     * @return a new Index instance
     * @throws IndexExistsException if the index for this model already exists
     */
    <T> Index<T> createIndex(Class<T> model) throws IndexExistsException;

    /**
     * Checks whether an Index for the given model type exists.
     * 
     * @param model the model type to check for
     * @return true if it exists, false otherwise
     */
    boolean indexExists(Class<?> model);

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
    <T> Index<T> getIndex(Class<T> model) throws IndexNotFoundException;

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
     * Retrieves all Index records. When an index is loaded this way, it may not contain typesafe type information (i.e.
     * the Class may not be loadable by the class loader).
     * 
     * @return a list of all Index instances currently managed
     */
    List<Index<?>> getAll();

    /**
     * Removes the given index and all related schema objects.
     * 
     * @param index the index to delete
     * @throws EDBIndexException if an error occurred while attempting to delete the given index
     */
    void removeIndex(Index<?> index) throws EDBIndexException;

    /**
     * Allows you to specify a ClassLoader that is used when restoring type information on a sparsely loaded Index.
     * 
     * @param classLoader the class loader to use
     */
    void setClassLoader(ClassLoader classLoader);

    /**
     * Merges the given IndexCommit into the underlying data structure.
     * 
     * @param commit the IndexCommit to merge
     * @throws EDBIndexException propagated underlying non-runtime exceptions
     */
    void commit(IndexCommit commit) throws EDBIndexException;
}
