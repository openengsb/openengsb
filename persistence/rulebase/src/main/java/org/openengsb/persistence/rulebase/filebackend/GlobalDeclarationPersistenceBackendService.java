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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.workflow.drools.model.GlobalConfiguration;
import org.openengsb.core.workflow.drools.model.GlobalDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A backend implementation for ConfigPersistence that saves GlobalDeclaration objects to file.
 */
public class GlobalDeclarationPersistenceBackendService implements ConfigPersistenceBackendService<GlobalDeclaration> {

    private File storageFile;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalDeclarationPersistenceBackendService.class);
    private static final String SEPARATOR = " ";

    public void setStorageFilePath(String storageFilePath) {
        storageFile = new File(storageFilePath);
    }

    @Override
    public List<ConfigItem<GlobalDeclaration>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {

        List<ConfigItem<GlobalDeclaration>> ret;
        LOGGER.debug("loading GlobalDeclaration Configuration");

        if (storageFile.exists()) {
            if (metadata.get(GlobalDeclaration.META_GLOBAL_VARIABLE) != null) {
                ret = loadSingleGlobal(metadata.get(GlobalDeclaration.META_GLOBAL_VARIABLE));
            } else {
                ret = loadAllGlobals();
            }
        } else {
            LOGGER.debug(
                "Can't load configuration, because \"{}\" doesn't exist. Returning default empty list!", storageFile);
            ret = new ArrayList<ConfigItem<GlobalDeclaration>>();
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void persist(ConfigItem<GlobalDeclaration> config) throws PersistenceException,
        InvalidConfigurationException {
        LOGGER.debug("persisting global \"{}\"", config.getContent().getVariableName());
        Preconditions.checkArgument(supports((Class<? extends ConfigItem<?>>) config.getClass()),
            "Argument type not supported");

        Map<String, String> globals = readStorageFile();
        GlobalDeclaration global = config.getContent();
        globals.put(global.getVariableName(), global.getClassName());
        writeStorageFile(globals);
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        LOGGER.debug("removing global");
        Preconditions.checkNotNull(metadata.get(GlobalDeclaration.META_GLOBAL_VARIABLE),
            "Variable name has to be defined");

        Map<String, String> globals = readStorageFile();
        String variableName = metadata.get(GlobalDeclaration.META_GLOBAL_VARIABLE);
        if (!globals.containsKey(variableName)) {
            LOGGER.warn("Couldn't remove global \"{}\", because it does not exist in \"{}\"", variableName,
                storageFile);
        } else {
            globals.remove(variableName);
            writeStorageFile(globals);
        }
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return GlobalConfiguration.class.isAssignableFrom(configItemType);
    }

    private Map<String, String> readStorageFile() throws PersistenceException {
        LOGGER.debug("try to read \"{}\"", storageFile);
        Map<String, String> ret = new HashMap<String, String>();
        List<String> lines;

        if (storageFile.exists()) {
            try {
                lines = FileUtils.readLines(storageFile);
            } catch (IOException e) {
                LOGGER.error("Error reading \"{}\"", storageFile);
                throw new PersistenceException(e);
            }
            for (String line : lines) {
                if (!line.equals("")) {
                    String[] splitLine = line.split(SEPARATOR);
                    ret.put(splitLine[1], splitLine[0]);
                }
            }
        }
        return ret;
    }

    private List<ConfigItem<GlobalDeclaration>> loadSingleGlobal(String variableName) throws PersistenceException {
        LOGGER.debug("Load single global \"{}\"", variableName);
        List<ConfigItem<GlobalDeclaration>> ret = new ArrayList<ConfigItem<GlobalDeclaration>>();
        Map<String, String> globals = readStorageFile();

        if (globals.containsKey(variableName)) {
            String className = globals.get(variableName);
            GlobalDeclaration global = new GlobalDeclaration(className, variableName);
            GlobalConfiguration cnf = new GlobalConfiguration(global.toMetadata(), global);
            ret.add(cnf);
        }
        return ret;
    }

    private List<ConfigItem<GlobalDeclaration>> loadAllGlobals() throws PersistenceException {
        LOGGER.debug("Load all globals");
        List<ConfigItem<GlobalDeclaration>> ret = new ArrayList<ConfigItem<GlobalDeclaration>>();
        Map<String, String> globals = readStorageFile();

        for (Entry<String, String> entry : globals.entrySet()) {
            GlobalDeclaration global = new GlobalDeclaration(entry.getValue(), entry.getKey());
            GlobalConfiguration cnf = new GlobalConfiguration(global.toMetadata(), global);
            ret.add(cnf);
        }
        return ret;
    }

    private void writeStorageFile(Map<String, String> globals) throws PersistenceException {
        LOGGER.debug("write globals to \"{}\"", storageFile);
        OutputStream os = null;
        try {
            os = FileUtils.openOutputStream(storageFile);
            for (Entry<String, String> entry : globals.entrySet()) {
                IOUtils.write(
                    entry.getValue() + SEPARATOR + entry.getKey() + IOUtils.LINE_SEPARATOR, os);
            }
        } catch (IOException ex) {
            throw new PersistenceException(ex);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

}
