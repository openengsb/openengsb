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

package org.openengsb.ui.admin.workflowEditor.event;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.editor.Action;
import org.openengsb.core.common.workflow.editor.Event;
import org.openengsb.core.common.workflow.editor.WorkflowEditorService;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;

public class EditEventTest {

    private WicketTester tester;

    private FormTester formTester;

    private ApplicationContextMock mock;

    private Event event;

    @Before
    public void setup() {
        Action action = new Action();
        action.setLocation("123");
        action.setDomain(NullDomain.class);
        action.setMethodName(NullDomain.class.getMethods()[0].getName());
        event = new Event();
        tester = new WicketTester();
        mock = new ApplicationContextMock();
        mock.putBean(mock(ContextCurrentService.class));
        mock.putBean("openengsbVersion", new OpenEngSBVersion());
        mock.putBean("workflowEditorService", mock(WorkflowEditorService.class));
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), mock, true));
        tester.startPage(new EditEvent(event, action));
        formTester = tester.newFormTester("eventForm");
    }

    @Test
    public void selectEvent_ShouldSetEventOnWorkflowEvent() {
        formTester.select("eventSelect", 0);
        formTester.submit();
        tester.assertRenderedPage(WorkflowEditor.class);
        assertEquals(NullEvent.class, event.getEvent());
    }

}
