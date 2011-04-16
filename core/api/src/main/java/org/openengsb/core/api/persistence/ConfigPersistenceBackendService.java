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
    List<ConfigItem<?>> load(Map<String, String> metadata) throws PersistenceException, InvalidConfigurationException;

    /**
     * Persists a configuration to the selected backend solution. Please do not use this metod if you're not completely
     * aware what you're doing. Persisting e.g. a ConnectorConfiguration would NOT create a connector, persisting a
     * RuleConfiguration would not activate the rule and so on. Please use the corresponding services like the
     * WorkflowService to do such things.
     *
     * @throws PersistenceException if the access to the persistence base is not possible
     * @throws InvalidConfigurationException if the configuration passed alone is not valid.
     */
    void persist(ConfigItem<?> config) throws PersistenceException, InvalidConfigurationException;

    /**
     * Removes all configurations which match the matadata specified. Be careful that the meta-data pattern matches to
     * more entries than you've expected. If you want make sure that you only get all entries you like try a
     * {@link #load(Map)} first and call foreach meta-data entry in the right entries you've found.
     * 
     * @throws PersistenceException thrown if the access to the persistence base is not possible or an error occurred
     *         during the remove operation.
     */
    void remove(Map<String, String> metadata) throws PersistenceException;

    /**
     * Returns if the backend is applicable for a specific {@link ConfigItem} type.
     */
    boolean supports(Class<? extends ConfigItem<?>> configItemType);

}
