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

package org.openengsb.ui.admin.organizeImportsPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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

public class OrganizeImportsPageTest extends AbstractUITest {
    private RuleManager ruleManager;
    private List<String> imports;

    @Before
    public void init() throws RuleBaseException {
        ruleManager = mock(RuleManager.class);
        context.putBean(ruleManager);

        imports = new ArrayList<String>();
        imports.add("aaaa.bbbb.ccc");
        imports.add("aaaa.bbbb.ddd");
        imports.add("aaaa.bbbb.eee");

        when(ruleManager.listImports()).thenReturn(imports);
        doThrow(new RuleBaseException()).when(ruleManager).addImport("test");
        doThrow(new RuleBaseException()).when(ruleManager).removeImport("test");

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                imports.add((String) args[0]);
                return null;
            }
        })
            .when(ruleManager).addImport("aaaa.bbbb.fff");

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                imports.remove(invocation.getArguments()[0]);
                return null;
            }
        })
            .when(ruleManager).removeImport("glob4");

        tester.startPage(OrganizeImportsPage.class);
        verify(ruleManager).listImports();
    }

    @Test
    public void renderOrganizeImportsPage() throws Exception {
        tester.assertRenderedPage(OrganizeImportsPage.class);
        tester.assertComponent("tree", LinkTree.class);
        tester.assertComponent("editForm", Form.class);
        tester.assertComponent("editForm:importName", TextField.class);
        tester.assertComponent("editForm:submitButton", AjaxButton.class);
        tester.assertComponent("editForm:deleteButton", AjaxButton.class);

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(imports.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSelectImport() throws Exception {
        tester.clickLink("tree:i:1:nodeComponent:contentLink", true);
        TextField<String> importName =
            (TextField<String>) tester.getComponentFromLastRenderedPage("editForm:importName");
        assertTrue(importName.isEnabled());
        assertEquals("aaaa.bbbb.ccc", importName.getModelObject());
    }

    @Test
    public void testAddNewImport() throws Exception {
        FormTester formTester = tester.newFormTester("editForm");
        formTester.setValue("importName", "aaaa.bbbb.fff");
        formTester.submit("submitButton");

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(imports.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }

    @Test
    public void testDeleteImport() throws Exception {
        FormTester formTester = tester.newFormTester("editForm");
        formTester.setValue("importName", "aaaa.bbbb.fff");
        formTester.submit("deleteButton");

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(imports.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }

    @Test
    public void testDeleteNotExistingImport() throws Exception {
        FormTester formTester = tester.newFormTester("editForm");
        formTester.setValue("importName", "test");
        formTester.submit("deleteButton");

        LinkTree tree = (LinkTree) tester.getComponentFromLastRenderedPage("tree");
        assertEquals(imports.size(), tree.getModelObject().getChildCount(tree.getModelObject().getRoot()));
    }
}
