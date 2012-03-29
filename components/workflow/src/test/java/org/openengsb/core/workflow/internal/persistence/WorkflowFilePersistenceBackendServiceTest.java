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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;

public class WorkflowFilePersistenceBackendServiceTest {

    private WorkflowRepresentationConverter converter;
    private String marshalled = "Marshalled";
    private WorkflowFilePersistenceBackendService service;
    private String folder;

    @Before
    public void setUp() {
        converter = mock(WorkflowRepresentationConverter.class);
        folder = "target/test/" + System.currentTimeMillis();
        System.setProperty("karaf.data", folder);
        new File(folder).mkdirs();
        createWorkflowFilePersistence();
    }

    public void createWorkflowFilePersistence() {
        service = new WorkflowFilePersistenceBackendService(converter);
    }

    @Test
    public void testPersist_shouldWriteContentToCorrectFile() throws PersistenceException, FileNotFoundException {
        String name = "SomeWorkflow";

        persistWorkflowToFile(name);

        File out = new File(folder + WorkflowFilePersistenceBackendService.PERSISTENCE_FOLDER + name);
        assertTrue(out.exists());
        Scanner s = new Scanner(out);
        assertThat(s.nextLine(), equalTo(marshalled));
        assertFalse(s.hasNextLine());
    }

    public void persistWorkflowToFile(String name) throws PersistenceException {
        WorkflowRepresentation representation = new WorkflowRepresentation();
        representation.setName(name);
        setUp();
        when(converter.marshallWorkflow(representation)).thenReturn(marshalled);

        ConfigItem<WorkflowRepresentation> config = new ConfigItem<WorkflowRepresentation>();
        config.setContent(representation);

        service.persist(config);
    }

    @Test
    public void testLoad_shouldLoadAllWorkflowsFromFolder() throws InvalidConfigurationException, PersistenceException {
        System.setProperty("karaf.data", "src/test/resources/");
        WorkflowRepresentation rep1 = new WorkflowRepresentation();
        WorkflowRepresentation rep2 = new WorkflowRepresentation();
        String workflow1 = "Workflow1\nWorkflow1";
        String workflow2 = "Workflow2\nWorkflow2";
        when(converter.unmarshallWorkflow(workflow1)).thenReturn(rep1);
        when(converter.unmarshallWorkflow(workflow2)).thenReturn(rep2);

        createWorkflowFilePersistence();
        List<ConfigItem<WorkflowRepresentation>> load = service.load(new HashMap<String, String>());

        verify(converter).unmarshallWorkflow(workflow2);
        verify(converter).unmarshallWorkflow(workflow1);
        assertThat(load.size(), equalTo(2));
        assertThat(load.get(0).getContent(), sameInstance(rep1));
        assertThat(load.get(1).getContent(), sameInstance(rep2));
    }

    @Test
    public void testRemove_shouldRemoveWorkflowFile() throws PersistenceException {
        final String name = "Workflow";
        persistWorkflowToFile(name);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("name", name);
        service.remove(hashMap);
        final String[] list = new File(folder + WorkflowFilePersistenceBackendService.PERSISTENCE_FOLDER).list();
        assertThat(list.length, equalTo(0));
    }
}
