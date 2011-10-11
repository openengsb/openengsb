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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FileUtils;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.model.RuleBaseConfiguration;
import org.openengsb.core.workflow.model.RuleBaseElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class RuleBaseElementPersistenceBackendService implements ConfigPersistenceBackendService<RuleBaseElement> {

    private File storageFolder;
    private URLCodec encoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleBaseElementPersistenceBackendService.class);
    protected static final String SEPARATOR = ",";

    @Override
    public List<ConfigItem<RuleBaseElement>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        LOGGER.debug("Loading Rulebase Configuration");

        RuleBaseFileNameFilter filter = new RuleBaseFileNameFilter(metadata);
        Collection<File> files = FileUtils.listFiles(storageFolder, filter, null);

        List<ConfigItem<RuleBaseElement>> ret = new ArrayList<ConfigItem<RuleBaseElement>>();
        for (File file : files) {
            ret.add(readConfigFile(file));
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void persist(ConfigItem<RuleBaseElement> config) throws PersistenceException, InvalidConfigurationException {
        LOGGER.debug("persisting \"{}\"", config.getContent().getType().toString());

        Map<String, String> metaData = config.getMetaData();

        Preconditions.checkArgument(supports((Class<? extends ConfigItem<?>>) config.getClass()),
            "Argument type not supported");
        Preconditions.checkNotNull(metaData.get(RuleBaseElement.META_RULE_TYPE),
            "Type metadata has to be supplied");
        Preconditions.checkNotNull(metaData.get(RuleBaseElement.META_RULE_PACKAGE),
            "package metadata has to be supplied");
        Preconditions.checkNotNull(metaData.get(RuleBaseElement.META_RULE_NAME),
            "name metadata has to be supplied");

        File targetFile = getPathForMetaData(config.getMetaData());
        try {
            FileUtils.writeStringToFile(targetFile, config.getContent().getCode());
        } catch (IOException e) {
            throw new PersistenceException(e);
        }

    }

    private File getPathForMetaData(Map<String, String> metaData) throws PersistenceException {
        String type;
        String name;
        String pack;
        try {
            type = encoder.encode(metaData.get(RuleBaseElement.META_RULE_TYPE));
            name = encoder.encode(metaData.get(RuleBaseElement.META_RULE_NAME));
            pack = encoder.encode(metaData.get(RuleBaseElement.META_RULE_PACKAGE));
        } catch (EncoderException e) {
            throw new PersistenceException(e);
        }

        String filename = type + SEPARATOR + name + SEPARATOR + pack;
        return new File(storageFolder, filename);
    }

    private RuleBaseConfiguration readConfigFile(File file) throws PersistenceException {
        LOGGER.debug("reading configfile \"{}\"", file);
        String[] parts = file.getName().split(SEPARATOR);

        RuleBaseElement element = new RuleBaseElement();
        element.setType(RuleBaseElementType.valueOf(parts[0]));

        try {
            element.setName(encoder.decode(parts[1]));
            element.setPackageName(encoder.decode(parts[2]));
            element.setCode(FileUtils.readFileToString(file));
        } catch (IOException e) {
            throw new PersistenceException(e);
        } catch (DecoderException e) {
            throw new PersistenceException(e);
        }
        return new RuleBaseConfiguration(element.toMetadata(), element);
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        LOGGER.debug("removing Rulebase Configuration");

        RuleBaseFileNameFilter filter = new RuleBaseFileNameFilter(metadata);
        Collection<File> files = FileUtils.listFiles(storageFolder, filter, null);

        for (File file : files) {
            if (!file.delete()) {
                LOGGER.warn("\"{}\" couldn't be deleted!", file);
            }
        }
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return RuleBaseConfiguration.class.isAssignableFrom(configItemType);
    }

    public void setStorageFolderPath(String storageFolderPath) {
        this.storageFolder = new File(storageFolderPath);
    }

    public void init() throws PersistenceException {
        encoder = new URLCodec();
        if (!storageFolder.exists()) {
            try {
                FileUtils.forceMkdir(storageFolder);
            } catch (IOException e) {
                throw new PersistenceException(e);
            }
        }
    }

}
