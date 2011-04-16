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
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;

/**
 * Default implementation of the {@link ConfigPersistenceService} registered by the ConfigPersistenceServiceFactory with
 * the correct properties at the right places. Basically this class does nothing than forwarding the backend services
 * and casting them to the right type.
 */
public class DefaultConfigPersistenceService implements ConfigPersistenceService {

    private final ConfigPersistenceBackendService backendService;

    public DefaultConfigPersistenceService(ConfigPersistenceBackendService backendService) {
        this.backendService = backendService;
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return backendService.supports(configItemType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ConfigType extends ConfigItem<?>> List<ConfigType> load(Map<String, String> metadata)
        throws PersistenceException, InvalidConfigurationException {
        List<ConfigType> configItems = new ArrayList<ConfigType>();
        List<ConfigItem<?>> result = backendService.load(metadata);
        for (ConfigItem<?> configItem : result) {
            configItems.add((ConfigType) configItem);
        }
        return configItems;
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        backendService.remove(metadata);
    }

    @Override
    public <ConfigType extends ConfigItem<?>> void persist(ConfigType configuration) throws PersistenceException,
        InvalidConfigurationException {
        backendService.persist(configuration);
    }

}
