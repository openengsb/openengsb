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

import java.util.Properties;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.RuleConfiguration;
import org.openengsb.core.api.workflow.WorkflowService;

/**
 * Service to store properties in one of the possible including metadata. The backend type can be very specific to te
 * configuration item type but don't have to. This strongly depends on how much information is required by the backend
 * service.
 */
public interface ConfigPersistenceBackendService {

    /**
     * Loads an configuration items according to meta-information available. Depending on the backend service the
     * searched items could be either have to map perfectly or also accept regex.
     *
     * @throws PersistenceException if the access to the persistence base is not possible
     * @throws InvalidConfigurationException if the configuration is no longer valid (modified e.g. directly in a file).
     */
    ConfigItem<?> load(Properties metadata) throws PersistenceException, InvalidConfigurationException;

    /**
     * Persists a configuration to the selected backend solution. Please do not use this metod if you're not completely
     * aware what you're doing. Persisting e.g. a {@link ConnectorConfiguration} would NOT create a connector,
     * persisting a {@link RuleConfiguration} would not activate the rule and so on. Please use the corresponding
     * services like the {@link WorkflowService} to do such things.
     *
     * @throws PersistenceException if the access to the persistence base is not possible
     * @throws InvalidConfigurationException if the configuration passed alone is not valid.
     */
    void persist(ConfigItem<?> config) throws PersistenceException, InvalidConfigurationException;

}
