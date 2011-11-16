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

package org.openengsb.ui.admin.sendEventPage;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.test.NullEvent;
import org.openengsb.core.test.NullEvent2;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.ui.admin.AbstractUITest;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class SendEventPageTest extends AbstractUITest {

    private DropDownChoice<Class<?>> dropdown;
    private WorkflowService eventService;
    private List<Class<? extends Event>> eventClasses;
    private FormTester formTester;
    private RepeatingView fieldList;
    private AuditingDomain domain;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
        eventService = mock(WorkflowService.class);
        RuleManager ruleManager = mock(RuleManager.class);
        domain = mock(AuditingDomain.class);

        List<Event> allAudits = new ArrayList<Event>();
        Event event1 = new Event();
        event1.setName("123");
        event1.setProcessId(1L);
        Event event2 = new Event();
        event2.setName("456");
        event2.setProcessId(2L);
        allAudits.add(event1);
        allAudits.add(event2);

        Mockito.when(domain.getAllAudits()).thenReturn(allAudits);
        context.putBean(ruleManager);
        context.putBean("eventService", eventService);
        context.putBean("audit", domain);
        eventClasses = Arrays.<Class<? extends Event>> asList(NullEvent2.class, NullEvent.class, BrokenEvent.class);
        tester.startPage(new SendEventPage(eventClasses));
        fieldList = (RepeatingView) tester.getComponentFromLastRenderedPage("form:fieldContainer:fields");
        dropdown = (DropDownChoice<Class<?>>) tester.getComponentFromLastRenderedPage("form:dropdown");
        formTester = tester.newFormTester("form");
    }

    static final class BrokenEvent extends Event {
        private BrokenEvent() {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testStandardPageComponents() throws Exception {
        tester.assertVisible("form:dropdown");
        tester.assertVisible("form:fieldContainer:fields");
        assertThat(dropdown, notNullValue());
    }

    @Test
    public void givenClassesInCtor_shouldAddThemToTheDropDown() {
        assertEquals(eventClasses.size(), dropdown.getChoices().size());
        assertEquals(NullEvent2.class, dropdown.getChoices().get(0));
        assertEquals("NullEvent2", dropdown.getValue());
        assertEquals(NullEvent.class, dropdown.getChoices().get(1));
    }

    @Test
    public void firstClassIsDefault_shouldCreateEditorFieldsBasedOnDefault() {
        assertThat(fieldList.size(), is(4));
        Component attributeName = fieldList.get("testProperty:row:name");
        assertThat(attributeName.getDefaultModelObjectAsString(), is("testProperty"));
    }

    @Test
    public void selectNewClassInDropDown_shouldRenderNewEditorPanelThroughAjax() {
        selectEventType(1);
        fieldList = (RepeatingView) tester.getComponentFromLastRenderedPage("form:fieldContainer:fields");
        assertThat(fieldList.size(), is(3));
        Component attributeName = fieldList.get("name:row:name");
        assertThat(attributeName.getDefaultModelObjectAsString(), is("name"));
    }

    @Test
    public void submittingForm_shouldCallDroolsServiceWithInstantiatedEvent() throws WorkflowException {
        formTester.setValue("fieldContainer:fields:testProperty:row:field", "a");
        submitForm();
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventService).processEvent(captor.capture());
        assertThat(captor.getValue(), notNullValue());
        assertThat(captor.getValue(), is(NullEvent2.class));
        assertThat(((NullEvent2) captor.getValue()).getTestProperty(), is((Object) "a"));
    }

    private void submitForm() {
        tester.executeAjaxEvent("form:submitButton", "onclick");
    }

    @Test
    public void sendingEvent_shouldShowSuccessFeedback() throws Exception {
        submitForm();
        tester.assertNoErrorMessage();
        assertThat(tester.getMessages(FeedbackMessage.INFO).size(), is(1));
    }

    @Test
    public void buildingEventFails_shouldShowErrorFeedback() throws Exception {
        selectEventType(2);
        submitForm();
        tester.assertNoInfoMessage();
        assertThat(tester.getMessages(FeedbackMessage.ERROR).size(), is(1));
    }

    @Test
    public void processingEventthrowsException_shouldShowErrorFeedback() throws Exception {
        doThrow(new WorkflowException()).when(eventService).processEvent(Mockito.<Event> any());
        submitForm();
        tester.assertNoInfoMessage();
        assertThat(tester.getMessages(FeedbackMessage.ERROR).size(), is(1));
    }

    private void selectEventType(int idx) {
        FormTester typeFormTester = tester.newFormTester("form");
        typeFormTester.select("dropdown", idx);
        tester.executeAjaxEvent(dropdown, "onchange");
        formTester = tester.newFormTester("form");
    }

    @Test
    public void openSite_shouldShowAuditLog() {
        tester.assertVisible("auditsContainer:audits");
        tester.assertVisible("auditsContainer:audits:0:audit");
        tester.assertVisible("auditsContainer:audits:1:audit");
        int i = 0;
        for (Event event : domain.getAllAudits()) {
            tester.assertLabel("auditsContainer:audits:" + i + ":audit", event.getName());
            i++;
        }

    }
}
