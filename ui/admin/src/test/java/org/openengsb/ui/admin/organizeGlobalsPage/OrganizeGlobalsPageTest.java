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

package org.openengsb.ui.admin.organizeGlobalsPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TreeMap;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.ui.admin.AbstractUITest;

public class OrganizeGlobalsPageTest extends AbstractUITest {
    private RuleManager ruleManager;
    private Map<String, String> globals;

    @Before
    public void init() throws RuleBaseException {
        ruleManager = mock(RuleManager.class);
        context.putBean(ruleManager);

        globals = new TreeMap<String, String>();
        globals.put("glob1", "aaaa.bbbb.ccc");
        globals.put("glob2", "aaaa.bbbb.ddd");
        globals.put("glob3", "aaaa.bbbb.eee");

        when(ruleManager.listGlobals()).thenReturn(globals);
        doThrow(new RuleBaseException()).when(ruleManager).addGlobal("test", "test");
        doThrow(new RuleBaseException()).when(ruleManager).removeGlobal("test");
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                globals.put((String) args[1], (String) args[0]);
                return null;
            }
        })
            .when(ruleManager).addGlobal("glob4", "aaaa.bbbb.fff");

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                globals.put((String) args[1], (String) args[0]);
                return null;
            }
        })
            .when(ruleManager).addGlobal("glob4", "aaaa.bbbb.ffff");

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                globals.remove("glob4");
                return null;
            }
        })
            .when(ruleManager).removeGlobal("glob4");

        tester.startPage(OrganizeGlobalsPage.class);
        verify(ruleManager).listGlobals();
    }

    @Test
    public void renderOrganizeGlobalsPage() throws Exception {
        tester.assertRenderedPage(OrganizeGlobalsPage.class);
        tester.assertComponent("tree", LinkTree.class);
        tester.assertComponent("editForm", Form.class);
        tester.assertComponent("editForm:globalName", TextField.class);
        tester.assertComponent("editForm:className", TextField.class);
        tester.assertComponent("editForm:submitButton", AjaxButton.class);
        tester.assertComponent("editForm:deleteButton", AjaxButton.class);

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(globals.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSelectGlobal() throws Exception {
        tester.clickLink("tree:i:2:nodeComponent:contentLink", true);
        TextField<String> globalName =
            (TextField<String>) tester.getComponentFromLastRenderedPage("editForm:globalName");
        assertTrue(globalName.isEnabled());
        assertEquals("glob2", globalName.getModelObject());
        TextField<String> className =
            (TextField<String>) tester.getComponentFromLastRenderedPage("editForm:className");
        assertTrue(className.isEnabled());
        assertEquals("aaaa.bbbb.ddd", className.getModelObject());
    }

    @Test
    public void testAddDoubleGlobal() throws Exception {
        FormTester formTester = tester.newFormTester("editForm");
        formTester.setValue("globalName", "test");
        formTester.setValue("className", "test");
        formTester.submit("submitButton");

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(globals.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }

    @Test
    public void testAddNewGlobal() throws Exception {
        FormTester formTester = tester.newFormTester("editForm");
        formTester.setValue("globalName", "glob4");
        formTester.setValue("className", "aaaa.bbbb.fff");
        formTester.submit("submitButton");

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(globals.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }

    @Test
    public void testDeleteGlobal() throws Exception {
        FormTester formTester = tester.newFormTester("editForm");
        formTester.setValue("globalName", "glob4");
        formTester.setValue("className", "aaaa.bbbb.fff");
        formTester.submit("deleteButton");

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(globals.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }

    @Test
    public void testDeleteNotExistingGlobal() throws Exception {
        FormTester formTester = tester.newFormTester("editForm");
        formTester.setValue("globalName", "test");
        formTester.setValue("className", "test");
        formTester.submit("deleteButton");

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(globals.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }



}
