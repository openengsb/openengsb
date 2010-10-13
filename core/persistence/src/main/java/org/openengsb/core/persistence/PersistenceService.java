/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.persistence;

import java.util.List;
import java.util.Map;

/**
 *
 * The interface of the common persistence solution of the OpenEngSB. This service is meant to be used by domains and
 * other components of the OpenEngSB to store their data.
 */
public interface PersistenceService {

    /**
     * Query for elements based on a given example element. Null field values are used as wildcards and will match with
     * any value.
     */
    <TYPE> List<TYPE> query(TYPE example);

    /**
     * Query for elements based on a list of example elements. This method returns the results of queries for the
     * examples contained in the example list accumulated in one result list.
     */
    <TYPE> List<TYPE> query(List<TYPE> examples);

    /**
     * Store the given element in the database. Multiple calls with the same element do not fail, but simply stores the
     * element twice independently. Be careful when storing object with references to other objects, as these referenced
     * objects are also stored in the database.
     *
     * @throws PersistenceException if the element could not be stored because of an error of the underlying database
     *         system.
     */
    void create(Object bean) throws PersistenceException;

    /**
     * Store the given list of elements. Behaves like multiple calls to {@link #create(Object)}, except for objects with
     * references to other objects in the list of objects to create. These are handled correctly storing the respective
     * element only once.
     *
     * @throws PersistenceException if the element could not be stored because of an error of the underlying database
     *         system.
     */
    void create(List<? extends Object> beans) throws PersistenceException;

    /**
     * Update {@code oldBean} stored in the database with the values from {@code newBean}.
     *
     * @throws PersistenceException if a query using {@code oldBean} does not return exactly one element stored in the
     *         database.
     */
    <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException;

    /**
     * Update the keys of the given map stored in the database with the values of the given map. Has transactional
     * behavior, so if one of the update operations fails no update is performed.
     *
     * @throws PersistenceException if a query using the key element in the map does not return exactly one element
     *         stored in the database.
     */
    <TYPE> void update(Map<TYPE, TYPE> beans) throws PersistenceException;

    /**
     *
     * Delete all elements which are returned by a query using the given {@code example} object.
     *
     * @throws PersistenceException if no element can be found for the given query by example object
     */
    <TYPE> void delete(TYPE example) throws PersistenceException;

    /**
     *
     * Delete all elements which are returned by a query using the given list of {@code examples}.
     *
     * @throws PersistenceException if the underlying database system throws an exception
     */
    <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException;

}
