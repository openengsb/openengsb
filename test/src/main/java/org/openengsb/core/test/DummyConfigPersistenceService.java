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

package org.openengsb.core.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;

/**
 * Simple {@link ConfigPersistenceBackendService} implementation using an in-memory list. This implementation is
 * intended only as a mockup implementation for ConfigPersistenceBackend.
 */
public class DummyConfigPersistenceService<E> implements ConfigPersistenceBackendService<E> {

    private List<ConfigItem<E>> data = Collections.synchronizedList(new ArrayList<ConfigItem<E>>());

    @Override
    public List<ConfigItem<E>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        return searchForMetadata(metadata);
    }

    @Override
    public void persist(ConfigItem<E> config) throws PersistenceException, InvalidConfigurationException {
        List<ConfigItem<E>> alreadyContained = searchForMetadata(config.getMetaData());
        if (alreadyContained.size() > 1) {
            throw new PersistenceException("Could not persist, since there are already to many configs");
        } else {
            if (alreadyContained.size() == 1) {
                data.remove(alreadyContained.get(0));
            }
            data.add(config);
        }
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        List<ConfigItem<E>> toDelete = searchForMetadata(metadata);
        data.removeAll(toDelete);
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return ConfigItem.class.isAssignableFrom(configItemType);
    }

    private List<ConfigItem<E>> searchForMetadata(Map<String, String> metadata) {
        List<ConfigItem<E>> ret = new ArrayList<ConfigItem<E>>();
        synchronized (data) {
            for (ConfigItem<E> item : data) {
                if (matches(item.getMetaData(), metadata)) {
                    ret.add(item);
                }
            }
        }
        return ret;
    }

    private boolean matches(Map<String, String> objMetadata, Map<String, String> exampleMetadata) {
        for (Entry<String, String> entry : exampleMetadata.entrySet()) {
            String example = entry.getValue();
            String objValue = objMetadata.get(entry.getKey());
            if (example != null && !example.equals(objValue)) {
                return false;
            }
        }
        return true;
    }
}
