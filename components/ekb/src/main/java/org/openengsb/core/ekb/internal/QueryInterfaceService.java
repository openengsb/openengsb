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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.ekb.QueryInterface;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the QueryInterface service. It's main responsibilities are the loading of elements from the EDB and
 * converting them to the correct format.
 */
public class QueryInterfaceService implements QueryInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryInterfaceService.class);

    private EngineeringDatabaseService edbService;
    private EDBConverter edbConverter;

    @Override
    public <T extends OpenEngSBModel> T getModel(Class<T> model, String oid) {
        LOGGER.debug("Invoked getModel with the model %s and the oid %s", model.getName(), oid);
        EDBObject object = edbService.getObject(oid);
        return edbConverter.convertEDBObjectToModel(model, object);
    }

    @Override
    public <T extends OpenEngSBModel> List<T> getModelHistory(Class<T> model, String oid) {
        LOGGER.debug("Invoked getModelHistory with the model %s and the oid %s", model.getName(), oid);
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.getHistory(oid));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> getModelHistoryForTimeRange(Class<T> model,
            String oid, Long from, Long to) {
        LOGGER.debug("Invoked getModelHistoryForTimeRange with the model %s and the oid %s for the "
                + "time period of %s to %s", new Object[]{ model.getName(), oid, new Date(from).toString(),
            new Date(to).toString() });
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.getHistory(oid, from, to));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, String key, Object value) {
        LOGGER.debug("Invoked queryForModels with the model %s, the key %s and the value %s", new Object[]{
            model.getName(), key, value });
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(key, value));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap) {
        LOGGER.debug("Invoked queryForModels with the model %s and a map", model.getName());
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(queryMap));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap,
            Long timestamp) {
        LOGGER.debug("Invoked queryForModels with the model %s, a map for the time %s", model.getName(), new Date(
            timestamp).toString());
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(queryMap, timestamp));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, String query, String timestamp) {
        Long time = Long.parseLong(timestamp);
        LOGGER.debug("Invoked queryForModels with the model %s and the querystring %s for the time %s",
            new Object[]{ model.getName(), query, new Date(time).toString() });
        Map<String, Object> map = generateMapOutOfString(query);
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(map, time));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForActiveModels(Class<T> model, Map<String, Object> queryMap) {
        LOGGER.debug("Invoked queryForActiveModels with the model %s and a query map", model.getName());
        Long now = System.currentTimeMillis();
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(queryMap, now));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForActiveModels(Class<T> model) {
        LOGGER.debug("Invoked queryForActiveModels with the model %s", model.getName());
        Long now = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<String, Object>();
        return edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(map, now));
    }

    /**
     * Generates a map out of a query string. A query string has the format "propA:valueA and propB:valueB and ..."
     */
    private Map<String, Object> generateMapOutOfString(String query) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (query.isEmpty()) {
            return map;
        }
        String regex = "(\\w+\\:\\w+(\\s(and)\\s\\w+\\:\\w+)*)?";
        if (!query.matches(regex)) {
            String errorMessage = "Query string must be empty or have the form 'a:b [and b:c and ...]'";
            throw new IllegalArgumentException(errorMessage);
        }
        String[] elements = query.split(" and ");
        for (String element : elements) {
            String[] parts = element.split(":");
            map.put(parts[0], parts[1]);
        }
        return map;
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }

    public void setEdbConverter(EDBConverter edbConverter) {
        this.edbConverter = edbConverter;
    }
}
