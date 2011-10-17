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

package org.openengsb.core.ekb.internal;

import java.util.List;
import java.util.Map;

import org.openengsb.core.api.ekb.EngineeringKnowledgeBaseService;
import org.openengsb.core.api.ekb.ModelFactory;
import org.openengsb.core.api.ekb.QueryInterface;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

/**
 * Service which implements the EngineeringKnowlegeBaseService. Also represents a proxy for simulating simple
 * OpenEngSBModel interfaces.
 */
public class EKBService implements EngineeringKnowledgeBaseService {
    private ModelFactory modelFactory;
    private QueryInterface queryInterface;

    @Override
    public <T extends OpenEngSBModel> T createEmptyModelObject(Class<T> model, OpenEngSBModelEntry... entries) {
        return modelFactory.createEmptyModelObject(model, entries);
    }

    @Override
    public <T extends OpenEngSBModel> T getModel(Class<T> model, String oid) {
        return queryInterface.getModel(model, oid);
    }

    @Override
    public <T extends OpenEngSBModel> List<T> getModelHistory(Class<T> model, String oid) {
        return queryInterface.getModelHistory(model, oid);
    }

    @Override
    public <T extends OpenEngSBModel> List<T> getModelHistoryForTimeRange(Class<T> model, String oid, 
            Long from, Long to) {
        return queryInterface.getModelHistoryForTimeRange(model, oid, from, to);
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, String key, Object value) {
        return queryInterface.queryForModels(model, key, value);
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap) {
        return queryInterface.queryForModels(model, queryMap);
    }

    public void setModelFactory(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    public void setQueryInterface(QueryInterface queryInterface) {
        this.queryInterface = queryInterface;
    }
}
