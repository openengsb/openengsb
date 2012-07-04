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
package org.openengsb.persistence.rulebase.filebackend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.workflow.model.ImportConfiguration;
import org.openengsb.core.workflow.model.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ImportDeclarationPersistenceBackendService implements ConfigPersistenceBackendService<ImportDeclaration> {

    private File storageFile;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDeclarationPersistenceBackendService.class);

    @Override
    public List<ConfigItem<ImportDeclaration>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        List<ConfigItem<ImportDeclaration>> ret;
        LOGGER.debug("loading ImportDeclaration Configuration");

        if (storageFile.exists()) {
            if (metadata.get(ImportDeclaration.META_IMPORT_CLASS) == null) {
                ret = loadAllImports();
            } else {
                ret = loadSingleImport(metadata.get(ImportDeclaration.META_IMPORT_CLASS));
            }
        } else {
            LOGGER.debug(
                "Can't load configuration, because \"{}\" doesn't exist. Returning default empty list!", storageFile);
            ret = new ArrayList<ConfigItem<ImportDeclaration>>();
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void persist(ConfigItem<ImportDeclaration> config) throws PersistenceException,
        InvalidConfigurationException {
        LOGGER.debug("persisting import \"{}\"", config.getContent().getClassName());
        Preconditions.checkArgument(supports((Class<? extends ConfigItem<?>>) config.getClass()),
            "Argument type not supported");
        List<String> imports = readStorageFile();
        String clazz = config.getContent().getClassName();
        if (!imports.contains(clazz)) {
            imports.add(clazz);
        }
        writeStorageFile(imports);
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        LOGGER.debug("removing import");
        Preconditions.checkNotNull(metadata.get(ImportDeclaration.META_IMPORT_CLASS),
            "class name has to be defined");
        List<String> imports = readStorageFile();
        imports.remove(metadata.get(ImportDeclaration.META_IMPORT_CLASS));
        writeStorageFile(imports);
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return ImportConfiguration.class.isAssignableFrom(configItemType);
    }

    private List<String> readStorageFile() throws PersistenceException {
        LOGGER.debug("try to read \"{}\"", storageFile);

        if (storageFile.exists()) {
            try {
                return FileUtils.readLines(storageFile);
            } catch (IOException e) {
                LOGGER.error("Error reading \"{}\"", storageFile);
                throw new PersistenceException(e);
            }
        }
        return new ArrayList<String>();
    }

    private List<ConfigItem<ImportDeclaration>> loadSingleImport(String clazz) throws PersistenceException {
        LOGGER.debug("Load single import \"{}\"", clazz);
        List<ConfigItem<ImportDeclaration>> ret = new ArrayList<ConfigItem<ImportDeclaration>>();
        List<String> imports = readStorageFile();

        if (imports.contains(clazz)) {
            ImportDeclaration importDec = new ImportDeclaration(clazz);
            ImportConfiguration cnf = new ImportConfiguration(importDec.toMetadata(), importDec);
            ret.add(cnf);
        }
        return ret;
    }

    private List<ConfigItem<ImportDeclaration>> loadAllImports() throws PersistenceException {
        LOGGER.debug("Load all imports");
        List<ConfigItem<ImportDeclaration>> ret = new ArrayList<ConfigItem<ImportDeclaration>>();
        List<String> imports = readStorageFile();

        for (String clazz : imports) {
            ImportDeclaration importDec = new ImportDeclaration(clazz);
            ImportConfiguration cnf = new ImportConfiguration(importDec.toMetadata(), importDec);
            ret.add(cnf);
        }
        return ret;
    }

    private void writeStorageFile(List<String> imports) throws PersistenceException {
        LOGGER.debug("write imports to \"{}\"", storageFile);
        try {
            FileUtils.writeLines(storageFile, imports);
        } catch (IOException e) {
            throw new PersistenceException(e);
        }

    }

    public void setStorageFilePath(String storageFilePath) {
        this.storageFile = new File(storageFilePath);
    }
}
