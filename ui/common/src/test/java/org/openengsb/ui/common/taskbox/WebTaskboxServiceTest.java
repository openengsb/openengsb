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

package org.openengsb.ui.common.taskbox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.model.Task;
import org.openengsb.core.workflow.internal.TaskboxServiceImpl;
import org.openengsb.ui.common.taskbox.web.CustomTaskPanel;
import org.openengsb.ui.common.taskbox.web.TaskPanel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class WebTaskboxServiceTest {
    private WebTaskboxServiceImpl service;
    private PersistenceService persistenceService;

    @SuppressWarnings("unused")
    private WicketTester tester;

    @Before
    public void init() throws Exception {
        tester = new WicketTester();

        BundleContext bundleContextMock = mock(BundleContext.class);
        Bundle bundleMock = mock(Bundle.class);
        when(bundleContextMock.getBundle()).thenReturn(bundleMock);
        TaskboxServiceImpl workflow = new TaskboxServiceImpl();
        persistenceService = mock(PersistenceService.class);
        PersistenceManager persistenceManager = mock(PersistenceManager.class);
        when(persistenceManager.getPersistenceForBundle(any(Bundle.class))).thenReturn(persistenceService);
        workflow.setPersistenceManager(persistenceManager);
        workflow.setBundleContext(bundleContextMock);
        workflow.init();

        service = new WebTaskboxServiceImpl();
        service.setTaskboxService(workflow);
        service.setBundleContext(bundleContextMock);
        service.setPersistenceManager(persistenceManager);
        service.init();
    }

    @Test
    public void testGetTaskPanel_shouldReturnDefaultPanel() throws Exception {
        Task t = new Task();
        t.setTaskType("Type1");
        Panel p = service.getTaskPanel(t, "panel");
        assertEquals(p.getClass(), TaskPanel.class);
    }

    @Test
    public void testGetRegisteredTaskPanel_shouldReturnCustomPanel() throws Exception {
        List<PanelRegistryEntry> list = new ArrayList<PanelRegistryEntry>();
        list.add(new PanelRegistryEntry("Type1", CustomTaskPanel.class));

        when(persistenceService.query(any(PanelRegistryEntry.class))).thenReturn(list);

        Task t = new Task();
        t.setTaskType("Type1");
        Panel p = service.getTaskPanel(t, "panel");
        assertEquals(p.getClass(), CustomTaskPanel.class);
    }
}
