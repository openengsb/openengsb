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
package org.openengsb.ui.common.workflow;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class WorkflowPanelTest extends AbstractOpenEngSBTest {

    private WicketTester tester;
    private RuleManager ruleManager;
    private WorkflowService workflowService;
    private long id;

    @Before
    public void setup() throws Exception {
        tester = new WicketTester();
        ApplicationContextMock context = new ApplicationContextMock();
        PaxWicketSpringBeanComponentInjector defaultPaxWicketInjector =
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context);
        tester.getApplication().getComponentInstantiationListeners().add(defaultPaxWicketInjector);

        ruleManager = mock(RuleManager.class);
        Collection<RuleBaseElementId> value = new ArrayList<RuleBaseElementId>();
        value.add(new RuleBaseElementId(RuleBaseElementType.Process, "foo"));
        when(ruleManager.list(RuleBaseElementType.Process)).thenReturn(value);
        context.putBean("ruleManager", ruleManager);

        id = 0;
        workflowService = mock(WorkflowService.class);
        when(workflowService.startFlow(anyString())).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return id++;
            }
        });
        context.putBean("workflowService", workflowService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void startPanel_shouldShowWorkflowList() throws Exception {
        tester.startComponentInPage(WorkflowStartPanel.class);
        DropDownChoice<RuleBaseElementId> selectBox =
            (DropDownChoice<RuleBaseElementId>) tester.getComponentFromLastRenderedPage("startFlowForm:startFlowBox");
        List<RuleBaseElementId> choices = (List<RuleBaseElementId>) selectBox.getChoices();
        assertThat(choices, hasItem(new RuleBaseElementId(RuleBaseElementType.Process, "foo")));
    }

    @Test
    public void selectWorkflow_shouldStartWorkflow() throws Exception {
        tester.startComponentInPage(WorkflowStartPanel.class);
        FormTester formTester = tester.newFormTester("startFlowForm");
        formTester.select("startFlowBox", 0);
        tester.clickLink("startFlowForm:startFlowButton", true);
        verify(workflowService).startFlow("foo");
        tester.assertFeedback("startFlowForm:feedback", "workflow started with id " + (id - 1));
    }
}
