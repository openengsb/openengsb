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

package org.openengsb.ui.admin.workflowEditor;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowConverter;
import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.WorkflowValidator;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.test.NullDomain;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.workflowEditor.action.EditAction;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class EditActionTest extends AbstractUITest {

    private FormTester formTester;

    private ActionRepresentation action;

    private ActionRepresentation parent;

    @Before
    public void setup() {
        parent = new ActionRepresentation();
        action = new ActionRepresentation();
        tester = new WicketTester();
        context.putBean("workflowEditorService", mock(WorkflowEditorService.class));

        DomainProvider provider = mock(DomainProvider.class);
        when(provider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
                return NullDomain.class;
            }
        });
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("domain", "example");
        registerService(provider, props, DomainProvider.class);
        context.putBean(mock(WorkflowConverter.class));
        context.putBean(mock(RuleManager.class));
        context.putBean("validators", new ArrayList<WorkflowValidator>());
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
        tester.startPage(new EditAction(parent, action));
        formTester = tester.newFormTester("actionForm");
    }

    @Test
    public void editForm_shouldUpdateAction() {
        assertThat(parent.getActions().size(), equalTo(0));
        String locationName = "location";
        formTester.submit("submit-button");
        formTester = tester.newFormTester("actionForm");
        formTester.select("domainSelect", 0);
        formTester.submit("submit-button");
        formTester = tester.newFormTester("actionForm");
        formTester.select("methodSelect", 1);
        formTester.submit("submit-button");
        formTester = tester.newFormTester("actionForm");
        formTester.setValue(locationName, locationName);
        formTester.submit("submit-button");
        formTester = tester.newFormTester("actionForm");
        String code = "code";
        formTester.setValue(code, code);
        formTester.submit("submit-button");
        tester.assertRenderedPage(WorkflowEditor.class);
        assertThat(action.getLocation(), equalTo(locationName));
        assertEquals(action.getDomain(), NullDomain.class);
        assertThat(action.getMethodName(), equalTo(NullDomain.class.getMethods()[1].getName()));
        assertThat(action.getCode(), equalTo(code));
        assertThat(parent.getActions().size(), equalTo(1));
        assertThat(parent.getActions().get(0), sameInstance(action));
    }

    @Test
    public void testCallCreateTemplateButton_shouldSetCode() {
        String domain = "Domain has to be set";
        String method = "Method has to be set";
        String location = "Location has to be set";
        formTester.submit("create-template-code");
        tester.assertErrorMessages(new String[]{ domain, method, location });
        formTester = tester.newFormTester("actionForm");
        formTester.select("domainSelect", 0);
        formTester.submit("create-template-code");

        tester.assertErrorMessages(new String[]{ method, location });
        formTester = tester.newFormTester("actionForm");
        formTester.select("methodSelect", 2);
        formTester.submit("create-template-code");

        tester.assertErrorMessages(new String[]{ location });

        formTester = tester.newFormTester("actionForm");
        formTester.setValue("location", "location");
        formTester.submit("create-template-code");
        tester.assertErrorMessages(new String[]{});
        assertThat(action.getCode(), equalTo("location." + action.getMethodName() + "(" + Object.class.getName() + ", "
                + String.class.getName() + ");"));
    }

    @Test
    public void cancelButton_shouldWhoWorkflowPage() {
        formTester.submit("cancel-button");
        assertThat(parent.getActions().size(), equalTo(0));
        tester.assertRenderedPage(WorkflowEditor.class);
    }
}
