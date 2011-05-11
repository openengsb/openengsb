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

package org.openengsb.core.workflow.internal.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;

public class WorkflowFilePersistenceBackendService implements ConfigPersistenceBackendService<WorkflowRepresentation> {

    private final WorkflowRepresentationConverter converter;

    private final File folder;

    public static final String PERSISTENCE_FOLDER = "/openengsb/workflows/persistence/";

    public WorkflowFilePersistenceBackendService(WorkflowRepresentationConverter marshaller) {
        super();
        this.converter = marshaller;
        String persistenceFolder = System.getProperty("karaf.data") + PERSISTENCE_FOLDER;
        folder = new File(persistenceFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public List<ConfigItem<WorkflowRepresentation>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        List<ConfigItem<WorkflowRepresentation>> result = new ArrayList<ConfigItem<WorkflowRepresentation>>();
        try {
            File[] listFiles = folder.listFiles();
            Arrays.sort(listFiles, NameFileComparator.NAME_COMPARATOR);
            for (File file : listFiles) {
                String readFileToString = FileUtils.readFileToString(file);
                WorkflowRepresentation unmarshallWorkflow = converter.unmarshallWorkflow(readFileToString);
                ConfigItem<WorkflowRepresentation> config = new ConfigItem<WorkflowRepresentation>();
                config.setContent(unmarshallWorkflow);
                result.add(config);
            }
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
        return result;
    }

    @Override
    public void persist(ConfigItem<WorkflowRepresentation> config) throws PersistenceException {
        WorkflowRepresentation content = config.getContent();
        String marshallWorkflow = converter.marshallWorkflow(content);
        File file = new File(folder, content.getName());
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(marshallWorkflow);
            writer.close();
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return false;
    }
}
