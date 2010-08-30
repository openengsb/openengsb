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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.Page;

import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import org.openengsb.core.common.context.ContextCurrentService;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.openengsb.core.common.internal.ContextImpl;

public class ContextSetPageTest {

    private WicketTester tester;
    private ContextCurrentService contextService;
    private Page page;
    private TreeTable treeTable;
    private AjaxLink<String> expandAllLink;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        tester = new WicketTester();
        contextService = mock(ContextCurrentService.class);
        AnnotApplicationContextMock appContext = new AnnotApplicationContextMock();
        appContext.putBean(contextService);
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), appContext, false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_initialisation_with_simple_tree() {
        ContextImpl context= new ContextImpl();
        context.createChild("foo").createChild("bar").createChild("fox").put("fix", "fux");
        when(contextService.getContext()).thenReturn(context);
        page = tester.startPage(new ContextSetPage());
        tester.assertComponent("form:treeTable", TreeTable.class);
        treeTable = (TreeTable) tester.getComponentFromLastRenderedPage("form:treeTable");
        tester.assertComponent("expandAll", AjaxLink.class);
        expandAllLink = (AjaxLink<String>) tester.getComponentFromLastRenderedPage("expandAll");
        tester.debugComponentTrees();
        tester.executeAjaxEvent(expandAllLink, "onclick");

        testLabel("root",  "form:treeTable:i:0:sideColumns:0:nodeLink:label");
        testLabel("foo",  "form:treeTable:i:1:sideColumns:0:nodeLink:label");
        testLabel("bar",  "form:treeTable:i:2:sideColumns:0:nodeLink:label");
        testLabel("fox",  "form:treeTable:i:3:sideColumns:0:nodeLink:label");
        testLabel("fix",  "form:treeTable:i:4:sideColumns:0:nodeLink:label");
        //testTextField("fux", "form:treeTable:i:4");

    }

    private void testLabel(String lableText, String path) {
        tester.assertComponent(path, Label.class);
        Label labelroot = (Label) tester.getComponentFromLastRenderedPage(path);
        assertThat((String) labelroot.getDefaultModel().getObject(), is(lableText));
    }

    private void testTextField(String text, String path) {
        tester.assertComponent(path, TextField.class);
        @SuppressWarnings("unchecked")
        TextField<String> textfield = (TextField<String>) tester.getComponentFromLastRenderedPage(path);
        assertThat((String) textfield.getDefaultModel().getObject(), is(text));
    }


}
