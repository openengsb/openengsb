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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ContextConfiguration;
import org.openengsb.core.api.model.ContextId;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ContextFilePersistenceService implements ConfigPersistenceBackendService<Map<String, String>> {

    public static final String CONTEXT_FILE_EXTENSION = "context";
    private File storageFolder;

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextFilePersistenceService.class);

    @Override
    public List<ConfigItem<Map<String, String>>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        LOGGER.debug("Loading Configuration");
        if (metadata == null || metadata.isEmpty()) {
            return loadAll();
        } else {
            return loadFiltered(metadata);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void persist(ConfigItem<Map<String, String>> config) throws PersistenceException,
        InvalidConfigurationException {
        Preconditions.checkArgument(supports((Class<? extends ConfigItem<?>>) config.getClass()),
            "Argument type not supported");
        Preconditions.checkNotNull(config.getMetaData(), "Invalid metadata");

        String contextFileName = getFileNameForMetaData(config.getMetaData());
        File contextPersistenceFile = new File(storageFolder, contextFileName);
        try {
            FileUtils.touch(contextPersistenceFile);
        } catch (IOException e) {
            throw new PersistenceException(String.format("Could not persist context configuration file %s",
                contextFileName), e);
        }
        LOGGER.info("Created context configuration file %s", contextFileName);
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        String contextFileName = getFileNameForMetaData(metadata);
        File contextPersistenceFile = new File(storageFolder, contextFileName);
        Boolean fileSuccessFullyDeleted = FileUtils.deleteQuietly(contextPersistenceFile);
        if (!fileSuccessFullyDeleted) {
            throw new PersistenceException(String.format("Could not delete context configuration file %s",
                contextFileName));
        }
        LOGGER.info("Deleted context configuration file %s", contextFileName);
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return ContextConfiguration.class.isAssignableFrom(configItemType);
    }

    private List<ConfigItem<Map<String, String>>> loadAll() {
        Collection<File> contextFiles = FileUtils.listFiles(storageFolder, getContextExtensions(), false);
        List<ConfigItem<Map<String, String>>> contexts = new ArrayList<ConfigItem<Map<String, String>>>();
        for (File contextFile : contextFiles) {
            contexts.add(loadContextConfigurationFromFile(contextFile));
        }
        return contexts;
    }

    private List<ConfigItem<Map<String, String>>> loadFiltered(Map<String, String> metaData)
        throws PersistenceException {
        List<ConfigItem<Map<String, String>>> configurations = new ArrayList<ConfigItem<Map<String, String>>>();
        File configurationFile = new File(storageFolder, getFileNameForMetaData(metaData));
        if (configurationFile.exists()) {
            configurations.add(loadContextConfigurationFromFile(configurationFile));
        }
        return configurations;
    }

    private String getFileNameForMetaData(Map<String, String> metaData) throws PersistenceException {
        ContextId contextId = ContextId.fromMetaData(metaData);
        return String.format("%s.%s", contextId.getId(), CONTEXT_FILE_EXTENSION);
    }

    private ConfigItem<Map<String, String>> loadContextConfigurationFromFile(File configurationFile) {
        String contextFileName = FilenameUtils.removeExtension(configurationFile.getName());
        ContextId contextId = new ContextId(contextFileName);
        ContextConfiguration contextConfig = new ContextConfiguration(contextId.toMetaData(), null);
        return contextConfig;
    }

    private String[] getContextExtensions() {
        String[] contextFileExtensions = { CONTEXT_FILE_EXTENSION };
        return contextFileExtensions;
    }

    public void setStorageFolderPath(String storageFolderPath) {
        storageFolder = new File(storageFolderPath);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }

}
