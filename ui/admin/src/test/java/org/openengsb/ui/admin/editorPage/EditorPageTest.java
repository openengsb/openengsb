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

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.PassThroughStringLocalizer;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.openengsb.ui.admin.testClient.TestClient;
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
        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }

    @Test
    public void testAttributesWithDefaultValues_shouldInitializeModelWithDefaults() {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        RepeatingView list =
            (RepeatingView) tester.getComponentFromLastRenderedPage("editor:form:attributesPanel:fields");
        @SuppressWarnings("unchecked")
        TextField<String> component = (TextField<String>) list.get("a:row:field");
        assertThat(component.getModelObject(), is("a_default"));
        tester.debugComponentTrees();
    }

    @Test
    public void testIfValuesOfAttributesAreShown_shouldShowAttributeValues() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("a", "testValue");
        String connectorId = serviceManager.create(
            new ConnectorDescription("testdomain", "testconnector", attributes, null));
        PageParameters pageParameters = new PageParameters();
        pageParameters.set("domainType", "testdomain");
        pageParameters.set("connectorType", "testconnector");
        pageParameters.set("id", connectorId);
        tester.startPage(ConnectorEditorPage.class, pageParameters);
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
    public void testAddProperty_shouldAddProperty() {
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
    public void testCreateService_shouldAddService() {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        tester.debugComponentTrees();
        newFormTester.submit("submitButton");
        tester.executeAjaxEvent("editor:form:submitButton", "onclick");
        Map<String, String> ref = new HashMap<String, String>();
        ref.put("a", "a_default");
        verify(factoryMock).applyAttributes(any(Connector.class), eq(ref));
        serviceUtils.getService(NullDomain.class, 100L);
    }
    
    @Test
    public void testCancelButton_shouldWork() {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        tester.debugComponentTrees();
        newFormTester.submit("cancelButton");
        tester.executeAjaxEvent("editor:form:cancelButton", "onclick");
        tester.assertRenderedPage(TestClient.class);
    }

    @Test
    public void testCreateServiceProperties_shouldRegisterWithProperties() {
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
    public void testCreateServicePropertiesLeaveFieldEmpty_shouldAddServiceProperty() {
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
    public void testEditService_shouldUpdateService() throws Exception {
        Map<String, Object> props = new Hashtable<String, Object>();
        props.put("test", "val");
        String id = serviceManager.create(new ConnectorDescription("testdomain", "testconnector", null, props));

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
        tester.executeAjaxEvent("editor:form:submitButton", "onclick");

        serviceUtils.getService("(newKey=new Value)", 100L);
        serviceUtils.getService("(test=42)", 100L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddServiceManagerValidationError_shouldPutErrorMessagesOnPage() {
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
    public void testUncheckValidationCheckbox_shouldBypassValidation() {
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
    public void testMultiValueServiceProperties_shouldAddFields() {
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
    public void testAddNewPropertyEntry_shouldResetKeyNameTextField() {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        FormTester newFormTester = tester.newFormTester("editor:form");
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        newFormTester.setValue("newPropertyKey", "testNew");
        tester.executeAjaxEvent(button, "onclick");
        assertThat(newFormTester.getTextComponentValue("newPropertyKey").isEmpty(), is(true));
    }

    @Test
    public void testAddPropertyWithoutName_shouldLeaveListUnchanged() {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        AjaxButton button = (AjaxButton) tester.getComponentFromLastRenderedPage("editor:form:addProperty");
        AbstractRepeater properties =
            (AbstractRepeater) tester.getComponentFromLastRenderedPage("editor:form:attributesPanel:properties");
        tester.executeAjaxEvent(button, "onclick");
        assertThat(properties.size(), is(0));
    }

    @Test
    public void testAddPropertyWithSameName_shouldLeaveListUnchanged() {
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
