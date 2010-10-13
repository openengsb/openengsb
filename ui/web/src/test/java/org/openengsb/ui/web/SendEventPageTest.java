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

package org.openengsb.ui.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.WorkflowException;
import org.openengsb.core.workflow.WorkflowService;
import org.openengsb.ui.web.editor.EditorPanel;

public class SendEventPageTest {

    private WicketTester tester;
    private EditorPanel editorPanel;
    private DropDownChoice<Class<?>> dropdown;
    private WorkflowService eventService;
    private List<Class<? extends Event>> eventClasses;
    private FormTester formTester;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        tester = new WicketTester();
        AnnotApplicationContextMock context = new AnnotApplicationContextMock();
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), context, false));
        eventService = mock(WorkflowService.class);
        RuleManager ruleManager = mock(RuleManager.class);
        context.putBean(ruleManager);
        context.putBean("eventService", eventService);
        context.putBean("domainService", mock(DomainService.class));
        context.putBean("contextCurrentService", mock(ContextCurrentService.class));
        eventClasses = Arrays.<Class<? extends Event>> asList(Dummy.class, Dummy2.class, BrokenEvent.class);
        tester.startPage(new SendEventPage(eventClasses));
        editorPanel = (EditorPanel) tester.getComponentFromLastRenderedPage("editor");
        dropdown = (DropDownChoice<Class<?>>) tester
                .getComponentFromLastRenderedPage("form:dropdown");
        formTester = tester.newFormTester("editor:form");
    }

    static class Dummy extends Event {

        private String testProperty;

        public String getTestProperty() {
            return testProperty;
        }

        public void setTestProperty(String testProperty) {
            this.testProperty = testProperty;
        }
    }

    static class Dummy2 extends Event {

        private String firstProperty;
        private String secondProperty;

        public String getFirstProperty() {
            return firstProperty;
        }

        public void setFirstProperty(String firstProperty) {
            this.firstProperty = firstProperty;
        }

        public String getSecondProperty() {
            return secondProperty;
        }

        public void setSecondProperty(String secondProperty) {
            this.secondProperty = secondProperty;
        }
    }

    static final class BrokenEvent extends Event {
        private BrokenEvent() {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testStandardPageComponents() throws Exception {
        tester.assertVisible("form:dropdown");
        tester.assertVisible("editor");
        assertThat(dropdown, notNullValue());
        assertThat(editorPanel, notNullValue());
    }

    @Test
    public void givenClassesInCtor_shouldAddThemToTheDropDown() {
        assertEquals(eventClasses.size(), dropdown.getChoices().size());
        assertEquals(Dummy.class, dropdown.getChoices().get(0));
        assertEquals("Dummy", dropdown.getValue());
        assertEquals(Dummy2.class, dropdown.getChoices().get(1));
    }

    @Test
    public void firstClassIsDefault_shouldCreateEditorFieldsBasedOnDefault() {
        final List<AttributeDefinition> attributes = editorPanel.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes.size(), 2);
        assertEquals(attributes.get(1).getName().getString(null), "testProperty");
    }

    @Test
    public void selectNewClassInDropDown_shouldRenderNewEditorPanelThroughAjax() {
        selectEventType(1);
        List<AttributeDefinition> attributes =
                ((EditorPanel) tester.getComponentFromLastRenderedPage("editor")).getAttributes();
        assertThat(attributes.size(), is(3));
        assertThat(attributes.get(1).getName().getString(null), is("firstProperty"));
    }

    @Test
    public void submittingForm_shouldCallDroolsServiceWithInstantiatedEvent() throws WorkflowException {
        formTester.setValue("fields:testProperty:row:field", "a");
        tester.debugComponentTrees();
        submitForm();
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventService).processEvent(captor.capture());
        assertThat(captor.getValue(), notNullValue());
        assertThat(captor.getValue(), is(Dummy.class));
        assertThat(((Dummy) captor.getValue()).getTestProperty(), is("a"));
    }

    private void submitForm() {
        tester.executeAjaxEvent("editor:form:submitButton", "onclick");
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
        formTester = tester.newFormTester("editor:form");
    }
}
