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

package org.openengsb.core.ekb.persistence.edb.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.QueryInterface;
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

    private static final Pattern MAP_OUT_OF_STRING_QUERY_PATTERN = Pattern
        .compile("(\\w+\\:\\\"[^\\\"]*\\\"(\\s(and)\\s\\w+\\:\\\"[^\\\"]*\\\")*)?");

    @Override
    public <T> T getModel(Class<T> model, String oid) {
        LOGGER.debug("Invoked getModel with the model {} and the oid {}", model.getName(), oid);
        EDBObject object = edbService.getObject(oid);
        return (T) edbConverter.convertEDBObjectToModel(model, object);
    }

    @Override
    public <T> List<T> getModelHistory(Class<T> model, String oid) {
        LOGGER.debug("Invoked getModelHistory with the model {} and the oid {}", model.getName(), oid);
        return (List<T>) edbConverter.convertEDBObjectsToModelObjects(model, edbService.getHistory(oid));
    }

    @Override
    public <T> List<T> getModelHistoryForTimeRange(Class<T> model, String oid, Long from, Long to) {
        LOGGER.debug("Invoked getModelHistoryForTimeRange with the model {} and the oid {} for the "
                + "time period of {} to {}", new Object[]{ model.getName(), oid, new Date(from).toString(),
            new Date(to).toString() });
        return (List<T>) edbConverter.convertEDBObjectsToModelObjects(model,
            edbService.getHistoryForTimeRange(oid, from, to));
    }

    @Override
    public <T> List<T> queryForModelsByQueryMap(Class<T> model, Map<String, Object> queryMap) {
        LOGGER.debug("Invoked queryForModels with the model {} and a map", model.getName());
        return (List<T>) edbConverter.convertEDBObjectsToModelObjects(model, edbService.queryByMap(queryMap));
    }

    @Override
    public <T> List<T> queryForModelsByQueryMapAtTimestamp(Class<T> model, Map<String, Object> queryMap,
            Long timestamp) {
        LOGGER.debug("Invoked queryForModels with the model {}, a map for the time {}",
            model.getName(), new Date(timestamp).toString());
        return (List<T>) edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(queryMap, timestamp));
    }

    @Override
    public <T> List<T> queryForModels(Class<T> model, String query) {
        return queryForModelsAtTimestamp(model, query, System.currentTimeMillis() + "");
    }

    @Override
    public <T> List<T> queryForModelsAtTimestamp(Class<T> model, String query, String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            LOGGER.debug("Got invalid timestamp string. Use the current timestamp instead");
            timestamp = new Date().getTime() + "";
        }
        Long time = Long.parseLong(timestamp);
        LOGGER.debug("Invoked queryForModels with the model {} and the querystring {} for the time {}",
            new Object[]{ model.getName(), query, new Date(time).toString() });
        Map<String, Object> map = generateMapOutOfString(query);
        return (List<T>) edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(map, time));
    }

    @Override
    public <T> List<T> queryForActiveModelsByQueryMap(Class<T> model, Map<String, Object> queryMap) {
        LOGGER.debug("Invoked queryForActiveModels with the model {} and a query map", model.getName());
        Long now = System.currentTimeMillis();
        return (List<T>) edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(queryMap, now));
    }

    @Override
    public <T> List<T> queryForActiveModels(Class<T> model) {
        LOGGER.debug("Invoked queryForActiveModels with the model {}", model.getName());
        Long now = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<String, Object>();
        return (List<T>) edbConverter.convertEDBObjectsToModelObjects(model, edbService.query(map, now));
    }

    /**
     * Generates a map out of a query string. A query string has the format "propA:valueA and propB:valueB and ..."
     */
    private Map<String, Object> generateMapOutOfString(String query) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (query.isEmpty()) {
            return map;
        }
        if (!MAP_OUT_OF_STRING_QUERY_PATTERN.matcher(query).matches()) {
            String errorMessage = "Query string must be empty or have the form 'a:b [and b:c and ...]'";
            throw new IllegalArgumentException(errorMessage);
        }
        String[] elements = query.split(" and ");
        for (String element : elements) {
            String[] parts = StringUtils.split(element, ":", 2);
            map.put(parts[0], parts[1].substring(1, parts[1].length() - 1));
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
