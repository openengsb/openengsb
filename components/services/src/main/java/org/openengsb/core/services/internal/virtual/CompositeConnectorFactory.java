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

package org.openengsb.core.services.internal.virtual;

import java.util.Collections;
import java.util.Map;

import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.common.VirtualConnectorFactory;
import org.openengsb.core.common.util.FilterUtils;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class CompositeConnectorFactory extends VirtualConnectorFactory<CompositeConnector> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeConnectorFactory.class);

    private OsgiUtilsService utilsService;

    protected CompositeConnectorFactory(DomainProvider domainProvider, OsgiUtilsService utilsService) {
        super(domainProvider);
        this.utilsService = utilsService;
    }

    @Override
    protected void updateHandlerAttributes(CompositeConnector handler, Map<String, String> attributes) {
        String strategyFilter = createStrategyFilterString(attributes.get("compositeStrategy"));
        Filter filter = FilterUtils.makeFilter(CompositeConnectorStrategy.class, strategyFilter);
        CompositeConnectorStrategy strategy =
                utilsService.getOsgiServiceProxy(filter, CompositeConnectorStrategy.class);
        handler.setQueryString(attributes.get("queryString"));
        handler.setCompositeHandler(strategy);
    }

    public String createStrategyFilterString(String attribute) {
        return String.format("(composite.strategy.name=%s)", attribute);
    }

    @Override
    protected CompositeConnector createNewHandler(String id) {
        return new CompositeConnector(id, utilsService);
    }

    @Override
    public Map<String, String> getValidationErrors(Map<String, String> attributes) {
        Map<String, String> result = Maps.newHashMap();
        String queryString = attributes.get("compositeStrategy");

        if (queryString.contains(")")) {
            result.put("compositeStrategy", "character \')\' not allowed in the strategy name");
        }

        String filterString = createStrategyFilterString(queryString);
        try {
            FrameworkUtil.createFilter(filterString);
        } catch (InvalidSyntaxException e) {
            result.put("compositeStrategy", "supplied name cannot be used to form a valid osgi-query: " + filterString
                    + "\n" + e.getMessage());
            LOGGER.error("invalid strategy-parameter supplied", e);
        }

        return result;
        // TODO OPENENGSB-1290: implement some validation
    }

    @Override
    public Map<String, String> getValidationErrors(Connector instance, Map<String, String> attributes) {
        // TODO OPENENGSB-1290: implement some validation
        return Collections.emptyMap();
    }

}
