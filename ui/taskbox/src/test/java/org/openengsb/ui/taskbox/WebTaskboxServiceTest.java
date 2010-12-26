/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.taskbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.ui.taskbox.web.CustomTaskPanel;
import org.openengsb.ui.taskbox.web.TaskPanel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class WebTaskboxServiceTest {
    private WebTaskboxServiceImpl service;
    private PersistenceService persistenceService;
    private WorkflowService workflowService;
    
    @Before
    public void init() throws Exception {
        workflowService = mock(WorkflowService.class);
        persistenceService = mock(PersistenceService.class);
        PersistenceManager persistenceManager = mock(PersistenceManager.class);
        when(persistenceManager.getPersistenceForBundle(any(Bundle.class))).thenReturn(persistenceService);

        service = new WebTaskboxServiceImpl();
        service.setBundleContext(mock(BundleContext.class));
        service.setWorkflowService(workflowService);
        service.setPersistenceManager(persistenceManager);
        service.init();
    }
    
    @Test
    public void testGetTaskPanel_shouldReturnDefaultPanel() throws PersistenceException {
        Task t = new Task();
        t.setTaskType("Type1");
        Panel p = null;
        
        try {
            p = service.getTaskPanel(t, "panel");
        } catch (TaskboxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(p.getClass(), TaskPanel.class);
    }
    
    @Test
    public void testGetRegisteredTaskPanel_shouldReturnCustomPanel() throws PersistenceException {
        Task t = new Task();
        t.setTaskType("Type1");
        Panel p = null;
        
        try {
            service.registerTaskPanel(t.getTaskType(), CustomTaskPanel.class);
            p = service.getTaskPanel(t, "panel");
        } catch (TaskboxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(p.getClass(), CustomTaskPanel.class);
    }
}
