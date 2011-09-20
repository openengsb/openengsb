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

package org.openengsb.ui.admin.workflowEditor.event;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowConverter;
import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.WorkflowValidator;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.openengsb.ui.admin.model.OpenEngSBFallbackVersion;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;
import org.openengsb.ui.api.OpenEngSBVersionService;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class EditEventTest {

    private WicketTester tester;

    private FormTester formTester;

    private ApplicationContextMock context;

    private EventRepresentation event;

    private ActionRepresentation action;

    @Before
    public void setup() {
        action = new ActionRepresentation();
        action.setLocation("123");
        action.setDomain(NullDomain.class);
        action.setMethodName(NullDomain.class.getMethods()[0].getName());
        event = new EventRepresentation();
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(mock(ContextCurrentService.class));
        context.putBean("openengsbVersion", new OpenEngSBFallbackVersion());
        List<OpenEngSBVersionService> versionService = new ArrayList<OpenEngSBVersionService>();
        context.putBean("openengsbVersionService", versionService);
        context.putBean("workflowEditorService", mock(WorkflowEditorService.class));
        context.putBean(mock(WorkflowConverter.class));
        context.putBean(mock(RuleManager.class));
        context.putBean("validators", new ArrayList<WorkflowValidator>());
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
        tester.startPage(new EditEvent(event, action));
        formTester = tester.newFormTester("eventForm");
    }

    @Test
    public void selectEvent_ShouldSetEventOnWorkflowEvent() {
        assertThat(action.getEvents().size(), equalTo(0));
        formTester.select("eventSelect", 0);
        formTester.submit();
        tester.assertRenderedPage(WorkflowEditor.class);
        assertEquals(NullEvent.class, event.getEvent());
        assertThat(action.getEvents().size(), equalTo(1));
    }

    @Test
    public void cancelButton_shouldWhoWorkflowPage() {
        formTester.submit("cancel-button");
        assertThat(event.getActions().size(), equalTo(0));
        tester.assertRenderedPage(WorkflowEditor.class);
    }

}
