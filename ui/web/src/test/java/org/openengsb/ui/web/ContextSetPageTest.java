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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.internal.ContextImpl;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.ServiceReference;

public class ContextSetPageTest {

    private WicketTester tester;
    private ContextCurrentService contextService;
    private DomainService domainService;
    private ContextImpl context;

    @Before
    public void setup() {
        tester = new WicketTester();
        contextService = mock(ContextCurrentService.class);
        domainService = mock(DomainService.class);
        AnnotApplicationContextMock appContext = new AnnotApplicationContextMock();
        appContext.putBean(contextService);
        appContext.putBean(domainService);
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), appContext, false));
        context = new ContextImpl();
        context.createChild("a").createChild("b").createChild("c").put("d", "e");
        context.createChild("domains").createChild("domains.example").createChild("defaultConnector")
                .put("id", "blabla");
        when(contextService.getContext()).thenReturn(context);
        when(contextService.getValue("/a/b/c/d")).thenReturn("e");
        when(contextService.getValue("/domains/domains.example/defaultConnector/id")).thenReturn("blabla");
        when(contextService.getCurrentContextId()).thenReturn("foo");
        tester.startPage(new ContextSetPage());
    }

    @Test
    public void test_initialisation_with_simple_tree() {
        tester.assertComponent("form:treeTable", TreeTable.class);
        tester.assertComponent("expandAll", AjaxLink.class);
        // testLabel("foo", "form:treeTable:i:0:sideColumns:0:nodeLink:label");
        testLabel("a", "form:treeTable:i:1:sideColumns:0:nodeLink:label");
        testLabel("b", "form:treeTable:i:2:sideColumns:0:nodeLink:label");
        testLabel("c", "form:treeTable:i:3:sideColumns:0:nodeLink:label");
        testLabel("d", "form:treeTable:i:4:sideColumns:0:nodeLink:label");
        tester.assertComponent("form:path", TextField.class);
        tester.assertComponent("form:value", TextField.class);
        tester.assertComponent("form:save", AjaxButton.class);
    }

    @Test
    public void editAttribute_shouldReflectChangeInModel() {
        String textFieldId = "treeTable:i:4:sideColumns:1:textfield";
        String nodeLinkId = "form:treeTable:i:4:sideColumns:0:nodeLink";
        AjaxLink<?> node = (AjaxLink<?>) tester.getComponentFromLastRenderedPage(nodeLinkId);
        tester.executeAjaxEvent(node, "onclick");
        TextField<?> textField = (TextField<?>) tester.getComponentFromLastRenderedPage("form:" + textFieldId);
        assertThat(textField, notNullValue());
        assertThat((String) textField.getModel().getObject(), is("e"));
        assertThat(textField.isEnabled(), is(true));
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue(textFieldId, "a");
        tester.executeAjaxEvent(textField, "onblur");
        verify(contextService).putValue("/a/b/c/d", "a");
    }

    @Test
    public void editAttributeToEmtyString_shouldResultEmptyStringInModel() {
        String textFieldId = "treeTable:i:4:sideColumns:1:textfield";
        String nodeLinkId = "form:treeTable:i:4:sideColumns:0:nodeLink";
        AjaxLink<?> node = (AjaxLink<?>) tester.getComponentFromLastRenderedPage(nodeLinkId);
        tester.executeAjaxEvent(node, "onclick");
        TextField<?> textField = (TextField<?>) tester.getComponentFromLastRenderedPage("form:" + textFieldId);
        assertThat(textField, notNullValue());
        assertThat((String) textField.getModel().getObject(), is("e"));
        assertThat(textField.isEnabled(), is(true));
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue(textFieldId, "");
        tester.executeAjaxEvent(textField, "onblur");
        verify(contextService).putValue("/a/b/c/d", "");
    }

    private void testLabel(String lableText, String path) {
        tester.assertComponent(path, Label.class);
        Label labelroot = (Label) tester.getComponentFromLastRenderedPage(path);
        assertThat((String) labelroot.getDefaultModel().getObject(), is(lableText));
    }

    @Test
    public void idValueIsDropdown() {
        testLabel("domains", "form:treeTable:i:5:sideColumns:0:nodeLink:label");
        testLabel("domains.example", "form:treeTable:i:6:sideColumns:0:nodeLink:label");
        testLabel("defaultConnector", "form:treeTable:i:7:sideColumns:0:nodeLink:label");
        testLabel("id", "form:treeTable:i:8:sideColumns:0:nodeLink:label");

        String nodeLinkId = "form:treeTable:i:8:sideColumns:0:nodeLink";

        String textFieldId = "form:treeTable:i:8:sideColumns:1:textfield";
        AjaxLink<?> node = (AjaxLink<?>) tester.getComponentFromLastRenderedPage(nodeLinkId);
        tester.executeAjaxEvent(node, "onclick");

        tester.assertComponent(textFieldId, DropDownChoice.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void showChoicesInDefaultDomainDropDown() throws Exception {
        List<ServiceReference> serviceReferenceList = new ArrayList<ServiceReference>();
        ServiceReference serviceReference = mock(ServiceReference.class);
        serviceReferenceList.add(serviceReference);

        when(serviceReference.getProperty("id")).thenReturn("connectorService");
        when(serviceReference.getProperty("openengsb.service.type")).thenReturn("connector");

        List<DomainProvider> domainProviderList = new ArrayList<DomainProvider>();
        DomainProvider domainProvider = mock(DomainProvider.class);
        domainProviderList.add(domainProvider);

        when(domainProvider.getId()).thenReturn("domains.example");
        when(domainProvider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return Domain.class;
            }
        });
        when(domainService.domains()).thenReturn(domainProviderList);
        when(domainService.serviceReferencesForDomain(any(Class.class))).thenReturn(serviceReferenceList);

        String nodeLinkId = "form:treeTable:i:8:sideColumns:0:nodeLink";

        String textFieldId = "form:treeTable:i:8:sideColumns:1:textfield";
        AjaxLink<?> node = (AjaxLink<?>) tester.getComponentFromLastRenderedPage(nodeLinkId);
        tester.executeAjaxEvent(node, "onclick");

        DropDownChoice<String> connectorChoices = (DropDownChoice<String>) tester
                .getComponentFromLastRenderedPage(textFieldId);
        List<? extends String> choices = connectorChoices.getChoices();
        assertTrue(choices.contains("connectorService"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void showThatNotConnectorServicesAreNotInDefaultDomainDropDown() throws Exception {
        List<ServiceReference> serviceReferenceList = new ArrayList<ServiceReference>();
        ServiceReference serviceReference = mock(ServiceReference.class);
        serviceReferenceList.add(serviceReference);
        when(serviceReference.getProperty("id")).thenReturn("connectorService");
        when(serviceReference.getProperty("openengsb.service.type")).thenReturn("connector");

        List<ServiceReference> wrongServiceReferenceList = new ArrayList<ServiceReference>();
        ServiceReference wrongServiceReference = mock(ServiceReference.class);
        wrongServiceReferenceList.add(wrongServiceReference);

        when(wrongServiceReference.getProperty("id")).thenReturn("domainService");
        when(wrongServiceReference.getProperty("openengsb.service.type")).thenReturn("domain");

        List<DomainProvider> domainProviderList = new ArrayList<DomainProvider>();
        DomainProvider domainProvider = mock(DomainProvider.class);
        DomainProvider wrongDomainProvider = mock(DomainProvider.class);

        when(domainProvider.getId()).thenReturn("domains.example");
        when(wrongDomainProvider.getId()).thenReturn("domains.example");

        domainProviderList.add(domainProvider);
        when(domainProvider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return TestInterface.class;
            }
        });
        domainProviderList.add(wrongDomainProvider);
        when(wrongDomainProvider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return Domain.class;
            }
        });
        when(domainService.domains()).thenReturn(domainProviderList);
        when(domainService.serviceReferencesForDomain(TestInterface.class)).thenReturn(serviceReferenceList);
        when(domainService.serviceReferencesForDomain(Domain.class)).thenReturn(wrongServiceReferenceList);

        String nodeLinkId = "form:treeTable:i:8:sideColumns:0:nodeLink";
        String textFieldId = "form:treeTable:i:8:sideColumns:1:textfield";
        AjaxLink<?> node = (AjaxLink<?>) tester.getComponentFromLastRenderedPage(nodeLinkId);
        tester.executeAjaxEvent(node, "onclick");
        DropDownChoice<String> connectorChoices = (DropDownChoice<String>) tester
                .getComponentFromLastRenderedPage(textFieldId);
        List<? extends String> choices = connectorChoices.getChoices();

        assertTrue(choices.contains("connectorService"));
        assertFalse(choices.contains("noConnectorService"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void servicesOfTypeDomainShouldNotBeInInDefaultDomainDropDown() throws Exception {
        List<ServiceReference> serviceReferenceList = new ArrayList<ServiceReference>();
        ServiceReference serviceReference = mock(ServiceReference.class);
        serviceReferenceList.add(serviceReference);

        when(serviceReference.getProperty("id")).thenReturn("connectorService");
        when(serviceReference.getProperty("openengsb.service.type")).thenReturn("domain");

        List<DomainProvider> domainProviderList = new ArrayList<DomainProvider>();
        DomainProvider domainProvider = mock(DomainProvider.class);
        domainProviderList.add(domainProvider);

        when(domainProvider.getId()).thenReturn("domains.example");
        when(domainProvider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return Domain.class;
            }
        });
        when(domainService.domains()).thenReturn(domainProviderList);
        when(domainService.serviceReferencesForDomain(any(Class.class))).thenReturn(serviceReferenceList);

        String nodeLinkId = "form:treeTable:i:8:sideColumns:0:nodeLink";

        String textFieldId = "form:treeTable:i:8:sideColumns:1:textfield";
        AjaxLink<?> node = (AjaxLink<?>) tester.getComponentFromLastRenderedPage(nodeLinkId);
        tester.executeAjaxEvent(node, "onclick");

        DropDownChoice<String> connectorChoices = (DropDownChoice<String>) tester
                .getComponentFromLastRenderedPage(textFieldId);
        List<? extends String> choices = connectorChoices.getChoices();
        assertFalse(choices.contains("connectorService"));
    }

    public interface TestInterface extends Domain {

    }

    @Test
    public void enterCorrectNonExsistingPathAndValue_shouldCreateLeavNodeInContext() throws Exception {
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("path", "x/y/z");
        formTester.setValue("value", "testvalue");
        context.createChild("x").createChild("y").put("z", "test-value");
        tester.executeAjaxEvent("form:save", "onclick");
        verify(contextService).putValue("x/y/z", "testvalue");
        testLabel("x", "form:treeTable:i:18:sideColumns:0:nodeLink:label");
        testLabel("y", "form:treeTable:i:19:sideColumns:0:nodeLink:label");
        testLabel("z", "form:treeTable:i:20:sideColumns:0:nodeLink:label");
        TextField<?> pathField = (TextField<?>) tester.getComponentFromLastRenderedPage("form:path");
        TextField<?> valueField = (TextField<?>) tester.getComponentFromLastRenderedPage("form:value");
        assertThat(pathField.getModelObject(), nullValue());
        assertThat(valueField.getModelObject(), nullValue());
    }

    @Test
    public void enterNonLeafNodePath_shouldShowMessage() throws Exception {
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("path", "x/y/z");
        formTester.setValue("value", "testvalue");
        doThrow(new IllegalArgumentException("key identifies a path, put operation not allowed")).when(contextService)
                .putValue("x/y/z", "testvalue");
        tester.executeAjaxEvent("form:save", "onclick");
        verify(contextService).putValue("x/y/z", "testvalue");
        tester.assertErrorMessages(new String[]{ "key identifies a path, put operation not allowed" });
    }
}
