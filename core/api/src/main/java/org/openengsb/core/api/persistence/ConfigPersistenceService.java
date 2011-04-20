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

package org.openengsb.core.api.persistence;

import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ConfigItem;

/**
 * A thin wrapper service around the {@link ConfigPersistenceBackendService} implementations providing them via the OSGi
 * registry.
 */
public interface ConfigPersistenceService {

    /**
     * Small wrapper around {@link ConfigPersistenceBackendService#load(Map)}
     */
    <ConfigType extends ConfigItem<?>> List<ConfigType> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException;

    /**
     * Small wrapper around {@link ConfigPersistenceBackendService#persist(ConfigItem)}
     */
    <ConfigType extends ConfigItem<?>> void persist(ConfigType configuration) throws PersistenceException,
        InvalidConfigurationException;

    /**
     * Small wrapper around {@link ConfigPersistenceBackendService#remove(Map)}
     */
    void remove(Map<String, String> metadata) throws PersistenceException;

    /**
     * Returns if the backend is applicable for a specific {@link ConfigItem} type.
     */
    boolean supports(Class<? extends ConfigItem<?>> configItemType);
}
