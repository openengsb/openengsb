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

package org.openengsb.ui.admin.editorPage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.PassThroughStringLocalizer;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class EditorPageTest extends AbstractUITest {

    private AttributeDefinition attrib1;
    private ConnectorInstanceFactory factoryMock;

    @Before
    public void setup() throws Exception {
        attrib1 =
            AttributeDefinition.builder(new PassThroughStringLocalizer()).id("a").defaultValue("a_default")
                .name("a_name").build();
        ServiceDescriptor d =
            ServiceDescriptor.builder(new PassThroughStringLocalizer()).implementationType(NullDomainImpl.class)
                .id("a").name("sn").description("sd").attribute(attrib1)
                .build();

        ConnectorProvider provider = createConnectorProviderMock("testconnector", "testdomain");
        when(provider.getDescriptor()).thenReturn(d);
        createDomainProviderMock(NullDomain.class, "testdomain");
        factoryMock = createFactoryMock("testconnector", NullDomainImpl.class, "testdomain");
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }

    @Test
    public void attributesWithDefaultValues_shouldInitializeModelWithDefaults() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        RepeatingView list =
            (RepeatingView) tester.getComponentFromLastRenderedPage("editor:form:attributesPanel:fields");
        @SuppressWarnings("unchecked")
        TextField<String> component = (TextField<String>) list.get("a:row:field");
        assertThat(component.getModelObject(), is("a_default"));
        tester.debugComponentTrees();
    }

    @Test
    public void testIfValuesOfAttributesAreShown() throws Exception {
        ConnectorId connectorId = ConnectorId.generate("testdomain", "testconnector");
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("a", "testValue");
        serviceManager.create(connectorId, new ConnectorDescription(attributes, null));
        PageParameters pageParams =
            new PageParameters("domainType=testdomain,connectorType=testconnector,id=" + connectorId.getInstanceId());
        tester.startPage(ConnectorEditorPage.class, pageParams);
        FormComponentLabel nameLabel =
            (FormComponentLabel) tester
                .getComponentFromLastRenderedPage("editor:form:attributesPanel:fields:a:row:name");
        assertThat(nameLabel.getDefaultModelObjectAsString(), is("a_name"));
        @SuppressWarnings("unchecked")
        TextField<String> value =
            (TextField<String>) tester
                .getComponentFromLastRenderedPage("editor:form:attributesPanel:fields:a:row:field");
        assertThat(value.getValue(), is("testValue"));
    }

    @Test
    public void testIdFieldIsEditable() {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        tester.debugComponentTrees();
        @SuppressWarnings("unchecked")
        TextField<String> idField =
            (TextField<String>) tester.getComponentFromLastRenderedPage("editor:form:serviceId");
        assertThat(idField.isEnabled(), is(true));
    }

    @Test
    public void testAddProperty() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        FormTester newFormTester = tester.newFormTester("editor:form");
        newFormTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(button, "onclick");
        Label propertyLabel =
            (Label) tester.getComponentFromLastRenderedPage("editor:form:attributesPanel:properties:0:key");
        assertThat(propertyLabel.getDefaultModelObjectAsString(), is("testNew"));
    }

    @Test
    public void testCreateService() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        tester.debugComponentTrees();
        newFormTester.submit("submitButton");
        tester.executeAjaxEvent("editor:form:submitButton", "onclick");
        Map<String, String> ref = new HashMap<String, String>();
        ref.put("a", "a_default");
        verify(factoryMock).applyAttributes(any(Domain.class), eq(ref));
        serviceUtils.getService(NullDomain.class, 100L);
    }

    @Test
    public void testCreateServiceProperties_shouldRegisterWithProperties() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        newFormTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(button, "onclick");
        tester.debugComponentTrees();
        tester.executeAjaxEvent("editor:form:attributesPanel:properties:0:values:1:value:label", "onclick");
        newFormTester.setValue("attributesPanel:properties:0:values:1:value:editor", "foo");
        tester.executeAjaxEvent("editor:form:submitButton", "onclick");

        serviceUtils.getService("(testNew=foo)", 100L);
    }

    @Test
    public void testCreateServicePropertiesLeaveFieldEmpty() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        newFormTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(button, "onclick");
        tester.debugComponentTrees();
        tester.executeAjaxEvent("editor:form:submitButton", "onclick");

        serviceUtils.getService(NullDomain.class, 100L);
    }

    @Test
    public void testEditService() throws Exception {
        ConnectorId id = ConnectorId.generate("testdomain", "testconnector");
        Map<String, Object> props = new Hashtable<String, Object>();
        props.put("test", "val");
        serviceManager.create(id, new ConnectorDescription(null, props));

        try {
            serviceUtils.getService("(test=val)", 100L);
        } catch (OsgiServiceNotAvailableException e) {
            fail("something is wrong, the servicemanager does not work properly");
        }

        tester.startPage(new ConnectorEditorPage(id));
        FormTester newFormTester = tester.newFormTester("editor:form");

        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        newFormTester.setValue("newPropertyKey", "newKey");
        tester.executeAjaxEvent(button, "onclick");
        tester.debugComponentTrees();
        tester.executeAjaxEvent("editor:form:attributesPanel:properties:1:values:1:value:label", "onclick");
        newFormTester.setValue("attributesPanel:properties:1:values:1:value:editor", "foo");
        tester.executeAjaxEvent("editor:form:attributesPanel:properties:2:values:1:value:label", "onclick");
        newFormTester.setValue("attributesPanel:properties:2:values:1:value:editor", "42");
        AjaxButton submitButton =
            (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:submitButton");
        tester.executeAjaxEvent(submitButton, "onclick");

        serviceUtils.getService("(newKey=foo)", 100L);
        serviceUtils.getService("(test=42)", 100L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addServiceManagerValidationError_ShouldPutErrorMessagesOnPage() {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("a", "Service Validation Error");
        when(factoryMock.getValidationErrors(anyMap())).thenReturn(errorMessages);

        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester formTester = tester.newFormTester("editor:form");
        formTester.setValue("attributesPanel:fields:a:row:field", "someValue");
        AjaxButton submitButton =
            (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:submitButton");
        tester.executeAjaxEvent(submitButton, "onclick");

        tester.assertErrorMessages(new String[]{ "a: Service Validation Error" });
        tester.assertRenderedPage(ConnectorEditorPage.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void uncheckValidationCheckbox_shouldBypassValidation() {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("a", "Service Validation Error");
        when(factoryMock.getValidationErrors(anyMap())).thenReturn(errorMessages);

        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester formTester = tester.newFormTester("editor:form");
        formTester.setValue("attributesPanel:fields:a:row:field", "someValue");
        tester.debugComponentTrees();
        formTester.setValue("attributesPanel:validate", false);
        AjaxButton submitButton =
            (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:submitButton");
        tester.executeAjaxEvent(submitButton, "onclick");

        tester.assertErrorMessages(new String[]{});
        serviceUtils.getService(NullDomain.class, 100L);
    }

    @Test
    public void testMultiValueServiceProperties_shouldAddFields() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        newFormTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(button, "onclick");
        tester.executeAjaxEvent("editor:form:attributesPanel:properties:0:values:1:value:label", "onclick");
        newFormTester.setValue("attributesPanel:properties:0:values:1:value:editor", "foo");
        tester.executeAjaxEvent("editor:form:attributesPanel:properties:0:newArrayEntry", "onclick");
        tester.executeAjaxEvent("editor:form:attributesPanel:properties:0:values:2:value:label", "onclick");
        newFormTester.setValue("attributesPanel:properties:0:values:2:value:editor", "bar");
        tester.executeAjaxEvent("editor:form:submitButton", "onclick");

        serviceUtils.getService("(testNew=bar)", 100L);
    }

    @Test
    public void testAddNewPropertyEntry_shouldResetKeyNameTextField() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        newFormTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(button, "onclick");
        assertThat(newFormTester.getTextComponentValue("newPropertyKey").isEmpty(), is(true));
    }

    @Test
    public void testAddPropertyWithoutName_shouldLeaveListUnchanged() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        AbstractRepeater properties =
            (AbstractRepeater) tester.getComponentFromLastRenderedPage("editor:form:attributesPanel:properties");
        tester.executeAjaxEvent(button, "onclick");
        assertThat(properties.size(), is(0));
    }

    // @SuppressWarnings("unchecked")
    // @Test
    // public void addServiceManagerValidationError_ShouldPutErrorMessagesOnPage() {
    // Map<String, String> errorMessages = new HashMap<String, String>();
    // errorMessages.put("a", "Service Validation Error");
    // when(factoryMock.getValidationErrors(anyMap())).thenReturn(errorMessages);
    //
    // tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
    // FormTester formTester = tester.newFormTester("editor:form");
    // formTester.setValue("attributesPanel:fields:a:row:field", "someValue");
    // AjaxButton submitButton =
    // (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:submitButton");
    // tester.executeAjaxEvent(submitButton, "onclick");
    //
    // tester.assertErrorMessages(new String[]{ "a: Service Validation Error" });
    // tester.assertRenderedPage(ConnectorEditorPage.class);
    // }

    @Test
    public void testAddPropertyWithSameName_shouldLeaveListUnchanged() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester formTester = tester.newFormTester("editor:form");
        formTester.setValue("attributesPanel:fields:a:row:field", "someValue");

        AjaxButton newPropertyButton = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");

        formTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(newPropertyButton, "onclick");

        tester.executeAjaxEvent("editor:form:attributesPanel:properties:0:values:1:value:label", "onclick");
        formTester.setValue("attributesPanel:properties:0:values:1:value:editor", "foo");

        tester.executeAjaxEvent("editor:form:attributesPanel:properties:0:newArrayEntry", "onclick");
        tester.executeAjaxEvent("editor:form:attributesPanel:properties:0:values:2:value:label", "onclick");
        formTester.setValue("attributesPanel:properties:0:values:2:value:editor", "bar");

        formTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(newPropertyButton, "onclick");

        AbstractRepeater list =
            (AbstractRepeater) tester
                .getComponentFromLastRenderedPage("editor:form:attributesPanel:properties:0:values");
        assertThat(list.size(), is(2));
    }

}
