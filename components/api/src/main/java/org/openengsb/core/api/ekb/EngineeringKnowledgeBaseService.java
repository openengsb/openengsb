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
import org.openengsb.core.api.model.OpenEngSBModelEntry;

/**
 * The interface for the ekb service. Contains the functionalities to query for models and also a function for creating
 * a proxy for simulating simple OpenEngSBModel interfaces.
 * 
 * This service interface is deprecated. If you want to use the proxiing functionality, you have to use the ModelFactory
 * service. If you want to query for elements from the EDB, you have to use the QueryInterface service.
 */
@Deprecated
public interface EngineeringKnowledgeBaseService {

    /**
     * Creates a proxy for the model interface which simulates an implementation of the interface.
     */
    @Deprecated
    <T extends OpenEngSBModel> T createEmptyModelObject(Class<T> model, OpenEngSBModelEntry... entries);

    /**
     * Loads the most actual tool data from the given oid
     */
    @Deprecated
    <T extends OpenEngSBModel> T getModel(Class<T> model, String oid);

    /**
     * Loads the history (all saved versions) of the tool data from the given oid
     */
    @Deprecated
    <T extends OpenEngSBModel> List<T> getModelHistory(Class<T> model, String oid);

    /**
     * Loads the history (all saved versions) of the tool data from the given oid for the given time range
     */
    @Deprecated
    <T extends OpenEngSBModel> List<T> getModelHistoryForTimeRange(Class<T> model, String oid, Long from, Long to);

    /**
     * Queries for models which have a OpenEngSBModelEntry with the given key and the given value saved
     */
    @Deprecated
    <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, String key, Object value);

    /**
     * Queries for models which have all key/value pairs given in the map saved in the OpenEngSBModelEntries
     */
    @Deprecated
    <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap);
}
