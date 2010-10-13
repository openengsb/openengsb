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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.ui.web.ruleeditor.RuleEditorPanel;

public class RuleEditorTest {
    private WicketTester tester;
    private RuleManager ruleManager;
    private RuleBaseElementId ruleBaseElementId;

    @Before
    public void init() throws RuleBaseException {
        tester = new WicketTester();
        AnnotApplicationContextMock appContext = new AnnotApplicationContextMock();
        ContextCurrentService contextService = mock(ContextCurrentService.class);
        appContext.putBean(contextService);
        ruleManager = mock(RuleManager.class);
        appContext.putBean(ruleManager);
        tester.getApplication()
            .addComponentInstantiationListener(new SpringComponentInjector(tester.getApplication(), appContext, false));
        ruleBaseElementId = new RuleBaseElementId(RuleBaseElementType.Rule, "org.opentest", "test1");
        Collection<RuleBaseElementId> rules = Arrays
            .asList(ruleBaseElementId, new RuleBaseElementId(RuleBaseElementType.Rule, "org.opentest", "test2"));
        when(ruleManager.list(RuleBaseElementType.Rule)).thenReturn(rules);
        when(ruleManager.get(ruleBaseElementId)).thenReturn("testsource");
        tester.startPage(RuleEditorPage.class);
        verify(ruleManager).list(RuleBaseElementType.Rule);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void renderEditorPage() throws Exception {
        tester.assertRenderedPage(RuleEditorPage.class);
        tester.assertComponent("ruleEditor", RuleEditorPanel.class);
        tester.assertComponent("ruleEditor:form", Form.class);
        tester.assertComponent("ruleEditor:form:typeChoice", DropDownChoice.class);
        tester.assertComponent("ruleEditor:form:ruleChoice", DropDownChoice.class);
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:ruleChoice").isVisible());
        tester.assertComponent("ruleEditor:form:save", AjaxButton.class);
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());
        // tester.assertComponent("ruleEditor:form:ruleName", TextField.class);
        // assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:ruleName").isVisible());
        tester.assertComponent("ruleEditor:form:cancel", AjaxButton.class);
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        tester.assertComponent("ruleEditor:form:new", AjaxButton.class);
        tester.assertComponent("ruleEditor:form:text", TextArea.class);

        DropDownChoice<RuleBaseElementId> typeDropDown = (DropDownChoice<RuleBaseElementId>) tester
            .getComponentFromLastRenderedPage("ruleEditor:form:typeChoice");
        assertEquals(RuleBaseElementType.Rule, typeDropDown.getModelObject());
        tester.assertComponent("ruleEditor:form:ruleChoice", DropDownChoice.class);

        DropDownChoice<RuleBaseElementId> ruleDropDown = (DropDownChoice<RuleBaseElementId>) tester
            .getComponentFromLastRenderedPage("ruleEditor:form:ruleChoice");
        assertNotNull(ruleDropDown.getModel());
        assertThat(ruleDropDown.getModelObject(), nullValue());

        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertFalse(textArea.isEnabled());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testChangeSelectedRule() throws Exception {
        FormTester formTester = tester.newFormTester("ruleEditor:form");
        formTester.select("ruleChoice", 0);
        tester.executeAjaxEvent("ruleEditor:form:ruleChoice", "onchange");
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("testsource", textArea.getModelObject());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testChangeSelectedRuleType() throws Exception {
        FormTester formTester = tester.newFormTester("ruleEditor:form");

        formTester.select("typeChoice", 1);
        tester.executeAjaxEvent("ruleEditor:form:typeChoice", "onchange");

        DropDownChoice<RuleBaseElementId> typeDropDown = (DropDownChoice<RuleBaseElementId>) tester
            .getComponentFromLastRenderedPage("ruleEditor:form:typeChoice");
        assertEquals(RuleBaseElementType.Function, typeDropDown.getModelObject());

        DropDownChoice<RuleBaseElementId> ruleDropDown = (DropDownChoice<RuleBaseElementId>) tester
            .getComponentFromLastRenderedPage("ruleEditor:form:ruleChoice");
        assertNotNull(ruleDropDown.getModel());
        assertThat(ruleDropDown.getModelObject(), nullValue());

        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertFalse(textArea.isEnabled());
        assertNotNull(textArea.getModel());
        assertEquals(null, textArea.getModelObject());
        verify(ruleManager).list(RuleBaseElementType.Rule);
        verify(ruleManager).list(RuleBaseElementType.Function);
    }

    @SuppressWarnings("unchecked")
    private void enterText() {
        FormTester formTester = tester.newFormTester("ruleEditor:form");
        formTester.select("ruleChoice", 0);
        tester.executeAjaxEvent("ruleEditor:form:ruleChoice", "onchange");
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("testsource", textArea.getModelObject());

        formTester.setValue("text", "modified source");
        tester.executeAjaxEvent(textArea, "onchange");
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSubmitChanges() throws Exception {
        enterText();
        tester.executeAjaxEvent("ruleEditor:form:save", "onclick");

        verify(ruleManager).update(ruleBaseElementId, "modified source");
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("modified source", textArea.getModelObject());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testClickCancleButton() throws Exception {
        enterText();
        tester.executeAjaxEvent("ruleEditor:form:cancel", "onclick");

        verify(ruleManager, times(2)).get(ruleBaseElementId);
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("testsource", textArea.getModelObject());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSubmitChanges_withErrors() throws Exception {
        doThrow(new RuleBaseException("error")).when(ruleManager).update((RuleBaseElementId) anyObject(), anyString());

        enterText();
        tester.executeAjaxEvent("ruleEditor:form:save", "onclick");

        verify(ruleManager, times(1)).update(ruleBaseElementId, "modified source");
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("modified source", textArea.getModelObject());
        tester.assertErrorMessages(new String[]{ "error" });
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());

        tester.assertComponent("ruleEditor:feedback", FeedbackPanel.class);
        FeedbackPanel feedbackPanel = (FeedbackPanel) tester.getComponentFromLastRenderedPage("ruleEditor:feedback");
        assertTrue(feedbackPanel.isVisible());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testButtonStateOnSecondChange() throws Exception {
        enterText();
        FormTester formTester = tester.newFormTester("ruleEditor:form");
        formTester.select("ruleChoice", 0);
        tester.executeAjaxEvent("ruleEditor:form:ruleChoice", "onchange");
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("testsource", textArea.getModelObject());

        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateNewRule() throws Exception {
        tester.executeAjaxEvent("ruleEditor:form:new", "onclick");
        TextField<String> rulename = (TextField<String>) tester
            .getComponentFromLastRenderedPage("ruleEditor:form:ruleName");
        assertEquals("rulename", rulename.getModelObject());
        assertTrue(rulename.isVisible());

        FormTester formTester = tester.newFormTester("ruleEditor:form");
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("", textArea.getModelObject());
        formTester.setValue("text", "new rule source");
        tester.executeAjaxEvent(textArea, "onchange");
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:new").isEnabled());

        tester.executeAjaxEvent("ruleEditor:form:save", "onclick");

        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:new").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:ruleChoice").isVisible());
        tester.assertComponent("ruleEditor:form:ruleChoice", DropDownChoice.class);

        assertEquals(RuleBaseElementType.Rule, ((DropDownChoice<RuleBaseElementType>) tester
            .getComponentFromLastRenderedPage("ruleEditor:form:typeChoice")).getDefaultModelObject());
        RuleBaseElementId ruleBaseElementId = new RuleBaseElementId(RuleBaseElementType.Rule, "rulename");
        verify(ruleManager).add(ruleBaseElementId, "new rule source");
        verify(ruleManager, times(2)).list(RuleBaseElementType.Rule);
        assertEquals(ruleBaseElementId,
            tester.getComponentFromLastRenderedPage("ruleEditor:form:ruleChoice").getDefaultModelObject());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSubmitNew_withErrors() throws Exception {
        doThrow(new RuleBaseException("error")).when(ruleManager).add((RuleBaseElementId) anyObject(), anyString());
        tester.executeAjaxEvent("ruleEditor:form:new", "onclick");
        TextField<String> rulename = (TextField<String>) tester
            .getComponentFromLastRenderedPage("ruleEditor:form:ruleName");
        assertEquals("rulename", rulename.getModelObject());
        assertTrue(rulename.isVisible());

        FormTester formTester = tester.newFormTester("ruleEditor:form");
        TextArea<String> textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("", textArea.getModelObject());
        formTester.setValue("text", "new rule source");
        tester.executeAjaxEvent(textArea, "onchange");
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:new").isEnabled());
        tester.executeAjaxEvent("ruleEditor:form:save", "onclick");

        verify(ruleManager, times(1))
            .add(new RuleBaseElementId(RuleBaseElementType.Rule, "rulename"), "new rule source");
        textArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("ruleEditor:form:text");
        assertTrue(textArea.isEnabled());
        assertEquals("new rule source", textArea.getModelObject());
        tester.assertErrorMessages(new String[]{ "error" });
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:cancel").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("ruleEditor:form:save").isEnabled());

        tester.assertComponent("ruleEditor:feedback", FeedbackPanel.class);
        FeedbackPanel feedbackPanel = (FeedbackPanel) tester.getComponentFromLastRenderedPage("ruleEditor:feedback");
        assertTrue(feedbackPanel.isVisible());
    }
}
