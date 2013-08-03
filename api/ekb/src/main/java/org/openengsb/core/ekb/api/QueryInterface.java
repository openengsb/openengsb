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

package org.openengsb.core.ekb.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.model.CommitQueryRequest;

/**
 * The query interface provides the functions to access the data stored in the EDB.
 */
public interface QueryInterface {

    /**
     * Loads the most actual tool data from the given oid
     */
    <T> T getModel(Class<T> model, String oid);

    /**
     * Loads the history (all saved versions) of the tool data from the given oid
     */
    <T> List<T> getModelHistory(Class<T> model, String oid);

    /**
     * Loads the history (all saved versions) of the tool data from the given oid for the given time range
     */
    <T> List<T> getModelHistoryForTimeRange(Class<T> model, String oid, Long from, Long to);

    /**
     * Queries for models which have all key/value pairs given by the query saved in the OpenEngSBModelEntries for a
     * given timestamp ("cut" at the timestamp and get all elements where the pairs fit)
     */
    <T> List<T> queryForModelsAtTimestamp(Class<T> model, String query, String timestamp);

    /**
     * Queries for the most actual models which have all key/value pairs given by the query saved in the
     * OpenEngSBModelEntries
     */
    <T> List<T> queryForModels(Class<T> model, String query);

    /**
     * Queries for models which have all key/value pairs given in the map saved in the OpenEngSBModelEntries for a given
     * timestamp ("cut" at the timestamp and get all elements where the pairs fit)
     */
    <T> List<T> queryForModelsByQueryMapAtTimestamp(Class<T> model, Map<String, Object> queryMap, Long timestamp);
    
    /**
     * Queries for models which have all key/value pairs given in the map saved in the OpenEngSBModelEntries
     */
    <T> List<T> queryForModelsByQueryMap(Class<T> model, Map<String, Object> queryMap);

    /**
     * Queries for active models which have all key/value pairs given in the map saved in the OpenEngSBModelEntries.
     * Active models mean models which are in the newest version.
     */
    <T> List<T> queryForActiveModelsByQueryMap(Class<T> model, Map<String, Object> queryMap);

    /**
     * Queries for active models of the given model type. Active models mean models which are in the newest version.
     */
    <T> List<T> queryForActiveModels(Class<T> model);
    
    /**
     * Returns the most recent revision number of the EDB
     */
    UUID getCurrentRevisionNumber();
    
    /**
     * Returns a list of revision strings of all commits which are matching the given request.
     */
    List<String> queryForCommits(CommitQueryRequest request) throws EKBException;
    
    /**
     * Loads the EKBCommit object with the given revision from the data source.
     */
    EKBCommit loadCommit(String revision) throws EKBException;
}
