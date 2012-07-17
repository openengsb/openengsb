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

package org.openengsb.core.persistence.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.osgi.framework.BundleContext;

/**
 * Simple {@link ConfigPersistenceBackendService} implementation directly using the core {@link PersistenceService}.
 * Only disadvantage at the current implementation is that objects have to be stored in simple form.
 */
@Deprecated
public class CorePersistenceServiceBackend<E> implements ConfigPersistenceBackendService<E> {

    private PersistenceManager persistenceManager;
    private PersistenceService persistenceService;
    private BundleContext bundleContext;

    public void init() {
        persistenceService = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    @Override
    public List<ConfigItem<E>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        List<ConfigItem<E>> configItems = new ArrayList<ConfigItem<E>>();
        List<ConfigItem<E>> result = persistenceService.query(new ConfigItem<E>(metadata, null));
        for (ConfigItem<E> configItem : result) {
            configItems.add(configItem);
        }
        return configItems;
    }

    @Override
    public void persist(ConfigItem<E> config) throws PersistenceException, InvalidConfigurationException {
        List<ConfigItem<E>> alreadyPresent = persistenceService.query(new ConfigItem<E>(config.getMetaData(), null));
        if (alreadyPresent.isEmpty()) {
            persistenceService.create(config);
        } else {
            persistenceService.update(alreadyPresent.get(0), config);
        }
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        List<ConfigItem<E>> result =
            persistenceService.query(new ConfigItem<E>(metadata, null));
        persistenceService.delete(result);
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        // as an object persistence this backend supports everything
        return true;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
