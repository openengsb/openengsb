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
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService.BackendStorageType;

/**
 * To persist {@link ConfigItem} correctly in various situations some model specific transformations are required. Those
 * transformation are depend on the type of the backend. For example a {@link BackendStorageType#File} requires a
 * unique-string-representation of the metadata while the {@link BackendStorageType#PersistenceService} can store the
 * {@link ConfigItem#getMetaData()} directly as object. Since the general {@link ConfigPersistenceBackendService} should
 * not have to be aware how those model specific transformation have to be done this transformer object should be used.
 */
public interface ConfigPersistenceTransformer {

    /**
     * Should transform the metaData of a {@link ConfigItem} to a single string representation which can be used as
     * unique identifier or as e.g. a file name for a configuration.
     */
    String transformMetadataToUniqueString(Properties metaData);

    /**
     * Various possible {@link ConfigPersistenceBackendService} backends store the configuration in various ways which
     * may require different representations. E.g. when the configuraion should be stored on disc it may require some
     * special (single-string) formation.
     */
    boolean requiresSpecialDiscRepresentation();

    /**
     * Transforms in in-memory {@link Properties} representation of the values of an {@link ConfigItem} into a disc
     * representation. E.g. a rule or a flow into a single string.
     */
    String transformToDiscRepresentation(Properties values) throws InvalidConfigurationException;

    /**
     * Transforms (e.g. the single string representation of a rule) back into an in-memory key-value pair property file.
     */
    Properties transformToMemoryRepresentation(String discRepresentation) throws InvalidConfigurationException;

}
