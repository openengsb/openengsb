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

package org.openengsb.core.api.ekb;

import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * The query interface provides the functions to access the data stored in the EDB.
 */
public interface QueryInterface {
    
    /**
     * Loads the most actual tool data from the given oid
     */
    <T extends OpenEngSBModel> T getModel(Class<T> model, String oid);

    /**
     * Loads the history (all saved versions) of the tool data from the given oid
     */
    <T extends OpenEngSBModel> List<T> getModelHistory(Class<T> model, String oid);

    /**
     * Loads the history (all saved versions) of the tool data from the given oid for the given time range
     */
    <T extends OpenEngSBModel> List<T> getModelHistoryForTimeRange(Class<T> model, String oid, Long from, Long to);

    /**
     * Queries for models which have a OpenEngSBModelEntry with the given key and the given value saved
     */
    <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, String key, Object value);

    /**
     * Queries for models which have all key/value pairs given in the map saved in the OpenEngSBModelEntries
     */
    <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap);
    
    /**
     * Queries for models which have all key/value pairs given in the map saved in the OpenEngSBModelEntries for a
     * given timestamp ("cut" at the timestamp and get all elements where the pairs fit)
     */
    <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap, Long timestamp);
}
