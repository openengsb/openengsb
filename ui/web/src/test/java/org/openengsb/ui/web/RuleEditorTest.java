/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.openengsb.ui.web;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.internal.RuleBaseException;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.ui.web.ruleeditor.RuleEditorPanel;

public class RuleEditorTest {
    private WicketTester tester;
    private RuleManager ruleManager;

    @Before
    public void init() throws RuleBaseException {
        tester = new WicketTester();
        AnnotApplicationContextMock appContext = new AnnotApplicationContextMock();
        ContextCurrentService contextService = mock(ContextCurrentService.class);
        appContext.putBean(contextService);
        ruleManager = mock(RuleManager.class);
        appContext.putBean(ruleManager);
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), appContext, false));
        RuleBaseElementId ruleBaseElementId = new RuleBaseElementId(RuleBaseElementType.Rule, "org.opentest", "test1");
        Collection<RuleBaseElementId> rules = Arrays.asList(ruleBaseElementId, new RuleBaseElementId(
                RuleBaseElementType.Rule, "org.opentest", "test2"));
        when(ruleManager.list(RuleBaseElementType.Rule)).thenReturn(rules);
        when(ruleManager.get(ruleBaseElementId)).thenReturn("testsource");
        tester.startPage(RuleEditorPage.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void renderEditorPage() throws Exception {
        tester.assertRenderedPage(RuleEditorPage.class);
        tester.assertComponent("ruleEditor", RuleEditorPanel.class);
        tester.assertComponent("ruleEditor:form", Form.class);
        tester.assertComponent("ruleEditor:form:typeChoice", DropDownChoice.class);
        tester.assertComponent("ruleEditor:form:ruleChoice", DropDownChoice.class);
        tester.assertComponent("ruleEditor:form:save", Button.class);
        tester.assertComponent("ruleEditor:form:cancel", Button.class);
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
    public void testCangeSelectedRule() throws Exception {
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
    
    @Test
    public void testSubmitChanges() throws Exception {
        
    }
}
