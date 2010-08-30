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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.internal.ContextImpl;

public class ContextSetPageTest {

    private WicketTester tester;
    private ContextCurrentService contextService;

    @Before
    public void setup() {
        tester = new WicketTester();
        contextService = mock(ContextCurrentService.class);
        AnnotApplicationContextMock appContext = new AnnotApplicationContextMock();
        appContext.putBean(contextService);
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), appContext, false));
        ContextImpl context = new ContextImpl();
        context.createChild("foo").createChild("bar").createChild("fox").put("fix", "fux");
        when(contextService.getContext()).thenReturn(context);
        when(contextService.getValue("/foo/bar/fox/fix")).thenReturn("fux");
        tester.startPage(new ContextSetPage());
    }

    @Test
    public void test_initialisation_with_simple_tree() {
        tester.assertComponent("form:treeTable", TreeTable.class);
        tester.assertComponent("expandAll", AjaxLink.class);
        testLabel("/", "form:treeTable:i:0:sideColumns:0:nodeLink:label");
        testLabel("/foo/", "form:treeTable:i:1:sideColumns:0:nodeLink:label");
        testLabel("/foo/bar/", "form:treeTable:i:2:sideColumns:0:nodeLink:label");
        testLabel("/foo/bar/fox/", "form:treeTable:i:3:sideColumns:0:nodeLink:label");
        testLabel("/foo/bar/fox/fix", "form:treeTable:i:4:sideColumns:0:nodeLink:label");
    }

    @Test
    public void editAttribute_shouldReflectChangeInModel() {
        String textFieldId = "treeTable:i:4:sideColumns:1:textfield";
        AjaxLink<?> node = (AjaxLink<?>) tester
                .getComponentFromLastRenderedPage("form:treeTable:i:4:sideColumns:0:nodeLink");
        tester.executeAjaxEvent(node, "onclick");
        TextField<?> textField = (TextField<?>) tester.getComponentFromLastRenderedPage("form:" + textFieldId);
        assertThat(textField, notNullValue());
        assertThat((String) textField.getModel().getObject(), is("fux"));
        assertThat(textField.isEnabled(), is(true));
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue(textFieldId, "a");
        node = (AjaxLink<?>) tester.getComponentFromLastRenderedPage("form:treeTable:i:4:sideColumns:0:nodeLink");
        tester.executeAjaxEvent(textField, "onblur");
        verify(contextService).putValue("/foo/bar/fox/fix", "a");
    }

    private void testLabel(String lableText, String path) {
        tester.assertComponent(path, Label.class);
        Label labelroot = (Label) tester.getComponentFromLastRenderedPage(path);
        assertThat((String) labelroot.getDefaultModel().getObject(), is(lableText));
    }
}
