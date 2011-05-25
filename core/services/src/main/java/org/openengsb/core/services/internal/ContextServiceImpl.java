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

package org.openengsb.core.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.context.Context;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.context.ContextPath;
import org.openengsb.core.api.model.ContextConfiguration;
import org.openengsb.core.api.model.ContextId;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ContextServiceImpl implements ContextCurrentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextServiceImpl.class);

    private BundleContext bundleContext;

    @Override
    public Context getContext(String path) {
        return getContext(path, false);
    }

    @Override
    public Context getContext() {
        String currentContextId = ContextHolder.get().getCurrentContextId();
        if (currentContextId == null) {
            return null;
        }
        return getContextById(currentContextId);
    }

    @Override
    public String getThreadLocalContext() {
        return ContextHolder.get().getCurrentContextId();
    }

    @Override
    public void setThreadLocalContext(String contextId) {
        ContextHolder.get().setCurrentContextId(contextId);
        Context context = getContextById(contextId);
        Preconditions.checkArgument(context != null, "No context exists for given context id");
        ContextHolder.get().setCurrentContextId(contextId);
    }

    @Override
    public void createContext(String contextId) {
        LOGGER.debug("Creating context %s", contextId);
        ContextConfiguration contextConfiguration =
            new ContextConfiguration(new ContextId(contextId).toMetaData(), null);
        try {
            getConfigPersistenceService().persist(contextConfiguration);
        } catch (InvalidConfigurationException e) {
            LOGGER.error("Error storing context " + contextId + ": Invalid configuration", e);
        } catch (PersistenceException e) {
            LOGGER.error("Error storing context " + contextId + ": Persistence error", e);
        }
    }

    @Override
    public List<String> getAvailableContexts() {
        List<ContextConfiguration> availableContextConfigurations =
            getContextConfigurationsOrEmptyOnError(ContextId.getContextWildCard());
        List<String> availableContexts = new ArrayList<String>();

        for (ContextConfiguration configuration : availableContextConfigurations) {
            availableContexts.add(ContextId.fromMetaData(configuration.getMetaData()).getId());
        }

        return availableContexts;
    }

    private Context getContextById(String currentContextId) {
        ContextId contextId = new ContextId(currentContextId);
        List<ContextConfiguration> contexts = getContextConfigurationsOrEmptyOnError(contextId.toMetaData());
        if (contexts.isEmpty()) {
            return null;
        }
        return contexts.get(0).toContext();
    }

    private Context getContext(String path, boolean create) {
        Context c = getContext();
        if (c == null) {
            return null;
        }
        Context parent = null;
        for (String pathElem : new ContextPath(path).getElements()) {
            parent = c;
            c = c.getChild(pathElem);
            if (c == null) {
                if (!create) {
                    return null;
                }
                c = parent.createChild(pathElem);
            }
        }
        return c;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private List<ContextConfiguration> getContextConfigurationsOrEmptyOnError(Map<String, String> metaData) {
        try {
            ConfigPersistenceService persistence = this.getConfigPersistenceService();
            return persistence.load(metaData);
        } catch (Exception e) {
            LOGGER.error("Error loading context configuration from configuration persistence", e);
            return Collections.emptyList();
        }
    }

    private ConfigPersistenceService getConfigPersistenceService() {
        return OpenEngSBCoreServices.getConfigPersistenceService(ContextConfiguration.TYPE_ID);
    }

}
