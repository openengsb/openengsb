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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
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

    @Before
    public void setUp() {
        converter = mock(WorkflowRepresentationConverter.class);
    }

    @Test
    public void testPersist_shouldWriteContentToCorrectFile() throws PersistenceException, FileNotFoundException {
        WorkflowRepresentation representation = new WorkflowRepresentation();
        String name = "SomeWorkflow";
        representation.setName(name);
        setUp();
        String marshalled = "Marshalled";
        when(converter.marshallWorkflow(representation)).thenReturn(marshalled);
        String folder = "target/test/" + System.currentTimeMillis();
        System.setProperty("karaf.data", folder);
        WorkflowFilePersistenceBackendService service = new WorkflowFilePersistenceBackendService(converter);
        new File(folder).mkdirs();
        ConfigItem<WorkflowRepresentation> config = new ConfigItem<WorkflowRepresentation>();
        config.setContent(representation);
        service.persist(config);
        File out = new File(folder + WorkflowFilePersistenceBackendService.PERSISTENCE_FOLDER + name);
        assertTrue(out.exists());
        Scanner s = new Scanner(out);
        assertThat(s.nextLine(), equalTo(marshalled));
        assertFalse(s.hasNextLine());
    }

    @Test
    public void testLoad_ShouldLoadAllWorkflowsFromFolder() throws InvalidConfigurationException, PersistenceException {
        System.setProperty("karaf.data", "src/test/resources/");
        WorkflowFilePersistenceBackendService service =
            new WorkflowFilePersistenceBackendService(converter);
        WorkflowRepresentation rep1 = new WorkflowRepresentation();
        WorkflowRepresentation rep2 = new WorkflowRepresentation();
        // Make sure it does not only read the first line
        String workflow1 = "Workflow1\nWorkflow1";
        String workflow2 = "Workflow2\nWorkflow2";
        when(converter.unmarshallWorkflow(workflow1)).thenReturn(rep1);
        when(converter.unmarshallWorkflow(workflow2)).thenReturn(rep2);

        List<ConfigItem<WorkflowRepresentation>> load = service.load(new HashMap<String, String>());
        verify(converter).unmarshallWorkflow(workflow2);
        verify(converter).unmarshallWorkflow(workflow1);
        assertThat(load.size(), equalTo(2));
        assertThat(load.get(0).getContent(), sameInstance(rep1));
        assertThat(load.get(1).getContent(), sameInstance(rep2));
    }
}
