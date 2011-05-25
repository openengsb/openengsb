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

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
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
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;

public class EditEventTest {

    private WicketTester tester;

    private FormTester formTester;

    private ApplicationContextMock mock;

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
        mock = new ApplicationContextMock();
        mock.putBean(mock(ContextCurrentService.class));
        mock.putBean("openengsbVersion", new OpenEngSBVersion());
        mock.putBean("workflowEditorService", mock(WorkflowEditorService.class));
        mock.putBean(mock(WorkflowConverter.class));
        mock.putBean(mock(RuleManager.class));
        mock.putBean("validators", new ArrayList<WorkflowValidator>());
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), mock, true));
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
