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

import org.apache.wicket.Page;

import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;

import static org.mockito.Mockito.*;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextService;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.openengsb.core.common.internal.ContextImpl;

public class ContextSetPageTest {

    private WicketTester tester;
    private ContextService contextService;
    private Page page;
    private TreeTable treeTable;
    private AjaxLink<String> expandAllLink;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        tester = new WicketTester();
        AnnotApplicationContextMock appContext = new AnnotApplicationContextMock();
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), appContext, false));
        contextService = mock(ContextService.class);
        appContext.putBean("contextService", contextService);
        tester = new WicketTester();
    }

    @Test
    public void test_initialisation_with_simple_tree() {
        ContextImpl context= new ContextImpl();
        context.createChild("foo").createChild("bar").createChild("fox").put("fix", "fux");
        when(contextService.getContext()).thenReturn(context);
        page = tester.startPage(new ContextSetPage());
        tester.assertComponent("treeTable", TreeTable.class);
        treeTable = (TreeTable) tester.getComponentFromLastRenderedPage("treeTable");
        tester.assertComponent("expandAll", AjaxLink.class);
        expandAllLink = (AjaxLink<String>) tester.getComponentFromLastRenderedPage("expandAll");
        tester.executeAjaxEvent(expandAllLink, "onclick");
        tester.assertComponent("treeTable", TreeTable.class);
        treeTable = (TreeTable) tester.getComponentFromLastRenderedPage("treeTable");
        tester.dumpPage();


    }
}
